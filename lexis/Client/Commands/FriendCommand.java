package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lexis.Hack.Utils.FriendsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FriendCommand {
   private static final SuggestionProvider ADD_SUGGESTIONS = (ctx, builder) -> {
      Minecraft mc = Minecraft.m_91087_();
      Set suggestions = new HashSet();
      Iterator var4;
      if (mc.f_91073_ != null) {
         var4 = mc.f_91073_.m_6907_().iterator();

         while(var4.hasNext()) {
            Player player = (Player)var4.next();
            suggestions.add(player.m_7755_().getString());
         }
      }

      var4 = suggestions.iterator();

      while(var4.hasNext()) {
         String s = (String)var4.next();
         builder.suggest(s);
      }

      return builder.buildFuture();
   };
   private static final SuggestionProvider REMOVE_SUGGESTIONS = (ctx, builder) -> {
      Iterator var2 = FriendsManager.getInstance().getFriendNames().iterator();

      while(var2.hasNext()) {
         String name = (String)var2.next();
         builder.suggest(name);
      }

      return builder.buildFuture();
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("friends").then(Commands.m_82127_("add").then(Commands.m_82129_("player", StringArgumentType.greedyString()).suggests(ADD_SUGGESTIONS).executes(FriendCommand::addFriend)))).then(Commands.m_82127_("remove").then(Commands.m_82129_("player", StringArgumentType.greedyString()).suggests(REMOVE_SUGGESTIONS).executes(FriendCommand::removeFriend)))).then(((LiteralArgumentBuilder)Commands.m_82127_("list").executes((ctx) -> {
         return listFriends(ctx, 1);
      })).then(Commands.m_82129_("page", IntegerArgumentType.integer(1)).executes((ctx) -> {
         return listFriends(ctx, IntegerArgumentType.getInteger(ctx, "page"));
      }))))));
   }

   private static int addFriend(CommandContext ctx) {
      String name = StringArgumentType.getString(ctx, "player");
      FriendsManager.getInstance().addFriend(name);
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f已添加好友: " + name);
      }, false);
      return 1;
   }

   private static int removeFriend(CommandContext ctx) {
      String name = StringArgumentType.getString(ctx, "player");
      FriendsManager.getInstance().removeFriend(name);
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f已移除好友: " + name);
      }, false);
      return 1;
   }

   private static int listFriends(CommandContext ctx, int page) {
      List names = FriendsManager.getInstance().getFriendNames();
      int total = names.size();
      if (total == 0) {
         ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f好友数量: 0");
         }, false);
         return 1;
      } else {
         int pageSize = 5;
         int totalPages = (int)Math.ceil((double)total / (double)pageSize);
         if (page < 1) {
            page = 1;
         }

         if (page > totalPages) {
            page = totalPages;
         }

         int start = (page - 1) * pageSize;
         int end = Math.min(start + pageSize, total);
         ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f好友数量: " + total);
         }, false);
         ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
            return Component.m_237113_("§d[§6Lexis§d] §f好友列表 (page " + page + "/" + totalPages + ")");
         }, false);

         for(int i = start; i < end; ++i) {
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
               Object var10000 = names.get(i);
               return Component.m_237113_("§d[§6Lexis§d] §f" + (String)var10000);
            }, false);
         }

         if (page < totalPages) {
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
               return Component.m_237113_("§d[§6Lexis§d] §7下一页: /lexis client friends list " + (page + 1));
            }, false);
         }

         if (page > 1) {
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
               return Component.m_237113_("§d[§6Lexis§d] §7上一页: /lexis client friends list " + (page - 1));
            }, false);
         }

         return 1;
      }
   }
}
