package fku.org.example.fku.features.tpgoto; /* water */

import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.config.GuiStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 传送前往 — 合并配置界面
 */
public class TpGotoScreen extends Screen {

    private static final int PANEL_X = 20, PANEL_Y = 20, PANEL_W = 350, PANEL_H = 260;
    private int guiLeft, guiTop;

    public TpGotoScreen() {
        super(Component.literal("传送前往 配置"));
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (width - PANEL_W) / 2;
        guiTop = (height - PANEL_H) / 2;

        TpGotoConfig cfg = TpGotoConfig.getInstance();
        int cx = guiLeft + 15, cy = guiTop + 30, sp = 24;

        // 开关
        addToggle(cx, cy, "§bTP时启用飞行", () -> cfg.tpFlightEnabled, v -> cfg.setTpFlightEnabled(v));
        addToggle(cx + 150, cy, "等待区块加载", () -> cfg.waitForChunk, v -> cfg.setWaitForChunk(v));
        cy += sp;

        addToggle(cx, cy, "允许空中路径", () -> cfg.airPath, v -> cfg.setAirPath(v));
        cy += sp;

        addToggle(cx, cy, "路径渲染", () -> cfg.renderPath, v -> cfg.setRenderPath(v));
        cy += sp;

        addToggle(cx, cy, "位置校验", () -> cfg.positionCheck, v -> cfg.setPositionCheck(v));
        cy += sp;

        // 数值输入
        addLabel(cx, cy, "发包间隔(ms):");
        addEditBox(cx + 100, cy, 50, String.valueOf(cfg.packetInterval), v -> cfg.setPacketInterval(parseInt(v, 30)));
        addLabel(cx + 170, cy, "等待超时(ms):");
        addEditBox(cx + 260, cy, 70, String.valueOf(cfg.chunkWaitTimeout), v -> cfg.setChunkWaitTimeout(parseInt(v, 3000)));
        cy += sp;

        addLabel(cx, cy, "停止距离:");
        addEditBox(cx + 100, cy, 50, String.valueOf(cfg.stopDistance), v -> cfg.setStopDistance(parseDouble(v, 1.5)));
        cy += sp;

        addLabel(cx, cy, "最大步长:");
        addEditBox(cx + 100, cy, 50, String.valueOf(cfg.maxStep), v -> cfg.setMaxStep(parseDouble(v, 3.0)));
        cy += sp;

        addLabel(cx, cy, "搜索范围:");
        addEditBox(cx + 100, cy, 50, String.valueOf(cfg.maxRange), v -> cfg.setMaxRange(parseDouble(v, 100.0)));
        cy += sp;

        addLabel(cx, cy, "校验间隔(ms):");
        addEditBox(cx + 100, cy, 50, String.valueOf(cfg.positionCheckInterval), v -> cfg.setPositionCheckInterval(parseInt(v, 1000)));
        cy += sp;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        GuiStyleConfig style = GuiStyleConfig.getInstance();
        GuiRenderHelper.drawRoundedRect(g, guiLeft, guiTop, PANEL_W, PANEL_H, style.getBackgroundColorWithAlpha(200), 6);
        GuiRenderHelper.drawRoundedOutline(g, guiLeft, guiTop, PANEL_W, PANEL_H, style.getPrimaryColorWithAlpha(200), 6, 2);
        g.drawString(Minecraft.getInstance().font, "§l传送前往 配置", guiLeft + 15, guiTop + 12, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (key == 256) { // ESC
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(key, scan, mod);
    }

    // ──────── 辅助方法 ────────

    private void addLabel(int x, int y, String text) {
        addRenderableOnly((g, mx, my, pt) -> g.drawString(Minecraft.getInstance().font, text, x, y + 4, 0xCCCCCC));
    }

    private void addToggle(int x, int y, String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        addRenderableWidget(new net.minecraft.client.gui.components.Button(
                x, y, 120, 18, Component.literal(""),
                btn -> { setter.accept(!getter.get()); },
                btn -> Component.literal("")) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
                boolean val = getter.get();
                int color = val ? 0xFF4CAF50 : 0xFF666666;
                GuiRenderHelper.drawRoundedRect(g, getX(), getY(), width, height, color, 3);
                String txt = label + ": " + (val ? "开" : "关");
                g.drawString(Minecraft.getInstance().font, txt, getX() + 5, getY() + 5, 0xFFFFFF);
            }
        });
    }

    private void addEditBox(int x, int y, int w, String val, Consumer<String> saver) {
        EditBox box = new EditBox(Minecraft.getInstance().font, x, y, w, 16, Component.literal(""));
        box.setValue(val);
        box.setResponder(s -> {
            if (!s.equals(val)) saver.accept(s);
        });
        addRenderableWidget(box);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }
}