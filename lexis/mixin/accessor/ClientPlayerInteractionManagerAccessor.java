package lexis.mixin.accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MultiPlayerGameMode.class})
public interface ClientPlayerInteractionManagerAccessor {
   @Accessor("destroyProgress")
   float getDestroyProgress();

   @Accessor("destroyProgress")
   void setDestroyProgress(float progress);

   @Accessor("destroyBlockPos")
   BlockPos getDestroyBlockPos();
}
