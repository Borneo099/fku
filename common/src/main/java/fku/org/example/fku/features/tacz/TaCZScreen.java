package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TaCZ 枪械辅助 — 配置界面
 * 移植自 Lexis 的 TaCZ 系列 Hack
 * 该功能由赛博教员实现
 */
public class TaCZScreen extends Screen {

    private static final int W = 340, H = 388;
    private int bx, by;
    private EditBox customEntitiesBox;

    public TaCZScreen() {
        super(Component.literal("TaCZ 配置"));
    }

    @Override
    protected void init() {
        super.init();
        bx = (width - W) / 2;
        by = (height - H) / 2;
        TaCZConfig cfg = TaCZConfig.getInstance();
        int cx = bx + 10, cy = by + 40, sp = 19;

        // 第一排
        addToggle(cx, cy, "子弹自瞄", () -> cfg.aimbotEnabled, v -> { cfg.aimbotEnabled = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "自动换弹", () -> cfg.autoReloadEnabled, v -> { cfg.autoReloadEnabled = v; TaCZConfig.save(); });
        cy += sp;
        addToggle(cx, cy, "子弹透视", () -> cfg.bulletTracersEnabled, v -> { cfg.bulletTracersEnabled = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "无尽自瞄", () -> cfg.endlessAimbotEnabled, v -> { cfg.endlessAimbotEnabled = v; TaCZConfig.save(); });
        cy += sp;
        addToggle(cx, cy, "瞬镜", () -> cfg.instantAimEnabled, v -> { cfg.instantAimEnabled = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "无后座", () -> cfg.noRecoilEnabled, v -> { cfg.noRecoilEnabled = v; TaCZConfig.save(); });
        cy += sp;
        // ★ 新增：无扩散/防抖（参考 NoSpread 02 实现）
        addToggle(cx, cy, "无扩散", () -> cfg.noSpreadEnabled, v -> { cfg.noSpreadEnabled = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "防抖", () -> cfg.antiShakeEnabled, v -> { cfg.antiShakeEnabled = v; TaCZConfig.save(); });
        cy += sp;
        addToggle(cx, cy, "疾跑不断", () -> cfg.noSprintInterruptEnabled, v -> { cfg.noSprintInterruptEnabled = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "全狙自动", () -> cfg.sniperFullAutoEnabled, v -> { cfg.sniperFullAutoEnabled = v; TaCZConfig.save(); });
        cy += sp;
        addToggle(cx, cy, "全枪自动", () -> cfg.fullAutoEnabled, v -> { cfg.fullAutoEnabled = v; TaCZConfig.save(); });
        cy += sp + 2;

        // 参数行
        addLabel(cx, cy, "§7自瞄范围: §b" + cfg.aimbotCircleSize);
        addCycleButton(cx + 100, cy, 60, new int[]{50, 80, 100, 150, 200, 300, 500}, cfg.aimbotCircleSize,
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
        addRenderableWidget(customEntitiesBox);
        cy += sp;

        addLabel(cx, cy, "§7" + ("自定义".equals(cfg.aimbotTargetMode) ? "当前生效实体id: §b" + cfg.aimbotCustomEntities : "（选择自定义后填写实体id）"));
        cy += sp;

        addToggle(cx, cy, "开镜锁定", () -> cfg.aimbotOnlyWhenAiming, v -> { cfg.aimbotOnlyWhenAiming = v; TaCZConfig.save(); });
        addToggle(cx + 165, cy, "穿墙锁定", () -> cfg.aimbotAllowThroughWalls, v -> { cfg.aimbotAllowThroughWalls = v; TaCZConfig.save(); });
        cy += sp;

        addToggle(cx, cy, "无尽仅左键", () -> cfg.endlessOnlyOnLeftClick, v -> { cfg.endlessOnlyOnLeftClick = v; TaCZConfig.save(); });
        cy += sp;

        // 返回按钮
        addRenderableWidget(Button.builder(Component.literal("§a← 返回"),
            btn -> minecraft.setScreen(new ClickGuiScreen()))
            .bounds(bx + W / 2 - 30, by + H - 28, 60, 18).build());
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

    private void addLabel(int x, int y, String text) {
        addRenderableOnly((g, mx, my, pt) -> g.drawString(font, text, x, y + 4, 0xCCCCCC));
    }

    private void addToggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        addRenderableWidget(new Button(x, y, 155, 16, Component.literal(""),
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
        addRenderableWidget(btn);
        return btn;
    }

    private Button addCycleButton(int x, int y, int w, float[] values, float current, Consumer<Float> setter) {
        int[] idx = {0};
        for (int i = 0; i < values.length; i++) { if (values[i] == current) { idx[0] = i; break; } }
        Button btn = Button.builder(Component.literal("§b" + String.format("%.1f", current)),
            b -> { idx[0] = (idx[0] + 1) % values.length; setter.accept(values[idx[0]]); b.setMessage(Component.literal("§b" + String.format("%.1f", values[idx[0]]))); })
            .bounds(x, y, w, 16).build();
        addRenderableWidget(btn);
        return btn;
    }

    private Button addCycleButton(int x, int y, int w, String[] values, String current, Consumer<String> setter) {
        int[] idx = {0};
        for (int i = 0; i < values.length; i++) { if (values[i].equals(current)) { idx[0] = i; break; } }
        Button btn = Button.builder(Component.literal("§b" + current),
            b -> { idx[0] = (idx[0] + 1) % values.length; setter.accept(values[idx[0]]); b.setMessage(Component.literal("§b" + values[idx[0]])); })
            .bounds(x, y, w, 16).build();
        addRenderableWidget(btn);
        return btn;
    }
}