package fku.org.example.fku.features.spear;

import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 矛之冲锋配置界面：EditBox 用 addRenderableWidget，由 Screen 自动路由鼠标/键盘，可正常输入。
 * 发包模式显示“戳击距离+发包数”，原版模式显示“原版速度”。
 */
public class SpearChargeScreen extends Screen {
    private static final int W = 320, H = 150;
    private int cx, cy;
    private EditBox boostIn, packetsIn, vanillaIn;
    private Button modePacketBtn, modeVanillaBtn, closeBtn;
    private boolean pendingPacket = true;

    public SpearChargeScreen() { super(Component.literal("矛之冲锋配置")); }

    @Override
    protected void init() {
        super.init();
        cx = (width - W) / 2; cy = (height - H) / 2;
        var c = SpearChargeConfig.getInstance();
        pendingPacket = "packet".equals(c.mode);

        boostIn = mkEdit(cx + 170, cy + 25, 60, String.valueOf(c.boostDistance), "-?\\d*\\.?\\d*");
        packetsIn = mkEdit(cx + 170, cy + 45, 60, String.valueOf(c.packets), "\\d*");
        vanillaIn = mkEdit(cx + 170, cy + 25, 60, String.valueOf(c.vanillaSpeed), "-?\\d*\\.?\\d*");

        modePacketBtn = addRenderableWidget(Button.builder(Component.literal("§f发包模式"),
                b -> setMode(true)).bounds(cx + 20, cy + 60, 130, 18).build());
        modeVanillaBtn = addRenderableWidget(Button.builder(Component.literal("§f原版模式"),
                b -> setMode(false)).bounds(cx + 170, cy + 60, 130, 18).build());

        closeBtn = addRenderableWidget(Button.builder(Component.literal("§c关闭"), b -> onClose()).bounds(cx + 110, cy + 110, 100, 20).build());

        refreshModeButtons();
        refreshVisibility();
    }

    private void setMode(boolean packet) { pendingPacket = packet; refreshModeButtons(); refreshVisibility(); }

    private void refreshModeButtons() {
        modePacketBtn.setMessage(Component.literal(pendingPacket ? "§a✔ 发包模式" : "§7发包模式"));
        modeVanillaBtn.setMessage(Component.literal(!pendingPacket ? "§a✔ 原版模式" : "§7原版模式"));
        modePacketBtn.active = !pendingPacket;
        modeVanillaBtn.active = pendingPacket;
    }

    private void refreshVisibility() {
        boostIn.setVisible(pendingPacket);
        packetsIn.setVisible(pendingPacket);
        vanillaIn.setVisible(!pendingPacket);
    }

    private EditBox mkEdit(int x, int y, int w, String val, String filter) {
        var b = new EditBox(font, x, y, w, 14, Component.literal(""));
        b.setValue(val); b.setMaxLength(8); b.setFilter(s -> s.matches(filter));
        addRenderableWidget(b); return b;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        GuiRenderHelper.drawPanelBackground(g, cx, cy, W, H, false);
        g.drawString(font, "§l矛之冲锋配置", cx + 10, cy + 8, 0xFFFFFF);
        g.fill(cx + 10, cy + 20, cx + W - 10, cy + 21, 0xFF444444);

        if (pendingPacket) {
            g.drawString(font, "戳击距离(矛速×20):", cx + 20, cy + 26, 0xAAAAAA);
            g.drawString(font, "每tick发包数:", cx + 20, cy + 46, 0xAAAAAA);
        } else {
            g.drawString(font, "原版突进速度(格/tick):", cx + 20, cy + 26, 0xAAAAAA);
        }
        g.drawString(font, "§7手持矛+按住右键蓄力；发包朝准星，原版锁定小范围目标并三维跟随", cx + 20, cy + 118, 0x888888);
        super.render(g, mx, my, pt); // EditBox / Button 由 Screen 自动路由与渲染
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        var c = SpearChargeConfig.getInstance();
        try { c.boostDistance = Double.parseDouble(boostIn.getValue()); } catch (NumberFormatException ignored) {}
        try { c.packets = Integer.parseInt(packetsIn.getValue()); } catch (NumberFormatException ignored) {}
        try { c.vanillaSpeed = Double.parseDouble(vanillaIn.getValue()); } catch (NumberFormatException ignored) {}
        c.mode = pendingPacket ? "packet" : "vanilla";
        SpearChargeConfig.save();
        super.onClose();
    }
}
