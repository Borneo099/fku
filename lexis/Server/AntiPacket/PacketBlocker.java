package lexis.Server.AntiPacket;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PacketBlocker {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static boolean enabled = true;
   private static int packetLimit = 1000;
   private static final Map packetCounts = new ConcurrentHashMap();

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("packetlimit").requires((source) -> {
         Entity patt1469$temp = source.m_81373_();
         if (!(patt1469$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82127_("on").executes((context) -> {
         enabled = true;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f开启发包限制");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("off").executes((context) -> {
         enabled = false;
         packetCounts.clear();
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f关闭发包限制");
         }, true);
         return 1;
      }))).then(Commands.m_82129_("上限", IntegerArgumentType.integer(100, 10000000)).executes((context) -> {
         int limit = IntegerArgumentType.getInteger(context, "上限");
         packetLimit = limit;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f已设置发包上限为: " + limit + " 包/秒");
         }, true);
         return 1;
      })))));
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != Phase.END) {
         if (enabled) {
            if (event.getServer().m_129921_() % 20 == 0) {
               packetCounts.clear();
            }

         }
      }
   }

   public static boolean checkPacket(ServerPlayer player) {
      if (!enabled) {
         return true;
      } else if (player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_())) {
         return true;
      } else {
         UUID playerId = player.m_20148_();
         PacketCounter counter = (PacketCounter)packetCounts.computeIfAbsent(playerId, (k) -> {
            return new PacketCounter();
         });
         ++counter.count;
         if (counter.count > packetLimit) {
            String playerName = player.m_7755_().getString();
            Component broadcastMsg = Component.m_237113_(String.format("%s§c玩家：%s ，服务器收到请求玩家的发包太多了 已经被踢出了！", "§c[§6Lexis-Server§c] §f", playerName));
            player.m_20194_().m_6846_().m_11314_().forEach((p) -> {
               p.m_213846_(broadcastMsg);
            });
            player.f_8906_.m_9942_(Component.m_237113_("§d[§6Lexis-Server§d]\n§f你是不是开了开了？这里服务器收到你请求发包太多次了！\n§c你已经被踢出了\n§f你可以重新进入服务器！不能是发包太多了！"));
            packetCounts.remove(playerId);
            return false;
         } else {
            return true;
         }
      }
   }

   private static class PacketCounter {
      int count = 0;
   }
}
