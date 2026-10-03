package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TaCZ 枪械辅助 — 配置界面（分页 Tab：通用功能 / 自瞄·扳机 / 幽灵窥视）
 * 移植自 Lexis 的 TaCZ 系列 Hack
 * 该功能由赛博教员实现
 */
public class TaCZScreen extends Screen {

    private static final int W = 340, H = 340;
    private static final String[] TAB_NAMES = {"通用功能", "自瞄·扳机", "幽灵窥视"};
    private int bx, by;
    private EditBox customEntitiesBox, triggerDelayBox;
    private Button ghostPeekKeyButton;
    private boolean waitingForGhostPeekKey = false;

    private int currentTab = 0;
    private final List<AbstractWidget> allTabWidgets = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();

    public TaCZScreen() {
        super(Component.literal("TaCZ 配置"));
    }

    @Override
    protected void init() {
        super.init();
        tabButtons.clear();
        allTabWidgets.clear();

        bx = (width - W) / 2;
        by = (height - H) / 2;

        // ── 顶部 Tab 按钮（常驻可见） ──
        int tbW = 104, tbH = 18, gap = 6;
        int totalW = tbW * TAB_NAMES.length + gap * (TAB_NAMES.length - 1);
        int startX = bx + (W - totalW) / 2;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            Button b = Button.builder(Component.literal(TAB_NAMES[i]),
                    btn -> { currentTab = idx; setFocused(null); buildTab(idx); updateTabButtons(); })
                    .bounds(startX + i * (tbW + gap), by + 36, tbW, tbH).build();
            addRenderableWidget(b);
            tabButtons.add(b);
        }
        updateTabButtons();

        // 返回按钮（常驻可见）
        addRenderableWidget(Button.builder(Component.literal("§a← 返回"),
            btn -> minecraft.setScreen(new ClickGuiScreen()))
            .bounds(bx + W / 2 - 30, by + H - 26, 60, 18).build());

