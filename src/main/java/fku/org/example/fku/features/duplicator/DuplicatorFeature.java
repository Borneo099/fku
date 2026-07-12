package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

/**
 * 三叉戟复制工具 — 功能注册与开关
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DuplicatorFeature {

    private static boolean initialized = false;
    private static boolean enabled = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        DuplicatorManager.registerEventHandlers();
        DuplicatorConfig.load();
    }

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean v) {
        enabled = v;
        if (v) {
            DuplicatorManager.getInstance().start(Minecraft.getInstance());
        } else {
            DuplicatorManager.getInstance().stop();
        }
    }

    public static void toggle() { setEnabled(!enabled); }
}
