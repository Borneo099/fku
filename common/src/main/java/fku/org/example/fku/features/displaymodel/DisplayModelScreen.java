package fku.org.example.fku.features.displaymodel;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.displaymodel.DisplayModelConfig;
import fku.org.example.fku.features.displaymodel.DisplayModelManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DisplayModelScreen
extends Screen {
    private static final int WIDTH = 480;
    private static final int BASE_HEIGHT = 222;
    private static final int ROW_HEIGHT = 24;
    private final List<CommandRow> commandRows = new ArrayList<CommandRow>();
    private EditBox placeDelayInput;
    private EditBox generationDelayInput;
    private EditBox entitySpacingInput;
    private EditBox placeXInput;
    private EditBox placeYInput;
    private EditBox placeZInput;
    private EditBox viewRangeInput;
    private Button summonButton;
    private Button saveButton;
    private Button cancelButton;
    private Button openWebsiteButton;
    private Button writePosButton;
    private Button clearPosButton;
    private Button savePresetButton;
    private Button loadPresetButton;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;
    private final DisplayModelConfig config;
    private final DisplayModelManager manager;
    private final List<GuiEventListener> myChildren = new ArrayList<GuiEventListener>();
    private final List<Renderable> myRenderables = new ArrayList<Renderable>();
    private boolean rebuildScheduled = false;
    private int totalHeight = 222;

    public DisplayModelScreen() {
        super(Component.literal("\u5b9e\u4f53\u6a21\u578b\u5c55\u793a"));
        this.config = DisplayModelConfig.getInstance();
        this.manager = DisplayModelManager.getInstance();
        this.manager.setOnStatusUpdate(this::updateFromManager);
    }

    protected void init() {
        super.init();
        this.commandRows.clear();
        if (this.config.commandLines != null && !this.config.commandLines.isEmpty()) {
            for (String line : this.config.commandLines) {
                CommandRow row = new CommandRow();
                row.savedValue = line;
                this.commandRows.add(row);
            }
        }
        if (this.commandRows.isEmpty()) {
            this.commandRows.add(new CommandRow());
        }
        this.rebuildLayout();
        this.updateFromManager();
    }

    private <T extends GuiEventListener & Renderable & NarratableEntry> T myAddRenderableWidget(T widget) {
        this.myChildren.add(widget);
        this.myRenderables.add(widget);
        return (T)this.addRenderableWidget(widget);
    }

    private <T extends GuiEventListener & NarratableEntry> T myAddWidget(T widget) {
        this.myChildren.add(widget);
        return (T)this.addWidget(widget);
    }

    private void rebuildLayout() {
        if (this.commandRows == null || this.commandRows.isEmpty()) {
            return;
        }
        ArrayList<String> savedCmds = new ArrayList<String>();
        for (CommandRow row : this.commandRows) {
            String val = row.input != null ? row.input.getValue() : row.savedValue;
            savedCmds.add(val != null ? val : "");
        }
        for (GuiEventListener w : this.myChildren) {
            this.removeWidget(w);
        }
        for (Renderable r : this.myRenderables) {
            this.renderables.remove(r);
        }
        this.myChildren.clear();
        this.myRenderables.clear();
        int x = (this.width - 480) / 2;
        if (this.config.guiX >= 0 && this.config.guiY >= 0) {
            x = this.config.guiX;
        }
        this.totalHeight = 222 + (this.commandRows.size() - 1) * 24;
        int y = (this.height - this.totalHeight) / 2;
        if (this.config.guiY >= 0) {
            y = this.config.guiY;
        }
        int currentY = y + 44;
        for (int i = 0; i < this.commandRows.size(); ++i) {
            CommandRow row = this.commandRows.get(i);
            boolean isFirst = i == 0;
            String savedVal = i < savedCmds.size() ? (String)savedCmds.get(i) : "";
            int rowIndex = i;
            String btnLabel = isFirst ? "\u00a7a+" : "\u00a7c-";
            row.toggleBtn = Button.builder(Component.literal((String)btnLabel), btn -> {
                if (isFirst) {
                    this.commandRows.add(new CommandRow());
                } else {
                    this.commandRows.remove(rowIndex);
                }
                Minecraft.getInstance().tell(this::rebuildLayout);
            }).bounds(x + 10, currentY, 18, 18).build();
            this.myAddRenderableWidget(row.toggleBtn);
            row.input = new EditBox(this.font, x + 32, currentY, 436, 18, Component.literal(""));
            row.input.setMaxLength(Short.MAX_VALUE);
            row.input.setValue(savedVal);
            this.myAddWidget(row.input);
            currentY += 24;
        }
        currentY += 14;
        int inputY = (currentY += 13) + 1;
        this.placeDelayInput = this.createConfigInput(x + 90, inputY, 60, String.valueOf(this.config.placeDelay), true, "\\d*");
        this.myAddWidget(this.placeDelayInput);
        this.generationDelayInput = this.createConfigInput(x + 240, inputY, 60, String.valueOf(this.config.generationDelay), true, "\\d*");
        this.myAddWidget(this.generationDelayInput);
        this.entitySpacingInput = this.createConfigInput(x + 380, inputY, 55, String.valueOf(this.config.entitySpacing), true, "\\d*\\.?\\d*");
        this.myAddWidget(this.entitySpacingInput);
        int viewRangeY = inputY + 22;
        this.viewRangeInput = this.createConfigInput(x + 80, viewRangeY, 60, this.config.viewRange > 0.0 ? String.valueOf(this.config.viewRange) : "", false, "\\d*\\.?\\d*");
        this.myAddWidget(this.viewRangeInput);
        int coordY = inputY + 44;
        this.placeXInput = this.createConfigInput(x + 80, coordY, 55, this.config.placeX != 0.0 ? String.valueOf(this.config.placeX) : "", false, "-?\\d*\\.?\\d*");
        this.myAddWidget(this.placeXInput);
        this.placeYInput = this.createConfigInput(x + 165, coordY, 55, this.config.placeY != 0.0 ? String.valueOf(this.config.placeY) : "", false, "-?\\d*\\.?\\d*");
        this.myAddWidget(this.placeYInput);
        this.placeZInput = this.createConfigInput(x + 250, coordY, 55, this.config.placeZ != 0.0 ? String.valueOf(this.config.placeZ) : "", false, "-?\\d*\\.?\\d*");
        this.myAddWidget(this.placeZInput);
        int btnCoordY = coordY - 1;
        this.writePosButton = Button.builder(Component.literal("\u5199\u5165\u73a9\u5bb6\u5750\u6807"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (p != null) {
                BlockPos bp = p.blockPosition();
                this.placeXInput.setValue(String.valueOf(bp.getX()));
                this.placeYInput.setValue(String.valueOf(bp.getY()));
                this.placeZInput.setValue(String.valueOf(bp.getZ()));
            }
        }).bounds(x + 313, btnCoordY, 80, 16).build();
        this.myAddRenderableWidget(this.writePosButton);
        this.clearPosButton = Button.builder(Component.literal("\u6e05\u7a7a\u5750\u6807"), btn -> {
            this.placeXInput.setValue("");
            this.placeYInput.setValue("");
            this.placeZInput.setValue("");
        }).bounds(x + 398, btnCoordY, 55, 16).build();
        this.myAddRenderableWidget(this.clearPosButton);
        int btnY1 = y + this.totalHeight - 54;
        int btnY2 = y + this.totalHeight - 30;
        int btnW = 72;
        int gap6 = (480 - 6 * btnW) / 7;
        int bX = x + gap6;
        this.openWebsiteButton = Button.builder(Component.literal("\u6253\u5f00\u6a21\u578b\u7f51\u7ad9"), btn -> Util.getPlatform().openUri(URI.create("https://block-display.com/"))).bounds(bX, btnY1, btnW, 20).build();
        this.myAddRenderableWidget(this.openWebsiteButton);
        this.savePresetButton = Button.builder(Component.literal("\u00a7a\u4fdd\u5b58\u9884\u8bbe"), btn -> this.savePreset()).bounds(bX + (btnW + gap6), btnY1, btnW, 20).build();
        this.myAddRenderableWidget(this.savePresetButton);
        this.loadPresetButton = Button.builder(Component.literal("\u00a7b\u8f7d\u5165\u9884\u8bbe"), btn -> this.loadPreset()).bounds(bX + 2 * (btnW + gap6), btnY1, btnW, 20).build();
        this.myAddRenderableWidget(this.loadPresetButton);
        this.saveButton = Button.builder(Component.literal("\u4fdd\u5b58\u914d\u7f6e"), btn -> this.saveInputsToConfig()).bounds(bX, btnY2, btnW, 20).build();
        this.myAddRenderableWidget(this.saveButton);
        this.summonButton = Button.builder(Component.literal("\u53ec\u5524\u6a21\u578b"), btn -> this.startSummon()).bounds(bX + (btnW + gap6), btnY2, btnW, 20).build();
        this.myAddRenderableWidget(this.summonButton);
        this.cancelButton = Button.builder(Component.literal("\u4e2d\u6b62"), btn -> {
            this.manager.stop();
            this.updateFromManager();
        }).bounds(bX + 2 * (btnW + gap6), btnY2, btnW, 20).build();
        this.cancelButton.active = false;
        this.myAddRenderableWidget(this.cancelButton);
    }

    private void savePreset() {
        List<String> cmds = this.collectCommands();
        if (cmds.isEmpty()) {
            this.setStatusMessage("\u00a7c\u81f3\u5c11\u8f93\u5165\u4e00\u884c\u6307\u4ee4", 0xFF5555);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen((Screen)new PresetSaveScreen(cmds, name -> {
            DisplayModelConfig.savePreset(name, cmds);
            this.setStatusMessage("\u00a7a\u9884\u8bbe\u5df2\u4fdd\u5b58: " + name, 0x55FF55);
            mc.setScreen((Screen)this);
        }));
    }

    private void loadPreset() {
        String[] presets = DisplayModelConfig.listPresets();
        if (presets.length == 0) {
            this.setStatusMessage("\u00a7e\u6ca1\u6709\u5df2\u4fdd\u5b58\u7684\u9884\u8bbe", 0xFFFF55);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen((Screen)new PresetLoadScreen(presets, name -> {
            List<String> cmds = DisplayModelConfig.loadPreset(name);
            if (!cmds.isEmpty()) {
                this.config.commandLines = new ArrayList<String>(cmds);
                DisplayModelScreen displayModelScreen = this;
                displayModelScreen.config.save();
                this.setStatusMessage("\u00a7a\u5df2\u8f7d\u5165\u9884\u8bbe: " + name + "\uff08" + cmds.size() + " \u6761\u6307\u4ee4\uff09", 0x55FF55);
            }
            mc.setScreen((Screen)this);
        }));
    }

    private List<String> collectCommands() {
        ArrayList<String> cmds = new ArrayList<String>();
        for (CommandRow row : this.commandRows) {
            String cmd = row.input != null ? row.input.getValue().trim() : "";
            if (cmd.isEmpty()) continue;
            cmds.add(cmd);
        }
        return cmds;
    }

    private EditBox createConfigInput(int x, int y, int width, String value, boolean intOnly, String filter) {
        EditBox box = new EditBox(this.font, x, y, width, 14, Component.literal(""));
        box.setValue(value);
        box.setMaxLength(intOnly ? 5 : 10);
        box.setFilter(s -> s.matches(filter));
        return box;
    }

    public void tick() {
        super.tick();
        for (CommandRow row : this.commandRows) {
            if (row.input == null) continue;
            row.input.tick();
        }
        if (this.placeDelayInput != null) {
            this.placeDelayInput.tick();
        }
        if (this.generationDelayInput != null) {
            this.generationDelayInput.tick();
        }
        if (this.entitySpacingInput != null) {
            this.entitySpacingInput.tick();
        }
        if (this.placeXInput != null) {
            this.placeXInput.tick();
        }
        if (this.placeYInput != null) {
            this.placeYInput.tick();
        }
        if (this.placeZInput != null) {
            this.placeZInput.tick();
        }
        if (this.viewRangeInput != null) {
            this.viewRangeInput.tick();
        }
        this.updateFromManager();
    }

    private void updateFromManager() {
        if (this.manager.isRunning()) {
            String msg = this.manager.getStatusMessage();
            if (msg != null && !msg.isEmpty()) {
                this.statusMessage = msg;
                int n = this.statusColor = msg.startsWith("\u00a7c") ? 0xFF5555 : 0x55FF55;
            }
            if (this.summonButton != null) {
                this.summonButton.setMessage(Component.literal((String)("\u653e\u7f6e\u4e2d " + this.manager.getCurrentIndex() + "/" + this.manager.getTotalCount())));
                this.summonButton.active = false;
            }
            if (this.cancelButton != null) {
                this.cancelButton.active = true;
            }
        } else {
            if (this.summonButton != null) {
                this.summonButton.setMessage(Component.literal("\u53ec\u5524\u6a21\u578b"));
                this.summonButton.active = true;
            }
            if (this.cancelButton != null) {
                this.cancelButton.active = false;
            }
        }
    }

    private void startSummon() {
        if (this.manager.isRunning()) {
            this.setStatusMessage("\u00a7e\u653e\u7f6e\u6b63\u5728\u8fdb\u884c\u4e2d.", 0xFFFF55);
            return;
        }
        ArrayList<String> cmds = new ArrayList<String>();
        for (CommandRow row : this.commandRows) {
            String cmd = row.input.getValue().trim();
            if (cmd.isEmpty()) continue;
            cmds.add(cmd);
        }
        if (cmds.isEmpty()) {
            this.setStatusMessage("\u00a7c\u8bf7\u81f3\u5c11\u8f93\u5165\u4e00\u884c /summon \u6307\u4ee4", 0xFF5555);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!player.isCreative()) {
            this.setStatusMessage("\u00a7c\u9700\u8981\u521b\u9020\u6a21\u5f0f", 0xFF5555);
            return;
        }
        int placeDelayMs = DisplayModelScreen.parseIntOrDefault(this.placeDelayInput, 50);
        int generationDelayMs = DisplayModelScreen.parseIntOrDefault(this.generationDelayInput, 50);
        double spacing = DisplayModelScreen.parseDoubleClamped(this.entitySpacingInput, 0.5, 0.0, 10.0);
        double px = DisplayModelScreen.parseDoubleOrDefault(this.placeXInput, 0.0);
        double py = DisplayModelScreen.parseDoubleOrDefault(this.placeYInput, 0.0);
        double pz = DisplayModelScreen.parseDoubleOrDefault(this.placeZInput, 0.0);
        double vr = DisplayModelScreen.parseDoubleOrDefault(this.viewRangeInput, 0.0);
        BlockPos fixedPos = px == 0.0 && py == 0.0 && pz == 0.0 ? player.blockPosition() : BlockPos.containing(px, py, pz);
        ArrayList<DisplayModelManager.CommandEntry> queue = new ArrayList<DisplayModelManager.CommandEntry>();
        for (String cmd : cmds) {
            queue.add(new DisplayModelManager.CommandEntry(cmd));
        }
        this.manager.start(queue, generationDelayMs, placeDelayMs, spacing, fixedPos, vr);
        if (this.manager.isRunning()) {
            this.setStatusMessage("\u00a7a\u5f00\u59cb\u653e\u7f6e\uff0c" + cmds.size() + " \u884c\u6307\u4ee4.", 0x55FF55);
            this.summonButton.setMessage(Component.literal("\u653e\u7f6e\u4e2d."));
            this.summonButton.active = false;
        }
    }

    private void saveInputsToConfig() {
        DisplayModelScreen.tryParseInt(this.placeDelayInput, v -> this.config.setPlaceDelay(v));
        DisplayModelScreen.tryParseInt(this.generationDelayInput, v -> this.config.setGenerationDelay(v));
        DisplayModelScreen.tryParseDouble(this.entitySpacingInput, v -> this.config.setEntitySpacing(Math.max(0.0, Math.min(10.0, v))));
        DisplayModelScreen.tryParseDouble(this.placeXInput, this.config::setPlaceX);
        DisplayModelScreen.tryParseDouble(this.placeYInput, this.config::setPlaceY);
        DisplayModelScreen.tryParseDouble(this.placeZInput, this.config::setPlaceZ);
        DisplayModelScreen.tryParseDouble(this.viewRangeInput, v -> this.config.setViewRange(Math.max(0.0, v)));
        this.config.commandLines = this.collectCommands();
        DisplayModelScreen displayModelScreen = this;
        displayModelScreen.config.save();
        this.config.guiX = (this.width - 480) / 2;
        this.config.guiY = (this.height - this.totalHeight) / 2;
        DisplayModelScreen displayModelScreen2 = this;
        displayModelScreen2.config.save();
        this.setStatusMessage("\u00a7a\u914d\u7f6e\u5df2\u4fdd\u5b58\uff08\u542b\u6307\u4ee4\u884c\uff09", 0x55FF55);
        Fku.LOGGER.info("[DisplayModel] \u914d\u7f6e\u5df2\u4fdd\u5b58");
    }

    public void onClose() {
        int totalHeight = 222 + (this.commandRows.size() - 1) * 24;
        this.config.guiX = (this.width - 480) / 2;
        this.config.guiY = (this.height - totalHeight) / 2;
        this.config.commandLines = this.collectCommands();
        DisplayModelScreen displayModelScreen = this;
        displayModelScreen.config.save();
        super.onClose();
    }

    private static void tryParseInt(EditBox input, IntConsumer consumer) {
        try {
            String val = input.getValue().trim();
            if (!val.isEmpty()) {
                consumer.accept(Integer.parseInt(val));
            }
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
    }

    private static void tryParseDouble(EditBox input, DoubleConsumer consumer) {
        try {
            String val = input.getValue().trim();
            if (!val.isEmpty()) {
                consumer.accept(Double.parseDouble(val));
            }
        }
        catch (NumberFormatException numberFormatException) {
            // ignored
        }
    }

    private static int parseIntOrDefault(EditBox input, int defaultValue) {
        try {
            String val = input.getValue().trim();
            return val.isEmpty() ? defaultValue : Integer.parseInt(val);
        }
        catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(EditBox input, double defaultValue) {
        try {
            String val = input.getValue().trim();
            return val.isEmpty() ? defaultValue : Double.parseDouble(val);
        }
        catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static double parseDoubleClamped(EditBox input, double defaultValue, double min, double max) {
        double val = DisplayModelScreen.parseDoubleOrDefault(input, defaultValue);
        return Math.max(min, Math.min(max, val));
    }

    private void setStatusMessage(String msg, int color) {
        this.statusMessage = msg;
        this.statusColor = color;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int x = (this.width - 480) / 2;
        if (this.config.guiX >= 0) {
            x = this.config.guiX;
        }
        int y = (this.height - this.totalHeight) / 2;
        if (this.config.guiY >= 0) {
            y = this.config.guiY;
        }
        guiGraphics.fill(x - 2, y - 2, x + 480 + 2, y + this.totalHeight + 2, -870178270);
        guiGraphics.renderOutline(x - 2, y - 2, 484, this.totalHeight + 4, -11184811);
        guiGraphics.drawString(this.font, "\u00a7l\u5b9e\u4f53\u6a21\u578b\u5c55\u793a", x + 10, y + 8, 0xFFFFFF);
        guiGraphics.drawString(this.font, "\u7c98\u8d34 /summon \u6307\u4ee4\uff08\u542b Passengers\uff09:", x + 10, y + 24, 0x888888);
        guiGraphics.fill(x + 10, y + 38, x + 480 - 10, y + 39, -12303292);
        int currentY = y + 44;
        for (CommandRow row : this.commandRows) {
            row.toggleBtn.render(guiGraphics, mouseX, mouseY, partialTick);
            row.input.render(guiGraphics, mouseX, mouseY, partialTick);
            if (row.input.getValue().isEmpty() && !row.input.isFocused()) {
                guiGraphics.drawString(this.font, "\u00a77/summon minecraft:block_display ~-0.5 ~-0.5 ~-0.5 {.}", x + 36, row.input.getY() + 2, 0x444444);
            }
            currentY += 24;
        }
        guiGraphics.fill(x + 10, (currentY += 14) - 4, x + 480 - 10, currentY - 3, -12303292);
        guiGraphics.drawString(this.font, "\u00a77\u914d\u7f6e\u9009\u9879:", x + 10, currentY, 0x888888);
        guiGraphics.drawString(this.font, "\u653e\u7f6e\u5ef6\u8fdf(ms):", x + 10, (currentY += 13) + 1, 0xAAAAAA);
        this.placeDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "\u751f\u6210\u95f4\u9694(ms):", x + 165, currentY + 1, 0xAAAAAA);
        this.generationDelayInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "\u5b9e\u4f53\u95f4\u8ddd(\u683c):", x + 320, currentY + 1, 0xAAAAAA);
        this.entitySpacingInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "\u53ef\u89c6\u8ddd\u79bb:", x + 10, (currentY += 22) + 1, 0xAAAAAA);
        this.viewRangeInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "\u00a77(0=\u9ed8\u8ba4)", x + 145, currentY + 1, 0x666666);
        guiGraphics.drawString(this.font, "\u653e\u7f6e\u5750\u6807:", x + 10, (currentY += 22) + 1, 0xAAAAAA);
        guiGraphics.drawString(this.font, "X", x + 72, currentY + 1, 0x888888);
        this.placeXInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "Y", x + 152, currentY + 1, 0x888888);
        this.placeYInput.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, "Z", x + 232, currentY + 1, 0x888888);
        this.placeZInput.render(guiGraphics, mouseX, mouseY, partialTick);
        this.writePosButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.clearPosButton.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.statusMessage.isEmpty()) {
            guiGraphics.drawString(this.font, this.statusMessage, x + 15, y + this.totalHeight - 62, this.statusColor);
        }
        this.openWebsiteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.savePresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.loadPresetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.saveButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.summonButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.cancelButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class CommandRow {
        EditBox input;
        Button toggleBtn;
        String savedValue = "";

        private CommandRow() {
        }
    }

    private static class PresetSaveScreen
    extends Screen {
        private final List<String> commands;
        private final Consumer<String> callback;
        private EditBox nameInput;

        PresetSaveScreen(List<String> commands, Consumer<String> callback) {
            super(Component.literal("\u4fdd\u5b58\u9884\u8bbe"));
            this.commands = commands;
            this.callback = callback;
        }

        protected void init() {
            int cx = this.width / 2;
            int cy = this.height / 2;
            this.addRenderableWidget(Button.builder(Component.literal("\u00a7c\u53d6\u6d88"), b -> this.onClose()).bounds(cx - 75, cy + 30, 70, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("\u00a7a\u4fdd\u5b58"), b -> {
                String name = this.nameInput.getValue().trim();
                if (!name.isEmpty()) {
                    this.callback.accept(name);
                }
            }).bounds(cx + 5, cy + 30, 70, 20).build());
            this.nameInput = new EditBox(this.font, cx - 70, cy - 10, 140, 18, Component.literal("\u9884\u8bbe\u540d"));
            this.nameInput.setMaxLength(64);
            this.addWidget(this.nameInput);
        }

        public void render(GuiGraphics g, int mx, int my, float pt) {
            this.renderBackground(g);
            g.drawString(this.font, "\u00a7l\u8f93\u5165\u9884\u8bbe\u540d\u79f0:", this.width / 2 - 50, this.height / 2 - 30, 0xFFFFFF);
            g.drawString(this.font, "\u00a77\u5171 " + this.commands.size() + " \u6761\u6307\u4ee4", this.width / 2 - 40, this.height / 2 + 12, 0x888888);
            this.nameInput.render(g, mx, my, pt);
            super.render(g, mx, my, pt);
        }

        public boolean keyPressed(int k, int sc, int mod) {
            if (k == 256) {
                this.onClose();
                return true;
            }
            if ((k == 257 || k == 335) && this.nameInput.isFocused()) {
                String name = this.nameInput.getValue().trim();
                if (!name.isEmpty()) {
                    this.callback.accept(name);
                }
                return true;
            }
            if (this.nameInput.isFocused()) {
                return this.nameInput.keyPressed(k, sc, mod);
            }
            return super.keyPressed(k, sc, mod);
        }

        public boolean isPauseScreen() {
            return false;
        }
    }

    private static class PresetLoadScreen
    extends Screen {
        private final String[] presets;
        private final Consumer<String> callback;
        private int scrollOffset = 0;

        PresetLoadScreen(String[] presets, Consumer<String> callback) {
            super(Component.literal("\u8f7d\u5165\u9884\u8bbe"));
            this.presets = presets;
            this.callback = callback;
        }

        protected void init() {
            int idx;
            int cx = this.width / 2;
            int cy = this.height / 2;
            int btnW = 120;
            int maxVis = Math.min(this.presets.length, 8);
            int startY = cy - maxVis * 12;
            for (int i = 0; i < maxVis && (idx = i + this.scrollOffset) < this.presets.length; ++i) {
                String name = this.presets[idx];
                this.addRenderableWidget(Button.builder(Component.literal((String)name), b -> this.callback.accept(name)).bounds(cx - btnW / 2, startY + i * 22, btnW, 20).build());
            }
            this.addRenderableWidget(Button.builder(Component.literal("\u00a7c\u5173\u95ed"), b -> this.onClose()).bounds(cx - 30, startY + maxVis * 22 + 8, 60, 20).build());
        }

        public void render(GuiGraphics g, int mx, int my, float pt) {
            this.renderBackground(g);
            g.drawString(this.font, "\u00a7l\u9009\u62e9\u9884\u8bbe:", this.width / 2 - 40, this.height / 2 - Math.min(this.presets.length, 8) * 22 / 2 - 20, 0xFFFFFF);
            super.render(g, mx, my, pt);
        }

        public boolean isPauseScreen() {
            return false;
        }

        public boolean mouseScrolled(double mx, double my, double delta) {
            this.scrollOffset = (int)Math.max(0.0, Math.min((this.presets.length - 1), this.scrollOffset - delta));
            this.rebuildWidgets();
            return true;
        }
    }

    @FunctionalInterface
    private static interface IntConsumer {
        public void accept(int var1);
    }

    @FunctionalInterface
    private static interface DoubleConsumer {
        public void accept(double var1);
    }
}

