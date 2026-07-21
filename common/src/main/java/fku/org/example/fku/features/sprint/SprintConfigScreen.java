package fku.org.example.fku.features.sprint;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.sprint.SprintConfig;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SprintConfigScreen
extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 260;
    private static final int ROW_MODE = 30;
    private static final int ROW_IGNORE_TITLE = 55;
    private static final int ROW_IGNORE_BLINDNESS = 75;
    private static final int ROW_IGNORE_HUNGER = 100;
    private static final int ROW_IGNORE_COLLISION = 125;
    private static final int ROW_STOP_GROUND = 30;
    private static final int ROW_STOP_AIR = 55;
    private static final int ROW_ELYTRA = 80;
    private static final int ROW_SMOOTH_SWITCH = 105;
    private static final int ROW_BUTTON = 220;
    private static final int COL_LABEL = 10;
    private static final int COL_WIDGET = 105;
    private final SprintConfig cfg;
    private final SprintConfig.Mode[] modeValues = SprintConfig.Mode.values();
    private int modeIndex;
    private int activeTab = 0;
    private int scrollOffset = 0;
    private static final String[] TAB_NAMES = new String[]{"\u57fa\u7840\u8bbe\u7f6e", "\u9ad8\u7ea7\u8bbe\u7f6e"};
    private EditBox rotationSpeedInput;
    private String cachedSpeedText = "";

    public SprintConfigScreen() {
        super(Component.literal((String)"\u5f3a\u5236\u75be\u8dd1\u914d\u7f6e"));
        this.cfg = SprintConfig.getInstance();
        for (int i = 0; i < this.modeValues.length; ++i) {
            if (!this.modeValues[i].name().equals(this.cfg.mode)) continue;
            this.modeIndex = i;
            break;
        }
    }

    private int cy(int row) {
        return (this.height - 260) / 2 + row - this.scrollOffset;
    }

    private int cx() {
        return (this.width - 300) / 2;
    }

    protected void init() {
        super.init();
        this.rebuildWidgets();
    }

    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        int cx = this.cx();
        int cyb = (this.height - 260) / 2;
        if (mouseX >= cx && mouseX <= (cx + 300) && mouseY >= cyb && mouseY <= (cyb + 260)) {
            this.scrollOffset = Math.max(0, this.scrollOffset - (delta * 20.0));
            this.rebuildWidgets();
            return true;
        }
        return super.m_6050_(mouseX, mouseY, delta);
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.rotationSpeedInput = null;
        int cx = this.cx();
        int cy = this.cy(0);
        int tabX = cx + 10;
        for (int i = 0; i < TAB_NAMES.length; ++i) {
            int fi = i;
            boolean isActive = i == this.activeTab;
            int tw = Minecraft.getInstance().font.m_92895_(TAB_NAMES[i]) + 12;
            this.addRenderableWidget(Button.builder(Component.literal((String)(isActive ? "\u00a7l[" + TAB_NAMES[i] + "]\u00a7r" : TAB_NAMES[i])), btn -> {
                this.saveInputNow();
                this.activeTab = fi;
                this.rebuildWidgets();
            }).bounds(tabX, cy + 7, Math.max(tw, 50), 16).build());
            tabX += Math.max(tw, 50) + 4;
        }
        if (this.activeTab == 0) {
            this.buildTabBasic();
        } else {
            this.buildTabAdvanced();
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u8fd4\u56de\u4e3b\u83dc\u5355"), btn -> {
            this.saveInputNow();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 40, this.cy(220), 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u91cd\u7f6e\u9ed8\u8ba4"), btn -> {
            this.cfg.mode = "OMNIROTATIONAL";
            this.cfg.ignoreBlindness = false;
            this.cfg.ignoreHunger = false;
            this.cfg.ignoreCollision = false;
            this.cfg.stopOnGround = false;
            this.cfg.stopOnAir = false;
            this.cfg.elytraRotation = true;
            this.cfg.smoothRotation = false;
            this.cfg.rotationSpeed = 90;
            for (int i = 0; i < this.modeValues.length; ++i) {
                if (!this.modeValues[i].name().equals(this.cfg.mode)) continue;
                this.modeIndex = i;
                break;
            }
            SprintConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 160, this.cy(220), 100, 20).build());
    }

    private void buildTabBasic() {
        int cx = this.cx();
        this.addRenderableWidget(Button.builder(Component.literal((String)this.modeValues[this.modeIndex].getChineseLabel()), btn -> {
            this.modeIndex = (this.modeIndex + 1) % this.modeValues.length;
            this.cfg.mode = this.modeValues[this.modeIndex].name();
            SprintConfig.save();
            this.rebuildWidgets();
        }).bounds(cx + 105, this.cy(30), 85, 18).build());
        this.addToggle(this.cy(75), () -> this.cfg.ignoreBlindness, v -> {
            this.cfg.ignoreBlindness = v;
        });
        this.addToggle(this.cy(100), () -> this.cfg.ignoreHunger, v -> {
            this.cfg.ignoreHunger = v;
        });
        this.addToggle(this.cy(125), () -> this.cfg.ignoreCollision, v -> {
            this.cfg.ignoreCollision = v;
        });
    }

    private void buildTabAdvanced() {
        int cx = this.cx();
        this.addToggle(this.cy(30), () -> this.cfg.stopOnGround, v -> {
            this.cfg.stopOnGround = v;
        });
        this.addToggle(this.cy(55), () -> this.cfg.stopOnAir, v -> {
            this.cfg.stopOnAir = v;
        });
        this.addToggle(this.cy(80), () -> this.cfg.elytraRotation, v -> {
            this.cfg.elytraRotation = v;
        });
        this.addToggle(this.cy(105), () -> this.cfg.smoothRotation, v -> {
            this.cfg.smoothRotation = v;
            SprintConfig.save();
            this.rebuildWidgets();
        });
        if (this.cfg.smoothRotation) {
            this.rotationSpeedInput = new EditBox(this.font, cx + 105 + 90, this.cy(105), 45, 16, Component.literal((String)""));
            this.rotationSpeedInput.m_94144_(String.valueOf(this.cfg.rotationSpeed));
            this.rotationSpeedInput.m_94199_(3);
            this.rotationSpeedInput.m_94153_(s -> s.matches("\\d*"));
            this.rotationSpeedInput.m_94151_(s -> {
                this.cachedSpeedText = s;
            });
            this.addRenderableWidget(this.rotationSpeedInput);
        }
    }

    private void addToggle(int y, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        int cx = this.cx();
        this.addRenderableWidget(Button.builder(Component.literal((String)(getter.get() != false ? "\u5f00" : "\u5173")), btn -> {
            boolean newVal = (Boolean)getter.get() == false;
            setter.accept(newVal);
            SprintConfig.save();
            btn.setMessage(Component.literal((String)(newVal ? "\u5f00" : "\u5173")));
        }).bounds(cx + 105, y, 40, 18).build());
    }

    private void saveInputNow() {
        if (this.rotationSpeedInput != null) {
            try {
                int val = Integer.parseInt(this.rotationSpeedInput.m_94155_());
                if (val > 0 && val <= 360) {
                    this.cfg.rotationSpeed = val;
                    SprintConfig.save();
                }
            }
            catch (NumberFormatException numberFormatException) {
                // ignored
            }
        }
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = this.cx();
        int cy = this.cy(0);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 300, 260, false);
        g.m_280588_(cx + 2, cy + 20, cx + 300 - 2, cy + 260 - 32);
        g.drawString(this.font, "\u5f3a\u5236\u75be\u8dd1\u914d\u7f6e - " + TAB_NAMES[this.activeTab], cx + 10, cy + 2, 0xFFFFFF);
        if (this.activeTab == 0) {
            g.drawString(this.font, "\u75be\u8dd1\u6a21\u5f0f:", cx + 10, this.cy(30), 0xAAAAAA);
            g.drawString(this.font, "\u00a77- - - \u5ffd\u7565\u6761\u4ef6 - - -", cx + 10, this.cy(55), 0x666666);
            g.drawString(this.font, "\u5ffd\u7565\u5931\u660e:", cx + 10, this.cy(75), 0xAAAAAA);
            g.drawString(this.font, "\u5ffd\u7565\u9965\u997f:", cx + 10, this.cy(100), 0xAAAAAA);
            g.drawString(this.font, "\u5ffd\u7565\u649e\u5899:", cx + 10, this.cy(125), 0xAAAAAA);
        } else {
            g.drawString(this.font, "\u00a77- - - Legit \u6a21\u5f0f - - -", cx + 10, this.cy(30) - 16, 0x666666);
            g.drawString(this.font, "\u5730\u9762\u505c\u6b62:", cx + 10, this.cy(30), 0xAAAAAA);
            g.drawString(this.font, "\u7a7a\u4e2d\u505c\u6b62:", cx + 10, this.cy(55), 0xAAAAAA);
            g.drawString(this.font, "\u00a77- - - \u5168\u5411\u65cb\u8f6c - - -", cx + 10, this.cy(80) - 16, 0x666666);
            g.drawString(this.font, "\u9798\u7fc5\u65cb\u8f6c:", cx + 10, this.cy(80), 0xAAAAAA);
            g.drawString(this.font, "\u5e73\u6ed1\u65cb\u8f6c:", cx + 10, this.cy(105), 0xAAAAAA);
            if (this.cfg.smoothRotation) {
                g.drawString(this.font, "\u00a77\u00b0/\u5e27", cx + 105 + 137, this.cy(105), 0x666666);
            }
        }
        g.m_280618_();
        super.render(g, mx, my, pt);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.saveInputNow();
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

