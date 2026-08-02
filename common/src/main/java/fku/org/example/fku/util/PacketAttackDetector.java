package fku.org.example.fku.util; /* water */

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/**
 * PacketAttackDetector — 通过 dispatch(Handler) 检测 ServerboundInteractPacket 是否为攻击包
 *
 * ★ 用途：
 *   ServerboundInteractPacket.Action 是包内可见类型，外部无法直接引用其字段或类型。
 *   通过 dispatch(Handler) 回调机制，在 onAttack() 被调用时标记为攻击包。
 *
 * ★ 为何放在 util 包而非 mixin 包：
 *   避免 Mixin 的包访问限制导致 IllegalClassLoadError。
 *
 * ★ 该方法是赛博教员实现
 */
public class PacketAttackDetector implements ServerboundInteractPacket.Handler {

    private boolean attack = false;

    @Override
    public void onInteraction(InteractionHand hand) {
        // 交互包 → 非攻击
    }

    @Override
    public void onInteraction(InteractionHand hand, Vec3 pos) {
        // 交互包（带位置）→ 非攻击
    }

    @Override
    public void onAttack() {
        this.attack = true;
    }

    /** 检测指定包是否为攻击包 */
    public static boolean isAttack(ServerboundInteractPacket packet) {
        PacketAttackDetector detector = new PacketAttackDetector();
        packet.dispatch(detector);
        return detector.attack;
    }
}