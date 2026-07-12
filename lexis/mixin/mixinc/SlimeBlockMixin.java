package lexis.mixin.mixinc;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.BouncyHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SlimeBlock.class})
public class SlimeBlockMixin {
   @Inject(
      method = {"fallOn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onFallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
      Iterator var8 = HackManager.getInstance().getHacks().iterator();

      while(var8.hasNext()) {
         Hack hack = (Hack)var8.next();
         if (hack instanceof BouncyHack bouncy && hack.isEnabled()) {
            break;
         }
      }

      if (bouncy != null) {
         double height = bouncy.getJumpHeight();
         double velocityY = Math.sqrt(0.16 * height);
         entity.m_20334_(entity.m_20184_().f_82479_, velocityY, entity.m_20184_().f_82481_);
         ci.cancel();
      }
   }
}
