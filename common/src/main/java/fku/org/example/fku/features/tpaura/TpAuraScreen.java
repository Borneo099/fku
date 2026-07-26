package fku.org.example.fku.features.tpaura;

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
 * TpAura 配置 — 分 Tab 排版（参考 ArrowDmg 32k弓风格）
 */
public class TpAuraScreen extends Screen {

    private static final int W = 300, H = 260;
    private int activeTab = 0;

    // 各输入框
    private AbstractWidget cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput;
    private AbstractWidget packetsInput, ceilingStepInput, entityTypesInput, whitelistInput;
    private AbstractWidget totemAttacksInput, totemHeightInput;
    private AbstractWidget autoFlightSpeedInput, autoFlightHoriInput;

    public TpAuraScreen() {
        super(Component.literal("TpAura 配置"));
    }

    @Override
    protected void init() {
        clearWidgets();
        cooldownInput = delayInput = rangeInput = attackDistInput = tpOffsetInput = null;
        packetsInput = ceilingStepInput = entityTypesInput = whitelistInput = null;
        totemAttacksInput = totemHeightInput = null;
        autoFlightSpeedInput = null; autoFlightHoriInput = null;

        var cfg = TpAuraConfig.getInstance();
        int cx = (width - W) / 2, cy = (height - H) / 2;

        // ── Tab 头 ──
        String[][] tabs = {{"攻击","瞬移","目标","白名单","其他"}};
        int tx = cx + 2;
        for (int i = 0; i < 5; i++) {
            final int fi = i;
            String name = tabs[0][i];
            int tw = font.width(name) + 14;
            addRenderableWidget(Button.builder(
                Component.literal(i == activeTab ? "§e["+name+"]§r" : name),
                b -> { saveInputs(); activeTab = fi; init(); }
            ).bounds(tx, cy + 2, Math.max(tw, 44), 16).build());
            tx += Math.max(tw, 44) + 2;
        }

        int ly = cy + 24, sp = 19;

        switch (activeTab) {
            case 0 -> { // 攻击
                addLabel(cx+2, ly, "蓄力阈值(0.1~1.0):");
                cooldownInput = mkEdit(cx+140, ly, 40, String.valueOf(cfg.cooldownThreshold));
                addToggle(cx+195, ly, "Smart", () -> cfg.attackMode.equals("Smart"), v -> cfg.setAttackMode(v ? "Smart" : "Fast"));
                ly += sp;

                addLabel(cx+2, ly, "额外延迟(0~20tick):");
                delayInput = mkEdit(cx+140, ly, 40, String.valueOf(cfg.attackDelay));
                ly += sp;

                addToggle(cx+2, ly, "自动切武", () -> cfg.autoSwitch, v -> cfg.setAutoSwitch(v));
                addToggle(cx+110, ly, "需要重锤", () -> cfg.requireMace, v -> cfg.setRequireMace(v));
                addToggle(cx+210, ly, "挥动手", () -> cfg.swingHand, v -> cfg.setSwingHand(v));
                ly += sp;

                addToggle(cx+2, ly, "静默切回", () -> cfg.silentSwap, v -> cfg.setSilentSwap(v));
            }
            case 1 -> { // 瞬移
                addToggle(cx+2, ly, "Vanilla", () -> cfg.mode.equals("Vanilla"), v -> cfg.setMode(v ? "Vanilla" : "Paper"));
                addToggle(cx+100, ly, "Paper", () -> cfg.mode.equals("Paper"), v -> cfg.setMode(v ? "Paper" : "Vanilla"));
                ly += sp;

                addLabel(cx+2, ly, "最大范围(1~99):");
                rangeInput = mkEdit(cx+120, ly, 40, String.valueOf((int)cfg.maxRange));
                ly += sp;

                addToggle(cx+2, ly, "V-Clip上升", () -> cfg.goUp, v -> cfg.setGoUp(v));
                addToggle(cx+110, ly, "攻击后回传", () -> cfg.returnPos, v -> cfg.setReturnPos(v));
                addToggle(cx+220, ly, "偏移同步", () -> cfg.offsetFix, v -> cfg.setOffsetFix(v));
                ly += sp;

                addToggle(cx+2, ly, "§b防摔", () -> cfg.antiFall, v -> cfg.setAntiFall(v));
                addToggle(cx+110, ly, "限制天花板", () -> cfg.limitCeiling, v -> cfg.setLimitCeiling(v));
                ly += sp;

                addLabel(cx+2, ly, "垫包数量:");
                packetsInput = mkEdit(cx+80, ly, 30, String.valueOf(cfg.paperPackets));
                ly += sp;

                addLabel(cx+2, ly, "天花板步长:");
                ceilingStepInput = mkEdit(cx+80, ly, 30, String.valueOf(cfg.ceilingScanStep));
                addToggle(cx+125, ly, "§a自动飞行", () -> cfg.autoFlight, v -> cfg.setAutoFlight(v));
                ly += sp;

                addLabel(cx+2, ly, "上升速度:");
                autoFlightSpeedInput = mkEdit(cx+80, ly, 30, String.valueOf(cfg.autoFlightSpeed));
                addLabel(cx+115, ly, "§7(0~2.0)");
                ly += sp;
                addLabel(cx+2, ly, "水平倍率:");
                autoFlightHoriInput = mkEdit(cx+80, ly, 30, String.valueOf(cfg.autoFlightHorizontalSpeed));
                addLabel(cx+115, ly, "§7(0~3.0)");
                ly += sp;
            }
            case 2 -> { // 目标
                addToggle(cx+2, ly, "全生物攻击", () -> cfg.attackAllEntities, v -> cfg.setAttackAllEntities(v));
                addToggle(cx+130, ly, "忽略已命名", () -> cfg.ignoreNamed, v -> cfg.setIgnoreNamed(v));
                ly += sp;

                addToggle(cx+2, ly, "忽略朋友", () -> cfg.ignoreFriends, v -> cfg.setIgnoreFriends(v));
                addToggle(cx+110, ly, "忽略已驯服", () -> cfg.ignoreTamed, v -> cfg.setIgnoreTamed(v));
                ly += sp;

                addLabel(cx+2, ly, "攻击距离(3~6):");
                attackDistInput = mkEdit(cx+115, ly, 30, String.valueOf(cfg.attackDistance));
                addLabel(cx+155, ly, "TP偏移(0~6):");
                tpOffsetInput = mkEdit(cx+225, ly, 30, String.valueOf(cfg.tpOffset));
                ly += sp;

                addLabel(cx+2, ly, "实体类型(逗号分隔):");
                entityTypesInput = mkTextEdit(cx+155, ly, 130, cfg.entityTypes);
            }
            case 3 -> { // 白名单
                addToggle(cx+2, ly, "启用白名单", () -> cfg.whitelistEnabled, v -> cfg.setWhitelistEnabled(v));
                ly += sp;
                addLabel(cx+2, ly, "白名单玩家(A,B):");
                whitelistInput = mkTextEdit(cx+130, ly, 150, cfg.whitelist);
            }
            case 4 -> { // 其他
                addToggle(cx+2, ly, "显示路径", () -> cfg.renderPath, v -> cfg.setRenderPath(v));
                addToggle(cx+110, ly, "图腾绕过", () -> cfg.totemBypass, v -> cfg.setTotemBypass(v));
                ly += sp;

                addLabel(cx+2, ly, "图腾攻击次数:");
                totemAttacksInput = mkEdit(cx+100, ly, 30, String.valueOf(cfg.totemAttacks));
                addLabel(cx+145, ly, "高度增加:");
                totemHeightInput = mkEdit(cx+215, ly, 30, String.valueOf(cfg.totemHeightIncrease));
                ly += sp;

                addLabel(cx+2, ly, "热键(中键点击组件绑定):");
                String hk = cfg.hotkeyKey >= 0 ? cfg.hotkeyName : "未绑定";
                addRenderableWidget(Button.builder(Component.literal("§7" + hk), b -> {}).bounds(cx+155, ly, 130, 14).build());
            }
        }

        // 保存并返回
        addRenderableWidget(Button.builder(Component.literal("§a保存并返回"),
            b -> { saveInputs(); this.minecraft.setScreen(null); })
            .bounds(cx + W/2 - 40, cy + H - 22, 80, 16).build());
    }

