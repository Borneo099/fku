package fku.org.example.fku.features.structure_locator;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 结构定位配置界面 — 结构列表在面板右侧弹出（▶），不遮挡下方按钮
 */
public class StructureLocatorScreen extends Screen {

    private static final int W = 260, H = 270;
    private static final int LIST_W = 150;
    private int cx, cy;

    private EditBox seedInput;
    private Button targetBtn, fetchSeedBtn;
    private Button locateBtn, coordBtn, nextBtn, clearBtn;
    private Button r10m, r1m, r1p, r10p;

    private final List<Button> structButtons = new ArrayList<>();
    private boolean showList = false;
    private int listScroll = 0;

    public StructureLocatorScreen() {
        super(Component.literal("结构定位"));
    }

    @Override
    protected void init() {
        super.init();
        // 右侧弹出列表不占用面板宽度，面板总是居中
        cx = (width - W) / 2;
        cy = (height - H) / 2;
        var cfg = StructureLocatorConfig.getInstance();

        // ── 种子输入 ──
        seedInput = new EditBox(font, cx + 10, cy + 52, W - 20, 16, Component.literal("种子"));
        seedInput.setValue(cfg.manualSeed);
        seedInput.setMaxLength(32);
        addRenderableWidget(seedInput);

        // ── 取按钮 ──
        fetchSeedBtn = mkBtn("§e取种子 (/seed)", cx + 10, cy + 72, W - 20, 16, () -> StructureLocatorFeature.requestSeed());

        // ── 结构选择 ──
        String cur = idxOk(cfg.targetIndex) ? StructureLocatorFeature.TARGETS.get(cfg.targetIndex).name : "?";
        targetBtn = mkBtn("§f" + cur + "  §7▶", cx + 10, cy + 108, W - 20, 16, () -> {
            showList = !showList;
            rebuildStructList();
        });

        // ── 半径 ──
        int rbY = cy + 152, rbw = 50, gap = 5;
        int rbX0 = cx + 10;
        r10m = mkBtn("-10", rbX0,             rbY, rbw, 16, () -> { cfg.searchRadius = Math.max(1, cfg.searchRadius - 10); cfg.save(); });
        r1m  = mkBtn("-1",  rbX0 + rbw + gap,  rbY, rbw, 16, () -> { cfg.searchRadius = Math.max(1, cfg.searchRadius - 1);  cfg.save(); });
        r1p  = mkBtn("+1",  rbX0 + (rbw+gap)*2, rbY, rbw, 16, () -> { cfg.searchRadius = Math.min(128, cfg.searchRadius + 1);  cfg.save(); });
        r10p = mkBtn("+10", rbX0 + (rbw+gap)*3, rbY, rbw, 16, () -> { cfg.searchRadius = Math.min(128, cfg.searchRadius + 10); cfg.save(); });

        // ── 操作（3 列布局） ──
        int bw3 = (W - 40) / 3, bh = 16, gap3 = 5;
        locateBtn = mkBtn("§a定位并前往", cx + 10,                  cy + 172, bw3, bh, () -> StructureLocatorFeature.locate(true));
        coordBtn  = mkBtn("§7只显示坐标",  cx + 15 + bw3,            cy + 172, bw3, bh, () -> StructureLocatorFeature.locate(false));
        mkBtn("§b标记结构",              cx + 20 + (bw3+gap3)*2, cy + 172, bw3, bh, () -> StructureLocatorFeature.markLocation());
        nextBtn   = mkBtn("§e空点→找下一个", cx + 10,                  cy + 192, bw3, bh, () -> StructureLocatorFeature.skipAndNext());
        clearBtn  = mkBtn("§7清空跳过记录",  cx + 15 + bw3,            cy + 192, bw3, bh, () -> StructureLocatorFeature.clearSkips());
        mkBtn("§c清除标记",                 cx + 20 + (bw3+gap3)*2, cy + 192, bw3, bh, () -> StructureLocatorFeature.clearMark());

        rebuildStructList();
    }

    private Button mkBtn(String text, int x, int y, int w, int h, Runnable action) {
        var b = Button.builder(Component.literal(text), b2 -> action.run()).bounds(x, y, w, h).build();
        addRenderableWidget(b);
        return b;
    }

    private boolean idxOk(int i) { return i >= 0 && i < StructureLocatorFeature.TARGETS.size(); }

