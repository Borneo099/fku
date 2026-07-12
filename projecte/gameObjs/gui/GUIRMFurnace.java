package moze_intel.projecte.gameObjs.gui;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.block_entities.RMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.container.RMFurnaceContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUIRMFurnace extends PEContainerScreen {
   private static final ResourceLocation texture = PECore.rl("textures/gui/rmfurnace.png");
   private final RMFurnaceBlockEntity furnace;

   public GUIRMFurnace(RMFurnaceContainer container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
      this.f_97726_ = 209;
      this.f_97727_ = 165;
      this.furnace = (RMFurnaceBlockEntity)container.furnace;
      this.f_97728_ = 76;
      this.f_97730_ = 76;
      this.f_97731_ = this.f_97727_ - 94;
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      graphics.m_280218_(texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      int progress;
      if (this.furnace.isBurning()) {
         progress = this.furnace.getBurnTimeRemainingScaled(12);
         graphics.m_280218_(texture, this.f_97735_ + 66, this.f_97736_ + 38 + 10 - progress, 210, 10 - progress, 21, progress + 2);
      }

      progress = this.furnace.getCookProgressScaled(24);
      graphics.m_280218_(texture, this.f_97735_ + 88, this.f_97736_ + 35, 210, 14, progress, 17);
   }
}
