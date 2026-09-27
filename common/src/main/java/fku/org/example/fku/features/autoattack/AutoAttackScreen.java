package fku.org.example.fku.features.autoattack; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * AutoAttack 配置界面 — 分 Tab 排版（风格对齐 TpAuraScreen）
 * Tab：通用设置 / 目标过滤 / 距离与安全
 */
public class AutoAttackScreen extends Screen {

    private static final int W = 320, H = 250;
    private int activeTab = 0;

    private AbstractWidget cooldownInput, intervalInput, rangeInput, maxApsInput;
    private AbstractWidget entitiesInput, playerListInput;

    public AutoAttackScreen() {
        super(Component.literal("自动攻击 配置"));
    }

    @Override
    protected void init() {
        clearWidgets();
        cooldownInput = intervalInput = rangeInput = maxApsInput = null;
        entitiesInput = playerListInput = null;

        var cfg = AutoAttackConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;

        // ── Tab 头 ──
        String[][] tabs = {{"通用设置", "目标过滤", "距离与安全"}};
        int tx = cx + 2;
        for (int i = 0; i < 3; i++) {
            final int fi = i;
            String name = tabs[0][i];
            int tw = font.width(name) + 14;
            addRenderableWidget(Button.builder(
                Component.literal(i == activeTab ? "§e[" + name + "]§r" : name),
                b -> { saveInputs(); activeTab = fi; init(); }
            ).bounds(tx, cy + 2, Math.max(tw, 60), 16).build());
            tx += Math.max(tw, 60) + 2;
        }

        int ly = cy + 24, sp = 19;

        switch (activeTab) {
            case 0 -> { // 通用设置
                addLabel(cx + 2, ly, "攻击模式:");
                addToggle(cx + 70, ly, "SMART", () -> cfg.attackMode.equals("SMART"), v -> cfg.attackMode = (v ? "SMART" : "FAST"));
                addToggle(cx + 165, ly, "FAST", () -> cfg.attackMode.equals("FAST"), v -> cfg.attackMode = (v ? "FAST" : "SMART"));
                ly += sp;

                addLabel(cx + 2, ly, "冷却阈值(0.1~1.0):");
                cooldownInput = mkEdit(cx + 140, ly, 40, String.valueOf(cfg.cooldownThreshold));
                ly += sp;

                addLabel(cx + 2, ly, "攻击间隔(0~10tick):");
                intervalInput = mkEdit(cx + 140, ly, 40, String.valueOf(cfg.attackInterval));
                ly += sp;

                addToggle(cx + 2, ly, "随机间隔±1", () -> cfg.randomInterval, v -> cfg.randomInterval = v);
                addToggle(cx + 130, ly, "挥动手", () -> cfg.swingHand, v -> cfg.swingHand = v);
                ly += sp;

                addToggle(cx + 2, ly, "仅持武器生效", () -> cfg.onlyWhenHoldingWeapon, v -> cfg.onlyWhenHoldingWeapon = v);
            }
            case 1 -> { // 目标过滤
                addToggle(cx + 2, ly, "忽略已命名", () -> cfg.ignoreNamed, v -> cfg.ignoreNamed = v);
                addToggle(cx + 130, ly, "忽略已驯服", () -> cfg.ignoreTamed, v -> cfg.ignoreTamed = v);
                ly += sp;

                addToggle(cx + 2, ly, "忽略隐身", () -> cfg.ignoreInvisible, v -> cfg.ignoreInvisible = v);
                addToggle(cx + 130, ly, "忽略创造", () -> cfg.ignoreCreative, v -> cfg.ignoreCreative = v);
                ly += sp;

                addToggle(cx + 2, ly, "忽略好友", () -> cfg.ignoreFriends, v -> cfg.ignoreFriends = v);
                addToggle(cx + 130, ly, "锁定目标", () -> cfg.lockTarget, v -> cfg.lockTarget = v);
                ly += sp;

                addLabel(cx + 2, ly, "名单模式:");
                // 单选组：Off / 白名单 / 黑名单 互斥，选中一个自动关闭其它
                listOffBtn = addToggle(cx + 70, ly, "Off", () -> cfg.listMode.equals("Off"),
                        v -> { if (v) { cfg.listMode = "Off"; updateListModeUI(); } });
                listWhiteBtn = addToggle(cx + 150, ly, "白名单", () -> cfg.listMode.equals("Whitelist"),
                        v -> { if (v) { cfg.listMode = "Whitelist"; updateListModeUI(); } });
                listBlackBtn = addToggle(cx + 230, ly, "黑名单", () -> cfg.listMode.equals("Blacklist"),
                        v -> { if (v) { cfg.listMode = "Blacklist"; updateListModeUI(); } });
                ly += sp;

                addLabel(cx + 2, ly, "玩家名单(逗号分隔):");
                playerListInput = mkTextEdit(cx + 140, ly, 165, cfg.playerList);
                ly += sp + 4;

                addLabel(cx + 2, ly, "实体类型(逗号分隔):");
                entitiesInput = mkTextEdit(cx + 140, ly, 165, cfg.entities);
                ly += sp;
                addLabel(cx + 2, ly, "§7(留空=攻击所有类型)");
            }
            case 2 -> { // 距离与安全
                addLabel(cx + 2, ly, "最大距离(1.0~8.0):");
                rangeInput = mkEdit(cx + 140, ly, 40, String.valueOf(cfg.maxRange));
                ly += sp;

                addLabel(cx + 2, ly, "每秒上限(1~20):");
                maxApsInput = mkEdit(cx + 140, ly, 40, String.valueOf(cfg.maxAttacksPerSecond));
                ly += sp;

                addToggle(cx + 2, ly, "仅瞄准时攻击", () -> cfg.onlyWhenLookingAt, v -> cfg.onlyWhenLookingAt = v);
                ly += sp;

                addToggle(cx + 2, ly, "潜行禁用", () -> cfg.disableOnSneak, v -> cfg.disableOnSneak = v);
                addToggle(cx + 130, ly, "水中禁用", () -> cfg.disableInLiquid, v -> cfg.disableInLiquid = v);
            }
        }

        addRenderableWidget(Button.builder(Component.literal("§a保存并返回"),
            b -> { saveInputs(); this.minecraft.setScreen(null); })
            .bounds(cx + W / 2 - 40, cy + H - 22, 80, 16).build());
    }

