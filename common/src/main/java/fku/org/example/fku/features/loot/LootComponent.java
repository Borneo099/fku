package fku.org.example.fku.features.loot;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.features.loot.LootConfig;
import fku.org.example.fku.features.loot.LootFeature;
import fku.org.example.fku.features.loot.LootScreen;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LootComponent
extends GuiComponent {
    public LootComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "\u4e00\u952e\u53d6\u7269");
        HotkeySystem.registerFeature("\u4e00\u952e\u53d6\u7269", () -> {
            LootConfig cfg = LootConfig.getInstance();
            if (cfg.enabled) {
                cfg.setEnabled(false);
                LootFeature.stop();
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7c\u5df2\u5173\u95ed\uff0c\u6e05\u7a7a\u5bb9\u5668\u6807\u8bb0"), false);
                }
            } else {
                cfg.setEnabled(true);
                LootFeature.start();
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7a\u5df2\u5f00\u542f\uff0c\u5f00\u59cb\u81ea\u52a8\u53d6\u7269"), false);
                }
            }
        });
    }

    protected String getFeatureName() {
        return "\u4e00\u952e\u53d6\u7269";
    }

    private boolean isEnabled() {
        return LootConfig.getInstance().enabled;
    }

    private void toggle() {
        LootConfig cfg = LootConfig.getInstance();
        if (cfg.enabled) {
            cfg.setEnabled(false);
            LootFeature.stop();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7c\u5df2\u5173\u95ed\uff0c\u6e05\u7a7a\u5bb9\u5668\u6807\u8bb0"), false);
            }
        } else {
            cfg.setEnabled(true);
            LootFeature.start();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.m_5661_(Component.literal((String)"\u00a76[\u4e00\u952e\u53d6\u7269] \u00a7a\u5df2\u5f00\u542f\uff0c\u5f00\u59cb\u81ea\u52a8\u53d6\u7269"), false);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        String rs;
        if (!this.visible) {
            return;
        }
        GuiStyleConfig config = GuiStyleConfig.getInstance();
        if (HotkeySystem.isWaitingFor("\u4e00\u952e\u53d6\u7269")) {
            GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, true);
            g.drawString(Minecraft.getInstance().font, "\u7ed1\u5b9a\u70ed\u952e\u4e2d. (Esc\u53d6\u6d88)", this.x + 5, this.y + (this.height - 8) / 2 - 4, 0xFFFF00);
            return;
        }
        boolean enabled = this.isEnabled();
        GuiRenderHelper.drawComponentBackground(g, this.x, this.y, this.width, this.height, enabled);
        String status = enabled ? "ON" : "OFF";
        FeatureHotkeyManager.IHotkeyInterface hk = FeatureHotkeyManager.getInstance().getHotkey("\u4e00\u952e\u53d6\u7269");
        String hkStr = hk.getHotkeyKey() >= 0 ? " \u00a77[" + hk.getHotkeyName() + "]" : "";
        g.drawString(Minecraft.getInstance().font, "\u4e00\u952e\u53d6\u7269: " + status + hkStr, this.x + 5, this.y + (this.height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
        if (LootFeature.isRunning() && !(rs = LootFeature.getStatus()).isEmpty()) {
            g.drawString(Minecraft.getInstance().font, "\u00a7a" + rs, this.x + 5, this.y + (this.height - 8) / 2 + 8, 0x55FF55);
        }
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
            this.toggle();
            if (this.isEnabled()) {
                LootFeature.start();
            }
            return true;
        }
        if (button == 1) {
            if (HotkeySystem.isWaiting()) {
                return false;
            }
            Minecraft.getInstance().setScreen((Screen)new LootScreen());
            return true;
        }
        if (button == 2) {
            HotkeySystem.startBinding("\u4e00\u952e\u53d6\u7269", () -> {});
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        return false;
    }
}

