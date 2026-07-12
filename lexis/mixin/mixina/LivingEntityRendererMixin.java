package lexis.mixin.mixina;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Render.TrueSightHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LivingEntityRenderer.class})
public class LivingEntityRendererMixin {
   @Redirect(
      method = {"render*"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"
)
   )
   private boolean redirectIsInvisibleTo(LivingEntity entity, Player player) {
      Iterator var3 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         if (!var3.hasNext()) {
            return entity.m_20177_(player);
         }

         hack = (Hack)var3.next();
      } while(!(hack instanceof TrueSightHack) || !hack.isEnabled());

      return false;
   }
}
