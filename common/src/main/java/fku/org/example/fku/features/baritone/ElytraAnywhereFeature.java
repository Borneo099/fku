package fku.org.example.fku.features.baritone;

import fku.org.example.fku.features.baritone.BaritoneConfig;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class ElytraAnywhereFeature {
    private static boolean wasElytraEquipped = false;
    public static volatile boolean hasGoal = false;
    public static int goalX;
    public static int goalY;
    public static int goalZ;

    public static boolean isEnabled() {
        return BaritoneConfig.getInstance().elytraEnabled;
    }

    public static void setEnabled(boolean v) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        cfg.elytraEnabled = v;
        cfg.save();
        if (v) {
            wasElytraEquipped = false;
            if (!BaritoneBridge.isAvailable()) {
                return;
            }
            BaritoneBridge.suppressNextSetMessage();
            BaritoneBridge.executeCommand("set elytraTermsAccepted true");
        } else {
            hasGoal = false;
            wasElytraEquipped = false;
        }
    }

    public static void setGoal(int x, int y, int z) {
        goalX = x;
        goalY = y;
        goalZ = z;
        hasGoal = true;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        double dz;
        double dy;
        double dx;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        if (!cfg.elytraEnabled || mc.player == null) {
            if (hasGoal) {
                hasGoal = false;
                wasElytraEquipped = false;
            }
            return;
        }
        if (hasGoal && Math.sqrt((dx = mc.player.getX() - goalX) * dx + (dy = mc.player.getY() - goalY) * dy + (dz = mc.player.getZ() - goalZ) * dz) < 5.0) {
            BaritoneBridge.stop();
            hasGoal = false;
            wasElytraEquipped = false;
            return;
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
        return ElytraAnywhereFeature.isEnabled() && hasGoal && BaritoneBridge.isElytraActive();
    }

    public static void emergencyStop() {
        hasGoal = false;
    }
}

