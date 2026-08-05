package fku.org.example.fku.mixin;

import fku.org.example.fku.config.FkuConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 CheatUtils 的 IgnoreServerViewDistance / dontUnloadChunks 导致远处地图不加载的问题。
 *
 * CheatUtils 通过拦截服务端发来的 {@code ClientboundForgetLevelChunkPacket}（让客户端“忘记”/卸载某个区块的包），
 * 在开启忽略服务器视距时直接跳过原版处理，使远处区块永远不被卸载/刷新，于是地图停在旧状态；
 * 直到死亡触发整段区块重载才恢复。
 *
 * 这里注入 CheatUtils 自己的 {@code ChunkController.processForgetLevelChunkPacket}，
 * 当 fku 的开关开启时取消该方法并令其返回 false（不拦截），让原版照常卸载远处区块，
 * 从代码层面强制让 CheatUtils 的忽略服务器视距功能失效。
 *
 * 使用字符串 target + remap=false + @Pseudo：编译期不依赖 CheatUtils（可能未安装），
 * 运行期仅在 CheatUtils 存在时按类名/方法名织入。
 */
@Pseudo
@Mixin(targets = "com.zergatul.cheatutils.controllers.ChunkController", remap = false)
public abstract class MixinCheatutilsChunk {

    @Inject(
            method = "processForgetLevelChunkPacket",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void fkuDisableChunkBypass(CallbackInfoReturnable<Boolean> cir) {
        if (FkuConfig.disableCheatutilsChunkBypass != null && FkuConfig.disableCheatutilsChunkBypass.get()) {
            // 返回 false => CheatUtils 不拦截该包，原版 ClientPacketListener 正常卸载区块
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
