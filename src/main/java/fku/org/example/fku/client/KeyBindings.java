package fku.org.example.fku.client;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    // 公开的按键映射对象
    public static final KeyMapping OPEN_GUI_KEY;
    /** 基岩破坏器触发热键，默认 B 键 */
    public static final KeyMapping BEDROCK_BREAKER_KEY;

    static {
        OPEN_GUI_KEY = new KeyMapping(
                "key.fku.open_gui",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.fku"
        );
        BEDROCK_BREAKER_KEY = new KeyMapping(
                "key.fku.bedrock_breaker",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.fku"
        );
        // 从配置文件加载自定义按键
        loadKeyFromConfig();
        loadBedrockBreakerKeyFromConfig();
    }

    /**
     * 从 GUI 配置读取按键并设置到 KeyMapping 中
     */
    private static void loadKeyFromConfig() {
        try {
            String keyName = FkuConfig.guiKey.get();
            if (keyName != null && !keyName.isEmpty()) {
                InputConstants.Key configKey = InputConstants.getKey(keyName);
                if (configKey != InputConstants.UNKNOWN) {
                    OPEN_GUI_KEY.setKey(configKey);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * 从基岩破坏器 JSON 配置文件读取热键
     */
    private static void loadBedrockBreakerKeyFromConfig() {
        try {
            String keyName = BedrockBreakerConfig.getInstance().triggerKey;
            if (keyName != null && !keyName.isEmpty()) {
                InputConstants.Key configKey = InputConstants.getKey(keyName);
                if (configKey != InputConstants.UNKNOWN) {
                    BEDROCK_BREAKER_KEY.setKey(configKey);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * 更新按键绑定：同时设置 KeyMapping 并保存到配置文件
     */
    public static void updateKeyBinding(InputConstants.Key newKey) {
        OPEN_GUI_KEY.setKey(newKey);
        FkuConfig.guiKey.set(newKey.getName());
        KeyMapping.resetMapping();
    }

    /**
     * 更新基岩破坏器热键
     */
    public static void updateBedrockBreakerKey(InputConstants.Key newKey) {
        BEDROCK_BREAKER_KEY.setKey(newKey);
        BedrockBreakerConfig.getInstance().setTriggerKey(newKey.getName());
        KeyMapping.resetMapping();
    }

    /**
     * 注册按键映射
     */
    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI_KEY);
        event.register(BEDROCK_BREAKER_KEY);
    }

    /**
     * 监听按键输入
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) return;

        // GUI 开关
        if (OPEN_GUI_KEY.consumeClick()) {
            mc.setScreen(new ClickGuiScreen());
        }

        // 基岩破坏器触发：看向基岩时按热键
        if (BEDROCK_BREAKER_KEY.consumeClick()) {
            BedrockBreakerManager.getInstance().process();
        }
    }
}