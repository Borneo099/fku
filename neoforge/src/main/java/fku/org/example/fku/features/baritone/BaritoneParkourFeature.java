package fku.org.example.fku.features.baritone;

import fku.org.example.fku.Fku;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Baritone 跑酷模式
 * <p>
 * 启用时自动覆盖 Baritone 的寻路设置（允许跑酷、疾跑、拆方块等），
 * 禁用时恢复原始设置。
 * <p>
 * 参考：lexis.Hack.Hacks.Baritone.BaritoneParkourHack
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class BaritoneParkourFeature {

    private static boolean hackActive = false;
    private static final String[] SETTING_NAMES = {
            "allowBreak", "allowPlace", "allowSprint",
            "allowParkour", "allowParkourPlace", "allowInventory"
    };
    private static final Boolean[] savedValues = new Boolean[SETTING_NAMES.length];
    private static final boolean[] currentApplied = new boolean[SETTING_NAMES.length];

    private static boolean getSetting(int idx) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        return switch (idx) {
            case 0 -> cfg.allowBreak;
            case 1 -> cfg.allowPlace;
            case 2 -> cfg.allowSprint;
            case 3 -> cfg.allowParkour;
            case 4 -> cfg.allowParkourPlace;
            case 5 -> cfg.allowInventory;
            default -> false;
        };
    }

    public static boolean isEnabled() {
        return BaritoneConfig.getInstance().parkourEnabled;
    }

    public static void setEnabled(boolean v) {
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        cfg.parkourEnabled = v;
        cfg.save();
        if (v) onEnable();
        else onDisable();
    }

    private static void onEnable() {
        if (hackActive) return;
        if (!BaritoneBridge.isAvailable()) return;

        for (int i = 0; i < SETTING_NAMES.length; i++) {
            savedValues[i] = BaritoneBridge.readBooleanSetting(SETTING_NAMES[i]);
            boolean wanted = getSetting(i);
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], wanted);
            currentApplied[i] = wanted;
        }
        hackActive = true;
    }

    private static void onDisable() {
        if (!hackActive) return;
        if (!BaritoneBridge.isAvailable()) return;

        for (int i = 0; i < SETTING_NAMES.length; i++) {
            if (savedValues[i] == null) continue;
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], savedValues[i]);
            savedValues[i] = null;
        }
        hackActive = false;
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!hackActive || mc.player == null) return;

        BaritoneConfig cfg = BaritoneConfig.getInstance();
        if (!cfg.parkourEnabled) return;

        boolean needSave = false;
        for (int i = 0; i < SETTING_NAMES.length; i++) {
            boolean wanted = getSetting(i);
            if (wanted == currentApplied[i]) continue;
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], wanted);
            currentApplied[i] = wanted;
            needSave = true;
        }
        if (needSave) {
            cfg.save();
        }
    }

    static {
        hackActive = false;
    }
}
