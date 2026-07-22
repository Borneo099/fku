package fku.org.example.fku.features.teleport;

import fku.org.example.fku.client.gui.components.ToggleComponent;
import fku.org.example.fku.features.teleport.TeleportConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TeleportComponent
extends ToggleComponent {
    public TeleportComponent(int x, int y, int w, int h) {
        super(x, y, w, h, "\u77ac\u79fb");
    }

    @Override
    protected boolean isEnabled() {
        return TeleportConfig.getInstance().enabled;
    }

    @Override
    protected void toggle() {
        TeleportConfig c = TeleportConfig.getInstance();
        c.setEnabled(!c.enabled);
    }

    @Override
    protected void saveConfig() {
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!this.visible || this.currentAlpha <= 0.01f) {
            return;
        }
        super.render(g, mx, my, pt);
        g.drawString(Minecraft.getInstance().font, ">>", this.x + this.width - 14, this.y + (this.height - 8) / 2, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!this.isHovered(mx, my)) {
            return false;
        }
        if (btn == 0) {
            if (this.listeningForKey) {
                return false;
            }
            this.toggle();
            return true;
        }
        if (btn == 1) {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p != null) {
                p.displayClientMessage(Component.literal((String)""), false);
                p.displayClientMessage(Component.literal((String)"\u00a76===== \u00a7e\u77ac\u79fb \u00a76====="), false);
                p.displayClientMessage(Component.literal((String)"\u00a77\u4f7f\u7528 /fku tp <x> <y> <z> [snap] \u77ac\u79fb"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77snap \u4e3a true/false\uff0c\u5f00\u542f\u843d\u70b9\u5438\u9644"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77\u4f8b\u5982: /fku tp ~ ~5 ~ true \u5411\u4e0a\u77ac\u79fb5\u683c"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77   /fku tp ~ ~ ~ false \u51c6\u661f\u77ac\u79fb"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77   /fku tp 100 64 100 false \u4f20\u9001\u5230\u5750\u6807"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77\u901a\u8fc7\u5feb\u6377\u6307\u4ee4\u7ed1\u5b9a\u70ed\u952e\uff0c\u5feb\u901f\u6267\u884c"), false);
                p.displayClientMessage(Component.literal((String)"\u00a77[\u4e2d\u952e] \u7ed1\u5b9a/\u66f4\u6539\u5f00\u5173\u70ed\u952e"), false);
                p.displayClientMessage(Component.literal((String)""), false);
            }
            return true;
        }
        if (btn == 2) {
            return this.handleMiddleClick(mx, my, btn);
        }
        return false;
    }

    @Override
    public String getFeatureName() {
        return "\u77ac\u79fb";
    }
}

