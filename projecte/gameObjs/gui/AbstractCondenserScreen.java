package moze_intel.projecte.gameObjs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCondenserScreen extends PEContainerScreen {
   public AbstractCondenserScreen(CondenserContainer condenser, Inventory playerInventory, Component title) {
      super(condenser, playerInventory, title);
      this.f_97726_ = 255;
      this.f_97727_ = 233;
   }

   protected abstract ResourceLocation getTexture();

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, this.getTexture());
      graphics.m_280218_(this.getTexture(), this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      int progress = ((CondenserContainer)this.f_97732_).getProgressScaled();
      graphics.m_280218_(this.getTexture(), this.f_97735_ + 33, this.f_97736_ + 10, 0, 235, progress, 10);
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
      long toDisplay = Math.min(((CondenserContainer)this.f_97732_).displayEmc.get(), ((CondenserContainer)this.f_97732_).requiredEmc.get());
      Component emc = TransmutationEMCFormatter.formatEMC(toDisplay);
      graphics.m_280614_(this.f_96547_, emc, 140, 10, 4210752, false);
   }

   protected void m_280072_(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      long toDisplay = Math.min(((CondenserContainer)this.f_97732_).displayEmc.get(), ((CondenserContainer)this.f_97732_).requiredEmc.get());
      if ((double)toDisplay < 1.0E12) {
         super.m_280072_(graphics, mouseX, mouseY);
      } else {
         int emcLeft = 140 + this.f_97735_;
         int emcRight = emcLeft + 110;
         int emcTop = 6 + this.f_97736_;
         int emcBottom = emcTop + 15;
         if (mouseX > emcLeft && mouseX < emcRight && mouseY > emcTop && mouseY < emcBottom) {
            this.m_257404_(PELang.EMC_TOOLTIP.translate(new Object[]{Constants.EMC_FORMATTER.format(toDisplay)}));
         } else {
            super.m_280072_(graphics, mouseX, mouseY);
         }

      }
   }

   public static class MK2 extends AbstractCondenserScreen {
      public MK2(CondenserMK2Container condenser, Inventory playerInventory, Component title) {
         super(condenser, playerInventory, title);
      }

      protected ResourceLocation getTexture() {
         return PECore.rl("textures/gui/condenser_mk2.png");
      }
   }

   public static class MK1 extends AbstractCondenserScreen {
      public MK1(CondenserContainer condenser, Inventory playerInventory, Component title) {
         super(condenser, playerInventory, title);
      }

      protected ResourceLocation getTexture() {
         return PECore.rl("textures/gui/condenser.png");
      }
   }
}
