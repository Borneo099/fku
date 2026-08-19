package fku.org.example.fku.features.antiknockback; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * AntiKnockbackFeature — 防击退（玩家被击退时免疫/减弱）
 *
 * ★ 实现原理：
 *   拦截服务端下发的击退 —— Forge 在玩家即将被施加击退时触发 LivingKnockBackEvent。
 *   - FULL 模式：直接取消事件，击退向量清零，玩家完全不动。
 *   - REDUCE 模式：按比例缩放击退强度（strength 越大，剩余击退越少）。
 *
 * ★ 注意：
 *   客户端取消/缩放该事件即可生效，无需服务端配合，是最稳定的防击退方案。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AntiKnockbackFeature {

    public static void init() {
        AntiKnockbackConfig.load();
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }
    public static void setEnabled(boolean v) {
        AntiKnockbackConfig cfg = AntiKnockbackConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
    }
    public static boolean isEnabled() { return AntiKnockbackConfig.getInstance().enabled; }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return; // 仅保护自己

        AntiKnockbackConfig cfg = AntiKnockbackConfig.getInstance();
        switch (cfg.getMode()) {
            case FULL -> event.setCanceled(true);
            case REDUCE -> {
                float remain = 1.0f - cfg.strength; // 剩余比例
                if (remain <= 0.0f) {
                    event.setCanceled(true);
                } else {
                    event.setStrength(event.getStrength() * remain);
                    event.setRatioX(event.getRatioX() * remain);
                    event.setRatioZ(event.getRatioZ() * remain);
                }
            }
        }
    }
}
