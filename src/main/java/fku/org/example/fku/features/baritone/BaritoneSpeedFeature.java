package fku.org.example.fku.features.baritone;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//? if neoforge {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//? }

/**
 * Baritone 加速
 * <p>
 * Baritone 寻路时覆盖移动速度，并且可选择仅地面加速。
 * <p>
 * 参考：lexis.Hack.Hacks.Baritone.BaritoneSpeedHack
 */
@OnlyIn(Dist.CLIENT)
//? if neoforge {
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
//? } else {
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//? }
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
    //? if neoforge {
        public static void onTick(ClientTickEvent.Post event) {
    //? } else {
        public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
    //? }
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
