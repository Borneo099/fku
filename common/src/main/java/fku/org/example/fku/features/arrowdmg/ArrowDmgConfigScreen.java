package fku.org.example.fku.features.arrowdmg; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * ArrowDmg（32k弓）配置 — 静默保存 + ESP 渲染选项
 */
public class ArrowDmgConfigScreen extends Screen {
    private static final int W = 280, H = 260;
    private int activeTab = 0;
    private AbstractWidget packetsInput, chargeInput, bypassStrInput, bypassDelInput, rangeInput, expandInput;
    private EditBox customBowIdsInput;

    public ArrowDmgConfigScreen() { super(Component.literal("32k弓配置")); }

    @Override
    protected void init() {
        clearWidgets();
        packetsInput = chargeInput = bypassStrInput = bypassDelInput = rangeInput = expandInput = null;

        var cfg = ArrowDmgConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;

        // Tab头
        String[] tabs = {"基础","连射","图腾","自瞄","渲染"};
        int tx = cx + 2;
        for (int i = 0; i < 5; i++) {
            final int fi = i;
            int tw = font.width(tabs[i]) + 14;
            addRenderableWidget(Button.builder(
                Component.literal(i == activeTab ? "§e["+tabs[i]+"]§r" : tabs[i]),
                b -> { saveInputs(); activeTab = fi; init(); }
            ).bounds(tx, cy + 2, Math.max(tw, 44), 16).build());
            tx += Math.max(tw, 44) + 2;
        }

        int ly = cy + 24, sp = 20;
        switch (activeTab) {
            case 0 -> {
                // ★ 模式选择：大狙/机关枪
                addRenderableWidget(Button.builder(Component.literal("§6大狙模式"), b -> {
                    ArrowDmgConfig c = ArrowDmgConfig.getInstance();
                    c.packets = 7000; c.autoShoot = false; ArrowDmgConfig.save();
                    if (packetsInput instanceof EditBox e) e.setValue("7000");
                    // 同步连射按钮状态
                    init();
                }).bounds(cx+2, ly, 80, 16).build());
                addRenderableWidget(Button.builder(Component.literal("§b机关枪模式"), b -> {
                    ArrowDmgConfig c = ArrowDmgConfig.getInstance();
                    c.packets = 500; c.autoShoot = true; ArrowDmgConfig.save();
                    if (packetsInput instanceof EditBox e) e.setValue("500");
                    init();
                }).bounds(cx+84, ly, 90, 16).build());
                ly += 18;

                addRenderableWidget(newButton(cx+2, ly, "发包数(建议≤10000):"));
                packetsInput = mkEdit(cx+115, ly, 50, String.valueOf((int)cfg.packets), "packets");
                addC(cx+2, ly+sp, "VClip瞬移", cfg.vClip, v -> cfg.vClip = v);
                addC(cx+85, ly+sp, "三叉戟", cfg.yeetTridents, v -> cfg.yeetTridents = v);
                addC(cx+2, ly+sp*2, "防摔", cfg.useOffset, v -> cfg.useOffset = v);
                addC(cx+85, ly+sp*2, "箭伤飞行", cfg.arrowDmgFly, v -> cfg.arrowDmgFly = v);
                addC(cx+2, ly+sp*3, "碰撞箱放大", cfg.expandHitbox > 0, v -> {
                    cfg.expandHitbox = v ? 1.5 : 1.0;
                    if (expandInput instanceof EditBox e) e.setValue(String.format("%.1f", cfg.expandHitbox));
                });
                addRenderableWidget(newButton(cx+2, ly+sp*4, "倍数:"));
                expandInput = mkEdit(cx+40, ly+sp*4, 30, String.format("%.1f",cfg.expandHitbox), "expand");
                addC(cx+85, ly+sp*3, "Y校准", cfg.yCalibrate, v -> cfg.yCalibrate = v);
                addC(cx+2, ly+sp*5, "自动下蹲", cfg.autoCrouch, v -> cfg.autoCrouch = v);
                // 自定义模组弓物品ID输入框（参考鬼手秒切配置界面风格）
                addRenderableWidget(Button.builder(Component.literal("§7自定义弓ID:"), b -> {}).bounds(cx+2, ly+sp*6+2, 80, 14).build());
                customBowIdsInput = new EditBox(font, cx+2, ly+sp*7+2, W-24, 14, Component.literal(""));
                customBowIdsInput.setMaxLength(100000);
                customBowIdsInput.setValue(cfg.customBowIds);
                addRenderableWidget(customBowIdsInput);
            }
            case 1 -> {
                addRenderableWidget(newButton(cx+2, ly, "蓄力Tick:"));
                chargeInput = mkEdit(cx+70, ly, 30, String.valueOf(cfg.charge), "charge");
                addC(cx+110, ly, "连射", cfg.autoShoot, v -> cfg.autoShoot = v);
                addC(cx+2, ly+sp, "仅右键时连射", cfg.onlyWhenHoldingRightClick, v -> cfg.onlyWhenHoldingRightClick = v);
            }
            case 2 -> {
                addRenderableWidget(newButton(cx+2, ly, "图腾绕过发包数:"));
                bypassStrInput = mkEdit(cx+105, ly, 50, String.valueOf((int)cfg.bypassStrength), "bypassStr");
                addRenderableWidget(newButton(cx+2, ly+sp, "延迟Tick:"));
                bypassDelInput = mkEdit(cx+70, ly+sp, 30, String.valueOf(cfg.bypassDelay), "bypassDel");
                addC(cx+165, ly, "启用", cfg.totemBypass, v -> cfg.totemBypass = v);
            }
            case 3 -> {
                addRenderableWidget(newButton(cx+2, ly, "范围:"));
                rangeInput = mkEdit(cx+35, ly, 40, String.valueOf((int)cfg.aimRange), "range");
                addC(cx+85, ly, "自瞄", cfg.aimbot, v -> cfg.aimbot = v);
                addC(cx+165, ly, "穿墙", cfg.ignoreWalls, v -> cfg.ignoreWalls = v);
                addC(cx+2, ly+sp, "仅拉弓时", cfg.aimOnlyWhenHoldingRightClick, v -> cfg.aimOnlyWhenHoldingRightClick = v);
                String[] pri = {"Angle","Distance","Health"};
                int pi = java.util.List.of(pri).indexOf(cfg.priority);
                if (pi < 0) pi = 0;
                final int fpi = pi;
                addRenderableWidget(Button.builder(Component.literal("优先:"+pri[fpi]),
                    b -> { int n = (fpi+1)%3; cfg.priority=pri[n]; ArrowDmgConfig.save(); b.setMessage(Component.literal("优先:"+pri[n])); })
                    .bounds(cx+85, ly+sp, 80, 14).build());
            }
            case 4 -> {
                // 渲染 Tab — 仅方框
                addC(cx+2, ly, "显示方框", cfg.showBox, v -> cfg.showBox = v);
                addRenderableWidget(newButton(cx+2, ly+sp, "渲染距离:"));
                rangeInput = mkEdit(cx+75, ly+sp, 50, String.valueOf(cfg.renderMaxDistance), "renderDist");
            }
        }

        // 保存并返回 按钮
        addRenderableWidget(Button.builder(Component.literal("§a保存并返回"),
            b -> { saveInputs(); ArrowDmgConfig.save(); this.minecraft.setScreen(null); })
            .bounds(cx + W/2 - 40, cy + H - 22, 80, 16).build());
    }

