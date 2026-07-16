package fku.org.example.fku.features.bedrockbreaker; /* water */

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * 基岩破坏器配置界面
 *
 * 配置项：
 *   - 热键绑定
 *   - 目标方块ID（可写入准星处方块）
 *   - 替换方块ID（可写入准星处方块）
 *   - 扫描模式开关
 *   - 各步骤超时时间
 *
 * 界面布局（Y行距 = 28px，标签在上、输入框在下，避免重叠）：
 *   标    签 → cy(rowOffset)
 *   输入框   → cy(rowOffset + 14)    （14px 间距，确保标签文本与输入框分离）
 *   提示文字 → cy(rowOffset + 28)
 */
public class BedrockBreakerScreen extends Screen {

    private static final int WIDTH = 290;
    private static final int HEIGHT = 375;

    // 行Y偏移基准（间距34px，确保提示文字与下一行标签不重叠）
    private static final int ROW_HOTKEY = 30;
    private static final int ROW_TARGET = 60;      // 标签60, 输入框74, 提示文字88
    private static final int ROW_REPLACE = 98;     // 标签98, 输入框112, 提示文字126
    private static final int ROW_MODE = 136;       // 扫描模式+全方块模式按钮
    private static final int ROW_TIMEOUT = 172;    // 双列超时：标签172, 输入框186
    private static final int ROW_LEVER = 208;      // 拉杆超时：标签208, 输入框222
    private static final int ROW_HELPER_SWITCH = 236;  // v2.2 辅助方块开关
    private static final int ROW_HELPER_LIST = 266;    // v2.2 辅助方块列表
    private static final int ROW_HINT = 310;       // 使用说明文字
    private static final int ROW_BUTTON = 340;     // 保存+完成按钮

    private EditBox targetBlockInput;
    private EditBox replaceBlockInput;
    private EditBox breakTimeoutInput;
    private EditBox extendTimeoutInput;
    private EditBox leverTimeoutInput;
    private Button hotkeyButton;
    private Button scanModeButton;
    private boolean listeningForKey = false;