    // ──────── 组件工厂 ────────

    private void addLabel(int x, int y, String text) {
        addRenderableWidget(Button.builder(Component.literal("§7" + text), b -> {}).bounds(x, y, font.width(text) + 4, 14).build());
    }

    private void addToggle(int x, int y, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
        boolean cur = getter.getAsBoolean();
        addRenderableWidget(Button.builder(
                Component.literal(label + (cur ? " §a开" : " §7关")),
                b -> {
                    boolean now = !getter.getAsBoolean();
                    setter.accept(now);
                    b.setMessage(Component.literal(label + (now ? " §a开" : " §7关")));
                }
        ).bounds(x, y, 90, 14).build());
    }

    /** 数字输入框（仅允许数字和小数点） */
    private AbstractWidget mkEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(8); b.setFilter(s -> s.matches("[\\d.]*"));
        addWidget(b); return b;
    }

    /** 文本输入框（用于实体类型/白名单等） */
    private AbstractWidget mkTextEdit(int x, int y, int w, String val) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(500); b.setFilter(s -> true); // ✅ 不能传 null，1.20.1 EditBox 会 NPE
        addWidget(b); return b;
    }

    private void saveInputs() {
        var cfg = TpAuraConfig.getInstance();
        try { if (cooldownInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setCooldownThreshold(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (delayInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAttackDelay(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (rangeInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setMaxRange(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (attackDistInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAttackDistance(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (tpOffsetInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTpOffset(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (packetsInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setPaperPackets(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (ceilingStepInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setCeilingScanStep(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        if (entityTypesInput instanceof EditBox e) cfg.setEntityTypes(e.getValue());
        if (whitelistInput instanceof EditBox e) cfg.setWhitelist(e.getValue());
        try { if (totemAttacksInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTotemAttacks(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (autoFlightSpeedInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAutoFlightSpeed(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (autoFlightHoriInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAutoFlightHorizontalSpeed(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (totemHeightInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTotemHeightIncrease(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        cfg.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§6TpAura 配置", cx + 8, cy + 20, 0xFFFFFF);

        var cfg = TpAuraConfig.getInstance();
        String modeDesc = switch (activeTab) {
            case 0 -> "攻击模式: " + cfg.attackMode + "  蓄力=" + cfg.cooldownThreshold;
            case 1 -> "模式: " + cfg.mode + "  范围=" + (int)cfg.maxRange + "  " + (cfg.autoFlight ? "§a飞行" : "§7无飞行");
            case 2 -> "全生物=" + cfg.attackAllEntities + "  距离=" + cfg.attackDistance;
            case 3 -> "白名单=" + cfg.whitelistEnabled;
            default -> "图腾=" + cfg.totemBypass + "  路径=" + cfg.renderPath;
        };
        g.drawString(font, "§7" + modeDesc, cx + 8, cy + H - 12, 0x666666);

        // 手动渲染 EditBox
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                packetsInput, ceilingStepInput, entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput}) {
            if (w instanceof EditBox e) e.render(g, mx, my, pt);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                packetsInput, ceilingStepInput, entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput}) {
            if (w instanceof EditBox e) e.mouseClicked(mx, my, button);
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                packetsInput, ceilingStepInput, entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput}) {
            if (w instanceof EditBox e && e.isFocused()) return e.keyPressed(k, s, m);
        }
        if (k == 256) { saveInputs(); this.minecraft.setScreen(null); return true; }
        return super.keyPressed(k, s, m);
    }

    @Override public void onClose() { saveInputs(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}