    // ════════ 保存输入框值到配置 ════════
    private void saveInputs() {
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        try {
            if (packetsInput instanceof EditBox e && !e.getValue().isEmpty())
                cfg.packets = Math.max(1, Double.parseDouble(e.getValue()));
        } catch (Exception ignored) {}
        try {
            if (chargeInput instanceof EditBox e && !e.getValue().isEmpty())
                cfg.charge = Math.max(1, Math.min(20, Integer.parseInt(e.getValue())));
        } catch (Exception ignored) {}
        try {
            if (bypassStrInput instanceof EditBox e && !e.getValue().isEmpty())
                cfg.bypassStrength = Math.max(1, Double.parseDouble(e.getValue()));
        } catch (Exception ignored) {}
        try {
            if (bypassDelInput instanceof EditBox e && !e.getValue().isEmpty())
                cfg.bypassDelay = Math.max(1, Math.min(10, Integer.parseInt(e.getValue())));
        } catch (Exception ignored) {}
        try {
            if (rangeInput instanceof EditBox e && !e.getValue().isEmpty()) {
                int v = Integer.parseInt(e.getValue());
                if (activeTab == 3) cfg.aimRange = Math.max(1, v);
                else cfg.renderMaxDistance = Math.max(0, v);
            }
        } catch (Exception ignored) {}
        try {
            if (expandInput instanceof EditBox e && !e.getValue().isEmpty())
                cfg.expandHitbox = Math.max(0.5, Math.min(5, Double.parseDouble(e.getValue())));
        } catch (Exception ignored) {}
        // 自定义弓ID（逗号分隔的物品ID列表）
        if (customBowIdsInput != null) {
            cfg.customBowIds = customBowIdsInput.getValue().trim();
        }
        ArrowDmgConfig.save();
    }

