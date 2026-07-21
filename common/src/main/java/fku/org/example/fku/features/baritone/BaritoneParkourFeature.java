package fku.org.example.fku.features.baritone;

import fku.org.example.fku.features.baritone.BaritoneConfig;
import fku.org.example.fku.util.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class BaritoneParkourFeature {
    private static boolean hackActive = false;
    private static final String[] SETTING_NAMES = new String[]{"allowBreak", "allowPlace", "allowSprint", "allowParkour", "allowParkourPlace", "allowInventory"};
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
        if (v) {
            BaritoneParkourFeature.onEnable();
        } else {
            BaritoneParkourFeature.onDisable();
        }
    }

    private static void onEnable() {
        if (hackActive) {
            return;
        }
        if (!BaritoneBridge.isAvailable()) {
            return;
        }
        for (int i = 0; i < SETTING_NAMES.length; ++i) {
            BaritoneParkourFeature.savedValues[i] = BaritoneBridge.readBooleanSetting(SETTING_NAMES[i]);
            boolean wanted = BaritoneParkourFeature.getSetting(i);
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], wanted);
            BaritoneParkourFeature.currentApplied[i] = wanted;
        }
        hackActive = true;
    }

    private static void onDisable() {
        if (!hackActive) {
            return;
        }
        if (!BaritoneBridge.isAvailable()) {
            return;
        }
        for (int i = 0; i < SETTING_NAMES.length; ++i) {
            if (savedValues[i] == null) continue;
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], savedValues[i]);
            BaritoneParkourFeature.savedValues[i] = null;
        }
        hackActive = false;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!hackActive || mc.player == null) {
            return;
        }
        BaritoneConfig cfg = BaritoneConfig.getInstance();
        if (!cfg.parkourEnabled) {
            return;
        }
        boolean needSave = false;
        for (int i = 0; i < SETTING_NAMES.length; ++i) {
            boolean wanted = BaritoneParkourFeature.getSetting(i);
            if (wanted == currentApplied[i]) continue;
            BaritoneBridge.writeBooleanSetting(SETTING_NAMES[i], wanted);
            BaritoneParkourFeature.currentApplied[i] = wanted;
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

