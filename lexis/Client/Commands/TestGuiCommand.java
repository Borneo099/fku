package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lexis.Hack.Utils.ESP.BlockSelectScreen;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.ESP.FontSelectScreen;
import lexis.Hack.Utils.ESP.SingleBlockSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class TestGuiCommand {
   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("testgui").then(Commands.m_82127_("entity").executes(TestGuiCommand::openEntitySelect))).then(Commands.m_82127_("block").executes(TestGuiCommand::openBlockSelect))).then(Commands.m_82127_("singleblock").executes(TestGuiCommand::openSingleBlockSelect))).then(Commands.m_82127_("font").executes(TestGuiCommand::openFontSelect)));
   }

   private static int openEntitySelect(CommandContext context) {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         mc.m_91152_(new EntitySelectScreen((Screen)null, "test", () -> {
            mc.f_91074_.m_213846_(Component.m_237113_("§a[TestGui] 实体选择保存成功"));
         }));
      });
      return 1;
   }

   private static int openBlockSelect(CommandContext context) {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         mc.m_91152_(new BlockSelectScreen((Screen)null, "test", () -> {
            mc.f_91074_.m_213846_(Component.m_237113_("§a[TestGui] 方块选择保存成功"));
         }));
      });
      return 1;
   }

   private static int openSingleBlockSelect(CommandContext context) {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         mc.m_91152_(new SingleBlockSelectScreen((Screen)null, "test", (blockId) -> {
            mc.f_91074_.m_213846_(Component.m_237113_("§a[TestGui] 选中方块: " + blockId));
         }));
      });
      return 1;
   }

   private static int openFontSelect(CommandContext context) {
      Minecraft mc = Minecraft.m_91087_();
      mc.execute(() -> {
         mc.m_91152_(new FontSelectScreen((Screen)null, "test", (fontName) -> {
            mc.f_91074_.m_213846_(Component.m_237113_("§a[TestGui] 选中字体: " + fontName));
         }));
      });
      return 1;
   }
}
