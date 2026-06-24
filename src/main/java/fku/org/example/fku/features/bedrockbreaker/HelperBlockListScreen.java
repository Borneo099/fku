package fku.org.example.fku.features.bedrockbreaker; /* water */

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
 * 辅助方块列表编辑界面（v2.5 新增）
 *
 *   ★ 矛盾定性：
 *     原配置界面中辅助方块列表使用单行 EditBox，长度限制 128 字符，
 *     无法容纳完整的 9 种方块 ID（约 180 字符），且输入框过小不便编辑。
 *
 *   ★ 实践路线：
 *     独立大界面，多行 EditBox（高度 100px），无长度限制（设为 2000），
 *     提供保存和重置按钮。重置时恢复默认 9 种方块列表。
 *
 *   界面布局：
 *     - 标题
 *     - 多行输入框（宽 260, 高 100）
 *     - 提示文字
 *     - 保存 / 重置 / 返回按钮
 */
public class HelperBlockListScreen extends Screen {

    private static final int WIDTH = 290;
    private static final int HEIGHT = 220;

    private static final int ROW_TITLE = 10;
    private static final int ROW_INPUT = 30;
    private static final int ROW_HINT = 145;
    private static final int ROW_BUTTON = 175;

    private EditBox listInput;

    public HelperBlockListScreen() {
        super(Component.literal("辅助方块列表"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();

        // 多行输入框（宽 260, 高 100, 最大长度 2000）
        listInput = new EditBox(font, cx + 15, cy(ROW_INPUT), 260, 100, Component.literal(""));
        listInput.setValue(cfg.helperBlockList != null ? cfg.helperBlockList : "");
        listInput.setMaxLength(2000);
        listInput.setHint(Component.literal("§7输入方块ID,逗号分隔..."));
        addRenderableWidget(listInput);

        // 保存按钮
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> {
                    cfg.setHelperBlockList(listInput.getValue());
                    Minecraft.getInstance().setScreen(new BedrockBreakerScreen());
                }
        ).bounds(cx + 30, cy(ROW_BUTTON), 60, 18).build());

        // 重置按钮：恢复默认列表
        addRenderableWidget(Button.builder(
                Component.literal("重置"),
                btn -> listInput.setValue(BedrockBreakerConfig.DEFAULT_HELPER_BLOCK_LIST)
        ).bounds(cx + 115, cy(ROW_BUTTON), 60, 18).build());

        // 返回按钮
        addRenderableWidget(Button.builder(
                Component.literal("返回"),
                btn -> Minecraft.getInstance().setScreen(new BedrockBreakerScreen())
        ).bounds(cx + 200, cy(ROW_BUTTON), 60, 18).build());
    }

    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy(0), WIDTH, HEIGHT, false);

        // 标题
        guiGraphics.drawString(font, "辅助方块列表编辑", cx + 15, cy(ROW_TITLE), 0xFFFFFF);

        // 提示文字
        guiGraphics.drawString(font, "§7逗号分隔,优先级从前到后", cx + 15, cy(ROW_HINT), 0x888888);
        guiGraphics.drawString(font, "§7点击【重置】恢复默认9种方块", cx + 15, cy(ROW_HINT + 12), 0x888888);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new BedrockBreakerScreen());
    }
}
