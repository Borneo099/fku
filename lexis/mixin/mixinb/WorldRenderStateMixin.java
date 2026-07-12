package lexis.mixin.mixinb;

import lexis.Hack.Hacks.Render.ItemOutlineHack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class WorldRenderStateMixin {
   @Inject(
      method = {"renderLevel"},
      at = {@At("HEAD")}
   )
   private void lexis$worldRenderBegin(CallbackInfo ci) {
      ItemOutlineHack.WORLD_RENDERING = true;
   }

   @Inject(
      method = {"renderLevel"},
      at = {@At("RETURN")}
   )
   private void lexis$worldRenderEnd(CallbackInfo ci) {
      ItemOutlineHack.WORLD_RENDERING = false;
   }
}
