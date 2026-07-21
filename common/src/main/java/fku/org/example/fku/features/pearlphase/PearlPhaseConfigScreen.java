package fku.org.example.fku.features.pearlphase;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.features.pearlphase.PearlPhaseConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class PearlPhaseConfigScreen
extends Screen {
    private static final int WIDTH = 290;
    private static final int HEIGHT = 280;
    private static final int ROW_AUTO_THROW = 30;
    private static final int ROW_NO_CLIP = 53;
    private static final int ROW_SPEED = 76;
    private static final int ROW_BASE_SPEED = 99;
    private static final int ROW_AIM_TIME = 122;
    private static final int ROW_MAX_WAIT = 145;
    private static final int ROW_EDGE_OFFSET = 168;
    private static final int ROW_REMOVE_OVERLAY = 191;
    private static final int ROW_NO_FRONT = 214;
    private static final int ROW_CLOSE = 245;
    private final PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();
    private int scrollOffset = 0;
    private EditBox speedField;
    private EditBox baseSpeedField;
    private EditBox aimTimeField;
    private EditBox maxWaitField;
    private EditBox edgeOffsetField;
    private Button autoThrowButton;
    private Button noClipButton;
    private Button removeOverlayButton;
    private Button noFrontButton;

    public PearlPhaseConfigScreen() {
        super(Component.literal((String)"\u73cd\u73e0\u5361\u5899\u914d\u7f6e"));
    }

    protected void init() {
        int cx = (this.width - 290) / 2;
        int cy = (this.height - 280) / 2 - this.scrollOffset;
        this.autoThrowButton = this.buildToggleButton(cx + 160, cy + 30, this.cfg.autoThrow, "\u81ea\u52a8\u6295\u63b7", btn -> {
            this.cfg.setAutoThrow(!this.cfg.autoThrow);
            btn.setMessage(Component.literal((String)(this.cfg.autoThrow ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed")));
        });
        this.addRenderableWidget(this.autoThrowButton);
        this.noClipButton = this.buildToggleButton(cx + 160, cy + 53, this.cfg.noClipEnabled, "NoClip", btn -> {
            this.cfg.setNoClipEnabled(!this.cfg.noClipEnabled);
            btn.setMessage(Component.literal((String)(this.cfg.noClipEnabled ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed")));
        });
        this.addRenderableWidget(this.noClipButton);
        this.speedField = new EditBox(this.font, cx + 150, cy + 76, 100, 16, Component.literal((String)"\u79fb\u52a8\u500d\u7387"));
        this.speedField.m_94144_(String.valueOf(this.cfg.speed));
        this.speedField.m_94151_(s -> {
            try {
                this.cfg.setSpeed(Double.parseDouble(s));
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        });
        this.addRenderableWidget(this.speedField);
        this.baseSpeedField = new EditBox(this.font, cx + 150, cy + 99, 100, 16, Component.literal((String)"\u57fa\u7840\u901f\u5ea6"));
        this.baseSpeedField.m_94144_(String.valueOf(this.cfg.baseSpeed));
        this.baseSpeedField.m_94151_(s -> {
            try {
                this.cfg.setBaseSpeed(Double.parseDouble(s));
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        });
        this.addRenderableWidget(this.baseSpeedField);
        this.aimTimeField = new EditBox(this.font, cx + 150, cy + 122, 100, 16, Component.literal((String)"\u7784\u51c6\u65f6\u95f4"));
        this.aimTimeField.m_94144_(String.valueOf(this.cfg.aimTime));
        this.aimTimeField.m_94151_(s -> {
            try {
                this.cfg.setAimTime(Integer.parseInt(s));
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        });
        this.addRenderableWidget(this.aimTimeField);
        this.maxWaitField = new EditBox(this.font, cx + 150, cy + 145, 100, 16, Component.literal((String)"\u7b49\u5f85Tick"));
        this.maxWaitField.m_94144_(String.valueOf(this.cfg.maxWaitTicks));
        this.maxWaitField.m_94151_(s -> {
            try {
                this.cfg.setMaxWaitTicks(Integer.parseInt(s));
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        });
        this.addRenderableWidget(this.maxWaitField);
        this.edgeOffsetField = new EditBox(this.font, cx + 150, cy + 168, 100, 16, Component.literal((String)"\u8fb9\u7f18\u504f\u79fb"));
        this.edgeOffsetField.m_94144_(String.valueOf(this.cfg.edgeOffset));
        this.edgeOffsetField.m_94151_(s -> {
            try {
                this.cfg.setEdgeOffset(Double.parseDouble(s));
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        });
        this.addRenderableWidget(this.edgeOffsetField);
        this.removeOverlayButton = this.buildToggleButton(cx + 160, cy + 191, this.cfg.removeOverlay, "\u79fb\u9664\u8d34\u56fe", btn -> {
            this.cfg.setRemoveOverlay(!this.cfg.removeOverlay);
            btn.setMessage(Component.literal((String)(this.cfg.removeOverlay ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed")));
        });
        this.addRenderableWidget(this.removeOverlayButton);
        this.noFrontButton = this.buildToggleButton(cx + 160, cy + 214, this.cfg.noFront, "\u7981\u7528\u524d\u89c6\u89d2", btn -> {
            this.cfg.setNoFront(!this.cfg.noFront);
            btn.setMessage(Component.literal((String)(this.cfg.noFront ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed")));
        });
        this.addRenderableWidget(this.noFrontButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5173\u95ed"), btn -> this.onClose()).bounds(cx + 100, cy + 245, 80, 20).build());
    }

    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        int cx = (this.width - 290) / 2;
        int cy2 = (this.height - 280) / 2;
        if (mouseX >= cx && mouseX <= (cx + 290) && mouseY >= cy2 && mouseY <= (cy2 + 280)) {
            this.scrollOffset = Math.max(0, this.scrollOffset - (delta * 20.0));
            this.init();
            return true;
        }
        return super.m_6050_(mouseX, mouseY, delta);
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int cx = (this.width - 290) / 2;
        int cy = (this.height - 280) / 2 - this.scrollOffset;
        guiGraphics.m_280588_(cx + 2, cy + 5, cx + 290 - 2, cy + 280 - 5);
        guiGraphics.drawString(this.font, "\u00a7l\u73cd\u73e0\u5361\u5899\u914d\u7f6e", cx, cy + 10, 0xFFFFFF);
        this.drawLabel(guiGraphics, cx, cy + 30, "\u81ea\u52a8\u6295\u63b7\uff1a\u770b\u5411\u5899\u58c1\u65f6\u81ea\u52a8\u6295\u63b7\u73cd\u73e0");
        this.drawLabel(guiGraphics, cx, cy + 53, "NoClip\uff1a\u5361\u5165\u65b9\u5757\u540e\u542f\u7528\u7a7f\u5899");
        this.drawLabel(guiGraphics, cx, cy + 76, "\u79fb\u52a8\u500d\u7387\uff080~20\uff09\uff1a\u65b9\u5757\u5185\u79fb\u52a8\u901f\u5ea6");
        this.drawLabel(guiGraphics, cx, cy + 99, "\u57fa\u7840\u901f\u5ea6\uff080.00001~0.1\uff09");
        this.drawLabel(guiGraphics, cx, cy + 122, "\u7784\u51c6\u65f6\u95f4(ms)\uff080~1000\uff09");
        this.drawLabel(guiGraphics, cx, cy + 145, "\u6295\u63b7\u540e\u7b49\u5f85Tick\uff0820~600\uff09");
        this.drawLabel(guiGraphics, cx, cy + 168, "\u8fb9\u7f18\u504f\u79fb\uff080.0001~0.1\uff09");
        this.drawLabel(guiGraphics, cx, cy + 191, "\u79fb\u9664\u7a92\u606f\u8d34\u56fe");
        this.drawLabel(guiGraphics, cx, cy + 214, "\u7981\u7528\u524d\u65b9\u7b2c\u4e09\u4eba\u79f0");
        guiGraphics.m_280618_();
    }

    private void drawLabel(GuiGraphics gui, int cx, int y, String text) {
        gui.drawString(this.font, text, cx + 10, y + 2, 0xCCCCCC);
    }

    private Button buildToggleButton(int x, int y, boolean initial, String label, Button.OnPress onClick) {
        String text = initial ? "\u00a7a\u2714 \u5f00\u542f" : "\u00a7c\u2718 \u5173\u95ed";
        return Button.builder(Component.literal((String)text), (Button.OnPress)onClick).bounds(x, y, 80, 16).build();
    }

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 257) {
            this.onClose();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        if (Minecraft.getInstance().screen instanceof ClickGuiScreen) {
            Minecraft.getInstance().setScreen(Minecraft.getInstance().screen);
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }
}

