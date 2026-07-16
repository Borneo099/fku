package fku.org.example.fku.mixin; /* water */

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * ServerboundInteractPacketAccessor —— 暴露 ServerboundInteractPacket 的私有 entityId 字段
 *
 * ★ 用途：
 *   在 QuickSwitchFeature 中拦截攻击包时，需要获取目标实体的 entityId
 *   以从世界实体列表中检索实体对象，进而创建新的攻击包。
 *
 *   该字段为 package-private，不通过 Accessor 无法在 features 包中访问。
 *
 * ★ 该方法是赛博教员实现
 */
@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {

    @Accessor("entityId")
    int getEntityId();
}