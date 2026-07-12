package lexis.Server.Commandsavailabletoplayers.Menu;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import lexis.Server.Commandsavailabletoplayers.Store.StoreMenu;
import lexis.Server.Commandsavailabletoplayers.TPA.TPACommand;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LexisMenuHandler {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final String MENU_TAG = "lexiscd:1b";
   private static final Map cooldownMap = new HashMap();
   private static final long COOLDOWN_MS = 1000L;
   private static int hue = 0;

   @SubscribeEvent
   public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (!NoMenuCommand.isNoMenuEnabled()) {
            giveMenuCompass(player);
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         ItemStack stack = event.getItemStack();
         if (isMenuCompass(stack)) {
            if (NoMenuCommand.isNoMenuEnabled()) {
               event.setCanceled(true);
               player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c对不起，Lexis的菜单 服务器已经禁止了"));
               return;
            }

            event.setCanceled(true);
            UUID playerId = player.m_20148_();
            long currentTime = System.currentTimeMillis();
            if (cooldownMap.containsKey(playerId) && currentTime - (Long)cooldownMap.get(playerId) < 1000L) {
               return;
            }

            cooldownMap.put(playerId, currentTime);
            openMainMenu(player);
         }
      }

   }

   @SubscribeEvent
   public static void onItemPickup(EntityItemPickupEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         ItemStack stack = event.getItem().m_32055_();
         if (isMenuCompass(stack)) {
            event.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public static void onItemToss(ItemTossEvent event) {
      Player player = event.getPlayer();
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack stack = event.getEntity().m_32055_();
         if (isMenuCompass(stack)) {
            event.setCanceled(true);
         }
      }

   }

   private static boolean isMenuCompass(ItemStack stack) {
      if (stack.m_41720_() != Items.f_42522_) {
         return false;
      } else {
         CompoundTag tag = stack.m_41783_();
         return tag != null && tag.m_128441_("lexiscd") && tag.m_128445_("lexiscd") == 1;
      }
   }

   private static void giveMenuCompass(ServerPlayer player) {
      ItemStack compass = new ItemStack(Items.f_42522_);
      CompoundTag tag = new CompoundTag();
      tag.m_128344_("lexiscd", (byte)1);
      compass.m_41751_(tag);
      compass.m_41714_(Component.m_237113_("§d§lLexis菜单 §7(右键打开)"));
      boolean hasCompass = false;
      Iterator var4 = player.m_150109_().f_35974_.iterator();

      while(var4.hasNext()) {
         ItemStack stack = (ItemStack)var4.next();
         if (isMenuCompass(stack)) {
            hasCompass = true;
            break;
         }
      }

      if (!hasCompass) {
         player.m_150109_().m_36054_(compass);
      }

   }

   static void openMainMenu(ServerPlayer player) {
      player.m_5893_(new SimpleMenuProvider((containerId, playerInventory, p) -> {
         return new MainMenuContainer(containerId, playerInventory);
      }, getRainbowTitle("Lexis的菜单")));
   }

   private static Component getRainbowTitle(String text) {
      StringBuilder rainbow = new StringBuilder();
      char[] chars = text.toCharArray();
      char[] var3 = chars;
      int var4 = chars.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         char c = var3[var5];
         int color = Color.HSBtoRGB((float)hue / 360.0F, 0.8F, 1.0F);
         String hexColor = String.format("#%06X", 16777215 & color);
         rainbow.append("§x");
         char[] var9 = hexColor.substring(1).toCharArray();
         int var10 = var9.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            char hex = var9[var11];
            rainbow.append('§').append(hex);
         }

         rainbow.append(c);
         hue = (hue + 10) % 360;
      }

      return Component.m_237113_(rainbow.toString());
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         hue = (hue + 1) % 360;
      }

   }

   private static void openGameModeMenu(ServerPlayer player) {
      player.m_5893_(new SimpleMenuProvider((containerId, playerInventory, p) -> {
         return new GameModeMenuContainer(containerId, playerInventory);
      }, Component.m_237113_("§6§l选择游戏模式")));
   }

   private static class GameModeMenuContainer extends AbstractContainerMenu {
      protected GameModeMenuContainer(int containerId, Inventory playerInventory) {
         super(MenuType.f_39957_, containerId);
         ItemStack creative = new ItemStack(Items.f_42276_);
         creative.m_41714_(Component.m_237113_("§b创造模式").m_130940_(ChatFormatting.BOLD));
         this.m_38897_(new GameModeSlot(creative, 0, 26, 18, 0));
         ItemStack survival = new ItemStack(Items.f_42383_);
         survival.m_41714_(Component.m_237113_("§a生存模式").m_130940_(ChatFormatting.BOLD));
         this.m_38897_(new GameModeSlot(survival, 1, 62, 18, 1));
         ItemStack adventure = new ItemStack(Items.f_42676_);
         adventure.m_41714_(Component.m_237113_("§e冒险模式").m_130940_(ChatFormatting.BOLD));
         this.m_38897_(new GameModeSlot(adventure, 2, 98, 18, 2));
         this.m_38897_(new LockedSlot(ItemStack.f_41583_, 3, 134, 18));
         int[] emptySlots = new int[]{0, 2, 4, 6, 8};

         for(int i = 0; i < emptySlots.length; ++i) {
            this.m_38897_(new LockedSlot(ItemStack.f_41583_, 4 + i, 8 + emptySlots[i] * 18, 18));
         }

      }

      public ItemStack m_7648_(Player player, int index) {
         return ItemStack.f_41583_;
      }

      public boolean m_6875_(Player player) {
         return true;
      }

      public void m_150399_(int slotId, int button, ClickType clickType, Player player) {
         if (player instanceof ServerPlayer serverPlayer) {
            if (slotId >= 0 && slotId < 3) {
               Object var7 = this.f_38839_.get(slotId);
               if (var7 instanceof GameModeSlot) {
                  GameModeSlot slot = (GameModeSlot)var7;
                  switch (slot.mode) {
                     case 0:
                        serverPlayer.m_143403_(GameType.CREATIVE);
                        serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f已切换到创造模式"));
                        break;
                     case 1:
                        serverPlayer.m_143403_(GameType.SURVIVAL);
                        serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f已切换到生存模式"));
                        break;
                     case 2:
                        serverPlayer.m_143403_(GameType.ADVENTURE);
                        serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f已切换到冒险模式"));
                  }

                  serverPlayer.m_6915_();
               }
            }
         }

      }
   }

   private static class MainMenuContainer extends AbstractContainerMenu {
      protected MainMenuContainer(int containerId, Inventory playerInventory) {
         super(MenuType.f_39957_, containerId);
         ItemStack goldenApple = new ItemStack(Items.f_42437_);
         goldenApple.m_41714_(Component.m_237113_("§6§l更改游戏模式").m_130940_(ChatFormatting.BOLD));
         this.m_38897_(new MainMenuItem(goldenApple, 0, 44, 18, LexisMenuHandler.MenuAction.CHANGE_GAMEMODE));
         ItemStack enderPearl = new ItemStack(Items.f_42584_);
         enderPearl.m_41714_(Component.m_237113_("§b§l请求传送玩家").m_130940_(ChatFormatting.BOLD));
         this.m_38897_(new MainMenuItem(enderPearl, 1, 80, 18, LexisMenuHandler.MenuAction.TPA));
         ItemStack chest = new ItemStack(Items.f_42009_);
         String nameJson = "[{\"text\":\"§6§l商店\"}]";
         chest.m_41698_("display").m_128359_("Name", nameJson);
         ListTag loreList = new ListTag();
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7这可以买东西 可以支持 NBT物品 + NBT神器 + NBT其他东西\"}]"));
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7输入 /Lexis Store Sellitems <价格> <物品名称> 上架物品\"}]"));
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7输入 /Lexis Store Del <物品名称> 删除自己上架的物品\"}]"));
         loreList.add(StringTag.m_129297_("[{\"text\":\"§7注：房主的电脑会保存这配置 是本地！如果你更换服务器了 这可能不同物品！\"}]"));
         chest.m_41698_("display").m_128365_("Lore", loreList);
         this.m_38897_(new MainMenuItem(chest, 2, 116, 18, LexisMenuHandler.MenuAction.STORE));

         for(int i = 3; i < 9; ++i) {
            this.m_38897_(new LockedSlot(ItemStack.f_41583_, i, 8 + i * 18, 18));
         }

      }

      public ItemStack m_7648_(Player player, int index) {
         return ItemStack.f_41583_;
      }

      public boolean m_6875_(Player player) {
         return true;
      }

      public void m_150399_(int slotId, int button, ClickType clickType, Player player) {
         if (player instanceof ServerPlayer serverPlayer) {
            if (slotId >= 0 && slotId < 3) {
               Slot slot = (Slot)this.f_38839_.get(slotId);
               if (slot instanceof MainMenuItem) {
                  MainMenuItem item = (MainMenuItem)slot;
                  switch (item.action) {
                     case CHANGE_GAMEMODE:
                        LexisMenuHandler.openGameModeMenu(serverPlayer);
                        break;
                     case TPA:
                        TPACommand.openTPAMenu(serverPlayer);
                        break;
                     case STORE:
                        StoreMenu.openStore(serverPlayer, 1);
                  }
               }
            }
         }

      }
   }

   private static class LockedSlot extends Slot {
      public LockedSlot(ItemStack stack, int slot, int x, int y) {
         super(new SimpleContainer(new ItemStack[]{stack}), 0, x, y);
      }

      public boolean m_5857_(ItemStack stack) {
         return false;
      }

      public boolean m_8010_(Player player) {
         return false;
      }

      public void onClicked(Player player) {
      }
   }

   private static class GameModeSlot extends LockedSlot {
      final int mode;
      private final ItemStack displayStack;

      public GameModeSlot(ItemStack stack, int slot, int x, int y, int mode) {
         super(stack, slot, x, y);
         this.mode = mode;
         this.displayStack = stack.m_41777_();
      }

      public ItemStack m_7993_() {
         return this.displayStack;
      }
   }

   private static class MainMenuItem extends LockedSlot {
      final MenuAction action;
      private final ItemStack displayStack;

      public MainMenuItem(ItemStack stack, int slot, int x, int y, MenuAction action) {
         super(stack, slot, x, y);
         this.action = action;
         this.displayStack = stack.m_41777_();
      }

      public ItemStack m_7993_() {
         return this.displayStack;
      }
   }

   private static enum MenuAction {
      CHANGE_GAMEMODE,
      TPA,
      STORE;

      // $FF: synthetic method
      private static MenuAction[] $values() {
         return new MenuAction[]{CHANGE_GAMEMODE, TPA, STORE};
      }
   }
}
