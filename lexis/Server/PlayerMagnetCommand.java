package lexis.Server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PlayerMagnetCommand {
   private static boolean publicMode = true;
   private static int maxPlayers = 20;
   private static final Set blockedPlayers = new HashSet();
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";

   private static boolean isOwner(CommandSourceStack source) {
      Entity var2 = source.m_81373_();
      if (!(var2 instanceof ServerPlayer player)) {
         return false;
      } else {
         return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
      }
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("lexis").requires((source) -> {
         return isOwner(source);
      })).then(((LiteralArgumentBuilder)Commands.m_82127_("server").requires((source) -> {
         return isOwner(source);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("playermagnet").requires((source) -> {
         return isOwner(source);
      })).then(((LiteralArgumentBuilder)Commands.m_82127_("on").requires((source) -> {
         return isOwner(source);
      })).executes((context) -> {
         publicMode = true;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f服务器已设置公开模式，允许其他玩家进入");
         }, true);
         return 1;
      }))).then(((LiteralArgumentBuilder)Commands.m_82127_("off").requires((source) -> {
         return isOwner(source);
      })).executes((context) -> {
         publicMode = false;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f服务器已设置私人模式，禁止其他玩家进入");
         }, true);
         return 1;
      }))).then(((LiteralArgumentBuilder)Commands.m_82127_("max").requires((source) -> {
         return isOwner(source);
      })).then(Commands.m_82129_("数量", IntegerArgumentType.integer(1, 64)).executes((context) -> {
         int newMax = IntegerArgumentType.getInteger(context, "数量");
         maxPlayers = newMax;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f最大玩家数已设置: " + maxPlayers);
         }, true);
         return 1;
      })))).then(((LiteralArgumentBuilder)Commands.m_82127_("clear").requires((source) -> {
         return isOwner(source);
      })).executes((context) -> {
         blockedPlayers.clear();
         PlayerMessageInterceptor.clearSilentPlayers();
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f已清除所有被禁止的玩家记录");
         }, true);
         return 1;
      }))).then(((LiteralArgumentBuilder)Commands.m_82127_("list").requires((source) -> {
         return isOwner(source);
      })).executes((context) -> {
         if (blockedPlayers.isEmpty()) {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f当前没有被禁止的玩家");
            }, false);
         } else {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> {
               return Component.m_237113_("§c[§6Lexis-Server§c] §f被禁止的玩家列表:");
            }, false);
            Iterator var1 = blockedPlayers.iterator();

            while(var1.hasNext()) {
               UUID uuid = (UUID)var1.next();
               ((CommandSourceStack)context.getSource()).m_288197_(() -> {
                  return Component.m_237113_("§7- " + uuid.toString());
               }, false);
            }
         }

         return 1;
      }))).executes((context) -> {
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f§e用法: /lexis server playermagnet <on/off/max/clear/list>");
         }, false);
         return 1;
      }))));
   }

   @SubscribeEvent
   public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
      ServerPlayer player = (ServerPlayer)event.getEntity();
      UUID playerUUID = player.m_20148_();
      String playerName = player.m_7755_().getString();
      boolean isOwner = player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
      if (!isOwner) {
         if (!publicMode) {
            blockedPlayers.add(playerUUID);
            PlayerMessageInterceptor.addSilentPlayer(playerUUID);
            player.f_8906_.m_9942_(Component.m_237113_("§c[Lexis-Server]\n§f抱歉！服务器未开启公开模式，你被禁止进入服务器！"));
            Iterator var9 = player.f_8924_.m_6846_().m_11314_().iterator();

            while(var9.hasNext()) {
               ServerPlayer onlinePlayer = (ServerPlayer)var9.next();
               if (onlinePlayer.m_20194_() != null && onlinePlayer.m_20194_().m_7779_(onlinePlayer.m_36316_())) {
                  onlinePlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c玩家 \"" + playerName + "\" 试图进入你的服务器！\n§7该玩家已被禁止，服务器当前是私人模式"));
               }
            }

         } else {
            if (publicMode) {
               if (blockedPlayers.contains(playerUUID)) {
                  PlayerMessageInterceptor.addSilentPlayer(playerUUID);
                  player.f_8906_.m_9942_(Component.m_237113_("§c[Lexis-Server]\n§f你已被服务器禁止进入"));
                  return;
               }

               int nonOwnerCount = 0;
               Iterator var6 = player.f_8924_.m_6846_().m_11314_().iterator();

               ServerPlayer onlinePlayer;
               while(var6.hasNext()) {
                  onlinePlayer = (ServerPlayer)var6.next();
                  boolean isOnlineOwner = onlinePlayer.m_20194_() != null && onlinePlayer.m_20194_().m_7779_(onlinePlayer.m_36316_());
                  if (!isOnlineOwner) {
                     ++nonOwnerCount;
                  }
               }

               if (nonOwnerCount >= maxPlayers) {
                  blockedPlayers.add(playerUUID);
                  PlayerMessageInterceptor.addSilentPlayer(playerUUID);
                  player.f_8906_.m_9942_(Component.m_237113_("§c[§6Lexis-Server§c]\n§f对不起！服务器已满人，你被禁止进入\n服务器当前最大玩家数: " + maxPlayers));
                  var6 = player.f_8924_.m_6846_().m_11314_().iterator();

                  while(var6.hasNext()) {
                     onlinePlayer = (ServerPlayer)var6.next();
                     if (onlinePlayer.m_20194_() != null && onlinePlayer.m_20194_().m_7779_(onlinePlayer.m_36316_())) {
                        onlinePlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c玩家 \"" + playerName + "\" 试图进入你的服务器！\n§7服务器已满 (最大 " + maxPlayers + " 人)，该玩家已被自动禁止"));
                     }
                  }
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onServerStarting(ServerStartingEvent event) {
      publicMode = true;
      maxPlayers = 20;
      blockedPlayers.clear();
      PlayerMessageInterceptor.clearSilentPlayers();
   }
}
