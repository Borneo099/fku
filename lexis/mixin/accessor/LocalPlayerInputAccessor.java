package lexis.mixin.accessor;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LocalPlayer.class})
public interface LocalPlayerInputAccessor {
   @Accessor("input")
   Input getInput();

   @Accessor("input")
   void setInput(Input input);
}
