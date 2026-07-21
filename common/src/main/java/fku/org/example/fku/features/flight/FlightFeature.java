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
                mc.player.m_6330_(SoundEvents.f_11932_, SoundSource.PLAYERS, 0.5f, 1.5f);
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
        if (mc == null || mc.player == null || mc.f_91073_ == null) {
            return;
        }
        LocalPlayer player = mc.player;
        FlightConfig cfg = FlightConfig.getInstance();
        if (!FlightFeature.isEnabled()) {
            return;
        }
        if (cfg.onlyInCreative && !player.m_7500_()) {
            FlightFeature.deactivate();
            return;
        }
        if (cfg.consumeHunger && active) {
            if (player.m_36324_().m_38702_() <= 0) {
                FlightFeature.deactivate();
                return;
            }
            player.m_36324_().m_38703_(cfg.hungerCost * 0.01f);
        }
        boolean jumping = player.f_108618_.f_108572_;
        if (!active) {
            boolean hasCreativeFlight;
            boolean bl = hasCreativeFlight = player.m_150110_().f_35936_ && player.m_150110_().f_35935_;
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
        float camYaw = mc.f_91063_.m_109153_().m_90590_();
        float fwd = player.f_108618_.f_108567_;
        float str = -player.f_108618_.f_108566_;
        Vec3 h = Vec3.m_82498_(0.0f, camYaw).m_82542_(fwd, 0.0, fwd).add(Vec3.m_82498_(0.0f, (camYaw + 90.0f)).m_82542_(str, 0.0, str));
        h = h.m_82556_() > 1.0E-4 ? h.normalize().scale(cfg.flySpeed) : Vec3.f_82478_;
        double vy = jumping ? cfg.verticalSpeed : (player.f_108618_.f_108573_ ? -cfg.verticalSpeed : 0.0);
        player.m_20334_(h.x, vy, h.z);
        if (cfg.antiKick) {
            if (++antiKickTicks > cfg.antiKickInterval) {
                antiKickTicks = 0;
                player.m_20334_(h.x, -cfg.antiKickDistance, h.z);
            } else if (antiKickTicks == 1) {
                player.m_20334_(h.x, cfg.antiKickDistance, h.z);
            }
        }
        player.f_19794_ = cfg.disableCollision;
        if (cfg.disableCollision && player.getY() < mc.f_91073_.m_141937_()) {
            player.m_6034_(player.getX(), (mc.f_91073_.m_141937_() + 1), player.getZ());
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
            player.m_6858_(false);
        }
        if (cfg.particleEffect && player.f_19797_ % 3 == 0) {
            mc.f_91073_.m_7106_((ParticleOptions)ParticleTypes.f_123796_, player.getX() + (player.m_217043_().m_188500_() - 0.5) * player.getBbWidth(), player.getY(), player.getZ() + (player.m_217043_().m_188500_() - 0.5) * player.getBbWidth(), 0.0, -0.1, 0.0);
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
            mc.player.m_6330_(SoundEvents.f_11932_, SoundSource.PLAYERS, 0.5f, 1.5f);
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
        mc.player.f_19794_ = false;
        if (FlightConfig.getInstance().soundFeedback) {
            mc.player.m_6330_(SoundEvents.f_11928_, SoundSource.PLAYERS, 0.3f, 0.8f);
        }
        Fku.LOGGER.debug("[Flight] \u964d\u843d");
    }
}

