package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lexis.Hack.HackGui;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClickGUICommand {
   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("ClickGui").executes(ClickGUICommand::openClickGui)));
   }

   private static int openClickGui(CommandContext context) {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         mc.m_91152_(new HackGui());
      });
      return 1;
   }
}
