package fku.org.example.fku.features.antipush; /* water */

import fku.org.example.fku.Fku;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * AntiPushFeature — 防推功能
 * 由 MixinEntityPush 拦截 Entity.push(Entity) 实现。
 * 开关状态始终从 Config 读取/保存（静默持久化）
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class AntiPushFeature {

    public static void init() {
        AntiPushConfig.load();
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }
    public static void setEnabled(boolean v) {
        AntiPushConfig cfg = AntiPushConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
    }
    public static boolean isEnabled() { return AntiPushConfig.getInstance().enabled; }
}
