package fku.org.example.fku.features.duplicator;

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DuplicatorFeature {

    private static boolean initialized = false;
    private static boolean enabled = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        DuplicatorManager.registerEventHandlers();
        DuplicatorConfig.getInstance();
        Fku.LOGGER.info("[Duplicator] 功能已初始化");
    }

    public static void toggle() { setEnabled(!enabled); }
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) {
        enabled = v;
        if (v) {
            Minecraft mc = Minecraft.getInstance();
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§7[复制] §a三叉戟复制已开启，请将三叉戟放入热栏"), false);
        } else {
            DuplicatorManager.getInstance().reset();
        }
    }

    public static boolean isRunning() { return DuplicatorManager.getInstance().isRunning(); }
}
