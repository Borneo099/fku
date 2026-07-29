package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.freecam.FreecamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ★ 灵魂出窍时阻止玩家角色移动
 *
 * 当 FreecamManager 激活时，将玩家输入设为0，防止玩家角色跟随 WASD 移动。
 * 参考自 Wurst FreecamHack 的 disablePlayerMovement 逻辑。
 * 该 Mixin 由赛博教员实现
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerFreecamMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        if (FreecamManager.isActive()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // 清除玩家输入，使玩家角色保持静止
                mc.player.input.forwardImpulse = 0;
                mc.player.input.leftImpulse = 0;
                mc.player.input.jumping = false;
                mc.player.input.shiftKeyDown = false;
            }
        }
    }
}