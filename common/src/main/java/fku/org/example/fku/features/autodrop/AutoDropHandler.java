package fku.org.example.fku.features.autodrop;

import fku.org.example.fku.features.autodrop.AutoDropConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class AutoDropHandler {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) {
            return;
        }
        int interval = Math.max(1, Math.min(20, config.scanInterval));
        if (++tickCounter % interval != 0) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        LocalPlayer localPlayer = (LocalPlayer)player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) {
            return;
        }
        AbstractContainerMenu menu = localPlayer.containerMenu;
        if (!menu.getCarried().isEmpty()) {
            return;
        }
        for (int i = 0; i < menu.slots.size(); ++i) {
            String itemId;
            int playerSlotIndex;
            ItemStack stack;
            Slot slot = (Slot)menu.slots.get(i);
            if (slot.container != localPlayer.getInventory() || (stack = slot.getItem()).isEmpty() || (playerSlotIndex = slot.getSlotIndex()) < 0 || playerSlotIndex > 35 || !config.isBlacklisted(itemId = AutoDropHandler.getItemId(stack))) continue;
            mc.gameMode.handleInventoryMouseClick(menu.containerId, i, 1, ClickType.THROW, (Player)localPlayer);
            return;
        }
    }

    public static String getItemId(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }
}