    // ──────── 组件工厂（对齐 TpAuraScreen） ────────

    private void addLabel(int x, int y, String text) {
        addRenderableWidget(Button.builder(Component.literal("§7" + text), b -> {}).bounds(x, y, font.width(text) + 4, 14).build());
    }

    private Button addToggle(int x, int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        boolean cur = getter.getAsBoolean();
        return addRenderableWidget(Button.builder(
                Component.literal(label + (cur ? " §a开" : " §7关")),
                b -> {
                    boolean now = !getter.getAsBoolean();
                    setter.accept(now);
                    b.setMessage(Component.literal(label + (now ? " §a开" : " §7关")));
                }
        ).bounds(x, y, 90, 14).build());
    }

    /** 名单模式三选项互斥（单选）：选中一个自动关闭其它，并即时刷新三个按钮的显示 */
    private Button listOffBtn, listWhiteBtn, listBlackBtn;
    private void updateListModeUI() {
        var cfg = AutoAttackConfig.getInstance();
        listOffBtn.setMessage(Component.literal("Off" + (cfg.listMode.equals("Off") ? " §a开" : " §7关")));
        listWhiteBtn.setMessage(Component.literal("白名单" + (cfg.listMode.equals("Whitelist") ? " §a开" : " §7关")));
        listBlackBtn.setMessage(Component.literal("黑名单" + (cfg.listMode.equals("Blacklist") ? " §a开" : " §7关")));
    }

    private AbstractWidget mkEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(8); b.setFilter(s -> s.matches("[\\d.]*"));
        addWidget(b); return b;
    }

    private AbstractWidget mkTextEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(4096); b.setFilter(s -> true);
        addWidget(b); return b;
    }

    private void saveInputs() {
        var cfg = AutoAttackConfig.getInstance();
        try { if (cooldownInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.cooldownThreshold = Math.max(0.1, Math.min(1.0, Double.parseDouble(e.getValue()))); } catch (Exception ignored) {}
        try { if (intervalInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.attackInterval = Math.max(0, Math.min(10, Integer.parseInt(e.getValue()))); } catch (Exception ignored) {}
        try { if (rangeInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.maxRange = Math.max(1.0, Math.min(8.0, Double.parseDouble(e.getValue()))); } catch (Exception ignored) {}
        try { if (maxApsInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.maxAttacksPerSecond = Math.max(1, Math.min(20, Integer.parseInt(e.getValue()))); } catch (Exception ignored) {}
        if (entitiesInput instanceof EditBox e) cfg.entities = e.getValue();
        if (playerListInput instanceof EditBox e) cfg.playerList = e.getValue();
        AutoAttackConfig.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§6自动攻击 配置", cx + 8, cy + 20, 0xFFFFFF);

        var cfg = AutoAttackConfig.getInstance();
        String desc = switch (activeTab) {
            case 0 -> "模式=" + cfg.attackMode + "  阈值=" + cfg.cooldownThreshold + "  间隔=" + cfg.attackInterval;
            case 1 -> "名单=" + cfg.listMode + "  类型数=" + cfg.getEntityTypeSet().size();
            default -> "距离=" + cfg.maxRange + "  上限=" + cfg.maxAttacksPerSecond + "/s";
        };
        g.drawString(font, "§7" + desc, cx + 8, cy + H - 12, 0x666666);

        for (var w : new AbstractWidget[]{cooldownInput, intervalInput, rangeInput, maxApsInput, entitiesInput, playerListInput}) {
            if (w instanceof EditBox e) e.render(g, mx, my, pt);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (var w : new AbstractWidget[]{cooldownInput, intervalInput, rangeInput, maxApsInput, entitiesInput, playerListInput}) {
            if (w instanceof EditBox e) e.mouseClicked(mx, my, button);
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        for (var w : new AbstractWidget[]{cooldownInput, intervalInput, rangeInput, maxApsInput, entitiesInput, playerListInput}) {
            if (w instanceof EditBox e && e.isFocused()) return e.keyPressed(k, s, m);
        }
        if (k == 256) { saveInputs(); this.minecraft.setScreen(null); return true; }
        return super.keyPressed(k, s, m);
    }

    @Override public void onClose() { saveInputs(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
