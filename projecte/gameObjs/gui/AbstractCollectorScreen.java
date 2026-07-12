package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.CollectorMK1Container;
import moze_intel.projecte.gameObjs.container.CollectorMK2Container;
import moze_intel.projecte.gameObjs.container.CollectorMK3Container;
import moze_intel.projecte.utils.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCollectorScreen extends PEContainerScreen {
   public AbstractCollectorScreen(CollectorMK1Container container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
   }

   protected abstract ResourceLocation getTexture();

   protected int getBonusXShift() {
      return 0;
   }

   protected int getTextureBonusXShift() {
      return 0;
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
      graphics.m_280056_(this.f_96547_, Long.toString(((CollectorMK1Container)this.f_97732_).emc.get()), 60 + this.getBonusXShift(), 32, 4210752, false);
      long kleinCharge = ((CollectorMK1Container)this.f_97732_).kleinEmc.get();
      if (kleinCharge > 0L) {
         graphics.m_280056_(this.f_96547_, Constants.EMC_FORMATTER.format(kleinCharge), 60 + this.getBonusXShift(), 44, 4210752, false);
      }

   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      graphics.m_280218_(this.getTexture(), this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      int progress = (int)((double)((CollectorMK1Container)this.f_97732_).sunLevel.m_6501_() * 12.0 / 16.0);
      graphics.m_280218_(this.getTexture(), this.f_97735_ + 126 + this.getBonusXShift(), this.f_97736_ + 49 - progress, 177 + this.getTextureBonusXShift(), 13 - progress, 12, progress);
      graphics.m_280218_(this.getTexture(), this.f_97735_ + 64 + this.getBonusXShift(), this.f_97736_ + 18, 0, 166, (int)((double)((CollectorMK1Container)this.f_97732_).emc.get() / (double)((CollectorMK1Container)this.f_97732_).collector.getMaximumEmc() * 48.0), 10);
      progress = (int)(((CollectorMK1Container)this.f_97732_).getKleinChargeProgress() * 48.0);
      graphics.m_280218_(this.getTexture(), this.f_97735_ + 64 + this.getBonusXShift(), this.f_97736_ + 58, 0, 166, progress, 10);
      progress = (int)(((CollectorMK1Container)this.f_97732_).getFuelProgress() * 24.0);
      graphics.m_280218_(this.getTexture(), this.f_97735_ + 138 + this.getBonusXShift(), this.f_97736_ + 55 - progress, 176 + this.getTextureBonusXShift(), 38 - progress, 10, progress + 1);
   }

   public static class MK3 extends AbstractCollectorScreen {
      public MK3(CollectorMK3Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title);
         this.f_97726_ = 218;
         this.f_97727_ = 165;
      }

      protected ResourceLocation getTexture() {
         return PECore.rl("textures/gui/collector3.png");
      }

      protected int getBonusXShift() {
         return 34;
      }

      protected int getTextureBonusXShift() {
         return 43;
      }
   }

   public static class MK2 extends AbstractCollectorScreen {
      public MK2(CollectorMK2Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title);
         this.f_97726_ = 200;
         this.f_97727_ = 165;
      }

      protected ResourceLocation getTexture() {
         return PECore.rl("textures/gui/collector2.png");
      }

      protected int getBonusXShift() {
         return 16;
      }

      protected int getTextureBonusXShift() {
         return 25;
      }
   }

   public static class MK1 extends AbstractCollectorScreen {
      public MK1(CollectorMK1Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title);
      }

      protected ResourceLocation getTexture() {
         return PECore.rl("textures/gui/collector1.png");
      }
   }
}
