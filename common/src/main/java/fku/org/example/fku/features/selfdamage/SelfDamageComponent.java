package fku.org.example.fku.features.selfdamage;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.selfdamage.SelfDamageConfig;
import fku.org.example.fku.features.selfdamage.SelfDamageFeature;
import fku.org.example.fku.features.selfdamage.SelfDamageScreen;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class SelfDamageComponent
extends GuiComponent {
    public SelfDamageComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u81ea\u4f24");
        HotkeySystem.registerFeature("\u81ea\u4f24", () -> SelfDamageFeature.applyDamage());
    }

    protected String getFeatureName() {
        return "\u81ea\u4f24";
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u81ea\u4f24")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
        SelfDamageConfig cfg = SelfDamageConfig.getInstance();
        String display = "\u81ea\u4f24: " + cfg.damageAmount + "\u2764";
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u81ea\u4f24");
        if (hk.getHotkeyKey() >= 0) {
            display = display + " \u00a77[" + hk.getHotkeyName() + "]";
        }
        g.drawString(Minecraft.getInstance().font, display, this.x + 5, this.y + (this.height - 8) / 2 - 4, config.getTextColor());
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 18, this.y + (this.height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!this.isHovered(mx, my)) {
            return false;
        }
        if (button == 0) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            SelfDamageFeature.applyDamage();
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new SelfDamageScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u81ea\u4f24", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}

