package lexis.mixin;

import lexis.Server.Commandsavailabletoplayers.NoCommandsBlockCommand;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BaseCommandBlock.class})
public class BaseCommandBlockMixin {
   @Inject(
      method = {"performCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPerformCommand(Level p_45415_, CallbackInfoReturnable cir) {
      if (NoCommandsBlockCommand.isEnabled()) {
         cir.setReturnValue(false);
      }

   }
}
