package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.EternalDensityContainer;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUIEternalDensity extends PEContainerScreen {
   private static final ResourceLocation texture = PECore.rl("textures/gui/eternal_density.png");

   public GUIEternalDensity(EternalDensityContainer container, Inventory inv, Component title) {
      super(container, inv, title);
      this.f_97726_ = 180;
      this.f_97727_ = 180;
   }

   public void m_7856_() {
      super.m_7856_();
      this.m_142416_(Button.m_253074_((((EternalDensityContainer)this.f_97732_).inventory.isWhitelistMode() ? PELang.WHITELIST : PELang.BLACKLIST).translate(new Object[0]), (b) -> {
         ((EternalDensityContainer)this.f_97732_).inventory.changeMode();
         b.m_93666_(((EternalDensityContainer)this.f_97732_).inventory.isWhitelistMode() ? PELang.WHITELIST.translate(new Object[0]) : PELang.BLACKLIST.translate(new Object[0]));
      }).m_252794_(this.f_97735_ + 62, this.f_97736_ + 4).m_253046_(52, 20).m_253136_());
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      graphics.m_280218_(texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
   }
}
