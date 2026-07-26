package fku.org.example.fku.mixin; /* water */

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
//绕过opmod的发包 功能 来自：Karucn / jks
/**
 * 绕过 OpMod 的模组列表检测。
 *
 * 在 PacketSendModList 构造完成后，把整个 modIds 替换成伪装列表：用合法
 * 可见的伪装 id（opmod / opmod_1 / opmod_2 ...）填满，保持与原列表相同长度。
 * 这样所有真实模组（含 fku 及其它模组）的 id 都被隐藏，且列表只含合法字符串，
 * 不会让 opmod 服务端在登记 / 计算 ModListDiff 时因收到非法（如不可见空白）
 * 字符串而中断登录流程（表现为卡在「加入世界中」）。
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.network.PacketSendModList", remap = false)
public abstract class MixinPacketSendModList {

    @Shadow private Set<String> modIds;

    /**
     * 伪装用的 modId。整份列表会被替换成仅包含合法字符串的伪装列表，
     * 因此绝不可使用不可见空白字符（如 ㅤ）——opmod 服务端在登记 / 计算
     * ModListDiff 时若收到非法字符串可能中断登录流程，表现为卡在「加入世界中」
     * （再叠加 ConnectionMixin 的 3600s 超时，便永久卡死）。
     */
    private static final String FAKE_MOD_ID = "§";

    @Inject(method = "<init>(Ljava/util/Set;)V", at = @At("RETURN"), remap = false)
    private void onInit(Set<String> originalModIds, CallbackInfo ci) {
        try {
            if (modIds == null) return;
            int originalSize = (originalModIds != null) ? originalModIds.size() : modIds.size();
            if (originalSize <= 0) return;
            // 用合法伪装 id 填满整个列表：隐藏全部真实模组，且所有条目都是
            // 合法可见字符串（opmod / opmod_1 / opmod_2 ...），不触发服务端异常。
            Set<String> fakeModIds = new TreeSet<>();
            for (int i = 0; i < originalSize; i++) {
                fakeModIds.add(i == 0 ? FAKE_MOD_ID : FAKE_MOD_ID + "？！" + i + "我真没开挂" + i + "！？");
            }
            // 直接变更集合内容（modIds 本质是可变 TreeSet，final 修饰的是引用而非对象）
            modIds.clear();
            modIds.addAll(fakeModIds);
        } catch (Throwable t) {
            // 任何异常都不应阻断加入流程：保留原列表即可。
        }
    }
}