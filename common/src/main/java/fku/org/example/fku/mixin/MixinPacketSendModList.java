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
 * 可见的伪装 id（FAKEMODID§、FAKEMODID§§ ...）填满，保持与原列表相同长度。
 * 这样所有真实模组（含 fku 及其它模组）的 id 都被隐藏，且列表只含合法字符串，
 * 不会让 opmod 服务端在登记 / 计算 ModListDiff 时因收到非法（如不可见空白）
 * 字符串而中断登录流程（表现为卡在「加入世界中」）。
 */
@Pseudo
@Mixin(targets = "lbxrman.mymod.opmod.network.PacketSendModList", remap = false)
public abstract class MixinPacketSendModList {

    @Shadow private Set<String> modIds;

    /**
     * 伪装用的 modId（此处不再使用，仅作保留）。
     */
    private static final String FAKE_MOD_ID = "§";

    @Inject(method = "<init>(Ljava/util/Set;)V", at = @At("RETURN"), remap = false)
    private void onInit(Set<String> originalModIds, CallbackInfo ci) {
        try {
            if (modIds == null) return;
            int originalSize = (originalModIds != null) ? originalModIds.size() : modIds.size();
            if (originalSize <= 0) return;

            // 用“FAKEMODID” + 递增数量的 § 填满整个列表
            Set<String> fakeModIds = new TreeSet<>();
            for (int i = 0; i < originalSize; i++) {
                // 生成 i+1 个 §（i=0 时得到一个 §，i=1 时得到两个 §，以此类推）
                String sectionRepeated = repeatChar('§', i + 1);
                fakeModIds.add(i == 0 ? FAKE_MOD_ID : FAKE_MOD_ID + sectionRepeated);
            }

            // 直接变更集合内容（modIds 本质是可变 TreeSet，final 修饰的是引用而非对象）
            modIds.clear();
            modIds.addAll(fakeModIds);
        } catch (Throwable t) {
            // 任何异常都不应阻断加入流程：保留原列表即可。
        }
    }

    /**
     * 重复字符 c 共 count 次（兼容 Java 8）
     */
    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}