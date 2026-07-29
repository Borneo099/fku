package fku.org.example.fku.features.standattack; /* water */

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
 * 替身攻击配置界面 — 加大版5Tab布局，防重叠
 *
 * 全面重排瞬移Tab，使用更紧凑的布局（双列/多行）
 * 该配置界面由赛博教员实现
 */
public class StandAttackScreen extends Screen {

    private static final int W = 380, H = 300;
    private int activeTab = 0;

    // 各输入框
    private AbstractWidget cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput;
    private AbstractWidget maxStepInput, packetIntervalInput;
    private AbstractWidget entityTypesInput, whitelistInput;
    private AbstractWidget totemAttacksInput, totemHeightInput;
    private AbstractWidget autoFlightSpeedInput, autoFlightHoriInput;
    private AbstractWidget packetsInput, ceilingStepInput;
    private AbstractWidget teleportIntervalInput;

    public StandAttackScreen() {
        super(Component.literal("替身攻击 配置"));
    }

    @Override
    protected void init() {
        clearWidgets();
        cooldownInput = delayInput = rangeInput = attackDistInput = tpOffsetInput = null;
        maxStepInput = packetIntervalInput = null;
        entityTypesInput = whitelistInput = null;
        totemAttacksInput = totemHeightInput = null;
        autoFlightSpeedInput = null; autoFlightHoriInput = null;
        packetsInput = null; ceilingStepInput = null;
        teleportIntervalInput = null;

        var cfg = StandAttackConfig.getInstance();
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

        int ly = cy + 24, sp = 16;

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
            case 1 -> { // 瞬移 — 重新排版，分组清晰
                // 第1行：寻路算法（互斥三选一，点击直接设值+刷新）
                // 使用直接按钮而非 addToggle，避免开关逻辑导致的互斥问题
                addRenderableWidget(Button.builder(
                    Component.literal("§7[直线传送] " + (cfg.pathfindingMode == 0 ? "§a✓" : "")),
                    b -> { cfg.setPathfindingMode(0); init(); }
                ).bounds(cx+2, ly, 95, 14).build());
                addRenderableWidget(Button.builder(
                    Component.literal("§7[Paper上升] " + (cfg.pathfindingMode == 1 ? "§a✓" : "")),
                    b -> { cfg.setPathfindingMode(1); init(); }
                ).bounds(cx+102, ly, 100, 14).build());
                addRenderableWidget(Button.builder(
                    Component.literal("§7[A星寻路] " + (cfg.pathfindingMode == 2 ? "§a✓" : "")),
                    b -> { cfg.setPathfindingMode(2); init(); }
                ).bounds(cx+207, ly, 95, 14).build());
                ly += sp;

                // 第2行：最大步长 + 发包间隔
                addLabel(cx+2, ly, "最大步长:");
                maxStepInput = mkEdit(cx+70, ly, 35, String.valueOf(cfg.maxStepLength));
                addLabel(cx+115, ly, "§7(1~64)");
                addLabel(cx+175, ly, "发包间隔:");
                packetIntervalInput = mkEdit(cx+240, ly, 35, String.valueOf(cfg.packetInterval));
                addLabel(cx+280, ly, "§7ms");
                ly += sp;

                // 第3行：范围 + 垫包 + 天花板步长
                addLabel(cx+2, ly, "范围:");
                rangeInput = mkEdit(cx+40, ly, 30, String.valueOf((int)cfg.maxRange));
                addLabel(cx+80, ly, "垫包:");
                packetsInput = mkEdit(cx+110, ly, 25, String.valueOf(cfg.paperPackets));
                addLabel(cx+145, ly, "天花板步长:");
                ceilingStepInput = mkEdit(cx+215, ly, 25, String.valueOf(cfg.ceilingScanStep));
                ly += sp;

                // 第4行：开关选项（V-Clip / 回传 / 偏移同步 / 防摔 / 限天花板）
                addToggle(cx+2, ly, "V-Clip", () -> cfg.goUp, v -> cfg.setGoUp(v));
                addToggle(cx+72, ly, "回传", () -> cfg.returnPos, v -> cfg.setReturnPos(v));
                addToggle(cx+142, ly, "偏移同步", () -> cfg.offsetFix, v -> cfg.setOffsetFix(v));
                addToggle(cx+212, ly, "§b防摔", () -> cfg.antiFall, v -> cfg.setAntiFall(v));
                addToggle(cx+282, ly, "限天花板", () -> cfg.limitCeiling, v -> cfg.setLimitCeiling(v));
                ly += sp;

                // 第5行：开关选项（自动飞行 / 相机锁 / 选中模式 / 死亡回传）
                addToggle(cx+2, ly, "§a自动飞行", () -> cfg.autoFlight, v -> cfg.setAutoFlight(v));
                addToggle(cx+82, ly, "§e相机锁", () -> cfg.cameraLock, v -> cfg.setCameraLock(v));
                addToggle(cx+162, ly, "§b选中模式", () -> cfg.selectMode, v -> cfg.setSelectMode(v));
                addToggle(cx+242, ly, "§c死亡回传", () -> cfg.deathReturn, v -> cfg.setDeathReturn(v));
                ly += sp;

                // 第6行：数值输入（上升速度 / 水平倍率 / 传送间隔）
                addLabel(cx+2, ly, "上升速度:");
                autoFlightSpeedInput = mkEdit(cx+70, ly, 30, String.valueOf(cfg.autoFlightSpeed));
                addLabel(cx+110, ly, "水平:");
                autoFlightHoriInput = mkEdit(cx+145, ly, 30, String.valueOf(cfg.autoFlightHorizontalSpeed));
                addLabel(cx+185, ly, "传送间隔:");
                teleportIntervalInput = mkEdit(cx+245, ly, 40, String.valueOf(cfg.teleportInterval));
                addLabel(cx+290, ly, "§7ms");
                ly += sp;

                // 第7行：提示信息
                addLabel(cx+2, ly, "§7选中模式：长按右键持续攻击，准星靠近实体自动显示红框");
            }
            case 2 -> { // 目标
                addToggle(cx+2, ly, "全生物攻击", () -> cfg.attackAllEntities, v -> cfg.setAttackAllEntities(v));
                addToggle(cx+130, ly, "忽略已命名", () -> cfg.ignoreNamed, v -> cfg.setIgnoreNamed(v));
                ly += sp;

                addToggle(cx+2, ly, "忽略朋友", () -> cfg.ignoreFriends, v -> cfg.setIgnoreFriends(v));
                addToggle(cx+110, ly, "忽略已驯服", () -> cfg.ignoreTamed, v -> cfg.setIgnoreTamed(v));
                ly += sp;

                addLabel(cx+2, ly, "攻击距离(1~10):");
                attackDistInput = mkEdit(cx+115, ly, 30, String.valueOf(cfg.attackDistance));
                addLabel(cx+155, ly, "TP偏移(0~6):");
                tpOffsetInput = mkEdit(cx+225, ly, 30, String.valueOf(cfg.tpOffset));
                ly += sp;

                addLabel(cx+2, ly, "实体类型(逗号分隔):");
                entityTypesInput = mkTextEdit(cx+155, ly, 180, cfg.entityTypes);
            }
            case 3 -> { // 白名单
                addToggle(cx+2, ly, "启用白名单", () -> cfg.whitelistEnabled, v -> cfg.setWhitelistEnabled(v));
                ly += sp;
                addLabel(cx+2, ly, "白名单玩家(A,B):");
                whitelistInput = mkTextEdit(cx+130, ly, 200, cfg.whitelist);
            }
            case 4 -> { // 其他
                addToggle(cx+2, ly, "显示路径", () -> cfg.renderPath, v -> cfg.setRenderPath(v));
                addToggle(cx+110, ly, "图腾绕过", () -> cfg.totemBypass, v -> cfg.setTotemBypass(v));
                addToggle(cx+220, ly, "显示消息", () -> cfg.showMessages, v -> cfg.setShowMessages(v));
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
        ).bounds(x, y, 72, 14).build());
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
        b.setValue(val); b.setMaxLength(500); b.setFilter(s -> true);
        addWidget(b); return b;
    }

    private void saveInputs() {
        var cfg = StandAttackConfig.getInstance();
        try { if (cooldownInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setCooldownThreshold(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (delayInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAttackDelay(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (rangeInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setMaxRange(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (attackDistInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAttackDistance(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (tpOffsetInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTpOffset(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (maxStepInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setMaxStepLength(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (packetIntervalInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setPacketInterval(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (packetsInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setPaperPackets(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (ceilingStepInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setCeilingScanStep(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        if (entityTypesInput instanceof EditBox e) cfg.setEntityTypes(e.getValue());
        if (whitelistInput instanceof EditBox e) cfg.setWhitelist(e.getValue());
        try { if (totemAttacksInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTotemAttacks(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (totemHeightInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTotemHeightIncrease(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        try { if (autoFlightSpeedInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAutoFlightSpeed(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (autoFlightHoriInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setAutoFlightHorizontalSpeed(Double.parseDouble(e.getValue())); } catch (Exception ignored) {}
        try { if (teleportIntervalInput instanceof EditBox e && !e.getValue().isEmpty()) cfg.setTeleportInterval(Integer.parseInt(e.getValue())); } catch (Exception ignored) {}
        cfg.save();
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width - W) / 2, cy = (height - H) / 2;
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l§6替身攻击 配置", cx + 8, cy + 20, 0xFFFFFF);

        var cfg = StandAttackConfig.getInstance();
        String modeDesc = switch (activeTab) {
            case 0 -> "攻击模式: " + cfg.attackMode + "  蓄力=" + cfg.cooldownThreshold;
            case 1 -> "寻路: " + StandAttackConfig.pathfindingModeName(cfg.pathfindingMode) + "  范围=" + (int)cfg.maxRange + "  " + (cfg.autoFlight ? "§a飞行" : "§7无飞行");
            case 2 -> "全生物=" + cfg.attackAllEntities + "  距离=" + cfg.attackDistance;
            case 3 -> "白名单=" + cfg.whitelistEnabled;
            default -> "图腾=" + cfg.totemBypass + "  路径=" + cfg.renderPath;
        };
        g.drawString(font, "§7" + modeDesc, cx + 8, cy + H - 12, 0x666666);

        // 手动渲染 EditBox
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                maxStepInput, packetIntervalInput, packetsInput, ceilingStepInput,
                entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput,
                teleportIntervalInput}) {
            if (w instanceof EditBox e) e.render(g, mx, my, pt);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                maxStepInput, packetIntervalInput, packetsInput, ceilingStepInput,
                entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput,
                teleportIntervalInput}) {
            if (w instanceof EditBox e) e.mouseClicked(mx, my, button);
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        for (var w : new AbstractWidget[]{cooldownInput, delayInput, rangeInput, attackDistInput, tpOffsetInput,
                maxStepInput, packetIntervalInput, packetsInput, ceilingStepInput,
                entityTypesInput, whitelistInput,
                totemAttacksInput, totemHeightInput, autoFlightSpeedInput, autoFlightHoriInput,
                teleportIntervalInput}) {
            if (w instanceof EditBox e && e.isFocused()) return e.keyPressed(k, s, m);
        }
        if (k == 256) { saveInputs(); this.minecraft.setScreen(null); return true; }
        return super.keyPressed(k, s, m);
    }

    @Override public void onClose() { saveInputs(); super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
}