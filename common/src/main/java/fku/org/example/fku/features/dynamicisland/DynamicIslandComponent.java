package fku.org.example.fku.features.dynamicisland;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class DynamicIslandComponent extends ToggleComponent {
   public DynamicIslandComponent(int x, int y, int width, int height) {
      super(x, y, width, height, "灵动岛");
   }

   protected boolean isEnabled() {
      return DynamicIslandConfig.getInstance().enabled;
   }

   protected void toggle() {
      DynamicIslandConfig.getInstance().enabled = !DynamicIslandConfig.getInstance().enabled;
   }

   protected void saveConfig() {
      DynamicIslandConfig.save();
   }

   public void render(GuiGraphics g, int mx, int my, float pt) {
      if (this.visible && !(this.currentAlpha <= 0.01F)) {
         if (!this.renderHotkeyWait(g)) {
            boolean enabled = this.isEnabled();
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled, this.currentAlpha, this);
            GuiStyleConfig config = GuiStyleConfig.getInstance();
            int textAlpha = (int)(255.0F * this.currentAlpha);
            int textColor = enabled ? textAlpha << 24 | config.getTextColor() & 16777215 : textAlpha << 24 | 11184810;
            String labelText = this.hotkeyAppend(this.withState(this.label));
            int maxLabelW = this.width - 10 - 18;
            g.drawString(Minecraft.getInstance().font, this.truncate(labelText, maxLabelW), this.x + 5, this.y + (this.height - 8) / 2, textColor);
            int var10003 = this.x + this.width - 18;
            int var10004 = this.y + (this.height - 8) / 2 - 4;
            g.drawString(Minecraft.getInstance().font, ">>", var10003, var10004, textAlpha << 24 | 8947848);
         }
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered(mouseX, mouseY) && button == 1) {
         Minecraft.getInstance().setScreen(new DynamicIslandConfigScreen());
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }
}
