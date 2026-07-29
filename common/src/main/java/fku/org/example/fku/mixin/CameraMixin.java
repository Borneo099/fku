package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.freecam.FreecamManager;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ★ 相机位置/旋转覆盖 Mixin（修复版）
 *
 * 当 FreecamManager 激活时，使用 @Inject HEAD + cancellable 完全接管
 * Camera.setup，阻止默认方法用玩家位置覆盖相机位置，彻底消除屏幕抖动。
 *
 * 参考自 Lexis FreeCamHack 的相机控制逻辑
 * 该 Mixin 由赛博教员实现
 */
@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow protected float xRot;
    @Shadow protected float yRot;

    @Shadow protected void setPosition(double x, double y, double z) {}
    @Shadow protected void setRotation(float yRot, float xRot) {}

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(BlockGetter level, Entity cameraEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (FreecamManager.isActive()) {
            // ★ 手动设置相机关键字段，避免默认 setup 方法用玩家位置覆盖
            this.entity = cameraEntity;
            this.eyeHeightOld = this.eyeHeight;
            this.eyeHeight = 0.0f; // 自由相机无眼高，设为0防止偏移

            // ★ 使用插值位置/旋转，实现60fps平滑移动
            //   参考 Wurst FreecamHack.getCamPos(partialTicks)
            var pos = FreecamManager.getInterpolatedPosition(tickDelta);
            this.setPosition(pos.x, pos.y, pos.z);
            this.setRotation(FreecamManager.getInterpolatedYRot(tickDelta), FreecamManager.getInterpolatedXRot(tickDelta));

            // ★ 取消默认setup方法，彻底避免玩家位置干扰
            ci.cancel();
        }
    }
}