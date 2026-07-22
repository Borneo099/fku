package fku.org.example.fku.features.sprint;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.sprint.SprintConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class SprintHandler {
    private static final boolean DEBUG = false;
    private static boolean yawModified = false;
    private static float realYaw;
    private static float realPitch;
    private static float smoothYaw;
    private static boolean overrideInputThisTick;
    private static boolean savedUp;
    private static boolean savedDown;
    private static boolean savedLeft;
    private static boolean savedRight;
    private static int lastKeyHash;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    private static boolean enabled() {
        return SprintConfig.getInstance().enabled;
    }

    private static void setEnabledPersist(boolean v) {
        SprintConfig cfg = SprintConfig.getInstance();
        cfg.enabled = v;
        SprintConfig.save();
    }

    public static boolean isEnabled() {
        return SprintHandler.enabled();
    }

    public static void setEnabled(boolean value) {
        SprintHandler.setEnabledPersist(value);
        if (!value) {
            Minecraft mc = SprintHandler.getMc();
            if (mc != null && mc.player != null) {
                mc.player.setSprinting(false);
            }
            SprintHandler.restoreAll();
        }
    }

    public static void init() {
        SprintConfig.getInstance();
        Fku.LOGGER.info("[Sprint] \u5f3a\u5236\u75be\u8dd1\u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = SprintHandler.getMc();
        if (mc == null || mc.player == null || mc.level == null) {
            return;
        }
        SprintConfig cfg = SprintConfig.getInstance();
        if (event.phase == TickEvent.Phase.START) {
            yawModified = false;
            overrideInputThisTick = false;
            if (!SprintHandler.isEnabled()) {
                return;
            }
            switch (cfg.getMode()) {
                case LEGIT: {
                    SprintHandler.handleLegit(cfg);
                    break;
                }
                case OMNIDIRECTIONAL: {
                    SprintHandler.handleOmnidirectional(cfg);
                    break;
                }
                case OMNIROTATIONAL: {
                    SprintHandler.handleOmnirotational(cfg);
                }
            }
        } else if (event.phase == TickEvent.Phase.END && yawModified) {
            mc.player.setYRot(realYaw);
            mc.player.setXRot(realPitch);
            yawModified = false;
            overrideInputThisTick = false;
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!overrideInputThisTick) {
            return;
        }
        Input input = event.getInput();
        input.forwardImpulse = 1.0f;
        input.leftImpulse = 0.0f;
    }

    private static void handleLegit(SprintConfig cfg) {
        if (!SprintHandler.canSprint(cfg)) {
            return;
        }
        Minecraft mc = SprintHandler.getMc();
        if (mc == null) {
            return;
        }
        if (mc.options.keyUp.isDown()) {
            mc.player.setSprinting(true);
        } else {
            if (!mc.player.isSprinting()) {
                return;
            }
            if (cfg.stopOnGround && mc.player.onGround()) {
                mc.player.setSprinting(false);
            } else if (cfg.stopOnAir && !mc.player.onGround()) {
                mc.player.setSprinting(false);
            }
        }
    }

    private static void handleOmnidirectional(SprintConfig cfg) {
        if (!SprintHandler.canSprint(cfg)) {
            return;
        }
        Minecraft mc = SprintHandler.getMc();
        if (mc == null) {
            return;
        }
        mc.player.setSprinting(true);
    }

    private static void handleOmnirotational(SprintConfig cfg) {
        Minecraft mc = SprintHandler.getMc();
        if (mc == null) {
            return;
        }
        realYaw = mc.player.getYRot();
        realPitch = mc.player.getXRot();
        savedUp = mc.options.keyUp.isDown();
        savedDown = mc.options.keyDown.isDown();
        savedLeft = mc.options.keyLeft.isDown();
        savedRight = mc.options.keyRight.isDown();
        int curHash = 0;
        if (savedUp) {
            curHash |= 1;
        }
        if (savedDown) {
            curHash |= 2;
        }
        if (savedLeft) {
            curHash |= 4;
        }
        if (savedRight) {
            curHash |= 8;
        }
        if (curHash == 0) {
            smoothYaw = Float.NaN;
            lastKeyHash = 0;
            if (mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
            return;
        }
        if (!SprintHandler.canSprint(cfg)) {
            return;
        }
        mc.player.setSprinting(true);
        if (curHash != lastKeyHash) {
            smoothYaw = Float.NaN;
            lastKeyHash = curHash;
        }
        float targetYaw = SprintHandler.getMovementDirection(realYaw, savedUp, savedDown, savedLeft, savedRight);
        if (cfg.smoothRotation && cfg.rotationSpeed > 0) {
            if (Float.isNaN(smoothYaw)) {
                smoothYaw = realYaw;
            }
            float factor = Math.max(0.05f, Math.min(1.0f, cfg.rotationSpeed / 180.0f));
            smoothYaw = SprintHandler.lerpAngle(smoothYaw, targetYaw, factor);
        } else {
            smoothYaw = targetYaw;
        }
        mc.player.setYRot(smoothYaw += ((Math.random() - 0.5) * 0.002));
        yawModified = true;
        overrideInputThisTick = true;
    }

    private static float lerpAngle(float from, float to, float factor) {
        float delta;
        for (delta = to - from; delta > 180.0f; delta -= 360.0f) {
        }
        while (delta < -180.0f) {
            delta += 360.0f;
        }
        if (Math.abs(delta) < 0.05f) {
            return to;
        }
        return from + delta * factor;
    }

    private static boolean canSprint(SprintConfig cfg) {
        boolean isHungry;
        Minecraft mc = SprintHandler.getMc();
        if (mc == null || mc.player == null) {
            return false;
        }
        boolean bl = isHungry = mc.player.getFoodData().getFoodLevel() <= 6 && !mc.player.isCreative();
        if (isHungry && !cfg.ignoreHunger) {
            return false;
        }
        if (mc.player.hasEffect(MobEffects.BLINDNESS) && !cfg.ignoreBlindness) {
            return false;
        }
        if (mc.player.horizontalCollision && !cfg.ignoreCollision) {
            return false;
        }
        if (!(savedUp || savedDown || savedLeft || savedRight)) {
            return false;
        }
        if (mc.player.isShiftKeyDown()) {
            return false;
        }
        if (mc.player.isPassenger()) {
            return false;
        }
        return !mc.player.isInWater() && !mc.player.isInLava();
    }

    private static void restoreAll() {
        Minecraft mc = SprintHandler.getMc();
        if (yawModified && mc != null && mc.player != null) {
            mc.player.setYRot(realYaw);
            mc.player.setXRot(realPitch);
            yawModified = false;
        }
        overrideInputThisTick = false;
        smoothYaw = Float.NaN;
        lastKeyHash = 0;
    }

    private static float getMovementDirection(float baseYaw, boolean forward, boolean backward, boolean left, boolean right) {
        int fwd = (forward ? 1 : 0) - (backward ? 1 : 0);
        int strafe = (left ? 1 : 0) - (right ? 1 : 0);
        if (fwd == 0 && strafe == 0) {
            return baseYaw;
        }
        float delta = 0.0f;
        if (fwd > 0) {
            if (strafe > 0) {
                delta = -45.0f;
            } else if (strafe < 0) {
                delta = 45.0f;
            }
        } else if (fwd < 0) {
            delta = strafe > 0 ? -135.0f : (strafe < 0 ? 135.0f : 180.0f);
        } else if (strafe > 0) {
            delta = -90.0f;
        } else if (strafe < 0) {
            delta = 90.0f;
        }
        return baseYaw + delta;
    }

    static {
        smoothYaw = Float.NaN;
        overrideInputThisTick = false;
        lastKeyHash = 0;
    }
}

