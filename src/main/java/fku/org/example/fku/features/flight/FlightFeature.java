package fku.org.example.fku.features.flight; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.sprint.SprintConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * FlightFeature — 飞行功能（Wurst 模式）
 *
 * 双击空格起飞/降落。起飞后以相机视角方向飞行。
 * 自动切换 Sprint 全向旋转 → 全向疾跑，关闭时恢复。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FlightFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean enabled = false;
    private static boolean active = false;

    /** ★ 从配置文件静默恢复开关状态 */
    public static void init() {
        FlightConfig.load();
        if (FlightConfig.getInstance().enabled) {
            enabled = true;
            Fku.LOGGER.debug("[Flight] 配置恢复: 已启用");
        }
    }

    // 双击检测
    private static boolean prevJumping = false;
    private static long lastJumpPress = 0;
    private static boolean tapWaiting = false;

    // 防踢
    private static int antiKickTicks = 0;

    // Sprint 模式还原
    private static String savedSprintMode = null;

    public static void toggleEnabled() { setEnabled(!enabled); }

    public static void setEnabled(boolean val) {
        FlightConfig cfg = FlightConfig.getInstance();
        enabled = val;
        cfg.setEnabled(val);
        if (!val) deactivate();
        else {
            if (cfg.soundFeedback && mc.player != null)
                mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.5f, 1.5f);
            if (!NoFallFeature.isEnabled()) NoFallFeature.setEnabled(true);
            Fku.LOGGER.debug("[Flight] 已启用");
        }
    }

    public static boolean isEnabled() { return enabled; }
    public static boolean isFlightActive() { return active; }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;
        LocalPlayer player = mc.player;
        FlightConfig cfg = FlightConfig.getInstance();
        if (!enabled) return;
        if (cfg.onlyInCreative && !player.isCreative()) { deactivate(); return; }

        // ── 饥饿消耗 ──
        if (cfg.consumeHunger && active) {
            if (player.getFoodData().getFoodLevel() <= 0) { deactivate(); return; }
            player.getFoodData().addExhaustion(cfg.hungerCost * 0.01f);
        }

        // ── 双击检测 ──
        boolean jumping = player.input.jumping;
        if (!active) {
            // ★ 创造模式冲突：玩家已经拥有原版飞行能力时，不激活 Wurst 飞行
            boolean hasCreativeFlight = player.getAbilities().mayfly && player.getAbilities().flying;
            if (jumping && !prevJumping && !hasCreativeFlight) {
                long now = System.currentTimeMillis();
                if (tapWaiting && now - lastJumpPress <= cfg.doubleTapWindow) {
                    activate(); tapWaiting = false;
                } else { tapWaiting = true; }
                lastJumpPress = now;
            }
            if (tapWaiting && System.currentTimeMillis() - lastJumpPress > cfg.doubleTapWindow) tapWaiting = false;
            prevJumping = jumping;
            return;
        }

        // ── 飞行控制 ──
        float camYaw = mc.gameRenderer.getMainCamera().getYRot();
        float fwd = player.input.forwardImpulse;
        float str = -player.input.leftImpulse;
        Vec3 h = Vec3.directionFromRotation(0, camYaw).multiply(fwd, 0, fwd)
                .add(Vec3.directionFromRotation(0, camYaw + 90).multiply(str, 0, str));
        if (h.lengthSqr() > 1e-4) h = h.normalize().scale(cfg.flySpeed);
        else h = Vec3.ZERO;

        double vy = jumping ? cfg.verticalSpeed : player.input.shiftKeyDown ? -cfg.verticalSpeed : 0;
        player.setDeltaMovement(h.x, vy, h.z);

        // 防踢
        if (cfg.antiKick) {
            antiKickTicks++;
            if (antiKickTicks > cfg.antiKickInterval) {
                antiKickTicks = 0;
                player.setDeltaMovement(h.x, -cfg.antiKickDistance, h.z);
            } else if (antiKickTicks == 1) {
                player.setDeltaMovement(h.x, cfg.antiKickDistance, h.z);
            }
        }

        player.noPhysics = cfg.disableCollision;
        if (cfg.disableCollision && player.getY() < mc.level.getMinBuildHeight())
            player.setPos(player.getX(), mc.level.getMinBuildHeight() + 1, player.getZ());

        // 双击降落
        if (jumping && !prevJumping) {
            long now = System.currentTimeMillis();
            if (now - lastJumpPress <= cfg.doubleTapWindow && now - lastJumpPress > 50) {
                deactivate(); prevJumping = jumping; return;
            }
            lastJumpPress = now;
        }
        prevJumping = jumping;

        if (!cfg.allowSprint) player.setSprinting(false);
        if (cfg.particleEffect && player.tickCount % 3 == 0)
            mc.level.addParticle(ParticleTypes.CLOUD,
                player.getX() + (player.getRandom().nextDouble() - 0.5) * player.getBbWidth(),
                player.getY(),
                player.getZ() + (player.getRandom().nextDouble() - 0.5) * player.getBbWidth(),
                0, -0.1, 0);

        if (!NoFallFeature.isEnabled()) NoFallFeature.setEnabled(true);
    }

    private static void activate() {
        if (mc.player == null) return;
        active = true; antiKickTicks = 0;
        savedSprintMode = SprintConfig.getInstance().mode;
        if ("OMNIROTATIONAL".equals(savedSprintMode)) {
            SprintConfig.getInstance().mode = "OMNIDIRECTIONAL";
            SprintConfig.save();
        }
        if (FlightConfig.getInstance().soundFeedback && mc.player != null)
            mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.5f, 1.5f);
        if (!NoFallFeature.isEnabled()) NoFallFeature.setEnabled(true);
        Fku.LOGGER.debug("[Flight] 起飞");
    }

    private static void deactivate() {
        if (mc.player == null) return;
        active = false;
        if (savedSprintMode != null) {
            SprintConfig.getInstance().mode = savedSprintMode;
            SprintConfig.save(); savedSprintMode = null;
        }
        mc.player.noPhysics = false;
        if (FlightConfig.getInstance().soundFeedback)
            mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.3f, 0.8f);
        Fku.LOGGER.debug("[Flight] 降落");
    }
}
