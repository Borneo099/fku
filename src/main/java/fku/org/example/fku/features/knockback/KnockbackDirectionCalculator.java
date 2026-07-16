package fku.org.example.fku.features.knockback; /* water */

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * KnockbackDirectionCalculator — 击退方向计算
 *
 * ★ 职责：
 *   根据当前模式计算目标假旋转 Yaw，使服务端将目标击退到指定方向。
 *
 * ==========================================
 * ★ 核心公式推导（主要矛盾 — 看过源码再说话）
 * ==========================================
 * Minecraft 击退方向由两种机制叠加：
 *   ① Yaw 击退（sprint/hurt）：push(-sin(yaw), 0, cos(yaw))
 *   ② 位置向量击退（hurt）    ：push(targetX - attackerX, 0, targetZ - attackerZ)
 *
 * 想让击退方向 = 期望向量 (wantedX, wantedZ)：
 *   (-sin(yaw), cos(yaw)) ∝ (wantedX, wantedZ)
 *   → sin(yaw) = -k * wantedX, cos(yaw) = k * wantedZ (k > 0)
 *   → yaw = atan2(sin(yaw), cos(yaw)) = atan2(-wantedX, wantedZ)
 *
 * ★ 旧方案错误根因：
 *   之前所有模式用 atan2(dz, dx) - 180，导致方向偏转 90°，推出侧向击退。
 *   悬崖模式只向下扫 5 格 + 遇到非空气直接 break，大面积漏检。
 *
 * ★ 参考：net.minecraft.world.entity.LivingEntity#hurt() 源码
 */
@OnlyIn(Dist.CLIENT)
public class KnockbackDirectionCalculator {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * 计算目标 Yaw（水平旋转角）
     *
     * @param player 攻击者（玩家）
     * @param target 被攻击者
     * @param mode   击退模式
     * @return 目标 Yaw 角度（度）
     */
    public static float calculateYaw(LivingEntity player, LivingEntity target, String mode) {
        KnockbackConfig config = KnockbackConfig.getInstance();

        switch (mode) {
            case "PULLBACK":
                // 把目标向玩家方向拉
                return calculatePullbackYaw(player, target);

            case "PUSHBACK":
                // 把目标推离玩家
                return calculatePushbackYaw(player, target);

            case "CLIFF":
                // 向最近的悬崖/虚空方向推
                return calculateCliffYaw(player, target, config.cliffSearchRadius);

            case "CUSTOM":
                // ★ 自定义模式：基于玩家当前视角叠加配置角度
                //   如玩家朝北(Yaw=0°)、配置90° → 实际击退方向为90°（右侧击退）
                //   如玩家朝东(Yaw=90°)、配置90° → 实际击退方向为180°（后方击退）
                return (player.getYRot() + config.customYaw) % 360;

            default:
                return player.getYRot();
        }
    }

    /**
     * 拉回模式：目标向玩家击退
     * wanted = (playerX - targetX, playerZ - targetZ)
     * yaw = atan2(targetX - playerX, playerZ - targetZ)
     */
    private static float calculatePullbackYaw(LivingEntity player, LivingEntity target) {
        double wx = player.getX() - target.getX();
        double wz = player.getZ() - target.getZ();
        // yaw = atan2(-wx, wz) = atan2(targetX - playerX, playerZ - targetZ)
        return (float) Math.toDegrees(Math.atan2(-wx, wz));
    }

    /**
     * 推离模式：目标远离玩家击退
     * wanted = (targetX - playerX, targetZ - playerZ)
     * yaw = atan2(playerX - targetX, targetZ - playerZ)
     */
    private static float calculatePushbackYaw(LivingEntity player, LivingEntity target) {
        double wx = target.getX() - player.getX();
        double wz = target.getZ() - player.getZ();
        // yaw = atan2(-wx, wz) = atan2(playerX - targetX, targetZ - playerZ)
        return (float) Math.toDegrees(Math.atan2(-wx, wz));
    }

    /**
     * 悬崖模式：扫描目标周围地形，找到最近的悬崖/虚空方向。
     *
     * 检测逻辑：
     *   1. 对每个水平偏移 (dx, dz)，从脚部往下全量扫描到基岩
     *   2. 找到「最后一块非空气方块」，如果其下方是空气（gap >= 2）或虚空 ≡ 悬崖
     *   3. 悬崖方向 yaw = atan2(-dx, dz)
     *
     * 旧方案失败原因：
     *   - 只向下扫 5 格 → 漏检大部分悬崖
     *   - 遇到非空气方块 break → 早期终止，下方悬崖全错过
     *   - yaw 公式错误 → 方向偏 90°
     */
    private static float calculateCliffYaw(LivingEntity player, LivingEntity target, int radius) {
        Level level = mc.level;
        if (level == null) return player.getYRot();

        int tx = (int) Math.floor(target.getX());
        int tz = (int) Math.floor(target.getZ());
        int ty = (int) Math.floor(target.getY());
        int minY = level.getMinBuildHeight();

        double bestDist = Double.MAX_VALUE;
        float bestAngle = player.getYRot();

        // ★ 扫描周围水平位置
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx == 0 && dz == 0) continue;

                int bx = tx + dx;
                int bz = tz + dz;

                // 从脚部往下全量扫描到基岩，找到「最后一块非空气方块」
                int lastSolidY = Integer.MIN_VALUE;
                boolean hasCliff = false;

                for (int y = ty; y >= minY; y--) {
                    BlockPos checkPos = new BlockPos(bx, y, bz);
                    BlockState state = level.getBlockState(checkPos);

                    if (y == minY && state.isAir()) {
                        // ★ 虚空直达：从目标脚部到基岩全都是空气
                        hasCliff = true;
                        break;
                    }

                    if (state.isAir()) {
                        if (lastSolidY != Integer.MIN_VALUE) {
                            // ★ 在非空气方块下方发现了空气 → 悬崖边缘
                            int gap = lastSolidY - y;
                            if (gap >= 2) { // >= 2 格落差才视为悬崖（排除 1 格台阶）
                                hasCliff = true;
                                break;
                            }
                        }
                    } else {
                        lastSolidY = y; // 记录非空气方块
                    }
                }

                if (hasCliff) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist < bestDist) {
                        bestDist = dist;
                        // ★ 正确 yaw：atan2(-dx, dz)
                        //   期望方向 (dx, dz) = 从目标到悬崖
                        double angleDeg = Math.toDegrees(Math.atan2(-dx, dz));
                        if (angleDeg < 0) angleDeg += 360;
                        bestAngle = (float) angleDeg;
                    }
                }
            }
        }

        return bestAngle;
    }

    /**
     * 计算目标 Pitch（垂直旋转角）
     * 击退方向主要由 Yaw 决定，Pitch 保持玩家当前值
     */
    public static float calculatePitch(LivingEntity player) {
        return player.getXRot();
    }
}