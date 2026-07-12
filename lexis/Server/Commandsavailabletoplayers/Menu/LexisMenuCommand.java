package lexis.Server.Commandsavailabletoplayers.Menu;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class LexisMenuCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("Lexis").then(Commands.m_82127_("cd").executes((context) -> {
         Entity patt1019$temp = ((CommandSourceStack)context.getSource()).m_81373_();
         if (patt1019$temp instanceof ServerPlayer player) {
            if (NoMenuCommand.isNoMenuEnabled()) {
               player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §c对不起，指令cd 的服务器 已经禁止了"));
               return 0;
            } else {
               LexisMenuHandler.openMainMenu(player);
               ((CommandSourceStack)context.getSource()).m_288197_(() -> {
                  return Component.m_237113_("§c[§6Lexis-Server§c] §a已打开Lexis菜单");
               }, false);
               return 1;
            }
         } else {
            return 0;
         }
      })));
   }
}
