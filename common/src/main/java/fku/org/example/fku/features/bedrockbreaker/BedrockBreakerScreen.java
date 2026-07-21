package fku.org.example.fku.features.bedrockbreaker;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.KeyBindings;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.client.gui.GuiRenderHelper;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.HelperBlockListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class BedrockBreakerScreen
extends Screen {
    private static final int WIDTH = 290;
    private static final int HEIGHT = 375;
    private static final int ROW_HOTKEY = 30;
    private static final int ROW_TARGET = 60;
    private static final int ROW_REPLACE = 98;
    private static final int ROW_MODE = 136;
    private static final int ROW_TIMEOUT = 172;
    private static final int ROW_LEVER = 208;
    private static final int ROW_HELPER_SWITCH = 236;
    private static final int ROW_HELPER_LIST = 266;
    private static final int ROW_HINT = 310;
    private static final int ROW_BUTTON = 340;
    private EditBox targetBlockInput;
    private EditBox replaceBlockInput;
    private EditBox breakTimeoutInput;
    private EditBox extendTimeoutInput;
    private EditBox leverTimeoutInput;
    private Button hotkeyButton;
    private Button scanModeButton;
    private boolean listeningForKey = false;

    public BedrockBreakerScreen() {
        super(Component.literal((String)"\u57fa\u5ca9\u7834\u574f\u5668\u914d\u7f6e"));
    }

    protected void init() {
        super.init();
        int cx = (this.width - 290) / 2;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        String currentKeyName = this.getCurrentKeyDisplay();
        this.hotkeyButton = Button.builder(Component.literal((String)("\u70ed\u952e: " + currentKeyName)), btn -> {
            this.listeningForKey = true;
            btn.setMessage(Component.literal((String)"\u70ed\u952e: \u6309\u4e0b\u65b0\u952e."));
        }).bounds(cx + 10, this.cy(30), 120, 18).build();
        this.addRenderableWidget(this.hotkeyButton);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u91cd\u7f6e"), btn -> {
            KeyBindings.updateBedrockBreakerKey(InputConstants.Type.KEYSYM.getOrCreate(66));
            this.hotkeyButton.setMessage(Component.literal((String)"\u70ed\u952e: B"));
            this.listeningForKey = false;
        }).bounds(cx + 135, this.cy(30), 50, 18).build());
        this.targetBlockInput = new EditBox(this.font, cx + 68, this.cy(74), 130, 14, Component.literal((String)""));
        this.targetBlockInput.m_94144_(cfg.targetBlockId != null ? cfg.targetBlockId : "minecraft:bedrock");
        this.targetBlockInput.m_94199_(64);
        this.addRenderableWidget(this.targetBlockInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5199\u5165"), btn -> this.writeCrosshairBlockTo(this.targetBlockInput)).bounds(cx + 202, this.cy(73), 40, 16).build());
        String allBlocksLabel = "\u5168\u65b9\u5757: " + (cfg.allBlocks ? "\u5f00" : "\u5173");
        this.addRenderableWidget(Button.builder(Component.literal((String)allBlocksLabel), btn -> {
            cfg.setAllBlocks(!cfg.allBlocks);
            btn.setMessage(Component.literal((String)("\u5168\u65b9\u5757: " + (cfg.allBlocks ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(88), 80, 16).build());
        this.replaceBlockInput = new EditBox(this.font, cx + 68, this.cy(112), 130, 14, Component.literal((String)""));
        this.replaceBlockInput.m_94144_(cfg.replaceBlockId != null ? cfg.replaceBlockId : "");
        this.replaceBlockInput.m_94199_(64);
        this.addRenderableWidget(this.replaceBlockInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5199\u5165"), btn -> this.writeCrosshairBlockTo(this.replaceBlockInput)).bounds(cx + 202, this.cy(111), 40, 16).build());
        this.scanModeButton = Button.builder(Component.literal((String)("\u626b\u63cf\u6a21\u5f0f: " + (cfg.scanMode ? "\u5f00" : "\u5173"))), btn -> {
            cfg.setScanMode(!cfg.scanMode);
            btn.setMessage(Component.literal((String)("\u626b\u63cf\u6a21\u5f0f: " + (cfg.scanMode ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(136), 100, 16).build();
        this.addRenderableWidget(this.scanModeButton);
        this.breakTimeoutInput = new EditBox(this.font, cx + 68, this.cy(174), 40, 14, Component.literal((String)""));
        this.breakTimeoutInput.m_94144_(String.valueOf(cfg.breakTimeout));
        this.breakTimeoutInput.m_94199_(3);
        this.breakTimeoutInput.m_94153_(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.breakTimeoutInput);
        this.extendTimeoutInput = new EditBox(this.font, cx + 170, this.cy(174), 40, 14, Component.literal((String)""));
        this.extendTimeoutInput.m_94144_(String.valueOf(cfg.extendTimeout));
        this.extendTimeoutInput.m_94199_(2);
        this.extendTimeoutInput.m_94153_(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.extendTimeoutInput);
        this.leverTimeoutInput = new EditBox(this.font, cx + 68, this.cy(210), 40, 14, Component.literal((String)""));
        this.leverTimeoutInput.m_94144_(String.valueOf(cfg.leverBreakTimeout));
        this.leverTimeoutInput.m_94199_(3);
        this.leverTimeoutInput.m_94153_(s -> s.matches("\\d*"));
        this.addRenderableWidget(this.leverTimeoutInput);
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u8f85\u52a9\u65b9\u5757: " + (cfg.enableHelperBlocks ? "\u5f00" : "\u5173"))), btn -> {
            cfg.setEnableHelperBlocks(!cfg.enableHelperBlocks);
            btn.setMessage(Component.literal((String)("\u8f85\u52a9\u65b9\u5757: " + (cfg.enableHelperBlocks ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 10, this.cy(236), 100, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)("\u6e05\u7406\u8f85\u52a9\u5757: " + (cfg.cleanupHelpers ? "\u5f00" : "\u5173"))), btn -> {
            cfg.setCleanupHelpers(!cfg.cleanupHelpers);
            btn.setMessage(Component.literal((String)("\u6e05\u7406\u8f85\u52a9\u5757: " + (cfg.cleanupHelpers ? "\u5f00" : "\u5173"))));
        }).bounds(cx + 120, this.cy(236), 110, 16).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u7f16\u8f91\u8f85\u52a9\u65b9\u5757\u5217\u8868."), btn -> Minecraft.getInstance().setScreen((Screen)new HelperBlockListScreen(this))).bounds(cx + 68, this.cy(278), 150, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u4fdd\u5b58"), btn -> this.saveConfig()).bounds(cx + 80, this.cy(340), 60, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal((String)"\u5b8c\u6210"), btn -> Minecraft.getInstance().setScreen(new ClickGuiScreen())).bounds(cx + 160, this.cy(340), 60, 18).build());
    }

    private void writeCrosshairBlockTo(EditBox input) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.f_91077_ == null || mc.f_91077_.m_6662_() != HitResult.Type.BLOCK) {
            input.m_94144_("\u00a7c\u672a\u7784\u51c6\u65b9\u5757");
            return;
        }
        BlockHitResult hit = (BlockHitResult)mc.f_91077_;
        Block block = mc.f_91073_.m_8055_(hit.m_82425_()).m_60734_();
        ResourceLocation id = BuiltInRegistries.f_256975_.m_7981_(block);
        if (id != null) {
            input.m_94144_(id.toString());
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

    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key newKey;
        if (this.listeningForKey && (newKey = InputConstants.m_84827_(keyCode, scanCode)) != InputConstants.UNKNOWN) {
            KeyBindings.updateBedrockBreakerKey(newKey);
            String display = this.getCurrentKeyDisplay();
            this.hotkeyButton.setMessage(Component.literal((String)("\u70ed\u952e: " + display)));
            this.listeningForKey = false;
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    private void saveConfig() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        cfg.setTargetBlockId(this.targetBlockInput.m_94155_());
        cfg.setReplaceBlockId(this.replaceBlockInput.m_94155_());
        try {
            cfg.setBreakTimeout(Integer.parseInt(this.breakTimeoutInput.m_94155_()));
        }
        catch (Exception exception) {
            // ignored
        }
        try {
            cfg.setExtendTimeout(Integer.parseInt(this.extendTimeoutInput.m_94155_()));
        }
        catch (Exception exception) {
            // ignored
        }
        try {
            cfg.setLeverBreakTimeout(Integer.parseInt(this.leverTimeoutInput.m_94155_()));
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private int cy(int rowOffset) {
        return (this.height - 375) / 2 + rowOffset;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.fillGradient(guiGraphics);
        int cx = (this.width - 290) / 2;
        GuiRenderHelper.drawPanelBackground(guiGraphics, cx, this.cy(0), 290, 375, false);
        guiGraphics.drawString(this.font, "\u57fa\u5ca9\u7834\u574f\u5668\u914d\u7f6e", cx + 10, this.cy(8), 0xFFFFFF);
        guiGraphics.drawString(this.font, "\u76ee\u6807\u65b9\u5757:", cx + 10, this.cy(60), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(\u70b9\u51fb\u3010\u5199\u5165\u3011\u5199\u5165\u51c6\u661f\u5904\u65b9\u5757)", cx + 96, this.cy(88), 0x666666);
        guiGraphics.drawString(this.font, "\u66ff\u6362\u65b9\u5757:", cx + 10, this.cy(98), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(\u7a7a=\u4e0d\u66ff\u6362)", cx + 10, this.cy(126), 0x666666);
        guiGraphics.drawString(this.font, "\u00a77| \u626b\u63cf\u6a21\u5f0f\u81ea\u52a8\u626b\u63cf\u5468\u56f4\u76ee\u6807\u65b9\u5757", cx + 115, this.cy(137), 0x666666);
        guiGraphics.drawString(this.font, "\u7834\u574f\u8d85\u65f6:", cx + 10, this.cy(172), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(tick)", cx + 110, this.cy(172), 0x666666);
        guiGraphics.drawString(this.font, "\u4f38\u51fa\u8d85\u65f6:", cx + 126, this.cy(172), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(tick)", cx + 212, this.cy(172), 0x666666);
        guiGraphics.drawString(this.font, "\u62c9\u6746\u8d85\u65f6:", cx + 10, this.cy(208), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(tick)", cx + 110, this.cy(208), 0x666666);
        guiGraphics.drawString(this.font, "\u00a77| \u627e\u4e0d\u5230\u62c9\u6746\u4f4d\u7f6e\u65f6\u81ea\u52a8\u653e\u7f6e\u8f85\u52a9\u65b9\u5757", cx + 10, this.cy(254), 0x666666);
        guiGraphics.drawString(this.font, "\u8f85\u52a9\u65b9\u5757\u5217\u8868:", cx + 10, this.cy(266), 0xAAAAAA);
        guiGraphics.drawString(this.font, "\u00a77(\u9017\u53f7\u5206\u9694,\u4f18\u5148\u7ea7\u4ece\u524d\u5230\u540e)", cx + 10, this.cy(294), 0x666666);
        guiGraphics.drawString(this.font, "\u00a77\u4f7f\u7528\u65b9\u6cd5\uff1a\u770b\u5411\u76ee\u6807\u65b9\u5757\u6309\u70ed\u952e\uff08\u9ed8\u8ba4 B\uff09", cx + 10, this.cy(310), 0x888888);
        guiGraphics.drawString(this.font, "\u00a77\u9700\u624b\u6301\u6d3b\u585e\u548c\u62c9\u6746(\u5feb\u6377\u680f)\uff0c\u6709\u9550\u66f4\u5feb", cx + 10, this.cy(322), 0x888888);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(new ClickGuiScreen());
    }
}

