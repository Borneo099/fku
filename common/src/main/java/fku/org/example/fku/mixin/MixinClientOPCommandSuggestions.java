package fku.org.example.fku.mixin; /* water */

import com.mojang.brigadier.suggestion.Suggestions;
import fku.org.example.fku.features.clientop.ClientOPFeature;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MixinClientOPCommandSuggestions — 本地 OP 指令补全注入
 *
 * 说明：真正的 OP 权限由服务端控制，客户端无法绕过。本 Mixin 仅在本地
 * 指令补全阶段，把“需 OP 权限”的指令合并进补全列表（或服务端列表），
 * 不修改任何发包逻辑，不向服务端发送权限请求。
 *
 *  - updateCommandInfo HEAD：实时捕获当前输入文本与输入框，供前缀匹配与预览定位
 *  - updateCommandInfo TAIL：无权限时客户端指令树不含 OP 指令、原版补全为空，
 *    此时用本地补全（指令名 + 参数：实体ID/物品ID/玩家名/选择器等）接管显示
 *  - sortSuggestions RETURN：把本地 OP 指令合并进排序后的补全列表
 */
@OnlyIn(Dist.CLIENT)
@Mixin(CommandSuggestions.class)
public abstract class MixinClientOPCommandSuggestions {

    @Shadow
    private EditBox input;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private void updateUsageInfo() {}

    @Inject(method = "updateCommandInfo", at = @At("HEAD"))
    private void fku$captureInput(CallbackInfo ci) {
        if (!ClientOPFeature.isEnabled()) return;
        try {
            ClientOPFeature.currentInput = this.input.getValue();
            ClientOPFeature.inputBox = this.input;
        } catch (Exception ignored) {}
    }

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void fku$localArgSuggestions(CallbackInfo ci) {
        if (!ClientOPFeature.isEnabled()) return;
        try {
            String text = this.input.getValue();
            if (text == null || !text.startsWith("/") || !this.input.isFocused()) return;
            // 原版（客户端指令树，已注入 OP 节点）已能给出补全时不接管
            if (this.pendingSuggestions == null || !this.pendingSuggestions.isDone()) return;
            if (!this.pendingSuggestions.join().isEmpty()) return;
            Suggestions local = ClientOPFeature.buildLocalSuggestions(text);
            if (local == null || local.isEmpty()) return;
            // 用本地参数补全替换后重走原版显示流程（updateUsageInfo → showSuggestions → sortSuggestions）
            this.pendingSuggestions = CompletableFuture.completedFuture(local);
            this.updateUsageInfo();
        } catch (Exception ignored) {}
    }
}