        // 初始分页内容
        buildTab(currentTab);
    }

    /** 构建指定 Tab 的内容（先移除旧控件，再重建） */
    private void buildTab(int tab) {
        for (AbstractWidget w : allTabWidgets) this.removeWidget(w);
        allTabWidgets.clear();

        TaCZConfig cfg = TaCZConfig.getInstance();
        int cx = bx + 10, cy = by + 62, sp = 18;

        if (tab == 0) {
            // ── 通用功能 ──
            addToggle(cx, cy, "子弹自瞄", () -> cfg.aimbotEnabled, v -> { cfg.aimbotEnabled = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "自动换弹", () -> cfg.autoReloadEnabled, v -> { cfg.autoReloadEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "子弹透视", () -> cfg.bulletTracersEnabled, v -> { cfg.bulletTracersEnabled = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "无尽自瞄", () -> cfg.endlessAimbotEnabled, v -> { cfg.endlessAimbotEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "瞬镜", () -> cfg.instantAimEnabled, v -> { cfg.instantAimEnabled = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "无后座", () -> cfg.noRecoilEnabled, v -> { cfg.noRecoilEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "无扩散", () -> cfg.noSpreadEnabled, v -> { cfg.noSpreadEnabled = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "防抖", () -> cfg.antiShakeEnabled, v -> { cfg.antiShakeEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "疾跑不断", () -> cfg.noSprintInterruptEnabled, v -> { cfg.noSprintInterruptEnabled = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "全狙自动", () -> cfg.sniperFullAutoEnabled, v -> { cfg.sniperFullAutoEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "全枪自动", () -> cfg.fullAutoEnabled, v -> { cfg.fullAutoEnabled = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "无尽仅左键", () -> cfg.endlessOnlyOnLeftClick, v -> { cfg.endlessOnlyOnLeftClick = v; TaCZConfig.save(); });
            cy += sp;
            // ★ 不打队友：队伍名颜色与自己相同的玩家不进入自瞄目标
            addToggle(cx, cy, "不打队友", () -> cfg.dontHitTeammates, v -> { cfg.dontHitTeammates = v; TaCZConfig.save(); });
            cy += sp;

        } else if (tab == 1) {
            // ── 自瞄·扳机 ──
            // ★ 扳机：准星射线与目标碰撞箱相交即自动左键开火（不再依赖自瞄框变绿）
            addToggle(cx, cy, "扳机开火", () -> cfg.aimbotTriggerEnabled, v -> { cfg.aimbotTriggerEnabled = v; TaCZConfig.save(); });
            addLabel(cx + 165, cy, "§7开火前间隔ms:");
            triggerDelayBox = new EditBox(font, cx + 255, cy, 55, 16, Component.literal(""));
        triggerDelayBox.setMaxLength(6);
        triggerDelayBox.setValue(String.valueOf(cfg.aimbotTriggerDelay));
        triggerDelayBox.setFilter(s -> s.matches("\\d*"));
        triggerDelayBox.setResponder(s -> { try { cfg.aimbotTriggerDelay = Integer.parseInt(s); TaCZConfig.save(); } catch (NumberFormatException ignored) {} });
        track(triggerDelayBox);
        cy += sp + 1;

        // 参数行（自瞄圈大小：连续滑动调节）
            addDynamicLabel(cx, cy, () -> "§7自瞄范围: §b" + TaCZConfig.getInstance().aimbotCircleSize);
            addSlider(cx + 100, cy, 100, 20, 600, cfg.aimbotCircleSize,
                v -> { cfg.aimbotCircleSize = v; TaCZConfig.save(); });
            cy += sp;
            addLabel(cx, cy, "§7旋转速度: §b" + String.format("%.1f", cfg.aimbotRotationSpeed));
            addCycleButton(cx + 100, cy, 60, new float[]{5, 10, 20, 30, 60, 90, 180}, cfg.aimbotRotationSpeed,
                v -> { cfg.aimbotRotationSpeed = v; TaCZConfig.save(); });
            cy += sp;
            addLabel(cx, cy, "§7锁定部位: §b" + cfg.aimbotBodyPart);
            addCycleButton(cx + 100, cy, 80, new String[]{"头", "身体", "腿", "脚", "自动"}, cfg.aimbotBodyPart,
                v -> { cfg.aimbotBodyPart = v; TaCZConfig.save(); });
            cy += sp;

            // ★ 自瞄对象选择器：全部实体 / 仅玩家 / 自定义
            addLabel(cx, cy, "§7自瞄对象: §b" + cfg.aimbotTargetMode);
            addCycleButton(cx + 100, cy, 70, new String[]{"全部实体", "仅玩家", "自定义"}, cfg.aimbotTargetMode,
                v -> {
                    cfg.aimbotTargetMode = v;
                    TaCZConfig.save();
                    if (customEntitiesBox != null) customEntitiesBox.setVisible("自定义".equals(v));
                });
            cy += sp;

            // 自定义模式下的实体 id 输入框（逗号分隔，如 minecraft:zombie,tacz:xxx）
            customEntitiesBox = new EditBox(font, cx, cy, 320, 16, Component.literal("实体id，逗号分隔"));
            customEntitiesBox.setMaxLength(2000);
            customEntitiesBox.setValue(cfg.aimbotCustomEntities == null ? "" : cfg.aimbotCustomEntities);
            customEntitiesBox.setVisible("自定义".equals(cfg.aimbotTargetMode));
            customEntitiesBox.setResponder(s -> { cfg.aimbotCustomEntities = s; TaCZConfig.save(); });
            track(customEntitiesBox);
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7" + ("自定义".equals(cfg.aimbotTargetMode) ? "当前生效实体id: §b" + cfg.aimbotCustomEntities : "（选择自定义后填写实体id）"));
            cy += sp;
            addToggle(cx, cy, "开镜锁定", () -> cfg.aimbotOnlyWhenAiming, v -> { cfg.aimbotOnlyWhenAiming = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "穿墙锁定", () -> cfg.aimbotAllowThroughWalls, v -> { cfg.aimbotAllowThroughWalls = v; TaCZConfig.save(); });
            cy += sp;

        } else {
            // ── 幽灵窥视 ──
            addDynamicLabel(cx, cy, () -> "§e┌─ Ghost Peek 幽灵窥视");
            cy += sp;
            addToggle(cx, cy, "幽灵窥视", () -> cfg.ghostPeekEnabled, v -> { cfg.ghostPeekEnabled = v; TaCZConfig.save(); });
            ghostPeekKeyButton = Button.builder(Component.literal("窥视按键: " + keyName(cfg.ghostPeekKey)),
                btn -> { waitingForGhostPeekKey = true; ghostPeekKeyButton.setMessage(Component.literal("§e按下要绑定的键/鼠标")); })
                .bounds(cx + 165, cy, 155, 16).build();
            track(ghostPeekKeyButton);
            cy += sp;

            addDynamicLabel(cx, cy, () -> "§7窥视距离: §b" + String.format("%.1f", cfg.ghostPeekBlocks) + " 格");
            addSlider(cx + 100, cy, 100, 1, 10, (int) Math.round(cfg.ghostPeekBlocks),
                v -> { cfg.ghostPeekBlocks = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7上窥视距离: §b" + String.format("%.1f", cfg.ghostPeekUpBlocks) + " 格");
            addSlider(cx + 100, cy, 100, 1, 5, (int) Math.round(cfg.ghostPeekUpBlocks),
                v -> { cfg.ghostPeekUpBlocks = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7下窥视距离: §b" + String.format("%.1f", cfg.ghostPeekDownBlocks) + " 格");
            addSlider(cx + 100, cy, 100, 1, 6, (int) Math.round(cfg.ghostPeekDownBlocks),
                v -> { cfg.ghostPeekDownBlocks = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7提前量: §b" + String.format("%.1f", cfg.ghostPeekBacktrack) + " 刻");
            addSlider(cx + 100, cy, 100, 0, 10, (int) Math.round(cfg.ghostPeekBacktrack),
                v -> { cfg.ghostPeekBacktrack = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7索敌锥角: §b" + (cfg.ghostPeekFov >= 360 ? "不限" : String.format("%.0f", cfg.ghostPeekFov) + "°"));
            addSlider(cx + 100, cy, 100, 30, 360, (int) Math.round(cfg.ghostPeekFov),
                v -> { cfg.ghostPeekFov = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7窥视频率: §b" + cfg.ghostPeekFrequency + " 刻");
            addSlider(cx + 100, cy, 100, 1, 30, cfg.ghostPeekFrequency,
                v -> { cfg.ghostPeekFrequency = v; TaCZConfig.save(); });
            cy += sp;
            addDynamicLabel(cx, cy, () -> "§7触发延迟: §b" + cfg.ghostPeekTriggerDelay + " ms");
            addSlider(cx + 100, cy, 100, 0, 1000, cfg.ghostPeekTriggerDelay,
                v -> { cfg.ghostPeekTriggerDelay = v; TaCZConfig.save(); });
            cy += sp;
            addToggle(cx, cy, "仅玩家", () -> cfg.ghostPeekPlayersOnly, v -> { cfg.ghostPeekPlayersOnly = v; TaCZConfig.save(); });
            addToggle(cx + 165, cy, "原地原点优先", () -> cfg.ghostPeekOriginEnabled, v -> { cfg.ghostPeekOriginEnabled = v; TaCZConfig.save(); });
            cy += sp;
            Button aimPartBtn = Button.builder(Component.literal("窥视瞄点: " + cfg.ghostPeekAimPart),
                btn -> { cfg.ghostPeekAimPart = "身体".equals(cfg.ghostPeekAimPart) ? "头部" : "身体"; TaCZConfig.save(); btn.setMessage(Component.literal("窥视瞄点: " + cfg.ghostPeekAimPart)); })
                .bounds(cx, cy, 155, 16).build();
            track(aimPartBtn);
            cy += sp;
            addToggle(cx, cy, "穿墙瞄准", () -> cfg.ghostPeekWallBang, v -> { cfg.ghostPeekWallBang = v; TaCZConfig.save(); });
            cy += sp;
        }
    }

    /** 把控件登记到屏幕并记入当前分页集合（切换分页时统一移除） */
    private void track(AbstractWidget w) {
        addRenderableWidget(w);
        allTabWidgets.add(w);
    }

    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            boolean active = i == currentTab;
            tabButtons.get(i).setMessage(Component.literal(active ? "§l§a" + TAB_NAMES[i] : "§7" + TAB_NAMES[i]));
        }
    }

    /** 按键码转可读名称 */
    private static String keyName(int code) {
        if (code < 0) return "未绑定";
        if (code >= 1000) return "鼠标" + (code - 1000);
        String name = GLFW.glfwGetKeyName(code, 0);
        return name != null ? name.toUpperCase() : ("键" + code);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForGhostPeekKey) {
            TaCZConfig cfg = TaCZConfig.getInstance();
            cfg.ghostPeekKey = keyCode;
            TaCZConfig.save();
            if (ghostPeekKeyButton != null) ghostPeekKeyButton.setMessage(Component.literal("窥视按键: " + keyName(cfg.ghostPeekKey)));
            waitingForGhostPeekKey = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (waitingForGhostPeekKey) {
            TaCZConfig cfg = TaCZConfig.getInstance();
            cfg.ghostPeekKey = 1000 + button;
            TaCZConfig.save();
            if (ghostPeekKeyButton != null) ghostPeekKeyButton.setMessage(Component.literal("窥视按键: " + keyName(cfg.ghostPeekKey)));
            waitingForGhostPeekKey = false;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawRoundedRect(g, bx - 10, by - 8, W + 20, H + 16, 0xAA2D2D2D, 8);
        // 标题
        g.drawString(font, "§lTaCZ 枪械辅助 配置", bx + 10, by + 10, 0xFFFFFF);
        // 说明文字
        g.drawString(font, "§7左键开关主开关，右键打开配置，中键绑定热键", bx + 10, by + 24, 0xCCCCCC);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        minecraft.setScreen(new ClickGuiScreen());
    }

    // ── 辅助方法 ──

    /** 纯文本标签（懒渲染）：不响应鼠标，仅用于显示 */
    private static class TextWidget extends AbstractWidget {
        private final Supplier<String> text;
        private final int color;
        TextWidget(int x, int y, Supplier<String> text, int color) {
            super(x, y, 320, 12, Component.literal(""));
            this.text = text;
            this.color = color;
            this.active = false;
        }
        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            g.drawString(Minecraft.getInstance().font, text.get(), getX(), getY() + 4, color);
        }
        @Override
        public boolean isMouseOver(double x, double y) { return false; }
        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput pNarration) {}
    }

    private void addLabel(int x, int y, String text) {
        track(new TextWidget(x, y, () -> text, 0xCCCCCC));
    }

    private void addDynamicLabel(int x, int y, Supplier<String> text) {
        track(new TextWidget(x, y, text, 0xCCCCCC));
    }

    private void addToggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        track(new Button(x, y, 155, 16, Component.literal(""),
            btn -> { setter.accept(!getter.get()); },
            btn -> Component.literal("")) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
                boolean val = getter.get();
                int color = val ? 0xFF4CAF50 : 0xFF666666;
                GuiRenderHelper.drawRoundedRect(g, getX(), getY(), width, height, color, 3);
                String txt = label + ": " + (val ? "开" : "关");
                g.drawString(Minecraft.getInstance().font, txt, getX() + 4, getY() + 4, 0xFFFFFF);
            }
        });
    }

    private Button addCycleButton(int x, int y, int w, int[] values, int current, Consumer<Integer> setter) {
        int[] idx = {0};
        for (int i = 0; i < values.length; i++) { if (values[i] == current) { idx[0] = i; break; } }
        Button btn = Button.builder(Component.literal("§b" + current),
            b -> { idx[0] = (idx[0] + 1) % values.length; setter.accept(values[idx[0]]); b.setMessage(Component.literal("§b" + values[idx[0]])); })
            .bounds(x, y, w, 16).build();
        track(btn);
        return btn;
    }

    private Button addCycleButton(int x, int y, int w, float[] values, float current, Consumer<Float> setter) {
        int[] idx = {0};
        for (int i = 0; i < values.length; i++) { if (values[i] == current) { idx[0] = i; break; } }
        Button btn = Button.builder(Component.literal("§b" + String.format("%.1f", current)),
            b -> { idx[0] = (idx[0] + 1) % values.length; setter.accept(values[idx[0]]); b.setMessage(Component.literal("§b" + String.format("%.1f", values[idx[0]]))); })
            .bounds(x, y, w, 16).build();
        track(btn);
        return btn;
    }

    private Button addCycleButton(int x, int y, int w, String[] values, String current, Consumer<String> setter) {
        int[] idx = {0};
        for (int i = 0; i < values.length; i++) { if (values[i].equals(current)) { idx[0] = i; break; } }
        Button btn = Button.builder(Component.literal("§b" + current),
            b -> { idx[0] = (idx[0] + 1) % values.length; setter.accept(values[idx[0]]); b.setMessage(Component.literal("§b" + values[idx[0]])); })
            .bounds(x, y, w, 16).build();
        track(btn);
        return btn;
    }

    /** 连续滑动条：拖动实时写入配置（min~max 整数值） */
    private void addSlider(int x, int y, int w, int min, int max, int current, Consumer<Integer> setter) {
        double v = (double) (current - min) / (max - min);
        v = Math.max(0, Math.min(1, v));
        AbstractSliderButton slider = new AbstractSliderButton(x, y, w, 16, Component.literal(""), v) {
            @Override
            protected void updateMessage() {
                int val = min + (int) Math.round(value * (max - min));
                setMessage(Component.literal("§b" + val));
            }
            @Override
            protected void applyValue() {
                int val = min + (int) Math.round(value * (max - min));
                setter.accept(val);
            }
        };
        track(slider);
    }
}
