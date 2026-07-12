package lexis.mixin.mixina;

import lexis.Hack.Hacks.Movement.JesusHack;
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

@Mixin({LiquidBlock.class})
public class LiquidBlockMixin {
   @Inject(
      method = {"getCollisionShape"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable cir) {
      if (JesusHack.isActive()) {
         if (context instanceof EntityCollisionContext) {
            Entity entity = ((EntityCollisionContext)context).m_193113_();
            if (entity instanceof Player) {
               cir.setReturnValue(Shapes.m_83144_());
            }
         }

      }
   }
}
