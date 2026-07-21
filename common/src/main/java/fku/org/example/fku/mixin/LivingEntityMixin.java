package fku.org.example.fku.mixin;

import fku.org.example.fku.api.ILivingEntityGui;
import fku.org.example.fku.features.healthtag.HealthTagManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
implements ILivingEntityGui {
    @Unique
    private boolean fku$isGuiRendering = false;

    @Override
    public void fku$setGuiRendering(boolean rendering) {
        this.fku$isGuiRendering = rendering;
    }

    @Override
    public boolean fku$isGuiRendering() {
        return this.fku$isGuiRendering;
    }

    @Inject(method={"handleEntityEvent"}, at={@At(value="HEAD")})
    private void onHandleEntityEvent(byte pId, CallbackInfo ci) {
        if (pId == 2) {
            HealthTagManager.onEntityHurt((LivingEntity)this);
        }
    }
}

