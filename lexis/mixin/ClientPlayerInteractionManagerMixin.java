package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Blocks.AutoToolHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"destroyBlock"},
      at = {@At("HEAD")}
   )
   private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable cir) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      while(var3.hasNext()) {
         Hack hack = (Hack)var3.next();
         if (hack instanceof AutoToolHack && hack.isEnabled()) {
            ((AutoToolHack)hack).onBlockBreaking(pos);
            break;
         }
      }

   }

   @Inject(
      method = {"startDestroyBlock"},
      at = {@At("HEAD")}
   )
   private void onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable cir) {
      Iterator var4 = HackManager.getInstance().getHacks().iterator();

      while(var4.hasNext()) {
         Hack hack = (Hack)var4.next();
         if (hack instanceof AutoToolHack && hack.isEnabled()) {
            ((AutoToolHack)hack).onBlockBreaking(pos);
            break;
         }
      }

   }
}
