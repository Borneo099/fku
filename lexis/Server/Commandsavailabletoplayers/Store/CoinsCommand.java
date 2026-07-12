package lexis.Server.Commandsavailabletoplayers.Store;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CoinsCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final SuggestionProvider PLAYER_SUGGESTIONS = (context, builder) -> {
      List playerNames = new ArrayList();
      Iterator var3 = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11314_().iterator();

      while(var3.hasNext()) {
         ServerPlayer player = (ServerPlayer)var3.next();
         playerNames.add(player.m_7755_().getString());
      }

      return SharedSuggestionProvider.m_82970_(playerNames, builder);
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("coins").requires((source) -> {
         Entity patt1802$temp = source.m_81373_();
         if (!(patt1802$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82127_("clear").then(Commands.m_82129_("玩家名", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).executes((context) -> {
         String targetName = StringArgumentType.getString(context, "玩家名");
         ServerPlayer target = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11255_(targetName);
         if (target == null) {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c找不到玩家: " + targetName));
            return 0;
         } else {
            StoreConfig.getInstance().getPlayerMoney().put(target.m_20148_(), 0);
            StoreConfig.getInstance().save();
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已清空 " + targetName + " 的金币");
            }, true);
            return 1;
         }
      })))).then(Commands.m_82127_("add").then(Commands.m_82129_("玩家名", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).then(Commands.m_82129_("数量", IntegerArgumentType.integer(1)).executes((context) -> {
         String targetName = StringArgumentType.getString(context, "玩家名");
         int amount = IntegerArgumentType.getInteger(context, "数量");
         ServerPlayer target = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11255_(targetName);
         if (target == null) {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c找不到玩家: " + targetName));
            return 0;
         } else {
            StoreConfig.getInstance().addPlayerMoney(target.m_20148_(), amount);
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已给 " + targetName + " 增加 " + amount + " 金币");
            }, true);
            return 1;
         }
      }))))).then(Commands.m_82127_("set").then(Commands.m_82129_("玩家名", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).then(Commands.m_82129_("数量", IntegerArgumentType.integer(0)).executes((context) -> {
         String targetName = StringArgumentType.getString(context, "玩家名");
         int amount = IntegerArgumentType.getInteger(context, "数量");
         ServerPlayer target = ((CommandSourceStack)context.getSource()).m_81377_().m_6846_().m_11255_(targetName);
         if (target == null) {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c找不到玩家: " + targetName));
            return 0;
         } else {
            StoreConfig.getInstance().getPlayerMoney().put(target.m_20148_(), amount);
            StoreConfig.getInstance().save();
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f§a已设置 " + targetName + " 的金币为 " + amount);
            }, true);
            return 1;
         }
      })))))));
   }
}
