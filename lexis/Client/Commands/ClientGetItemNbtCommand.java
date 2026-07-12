package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientGetItemNbtCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("getitemnbt").executes(ClientGetItemNbtCommand::getItemNbt))));
   }

   private static void sendMessage(CommandContext ctx, Component msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return msg;
      }, false);
   }

   private static int getItemNbt(CommandContext ctx) {
      if (mc.f_91074_ == null) {
         sendMessage(ctx, Component.m_237113_("§d[§6Lexis§d] §f§c只有游戏内玩家才能使用此指令"));
         return 0;
      } else {
         ItemStack stack = mc.f_91074_.m_21205_();
         if (stack.m_41619_()) {
            stack = mc.f_91074_.m_21206_();
            if (stack.m_41619_()) {
               sendMessage(ctx, Component.m_237113_("§d[§6Lexis§d] §f§c主手和副手都没有物品"));
               return 0;
            }
         }

         String itemName = stack.m_41786_().getString();
         String itemId = BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_()).toString();
         CompoundTag tag = stack.m_41783_();
         String nbtString = tag != null && !tag.m_128456_() ? tag.toString() : "{}";
         String coloredNbt = nbtString.replace("{", "§7{§r").replace("}", "§7}§r").replace("[", "§7[§r").replace("]", "§7]§r").replace(":", "§7:§r").replace(",", "§7,§r");
         Component header = Component.m_237113_("§d[§6Lexis§d] §f§e物品: §f" + itemName + " §7(" + itemId + ")§r");
         Component nbtComponent = Component.m_237113_("§7NBT: §r" + coloredNbt).m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.COPY_TO_CLIPBOARD, nbtString)).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§a点击复制 NBT"))));
         sendMessage(ctx, header);
         sendMessage(ctx, nbtComponent);
         Component copyLine = Component.m_237113_("§d[§6Lexis§d] §f§7[点击复制原始 NBT]").m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.COPY_TO_CLIPBOARD, nbtString)).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§a复制原始 NBT"))));
         sendMessage(ctx, copyLine);
         return 1;
      }
   }
}
