package fku.org.example.fku.client;

import com.mojang.blaze3d.platform.InputConstants;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.config.FkuConfig;
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

    static {
        OPEN_GUI_KEY = new KeyMapping(
                "key.fku.open_gui",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.fku"
        );
        // 从配置文件加载自定义按键
        loadKeyFromConfig();
    }

    /**
     * 从配置读取按键并设置到 KeyMapping 中
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
     * 更新按键绑定：同时设置 KeyMapping 并保存到配置文件
     */
    public static void updateKeyBinding(InputConstants.Key newKey) {
        // 1. 设置到活按键映射
        OPEN_GUI_KEY.setKey(newKey);
        // 2. 保存名称到配置文件
        FkuConfig.guiKey.set(newKey.getName());
        // 3. 强制刷新按键映射管理器（关键！）
        //    KeyMapping.resetMapping() 会重新加载所有按键映射，我们手动调用一次
        KeyMapping.resetMapping();
    }

    /**
     * 注册按键映射
     */
    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI_KEY);
    }

    /**
     * 监听按键输入，打开 GUI
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // 只在自己世界中且没有打开任何界面时响应
        if (Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null) {
            if (OPEN_GUI_KEY.consumeClick()) {
                Minecraft.getInstance().setScreen(new ClickGuiScreen());
            }
        }
    }
}