package lexis.Hack.Hacks.Items;

import lexis.Hack.Hack;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketSendListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;

public class RetainCraftingHack extends Hack implements PacketSendListener {
   public static boolean isEnabledStatic = false;

   public RetainCraftingHack() {
      super("保留合成格", new String[]{"关闭背包后保留2x2合成格中的物品", "§6§l可用：放入物品在2x2 服务器指令data读取不到你的有物品数据不存在", "§c§l注意：生存 放入2x2物品 切换创造 重新打开/关闭背包 会弹出来到你的背包"}, Hack.Category.ITEMS, true);
   }

   public void onEnable() {
      isEnabledStatic = true;
      EventManager.add(PacketSendListener.class, this);
   }

   public void onDisable() {
      isEnabledStatic = false;
      EventManager.remove(PacketSendListener.class, this);
   }

   public void onUpdate() {
   }

   public void onPacketSend(PacketEvent.Send event) {
      if (isEnabledStatic) {
         Packet var3 = event.packet;
         if (var3 instanceof ServerboundContainerClosePacket) {
            ServerboundContainerClosePacket packet = (ServerboundContainerClosePacket)var3;
            if (packet.m_179585_() == 0) {
               Screen screen = mc.f_91080_;
               if (screen instanceof InventoryScreen) {
                  InventoryScreen inventoryScreen = (InventoryScreen)screen;
                  InventoryMenu var5 = (InventoryMenu)inventoryScreen.m_6262_();
                  CraftingContainer craftSlots = var5.m_39730_();

                  for(int i = 0; i < craftSlots.m_6643_(); ++i) {
                     if (!craftSlots.m_8020_(i).m_41619_()) {
                        event.cancel();
                        break;
                     }
                  }

               }
            }
         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
