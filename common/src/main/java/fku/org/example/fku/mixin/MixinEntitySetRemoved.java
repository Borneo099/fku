package fku.org.example.fku.mixin;

import fku.org.example.fku.features.killicon.KillIconFeature;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(value={Entity.class})
public abstract class MixinEntitySetRemoved {
    @Inject(method={"setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"}, at={@At(value="HEAD")})
    private void onSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity)this;
        if (self.m_9236_().f_46443_ && (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED)) {
            KillIconFeature.onEntityRemoved(self);
        }
    }
}

