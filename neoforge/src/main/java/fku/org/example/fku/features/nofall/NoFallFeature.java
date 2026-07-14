package fku.org.example.fku.features.nofall; /* water */

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.flight.FlightFeature;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * NoFallFeature — 防摔功能
 *
 * ★ 职责：
 *   拦截 LivingFallEvent，在掉落距离超过 minFallDistance 时取消伤害。
 *   可配置仅飞行时保护，或完全免疫掉落。
 *
 * ★ 参考来源：
 *   Meteor Client NoFall 模块
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class NoFallFeature {

    /** ★ 从配置文件静默恢复开关状态 */
    public static void init() {
        NoFallConfig.load();
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }

    public static void setEnabled(boolean val) {
        NoFallConfig cfg = NoFallConfig.getInstance();
        cfg.enabled = val;
        cfg.save();
    }

    public static boolean isEnabled() { return NoFallConfig.getInstance().enabled; }

    /**
     * LivingFallEvent — 拦截掉落伤害
     */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!isEnabled()) return;

        // ★ 只处理玩家
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player)) return;
        // 客户端也处理（客户端模组需要的本地响应）

        NoFallConfig cfg = NoFallConfig.getInstance();

        // ★ 仅飞行保护：只有飞行激活时才取消
        if (cfg.onlyWhenFlying && !FlightFeature.isFlightActive()) return;

        // ★ 低于最小高度不保护
        if (event.getDistance() < (float) cfg.minFallDistance) return;

        // ★ 取消掉落伤害
        if (cfg.immune) {
            event.setCanceled(true);
        }
    }
}
