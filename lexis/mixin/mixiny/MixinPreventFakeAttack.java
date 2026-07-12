package lexis.mixin.mixiny;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class MixinPreventFakeAttack {
   private static boolean isFakePlayer(Entity target) {
      return target.getClass().getSimpleName().equals("FakePlayerEntity");
   }

   @Inject(
      method = {"attack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lexis$attackFake(Player player, Entity target, CallbackInfo ci) {
      if (isFakePlayer(target)) {
         player.m_6674_(InteractionHand.MAIN_HAND);
         target.m_6469_(player.m_269291_().m_269075_(player), (float)player.m_21133_(Attributes.f_22281_));
         ci.cancel();
      }
   }

   @Inject(
      method = {"interact"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lexis$interactFake(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable cir) {
      if (isFakePlayer(target)) {
         cir.setReturnValue(InteractionResult.PASS);
      }

   }

   @Inject(
      method = {"interactAt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void lexis$interactAtFake(Player player, Entity target, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable cir) {
      if (isFakePlayer(target)) {
         cir.setReturnValue(InteractionResult.PASS);
      }

   }
}
