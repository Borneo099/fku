package lexis.Server.Commandsavailabletoplayers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PlayerInventoryViewer {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final SuggestionProvider PLAYER_SUGGESTIONS = (context, builder) -> {
      List playerNames = new ArrayList();
      Iterator var3 = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11314_().iterator();

      while(var3.hasNext()) {
         ServerPlayer player = (ServerPlayer)var3.next();
         playerNames.add(player.m_7755_().getString());
      }

      return SharedSuggestionProvider.m_82970_(playerNames, builder);
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("Lexis").requires((source) -> {
         return source.m_6761_(2);
      })).then(Commands.m_82127_("InventoryView").then(Commands.m_82129_("玩家名", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).executes((context) -> {
         String targetName = StringArgumentType.getString(context, "玩家名");
         ServerPlayer targetPlayer = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11255_(targetName);
         if (targetPlayer == null) {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c找不到玩家: " + targetName));
            return 0;
         } else {
            ServerPlayer viewer = ((CommandSourceStack)context.getSource()).m_81375_();
            openInventoryViewer(viewer, targetPlayer);
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已打开 " + targetName + " 的背包");
            }, true);
            return 1;
         }
      }))));
   }

   private static void openInventoryViewer(ServerPlayer viewer, ServerPlayer target) {
      viewer.m_5893_(new SimpleMenuProvider((containerId, playerInventory, player) -> {
         return new InventoryViewContainer(containerId, playerInventory, viewer, target);
      }, Component.m_237113_("§6" + target.m_7755_().getString() + " 的背包")));
   }

   private static class InventoryViewContainer extends AbstractContainerMenu {
      private final ServerPlayer viewer;
      private final ServerPlayer targetPlayer;
      private static final int TARGET_MAIN_START = 0;
      private static final int TARGET_MAIN_END = 27;
      private static final int TARGET_HOTBAR_START = 27;
      private static final int TARGET_HOTBAR_END = 36;
      private static final int TARGET_ARMOR_START = 36;
      private static final int TARGET_ARMOR_END = 40;
      private static final int TARGET_OFFHAND = 40;
      private static final int TARGET_TOTAL_SLOTS = 41;
      private static final int VIEWER_MAIN_START = 41;
      private static final int VIEWER_MAIN_END = 68;
      private static final int VIEWER_HOTBAR_START = 68;
      private static final int VIEWER_HOTBAR_END = 77;

      protected InventoryViewContainer(int containerId, Inventory viewerInventory, ServerPlayer viewer, ServerPlayer target) {
         super(MenuType.f_39962_, containerId);
         this.viewer = viewer;
         this.targetPlayer = target;

         int i;
         for(i = 0; i < 27; ++i) {
            int slotIndex = 9 + i;
            this.m_38897_(new TargetSlot(target.m_150109_(), slotIndex, 8 + i % 9 * 18, 18 + i / 9 * 18));
         }

         int i;
         for(i = 0; i < 9; ++i) {
            i = 27 + i;
            this.m_38897_(new TargetSlot(target.m_150109_(), i, 8 + i * 18, 76));
         }

         int[] armorSlots = new int[]{36, 37, 38, 39};
         String[] armorNames = new String[]{"靴子", "护腿", "胸甲", "头盔"};

         int slotIndex;
         for(i = 0; i < 4; ++i) {
            slotIndex = 36 + i;
            this.m_38897_((new TargetSlot(target.m_150109_(), armorSlots[i], 8 + i * 18, 112)).setName(Component.m_237113_("§7" + armorNames[i])));
         }

         this.m_38897_((new TargetSlot(target.m_150109_(), 40, 80, 112)).setName(Component.m_237113_("§7副手")));

         int guiSlot;
         for(i = 0; i < 27; ++i) {
            slotIndex = 9 + i;
            guiSlot = 41 + i;
            this.m_38897_(new Slot(viewerInventory, slotIndex, 8 + i % 9 * 18, 148 + i / 9 * 18));
         }

         for(i = 0; i < 9; ++i) {
            guiSlot = 68 + i;
            this.m_38897_(new Slot(viewerInventory, i, 8 + i * 18, 206));
         }

      }

      public ItemStack m_7648_(Player player, int index) {
         Slot slot = (Slot)this.f_38839_.get(index);
         if (slot != null && slot.m_6657_()) {
            ItemStack stack = slot.m_7993_();
            ItemStack copy = stack.m_41777_();
            if (index < 41) {
               if (!this.m_38903_(stack, 41, 77, true)) {
                  return ItemStack.f_41583_;
               }
            } else if (!this.m_38903_(stack, 0, 41, false)) {
               return ItemStack.f_41583_;
            }

            if (stack.m_41619_()) {
               slot.m_5852_(ItemStack.f_41583_);
            } else {
               slot.m_6654_();
            }

            return copy;
         } else {
            return ItemStack.f_41583_;
         }
      }

      public void m_150399_(int slotId, int button, ClickType clickType, Player player) {
         super.m_150399_(slotId, button, clickType, player);
         if (this.targetPlayer != null && !this.targetPlayer.m_9236_().f_46443_) {
            this.targetPlayer.f_36095_.m_38946_();
         }

      }

      public void m_6877_(Player player) {
         super.m_6877_(player);
         if (this.targetPlayer != null && !this.targetPlayer.m_9236_().f_46443_) {
            this.targetPlayer.f_36095_.m_38946_();
         }

      }

      public boolean m_6875_(Player player) {
         return true;
      }
   }

   private static class TargetSlot extends Slot {
      private Component customName = null;

      public TargetSlot(Inventory inventory, int slot, int x, int y) {
         super(inventory, slot, x, y);
      }

      public TargetSlot setName(Component name) {
         this.customName = name;
         return this;
      }

      public boolean m_5857_(ItemStack stack) {
         return true;
      }

      public boolean m_8010_(Player player) {
         return true;
      }
   }
}
