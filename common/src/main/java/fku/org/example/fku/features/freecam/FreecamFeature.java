package fku.org.example.fku.features.freecam; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * ★ 灵魂出窍（自由相机）功能
 *
 * 独立的自由相机功能，让相机完全脱离玩家实体自由移动。
 * 参考自 Lexis FreeCamHack 的完整实现：
 * - 独立位置/旋转追踪
 * - 平滑速度插值（三次方缓动 + 惯性衰减）
 * - WASD 移动 + 鼠标旋转
 * - 自动切换第三人称视角
 * - 穿过方块时自动释放潜行键防卡住
 *
 * 该功能由赛博教员实现
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FreecamFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean wasEnabled = false;
    private static CameraType oldCameraType = CameraType.FIRST_PERSON;
    private static boolean wasFlying = false;
    private static boolean wasNoClip = false;
    private static long overlayShowUntil = 0;
    private static boolean wasStandAttackActive = false; // 标记是否被StandAttack激活

    public static boolean isEnabled() { return FreecamConfig.getInstance().enabled; }

    public static void setEnabled(boolean v) {
        FreecamConfig cfg = FreecamConfig.getInstance();
        cfg.setEnabled(v);
        if (v) enable();
        else disable();
    }

    /** 由 StandAttack 激活（不修改配置文件的 enabled 状态） */
    public static void activateForStandAttack() {
        if (FreecamManager.isActive()) return;
        wasStandAttackActive = true;
        enable();
    }

    /** 由 StandAttack 停用 */
    public static void deactivateForStandAttack() {
        if (!wasStandAttackActive) return;
        wasStandAttackActive = false;
        disable();
        // 恢复玩家位置到自由相机位置（StandAttack回传）
    }

    private static void enable() {
        if (mc.player == null) { disable(); return; }
        if (FreecamManager.isActive()) return;

        // 保存当前相机设置
        oldCameraType = mc.options.getCameraType();
        // ★ 被StandAttack激活时不切换视角，保持当前视角
        //   灵魂出窍时强制第一人称，使准星和手持物品可见
        if (!wasStandAttackActive) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
        // 释放潜行键（防止卡在方块里）
        KeyMapping.set(mc.options.keyShift.getKey(), false);
        while (mc.options.keyShift.consumeClick()) {}

        // 初始化自由相机位置（使用玩家眼部位置，而非脚部位置）
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity != null) {
            Vec3 pos = cameraEntity.getEyePosition(1.0f);
            FreecamManager.activate(pos.x, pos.y, pos.z, cameraEntity.getYRot(), cameraEntity.getXRot());
            // 稍微后退一点（避免卡在玩家头里）
            Vec3 lookVec = Vec3.directionFromRotation(FreecamManager.getXRot(), FreecamManager.getYRot());
            FreecamManager.setPosition(pos.x - lookVec.x * 0.05, pos.y - lookVec.y * 0.05, pos.z - lookVec.z * 0.05);
        }

        // 保存玩家飞行状态
        wasFlying = mc.player.getAbilities().flying;
        wasNoClip = mc.player.isSpectator();
        overlayShowUntil = System.currentTimeMillis() + 2000;

        if (!wasStandAttackActive) {
            // 仅显示 overlay 提示，不发送聊天栏消息
        }
    }

    private static void disable() {
        wasStandAttackActive = false;

        // 恢复相机设置
        mc.options.setCameraType(oldCameraType);
        FreecamManager.deactivate();
        overlayShowUntil = System.currentTimeMillis() + 1000;
    }

    // ════════ 鼠标旋转 — 由 MouseHandlerMixin 代理 ════════

    // ════════ Tick 更新 ════════

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;

        FreecamConfig cfg = FreecamConfig.getInstance();
        FreecamManager.setMaxSpeed(cfg.maxSpeed);
        FreecamManager.setSmoothness(cfg.smoothness);

        if (cfg.enabled || wasStandAttackActive) {
            if (!FreecamManager.isActive()) {
                // 重新激活（如配置文件被外部修改）
                enable();
                return;
            }

            // ★ 被StandAttack激活时不强制切换视角，保持当前视角
            //   灵魂出窍时强制第一人称，使准星和手持物品可见
            if (!wasStandAttackActive && mc.options.getCameraType() != CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }

            // 防卡方块：穿过方块时释放潜行键
            BlockPos camPos = BlockPos.containing(
                FreecamManager.getPosition().x,
                FreecamManager.getPosition().y,
                FreecamManager.getPosition().z);
            if (mc.level.getBlockState(camPos).isSolid()) {
                KeyMapping.set(mc.options.keyShift.getKey(), false);
                while (mc.options.keyShift.consumeClick()) {}
            }

            // 读取 WASD 输入（正方向：forward=前，strafe=左，up=上）
            float forward = (float)((mc.options.keyUp.isDown() ? 1 : 0) + (mc.options.keyDown.isDown() ? -1 : 0));
            float strafe = (float)((mc.options.keyLeft.isDown() ? 1 : 0) + (mc.options.keyRight.isDown() ? -1 : 0));
            float up = (float)((mc.options.keyJump.isDown() ? 1 : 0) + (mc.options.keyShift.isDown() ? -1 : 0));

            // 计算帧时间差
            long now = System.nanoTime();
            float delta = 0.05f; // 默认20tick
            // 使用固定步长（Minecraft tick 是 50ms）

            FreecamManager.updateMovement(forward, strafe, up, delta);
        } else {
            if (FreecamManager.isActive() && !wasStandAttackActive) {
                disable();
            }
        }
    }

    // ════════ Overlay 显示 ════════

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (System.currentTimeMillis() > overlayShowUntil) return;
        if (!FreecamConfig.getInstance().showOverlay) return;

        String text = "§6[灵魂出窍 " + (FreecamManager.isActive() ? "§aON" : "§cOFF") + "§6]";
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int textX = w / 2 - mc.font.width(text) / 2;
        int textY = h - 82;
        event.getGuiGraphics().drawString(mc.font, text, textX, textY, 0xFFFFFF);
    }

    /** 获取自由相机位置（供 StandAttack 回传使用） */
    public static Vec3 getCameraPosition() {
        return FreecamManager.getPosition();
    }
}