package fku.org.example.fku.features.autodrop;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutoDropHandler {
    private static int tickCounter = 0;
    private static final int SCAN_INTERVAL = 5;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        AutoDropConfig config = AutoDropConfig.getInstance();
        if (!config.enabled) return;

        tickCounter++;
        if (tickCounter % SCAN_INTERVAL != 0) return;

        Player player = event.player;
        if (player == null) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = getItemId(stack);
            if (config.isBlacklisted(itemId)) {
                if (config.dropAsEntity) {
                    player.drop(stack, false);
                }
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static String getItemId(ItemStack stack) {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }
}