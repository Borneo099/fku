package lexis.mixin.mixins;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayer.class})
public class DevSkinMixin {
   private static final ResourceLocation DEV_SKIN = new ResourceLocation("lexis", "modxpy/playernameskin/dev/skin.png");

   @Inject(
      method = {"getSkinTextureLocation"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetSkinTexture(CallbackInfoReturnable cir) {
      AbstractClientPlayer player = (AbstractClientPlayer)this;
      if (player.m_36316_() != null && "Dev".equals(player.m_36316_().getName()) && player.m_7578_()) {
         cir.setReturnValue(DEV_SKIN);
      }

   }
}
