package moze_intel.projecte.gameObjs.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public abstract class PEContainerScreen extends AbstractContainerScreen {
   public boolean switchingToJEI;

   public PEContainerScreen(AbstractContainerMenu container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
   }

   public void m_88315_(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTicks);
      this.m_280072_(graphics, mouseX, mouseY);
   }

   public void m_7861_() {
      if (!this.switchingToJEI) {
         super.m_7861_();
      }

   }

   public void m_7856_() {
      this.switchingToJEI = false;
      super.m_7856_();
   }
}
