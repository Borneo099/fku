package lexis.Server.Commandsavailabletoplayers.Menu;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class NoMenuCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static boolean noMenuEnabled = false;

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)Commands.m_82127_("NoMenu").requires((source) -> {
         Entity patt1294$temp = source.m_81373_();
         if (!(patt1294$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82129_("状态", StringArgumentType.word()).suggests((context, builder) -> {
         builder.suggest("on");
         builder.suggest("off");
         return builder.buildFuture();
      }).executes((context) -> {
         String state = StringArgumentType.getString(context, "状态");
         if (state.equalsIgnoreCase("on")) {
            noMenuEnabled = true;
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f禁止菜单功能");
            }, true);
         } else if (state.equalsIgnoreCase("off")) {
            noMenuEnabled = false;
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f开启菜单功能");
            }, true);
         }

         return 1;
      })))));
   }

   private static boolean isMenuCompass(ItemStack stack) {
      if (stack.m_41720_() != Items.f_42522_) {
         return false;
      } else {
         return stack.m_41782_() && stack.m_41783_().m_128441_("lexiscd") && stack.m_41783_().m_128445_("lexiscd") == 1;
      }
   }

   @SubscribeEvent
   public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
      if (noMenuEnabled) {
         Player var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            ItemStack stack = event.getItemStack();
            if (isMenuCompass(stack)) {
               event.setCanceled(true);
               stack.m_41764_(0);
               player.m_150109_().m_36057_(stack);
               player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c对不起，Lexis的菜单 服务器已经禁止了"));
            }

         }
      }
   }

   @SubscribeEvent
   public static void onCommand(CommandEvent event) {
      if (noMenuEnabled) {
         String command = event.getParseResults().getReader().getString();
         if (command.startsWith("/Lexis cd") || command.startsWith("/lexis cd")) {
            Entity var3 = ((CommandSourceStack)event.getParseResults().getContext().getSource()).m_81373_();
            if (var3 instanceof ServerPlayer) {
               ServerPlayer player = (ServerPlayer)var3;
               event.setCanceled(true);
               player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c对不起，指令cd 的服务器 已经禁止了"));
            }
         }

      }
   }

   public static boolean isNoMenuEnabled() {
      return noMenuEnabled;
   }
}
