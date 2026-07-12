package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.RelayMK1Container;
import moze_intel.projecte.gameObjs.container.RelayMK2Container;
import moze_intel.projecte.gameObjs.container.RelayMK3Container;
import moze_intel.projecte.utils.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUIRelay extends PEContainerScreen {
   private final ResourceLocation texture;
   private final int emcX;
   private final int emcY;
   private final int vOffset;
   private final int emcBarShift;
   private final int shiftX;
   private final int shiftY;

   protected GUIRelay(RelayMK1Container container, Inventory invPlayer, Component title, ResourceLocation texture, int emcX, int emcY, int vOffset, int emcBarShift, int shiftX, int shiftY) {
      super(container, invPlayer, title);
      this.texture = texture;
      this.emcX = emcX;
      this.emcY = emcY;
      this.vOffset = vOffset;
      this.emcBarShift = emcBarShift;
      this.shiftX = shiftX;
      this.shiftY = shiftY;
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
      graphics.m_280614_(this.f_96547_, this.f_96539_, this.f_97728_, this.f_97729_, 4210752, false);
      graphics.m_280056_(this.f_96547_, Constants.EMC_FORMATTER.format(((RelayMK1Container)this.f_97732_).emc.get()), this.emcX, this.emcY, 4210752, false);
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      graphics.m_280218_(this.texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      int progress = (int)((double)((RelayMK1Container)this.f_97732_).emc.get() / (double)((RelayMK1Container)this.f_97732_).relay.getMaximumEmc() * 102.0);
      graphics.m_280218_(this.texture, this.f_97735_ + this.emcBarShift, this.f_97736_ + 6, 30, this.vOffset, progress, 10);
      progress = (int)(((RelayMK1Container)this.f_97732_).getKleinChargeProgress() * 30.0);
      graphics.m_280218_(this.texture, this.f_97735_ + 116 + this.shiftX, this.f_97736_ + 67 + this.shiftY, 0, this.vOffset, progress, 10);
      progress = (int)(((RelayMK1Container)this.f_97732_).getInputBurnProgress() * 30.0);
      graphics.m_280218_(this.texture, this.f_97735_ + 64 + this.shiftX, this.f_97736_ + 67 + this.shiftY, 0, this.vOffset, progress, 10);
   }

   public static class GUIRelayMK3 extends GUIRelay {
      private static final ResourceLocation MK3_TEXTURE = PECore.rl("textures/gui/relay3.png");

      public GUIRelayMK3(RelayMK3Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title, MK3_TEXTURE, 125, 39, 195, 105, 37, 15);
         this.f_97726_ = 212;
         this.f_97727_ = 194;
         this.f_97728_ = 38;
      }
   }

   public static class GUIRelayMK2 extends GUIRelay {
      private static final ResourceLocation MK2_TEXTURE = PECore.rl("textures/gui/relay2.png");

      public GUIRelayMK2(RelayMK2Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title, MK2_TEXTURE, 107, 25, 183, 86, 17, 1);
         this.f_97726_ = 193;
         this.f_97727_ = 182;
         this.f_97728_ = 28;
      }
   }

   public static class GUIRelayMK1 extends GUIRelay {
      private static final ResourceLocation MK1_TEXTURE = PECore.rl("textures/gui/relay1.png");

      public GUIRelayMK1(RelayMK1Container container, Inventory invPlayer, Component title) {
         super(container, invPlayer, title, MK1_TEXTURE, 88, 24, 177, 64, 0, 0);
         this.f_97726_ = 175;
         this.f_97727_ = 176;
         this.f_97728_ = 10;
      }
   }
}
