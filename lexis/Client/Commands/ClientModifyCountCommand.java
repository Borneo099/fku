package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientModifyCountCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)Commands.m_82127_("ModifyCount").then(Commands.m_82127_("setitemCount").then(Commands.m_82129_("count", IntegerArgumentType.integer(0, 127)).executes(ClientModifyCountCommand::setItemCount)))).then(Commands.m_82127_("setallitemCount").then(Commands.m_82129_("count", IntegerArgumentType.integer(0, 127)).executes(ClientModifyCountCommand::setAllItemCount))))));
   }

   private static boolean checkCreativeMode(CommandContext ctx) {
      if (mc.f_91074_ != null && mc.f_91074_.m_150110_().f_35937_) {
         return true;
      } else {
         sendMessage(ctx, "指令仅能在创造模式下使用");
         return false;
      }
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }

   private static void updateSlot(int internalSlot, int networkSlot, ItemStack newStack) {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(networkSlot, newStack));
         if (internalSlot < 36) {
            mc.f_91074_.m_150109_().f_35974_.set(internalSlot, newStack);
         } else if (internalSlot == 40) {
            mc.f_91074_.m_150109_().f_35976_.set(0, newStack);
         }

      }
   }

   private static int toNetworkSlot(int internalSlot) {
      if (internalSlot < 9) {
         return 36 + internalSlot;
      } else if (internalSlot < 36) {
         return internalSlot;
      } else {
         return internalSlot == 40 ? 45 : -1;
      }
   }

   private static int getCurrentSlot() {
      if (!mc.f_91074_.m_21205_().m_41619_()) {
         return mc.f_91074_.m_150109_().f_35977_;
      } else {
         return !mc.f_91074_.m_21206_().m_41619_() ? 40 : -1;
      }
   }

   private static int setItemCount(CommandContext ctx) {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         int count = IntegerArgumentType.getInteger(ctx, "count");
         int slot = getCurrentSlot();
         if (slot == -1) {
            sendMessage(ctx, "主手和副手都没有物品");
            return 0;
         } else {
            ItemStack stack = slot == 40 ? mc.f_91074_.m_21206_() : mc.f_91074_.m_21205_();
            if (stack.m_41619_()) {
               sendMessage(ctx, "物品是空 无法修改数量");
               return 0;
            } else {
               ItemStack newStack = Utils.fixItemCount(stack.m_41777_(), count);
               int networkSlot = toNetworkSlot(slot);
               if (networkSlot == -1) {
                  sendMessage(ctx, "无效槽位");
                  return 0;
               } else {
                  updateSlot(slot, networkSlot, newStack);
                  String var10001 = stack.m_41786_().getString();
                  sendMessage(ctx, "已将物品 " + var10001 + " 数量改为 " + newStack.m_41613_());
                  return 1;
               }
            }
         }
      }
   }

   private static int setAllItemCount(CommandContext ctx) {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         int count = IntegerArgumentType.getInteger(ctx, "count");
         int total = 0;

         ItemStack newOffhand;
         for(int slot = 0; slot < 36; ++slot) {
            newOffhand = mc.f_91074_.m_150109_().m_8020_(slot);
            if (!newOffhand.m_41619_()) {
               ItemStack newStack = Utils.fixItemCount(newOffhand.m_41777_(), count);
               int networkSlot = toNetworkSlot(slot);
               if (networkSlot != -1) {
                  updateSlot(slot, networkSlot, newStack);
                  ++total;
               }
            }
         }

         ItemStack offhand = mc.f_91074_.m_21206_();
         if (!offhand.m_41619_()) {
            newOffhand = Utils.fixItemCount(offhand.m_41777_(), count);
            updateSlot(40, 45, newOffhand);
            ++total;
         }

         sendMessage(ctx, "已修改 " + total + " 个物品的数量为 " + count);
         return 1;
      }
   }
}
