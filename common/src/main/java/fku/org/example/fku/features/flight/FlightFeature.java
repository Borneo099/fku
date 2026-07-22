package fku.org.example.fku.features.flight;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.flight.FlightConfig;
import fku.org.example.fku.features.nofall.NoFallFeature;
import fku.org.example.fku.features.sprint.SprintConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class FlightFeature {
    private static boolean active = false;
    private static boolean prevJumping = false;
    private static long lastJumpPress = 0L;
    private static boolean tapWaiting = false;
    private static int antiKickTicks = 0;
    private static String savedSprintMode = null;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        FlightConfig.load();
        if (FlightFeature.isEnabled()) {
            Fku.LOGGER.debug("[Flight] \u914d\u7f6e\u6062\u590d: \u5df2\u542f\u7528");
        }
    }

    public static void toggleEnabled() {
        FlightFeature.setEnabled(!FlightFeature.isEnabled());
    }

    public static void setEnabled(boolean val) {
        FlightConfig cfg = FlightConfig.getInstance();
        cfg.enabled = val;
        cfg.save();
        if (!val) {
            FlightFeature.deactivate();
        } else {
            Minecraft mc = FlightFeature.getMc();
            if (cfg.soundFeedback && mc != null && mc.player != null) {
                mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.5f, 1.5f);
            }
            if (!NoFallFeature.isEnabled()) {
                NoFallFeature.setEnabled(true);
            }
            Fku.LOGGER.debug("[Flight] \u5df2\u542f\u7528");
        }
    }

    public static boolean isEnabled() {
        return FlightConfig.getInstance().enabled;
    }

    public static boolean isFlightActive() {
        return active;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = FlightFeature.getMc();
        if (mc == null || mc.player == null || mc.level == null) {
            return;
        }
        LocalPlayer player = mc.player;
        FlightConfig cfg = FlightConfig.getInstance();
        if (!FlightFeature.isEnabled()) {
            return;
        }
        if (cfg.onlyInCreative && !player.isCreative()) {
            FlightFeature.deactivate();
            return;
        }
        if (cfg.consumeHunger && active) {
            if (player.getFoodData().getFoodLevel() <= 0) {
                FlightFeature.deactivate();
                return;
            }
            player.getFoodData().addExhaustion(cfg.hungerCost * 0.01f);
        }
        boolean jumping = player.input.jumping;
        if (!active) {
            boolean hasCreativeFlight;
            boolean bl = hasCreativeFlight = player.getAbilities().mayfly && player.getAbilities().flying;
            if (jumping && !prevJumping && !hasCreativeFlight) {
                long now = System.currentTimeMillis();
                if (tapWaiting && now - lastJumpPress <= cfg.doubleTapWindow) {
                    FlightFeature.activate();
                    tapWaiting = false;
                } else {
                    tapWaiting = true;
                }
                lastJumpPress = now;
            }
            if (tapWaiting && System.currentTimeMillis() - lastJumpPress > cfg.doubleTapWindow) {
                tapWaiting = false;
            }
            prevJumping = jumping;
            return;
        }
        float camYaw = mc.gameRenderer.getMainCamera().getYRot();
        float fwd = player.input.forwardImpulse;
        float str = -player.input.leftImpulse;
        Vec3 h = Vec3.directionFromRotation(0.0f, camYaw).multiply(fwd, 0.0, fwd).add(Vec3.directionFromRotation(0.0f, (camYaw + 90.0f)).multiply(str, 0.0, str));
        h = h.lengthSqr() > 1.0E-4 ? h.normalize().scale(cfg.flySpeed) : Vec3.ZERO;
        double vy = jumping ? cfg.verticalSpeed : (player.input.shiftKeyDown ? -cfg.verticalSpeed : 0.0);
        player.setDeltaMovement(h.x, vy, h.z);
        if (cfg.antiKick) {
            if (++antiKickTicks > cfg.antiKickInterval) {
                antiKickTicks = 0;
                player.setDeltaMovement(h.x, -cfg.antiKickDistance, h.z);
            } else if (antiKickTicks == 1) {
                player.setDeltaMovement(h.x, cfg.antiKickDistance, h.z);
            }
        }
        player.noPhysics = cfg.disableCollision;
        if (cfg.disableCollision && player.getY() < mc.level.getMinBuildHeight()) {
            player.setPos(player.getX(), (mc.level.getMinBuildHeight() + 1), player.getZ());
        }
        if (jumping && !prevJumping) {
            long now = System.currentTimeMillis();
            if (now - lastJumpPress <= cfg.doubleTapWindow && now - lastJumpPress > 50L) {
                FlightFeature.deactivate();
                prevJumping = jumping;
                return;
            }
            lastJumpPress = now;
        }
        prevJumping = jumping;
        if (!cfg.allowSprint) {
            player.setSprinting(false);
        }
        if (cfg.particleEffect && player.tickCount % 3 == 0) {
            mc.level.addParticle((ParticleOptions)ParticleTypes.CLOUD, player.getX() + (player.getRandom().nextDouble() - 0.5) * player.getBbWidth(), player.getY(), player.getZ() + (player.getRandom().nextDouble() - 0.5) * player.getBbWidth(), 0.0, -0.1, 0.0);
        }
        if (!NoFallFeature.isEnabled()) {
            NoFallFeature.setEnabled(true);
        }
    }

    private static void activate() {
        Minecraft mc = FlightFeature.getMc();
        if (mc == null || mc.player == null) {
            return;
        }
        active = true;
        antiKickTicks = 0;
        savedSprintMode = SprintConfig.getInstance().mode;
        if ("OMNIROTATIONAL".equals(savedSprintMode)) {
            SprintConfig.getInstance().mode = "OMNIDIRECTIONAL";
            SprintConfig.save();
        }
        if (FlightConfig.getInstance().soundFeedback && mc.player != null) {
            mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.5f, 1.5f);
        }
        if (!NoFallFeature.isEnabled()) {
            NoFallFeature.setEnabled(true);
        }
        Fku.LOGGER.debug("[Flight] \u8d77\u98de");
    }

    private static void deactivate() {
        Minecraft mc = FlightFeature.getMc();
        if (mc == null || mc.player == null) {
            return;
        }
        active = false;
        if (savedSprintMode != null) {
            SprintConfig.getInstance().mode = savedSprintMode;
            SprintConfig.save();
            savedSprintMode = null;
        }
        mc.player.noPhysics = false;
        if (FlightConfig.getInstance().soundFeedback) {
            mc.player.playNotifySound(SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.3f, 0.8f);
        }
        Fku.LOGGER.debug("[Flight] \u964d\u843d");
    }
}

