package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.freecam.FreecamManager;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ★ 灵魂出窍时阻止玩家角色移动（正确方式）
 *
 * 在 KeyboardInput.tick() 方法执行完毕后（@At("RETURN")），
 * 将输入值重置为0。这样 LivingEntity.aiStep() 读取到的 xxa/zza 都是0，
 * 玩家角色不会移动，而 FreecamManager 独立处理相机移动。
 *
 * 参考自 Lexis KeyboardInputMixin 的 tick() RETURN 注入方式。
 * 该 Mixin 由赛博教员实现
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputFreecamMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickReturn(boolean slowDown, float f, CallbackInfo ci) {
        if (FreecamManager.isActive()) {
            // 通过 this 访问所有字段（KeyboardInput 继承自 Input）
            KeyboardInput self = (KeyboardInput)(Object)this;
            self.forwardImpulse = 0.0F;
            self.leftImpulse = 0.0F;
            self.jumping = false;
            self.shiftKeyDown = false;
        }
    }
}