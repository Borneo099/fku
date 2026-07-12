package lexis.Client.Commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientDropCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("drop").executes(ClientDropCommand::dropAll))));
   }

   private static int dropAll(CommandContext ctx) {
      if (mc.f_91074_ == null) {
         sendMessage(ctx, "只有游戏内玩家才能使用指令");
         return 0;
      } else if (mc.f_91074_.m_5833_()) {
         sendMessage(ctx, "旁观模式不能扔物品");
         return 0;
      } else {
         int containerId = mc.f_91074_.f_36096_.f_38840_;

         for(int slot = 9; slot <= 44; ++slot) {
            int adjustedSlot;
            if (slot >= 36) {
               adjustedSlot = slot - 36;
            } else {
               adjustedSlot = slot;
            }

            if (!mc.f_91074_.m_150109_().m_8020_(adjustedSlot).m_41619_()) {
               mc.f_91072_.m_171799_(containerId, slot, 1, ClickType.THROW, mc.f_91074_);
            }
         }

         if (!mc.f_91074_.m_21206_().m_41619_()) {
            mc.f_91072_.m_171799_(containerId, 45, 1, ClickType.THROW, mc.f_91074_);
         }

         sendMessage(ctx, "已扔出所有物品");
         return 1;
      }
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }
}
