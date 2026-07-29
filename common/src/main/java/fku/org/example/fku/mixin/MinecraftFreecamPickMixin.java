package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.freecam.FreecamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * ★ 灵魂出窍时覆盖 GameRenderer.pick() 的射线检测
 *
 * 当 FreecamManager 激活时，使用自由相机位置/旋转进行射线检测，
 * 替代默认的玩家眼位检测，使灵魂出窍后准星能正确选中方块/实体。
 *
 * 参考自 Lexis FreeCamHack 的 GameRendererMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(GameRenderer.class)
public class MinecraftFreecamPickMixin {

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void onPick(float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (FreecamManager.isActive() && mc.level != null && mc.player != null) {
            // ★ 使用自由相机位置/旋转计算射线
            Vec3 eyePos = FreecamManager.getInterpolatedPosition(partialTicks);
            float yRot = FreecamManager.getInterpolatedYRot(partialTicks);
            float xRot = FreecamManager.getInterpolatedXRot(partialTicks);
            Vec3 lookVec = Vec3.directionFromRotation(xRot, yRot);

            // 获取拾取距离
            double d0 = mc.gameMode != null ? mc.gameMode.getPickRange() : 6.0;

            // 先做方块射线检测
            Vec3 end = eyePos.add(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0);
            mc.hitResult = mc.level.clip(new ClipContext(
                    eyePos, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    mc.player));

            // 再做实体射线检测（遍历所有碰撞箱）
            double maxDist = mc.hitResult.getLocation().distanceTo(eyePos);
            Entity closestEntity = null;
            Vec3 closestHit = null;

            for (Entity entity : mc.level.getEntities(mc.player,
                    mc.player.getBoundingBox().inflate(d0))) {
                if (entity.isSpectator() || !entity.isAlive() || !entity.isPickable())
                    continue;
                AABB aabb = entity.getBoundingBox().inflate(0.3);
                Optional<Vec3> hit = aabb.clip(eyePos, end);
                if (hit.isPresent()) {
                    double dist = hit.get().distanceTo(eyePos);
                    if (dist < maxDist) {
                        maxDist = dist;
                        closestEntity = entity;
                        closestHit = hit.get();
                    }
                }
            }

            // 如果实体更近，覆盖为实体检测结果
            if (closestEntity != null) {
                mc.hitResult = new EntityHitResult(closestEntity, closestHit);
            }

            // 取消默认 pick 方法，防止玩家眼位覆盖
            ci.cancel();
        }
    }
}