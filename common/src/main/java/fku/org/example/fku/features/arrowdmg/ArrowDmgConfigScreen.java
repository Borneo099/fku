package fku.org.example.fku.features.arrowdmg;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.arrowdmg.ArrowDmgConfig;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ArrowDmgConfigScreen
extends Screen {
    private static final int W = 280;
    private static final int H = 240;
    private int activeTab = 0;
    private AbstractWidget packetsInput;
    private AbstractWidget chargeInput;
    private AbstractWidget bypassStrInput;
    private AbstractWidget bypassDelInput;
    private AbstractWidget rangeInput;
    private AbstractWidget expandInput;
    private AbstractWidget customBowInput;

    public ArrowDmgConfigScreen() {
        super(Component.literal((String)"32k\u5f13\u914d\u7f6e"));
    }

    protected void init() {
        this.clearWidgets();
        this.expandInput = null;
        this.rangeInput = null;
        this.bypassDelInput = null;
        this.bypassStrInput = null;
        this.chargeInput = null;
        this.packetsInput = null;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 240) / 2;
        String[] tabs = new String[]{"\u57fa\u7840", "\u8fde\u5c04", "\u56fe\u817e", "\u81ea\u7784", "\u6e32\u67d3"};
        int tx = cx + 2;
        for (int i = 0; i < 5; ++i) {
            int fi = i;
            int tw = this.font.m_92895_(tabs[i]) + 14;
            this.addRenderableWidget(Button.builder(Component.literal((String)(i == this.activeTab ? "\u00a7e[" + tabs[i] + "]\u00a7r" : tabs[i])), b -> {
                this.saveInputs();
                this.activeTab = fi;
                this.init();
            }).bounds(tx, cy + 2, Math.max(tw, 44), 16).build());
            tx += Math.max(tw, 44) + 2;
        }
        int ly = cy + 24;
        int sp = 20;
        switch (this.activeTab) {
            case 0: {
                this.addRenderableWidget(this.newButton(cx + 2, ly, "\u53d1\u5305\u6570(\u5efa\u8bae\u226410000):"));
                this.packetsInput = this.mkEdit(cx + 115, ly, 50, String.valueOf(cfg.packets), "packets");
                this.addC(cx + 2, ly + sp, "VClip\u77ac\u79fb", cfg.vClip, v -> {
                    cfg.vClip = v;
                });
                this.addC(cx + 85, ly + sp, "\u4e09\u53c9\u621f", cfg.yeetTridents, v -> {
                    cfg.yeetTridents = v;
                });
                this.addC(cx + 2, ly + sp * 2, "\u9632\u6454", cfg.useOffset, v -> {
                    cfg.useOffset = v;
                });
                this.addC(cx + 85, ly + sp * 2, "\u7bad\u4f24\u98de\u884c", cfg.arrowDmgFly, v -> {
                    cfg.arrowDmgFly = v;
                });
                this.addC(cx + 2, ly + sp * 3, "\u78b0\u649e\u7bb1\u653e\u5927", cfg.expandHitbox > 0.0, v -> {
                    cfg.expandHitbox = v != false ? 1.5 : 1.0;
                    AbstractWidget patt2434$temp = this.expandInput;
                    if (patt2434$temp instanceof EditBox) {
                        EditBox e = (EditBox)patt2434$temp;
                        e.m_94144_(String.format("%.1f", cfg.expandHitbox));
                    }
                });
                this.addRenderableWidget(this.newButton(cx + 2, ly + sp * 4, "\u500d\u6570:"));
                this.expandInput = this.mkEdit(cx + 40, ly + sp * 4, 30, String.format("%.1f", cfg.expandHitbox), "expand");
                this.addC(cx + 85, ly + sp * 3, "Y\u6821\u51c6", cfg.yCalibrate, v -> {
                    cfg.yCalibrate = v;
                });
                this.addC(cx + 2, ly + sp * 5, "\u81ea\u52a8\u4e0b\u8e72", cfg.autoCrouch, v -> {
                    cfg.autoCrouch = v;
                });
                this.addRenderableWidget(this.newButton(cx + 2, ly + sp * 6, "\u00a77\u81ea\u5b9a\u4e49\u5f13ID(\u9017\u53f7\u5206\u9694):"));
                this.customBowInput = new EditBox(this.font, cx + 2, ly + sp * 7, 260, 16, Component.literal((String)""));
                ((EditBox)this.customBowInput).m_94199_(100000);
                ((EditBox)this.customBowInput).m_94144_(cfg.customBowIds);
                this.addRenderableWidget(this.customBowInput);
                break;
            }
            case 1: {
                this.addRenderableWidget(this.newButton(cx + 2, ly, "\u84c4\u529bTick:"));
                this.chargeInput = this.mkEdit(cx + 70, ly, 30, String.valueOf(cfg.charge), "charge");
                this.addC(cx + 110, ly, "\u8fde\u5c04", cfg.autoShoot, v -> {
                    cfg.autoShoot = v;
                });
                this.addC(cx + 2, ly + sp, "\u4ec5\u53f3\u952e\u65f6\u8fde\u5c04", cfg.onlyWhenHoldingRightClick, v -> {
                    cfg.onlyWhenHoldingRightClick = v;
                });
                break;
            }
            case 2: {
                this.addRenderableWidget(this.newButton(cx + 2, ly, "\u56fe\u817e\u7ed5\u8fc7\u53d1\u5305\u6570:"));
                this.bypassStrInput = this.mkEdit(cx + 105, ly, 50, String.valueOf(cfg.bypassStrength), "bypassStr");
                this.addRenderableWidget(this.newButton(cx + 2, ly + sp, "\u5ef6\u8fdfTick:"));
                this.bypassDelInput = this.mkEdit(cx + 70, ly + sp, 30, String.valueOf(cfg.bypassDelay), "bypassDel");
                this.addC(cx + 165, ly, "\u542f\u7528", cfg.totemBypass, v -> {
                    cfg.totemBypass = v;
                });
                break;
            }
            case 3: {
                this.addRenderableWidget(this.newButton(cx + 2, ly, "\u8303\u56f4:"));
                this.rangeInput = this.mkEdit(cx + 35, ly, 40, String.valueOf(cfg.aimRange), "range");
                this.addC(cx + 85, ly, "\u81ea\u7784", cfg.aimbot, v -> {
                    cfg.aimbot = v;
                });
                this.addC(cx + 165, ly, "\u7a7f\u5899", cfg.ignoreWalls, v -> {
                    cfg.ignoreWalls = v;
                });
                this.addC(cx + 2, ly + sp, "\u4ec5\u62c9\u5f13\u65f6", cfg.aimOnlyWhenHoldingRightClick, v -> {
                    cfg.aimOnlyWhenHoldingRightClick = v;
                });
                String[] pri = new String[]{"Angle", "Distance", "Health"};
                int pi = List.of(pri).indexOf(cfg.priority);
                if (pi < 0) {
                    pi = 0;
                }
                int fpi = pi;
                this.addRenderableWidget(Button.builder(Component.literal((String)("\u4f18\u5148:" + pri[fpi])), b -> {
                    int n = (fpi + 1) % 3;
                    cfg.priority = pri[n];
                    ArrowDmgConfig.save();
                    b.setMessage(Component.literal((String)("\u4f18\u5148:" + pri[n])));
                }).bounds(cx + 85, ly + sp, 80, 14).build());
                break;
            }
            case 4: {
                this.addC(cx + 2, ly, "\u663e\u793a\u65b9\u6846", cfg.showBox, v -> {
                    cfg.showBox = v;
                });
                this.addRenderableWidget(this.newButton(cx + 2, ly + sp, "\u6e32\u67d3\u8ddd\u79bb:"));
                this.rangeInput = this.mkEdit(cx + 75, ly + sp, 50, String.valueOf(cfg.renderMaxDistance), "renderDist");
            }
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u8fd4\u56de"), b -> {
            this.saveInputs();
            ArrowDmgConfig.save();
            this.minecraft.setScreen(null);
        }).bounds(cx + 140 - 40, cy + 240 - 22, 80, 16).build());
    }

    private void saveInputs() {
        EditBox e2;
        AbstractWidget abstractWidget;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        try {
            abstractWidget = this.packetsInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.packets = Math.max(1.0, Double.parseDouble(e2.m_94155_()));
            }
        }
        catch (Exception e2) {
            // ignored
        }
        try {
            abstractWidget = this.chargeInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.charge = Math.max(1, Math.min(20, Integer.parseInt(e2.m_94155_())));
            }
        }
        catch (Exception e3) {
            // ignored
        }
        try {
            abstractWidget = this.bypassStrInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.bypassStrength = Math.max(1.0, Double.parseDouble(e2.m_94155_()));
            }
        }
        catch (Exception e4) {
            // ignored
        }
        try {
            abstractWidget = this.bypassDelInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.bypassDelay = Math.max(1, Math.min(10, Integer.parseInt(e2.m_94155_())));
            }
        }
        catch (Exception e5) {
            // ignored
        }
        try {
            abstractWidget = this.rangeInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                int v = Integer.parseInt(e2.m_94155_());
                if (this.activeTab == 3) {
                    cfg.aimRange = Math.max(1, v);
                } else {
                    cfg.renderMaxDistance = Math.max(0, v);
                }
            }
        }
        catch (Exception e6) {
            // ignored
        }
        try {
            AbstractWidget abstractWidget2 = this.expandInput;
            if (abstractWidget2 instanceof EditBox && !(e2 = (EditBox)abstractWidget2).m_94155_().isEmpty()) {
                cfg.expandHitbox = Math.max(0.5, Math.min(5.0, Double.parseDouble(e2.m_94155_())));
            }
        }
        catch (Exception e7) {
            // ignored
        }
        AbstractWidget abstractWidget3 = this.customBowInput;
        if (abstractWidget3 instanceof EditBox) {
            e2 = (EditBox)abstractWidget3;
            cfg.customBowIds = e2.m_94155_();
        }
        ArrowDmgConfig.save();
    }

    private void addC(int x, int y, String label, boolean cur, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(Component.literal((String)(label + (cur ? "\u00a7a ON" : "\u00a7c OFF"))), b -> {
            ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
            boolean actual = ArrowDmgConfigScreen.getToggleVal(label, cfg);
            setter.accept(!actual);
            ArrowDmgConfig.save();
            b.setMessage(Component.literal((String)(label + (!actual ? "\u00a7a ON" : "\u00a7c OFF"))));
        }).bounds(x, y, 90, 14).build());
    }

    private static boolean getToggleVal(String label, ArrowDmgConfig cfg) {
        return switch (label) {
            case "VClip\u77ac\u79fb" -> cfg.vClip;
            case "\u4e09\u53c9\u621f" -> cfg.yeetTridents;
            case "\u9632\u6454" -> cfg.useOffset;
            case "\u7bad\u4f24\u98de\u884c" -> cfg.arrowDmgFly;
            case "\u8fde\u5c04" -> cfg.autoShoot;
            case "\u4ec5\u53f3\u952e\u65f6\u8fde\u5c04" -> cfg.onlyWhenHoldingRightClick;
            case "\u56fe\u817e\u7ed5\u8fc7", "\u542f\u7528" -> cfg.totemBypass;
            case "\u81ea\u7784" -> cfg.aimbot;
            case "\u7a7f\u5899" -> cfg.ignoreWalls;
            case "\u4ec5\u62c9\u5f13\u65f6" -> cfg.aimOnlyWhenHoldingRightClick;
            case "\u663e\u793a\u65b9\u6846" -> cfg.showBox;
            case "\u78b0\u649e\u7bb1\u653e\u5927" -> {
                if (cfg.expandHitbox > 1.0) {
                    yield true;
                }
                yield false;
            }
            case "Y\u6821\u51c6" -> cfg.yCalibrate;
            case "\u81ea\u52a8\u4e0b\u8e72" -> cfg.autoCrouch;
            default -> false;
        };
    }

    private AbstractWidget newButton(int x, int y, String t) {
        return Button.builder(Component.literal((String)t), b -> {}).bounds(x, y, this.font.m_92895_(t), 14).build();
    }

    private AbstractWidget mkEdit(int x, int y, int w, String v, String field) {
        EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal((String)""));
        b.m_94144_(v);
        b.m_94199_(8);
        b.m_94153_(s -> s.matches("[\\d.]*"));
        this.m_7787_(b);
        return b;
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        EditBox e;
        this.fillGradient(g);
        int cx = (this.width - 280) / 2;
        int cy = (this.height - 240) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 280, 240, false);
        super.render(g, mx, my, pt);
        AbstractWidget abstractWidget = this.packetsInput;
        if (abstractWidget instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.chargeInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.bypassStrInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.bypassDelInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.rangeInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.expandInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if ((abstractWidget = this.customBowInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.render(g, mx, my, pt);
        }
        if (this.activeTab == 0) {
            g.drawString(this.font, "\u00a77\u63d0\u793a: \u9ad8\u53d1\u5305\u6570+VClip\u5f00\u542f\u6613\u5361\u6b7b", cx + 5, cy + 240 - 12, 0x666666);
        }
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        EditBox e;
        AbstractWidget abstractWidget = this.packetsInput;
        if (abstractWidget instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.chargeInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.bypassStrInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.bypassDelInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.rangeInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.expandInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        if ((abstractWidget = this.customBowInput) instanceof EditBox) {
            e = (EditBox)abstractWidget;
            e.mouseClicked(mx, my, btn);
        }
        return super.mouseClicked(mx, my, btn);
    }

    public boolean m_7933_(int k, int s, int m) {
        EditBox e;
        AbstractWidget abstractWidget = this.packetsInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.chargeInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.bypassStrInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.bypassDelInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.rangeInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.expandInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        abstractWidget = this.customBowInput;
        if (abstractWidget instanceof EditBox && (e = (EditBox)abstractWidget).m_93696_()) {
            return e.m_7933_(k, s, m);
        }
        if (k == 256) {
            this.saveInputs();
            this.minecraft.setScreen(null);
            return true;
        }
        return super.m_7933_(k, s, m);
    }

    public void onClose() {
        this.saveInputs();
        super.onClose();
    }

    public boolean isPauseScreen() {
        return false;
    }
}

