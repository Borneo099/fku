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
        if (mc.f_91072_ == null) {
            return;
        }
        AbstractContainerMenu menu = localPlayer.f_36096_;
        if (!menu.m_142621_().m_41619_()) {
            return;
        }
        for (int i = 0; i < menu.f_38839_.size(); ++i) {
            String itemId;
            int playerSlotIndex;
            ItemStack stack;
            Slot slot = (Slot)menu.f_38839_.get(i);
            if (slot.f_40218_ != localPlayer.m_150109_() || (stack = slot.m_7993_()).m_41619_() || (playerSlotIndex = slot.getSlotIndex()) < 0 || playerSlotIndex > 35 || !config.isBlacklisted(itemId = AutoDropHandler.getItemId(stack))) continue;
            mc.f_91072_.m_171799_(menu.f_38840_, i, 1, ClickType.THROW, (Player)localPlayer);
            return;
        }
    }

    public static String getItemId(ItemStack stack) {
        return ForgeRegistries.ITEMS.getKey(stack.m_41720_()).toString();
    }
}

