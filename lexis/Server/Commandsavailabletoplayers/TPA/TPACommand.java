package lexis.Server.Commandsavailabletoplayers.TPA;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class TPACommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final Map requests = new ConcurrentHashMap();
   private static final Map cooldownMap = new ConcurrentHashMap();
   private static final long REQUEST_TIMEOUT = 30000L;
   private static final long COOLDOWN_TIME = 3000L;

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("Lexis").then(((LiteralArgumentBuilder)Commands.m_82127_("tpa").then(Commands.m_82127_("accept").executes((context) -> {
         Entity patt2065$temp = ((CommandSourceStack)context.getSource()).m_81373_();
         if (patt2065$temp instanceof ServerPlayer player) {
            handleAccept(player);
         }

         return 1;
      }))).then(Commands.m_82127_("deny").executes((context) -> {
         Entity patt2440$temp = ((CommandSourceStack)context.getSource()).m_81373_();
         if (patt2440$temp instanceof ServerPlayer player) {
            handleDeny(player);
         }

         return 1;
      }))));
   }

   public static void openTPAMenu(ServerPlayer player) {
      player.m_5893_(new SimpleMenuProvider((containerId, playerInventory, p) -> {
         return new TPAMenuContainer(containerId, playerInventory, 1);
      }, Component.m_237113_("§b§l传送玩家 - 第1页")));
   }

   private static void sendTPARequest(ServerPlayer requester, ServerPlayer target) {
      UUID requesterId = requester.m_20148_();
      UUID targetId = target.m_20148_();
      requests.entrySet().removeIf((entry) -> {
         return ((TPARequest)entry.getValue()).targetId.equals(targetId) || System.currentTimeMillis() - ((TPARequest)entry.getValue()).timestamp > 30000L;
      });
      TPARequest request = new TPARequest(requesterId, targetId, requester.m_20183_());
      requests.put(requesterId, request);
      Component message = Component.m_237113_(String.format("%s§e%s §a请求传送到你的位置！\n", "§c[§6Lexis-Server§c] §f", requester.m_7755_().getString()));
      Component acceptBtn = Component.m_237113_("                      §a[同意]                      ").m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, "/Lexis tpa accept")).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§a点击同意传送请求"))));
      Component denyBtn = Component.m_237113_("                      §c[拒绝]                      ").m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, "/Lexis tpa deny")).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§c点击拒绝传送请求"))));
      target.m_213846_(message);
      target.m_213846_(acceptBtn);
      target.m_213846_(denyBtn);
      requester.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a已向 " + target.m_7755_().getString() + " 发送传送请求"));
   }

   private static void handleAccept(ServerPlayer player) {
      UUID playerId = player.m_20148_();
      Optional request = requests.entrySet().stream().filter((entryx) -> {
         return ((TPARequest)entryx.getValue()).targetId.equals(playerId);
      }).findFirst();
      if (request.isPresent()) {
         Map.Entry entry = (Map.Entry)request.get();
         TPARequest req = (TPARequest)entry.getValue();
         ServerPlayer requester = player.m_20194_().m_6846_().m_11259_(req.requesterId);
         if (requester != null && requester.m_6084_()) {
            requester.m_6021_(player.m_20185_(), player.m_20186_(), player.m_20189_());
            requester.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a" + player.m_7755_().getString() + " 已同意你的传送请求！"));
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a已同意 " + requester.m_7755_().getString() + " 的传送请求"));
         } else {
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c请求者已离线或不存在"));
         }

         requests.remove(entry.getKey());
      } else {
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你没有待处理的传送请求"));
      }

   }

   private static void handleDeny(ServerPlayer player) {
      UUID playerId = player.m_20148_();
      Optional request = requests.entrySet().stream().filter((entryx) -> {
         return ((TPARequest)entryx.getValue()).targetId.equals(playerId);
      }).findFirst();
      if (request.isPresent()) {
         Map.Entry entry = (Map.Entry)request.get();
         TPARequest req = (TPARequest)entry.getValue();
         ServerPlayer requester = player.m_20194_().m_6846_().m_11259_(req.requesterId);
         if (requester != null) {
            requester.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c" + player.m_7755_().getString() + " 拒绝了你的传送请求"));
         }

         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c已拒绝传送请求"));
         requests.remove(entry.getKey());
      } else {
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你没有待处理的传送请求"));
      }

   }

   private static ItemStack createPlayerHead(ServerPlayer player) {
      ItemStack head = new ItemStack(Items.f_42680_);
      head.m_41714_(Component.m_237113_("§e" + player.m_7755_().getString()));
      CompoundTag tag = head.m_41784_();
      tag.m_128359_("SkullOwner", player.m_7755_().getString());
      head.m_41751_(tag);
      return head;
   }

   private static class TPARequest {
      final UUID requesterId;
      final UUID targetId;
      final BlockPos pos;
      final long timestamp;

      TPARequest(UUID requesterId, UUID targetId, BlockPos pos) {
         this.requesterId = requesterId;
         this.targetId = targetId;
         this.pos = pos;
         this.timestamp = System.currentTimeMillis();
      }
   }

   private static class TPAMenuContainer extends AbstractContainerMenu {
      private final int currentPage;
      private final List onlinePlayers;
      private static final int SLOTS_PER_PAGE = 54;

      protected TPAMenuContainer(int containerId, Inventory playerInventory, int page) {
         super(MenuType.f_39962_, containerId);
         this.currentPage = page;
         ServerPlayer viewer = (ServerPlayer)playerInventory.f_35978_;
         this.onlinePlayers = new ArrayList(viewer.m_20194_().m_6846_().m_11314_());
         this.onlinePlayers.remove(viewer);
         int startIndex = (page - 1) * 52;
         int endIndex = Math.min(startIndex + 52, this.onlinePlayers.size());
         int slotIndex = 0;

         for(int i = startIndex; i < endIndex; ++i) {
            ServerPlayer target = (ServerPlayer)this.onlinePlayers.get(i);
            ItemStack playerHead = TPACommand.createPlayerHead(target);
            this.m_38897_(new TPASlot(playerHead, slotIndex, 8 + slotIndex % 9 * 18, 18 + slotIndex / 9 * 18, target));
            ++slotIndex;
         }

         while(slotIndex < 52) {
            this.m_38897_(new LockedSlot(ItemStack.f_41583_, slotIndex, 8 + slotIndex % 9 * 18, 18 + slotIndex / 9 * 18));
            ++slotIndex;
         }

         ItemStack nextPage;
         if (page > 1) {
            nextPage = new ItemStack(Items.f_42412_);
            nextPage.m_41714_(Component.m_237113_("§a上一页"));
            this.m_38897_(new PageSlot(nextPage, 52, 62, 108, page - 1));
         } else {
            this.m_38897_(new LockedSlot(ItemStack.f_41583_, 52, 62, 108));
         }

         if (endIndex < this.onlinePlayers.size()) {
            nextPage = new ItemStack(Items.f_42412_);
            nextPage.m_41714_(Component.m_237113_("§a下一页"));
            this.m_38897_(new PageSlot(nextPage, 53, 98, 108, page + 1));
         } else {
            this.m_38897_(new LockedSlot(ItemStack.f_41583_, 53, 98, 108));
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
            if (slotId >= 0 && slotId < this.f_38839_.size()) {
               Slot slot = (Slot)this.f_38839_.get(slotId);
               if (slot instanceof TPASlot) {
                  TPASlot tpaSlot = (TPASlot)slot;
                  if (tpaSlot.target != null) {
                     UUID playerId = serverPlayer.m_20148_();
                     long currentTime = System.currentTimeMillis();
                     if (TPACommand.cooldownMap.containsKey(playerId) && currentTime - (Long)TPACommand.cooldownMap.get(playerId) < 3000L) {
                        serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c请等待 3 秒后再发送请求"));
                        return;
                     }

                     TPACommand.sendTPARequest(serverPlayer, tpaSlot.target);
                     TPACommand.cooldownMap.put(playerId, currentTime);
                     return;
                  }
               }

               if (slot instanceof PageSlot) {
                  PageSlot pageSlot = (PageSlot)slot;
                  serverPlayer.m_5893_(new SimpleMenuProvider((cid, inv, p) -> {
                     return new TPAMenuContainer(cid, inv, pageSlot.page);
                  }, Component.m_237113_("§b§l传送玩家 - 第" + pageSlot.page + "页")));
               }
            }
         }

      }
   }

   private static class LockedSlot extends Slot {
      private final ItemStack displayStack;

      public LockedSlot(ItemStack stack, int slot, int x, int y) {
         super(new SimpleContainer(new ItemStack[]{stack}), 0, x, y);
         this.displayStack = stack.m_41777_();
      }

      public boolean m_5857_(ItemStack stack) {
         return false;
      }

      public boolean m_8010_(Player player) {
         return false;
      }

      public ItemStack m_7993_() {
         return this.displayStack;
      }

      public void m_5852_(ItemStack stack) {
      }

      public void m_6654_() {
      }

      public int m_6641_() {
         return 0;
      }

      public ItemStack m_6201_(int amount) {
         return ItemStack.f_41583_;
      }
   }

   private static class PageSlot extends LockedSlot {
      final int page;

      public PageSlot(ItemStack stack, int slot, int x, int y, int page) {
         super(stack, slot, x, y);
         this.page = page;
      }
   }

   private static class TPASlot extends LockedSlot {
      final ServerPlayer target;

      public TPASlot(ItemStack stack, int slot, int x, int y, ServerPlayer target) {
         super(stack, slot, x, y);
         this.target = target;
      }
   }
}
