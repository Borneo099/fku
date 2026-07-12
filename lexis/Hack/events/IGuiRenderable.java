package lexis.Hack.events;

import net.minecraft.client.gui.GuiGraphics;

public interface IGuiRenderable {
   void onRenderGui(GuiGraphics guiGraphics, float partialTick);
}
