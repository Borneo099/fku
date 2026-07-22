package fku.org.example.fku.features.baritone;

import fku.org.example.fku.features.baritone.BaritoneConfig;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class BaritoneSpeedFeature {
    private static final double VANILLA_SPEED = 0.1;
    private static boolean wasActive = false;

    public static boolean isEnabled() {
        return BaritoneConfig.getInstance().speedEnabled;
    }

    public static double getMultiplier() {
        return BaritoneConfig.getInstance().speedMultiplier;
    }

    public static boolean isGroundOnly() {
        return BaritoneConfig.getInstance().groundOnly;
    }

    public static void setEnabled(boolean v) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        cfg.speedEnabled = v;
        cfg.save();
        if (!v) {
            BaritoneSpeedFeature.restoreSpeed();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        if (!cfg.speedEnabled || mc.player == null || mc.level == null) {
            if (wasActive) {
                BaritoneSpeedFeature.restoreSpeed();
                wasActive = false;
            }
            return;
        }
        boolean active = BaritoneBridge.isActive();
        if (active) {
            if (cfg.groundOnly && !mc.player.onGround()) {
                BaritoneSpeedFeature.restoreSpeed();
                wasActive = true;
                return;
            }
            AttributeInstance attr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(0.1 * cfg.speedMultiplier);
            }
        }
        if (wasActive && !active) {
            BaritoneSpeedFeature.restoreSpeed();
        }
        wasActive = active;
    }

    private static void restoreSpeed() {
        AttributeInstance attr;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (attr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED)) != null) {
            attr.setBaseValue(0.1);
        }
    }
}

