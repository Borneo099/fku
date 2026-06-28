package fku.org.example.fku.features.sprint; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SprintHandler — 强制疾跑（BMWSprint）核心逻辑
 *
 * ★ 职责：
 *   实现三种疾跑模式：
 *   - LEGIT（原版模式）：仅 W 键疾跑
 *   - OMNIDIRECTIONAL（全向疾跑）：任意方向保持疾跑
 *   - OMNIROTATIONAL（全向旋转）：任意方向疾跑 + 假旋转包
 *
 * ★ 矛盾定性（v4）：
 *   v1 直接改 player.yaw → 累积偏移无限旋转
 *   v2 player.input.zza/xxa 覆盖 → 因 input.tick() 重新计算 zza/xxa，无效
 *   v3 KeyMapping.setDown() → OS 键盘事件异步覆写，不可靠
 *   v4 → 用 MovementInputUpdateEvent 在 input.tick() 完成后再覆盖 forwardImpulse/leftImpulse，
 *       彻底回避 OS 键盘事件与 setDown() 的时序竞态，zzz/xxa 100% 生效
 *
 * ★ 实践路线：
 *   1. START 阶段保存真实视角 + 真实键状态（用于方向计算）
 *   2. 基于真实键计算目标 yaw 和键组合 hash
 *   3. 应用平滑旋转 yaw → 修改 player.yaw（aiStep 发包用）
 *   4. MovementInputUpdateEvent 在 input.tick() 后强制 zza=1/xxa=0
 *   5. END 阶段恢复真实视角
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SprintHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean enabled = false;
    private static final boolean DEBUG = false;

    // ════════════════════════════════════════════════════════
    // ★ CHANGE_LOOK 状态
    // ════════════════════════════════════════════════════════

    /** 当前 Tick 是否修改了 yaw（用于 END 恢复） */
    private static boolean yawModified = false;
    private static float realYaw;
    private static float realPitch;

    /** 平滑旋转：当前插值 yaw（跨 Tick 累计） */
    private static float smoothYaw = Float.NaN;

    /** 标记本 Tick 需要覆盖移动输入（给 MovementInputUpdateEvent 用） */
    private static boolean overrideInputThisTick = false;

    // ════════════════════════════════════════════════════════
    // ★ 键状态保存（仅用于 hash 和方向计算，不再修改 KeyMapping 状态）
    // ════════════════════════════════════════════════════════

    private static boolean savedUp, savedDown, savedLeft, savedRight;

    // ════════════════════════════════════════════════════════
    // ★ 键组合哈希，检测方向变化时重置 smoothYaw
    // ════════════════════════════════════════════════════════

    private static int lastKeyHash = 0;

    // ════════════════════════════════════════════════════════
    // ★ 公开接口
    // ════════════════════════════════════════════════════════

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        if (DEBUG) System.out.println("[Sprint] setEnabled: " + value);
        enabled = value;
        if (!enabled) {
            if (mc.player != null) {
                mc.player.setSprinting(false);
            }
            restoreAll();
        }
    }

    public static void init() {
        SprintConfig.getInstance();
        Fku.LOGGER.info("[Sprint] 强制疾跑功能已初始化");
    }

    // ════════════════════════════════════════════════════════
    // ★ ClientTickEvent — START 设假 yaw, END 恢复
    // ════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null || mc.level == null) return;

        SprintConfig cfg = SprintConfig.getInstance();

        if (event.phase == TickEvent.Phase.START) {
            yawModified = false;
            overrideInputThisTick = false;

            if (!enabled) return;

            switch (cfg.getMode()) {
                case LEGIT -> handleLegit(cfg);
                case OMNIDIRECTIONAL -> handleOmnidirectional(cfg);
                case OMNIROTATIONAL -> handleOmnirotational(cfg);
            }
        } else if (event.phase == TickEvent.Phase.END) {
            // ★ END：恢复真实视角（yaw 在 aiStep 发包后不再需要）
            if (yawModified) {
                mc.player.setYRot(realYaw);
                mc.player.setXRot(realPitch);
                yawModified = false;
                overrideInputThisTick = false;

                if (DEBUG) System.out.println("[Sprint] END: 恢复 yaw=" + realYaw);
            }
        }
    }

    /**
     * ★ MovementInputUpdateEvent — 在 input.tick() 完成后强行覆盖移动输入
     *
     * 时机：KeyboardInput.tick() 设置 zza/xxa 之后，aiStep() 使用它们之前
     * 效果：无论玩家实际按了哪些 WASD 键，强制 zza=1/xxa=0，实现纯向前移动
     * 解决：KeyMapping.setDown() 被 OS 键盘事件异步覆写的问题
     */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!overrideInputThisTick) return;

        Input input = event.getInput();
        // ★ 强制纯向前 + 无侧移
        //    forwardImpulse = zza（前1 后-1 不按0）
        //    leftImpulse    = xxa（左1 右-1 不按0）
        input.forwardImpulse = 1.0F;
        input.leftImpulse = 0.0F;
    }

    // ════════════════════════════════════════════════════════
    // ★ 三种模式实现
    // ════════════════════════════════════════════════════════

    private static void handleLegit(SprintConfig cfg) {
        if (!canSprint(cfg)) return;

        if (mc.options.keyUp.isDown()) {
            mc.player.setSprinting(true);
        } else {
            if (!mc.player.isSprinting()) return;
            if (cfg.stopOnGround && mc.player.onGround()) {
                mc.player.setSprinting(false);
            } else if (cfg.stopOnAir && !mc.player.onGround()) {
                mc.player.setSprinting(false);
            }
        }
    }

    private static void handleOmnidirectional(SprintConfig cfg) {
        if (!canSprint(cfg)) return;
        mc.player.setSprinting(true);
    }

    /**
     * OMNIROTATIONAL 模式核心逻辑
     *
     * 流程：
     *   1. 保存真实视角（yaw/pitch）
     *   2. 读取真实键状态（用于方向计算）
     *   3. 基于方向计算目标 yaw + 平滑插值
     *   4. 修改 player.yaw（aiStep 按此方向移动/发包）
     *   5. 标注 overrideInputThisTick → MovementInputUpdateEvent 会处理输入
     *   6. END 阶段恢复 yaw/pitch → 玩家视角不变（SILENT 模式）
     */
    private static void handleOmnirotational(SprintConfig cfg) {
        // ★ 1. 保存真实视角
        realYaw = mc.player.getYRot();
        realPitch = mc.player.getXRot();

        // ★ 2. 读取真实键状态
        savedUp    = mc.options.keyUp.isDown();
        savedDown  = mc.options.keyDown.isDown();
        savedLeft  = mc.options.keyLeft.isDown();
        savedRight = mc.options.keyRight.isDown();

        // ★ 3. 计算键组合 hash
        int curHash = 0;
        if (savedUp)    curHash |= 1;
        if (savedDown)  curHash |= 2;
        if (savedLeft)  curHash |= 4;
        if (savedRight) curHash |= 8;

        // ★ 4. 无方向键 → 停止疾跑，重置
        if (curHash == 0) {
            smoothYaw = Float.NaN;
            lastKeyHash = 0;
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
            return;
        }

        if (!canSprint(cfg)) {
            return;
        }

        mc.player.setSprinting(true);

        // ★ 5. 键组合变化 → 重置平滑旋转（防止跨组合角度误差累积）
        if (curHash != lastKeyHash) {
            smoothYaw = Float.NaN;
            lastKeyHash = curHash;

            if (DEBUG) System.out.println("[Sprint] hash变化: " + lastKeyHash + " → " + curHash);
        }

        // ★ 6. 基于【真实键】计算目标 yaw
        float targetYaw = getMovementDirection(realYaw, savedUp, savedDown, savedLeft, savedRight);

        // ★ 7. 平滑旋转：渐进插值
        if (cfg.smoothRotation && cfg.rotationSpeed > 0) {
            if (Float.isNaN(smoothYaw)) {
                // ★ 关键修复：从真实视角开始平滑，而非直接跳到目标
                //   bug: smoothYaw = targetYaw → 第一帧就跳到目标，无平滑
                //   fix: smoothYaw = realYaw → 从真实视角逐步逼近目标
                smoothYaw = realYaw;
            }
            // ★ 改为指数因子插值：rotationSpeed 映射为 [0.05, 1.0] 因子
            //   用户可调范围 1~360，映射到 0.05~1.0，默认 30→~0.17
            //   指数平滑比固定步进更自然（先快后慢，类似真实鼠标平滑）
            float factor = Math.max(0.05f, Math.min(1.0f, cfg.rotationSpeed / 180.0f));
            smoothYaw = lerpAngle(smoothYaw, targetYaw, factor);
        } else {
            smoothYaw = targetYaw;
        }

        // ★ 8. 防检测随机偏移（±0.001°，极轻微）
        smoothYaw += (float) ((Math.random() - 0.5) * 0.002);

        // ★ 9. 应用假 yaw → aiStep 发包用
        mc.player.setYRot(smoothYaw);
        yawModified = true;
        // ★ 标注本 Tick 需要强制纯向前输入
        overrideInputThisTick = true;

        if (DEBUG) {
            System.out.println("[Sprint] OMNIROTATIONAL: real=" + realYaw
                + " target=" + targetYaw + " smooth=" + smoothYaw
                + " hash=" + curHash);
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 角度插值
    // ════════════════════════════════════════════════════════

    /**
     * 角度指数平滑插值
     *
     * 每帧移动剩余距离的 factor 比例，自然减速（先快后慢）
     * 处理 -180/180 边界环绕
     *
     * ★ v4→v5 升级原因：
     *   旧版 lerpAngle(from,to,step) 用固定步进（°/帧），导致：
     *   - step=120 时 ≤120° 转向直接跳过（abs(delta)<=step），无平滑
     *   - >120° 也仅需 1-2 帧完成，肉眼不可见
     *   新版用指数因子，delta 大时移动快、delta 小时移动慢，
     *   模拟真实鼠标平滑（同 BMWSprint 的 BMWRotationManager 策略）
     */
    private static float lerpAngle(float from, float to, float factor) {
        float delta = to - from;
        while (delta > 180) delta -= 360;
        while (delta < -180) delta += 360;
        if (Math.abs(delta) < 0.05f) return to;
        return from + delta * factor;
    }

    // ════════════════════════════════════════════════════════
    // ★ 通用条件检查
    // ════════════════════════════════════════════════════════

    private static boolean canSprint(SprintConfig cfg) {
        if (mc.player == null) return false;

        boolean isHungry = mc.player.getFoodData().getFoodLevel() <= 6 && !mc.player.isCreative();
        if (isHungry && !cfg.ignoreHunger) return false;

        if (mc.player.hasEffect(MobEffects.BLINDNESS) && !cfg.ignoreBlindness) {
            return false;
        }

        if (mc.player.horizontalCollision && !cfg.ignoreCollision) return false;

        if (!(savedUp || savedDown || savedLeft || savedRight)) return false;

        if (mc.player.isShiftKeyDown()) return false;
        if (mc.player.isPassenger()) return false;
        if (mc.player.isInWater() || mc.player.isInLava()) return false;

        return true;
    }

    // ════════════════════════════════════════════════════════
    // ★ 恢复
    // ════════════════════════════════════════════════════════

    /** 恢复所有临时修改（禁用功能时调用） */
    private static void restoreAll() {
        if (yawModified && mc.player != null) {
            mc.player.setYRot(realYaw);
            mc.player.setXRot(realPitch);
            yawModified = false;
        }
        overrideInputThisTick = false;
        smoothYaw = Float.NaN;
        lastKeyHash = 0;
    }

    // ════════════════════════════════════════════════════════
    // ★ 方向计算
    // ════════════════════════════════════════════════════════

    /**
     * 根据 WASD 输入和当前 yaw 计算目标移动方向 yaw
     *
     * ★ 方向映射（相对于相机视角）：
     *   W（前）：   0°           S（后）：   180°
     *   A（左）：  -90°          D（右）：   90°
     *   W+A：     -45°          W+D：      45°
     *   S+A：    -135°          S+D：     135°
     *
     * 参考来源：
     *   ElytraFlyPlus.getSprintYaw() — InvincibleMachineGun Meteor addon
     *   该函数的 delta 映射在 1.20.1 环境已验证正确
     */
    private static float getMovementDirection(float baseYaw,
                                               boolean forward, boolean backward,
                                               boolean left, boolean right) {
        int fwd = (forward ? 1 : 0) - (backward ? 1 : 0);
        int strafe = (left ? 1 : 0) - (right ? 1 : 0);

        if (fwd == 0 && strafe == 0) return baseYaw;

        float delta = 0;

        if (fwd > 0) {
            if (strafe > 0) delta = -45;      // 前+左
            else if (strafe < 0) delta = 45;  // 前+右
            // else 纯前：delta=0
        } else if (fwd < 0) {
            if (strafe > 0) delta = -135;     // 后+左
            else if (strafe < 0) delta = 135; // 后+右
            else delta = 180;                 // 纯后
        } else {
            if (strafe > 0) delta = -90;      // 纯左
            else if (strafe < 0) delta = 90;  // 纯右
        }

        return baseYaw + delta;
    }
}