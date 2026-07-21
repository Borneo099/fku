package fku.org.example.fku.mixin;

import fku.org.example.fku.features.antipush.AntiPushFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Entity.class})
public abstract class MixinEntityPush {
    @Inject(method={"push(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void onPush(Entity other, CallbackInfo ci) {
        if (!AntiPushFeature.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (this == mc.player) {
            ci.cancel();
        }
    }
}

