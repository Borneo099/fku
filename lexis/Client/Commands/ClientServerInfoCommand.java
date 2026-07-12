package lexis.Client.Commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lexis.Client.ClientUtils.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientServerInfoCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static TickRate tickRate = new TickRate();

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("Serverinfo").executes(ClientServerInfoCommand::serverInfo))));
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }

   private static int serverInfo(CommandContext ctx) {
      if (mc.f_91074_ == null) {
         sendMessage(ctx, "你不在游戏中");
         return 0;
      } else {
         String address;
         if (mc.m_257720_()) {
            IntegratedServer server = mc.m_91092_();
            sendMessage(ctx, "§l======== 服务器信息 ========");
            sendMessage(ctx, "类型: 单机/局域网");
            if (server != null) {
               address = server.m_7630_();
               sendMessage(ctx, "版本: " + address);
            }

            sendMessage(ctx, "§l============================");
            return 1;
         } else {
            ServerData server = mc.m_91089_();
            if (server == null) {
               sendMessage(ctx, "无法读取服务器信息");
               return 0;
            } else {
               address = server.f_105363_;
               int port = 25565;
               if (address.contains(":")) {
                  String[] split = address.split(":");
                  address = split[0];

                  try {
                     port = Integer.parseInt(split[1]);
                  } catch (NumberFormatException var12) {
                  }
               }

               int ping = (int)server.f_105366_;
               float tps = tickRate.getTps();
               String tpsColor;
               if ((double)tps >= 19.5) {
                  tpsColor = "§a";
               } else if (tps >= 15.0F) {
                  tpsColor = "§e";
               } else {
                  tpsColor = "§c";
               }

               String version = server.f_105368_.getString();
               String difficulty = mc.f_91073_.m_46791_().m_19033_().getString();
               long day = mc.f_91073_.m_46468_() / 24000L;
               String perms = "0";
               if (mc.f_91074_.m_20310_(4)) {
                  perms = "4 (Owner)";
               } else if (mc.f_91074_.m_20310_(2)) {
                  perms = "2 (OP)";
               } else {
                  perms = "0 (普通)";
               }

               sendMessage(ctx, "§l======== 服务器信息 ========");
               sendMessage(ctx, "地址: " + address + ":" + port);
               sendMessage(ctx, "延迟: " + ping + "ms");
               sendMessage(ctx, "TPS: " + tpsColor + String.format("%.1f", tps) + "§f");
               sendMessage(ctx, "版本: " + version);
               sendMessage(ctx, "难度: " + difficulty);
               sendMessage(ctx, "天数: " + day);
               sendMessage(ctx, "权限: " + perms);
               sendMessage(ctx, "§l============================");
               return 1;
            }
         }
      }
   }
}
