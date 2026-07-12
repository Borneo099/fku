package lexis.mixin.mixina;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.NametagsHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class EntityRendererMixin {
   @Inject(
      method = {"renderNameTag"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onRenderNameTag(Entity p_114498_, Component p_114499_, PoseStack p_114500_, MultiBufferSource p_114501_, int p_114502_, CallbackInfo ci) {
      if (p_114498_ instanceof Player) {
         Iterator var7 = HackManager.getInstance().getHacks().iterator();

         Hack hack;
         do {
            if (!var7.hasNext()) {
               return;
            }

            hack = (Hack)var7.next();
         } while(!(hack instanceof NametagsHack) || !hack.isEnabled());

         ci.cancel();
      }
   }
}