    private void rebuildStructList() {
        structButtons.clear();
        if (!showList) return;
        var targets = StructureLocatorFeature.TARGETS;
        int lx = cx + W + 6;
        int ly = cy + 28;
        int itemH = 13;
        int maxVis = Math.min(targets.size(), Math.max(1, (H - 50) / itemH));
        int start = Math.max(0, Math.min(listScroll, targets.size() - maxVis));

        for (int i = start; i < targets.size() && i < start + maxVis; i++) {
            final int idx = i;
            boolean sel = idx == StructureLocatorConfig.getInstance().targetIndex;
            Button btn = Button.builder(
                    Component.literal((sel ? "§6▶ " : "  ") + targets.get(i).name),
                    b -> {
                        var c = StructureLocatorConfig.getInstance();
                        c.targetIndex = idx; c.save();
                        showList = false;
                        targetBtn.setMessage(Component.literal("§f" + targets.get(idx).name + "  §7▶"));
                    }
            ).bounds(lx + 4, ly + (i - start) * itemH, LIST_W - 8, itemH).build();
            structButtons.add(btn);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        var cfg = StructureLocatorConfig.getInstance();

        // ── 主面板 ──
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);

        g.drawString(font, "§l§6结构定位", cx + 10, cy + 8, 0xFFFFFFFF);
        g.drawString(font, "§7种子: " + seedStr(cfg), cx + 10, cy + 28, 0xFFFFFFFF);
        g.drawString(font, "§7手动种子 (留空用捕获的):", cx + 10, cy + 40, 0xFFCCCCCC);
        g.fill(cx + 10, cy + 94, cx + W - 10, cy + 95, 0xFF444444);
        g.drawString(font, "§7目标结构:", cx + 10, cy + 100, 0xFFCCCCCC);
        g.drawString(font, "§7搜索范围: §f" + cfg.searchRadius + " §7区域", cx + 10, cy + 142, 0xFFCCCCCC);

        super.render(g, mx, my, pt);

        // ── 右侧结构列表面板（点击 ▶ 展开） ──
        if (showList) {
            int lx = cx + W + 4, ly = cy + 20;
            int lh = Math.min(StructureLocatorFeature.TARGETS.size() * 13 + 20, H - 20);
            GuiRenderHelper.drawPanelBackground(g, lx, ly, LIST_W, lh, false);
            g.drawString(font, "§7选择结构", lx + 6, ly + 6, 0xFFCCCCCC);
            g.fill(lx + 4, ly + 16, lx + LIST_W - 4, ly + 17, 0xFF444444);
            for (Button b : structButtons) b.render(g, mx, my, pt);
        }

        g.drawString(font, "§7§o①取种子 ②选结构(▶) ③定位", cx + 10, cy + H - 14, 0xFF888888);
    }

    private String seedStr(StructureLocatorConfig cfg) {
        if (cfg.manualSeed != null && !cfg.manualSeed.trim().isEmpty()) return "§a手动:" + cfg.manualSeed;
        if (cfg.hasSeed) return "§b捕获:" + cfg.capturedSeed;
        return "§7无种子";
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && showList) {
            for (Button b : structButtons) {
                if (mx >= b.getX() && mx <= b.getX() + b.getWidth() && my >= b.getY() && my <= b.getY() + b.getHeight()) {
                    b.onPress(); return true;
                }
            }
            // 点击列表外部区域（不在右侧列表范围内）关闭
            int lx = cx + W + 4, ly = cy + 20;
            if (!(mx >= lx && mx <= lx + LIST_W && my >= ly && my <= ly + H - 20)) {
                showList = false;
            }
        }

        if (button == 0 && seedInput != null) {
            seedInput.mouseClicked(mx, my, button);
            if (mx >= seedInput.getX() && mx <= seedInput.getX() + seedInput.getWidth()
                    && my >= seedInput.getY() && my <= seedInput.getY() + seedInput.getHeight()) {
                var cfg = StructureLocatorConfig.getInstance();
                cfg.manualSeed = seedInput.getValue().trim(); cfg.save();
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double deltaX, double deltaY) {
        if (showList && mx >= cx + W + 4 && mx <= cx + W + 4 + LIST_W && my >= cy + 20 && my <= cy + H) {
            int maxVis = Math.min(StructureLocatorFeature.TARGETS.size(), Math.max(1, (H - 50) / 13));
            listScroll = (int) Math.max(0, Math.min(StructureLocatorFeature.TARGETS.size() - maxVis, listScroll - deltaY));
            rebuildStructList();
            return true;
        }
        return super.mouseScrolled(mx, my, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { onClose(); return true; }
        if (seedInput != null && seedInput.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                seedInput.setFocused(false);
                var cfg = StructureLocatorConfig.getInstance();
                cfg.manualSeed = seedInput.getValue().trim(); cfg.save();
                return true;
            }
            return seedInput.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }
}
