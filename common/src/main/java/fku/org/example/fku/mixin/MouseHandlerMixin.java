package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.freecam.FreecamManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ★ 鼠标移动拦截 Mixin
 *
 * 当 FreecamManager 激活时，拦截鼠标原始移动数据，
 * 将视角旋转交给 FreecamManager 处理，而非玩家实体。
 *
 * 解决 Forge 1.20.1 无 InputEvent.MouseMovementEvent 的问题。
 * 参考自 Lexis FreeCamHack 的鼠标旋转逻辑。
 * 该 Mixin 由赛博教员实现
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    /** 上一帧鼠标位置（用于计算增量） */
    private static double freecam_lastX = Double.NaN;
    private static double freecam_lastY = Double.NaN;

    @Inject(method = "onMove", at = @At("HEAD"))
    private void onFreecamMouseMove(long window, double xpos, double ypos, CallbackInfo ci) {
        if (FreecamManager.isActive()) {
            if (!Double.isNaN(freecam_lastX)) {
                double dx = xpos - freecam_lastX;
                double dy = ypos - freecam_lastY;
                FreecamManager.onMouseTurn(dx, dy);
            }
            freecam_lastX = xpos;
            freecam_lastY = ypos;
        } else {
            freecam_lastX = Double.NaN;
            freecam_lastY = Double.NaN;
        }
    }
}