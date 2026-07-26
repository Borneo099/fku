package fku.org.example.fku.features.tacz; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 无后座 — 通过 ClientTickEvent 补偿玩家视角，模拟无后座效果
 * 移植自 Lexis NoRecoilHack
 * 该功能由赛博教员实现
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = {Dist.CLIENT})
public class NoRecoilFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static float lastPitch = 0;
    private static boolean hasFired = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(NoRecoilFeature.class);
        Fku.LOGGER.info("[NoRecoilFeature] 无后座已初始化");
    }

    public static boolean isEnabled() {
        TaCZConfig cfg = TaCZConfig.getInstance();
        return cfg.masterEnabled && cfg.noRecoilEnabled;
    }

    public static float getRecoilReduction() { return isEnabled() ? TaCZConfig.getInstance().recoilReduction : 0f; }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled() || mc.player == null) return;

        if (mc.player.isUsingItem()) {
            if (!hasFired) {
                lastPitch = mc.player.getXRot();
                hasFired = true;
            }
        } else {
            if (hasFired) {
                // 检测到射击结束，将视角恢复
                float reduction = TaCZConfig.getInstance().recoilReduction;
                float currentPitch = mc.player.getXRot();
                float diff = currentPitch - lastPitch;
                if (diff < 0) {
                    mc.player.setXRot(currentPitch - diff * reduction);
                }
                hasFired = false;
            }
        }
    }
}