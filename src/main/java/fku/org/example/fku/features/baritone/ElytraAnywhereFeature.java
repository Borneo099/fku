package fku.org.example.fku.features.baritone;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
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
 * Baritone 允许任意维度鞘翅
 * <p>
 * 强制 Baritone 的鞘翅飞行在所有维度可用，解除 Y 轴限制。
 * 开启时自动接受 Baritone 的鞘翅条款。
 * <p>
 * 参考：lexis.Hack.Hacks.Baritone.ElytraAnywhereHack
 */
@OnlyIn(Dist.CLIENT)
//? if neoforge {
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
//? } else {
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
//? }
public class ElytraAnywhereFeature {

    private static boolean wasElytraEquipped = false;

    public static boolean isEnabled() {
        return BaritoneConfig.getInstance().elytraEnabled;
    }

    public static void setEnabled(boolean v) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        cfg.elytraEnabled = v;
        cfg.save();
        if (v) {
            wasElytraEquipped = false;
            if (!BaritoneBridge.isAvailable()) return;
            BaritoneBridge.suppressNextSetMessage();
            BaritoneBridge.executeCommand("set elytraTermsAccepted true");
        } else {
            hasGoal = false;
            wasElytraEquipped = false;
        }
    }

    public static volatile boolean hasGoal = false;
    public static int goalX, goalY, goalZ;

    public static void setGoal(int x, int y, int z) {
        goalX = x;
        goalY = y;
        goalZ = z;
        hasGoal = true;
    }

    @SubscribeEvent
    //? if neoforge {
        public static void onTick(ClientTickEvent.Post event) {
    //? } else {
        public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
    //? }
        Minecraft mc = Minecraft.getInstance();
        BaritoneConfig cfg = BaritoneConfig.getInstance();

        if (!cfg.elytraEnabled || mc.player == null) {
            if (hasGoal) {
                hasGoal = false;
                wasElytraEquipped = false;
            }
            return;
        }

        // 到达目标 5 格内自动停止
        if (hasGoal) {
            double dx = mc.player.getX() - goalX;
            double dy = mc.player.getY() - goalY;
            double dz = mc.player.getZ() - goalZ;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) < 5.0) {
                BaritoneBridge.stop();
                hasGoal = false;
                wasElytraEquipped = false;
                return;
            }
        }

        boolean hasElytra = mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        if (hasGoal && wasElytraEquipped && !hasElytra && BaritoneBridge.isAvailable()) {
            BaritoneBridge.stop();
            hasGoal = false;
            wasElytraEquipped = false;
            return;
        }
        wasElytraEquipped = hasElytra;
    }

    public static boolean isProtecting() {
        return isEnabled() && hasGoal && BaritoneBridge.isElytraActive();
    }

    public static void emergencyStop() {
        hasGoal = false;
    }
}
