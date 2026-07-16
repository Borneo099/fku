package fku.org.example.fku.features.knockback; /* water */

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * FakeRotationManager — 假旋转 + 假位置管理
 *
 * ★ 职责：
 *   在攻击前发送带假位置+假旋转的 PosRot 包到服务端，使 BOTH 击退机制生效：
 *     1.「位置向量击退」（LivingEntity#hurt() 中的 attackerPos - targetPos）：通过偏移玩家假位置控制
 *     2.「Yaw 击退」（Player#attack() 冲刺攻击）：通过假 Yaw 控制
 *   攻击完成后延迟 N Tick 发送恢复包。
 *
 * ★ 原理（抓到主要矛盾）：
 *   旧方案只发假旋转 → 只影响冲刺攻击（Yaw 击退），普通攻击走位置向量完全无效果。
 *   新方案同时发假位置 + 假旋转 → 两种击退机制都走同一方向，对所有攻击类型 100% 生效。
 *
 * ★ 参考：
 *   Litematica Printer 的 Fake Rotation System 设计思路（旋转部分）
 *   位置偏移算法基于 Minecraft 击退机制反推（攻击者位置 - 目标位置）方向
 */
@OnlyIn(Dist.CLIENT)
public class FakeRotationManager {

    private static final Minecraft mc = Minecraft.getInstance();

    /** 假位置偏移量（方块），足够大以覆盖原始位置向量，小到不触发反作弊 */
    private static final double POS_OFFSET = 0.3;

    /** 是否正在使用假旋转 */
    private static boolean active = false;

    /** 原始位置和旋转 */
    private static double origX, origY, origZ;
    private static float origYaw, origPitch;

    /** 恢复计时器（Tick 数），倒计时到 0 时发恢复包 */
    private static int restoreTimer = 0;

    /** 恢复需要等待的 Tick 数（给服务端足够时间处理攻击包） */
    private static final int RESTORE_DELAY_TICKS = 2;

    // ════════════════════════════════════════════════
    // ★ pending 待发送状态（用于 Connection.send 层拦截）
    //   MixinMultiPlayerGameMode 调用 setPending() 计算但不发送，
    //   MixinConnectionAttackInterceptor 在攻击包前通过 channel 直发 PosRot。
    // ════════════════════════════════════════════════
    /** 是否有待发送的假旋转包 */
    private static boolean pending = false;
    private static double pendingX, pendingY, pendingZ;
    private static float pendingYaw, pendingPitch;
    private static boolean pendingOnGround;

    /**
     * 设置待发送状态（只计算不发送）
     * 由 MixinMultiPlayerGameMode.onAttackHead() 调用
     */
    public static void setPending(LivingEntity target, float targetYaw) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!active) {
            // ★ 首次启用：记录原始位置和旋转
            origX = player.getX();
            origY = player.getY();
            origZ = player.getZ();
            origYaw = player.getYRot();
            origPitch = player.getXRot();
            active = true;
        }

        // ★ 计算假位置（同 enable() 中的公式）
        float yawRad = (float) Math.toRadians(targetYaw);
        pendingX = target.getX() + POS_OFFSET * Math.sin(yawRad);
        pendingZ = target.getZ() - POS_OFFSET * Math.cos(yawRad);
        pendingY = player.getY();
        pendingYaw = targetYaw;
        pendingPitch = player.getXRot();
        pendingOnGround = player.onGround();
        pending = true;
    }

    /** 是否有待发送的假旋转包 */
    public static boolean hasPending() { return pending; }

    /** 清除待发送状态 */
    public static void clearPending() { pending = false; }

    /**
     * 创建待发送的 PosRot 包
     * 由 MixinConnectionAttackInterceptor 在 Connection.send() 处调用
     */
    public static ServerboundMovePlayerPacket.PosRot createPendingPacket() {
        return new ServerboundMovePlayerPacket.PosRot(
                pendingX, pendingY, pendingZ, pendingYaw, pendingPitch, pendingOnGround);
    }

    /**
     * 启用假旋转 + 假位置，发送 PosRot 包到服务端
     *
     * @param target    被攻击的实体（计算位置偏移用）
     * @param targetYaw 目标假 Yaw（度）
     */
    public static void enable(LivingEntity target, float targetYaw) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();

        if (!active) {
            // ★ 首次启用：记录原始位置和旋转
            origX = playerPos.x;
            origY = playerPos.y;
            origZ = playerPos.z;
            origYaw = player.getYRot();
            origPitch = player.getXRot();
            active = true;
        }

        // ★ 计算假位置：使位置向量击退与 Yaw 击退方向一致
        //   位置击退: push(targetX - attackerX, targetZ - attackerZ)  [hurt()]
        //   Yaw击退:  push(-sin(yaw), cos(yaw))                     [hurt()/sprint]
        //   要两者同向：(targetX - fakedX, targetZ - fakedZ) ∝ (-sin(yaw), cos(yaw))
        //   → fakedX = targetX + POS_OFFSET * sin(yawRad)
        //   → fakedZ = targetZ - POS_OFFSET * cos(yawRad)
        float yawRad = (float) Math.toRadians(targetYaw);
        double fakedX = targetPos.x + POS_OFFSET * Math.sin(yawRad);
        double fakedZ = targetPos.z - POS_OFFSET * Math.cos(yawRad);
        double fakedY = playerPos.y; // Y 保持不变，避免高度变化

        // ★ 发送组合 PosRot 包：同时改变服务端位置 + 旋转
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                fakedX, fakedY, fakedZ, targetYaw, player.getXRot(), player.onGround()));

        // ★ 设置恢复计时器
        restoreTimer = RESTORE_DELAY_TICKS;
    }

    /**
     * 立即禁用并恢复原始位置和旋转
     */
    public static void disable() {
        if (!active) return;
        active = false;
        restoreTimer = 0;

        LocalPlayer player = mc.player;
        if (player == null) return;

        // ★ 发送恢复包（原始位置 + 原始旋转）
        player.connection.send(new ServerboundMovePlayerPacket.PosRot(
                origX, origY, origZ, origYaw, origPitch, player.onGround()));
    }

    /**
     * 每 Tick 调用的降计时器逻辑
     * 由 KnockbackFeature.onClientTick() 驱动
     */
    public static void tick() {
        if (restoreTimer > 0) {
            restoreTimer--;
            if (restoreTimer == 0) {
                disable();
            }
        }
    }

    public static boolean isActive() {
        return active;
    }

    /** 当前是否在恢复等待期 */
    public static boolean isRestoring() {
        return restoreTimer > 0;
    }
}