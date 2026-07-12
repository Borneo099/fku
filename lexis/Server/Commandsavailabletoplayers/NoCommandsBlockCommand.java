package lexis.Server.Commandsavailabletoplayers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber
public class NoCommandsBlockCommand {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static boolean isEnabled = true;

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("Lexis").requires((source) -> {
         return source.m_6761_(2);
      })).then(Commands.m_82127_("NoCommandsBlock").then(Commands.m_82129_("状态", StringArgumentType.word()).suggests((context, builder) -> {
         builder.suggest("on");
         builder.suggest("off");
         return builder.buildFuture();
      }).executes((context) -> {
         String state = StringArgumentType.getString(context, "状态");
         if (state.equalsIgnoreCase("on")) {
            isEnabled = true;
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f开启命令方块禁止模式");
            }, true);
         } else if (state.equalsIgnoreCase("off")) {
            isEnabled = false;
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f关闭命令方块禁止模式");
            }, true);
            return 0;
         }

         return 1;
      }))));
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onCommand(CommandEvent event) {
      try {
         if (!isEnabled) {
            return;
         }

         CommandSourceStack source = (CommandSourceStack)event.getParseResults().getContext().getSource();
         if (isCommandBlock(source)) {
            String command = event.getParseResults().getReader().getString();
            event.setCanceled(true);
            if (command.length() > 50) {
               command = command.substring(0, 47) + "...";
            }

            LOGGER.info("已阻止命令方块执行命令: {}", command);
         }
      } catch (Exception var3) {
         LOGGER.error("处理命令事件时出错: {}", var3.getMessage());
      }

   }

   private static boolean isCommandBlock(CommandSourceStack source) {
      try {
         String sourceName = source.m_81368_();
         if (sourceName != null && sourceName.toLowerCase().contains("commandblock")) {
            return true;
         } else {
            String sourceStr = source.toString().toLowerCase();
            return sourceStr.contains("commandblock") || sourceStr.contains("command_block") || sourceStr.contains("方块") || sourceStr.contains("命令块");
         }
      } catch (Exception var3) {
         LOGGER.error("检测命令方块时出错: {}", var3.getMessage());
         return false;
      }
   }

   public static boolean isEnabled() {
      return isEnabled;
   }

   public static void setEnabled(boolean enabled) {
      isEnabled = enabled;
   }
}
