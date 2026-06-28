package fku.org.example.fku.features.pearlphase; /* water */

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 珍珠卡墙（PearlPhase）核心逻辑
 *
 * ★ 职责：
 *   1. 自动检测玩家是否卡入方块内部，启用 NoClip 并提供受限移动
 *   2. 自动投掷末影珍珠：看向墙壁时自动切换到珍珠并投掷
 *   3. 移除方块内窒息贴图 + 禁用前方第三人称
 *
 * ★ 设计思想（矛盾论）：
 *   主要矛盾是「珍珠穿墙的成功率不可控性 × 自动化执行的确定性要求」。
 *   解法：将功能拆解为「自动投掷」和「卡墙移动」两个独立阶段，
 *   投掷阶段仅负责计算角度 → 切换物品 → 发包，
 *   卡墙阶段复用参考 PearlPhase.java 的 NoClip + 方向速度计算。
 *   失败时自动重置，不阻塞玩家操作。
 *
 * ★ 参考来源：
 *   - PearlPhase.java (InvincibleMachineGun)：方块内 NoClip + 移动速度
 *   - CameraClip.java (InvincibleMachineGun)：视角动画模式
 */
@OnlyIn(Dist.CLIENT)
public class PearlPhaseFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    // ════════ 状态机 ════════
    public enum PhaseState {
        IDLE,        // 空闲，检测条件
        AIMING,      // 平滑旋转瞄准目标角度
        THROWING,    // 执行投掷发包
        WAITING,     // 等待珍珠落地/传送
        INSIDE       // 已卡入方块，启用 NoClip
    }

    private static PhaseState state = PhaseState.IDLE;
    private static int stateTick = 0;          // 当前状态已持续 tick 数
    private static int waitTicks = 0;          // 等待珍珠的剩余 tick
    private static float targetYaw = 0;        // 瞄准目标偏航
    private static float targetPitch = 0;      // 瞄准目标俯仰
    private static float smoothYaw = Float.NaN; // 平滑旋转当前值
    private static float smoothPitch = Float.NaN;

    // ════════ 按键状态缓存（用于方块内移动） ════════
    private static boolean savedUp = false;
    private static boolean savedDown = false;
    private static boolean savedLeft = false;
    private static boolean savedRight = false;
    private static boolean wasInsideBlock = false;

    /** 初始化 */
    public static void init() {
        PearlPhaseConfig.getInstance();
        Fku.LOGGER.info("[PearlPhase] 功能已初始化");
    }

    // ══════════════════════════════════════════════
    //  按键监听：缓存方向键状态
    // ══════════════════════════════════════════════

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!PearlPhaseConfig.getInstance().enabled) return;

        int keyW = mc.options.keyUp.getKey().getValue();
        int keyS = mc.options.keyDown.getKey().getValue();
        int keyA = mc.options.keyLeft.getKey().getValue();
        int keyD = mc.options.keyRight.getKey().getValue();

        if (event.getKey() == keyW) savedUp = event.getAction() != 0;
        if (event.getKey() == keyS) savedDown = event.getAction() != 0;
        if (event.getKey() == keyA) savedLeft = event.getAction() != 0;
        if (event.getKey() == keyD) savedRight = event.getAction() != 0;
    }

    // ══════════════════════════════════════════════
    //  主 Tick 逻辑
    // ══════════════════════════════════════════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;

        PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();
        if (!cfg.enabled) {
            // 禁用时立即恢复
            if (state != PhaseState.IDLE) resetState();
            if (mc.player.noPhysics) mc.player.noPhysics = false;
            wasInsideBlock = false;
            return;
        }

        stateTick++;

        // ★ 检测是否在方块内
        boolean inside = isInsideBlock(mc.player);
        if (inside) {
            // 卡入方块 → 进入 INSIDE 状态
            handleInsideBlock(mc.player, cfg);
            wasInsideBlock = true;
            return;
        }

        // 从方块内出来后清除 NoClip
        if (wasInsideBlock && !inside) {
            mc.player.noPhysics = false;
            wasInsideBlock = false;
        }

        // 不在方块内且未卡入 → 执行自动投掷逻辑
        if (cfg.autoThrow) {
            handleAutoThrow(cfg);
        } else {
            if (state != PhaseState.IDLE) resetState();
        }

        // ★ 移除窒息贴图（ClientPlayer 覆盖，由 mixin 处理）
        if (cfg.removeOverlay) {
            mc.player.setNoGravity(false);
        }

        // ★ 禁用前方第三人称
        if (cfg.noFront && mc.options.getCameraType() == net.minecraft.client.CameraType.THIRD_PERSON_FRONT) {
            mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        }
    }

    // ══════════════════════════════════════════════
    //  方块内 NoClip + 移动逻辑
    // ══════════════════════════════════════════════

    /**
     * ★ 方块内处理
     *
     * 参考 PearlPhase.java 的 NoClip + 移动速度逻辑：
     * - 启用 NoClip
     * - 清除下落距离，设为接地状态
     * - 根据缓存的 WASD 按键计算移动方向向量
     */
    private static void handleInsideBlock(LocalPlayer player, PearlPhaseConfig cfg) {
        player.noPhysics = cfg.noClipEnabled;
        player.fallDistance = 0;
        player.setOnGround(true);

        double baseSpeed = cfg.baseSpeed;
        double finalSpeed = baseSpeed * cfg.speed;

        // ★ WASD 方向键 → 移动向量（参考 PearlPhase.java handleMove）
        double forward = savedUp ? 1.0 : 0.0;
        double backward = savedDown ? 1.0 : 0.0;
        double left = savedLeft ? 1.0 : 0.0;
        double right = savedRight ? 1.0 : 0.0;

        // 计算净前后方向
        double fwd = forward - backward;

        // 计算净左右（正=左，负=右）
        double strafe = left - right;

        if (fwd == 0 && strafe == 0) {
            player.setDeltaMovement(0, 0, 0);
            return;
        }

        // 斜向移动归一化
        if (fwd != 0 && strafe != 0) {
            fwd *= Math.sin(Math.PI / 4);
            strafe *= Math.cos(Math.PI / 4);
        }

        float yaw = player.getYRot();
        double motionX = fwd * finalSpeed * -Math.sin(Math.toRadians(yaw))
                       + strafe * finalSpeed * Math.cos(Math.toRadians(yaw));
        double motionZ = fwd * finalSpeed * Math.cos(Math.toRadians(yaw))
                       - strafe * finalSpeed * -Math.sin(Math.toRadians(yaw));

        player.setDeltaMovement(motionX, forward > 0 ? finalSpeed * 0.5 : (backward > 0 ? -finalSpeed * 0.5 : 0), motionZ);
        state = PhaseState.INSIDE;
    }

    // ══════════════════════════════════════════════
    //  自动投掷逻辑（状态机）
    // ══════════════════════════════════════════════

    /**
     * ★ 自动投掷末影珍珠
     *
     * 状态机：IDLE → AIMING → THROWING → WAITING → IDLE
     *
     * 参考 PearlPhase.java 的「服务器端珍珠穿墙」思路：
     *   珍珠在特定角度瞄准方块边缘/夹角时，
     *   可穿过碰撞箱间隙到达另一侧。
     *   本功能自动计算该角度并执行投掷。
     */
    private static void handleAutoThrow(PearlPhaseConfig cfg) {
        if (mc.player == null) return;

        switch (state) {
            case IDLE:
                // ★ 检测准星是否指向方块
                if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
                    resetState();
                    return;
                }

                // 检查手中/背包是否有末影珍珠
                if (!hasPearl()) {
                    resetState();
                    return;
                }

                BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
                BlockPos targetPos = blockHit.getBlockPos();
                BlockState targetState = mc.level.getBlockState(targetPos);

                // 只对固体方块触发
                if (targetState.isAir() || !targetState.isSolid()) {
                    resetState();
                    return;
                }

                // ★ 计算瞄准角度：看向方块边缘
                float[] angles = calculateTargetAngle(targetPos, cfg.edgeOffset);
                if (angles == null) {
                    resetState();
                    return;
                }
                targetYaw = angles[0];
                targetPitch = angles[1];
                smoothYaw = Float.NaN;
                smoothPitch = Float.NaN;
                stateTick = 0;
                state = PhaseState.AIMING;
                break;

            case AIMING:
                // ★ 平滑旋转到目标角度
                if (cfg.aimTime <= 0 || smoothRotate(cfg.aimTime)) {
                    // 瞄准完成，切换到珍珠
                    if (!selectPearl()) {
                        resetState();
                        return;
                    }
                    stateTick = 0;
                    state = PhaseState.THROWING;
                }
                break;

            case THROWING:
                // ★ 执行投掷
                if (stateTick >= 1) {  // 至少等 1 tick 让切换生效
                    throwPearl();
                    waitTicks = cfg.maxWaitTicks;
                    stateTick = 0;
                    state = PhaseState.WAITING;
                }
                break;

            case WAITING:
                // ★ 等待珍珠落地或超时
                if (waitTicks-- <= 0) {
                    resetState();
                    return;
                }
                // 如果玩家突然进入方块内，说明珍珠穿墙成功
                if (isInsideBlock(mc.player)) {
                    handleInsideBlock(mc.player, cfg);
                }
                break;

            case INSIDE:
                // 已在方块内，由主逻辑处理
                break;
        }
    }

    // ══════════════════════════════════════════════
    //  辅助方法
    // ══════════════════════════════════════════════

    /**
     * ★ 检查是否在方块内
     *
     * 参考 PearlPhase.java isInsideBlock()
     */
    private static boolean isInsideBlock(LocalPlayer player) {
        if (mc.level == null) return false;
        return mc.level.getBlockCollisions(player, player.getBoundingBox().contract(0.001, 0.001, 0.001))
                .iterator().hasNext();
    }

    /**
     * ★ 计算瞄准方块边缘的角度
     *
     * 对准方块边缘（而非中心），让珍珠更容易从碰撞箱间隙穿过。
     * 参考：
     *   - 边缘偏移量越小，越靠近方块边缘，穿墙成功率越高
     *   - 太小时可能直接错过方块，需配合 edgeOffset 调节
     *
     * @return [yaw, pitch] 或 null（计算失败）
     */
    private static float[] calculateTargetAngle(BlockPos blockPos, double edgeOffset) {
        if (mc.player == null || mc.level == null) return null;

        // ★ 获取方块碰撞箱
        BlockState state = mc.level.getBlockState(blockPos);
        VoxelShape shape = state.getCollisionShape(mc.level, blockPos);
        if (shape.isEmpty()) return null;

        // 计算方块包围盒
        var bounds = shape.bounds();
        double minX = blockPos.getX() + bounds.minX;
        double maxX = blockPos.getX() + bounds.maxX;
        double minY = blockPos.getY() + bounds.minY;
        double maxY = blockPos.getY() + bounds.maxY;
        double minZ = blockPos.getZ() + bounds.minZ;
        double maxZ = blockPos.getZ() + bounds.maxZ;

        // ★ 计算玩家眼睛位置到方块各边缘的方向，选择最接近当前视线的方向
        double eyeX = mc.player.getX();
        double eyeY = mc.player.getEyeY();
        double eyeZ = mc.player.getZ();
        double lookX = mc.player.getLookAngle().x;
        double lookY = mc.player.getLookAngle().y;
        double lookZ = mc.player.getLookAngle().z;

        // 生成 8 个边缘候选点
        double[][] candidates = {
            {minX + edgeOffset, minY + edgeOffset, minZ + edgeOffset},
            {minX + edgeOffset, minY + edgeOffset, maxZ - edgeOffset},
            {minX + edgeOffset, maxY - edgeOffset, minZ + edgeOffset},
            {minX + edgeOffset, maxY - edgeOffset, maxZ - edgeOffset},
            {maxX - edgeOffset, minY + edgeOffset, minZ + edgeOffset},
            {maxX - edgeOffset, minY + edgeOffset, maxZ - edgeOffset},
            {maxX - edgeOffset, maxY - edgeOffset, minZ + edgeOffset},
            {maxX - edgeOffset, maxY - edgeOffset, maxZ - edgeOffset}
        };

        double bestDot = -Double.MAX_VALUE;
        double bestX = 0, bestY = 0, bestZ = 0;

        for (double[] c : candidates) {
            double dx = c[0] - eyeX;
            double dy = c[1] - eyeY;
            double dz = c[2] - eyeZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.001) continue;

            double dot = (dx / len) * lookX + (dy / len) * lookY + (dz / len) * lookZ;
            if (dot > bestDot) {
                bestDot = dot;
                bestX = c[0];
                bestY = c[1];
                bestZ = c[2];
            }
        }

        // 计算偏航俯仰
        double dx = bestX - eyeX;
        double dy = bestY - eyeY;
        double dz = bestZ - eyeZ;

        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDist));

        return new float[]{yaw, pitch};
    }

    /**
     * ★ 平滑旋转到目标角度（一次性完成）
     *
     * @param aimTimeMs 瞄准动画时间毫秒
     * @return true 如果旋转完成
     */
    private static boolean smoothRotate(int aimTimeMs) {
        if (mc.player == null) return true;

        // 初始化平滑值
        if (Float.isNaN(smoothYaw)) {
            smoothYaw = mc.player.getYRot();
            smoothPitch = mc.player.getXRot();
        }

        int totalTicks = Math.max(1, aimTimeMs / 50);  // 毫秒 → tick
        float progress = Math.min(1.0f, (float) stateTick / totalTicks);

        // 指数平滑（参考 SprintHandler 的平滑旋转）
        float factor = 0.3f;
        smoothYaw = smoothYaw + (targetYaw - smoothYaw) * factor;
        smoothPitch = smoothPitch + (targetPitch - smoothPitch) * factor;

        // 直接应用
        mc.player.setYRot(smoothYaw);
        mc.player.setXRot(smoothPitch);

        return progress >= 1.0f || Math.abs(smoothYaw - targetYaw) < 0.1f;
    }

    /**
     * ★ 检查玩家是否持有末影珍珠
     */
    private static boolean hasPearl() {
        if (mc.player == null) return false;
        // 检查主手和背包
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                return true;
            }
        }
        return false;
    }

    /**
     * ★ 自动切换到末影珍珠
     *
     * @return true 如果切换成功
     */
    private static boolean selectPearl() {
        if (mc.player == null) return false;

        // 先检查主手是否已经是珍珠
        if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL) {
            return true;
        }

        // 遍历热栏（0-8）
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                mc.player.getInventory().selected = i;
                return true;
            }
        }

        // 热栏没有，找背包
        for (int i = 9; i < mc.player.getInventory().getContainerSize(); i++) {
            var stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                // 快捷交换到热栏
                mc.player.getInventory().selected = 0;
                mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                        mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround()
                ));
                return false;  // 背包交换太复杂，暂不支持
            }
        }

        return false;
    }

    /**
     * ★ 执行投掷
     *
     * 使用 MultiPlayerGameMode.useItem 模拟右键使用珍珠，
     * 内部自动发送 ServerboundUseItemPacket。
     */
    private static void throwPearl() {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        // 使用后挥动手（视觉效果）
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    /**
     * ★ 重置状态
     */
    private static void resetState() {
        state = PhaseState.IDLE;
        stateTick = 0;
        waitTicks = 0;
        smoothYaw = Float.NaN;
        smoothPitch = Float.NaN;
    }

    // ════════ 公共查询方法 ════════

    public static PhaseState getState() {
        return state;
    }

    public static boolean isInside() {
        return state == PhaseState.INSIDE;
    }

    /**
     * 切换启用/禁用
     */
    public static void toggle() {
        PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            resetState();
            if (mc.player != null) mc.player.noPhysics = false;
            wasInsideBlock = false;
        }
    }
}