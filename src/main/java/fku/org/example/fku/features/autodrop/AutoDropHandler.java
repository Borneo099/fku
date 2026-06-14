package fku.org.example.fku.features.autodrop;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
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
        if (!(player instanceof LocalPlayer)) return;

        LocalPlayer localPlayer = (LocalPlayer) player;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) return;

        // 无论背包是否打开，使用当前 player.containerMenu 处理
        // 如果没有打开任何 GUI，containerMenu 默认就是 inventoryMenu
        net.minecraft.world.inventory.AbstractContainerMenu menu = localPlayer.containerMenu;

        // 如果鼠标上正拖着物品，先不处理，避免逻辑混乱
        if (!menu.getCarried().isEmpty()) return;

        // 遍历当前容器的所有槽位
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            
            // 只处理属于玩家背包的槽位
            if (slot.container != localPlayer.getInventory()) continue;

            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            // 只处理主背包和快捷栏 (0-35)，跳过装备栏和副手
            int playerSlotIndex = slot.getSlotIndex();
            if (playerSlotIndex < 0 || playerSlotIndex > 35) continue;

            String itemId = getItemId(stack);
            if (config.isBlacklisted(itemId)) {
                // 使用 handleInventoryMouseClick 是同步服务端的唯一正确方式
                // containerId: 当前容器 ID
                // slotId: 在当前 menu 中的槽位索引 (i)
                // mouseButton: 1 表示丢弃整组 (Ctrl+Q 效果)，0 表示丢弃单个 (Q 效果)
                // clickType: THROW 专门用于丢弃操作
                mc.gameMode.handleInventoryMouseClick(
                    menu.containerId,
                    i,
                    1, 
                    ClickType.THROW,
                    localPlayer
                );
                
                // 每次 tick 只处理一个槽位，防止发包过快被服务端拦截，也给服务端同步留出时间
                return;
            }
        }
    }

    public static String getItemId(ItemStack stack) {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }
}