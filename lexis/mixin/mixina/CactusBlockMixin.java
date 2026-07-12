package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Blocks.AntiCactusHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CactusBlock.class})
public class CactusBlockMixin {
   @Inject(
      method = {"getCollisionShape"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable cir) {
      Iterator var6 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var6.hasNext()) {
            return;
         }

         hack = (Hack)var6.next();
      } while(!(hack instanceof AntiCactusHack) || !hack.isEnabled());

      cir.setReturnValue(Shapes.m_83048_(0.0, 0.0, 0.0, 1.0, 1.38, 1.0));
   }
}
