package fku.org.example.fku.features.tpaura;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.tpaura.TpAuraConfig;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TpAuraScreen
extends Screen {
    private static final int W = 300;
    private static final int H = 260;
    private int activeTab = 0;
    private AbstractWidget cooldownInput;
    private AbstractWidget delayInput;
    private AbstractWidget rangeInput;
    private AbstractWidget attackDistInput;
    private AbstractWidget tpOffsetInput;
    private AbstractWidget packetsInput;
    private AbstractWidget ceilingStepInput;
    private AbstractWidget entityTypesInput;
    private AbstractWidget whitelistInput;
    private AbstractWidget totemAttacksInput;
    private AbstractWidget totemHeightInput;
    private AbstractWidget autoFlightSpeedInput;
    private AbstractWidget autoFlightHoriInput;

    public TpAuraScreen() {
        super(Component.literal((String)"TpAura \u914d\u7f6e"));
    }

    protected void init() {
        this.clearWidgets();
        this.tpOffsetInput = null;
        this.attackDistInput = null;
        this.rangeInput = null;
        this.delayInput = null;
        this.cooldownInput = null;
        this.whitelistInput = null;
        this.entityTypesInput = null;
        this.ceilingStepInput = null;
        this.packetsInput = null;
        this.totemHeightInput = null;
        this.totemAttacksInput = null;
        this.autoFlightSpeedInput = null;
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 260) / 2;
        String[][] tabs = new String[][]{{"\u653b\u51fb", "\u77ac\u79fb", "\u76ee\u6807", "\u767d\u540d\u5355", "\u5176\u4ed6"}};
        int tx = cx + 2;
        for (int i = 0; i < 5; ++i) {
            int fi = i;
            String name = tabs[0][i];
            int tw = this.font.m_92895_(name) + 14;
            this.addRenderableWidget(Button.builder(Component.literal((String)(i == this.activeTab ? "\u00a7e[" + name + "]\u00a7r" : name)), b -> {
                this.saveInputs();
                this.activeTab = fi;
                this.init();
            }).bounds(tx, cy + 2, Math.max(tw, 44), 16).build());
            tx += Math.max(tw, 44) + 2;
        }
        int ly = cy + 24;
        int sp = 19;
        switch (this.activeTab) {
            case 0: {
                this.addLabel(cx + 2, ly, "\u84c4\u529b\u9608\u503c(0.1~1.0):");
                this.cooldownInput = this.mkEdit(cx + 140, ly, 40, String.valueOf(cfg.cooldownThreshold));
                this.addToggle(cx + 195, ly, "Smart", () -> cfg.attackMode.equals("Smart"), v -> cfg.setAttackMode(v != false ? "Smart" : "Fast"));
                this.addLabel(cx + 2, ly += sp, "\u989d\u5916\u5ef6\u8fdf(0~20tick):");
                this.delayInput = this.mkEdit(cx + 140, ly, 40, String.valueOf(cfg.attackDelay));
                this.addToggle(cx + 2, ly += sp, "\u81ea\u52a8\u5207\u6b66", () -> cfg.autoSwitch, v -> cfg.setAutoSwitch((boolean)v));
                this.addToggle(cx + 110, ly, "\u9700\u8981\u91cd\u9524", () -> cfg.requireMace, v -> cfg.setRequireMace((boolean)v));
                this.addToggle(cx + 210, ly, "\u6325\u52a8\u624b", () -> cfg.swingHand, v -> cfg.setSwingHand((boolean)v));
                this.addToggle(cx + 2, ly += sp, "\u9759\u9ed8\u5207\u56de", () -> cfg.silentSwap, v -> cfg.setSilentSwap((boolean)v));
                break;
            }
            case 1: {
                this.addToggle(cx + 2, ly, "Vanilla", () -> cfg.mode.equals("Vanilla"), v -> cfg.setMode(v != false ? "Vanilla" : "Paper"));
                this.addToggle(cx + 100, ly, "Paper", () -> cfg.mode.equals("Paper"), v -> cfg.setMode(v != false ? "Paper" : "Vanilla"));
                this.addLabel(cx + 2, ly += sp, "\u6700\u5927\u8303\u56f4(1~99):");
                this.rangeInput = this.mkEdit(cx + 120, ly, 40, String.valueOf(cfg.maxRange));
                this.addToggle(cx + 2, ly += sp, "V-Clip\u4e0a\u5347", () -> cfg.goUp, v -> cfg.setGoUp((boolean)v));
                this.addToggle(cx + 110, ly, "\u653b\u51fb\u540e\u56de\u4f20", () -> cfg.returnPos, v -> cfg.setReturnPos((boolean)v));
                this.addToggle(cx + 220, ly, "\u504f\u79fb\u540c\u6b65", () -> cfg.offsetFix, v -> cfg.setOffsetFix((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u57ab\u5305\u6570\u91cf:");
                this.packetsInput = this.mkEdit(cx + 80, ly, 30, String.valueOf(cfg.paperPackets));
                this.addToggle(cx + 125, ly, "\u9650\u5236\u5929\u82b1\u677f", () -> cfg.limitCeiling, v -> cfg.setLimitCeiling((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u5929\u82b1\u677f\u6b65\u957f:");
                this.ceilingStepInput = this.mkEdit(cx + 80, ly, 30, String.valueOf(cfg.ceilingScanStep));
                this.addToggle(cx + 125, ly, "\u00a7a\u81ea\u52a8\u98de\u884c", () -> cfg.autoFlight, v -> cfg.setAutoFlight((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u4e0a\u5347\u901f\u5ea6:");
                this.autoFlightSpeedInput = this.mkEdit(cx + 80, ly, 30, String.valueOf(cfg.autoFlightSpeed));
                this.addLabel(cx + 115, ly, "\u00a77(0~2.0)");
                this.addLabel(cx + 2, ly += sp, "\u6c34\u5e73\u500d\u7387:");
                this.autoFlightHoriInput = this.mkEdit(cx + 80, ly, 30, String.valueOf(cfg.autoFlightHorizontalSpeed));
                this.addLabel(cx + 115, ly, "\u00a77(0~3.0)");
                ly += sp;
                break;
            }
            case 2: {
                this.addToggle(cx + 2, ly, "\u5168\u751f\u7269\u653b\u51fb", () -> cfg.attackAllEntities, v -> cfg.setAttackAllEntities((boolean)v));
                this.addToggle(cx + 130, ly, "\u5ffd\u7565\u5df2\u547d\u540d", () -> cfg.ignoreNamed, v -> cfg.setIgnoreNamed((boolean)v));
                this.addToggle(cx + 2, ly += sp, "\u5ffd\u7565\u670b\u53cb", () -> cfg.ignoreFriends, v -> cfg.setIgnoreFriends((boolean)v));
                this.addToggle(cx + 110, ly, "\u5ffd\u7565\u5df2\u9a6f\u670d", () -> cfg.ignoreTamed, v -> cfg.setIgnoreTamed((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u653b\u51fb\u8ddd\u79bb(3~6):");
                this.attackDistInput = this.mkEdit(cx + 115, ly, 30, String.valueOf(cfg.attackDistance));
                this.addLabel(cx + 155, ly, "TP\u504f\u79fb(0~6):");
                this.tpOffsetInput = this.mkEdit(cx + 225, ly, 30, String.valueOf(cfg.tpOffset));
                this.addLabel(cx + 2, ly += sp, "\u5b9e\u4f53\u7c7b\u578b(\u9017\u53f7\u5206\u9694):");
                this.entityTypesInput = this.mkTextEdit(cx + 155, ly, 130, cfg.entityTypes);
                break;
            }
            case 3: {
                this.addToggle(cx + 2, ly, "\u542f\u7528\u767d\u540d\u5355", () -> cfg.whitelistEnabled, v -> cfg.setWhitelistEnabled((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u767d\u540d\u5355\u73a9\u5bb6(A,B):");
                this.whitelistInput = this.mkTextEdit(cx + 130, ly, 150, cfg.whitelist);
                break;
            }
            case 4: {
                this.addToggle(cx + 2, ly, "\u663e\u793a\u8def\u5f84", () -> cfg.renderPath, v -> cfg.setRenderPath((boolean)v));
                this.addToggle(cx + 110, ly, "\u56fe\u817e\u7ed5\u8fc7", () -> cfg.totemBypass, v -> cfg.setTotemBypass((boolean)v));
                this.addLabel(cx + 2, ly += sp, "\u56fe\u817e\u653b\u51fb\u6b21\u6570:");
                this.totemAttacksInput = this.mkEdit(cx + 100, ly, 30, String.valueOf(cfg.totemAttacks));
                this.addLabel(cx + 145, ly, "\u9ad8\u5ea6\u589e\u52a0:");
                this.totemHeightInput = this.mkEdit(cx + 215, ly, 30, String.valueOf(cfg.totemHeightIncrease));
                this.addLabel(cx + 2, ly += sp, "\u70ed\u952e(\u4e2d\u952e\u70b9\u51fb\u7ec4\u4ef6\u7ed1\u5b9a):");
                String hk = cfg.hotkeyKey >= 0 ? cfg.hotkeyName : "\u672a\u7ed1\u5b9a";
                this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77" + hk)), b -> {}).bounds(cx + 155, ly, 130, 14).build());
            }
        }
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u00a7a\u4fdd\u5b58\u5e76\u8fd4\u56de"), b -> {
            this.saveInputs();
            this.minecraft.setScreen(null);
        }).bounds(cx + 150 - 40, cy + 260 - 22, 80, 16).build());
    }

    private void addLabel(int x, int y, String text) {
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u00a77" + text)), b -> {}).bounds(x, y, this.font.m_92895_(text) + 4, 14).build());
    }

    private void addToggle(int x, int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        boolean cur = getter.getAsBoolean();
        this.addRenderableWidget(Button.builder(Component.literal((String)(label + (cur ? " \u00a7a\u5f00" : " \u00a77\u5173"))), b -> {
            boolean now = !getter.getAsBoolean();
            setter.accept(now);
            b.setMessage(Component.literal((String)(label + (now ? " \u00a7a\u5f00" : " \u00a77\u5173"))));
        }).bounds(x, y, 90, 14).build());
    }

    private AbstractWidget mkEdit(int x, int y, int w, String val) {
        EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal((String)""));
        b.m_94144_(val);
        b.m_94199_(8);
        b.m_94153_(s -> s.matches("[\\d.]*"));
        this.m_7787_(b);
        return b;
    }

    private AbstractWidget mkTextEdit(int x, int y, int w, String val) {
        EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal((String)""));
        b.m_94144_(val);
        b.m_94199_(500);
        b.m_94153_(s -> true);
        this.m_7787_(b);
        return b;
    }

    private void saveInputs() {
        EditBox e2;
        AbstractWidget abstractWidget;
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        try {
            abstractWidget = this.cooldownInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setCooldownThreshold(Double.parseDouble(e2.m_94155_()));
            }
        }
        catch (Exception e2) {
            // ignored
        }
        try {
            abstractWidget = this.delayInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setAttackDelay(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e3) {
            // ignored
        }
        try {
            abstractWidget = this.rangeInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setMaxRange(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e4) {
            // ignored
        }
        try {
            abstractWidget = this.attackDistInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setAttackDistance(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e5) {
            // ignored
        }
        try {
            abstractWidget = this.tpOffsetInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setTpOffset(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e6) {
            // ignored
        }
        try {
            abstractWidget = this.packetsInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setPaperPackets(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e7) {
            // ignored
        }
        try {
            abstractWidget = this.ceilingStepInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setCeilingScanStep(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e8) {
            // ignored
        }
        abstractWidget = this.entityTypesInput;
        if (abstractWidget instanceof EditBox) {
            e2 = (EditBox)abstractWidget;
            cfg.setEntityTypes(e2.m_94155_());
        }
        if ((abstractWidget = this.whitelistInput) instanceof EditBox) {
            e2 = (EditBox)abstractWidget;
            cfg.setWhitelist(e2.m_94155_());
        }
        try {
            abstractWidget = this.totemAttacksInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setTotemAttacks(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception e9) {
            // ignored
        }
        try {
            abstractWidget = this.autoFlightSpeedInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setAutoFlightSpeed(Double.parseDouble(e2.m_94155_()));
            }
        }
        catch (Exception e10) {
            // ignored
        }
        try {
            abstractWidget = this.autoFlightHoriInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setAutoFlightHorizontalSpeed(Double.parseDouble(e2.m_94155_()));
            }
        }
        catch (Exception e11) {
            // ignored
        }
        try {
            abstractWidget = this.totemHeightInput;
            if (abstractWidget instanceof EditBox && !(e2 = (EditBox)abstractWidget).m_94155_().isEmpty()) {
                cfg.setTotemHeightIncrease(Integer.parseInt(e2.m_94155_()));
            }
        }
        catch (Exception exception) {
            // ignored
        }
        cfg.save();
    }

    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        this.fillGradient(g);
        int cx = (this.width - 300) / 2;
        int cy = (this.height - 260) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, 300, 260, false);
        g.drawString(this.font, "\u00a7l\u00a76TpAura \u914d\u7f6e", cx + 8, cy + 20, 0xFFFFFF);
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        String modeDesc = switch (this.activeTab) {
            case 0 -> "\u653b\u51fb\u6a21\u5f0f: " + cfg.attackMode + "  \u84c4\u529b=" + cfg.cooldownThreshold;
            case 1 -> "\u6a21\u5f0f: " + cfg.mode + "  \u8303\u56f4=" + cfg.maxRange + "  " + (cfg.autoFlight ? "\u00a7a\u98de\u884c" : "\u00a77\u65e0\u98de\u884c");
            case 2 -> "\u5168\u751f\u7269=" + cfg.attackAllEntities + "  \u8ddd\u79bb=" + cfg.attackDistance;
            case 3 -> "\u767d\u540d\u5355=" + cfg.whitelistEnabled;
            default -> "\u56fe\u817e=" + cfg.totemBypass + "  \u8def\u5f84=" + cfg.renderPath;
        };
        g.drawString(this.font, "\u00a77" + modeDesc, cx + 8, cy + 260 - 12, 0x666666);
        for (AbstractWidget w : new AbstractWidget[]{this.cooldownInput, this.delayInput, this.rangeInput, this.attackDistInput, this.tpOffsetInput, this.packetsInput, this.ceilingStepInput, this.entityTypesInput, this.whitelistInput, this.totemAttacksInput, this.totemHeightInput, this.autoFlightSpeedInput, this.autoFlightHoriInput}) {
            if (!(w instanceof EditBox)) continue;
            EditBox e = (EditBox)w;
            e.render(g, mx, my, pt);
        }
        super.render(g, mx, my, pt);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        for (AbstractWidget w : new AbstractWidget[]{this.cooldownInput, this.delayInput, this.rangeInput, this.attackDistInput, this.tpOffsetInput, this.packetsInput, this.ceilingStepInput, this.entityTypesInput, this.whitelistInput, this.totemAttacksInput, this.totemHeightInput, this.autoFlightSpeedInput, this.autoFlightHoriInput}) {
            if (!(w instanceof EditBox)) continue;
            EditBox e = (EditBox)w;
            e.mouseClicked(mx, my, button);
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean m_7933_(int k, int s, int m) {
        for (AbstractWidget w : new AbstractWidget[]{this.cooldownInput, this.delayInput, this.rangeInput, this.attackDistInput, this.tpOffsetInput, this.packetsInput, this.ceilingStepInput, this.entityTypesInput, this.whitelistInput, this.totemAttacksInput, this.totemHeightInput, this.autoFlightSpeedInput, this.autoFlightHoriInput}) {
            EditBox e;
            if (!(w instanceof EditBox) || !(e = (EditBox)w).m_93696_()) continue;
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

