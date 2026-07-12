package lexis.Client.Commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Iterator;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientInvseeCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static final SuggestionProvider PLAYER_SUGGESTIONS = (context, builder) -> {
      return mc.f_91073_ != null && mc.f_91074_ != null ? SharedSuggestionProvider.m_82970_((Iterable)mc.f_91073_.m_6907_().stream().filter((p) -> {
         return p != mc.f_91074_;
      }).map((p) -> {
         return p.m_7755_().getString();
      }).collect(Collectors.toList()), builder) : builder.buildFuture();
   };

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("invsee").then(Commands.m_82129_("player", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).executes(ClientInvseeCommand::invsee)))));
   }

   private static int invsee(CommandContext context) {
      String targetName = StringArgumentType.getString(context, "player");
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (!mc.f_91074_.m_150110_().f_35937_ && !mc.f_91074_.m_5833_()) {
            Player target = null;
            Iterator var3 = mc.f_91073_.m_6907_().iterator();

            while(var3.hasNext()) {
               Player player = (Player)var3.next();
               if (player != mc.f_91074_ && player.m_7755_().getString().equalsIgnoreCase(targetName)) {
                  target = player;
                  break;
               }
            }

            if (target == null) {
               ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f§f未找到玩家: " + targetName));
               return 0;
            } else {
               mc.m_91152_(new ReadOnlyInventoryScreen(target));
               ((CommandSourceStack)context.getSource()).m_288197_(() -> {
                  return Component.m_237113_("§d[§6Lexis§d] §f§f正在查看 " + targetName + " 的背包(只读)");
               }, false);
               return 1;
            }
         } else {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f§f指令仅生存+冒险模式使用"));
            return 0;
         }
      } else {
         ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f§f只有游戏内玩家才能使用此指令"));
         return 0;
      }
   }

   private static class ReadOnlyInventoryScreen extends InventoryScreen {
      private final String playerName;

      public ReadOnlyInventoryScreen(Player player) {
         super(player);
         this.playerName = player.m_7755_().getString();
      }

      public Component m_96636_() {
         return Component.m_237113_(this.playerName + " 的背包 §7[只读]");
      }

      public boolean m_6375_(double mouseX, double mouseY, int button) {
         return false;
      }
   }
}
