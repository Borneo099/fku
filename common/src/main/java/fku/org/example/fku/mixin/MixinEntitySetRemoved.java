package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.killicon.KillIconFeature;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听实体被移除（死亡），触发击杀图标
 * 参考：gd656killicon EntitySetRemovedMixin
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Entity.class)
public abstract class MixinEntitySetRemoved {

    @Inject(method = "setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("HEAD"))
    private void onSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) {
            if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) {
                KillIconFeature.onEntityRemoved(self);
            }
        }
    }
}
