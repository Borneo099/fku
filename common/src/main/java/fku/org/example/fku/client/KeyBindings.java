package fku.org.example.fku.client;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyBindings {
    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping("key.fku.open_gui", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 344, "key.categories.fku");
    public static final KeyMapping BEDROCK_BREAKER_KEY = new KeyMapping("key.fku.bedrock_breaker", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 66, "key.categories.fku");

    private static void loadKeyFromConfig() {
        try {
            InputConstants.Key configKey;
            String keyName = (String)FkuConfig.guiKey.get();
            if (keyName != null && !keyName.isEmpty() && (configKey = InputConstants.getKey(keyName)) != InputConstants.UNKNOWN) {
                OPEN_GUI_KEY.setKey(configKey);
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private static void loadBedrockBreakerKeyFromConfig() {
        try {
            InputConstants.Key configKey;
            String keyName = BedrockBreakerConfig.getInstance().triggerKey;
            if (keyName != null && !keyName.isEmpty() && (configKey = InputConstants.getKey(keyName)) != InputConstants.UNKNOWN) {
                BEDROCK_BREAKER_KEY.setKey(configKey);
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }

    public static void updateKeyBinding(InputConstants.Key newKey) {
        OPEN_GUI_KEY.setKey(newKey);
        FkuConfig.guiKey.set(newKey.getName());
        KeyMapping.resetMapping();
    }

    public static void updateBedrockBreakerKey(InputConstants.Key newKey) {
        BEDROCK_BREAKER_KEY.setKey(newKey);
        BedrockBreakerConfig.getInstance().setTriggerKey(newKey.getName());
        KeyMapping.resetMapping();
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI_KEY);
        event.register(BEDROCK_BREAKER_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        if (OPEN_GUI_KEY.consumeClick()) {
            mc.setScreen(new ClickGuiScreen());
        }
        if (BEDROCK_BREAKER_KEY.consumeClick()) {
            BedrockBreakerManager.getInstance().process();
        }
    }

    static {
        KeyBindings.loadKeyFromConfig();
        KeyBindings.loadBedrockBreakerKeyFromConfig();
    }
}

