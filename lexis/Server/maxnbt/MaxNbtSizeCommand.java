package lexis.Server.maxnbt;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MaxNbtSizeCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static int maxNbtSize = 1650000;
   private static Map lastWarningTime = new HashMap();
   private static final long WARNING_COOLDOWN = 300L;

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("lexis").requires((source) -> {
         return isOwner(source);
      })).then(((LiteralArgumentBuilder)Commands.m_82127_("server").requires((source) -> {
         return isOwner(source);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("maxNbtSize").requires((source) -> {
         return isOwner(source);
      })).then(Commands.m_82129_("大小", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).executes((context) -> {
         int newSize = IntegerArgumentType.getInteger(context, "大小");
         maxNbtSize = newSize;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已设置NBT上限为: " + newSize + " 字节");
         }, true);
         return 1;
      }))).executes((context) -> {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§e当前NBT上限: " + maxNbtSize + " 字节");
         }, true);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§7用法: /lexis server maxNbtSize <1~2147483647>");
         }, true);
         return 1;
      }))));
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!event.player.m_9236_().f_46443_) {
            ServerPlayer player = (ServerPlayer)event.player;
            checkPlayerNbt(player);
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event) {
      if (!event.getEntity().m_9236_().f_46443_) {
         checkPlayerNbt((ServerPlayer)event.getEntity());
      }
   }

   private static void checkPlayerNbt(ServerPlayer player) {
      int totalNbtSize = 0;

      Iterator var2;
      ItemStack stack;
      for(var2 = player.m_150109_().f_35974_.iterator(); var2.hasNext(); totalNbtSize += getItemNbtSize(stack)) {
         stack = (ItemStack)var2.next();
      }

      totalNbtSize += getItemNbtSize((ItemStack)player.m_150109_().f_35976_.get(0));

      for(var2 = player.m_150109_().f_35975_.iterator(); var2.hasNext(); totalNbtSize += getItemNbtSize(stack)) {
         stack = (ItemStack)var2.next();
      }

      if (totalNbtSize > maxNbtSize) {
         clearPlayerInventory(player);
         long currentTime = System.currentTimeMillis();
         UUID playerId = player.m_20148_();
         if (!lastWarningTime.containsKey(playerId) || currentTime - (Long)lastWarningTime.get(playerId) > 300L) {
            String message = "§c[§6Lexis-Server§c] §f玩家 " + player.m_7755_().getString() + " 的NBT数据过多，已自动清空背包了";
            player.m_20194_().m_6846_().m_11314_().forEach((p) -> {
               p.m_213846_(Component.m_237113_(message));
            });
            lastWarningTime.put(playerId, currentTime);
         }
      }

   }

   private static int getItemNbtSize(ItemStack stack) {
      if (!stack.m_41619_() && stack.m_41782_()) {
         CompoundTag tag = stack.m_41783_();
         return tag == null ? 0 : tag.toString().getBytes(StandardCharsets.UTF_8).length;
      } else {
         return 0;
      }
   }

   private static void clearPlayerInventory(ServerPlayer player) {
      int i;
      for(i = 0; i < player.m_150109_().f_35974_.size(); ++i) {
         player.m_150109_().f_35974_.set(i, ItemStack.f_41583_);
      }

      player.m_150109_().f_35976_.set(0, ItemStack.f_41583_);

      for(i = 0; i < player.m_150109_().f_35975_.size(); ++i) {
         player.m_150109_().f_35975_.set(i, ItemStack.f_41583_);
      }

      player.f_36095_.m_38946_();
   }

   private static boolean isOwner(CommandSourceStack source) {
      Entity var2 = source.m_81373_();
      if (!(var2 instanceof ServerPlayer player)) {
         return false;
      } else {
         return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
      }
   }

   public static int getMaxNbtSize() {
      return maxNbtSize;
   }
}
