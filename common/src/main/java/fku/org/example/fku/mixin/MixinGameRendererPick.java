package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.autoattack.AutoAttackFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ★ 自动攻击优化：长按左键且已“选中”目标时，把方块命中改写为“选中目标”的实体命中。
 *
 * 效果：
 *   1) 准星即便掠过方块也不会高亮/选中方块（方块描边仅 BLOCK 类型才绘制，改写为 ENTITY 后不再显示）；
 *   2) 不会破坏方块（InteractionKeyMappingTriggered 与 MultiPlayerGameMode 的 mixin 已拦截）；
 *   3) 外部“自动切换工具”mod 读取 Minecraft.hitResult 时拿到的是 ENTITY，从而不会乱切工具；
 *   4) 武器攻击冷却能正常回满。
 *
 * 仅对已“选中”目标（selectedTarget 非空）生效；未选中目标时放行，原版拾取/挖掘照常。
 * 在 GameRenderer#pick 返回后再覆盖 hitResult，不影响自由相机等其它 pick 改写。
 */
@Mixin(GameRenderer.class)
public class MixinGameRendererPick {

    @Inject(method = "pick", at = @At("RETURN"))
    private void fkuSuppressBlockPick(float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.hitResult instanceof BlockHitResult)) return;
        if (!AutoAttackFeature.isAttackingWithTarget()) return;
        Entity target = AutoAttackFeature.getSelectedTarget();
        if (target != null && target.isAlive()) {
            mc.hitResult = new EntityHitResult(target);
        }
    }
}
