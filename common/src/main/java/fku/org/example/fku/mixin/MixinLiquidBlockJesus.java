package fku.org.example.fku.mixin; /* water */

import fku.org.example.fku.features.waterwalk.WaterWalkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MixinLiquidBlockJesus —— 把液体当实体方块（核心魔法）
 *
 * ★ 原理：当 WaterWalk 激活时，让 LiquidBlock.getCollisionShape() 对玩家
 *   返回完整方块碰撞箱（Shapes.block()），液体因此对玩家变成「实心」。
 *   玩家站其顶上 —— 不沉、不烧（身体在液面上不在流体里）、不减速、如履平地。
 *
 * ★ 潜行放行：玩家按住 Shift 时返回正常碰撞，可正常下潜。
 *
 * 来源：lexis1.20.1/lexis/mixin/mixina/LiquidBlockMixin.java
 */
@Mixin(LiquidBlock.class)
public abstract class MixinLiquidBlockJesus {

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                     CollisionContext context, CallbackInfoReturnable cir) {
        if (!WaterWalkConfig.getInstance().enabled) return;

        if (!(context instanceof EntityCollisionContext)) return;
        Entity entity = ((EntityCollisionContext) context).getEntity();
        if (!(entity instanceof Player)) return;

        // ★ 潜行时放行下潜
        if (((Player) entity).isShiftKeyDown()) return;

        // ★ 让液体对玩家变成完整实心方块
        cir.setReturnValue(Shapes.block());
    }
}
