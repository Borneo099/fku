package fku.org.example.fku.mixin;

import fku.org.example.fku.features.spear.SpearChargeFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 矛之冲锋·原版模式：
 * LocalPlayer 在 onGround=true 时 travel() 会用输入加速度(xxa/zza)覆盖 setDeltaMovement 的水平分量，
 * 导致站地上无法朝目标冲锋（必须跳/飞才生效）。本 mixin 在 travel() 方法头部、travel 计算之前强制
 * onGround=false，使 travel 走空中分支、直接使用我们设置的速度向量——站地/蹲/飞/跳均可冲。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"))
    private void fku_travelHead(net.minecraft.world.phys.Vec3 pTravelVec, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (Object) this == mc.player && SpearChargeFeature.isVanillaCharging()) {
            ((LivingEntity) (Object) this).setOnGround(false);
        }
    }
}
