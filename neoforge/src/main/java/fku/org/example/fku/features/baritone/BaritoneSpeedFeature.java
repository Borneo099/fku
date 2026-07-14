package fku.org.example.fku.features.baritone;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.tick.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Baritone 加速
 * <p>
 * Baritone 寻路时覆盖移动速度，并且可选择仅地面加速。
 * <p>
 * 参考：lexis.Hack.Hacks.Baritone.BaritoneSpeedHack
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
        if (!v) restoreSpeed();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = net.minecraft.client.Minecraft.getInstance();
        BaritoneConfig cfg = BaritoneConfig.getInstance();

        if (!cfg.speedEnabled || mc.player == null || mc.level == null) {
            if (wasActive) {
                restoreSpeed();
                wasActive = false;
            }
            return;
        }

        boolean active = BaritoneBridge.isActive();

        if (active) {
            if (cfg.groundOnly && !mc.player.onGround()) {
                restoreSpeed();
                wasActive = true;
                return;
            }
            var attr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(VANILLA_SPEED * cfg.speedMultiplier);
            }
        }

        if (wasActive && !active) {
            restoreSpeed();
        }
        wasActive = active;
    }

    private static void restoreSpeed() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            var attr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.setBaseValue(VANILLA_SPEED);
            }
        }
    }
}
