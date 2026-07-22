package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.structure_locator.StructureLocatorConfig;
import fku.org.example.fku.features.structure_locator.StructureLocatorFeature;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class StructureLocatorScreen
extends Screen {
    private static final int W = 260;
    private static final int H = 290;
    private static final int LIST_W = 150;
    private int cx;
    private int cy;
    private EditBox seedInput;
    private Button targetBtn;
    private Button fetchSeedBtn;
    private Button locateBtn;
    private Button coordBtn;
    private Button nextBtn;
    private Button clearBtn;
    private Button r10m;
    private Button r1m;
    private Button r1p;
    private Button r10p;
    private Button cd10m;
    private Button cd1m;
    private Button cd1p;
    private Button cd10p;
    private final List<Button> structButtons = new ArrayList<Button>();
    private boolean showList = false;
    private int listScroll = 0;

    public StructureLocatorScreen() {
        super(Component.literal("\u7ed3\u6784\u5b9a\u4f4d"));
    }

    protected void init() {
        super.init();
        this.cx = (this.width - 260) / 2;
        this.cy = (this.height - 290) / 2;
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        this.seedInput = new EditBox(this.font, this.cx + 10, this.cy + 52, 240, 16, Component.literal("\u79cd\u5b50"));
        this.seedInput.setValue(cfg.manualSeed);
        this.seedInput.setMaxLength(32);
        this.addRenderableWidget(this.seedInput);
        this.fetchSeedBtn = this.mkBtn("\u00a7e\u53d6\u79cd\u5b50 (/seed)", this.cx + 10, this.cy + 72, 240, 16, () -> StructureLocatorFeature.requestSeed());
        String cur = this.idxOk(cfg.targetIndex) ? StructureLocatorFeature.TARGETS.get(cfg.targetIndex).name : "?";
        this.targetBtn = this.mkBtn("\u00a7f" + cur + "  \u00a77\u25b6", this.cx + 10, this.cy + 108, 240, 16, () -> {
            this.showList = !this.showList;
            this.rebuildStructList();
        });
        int rbY = this.cy + 152;
        int rbw = 50;
        int gap = 5;
        int rbX0 = this.cx + 10;
        this.r10m = this.mkBtn("-10", rbX0, rbY, rbw, 16, () -> {
            cfg.searchRadius = Math.max(1, cfg.searchRadius - 10);
            cfg.save();
        });
        this.r1m = this.mkBtn("-1", rbX0 + rbw + gap, rbY, rbw, 16, () -> {
            cfg.searchRadius = Math.max(1, cfg.searchRadius - 1);
            cfg.save();
        });
        this.r1p = this.mkBtn("+1", rbX0 + (rbw + gap) * 2, rbY, rbw, 16, () -> {
            cfg.searchRadius = Math.min(128, cfg.searchRadius + 1);
            cfg.save();
        });
        this.r10p = this.mkBtn("+10", rbX0 + (rbw + gap) * 3, rbY, rbw, 16, () -> {
            cfg.searchRadius = Math.min(128, cfg.searchRadius + 10);
            cfg.save();
        });
        int cdY = this.cy + 172;
        int cdw = 50;
        int cdGap = 5;
        int cdX0 = this.cx + 10;
        this.cd10m = this.mkBtn("-10", cdX0, cdY, cdw, 16, () -> {
            cfg.markClearDistance = Math.max(1, cfg.markClearDistance - 10);
            cfg.save();
        });
        this.cd1m = this.mkBtn("-1", cdX0 + cdw + cdGap, cdY, cdw, 16, () -> {
            cfg.markClearDistance = Math.max(1, cfg.markClearDistance - 1);
            cfg.save();
        });
        this.cd1p = this.mkBtn("+1", cdX0 + (cdw + cdGap) * 2, cdY, cdw, 16, () -> {
            cfg.markClearDistance = Math.min(128, cfg.markClearDistance + 1);
            cfg.save();
        });
        this.cd10p = this.mkBtn("+10", cdX0 + (cdw + cdGap) * 3, cdY, cdw, 16, () -> {
            cfg.markClearDistance = Math.min(128, cfg.markClearDistance + 10);
            cfg.save();
        });
        int bw3 = 73;
        int bh = 16;
        int gap3 = 5;
        this.locateBtn = this.mkBtn("\u00a7a\u5b9a\u4f4d\u5e76\u524d\u5f80", this.cx + 10, this.cy + 192, bw3, bh, () -> StructureLocatorFeature.locate(true));
        this.coordBtn = this.mkBtn("\u00a77\u53ea\u663e\u793a\u5750\u6807", this.cx + 15 + bw3, this.cy + 192, bw3, bh, () -> StructureLocatorFeature.locate(false));
        this.mkBtn("\u00a7b\u6807\u8bb0\u7ed3\u6784", this.cx + 20 + (bw3 + gap3) * 2, this.cy + 192, bw3, bh, () -> StructureLocatorFeature.markLocation());
        this.nextBtn = this.mkBtn("\u00a7e\u7a7a\u70b9\u2192\u627e\u4e0b\u4e00\u4e2a", this.cx + 10, this.cy + 212, bw3, bh, () -> StructureLocatorFeature.skipAndNext());
        this.clearBtn = this.mkBtn("\u00a77\u6e05\u7a7a\u8df3\u8fc7\u8bb0\u5f55", this.cx + 15 + bw3, this.cy + 212, bw3, bh, () -> StructureLocatorFeature.clearSkips());
        this.mkBtn("\u00a7c\u6e05\u9664\u6807\u8bb0", this.cx + 20 + (bw3 + gap3) * 2, this.cy + 212, bw3, bh, () -> StructureLocatorFeature.clearMark());
        this.rebuildStructList();
    }

    private Button mkBtn(String text, int x, int y, int w, int h, Runnable action) {
        Button b = Button.builder(Component.literal(text), b2 -> action.run()).bounds(x, y, w, h).build();
        this.addRenderableWidget(b);
        return b;
    }

    private boolean idxOk(int i) {
        return i >= 0 && i < StructureLocatorFeature.TARGETS.size();
    }

    private void rebuildStructList() {
        int start;
        this.structButtons.clear();
        if (!this.showList) {
            return;
        }
        List<StructureLocatorFeature.Target> targets = StructureLocatorFeature.TARGETS;
        int lx = this.cx + 260 + 6;
        int ly = this.cy + 28;
        int itemH = 13;
        int maxVis = Math.min(targets.size(), Math.max(1, 240 / itemH));
        for (int i = start = Math.max(0, Math.min(this.listScroll, targets.size() - maxVis)); i < targets.size() && i < start + maxVis; ++i) {
            int idx = i;
            boolean sel = idx == StructureLocatorConfig.getInstance().targetIndex;
            Button btn = Button.builder(Component.literal(((sel ? "\u00a76\u25b6 " : "  ") + targets.get(i).name)), b -> {
                StructureLocatorConfig c = StructureLocatorConfig.getInstance();
                c.targetIndex = idx;
                c.save();
                this.showList = false;
                this.targetBtn.setMessage(Component.literal(("\u00a7f" + ((StructureLocatorFeature.Target)targets.get(idx)).name + "  \u00a77\u25b6")));
            }).bounds(lx + 4, ly + (i - start) * itemH, 142, itemH).build();
            this.structButtons.add(btn);
        }
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.renderBackground(g);
        StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
        GuiRenderHelper.drawPanelBackground(g, this.cx, this.cy, 260, 290, false);
        g.drawString(this.font, "\u00a7l\u00a76\u7ed3\u6784\u5b9a\u4f4d", this.cx + 10, this.cy + 8, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u79cd\u5b50: " + this.seedStr(cfg), this.cx + 10, this.cy + 28, 0xFFFFFF);
        g.drawString(this.font, "\u00a77\u624b\u52a8\u79cd\u5b50 (\u7559\u7a7a\u7528\u6355\u83b7\u7684):", this.cx + 10, this.cy + 40, 0xCCCCCC);
        g.fill(this.cx + 10, this.cy + 94, this.cx + 260 - 10, this.cy + 95, -12303292);
        g.drawString(this.font, "\u00a77\u76ee\u6807\u7ed3\u6784:", this.cx + 10, this.cy + 100, 0xCCCCCC);
        g.drawString(this.font, "\u00a77\u641c\u7d22\u8303\u56f4: \u00a7f" + cfg.searchRadius + " \u00a77\u533a\u57df", this.cx + 10, this.cy + 142, 0xCCCCCC);
        g.drawString(this.font, "\u00a77\u6807\u8bb0\u6e05\u9664\u8ddd\u79bb: \u00a7f" + cfg.markClearDistance + " \u00a77\u683c", this.cx + 10, this.cy + 162, 0xCCCCCC);
        super.render(g, mx, my, pt);
        if (this.showList) {
            int lx = this.cx + 260 + 4;
            int ly = this.cy + 20;
            int lh = Math.min(StructureLocatorFeature.TARGETS.size() * 13 + 20, 270);
            GuiRenderHelper.drawPanelBackground(g, lx, ly, 150, lh, false);
            g.drawString(this.font, "\u00a77\u9009\u62e9\u7ed3\u6784", lx + 6, ly + 6, 0xCCCCCC);
            g.fill(lx + 4, ly + 16, lx + 150 - 4, ly + 17, -12303292);
            for (Button b : this.structButtons) {
                b.render(g, mx, my, pt);
            }
        }
        g.drawString(this.font, "\u00a77\u00a7o\u2460\u53d6\u79cd\u5b50 \u2461\u9009\u7ed3\u6784(\u25b6) \u2462\u5b9a\u4f4d", this.cx + 10, this.cy + 290 - 14, 0x888888);
    }

    private String seedStr(StructureLocatorConfig cfg) {
        if (cfg.manualSeed != null && !cfg.manualSeed.trim().isEmpty()) {
            return "\u00a7a\u624b\u52a8:" + cfg.manualSeed;
        }
        if (cfg.hasSeed) {
            return "\u00a7b\u6355\u83b7:" + cfg.capturedSeed;
        }
        return "\u00a77\u65e0\u79cd\u5b50";
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && this.showList) {
            for (Button b : this.structButtons) {
                if (!(mx >= b.getX()) || !(mx <= (b.getX() + b.getWidth())) || !(my >= b.getY()) || !(my <= (b.getY() + b.getHeight()))) continue;
                b.onPress();
                return true;
            }
            int lx = this.cx + 260 + 4;
            int ly = this.cy + 20;
            if (!(mx >= lx && mx <= (lx + 150) && my >= ly && my <= (ly + 290 - 20))) {
                this.showList = false;
            }
        }
        if (button == 0 && this.seedInput != null) {
            this.seedInput.mouseClicked(mx, my, button);
            if (mx >= this.seedInput.getX() && mx <= (this.seedInput.getX() + this.seedInput.getWidth()) && my >= this.seedInput.getY() && my <= (this.seedInput.getY() + this.seedInput.getHeight())) {
                StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
                cfg.manualSeed = this.seedInput.getValue().trim();
                cfg.save();
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseScrolled(double mx, double my, double delta) {
        if (this.showList && mx >= (this.cx + 260 + 4) && mx <= (this.cx + 260 + 4 + 150) && my >= (this.cy + 20) && my <= (this.cy + 290)) {
            int maxVis = Math.min(StructureLocatorFeature.TARGETS.size(), Math.max(1, 18));
            this.listScroll = (int)Math.max(0.0, Math.min((StructureLocatorFeature.TARGETS.size() - maxVis), this.listScroll - delta));
            this.rebuildStructList();
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        if (this.seedInput != null && this.seedInput.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                this.seedInput.setFocused(false);
                StructureLocatorConfig cfg = StructureLocatorConfig.getInstance();
                cfg.manualSeed = this.seedInput.getValue().trim();
                cfg.save();
                return true;
            }
            return this.seedInput.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

