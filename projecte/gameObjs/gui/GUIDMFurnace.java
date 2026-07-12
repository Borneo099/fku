package moze_intel.projecte.gameObjs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.block_entities.DMFurnaceBlockEntity;
import moze_intel.projecte.gameObjs.container.DMFurnaceContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUIDMFurnace extends PEContainerScreen {
   private static final ResourceLocation texture = PECore.rl("textures/gui/dmfurnace.png");
   private final DMFurnaceBlockEntity furnace;

   public GUIDMFurnace(DMFurnaceContainer container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
      this.f_97726_ = 178;
      this.f_97727_ = 165;
      this.furnace = container.furnace;
      this.f_97728_ = 57;
      this.f_97730_ = 57;
      this.f_97731_ = this.f_97727_ - 94;
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int x, int y) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, texture);
      graphics.m_280218_(texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      int progress;
      if (this.furnace.isBurning()) {
         progress = this.furnace.getBurnTimeRemainingScaled(12);
         graphics.m_280218_(texture, this.f_97735_ + 49, this.f_97736_ + 36 + 12 - progress, 179, 12 - progress, 14, progress + 2);
      }

      progress = this.furnace.getCookProgressScaled(24);
      graphics.m_280218_(texture, this.f_97735_ + 73, this.f_97736_ + 34, 179, 14, progress + 1, 16);
   }
}
