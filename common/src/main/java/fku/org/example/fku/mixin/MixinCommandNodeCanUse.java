package fku.org.example.fku.mixin; /* water */

import com.mojang.brigadier.tree.CommandNode;
import fku.org.example.fku.features.clientop.ClientOPFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MixinCommandNodeCanUse — 让客户端命令补全“假装自己是 OP”
 *
 * 真正权限由服务端控制，无法绕过。本 Mixin 仅 hook 命令节点的可用性判断
 * {@link CommandNode#canUse(Object)}：当 ClientOP 功能开启时恒返回 true，使客户端
 * 命令树中所有（含 OP）指令节点被视为“可用”——
 *   - 命令名不再标红（像真有 OP）
 *   - 输入 “/” 即列出全部指令（含 gamemode / give / op 等）
 *   - 参数补全正常
 * 仅影响本地显示与补全，服务端执行时仍按真实权限校验，不会越权。
 */
@OnlyIn(Dist.CLIENT)
@Mixin(value = CommandNode.class, remap = false)
public class MixinCommandNodeCanUse {

    @Inject(method = "canUse(Ljava/lang/Object;)Z", at = @At("HEAD"), cancellable = true)
    private void fku$opCanUse(Object source, CallbackInfoReturnable<Boolean> cir) {
        if (ClientOPFeature.isEnabled()) cir.setReturnValue(true);
    }
}
