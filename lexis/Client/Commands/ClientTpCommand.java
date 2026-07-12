package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientTpCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static final Random RANDOM = new Random();
   private static final double MAX_DISTANCE = 11.0;

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("tp").then(Commands.m_82129_("pos", BlockPosArgument.m_118239_()).executes((ctx) -> {
         return teleportToPos(ctx, BlockPosArgument.m_264582_(ctx, "pos"));
      }))).then(Commands.m_82127_("@p").executes(ClientTpCommand::teleportToNearest))).then(Commands.m_82127_("@r").executes(ClientTpCommand::teleportToRandom)))));
   }

   private static int teleportToPos(CommandContext ctx, BlockPos pos) {
      if (!checkPlayer()) {
         return 0;
      } else {
         double dist = mc.f_91074_.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5);
         if (Math.sqrt(dist) > 11.0) {
            sendMessage(ctx, "传送过很远最大11米能传送(你是客户端传送很远会回弹位置)");
            return 0;
         } else {
            teleport((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_());
            int var10001 = pos.m_123341_();
            sendMessage(ctx, "传到: " + var10001 + ", " + pos.m_123342_() + ", " + pos.m_123343_());
            return 1;
         }
      }
   }

   private static int teleportToNearest(CommandContext ctx) {
      if (!checkPlayer()) {
         return 0;
      } else {
         Player nearest = getNearestPlayer();
         if (nearest == null) {
            sendMessage(ctx, "附近没有其他玩家");
            return 0;
         } else {
            double dist = (double)mc.f_91074_.m_20270_(nearest);
            if (dist > 11.0) {
               sendMessage(ctx, "传送玩家位置过很远最大11米能传送(你是客户端传送很远会回弹位置)");
               return 0;
            } else {
               teleport(nearest.m_20185_(), nearest.m_20186_(), nearest.m_20189_());
               sendMessage(ctx, "传到玩家 " + nearest.m_36316_().getName());
               return 1;
            }
         }
      }
   }

   private static int teleportToRandom(CommandContext ctx) {
      if (!checkPlayer()) {
         return 0;
      } else {
         Player random = getRandomPlayer();
         if (random == null) {
            sendMessage(ctx, "附近没有其他玩家");
            return 0;
         } else {
            double dist = (double)mc.f_91074_.m_20270_(random);
            if (dist > 11.0) {
               sendMessage(ctx, "传送玩家位置过很远最大11米能传送(你是客户端传送很远会回弹位置)");
               return 0;
            } else {
               teleport(random.m_20185_(), random.m_20186_(), random.m_20189_());
               sendMessage(ctx, "传到玩家 " + random.m_36316_().getName());
               return 1;
            }
         }
      }
   }

   private static boolean checkPlayer() {
      return mc.f_91074_ != null && mc.m_91403_() != null;
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }

   private static void teleport(double x, double y, double z) {
      LocalPlayer player = mc.f_91074_;
      player.m_6034_(x, y, z);
      player.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y, z, player.m_20096_()));
   }

   private static Player getNearestPlayer() {
      return mc.f_91073_ == null ? null : (Player)mc.f_91073_.m_6907_().stream().filter((p) -> {
         return p != mc.f_91074_ && p.m_6084_();
      }).min((p1, p2) -> {
         return Double.compare(mc.f_91074_.m_20280_(p1), mc.f_91074_.m_20280_(p2));
      }).orElse((Object)null);
   }

   private static Player getRandomPlayer() {
      if (mc.f_91073_ == null) {
         return null;
      } else {
         List players = (List)mc.f_91073_.m_6907_().stream().filter((p) -> {
            return p != mc.f_91074_ && p.m_6084_();
         }).collect(Collectors.toList());
         return players.isEmpty() ? null : (Player)players.get(RANDOM.nextInt(players.size()));
      }
   }
}
