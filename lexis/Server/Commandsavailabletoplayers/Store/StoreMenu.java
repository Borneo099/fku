package lexis.Server.Commandsavailabletoplayers.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StoreMenu {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final int SLOTS_PER_PAGE = 54;
   private static final int ITEMS_PER_PAGE = 45;

   public static void openStore(ServerPlayer player, int page) {
      player.m_5893_(new SimpleMenuProvider((containerId, playerInventory, p) -> {
         return new StoreContainer(containerId, playerInventory, page);
      }, Component.m_237113_("§6§l商店 - 第" + page + "页")));
   }

   private static class StoreContainer extends AbstractContainerMenu {
      private final int currentPage;
      private final List items;
      private final ServerPlayer player;

      protected StoreContainer(int containerId, Inventory playerInventory, int page) {
         super(MenuType.f_39962_, containerId);
         this.currentPage = page;
         this.player = (ServerPlayer)playerInventory.f_35978_;
         this.items = new ArrayList(StoreCommand.getConfig().getItems().values());
         ItemStack infoPaper = this.createInfoPaper(0);
         this.m_38897_(new StoreSlot(infoPaper, 0, 8, 18, -1, (StoreConfig.StoreItem)null));
         ItemStack helpPaper = this.createInfoPaper(1);
         this.m_38897_(new StoreSlot(helpPaper, 1, 26, 18, -1, (StoreConfig.StoreItem)null));

         int startIndex;
         for(startIndex = 2; startIndex < 9; ++startIndex) {
            ItemStack separator = this.createSeparator();
            this.m_38897_(new StoreSlot(separator.m_41777_(), startIndex, 8 + startIndex * 18, 18, -2, (StoreConfig.StoreItem)null));
         }

         startIndex = (page - 1) * 45;
         int endIndex = Math.min(startIndex + 45, this.items.size());
         int slotIndex = 10;

         for(int i = startIndex; i < endIndex; ++i) {
            StoreConfig.StoreItem storeItem = (StoreConfig.StoreItem)this.items.get(i);
            ItemStack displayStack = this.createDisplayStack(storeItem);
            int row = (slotIndex - 10) / 9;
            int col = (slotIndex - 10) % 9;
            int x = 8 + col * 18;
            int y = 62 + row * 18;
            this.m_38897_(new StoreSlot(displayStack, slotIndex, x, y, i, storeItem));
            ++slotIndex;
         }

         while(slotIndex < 54) {
            this.m_38897_(new StoreSlot(ItemStack.f_41583_, slotIndex, 8 + (slotIndex - 10) % 9 * 18, 62 + (slotIndex - 10) / 9 * 18, -3, (StoreConfig.StoreItem)null));
            ++slotIndex;
         }

         ItemStack nextPage;
         if (page > 1) {
            nextPage = new ItemStack(Items.f_42412_);
            nextPage.m_41714_(Component.m_237113_("§a上一页"));
            this.m_38897_(new PageSlot(nextPage, 52, 62, 158, page - 1));
         }

         if (endIndex < this.items.size()) {
            nextPage = new ItemStack(Items.f_42412_);
            nextPage.m_41714_(Component.m_237113_("§a下一页"));
            this.m_38897_(new PageSlot(nextPage, 53, 98, 158, page + 1));
         }

      }

      private ItemStack createInfoPaper(int slotIndex) {
         ItemStack paper = new ItemStack(Items.f_42516_);
         String nameJson;
         if (slotIndex == 0) {
            nameJson = "[{\"text\":\"§6§l你的信息\"}]";
            paper.m_41698_("display").m_128359_("Name", nameJson);
            UUID playerId = this.player.m_20148_();
            int money = StoreCommand.getConfig().getPlayerMoney(playerId);
            int onlinePlayers = this.player.m_20194_().m_6846_().m_11309_();
            long onlineSeconds = StoreConfig.getInstance().getPlayerOnlineTime(playerId);
            long days = onlineSeconds / 86400L;
            long hours = onlineSeconds % 86400L / 3600L;
            long minutes = onlineSeconds % 3600L / 60L;
            long seconds = onlineSeconds % 60L;
            ListTag loreList = new ListTag();
            loreList.add(StringTag.m_129297_("[{\"text\":\"§e玩家: §f" + this.player.m_7755_().getString() + "\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§e在线玩家: §a" + onlinePlayers + "\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§e金币: §a" + money + "\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§e累计在线: §a" + days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§7点击物品购买\"}]"));
            paper.m_41698_("display").m_128365_("Lore", loreList);
         }

         if (slotIndex == 1) {
            nameJson = "[{\"text\":\"§6§l如何怎么赚金币？\"}]";
            paper.m_41698_("display").m_128359_("Name", nameJson);
            ListTag loreList = new ListTag();
            loreList.add(StringTag.m_129297_("[{\"text\":\"§7你要在线这服务器 1分钟 = 奖励一个金币\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§7你可以击杀玩家有奖励 要击杀5个玩家 可以奖励 35个金币！\"}]"));
            loreList.add(StringTag.m_129297_("[{\"text\":\"§7你需要放置方块 256个 有奖励 120个金币！\"}]"));
            paper.m_41698_("display").m_128365_("Lore", loreList);
         }

         return paper;
      }

      private ItemStack createSeparator() {
         ItemStack separator = new ItemStack(Items.f_42183_);
         String nameJson = "[{\"text\":\"§8别看！这没有卖\"}]";
         separator.m_41698_("display").m_128359_("Name", nameJson);
         return separator;
      }

      private ItemStack createDisplayStack(StoreConfig.StoreItem storeItem) {
         ItemStack original = storeItem.toItemStack();
         ItemStack display = original.m_41777_();
         Component originalName = display.m_41786_();
         String displayName = originalName.getString();
         String nameJson = "[{\"text\":\"" + displayName + "\"}]";
         display.m_41698_("display").m_128359_("Name", nameJson);
         CompoundTag displayTag = display.m_41698_("display");
         ListTag loreList = displayTag.m_128441_("Lore") ? displayTag.m_128437_("Lore", 8) : new ListTag();
         ListTag originalLore = new ListTag();

         for(int i = 0; i < loreList.size(); ++i) {
            originalLore.add(loreList.get(i));
         }

         loreList.add(StringTag.m_129297_("[{\"text\":\"§7出售者: §f" + storeItem.sellerName + "\"}]"));
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7价格: §a" + storeItem.price + " 金币\"}]"));
         int var10001 = storeItem.maxSales - storeItem.currentSales;
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7可买次数: §e" + var10001 + "/" + storeItem.maxSales + "\"}]"));
         CompoundTag lexisTag = display.m_41698_("lexis_store");
         lexisTag.m_128365_("OriginalLore", originalLore);
         displayTag.m_128365_("Lore", loreList);
         display.m_41700_("display", displayTag);
         return display;
      }

      public ItemStack m_7648_(Player player, int index) {
         return ItemStack.f_41583_;
      }

      public boolean m_6875_(Player player) {
         return true;
      }

      public void m_150399_(int slotId, int button, ClickType clickType, Player player) {
         if (player instanceof ServerPlayer serverPlayer) {
            if (slotId >= 0 && slotId < this.f_38839_.size()) {
               Slot slot = (Slot)this.f_38839_.get(slotId);
               if (slot instanceof PageSlot) {
                  PageSlot pageSlot = (PageSlot)slot;
                  StoreMenu.openStore(serverPlayer, pageSlot.page);
                  return;
               }

               if (slot instanceof StoreSlot) {
                  StoreSlot storeSlot = (StoreSlot)slot;
                  if (storeSlot.itemIndex >= 0 && storeSlot.storeItem != null) {
                     this.handlePurchase(serverPlayer, storeSlot.storeItem);
                  }
               }
            }
         }

      }

      private void handlePurchase(ServerPlayer buyer, StoreConfig.StoreItem storeItem) {
         if (!storeItem.canBuy()) {
            buyer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c抱歉！物品已卖完！"));
            StoreCommand.getConfig().removeItem(storeItem.itemName);
            StoreMenu.openStore(buyer, this.currentPage);
         } else {
            UUID buyerId = buyer.m_20148_();
            int buyerMoney = StoreCommand.getConfig().getPlayerMoney(buyerId);
            if (buyerMoney < storeItem.price) {
               buyer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c金币不足！需要 " + storeItem.price + " 金币"));
            } else {
               StoreCommand.getConfig().removePlayerMoney(buyerId, storeItem.price);
               StoreCommand.getConfig().addPlayerMoney(storeItem.sellerId, storeItem.price);
               storeItem.buy();
               ItemStack item = storeItem.toItemStack();
               CompoundTag lexisTag = item.m_41737_("lexis_store");
               if (lexisTag != null && lexisTag.m_128441_("OriginalLore")) {
                  CompoundTag displayTag = item.m_41698_("display");
                  displayTag.m_128365_("Lore", lexisTag.m_128437_("OriginalLore", 8));
                  item.m_41700_("display", displayTag);
               }

               buyer.m_150109_().m_36054_(item);
               buyer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a成功购买 " + storeItem.itemName));
               ServerPlayer seller = buyer.m_20194_().m_6846_().m_11259_(storeItem.sellerId);
               if (seller != null) {
                  String var10001 = storeItem.itemName;
                  seller.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§e你的物品 " + var10001 + " 被 " + buyer.m_7755_().getString() + " 购买，剩余次数 " + (storeItem.maxSales - storeItem.currentSales) + "/" + storeItem.maxSales));
               }

               if (storeItem.currentSales >= storeItem.maxSales) {
                  StoreCommand.getConfig().removeItem(storeItem.itemName);
                  buyer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§e物品已卖完，已从商店移除"));
               } else {
                  StoreCommand.getConfig().save();
               }

               StoreMenu.openStore(buyer, this.currentPage);
            }
         }
      }

      private static class StoreSlot extends Slot {
         final int itemIndex;
         final StoreConfig.StoreItem storeItem;

         public StoreSlot(ItemStack stack, int slot, int x, int y, int itemIndex, StoreConfig.StoreItem storeItem) {
            super(new SimpleContainer(new ItemStack[]{stack}), 0, x, y);
            this.itemIndex = itemIndex;
            this.storeItem = storeItem;
         }

         public boolean m_5857_(ItemStack stack) {
            return false;
         }

         public boolean m_8010_(Player player) {
            return false;
         }
      }

      private static class PageSlot extends Slot {
         final int page;

         public PageSlot(ItemStack stack, int slot, int x, int y, int page) {
            super(new SimpleContainer(new ItemStack[]{stack}), 0, x, y);
            this.page = page;
         }

         public boolean m_5857_(ItemStack stack) {
            return false;
         }

         public boolean m_8010_(Player player) {
            return false;
         }
      }
   }
}
