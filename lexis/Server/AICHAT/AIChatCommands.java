package lexis.Server.AICHAT;

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
public class AIChatCommands {
   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("aichat").requires((source) -> {
         Entity patt1128$temp = source.m_81373_();
         if (!(patt1128$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82127_("clear").executes((context) -> {
         AIService.clearAllHistories();
         ServerAIChatHandler.clearCooldowns();
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f已清除所有对话历史和冷却时间");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("on").executes((context) -> {
         ServerAIChatHandler.setEnabled(true);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §fAI聊天 开启");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("off").executes((context) -> {
         ServerAIChatHandler.setEnabled(false);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §fAI聊天 关闭");
         }, true);
         return 1;
      })))));
   }
}
