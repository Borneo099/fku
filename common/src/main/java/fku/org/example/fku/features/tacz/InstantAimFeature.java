package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

/**
 * 瞬镜 — 通过反射设置 TaCZ 的开镜状态，跳过开镜动画
 * 移植自 Lexis InstantAimHack
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class InstantAimFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Field taczDataField = null;
    private static Field taczAimingField = null;
    private static boolean reflectionFailed = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(InstantAimFeature.class);
        Fku.LOGGER.info("[InstantAimFeature] 瞬镜已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.instantAimEnabled;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null) return;
        if (reflectionFailed) return;

        LocalPlayer player = mc.player;
        if (player.isUsingItem()) {
            try {
                if (taczDataField == null) {
                    for (Field f : player.getClass().getDeclaredFields()) {
                        if (f.getType().getName().equals("com.tacz.guns.client.gameplay.LocalPlayerDataHolder")) {
                            f.setAccessible(true); taczDataField = f; break;
                        }
                    }
                    if (taczDataField == null) { reflectionFailed = true; return; }
                }
                Object data = taczDataField.get(player);
                if (data == null) return;
                if (taczAimingField == null) {
                    taczAimingField = data.getClass().getDeclaredField("clientIsAiming");
                    taczAimingField.setAccessible(true);
                }
                taczAimingField.set(data, true);
            } catch (Exception e) {
                reflectionFailed = true;
            }
        }
    }
}