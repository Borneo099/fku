package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.flight.FlightFeature;
import fku.org.example.fku.features.nofall.NoFallFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * NoFallPacketMixin — 防摔落地伪造（参考 Meteor Client NoFallPacketMixin）
 *
 * ★ 根因：
 *   原 NoFall 仅取消客户端 LivingFallEvent，但掉落伤害由服务端判定（含内置/联机服务器），
 *   客户端的 LivingFallEvent 取消无效 → 照样摔伤。
 *
 * ★ 修复：
 *   在 Connection.send 处把"正在下落"的 ServerboundMovePlayerPacket 替换为 onGround=true 的同类型包，
 *   让服务端误以为玩家已落地，从而不施加掉落伤害。对任意服务器（含联机）生效。
 *
 * ★ 注意：
 *   1) ServerboundMovePlayerPacket.onGround 是 final 字段，无法运行期直接改写（会抛 IllegalAccessError），
 *      故采用"重建等价包"的方式。
 *   2) @ModifyArg 必须作用于 INVOKE 指令，不能放在方法 HEAD；改用 @ModifyVariable 替换入参局部变量。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Connection.class)
public abstract class NoFallPacketMixin {

    @ModifyVariable(
        method = "send(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At(value = "HEAD"),
        index = 1,
        argsOnly = true
    )
    private Packet<?> fku$modifyNoFallPacket(Packet<?> packet) {
        if (!NoFallFeature.isEnabled() || !NoFallFeature.isImmune()) return packet;
        if (!(packet instanceof ServerboundMovePlayerPacket movePkt)) return packet;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return packet;

        // 仅飞行保护：只有飞行激活时才伪造落地（其余情况交给自然摔伤）
        if (NoFallFeature.onlyWhenFlying() && !FlightFeature.isFlightActive()) return packet;

        // 仅真正下落（竖直速度 <= -0.5）时伪造，避免干扰跳跃/上升
        if (player.getDeltaMovement().y > -0.5) return packet;
        if (movePkt.isOnGround()) return packet;

        // 重建 onGround=true 的同类型包（onGround 为 final，无法直接改写；基类为抽象类不可实例化）
        if (movePkt instanceof ServerboundMovePlayerPacket.Pos p) {
            return new ServerboundMovePlayerPacket.Pos(p.getX(0.0), p.getY(0.0), p.getZ(0.0), true);
        } else if (movePkt instanceof ServerboundMovePlayerPacket.PosRot pr) {
            return new ServerboundMovePlayerPacket.PosRot(pr.getX(0.0), pr.getY(0.0), pr.getZ(0.0), pr.getYRot(0.0f), pr.getXRot(0.0f), true);
        } else if (movePkt instanceof ServerboundMovePlayerPacket.Rot r) {
            return new ServerboundMovePlayerPacket.Rot(r.getYRot(0.0f), r.getXRot(0.0f), true);
        }
        return packet;
    }
}
