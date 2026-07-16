package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.antipush.AntiPushFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MixinEntityPush — 拦截实体推动，实现防推功能
 */
@Mixin(Entity.class)
public abstract class MixinEntityPush {

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    public void onPush(Entity other, CallbackInfo ci) {
        if (!AntiPushFeature.isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        // 只拦截对本地玩家的推动
        if ((Object) this == mc.player) {
            ci.cancel();
        }
    }
}
