package fku.org.example.fku.features.displaymodel;

import fku.org.example.fku.Fku;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 实体模型展示 GUI（纯 UI）
 *
 * 职责：
 * - 粘贴 /summon 指令
 * - 配置选项（放置延迟、生成间隔、实体间距）
 * - 点击"召唤模型" → 委托 DisplayModelManager 执行状态机
 * - 实时显示 Manager 的进度和状态
 *
 * 设计思想（实践论）：
 * - 状态机抽离到 DisplayModelManager，关闭 Screen 后仍然继续放置
 * - Screen 只管 UI 布局和事件委托
 * - 配置保存：仅"保存配置"按钮持久化，激发时直接从输入框读取数值传给 Manager
 */
public class DisplayModelScreen extends Screen {

    private static final int WIDTH = 440;
    private static final int HEIGHT = 220;

    private EditBox commandInput;
    private EditBox placeDelayInput;
    private EditBox generationDelayInput;
    private EditBox entitySpacingInput;
    private Button summonButton;
    private Button saveButton;

    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;

    private final DisplayModelConfig config;
    private final DisplayModelManager manager;

    public DisplayModelScreen() {
        super(Component.literal("实体模型展示"));
        this.config = DisplayModelConfig.getInstance();
        this.manager = DisplayModelManager.getInstance();
        manager.setOnStatusUpdate(() -> {
            String msg = manager.getStatusMessage();
            if (msg != null && !msg.isEmpty()) {
                this.statusMessage = msg;
                this.statusColor = msg.startsWith("§c") ? 0xFF5555 : 0x55FF55;
            }
            if (!manager.isRunning() && summonButton != null) {
                summonButton.setMessage(Component.literal("召唤模型"));
                summonButton.active = true;
            } else if (manager.isRunning() && summonButton != null) {
                summonButton.setMessage(Component.literal("放置中 " + manager.getCurrentIndex() + "/" + manager.getTotalCount()));
                summonButton.active = false;
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        // ---- 指令输入框 ----
        commandInput = new EditBox(font, x + 10, y + 35, WIDTH - 20, 20, Component.literal(""));
        commandInput.setMaxLength(32767);
        addWidget(commandInput);

        // ---- 放置延迟（ms）----
        placeDelayInput = new EditBox(font, x + 95, y + 78, 60, 16, Component.literal(""));
        placeDelayInput.setValue(String.valueOf((int) config.placeDelay));
        placeDelayInput.setMaxLength(5);
        placeDelayInput.setFilter(s -> s.matches("\\d*"));
        addWidget(placeDelayInput);

        // ---- 生成间隔（ms）----
        generationDelayInput = new EditBox(font, x + 255, y + 78, 60, 16, Component.literal(""));
        generationDelayInput.setValue(String.valueOf((int) config.generationDelay));
        generationDelayInput.setMaxLength(5);
        generationDelayInput.setFilter(s -> s.matches("\\d*"));
        addWidget(generationDelayInput);

        // ---- 实体间距（格）----
        entitySpacingInput = new EditBox(font, x + 355, y + 78, 55, 16, Component.literal(""));
        entitySpacingInput.setValue(String.valueOf(config.entitySpacing));
        entitySpacingInput.setMaxLength(4);
        entitySpacingInput.setFilter(s -> s.matches("\\d*\\.?\\d*"));
        addWidget(entitySpacingInput);

        // ---- 保存配置按钮 ----
        saveButton = Button.builder(Component.literal("保存配置"), btn -> {
                    saveInputsToConfig();
                })
                .bounds(x + WIDTH - 85, y + HEIGHT - 30, 75, 20).build();
        addRenderableWidget(saveButton);

        // ---- 召唤按钮 ----
        summonButton = Button.builder(Component.literal("召唤模型"), btn -> startSummon())
                .bounds(width / 2 - 100, y + HEIGHT - 30, 90, 20).build();
        addRenderableWidget(summonButton);

        if (manager.isRunning()) {
            summonButton.setMessage(Component.literal("放置中..."));
            summonButton.active = false;
            statusMessage = manager.getStatusMessage();
        }
    }

    @Override
    public void tick() {
        super.tick();
        commandInput.tick();
        placeDelayInput.tick();
        generationDelayInput.tick();
        entitySpacingInput.tick();

        if (manager.isRunning()) {
            String msg = manager.getStatusMessage();
            if (msg != null && !msg.isEmpty()) {
                this.statusMessage = msg;
                this.statusColor = msg.startsWith("§c") ? 0xFF5555 : 0x55FF55;
            }
            summonButton.setMessage(Component.literal(
                    "放置中 " + manager.getCurrentIndex() + "/" + manager.getTotalCount()));
            summonButton.active = false;
        }
    }

    // ====================================================================
    //  startSummon — 直接从输入框读取数值，不依赖 config 单例
    // ====================================================================
    private void startSummon() {
        if (manager.isRunning()) {
            setStatusMessage("§e放置正在进行中...", 0xFFFF55);
            return;
        }

        String command = commandInput.getValue().trim();
        if (command.isEmpty()) {
            setStatusMessage("§c请输入 /summon 指令", 0xFF5555);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!player.isCreative()) {
            setStatusMessage("§c需要创造模式", 0xFF5555);
            return;
        }

        // ★ 直接从输入框读取数值（不依赖 config 单例）
        int placeDelayMs = 50;
        int generationDelayMs = 50;
        double entitySpacing = 0.5;

        try {
            String v = placeDelayInput.getValue();
            if (!v.isEmpty()) placeDelayMs = Integer.parseInt(v);
        } catch (NumberFormatException ignored) {}

        try {
            String v = generationDelayInput.getValue();
            if (!v.isEmpty()) generationDelayMs = Integer.parseInt(v);
        } catch (NumberFormatException ignored) {}

        try {
            String v = entitySpacingInput.getValue();
            if (!v.isEmpty()) entitySpacing = Math.max(0, Math.min(10, Double.parseDouble(v)));
        } catch (NumberFormatException ignored) {}

        // 解析指令
        List<CompoundTag> passengers;
        Vec3 offset;
        try {
            offset = ModelParser.extractOffset(command);
            passengers = ModelParser.extractPassengers(command);
        } catch (Exception e) {
            setStatusMessage("§c指令格式错误: " + e.getMessage(), 0xFF5555);
            Fku.LOGGER.error("解析指令失败: {}", e.getMessage());
            return;
        }

        if (passengers.isEmpty()) {
            setStatusMessage("§c指令中没有找到 Passengers", 0xFF5555);
            return;
        }

        BlockPos fixedPos = player.blockPosition();

        // ★ 直接传递数值给 Manager
        manager.start(passengers, offset, fixedPos, entitySpacing, generationDelayMs, placeDelayMs);

        if (manager.isRunning()) {
            setStatusMessage("§a开始放置实体，共 " + passengers.size() + " 个...", 0x55FF55);
            summonButton.setMessage(Component.literal("放置中..."));
            summonButton.active = false;
        }
    }

    // ====================================================================
    //  saveInputsToConfig — 只有点击"保存配置"按钮时调用
    // ====================================================================
    private void saveInputsToConfig() {
        try {
            String val = placeDelayInput.getValue();
            if (!val.isEmpty()) config.setPlaceDelay(Double.parseDouble(val));
        } catch (NumberFormatException ignored) {}

        try {
            String val = generationDelayInput.getValue();
            if (!val.isEmpty()) config.setGenerationDelay(Double.parseDouble(val));
        } catch (NumberFormatException ignored) {}

        try {
            String val = entitySpacingInput.getValue();
            if (!val.isEmpty()) {
                double p = Double.parseDouble(val);
                if (p >= 0 && p <= 10) config.setEntitySpacing(p);
            }
        } catch (NumberFormatException ignored) {}

        setStatusMessage("§a配置已保存", 0x55FF55);
        Fku.LOGGER.info("[DisplayModel] 配置已保存: placeDelay={}, genDelay={}, spacing={}",
                config.placeDelay, config.generationDelay, config.entitySpacing);
    }

    // ====================================================================
    //  setStatusMessage
    // ====================================================================
    private void setStatusMessage(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
    }

    // ====================================================================
    //  render
    // ====================================================================
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int x = (width - WIDTH) / 2;
        int y = (height - HEIGHT) / 2;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xCC333333);
        guiGraphics.renderOutline(x, y, WIDTH, HEIGHT, 0xFF555555);

        guiGraphics.drawString(font, "实体模型展示", x + 10, y + 8, 0xFFFFFF);
        guiGraphics.drawString(font, "粘贴 /summon 指令（含 Passengers 嵌套）:", x + 10, y + 22, 0x888888);

        commandInput.render(guiGraphics, mouseX, mouseY, partialTick);

        if (commandInput.getValue().isEmpty() && !commandInput.isFocused()) {
            guiGraphics.drawString(font, "例如: /summon minecraft:block_display ~-0.5 ~-0.5 ~-0.5 {...}",
                    x + 14, y + 40, 0x555555);
        }

        guiGraphics.drawString(font, "配置选项:", x + 10, y + 57, 0x888888);

        guiGraphics.drawString(font, "放置延迟(ms):", x + 10, y + 65, 0xAAAAAA);
        placeDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(font, "生成间隔(ms):", x + 170, y + 65, 0xAAAAAA);
        generationDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(font, "间距(格):", x + 325, y + 65, 0xAAAAAA);
        entitySpacingInput.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!statusMessage.isEmpty()) {
            guiGraphics.drawString(font, statusMessage, x + 10, y + 102, statusColor);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}