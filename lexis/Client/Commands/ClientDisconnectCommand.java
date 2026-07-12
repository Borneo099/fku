package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientDisconnectCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)Commands.m_82127_("disconnect").executes(ClientDisconnectCommand::disconnectDefault)).then(Commands.m_82129_("reason", StringArgumentType.greedyString()).executes(ClientDisconnectCommand::disconnectWithReason)))));
   }

   private static int disconnectDefault(CommandContext ctx) {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         String defaultReason = String.format("§4§l§oUnknown host");
         mc.m_91403_().m_7026_(Component.m_237113_(defaultReason));
         return 1;
      } else {
         sendMessage(ctx, "未连接到服务器");
         return 0;
      }
   }

   private static int disconnectWithReason(CommandContext ctx) {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         String reason = StringArgumentType.getString(ctx, "reason");
         sendMessage(ctx, reason);
         mc.m_91403_().m_7026_(Component.m_237113_(reason));
         return 1;
      } else {
         sendMessage(ctx, "未连接到服务器");
         return 0;
      }
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }
}
