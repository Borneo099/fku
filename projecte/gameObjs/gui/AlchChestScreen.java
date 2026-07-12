package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.AlchChestContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class AlchChestScreen extends PEContainerScreen {
   private static final ResourceLocation texture = PECore.rl("textures/gui/alchchest.png");

   public AlchChestScreen(AlchChestContainer container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
      this.f_97726_ = 255;
      this.f_97727_ = 230;
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      graphics.m_280218_(texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
   }
}
