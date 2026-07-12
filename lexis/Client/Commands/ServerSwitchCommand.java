package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ServerSwitchCommand {
   private static final Minecraft mc = Minecraft.m_91087_();

   @SubscribeEvent
   public static void onRegisterCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("ServerSwitch").then(Commands.m_82129_("ip", StringArgumentType.greedyString()).executes((ctx) -> {
         String ip = StringArgumentType.getString(ctx, "ip");
         return switchServer(ip);
      })))));
   }

   private static int switchServer(String ip) {
      if (mc.f_91074_ == null) {
         return 0;
      } else if (mc.m_91403_() == null) {
         mc.f_91074_.m_213846_(Component.m_237113_("[Lexis] §f你在单人模式！无法直接切换服务器，仅在进入服务器"));
         return 0;
      } else {
         ip = ip.trim();
         if (ip.isEmpty()) {
            mc.f_91074_.m_213846_(Component.m_237113_("[Lexis] §f输入服务器 IP 听不懂吗？"));
            return 0;
         } else {
            ServerAddress address = ServerAddress.m_171864_(ip);
            String host = address.m_171863_();
            int port = address.m_171866_();
            mc.f_91074_.m_213846_(Component.m_237113_("[Lexis] §f正在切换到 §e" + host + ":" + port + " §f..."));
            ServerData serverData = new ServerData("Lexis Quick Switch", ip, false);
            ConnectScreen.m_278792_((Screen)null, mc, address, serverData, false);
            return 1;
         }
      }
   }
}