    // ════════ 组件工厂 ════════
    private void addC(int x, int y, String label, boolean cur, java.util.function.Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(Component.literal(label+(cur?"§a ON":"§c OFF")),
            b -> {
                var cfg = ArrowDmgConfig.getInstance();
                boolean actual = getToggleVal(label, cfg);
                setter.accept(!actual);
                ArrowDmgConfig.save();
                b.setMessage(Component.literal(label+(!actual?"§a ON":"§c OFF")));
            }).bounds(x, y, 90, 14).build());
    }
    private static boolean getToggleVal(String label, ArrowDmgConfig cfg) {
        return switch (label) {
            case "VClip瞬移" -> cfg.vClip; case "三叉戟" -> cfg.yeetTridents;
            case "防摔" -> cfg.useOffset;
            case "箭伤飞行" -> cfg.arrowDmgFly; case "连射" -> cfg.autoShoot;
            case "仅右键时连射" -> cfg.onlyWhenHoldingRightClick;
            case "图腾绕过","启用" -> cfg.totemBypass;
            case "自瞄" -> cfg.aimbot; case "穿墙" -> cfg.ignoreWalls;
            case "仅拉弓时" -> cfg.aimOnlyWhenHoldingRightClick;
            case "显示方框" -> cfg.showBox;
            case "碰撞箱放大" -> cfg.expandHitbox > 1.0;
            case "Y校准" -> cfg.yCalibrate;
            case "自动下蹲" -> cfg.autoCrouch;
            default -> false;
        };
    }
    private AbstractWidget newButton(int x, int y, String t) {
        return Button.builder(Component.literal(t), b->{}).bounds(x, y, font.width(t), 14).build();
    }
    private AbstractWidget mkEdit(int x, int y, int w, String v, String field) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(v); b.setMaxLength(8); b.setFilter(s -> s.matches("[\\d.]*"));
        addWidget(b); return b;
    }

    @Override public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width-W)/2, cy = (height-H)/2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        super.render(g, mx, my, pt);
        if (packetsInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (chargeInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (bypassStrInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (bypassDelInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (rangeInput instanceof EditBox e) e.render(g, mx, my, pt);
        if (expandInput instanceof EditBox e) e.render(g, mx, my, pt);
        // 自定义弓ID输入框（仅基础Tab显示）
        if (activeTab == 0 && customBowIdsInput != null) {
            g.drawString(font, "§7逗号分隔多个物品ID，如: §fmymod:mybow,othermod:otherbow", cx + 5, cy + H - 38, 0x888888);
            customBowIdsInput.render(g, mx, my, pt);
        }
        // 底部提示
        if (activeTab == 0)
            g.drawString(font, "§7提示: 高发包数+VClip开启易卡死", cx + 5, cy + H - 12, 0x666666);
    }
    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (packetsInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (chargeInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (bypassStrInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (bypassDelInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (rangeInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (expandInput instanceof EditBox e) e.mouseClicked(mx, my, btn);
        if (customBowIdsInput != null) customBowIdsInput.mouseClicked(mx, my, btn);
        return super.mouseClicked(mx, my, btn);
    }
    @Override public boolean keyPressed(int k, int s, int m) {
        if (packetsInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (chargeInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (bypassStrInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (bypassDelInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (rangeInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (expandInput instanceof EditBox e && e.isFocused()) return e.keyPressed(k,s,m);
        if (customBowIdsInput != null && customBowIdsInput.isFocused()) return customBowIdsInput.keyPressed(k,s,m);
        if (k == 256) { saveInputs(); this.minecraft.setScreen(null); return true; }
        return super.keyPressed(k,s,m);
    }
    @Override public void onClose() { saveInputs(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
