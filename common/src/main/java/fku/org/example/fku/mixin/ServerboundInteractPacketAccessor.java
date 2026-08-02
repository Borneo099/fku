package fku.org.example.fku.mixin; /* water */

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * ServerboundInteractPacketAccessor —— 暴露 ServerboundInteractPacket 的私有字段
 *
 * ★ 用途：
 *   - entityId：在 QuickSwitchFeature 中拦截攻击包时获取目标实体
 *
 * ★ 注意：
 *   不提供 getAction() 访问器，因为 Action 接口是包内可见类型，
 *   无法从外部引用。如需检测攻击类型，使用 packet.dispatch(Handler) 方式。
 *
 * ★ 该方法是赛博教员实现
 */
@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {

    @Accessor("entityId")
    int getEntityId();
}