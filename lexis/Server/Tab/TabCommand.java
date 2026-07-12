package lexis.Server.Tab;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
public class TabCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)Commands.m_82127_("tab").requires((source) -> {
         Entity patt1058$temp = source.m_81373_();
         if (!(patt1058$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(((LiteralArgumentBuilder)Commands.m_82127_("maxentities").then(Commands.m_82129_("数量", IntegerArgumentType.integer(100, 10000)).executes((context) -> {
         int max = IntegerArgumentType.getInteger(context, "数量");
         TabListManager.setMaxEntities(max);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已设置实体数量阈值为: " + max);
         }, true);
         return 1;
      }))).executes((context) -> {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§e当前实体数量阈值: " + TabListManager.getMaxEntities());
         }, true);
         return 1;
      })))));
   }
}
