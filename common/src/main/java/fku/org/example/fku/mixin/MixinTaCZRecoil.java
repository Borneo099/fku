package fku.org.example.fku.mixin; /* water */

import com.tacz.guns.resource.pojo.data.gun.GunRecoil;
import fku.org.example.fku.features.tacz.TaCZConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

/**
 * MixinTaCZRecoil — 无后座功能
 * 参考自 NoSpread 02 的 MixinGunRecoil
 * 当 TaCZConfig.noRecoilEnabled 开启时，将后坐力样条函数替换为零函数
 * 使用反射创建 PolynomialSplineFunction 避免编译期依赖
 * 该 Mixin 由赛博教员实现
 */
@Mixin(value = GunRecoil.class, remap = false)
public class MixinTaCZRecoil {

    /** 缓存的零函数实例，通过反射创建 */
    private static Object ZERO_FUNCTION;

    private static Object getZeroFunction() {
        if (ZERO_FUNCTION != null) return ZERO_FUNCTION;
        try {
            Class<?> polyFunc = Class.forName("org.apache.commons.math3.analysis.polynomials.PolynomialFunction");
            Constructor<?> polyCtor = polyFunc.getConstructor(double[].class);
            Object zeroPoly = polyCtor.newInstance(new double[]{0.0});

            Class<?> splineFunc = Class.forName("org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction");
            Constructor<?> splineCtor = splineFunc.getConstructor(double[].class, polyFunc);
            ZERO_FUNCTION = splineCtor.newInstance(new double[]{0.0, 10.0}, zeroPoly);
        } catch (Exception e) {
            ZERO_FUNCTION = null;
        }
        return ZERO_FUNCTION;
    }

    @Inject(method = "genPitchSplineFunction", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGenPitch(float modifier, CallbackInfoReturnable<Object> cir) {
        if (TaCZConfig.getInstance().noRecoilEnabled) {
            Object zero = getZeroFunction();
            if (zero != null) {
                cir.setReturnValue(zero);
            }
        }
    }

    @Inject(method = "genYawSplineFunction", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGenYaw(float modifier, CallbackInfoReturnable<Object> cir) {
        if (TaCZConfig.getInstance().noRecoilEnabled) {
            Object zero = getZeroFunction();
            if (zero != null) {
                cir.setReturnValue(zero);
            }
        }
    }
}