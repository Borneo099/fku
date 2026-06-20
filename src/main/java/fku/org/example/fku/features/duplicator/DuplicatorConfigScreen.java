package fku.org.example.fku.features.duplicator; /* water */

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * 三叉戟复制配置界面
 *
 * 配置项：
 *   - 复制延迟（tick）：两次复制循环的间隔
 *   - 蓄力时间（tick）：useItem 后等待的时间
 *   - 自动丢出：复制品是否自动丢出
 *   - 耐久管理：是否自动选择高耐久三叉戟
 */
public class DuplicatorConfigScreen extends Screen {

    private static final int WIDTH = 260;
    private static final int HEIGHT = 200;

    private EditBox dupeDelayInput;
    private EditBox holdDurationInput;
    private Button dropToggle;
    private Button durabilityToggle;

    private final DuplicatorConfig config;

    public DuplicatorConfigScreen() {
        super(Component.literal("三叉戟复制配置"));
        this.config = DuplicatorConfig.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        // ---- 复制延迟 (tick) ----
        dupeDelayInput = new EditBox(font, cx + 140, cy + 30, 60, 18, Component.literal(""));
        dupeDelayInput.setValue(String.valueOf(config.dupeDelay));
        dupeDelayInput.setMaxLength(4);
        dupeDelayInput.setFilter(s -> s.matches("\\d*"));
        addWidget(dupeDelayInput);

        // ---- 蓄力时间 (tick) ----
        holdDurationInput = new EditBox(font, cx + 140, cy + 60, 60, 18, Component.literal(""));
        holdDurationInput.setValue(String.valueOf(config.holdDuration));
        holdDurationInput.setMaxLength(4);
        holdDurationInput.setFilter(s -> s.matches("\\d*"));
        addWidget(holdDurationInput);

        // ---- 自动丢出（切换按钮） ----
        dropToggle = Button.builder(
                Component.literal(config.dropTridents ? "是" : "否"),
                btn -> {
                    config.setDropTridents(!config.dropTridents);
                    btn.setMessage(Component.literal(config.dropTridents ? "是" : "否"));
                })
                .bounds(cx + 140, cy + 90, 50, 18).build();
        addRenderableWidget(dropToggle);

        // ---- 耐久管理（切换按钮） ----
        durabilityToggle = Button.builder(
                Component.literal(config.durabilityManagement ? "是" : "否"),
                btn -> {
                    config.setDurabilityManagement(!config.durabilityManagement);
                    btn.setMessage(Component.literal(config.durabilityManagement ? "是" : "否"));
                })
                .bounds(cx + 140, cy + 117, 50, 18).build();
        addRenderableWidget(durabilityToggle);

        // ---- 完成按钮 ----
        addRenderableWidget(Button.builder(Component.literal("完成"), btn -> {
            saveFromInputs();
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
        }).bounds(cx + 80, cy + 155, 100, 20).build());
    }

    private void saveFromInputs() {
        try {
            String v1 = dupeDelayInput.getValue();
            if (!v1.isEmpty()) {
                int val = Integer.parseInt(v1);
                if (val >= 1 && val <= 200) config.setDupeDelay(val);
            }
        } catch (NumberFormatException ignored) {}

        try {
            String v2 = holdDurationInput.getValue();
            if (!v2.isEmpty()) {
                int val = Integer.parseInt(v2);
                if (val >= 1 && val <= 200) config.setHoldDuration(val);
            }
        } catch (NumberFormatException ignored) {}

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§a复制工具配置已保存"), true);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;
        int cy = (height - HEIGHT) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy, WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "复制工具配置", cx + 10, cy + 8, 0xFFFFFF);

        // 标签
        guiGraphics.drawString(font, "复制延迟(tick):", cx + 12, cy + 34, 0xAAAAAA);
        guiGraphics.drawString(font, "蓄力时间(tick):", cx + 12, cy + 64, 0xAAAAAA);
        guiGraphics.drawString(font, "自动丢出:", cx + 12, cy + 94, 0xAAAAAA);
        guiGraphics.drawString(font, "耐久管理:", cx + 12, cy + 121, 0xAAAAAA);

        dupeDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);
        holdDurationInput.render(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}