    public BedrockBreakerScreen() {
        super(Component.literal("基岩破坏器配置"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - WIDTH) / 2;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();

        // ── 行1：热键 ──
        String currentKeyName = getCurrentKeyDisplay();
        hotkeyButton = Button.builder(
                Component.literal("热键: " + currentKeyName),
                btn -> {
                    listeningForKey = true;
                    btn.setMessage(Component.literal("热键: 按下新键..."));
                }
        ).bounds(cx + 10, cy(ROW_HOTKEY), 120, 18).build();
        addRenderableWidget(hotkeyButton);

        addRenderableWidget(Button.builder(
                Component.literal("重置"),
                btn -> {
                    KeyBindings.updateBedrockBreakerKey(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_B));
                    hotkeyButton.setMessage(Component.literal("热键: B"));
                    listeningForKey = false;
                }
        ).bounds(cx + 135, cy(ROW_HOTKEY), 50, 18).build());

        // ── 行2：目标方块（标签 + 输入框 + 写入按钮 + allBlocks开关）──
        targetBlockInput = new EditBox(font, cx + 68, cy(ROW_TARGET + 14), 130, 14, Component.literal(""));
        targetBlockInput.setValue(cfg.targetBlockId != null ? cfg.targetBlockId : "minecraft:bedrock");
        targetBlockInput.setMaxLength(64);
        addRenderableWidget(targetBlockInput);

        addRenderableWidget(Button.builder(
                Component.literal("写入"),
                btn -> writeCrosshairBlockTo(targetBlockInput)
        ).bounds(cx + 202, cy(ROW_TARGET + 13), 40, 16).build());

        // 所有方块开关（目标方块右侧）
        String allBlocksLabel = "全方块: " + (cfg.allBlocks ? "开" : "关");
        addRenderableWidget(Button.builder(
                Component.literal(allBlocksLabel),
                btn -> {
                    cfg.setAllBlocks(!cfg.allBlocks);
                    btn.setMessage(Component.literal("全方块: " + (cfg.allBlocks ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_TARGET + 28), 80, 16).build());

        // ── 行3：替换方块（标签 + 输入框 + 写入按钮）──
        replaceBlockInput = new EditBox(font, cx + 68, cy(ROW_REPLACE + 14), 130, 14, Component.literal(""));
        replaceBlockInput.setValue(cfg.replaceBlockId != null ? cfg.replaceBlockId : "");
        replaceBlockInput.setMaxLength(64);
        addRenderableWidget(replaceBlockInput);

        addRenderableWidget(Button.builder(
                Component.literal("写入"),
                btn -> writeCrosshairBlockTo(replaceBlockInput)
        ).bounds(cx + 202, cy(ROW_REPLACE + 13), 40, 16).build());

        // ── 行4：扫描模式 + allBlocks（双按钮同行）──
        scanModeButton = Button.builder(
                Component.literal("扫描模式: " + (cfg.scanMode ? "开" : "关")),
                btn -> {
                    cfg.setScanMode(!cfg.scanMode);
                    btn.setMessage(Component.literal("扫描模式: " + (cfg.scanMode ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_MODE), 100, 16).build();
        addRenderableWidget(scanModeButton);

        // ── 行5：破坏超时 + 伸出超时（双列）──
        breakTimeoutInput = new EditBox(font, cx + 68, cy(ROW_TIMEOUT + 2), 40, 14, Component.literal(""));
        breakTimeoutInput.setValue(String.valueOf(cfg.breakTimeout));
        breakTimeoutInput.setMaxLength(3);
        breakTimeoutInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(breakTimeoutInput);

        extendTimeoutInput = new EditBox(font, cx + 170, cy(ROW_TIMEOUT + 2), 40, 14, Component.literal(""));
        extendTimeoutInput.setValue(String.valueOf(cfg.extendTimeout));
        extendTimeoutInput.setMaxLength(2);
        extendTimeoutInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(extendTimeoutInput);

        // ── 行6：拉杆超时 ──
        leverTimeoutInput = new EditBox(font, cx + 68, cy(ROW_LEVER + 2), 40, 14, Component.literal(""));
        leverTimeoutInput.setValue(String.valueOf(cfg.leverBreakTimeout));
        leverTimeoutInput.setMaxLength(3);
        leverTimeoutInput.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(leverTimeoutInput);

        // ── 行7：辅助方块开关（v2.2 新增） ──
        addRenderableWidget(Button.builder(
                Component.literal("辅助方块: " + (cfg.enableHelperBlocks ? "开" : "关")),
                btn -> {
                    cfg.setEnableHelperBlocks(!cfg.enableHelperBlocks);
                    btn.setMessage(Component.literal("辅助方块: " + (cfg.enableHelperBlocks ? "开" : "关")));
                }
        ).bounds(cx + 10, cy(ROW_HELPER_SWITCH), 100, 16).build());

        addRenderableWidget(Button.builder(
                Component.literal("清理辅助块: " + (cfg.cleanupHelpers ? "开" : "关")),
                btn -> {
                    cfg.setCleanupHelpers(!cfg.cleanupHelpers);
                    btn.setMessage(Component.literal("清理辅助块: " + (cfg.cleanupHelpers ? "开" : "关")));
                }
        ).bounds(cx + 120, cy(ROW_HELPER_SWITCH), 110, 16).build());

        // ── 行8：辅助方块列表（v2.5 改为按钮打开大编辑界面） ──
        addRenderableWidget(Button.builder(
                Component.literal("编辑辅助方块列表..."),
                btn -> Minecraft.getInstance().setScreen(new HelperBlockListScreen(this))
        ).bounds(cx + 68, cy(ROW_HELPER_LIST + 12), 150, 18).build());

        // ── 行9：底部按钮 ──
        addRenderableWidget(Button.builder(
                Component.literal("保存"),
                btn -> saveConfig()
        ).bounds(cx + 80, cy(ROW_BUTTON), 60, 18).build());

        addRenderableWidget(Button.builder(
                Component.literal("完成"),
                btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())
        ).bounds(cx + 160, cy(ROW_BUTTON), 60, 18).build());
    }

    /** 将准星位置的方块ID写入指定输入框 */
    private void writeCrosshairBlockTo(EditBox input) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            input.setValue("§c未瞄准方块");
            return;
        }
        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        Block block = mc.level.getBlockState(hit.getBlockPos()).getBlock();
        net.minecraft.resources.ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null) {
            input.setValue(id.toString());
        }
    }

    private String getCurrentKeyDisplay() {
        InputConstants.Key key = KeyBindings.BEDROCK_BREAKER_KEY.getKey();
        String name = key.getName();
        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            return parts[parts.length - 1].toUpperCase();
        }
        return name.toUpperCase();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningForKey) {
            InputConstants.Key newKey = InputConstants.getKey(keyCode, scanCode);
            if (newKey != InputConstants.UNKNOWN) {
                KeyBindings.updateBedrockBreakerKey(newKey);
                String display = getCurrentKeyDisplay();
                hotkeyButton.setMessage(Component.literal("热键: " + display));
                listeningForKey = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveConfig() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        cfg.setTargetBlockId(targetBlockInput.getValue());
        cfg.setReplaceBlockId(replaceBlockInput.getValue());
        try { cfg.setBreakTimeout(Integer.parseInt(breakTimeoutInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setExtendTimeout(Integer.parseInt(extendTimeoutInput.getValue())); } catch (Exception ignored) {}
        try { cfg.setLeverBreakTimeout(Integer.parseInt(leverTimeoutInput.getValue())); } catch (Exception ignored) {}
    }

    /** 计算相对于面板顶部的Y坐标 */
    private int cy(int rowOffset) {
        return (height - HEIGHT) / 2 + rowOffset;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        int cx = (width - WIDTH) / 2;

        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, cy(0), WIDTH, HEIGHT, false);
        guiGraphics.drawString(font, "基岩破坏器配置", cx + 10, cy(8), 0xFFFFFF);

        // ── 行2：目标方块 ──
        guiGraphics.drawString(font, "目标方块:", cx + 10, cy(ROW_TARGET), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(点击【写入】写入准星处方块)", cx + 96, cy(ROW_TARGET + 28), 0x666666);

        // ── 行3：替换方块 ──
        guiGraphics.drawString(font, "替换方块:", cx + 10, cy(ROW_REPLACE), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(空=不替换)", cx + 10, cy(ROW_REPLACE + 28), 0x666666);

        // ── 行4：模式 ──
        guiGraphics.drawString(font, "§7| 扫描模式自动扫描周围目标方块", cx + 115, cy(ROW_MODE + 1), 0x666666);

        // ── 行5：双列超时 ──
        guiGraphics.drawString(font, "破坏超时:", cx + 10, cy(ROW_TIMEOUT), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(tick)", cx + 110, cy(ROW_TIMEOUT), 0x666666);
        guiGraphics.drawString(font, "伸出超时:", cx + 126, cy(ROW_TIMEOUT), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(tick)", cx + 212, cy(ROW_TIMEOUT), 0x666666);

        // ── 行6：拉杆超时 ──
        guiGraphics.drawString(font, "拉杆超时:", cx + 10, cy(ROW_LEVER), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(tick)", cx + 110, cy(ROW_LEVER), 0x666666);

        // ── 行7：辅助方块（v2.2 新增） ──
        guiGraphics.drawString(font, "§7| 找不到拉杆位置时自动放置辅助方块", cx + 10, cy(ROW_HELPER_SWITCH + 18), 0x666666);

        // ── 行8：辅助方块列表（v2.2 新增） ──
        guiGraphics.drawString(font, "辅助方块列表:", cx + 10, cy(ROW_HELPER_LIST), 0xAAAAAA);
        guiGraphics.drawString(font, "§7(逗号分隔,优先级从前到后)", cx + 10, cy(ROW_HELPER_LIST + 28), 0x666666);

        // ── 行9：使用方法 ──
        guiGraphics.drawString(font, "§7使用方法：看向目标方块按热键（默认 B）", cx + 10, cy(ROW_HINT), 0x888888);
        guiGraphics.drawString(font, "§7需手持活塞和拉杆(快捷栏)，有镐更快", cx + 10, cy(ROW_HINT + 12), 0x888888);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}