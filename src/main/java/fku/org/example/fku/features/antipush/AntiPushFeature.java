package fku.org.example.fku.features.antipush; /* water */

import fku.org.example.fku.Fku;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

/**
 * AntiPushFeature — 防推功能
 * 由 MixinEntityPush 拦截 Entity.push(Entity) 实现。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AntiPushFeature {

    private static boolean enabled = false;

    /** ★ 从配置文件静默恢复开关状态 */
    public static void init() {
        AntiPushConfig.load();
        if (AntiPushConfig.getInstance().enabled) {
            enabled = true;
        }
    }

    public static void toggleEnabled() { setEnabled(!enabled); }
    public static void setEnabled(boolean v) { enabled = v; AntiPushConfig.getInstance().setEnabled(v); }
    public static boolean isEnabled() { return enabled; }
}
