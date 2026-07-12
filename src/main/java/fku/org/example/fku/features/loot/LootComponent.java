package fku.org.example.fku.features.loot;

import fku.org.example.fku.config.GuiStyleConfig;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.client.gui.components.GuiComponent;
import fku.org.example.fku.util.FeatureHotkeyManager;
import fku.org.example.fku.util.HotkeySystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class LootComponent extends GuiComponent {

    public LootComponent(int x, int y, int width, int height) {
        super(x, y, width, height, "一键取物");
        HotkeySystem.registerFeature("一键取物", () -> {
            LootConfig cfg = LootConfig.getInstance();
            if (cfg.enabled) {
                cfg.setEnabled(false);
                LootFeature.stop();
                if (Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§6[一键取物] §c已关闭，清空容器标记"), false);
            } else {
                cfg.setEnabled(true);
                LootFeature.start();
                if (Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§6[一键取物] §a已开启，开始自动取物"), false);
            }
        });
    }

    protected String getFeatureName() { return "一键取物"; }

    private boolean isEnabled() { return LootConfig.getInstance().enabled; }
    private void toggle() {
        LootConfig cfg = LootConfig.getInstance();
        if (cfg.enabled) {
            cfg.setEnabled(false);
            LootFeature.stop();
            if (Minecraft.getInstance().player != null)
                Minecraft.getInstance().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6[一键取物] §c已关闭，清空容器标记"), false);
        } else {
            cfg.setEnabled(true);
            LootFeature.start();
            if (Minecraft.getInstance().player != null)
                Minecraft.getInstance().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6[一键取物] §a已开启，开始自动取物"), false);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        if (!visible) return;
        GuiStyleConfig config = GuiStyleConfig.getInstance();

        if (HotkeySystem.isWaitingFor("一键取物")) {
            GuiRenderHelper.drawComponentBackground(g, x, y, width, height, true);
            g.drawString(Minecraft.getInstance().font, "绑定热键中... (Esc取消)", x + 5, y + (height - 8) / 2 - 4, 0xFFFF00);
            return;
        }

        boolean enabled = isEnabled();
        GuiRenderHelper.drawComponentBackground(g, x, y, width, height, enabled);
        String status = enabled ? "ON" : "OFF";
        var hk = FeatureHotkeyManager.getInstance().getHotkey("一键取物");
        String hkStr = hk.getHotkeyKey() >= 0 ? " §7[" + hk.getHotkeyName() + "]" : "";
        g.drawString(Minecraft.getInstance().font, "一键取物: " + status + hkStr, x + 5, y + (height - 8) / 2 - 4, enabled ? config.getTextColor() : 0xAAAAAA);
        if (LootFeature.isRunning()) {
            String rs = LootFeature.getStatus();
            if (!rs.isEmpty()) g.drawString(Minecraft.getInstance().font, "§a" + rs, x + 5, y + (height - 8) / 2 + 8, 0x55FF55);
        }
        g.drawString(Minecraft.getInstance().font, ">>", x + width - 18, y + (height - 8) / 2 - 4, 0x888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            if (HotkeySystem.isWaiting()) return false;
            toggle();
            if (isEnabled()) LootFeature.start();
            return true;
        } else if (button == 1) {
            if (HotkeySystem.isWaiting()) return false;
            Minecraft.getInstance().setScreen(new LootScreen()); return true;
        } else if (button == 2) {
            HotkeySystem.startBinding("一键取物", () -> {});
            return true;
        }
        return false;
    }

    @Override public boolean keyPressed(int k, int s, int m) { return false; }
}
