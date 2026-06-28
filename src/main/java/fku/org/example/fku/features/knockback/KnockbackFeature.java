package fku.org.example.fku.features.knockback; /* water */

import fku.org.example.fku.features.quickswitch.QuickSwitchFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * KnockbackFeature — 自由击退方向主功能类
 *
 * ★ 职责：
 *   注册事件监听器，管理功能生命周期。
 *   在 ClientTickEvent 中调用 FakeRotationManager.tick() 恢复旋转。
 *
 * ★ 设计思想（实践论）：
 *   功能由三部分组成：
 *     1. Config（配置持久化：开关/模式/参数）
 *     2. MixinMultiPlayerGameMode（攻击拦截：注入假旋转）
 *     3. FakeRotationManager（旋转管理：发送/恢复旋转包）
 *     4. KnockbackDirectionCalculator（计算逻辑：各模式方向计算）
 *
 * ★ 兼容性：
 *   由于 Mixin 拦截了所有 MultiPlayerGameMode#attack() 调用，
 *   无论攻击来自手动操作还是杀戮光环，行为一致。
 *   旋转恢复延迟到下一 ClientTick，不影响连续攻击。
 */
@OnlyIn(Dist.CLIENT)
public class KnockbackFeature {

    /**
     * 初始化：加载配置
     * 由 Fku.java commonSetup 调用
     */
    public static void init() {
        KnockbackConfig.load();
    }

    /**
     * ClientTick：恢复假旋转（延迟一个 Tick）
     * 确保服务端已处理攻击包后再恢复原始旋转
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        FakeRotationManager.tick();
        QuickSwitchFeature.tick(); // ★ 秒切恢复定时器（照搬击退方向模式）
    }
}