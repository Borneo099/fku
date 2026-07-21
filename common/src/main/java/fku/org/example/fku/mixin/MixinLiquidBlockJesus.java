package fku.org.example.fku.mixin;

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

@Mixin(value={LiquidBlock.class})
public abstract class MixinLiquidBlockJesus {
    @Inject(method={"getCollisionShape"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable cir) {
        if (!WaterWalkConfig.getInstance().enabled) {
            return;
        }
        if (!(context instanceof EntityCollisionContext)) {
            return;
        }
        Entity entity = ((EntityCollisionContext)context).m_193113_();
        if (!(entity instanceof Player)) {
            return;
        }
        if (((Player)entity).m_6144_()) {
            return;
        }
        cir.setReturnValue(Shapes.m_83144_());
    }
}

