package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.NoWebHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WebBlock.class})
public class WebBlockMixin {
   @Inject(
      method = {"entityInside"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onEntityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
      if (level.f_46443_ && entity == Minecraft.m_91087_().f_91074_) {
         Iterator var6 = HackManager.getInstance().getHacks().iterator();

         while(var6.hasNext()) {
            Hack hack = (Hack)var6.next();
            if (hack instanceof NoWebHack && hack.isEnabled()) {
               ci.cancel();
               return;
            }
         }
      }

   }
}
