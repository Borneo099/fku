package lexis.mixin;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Movement.AntiExplosionHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientboundExplodePacket.class})
public class AntiExplosionMixin {
   private Vec3 modifiedKnockback = null;

   @Inject(
      method = {"<init>*"},
      at = {@At("RETURN")}
   )
   private void onInit(CallbackInfo ci) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         Iterator var3 = HackManager.getInstance().getHacks().iterator();

         Hack hack;
         do {
            if (!var3.hasNext()) {
               return;
            }

            hack = (Hack)var3.next();
         } while(!(hack instanceof AntiExplosionHack) || !hack.isEnabled());

         this.modifiedKnockback = Vec3.f_82478_;
      }
   }

   @Inject(
      method = {"getKnockbackX"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetKnockbackX(CallbackInfoReturnable cir) {
      if (this.modifiedKnockback != null) {
         cir.setReturnValue((float)this.modifiedKnockback.f_82479_);
      }

   }

   @Inject(
      method = {"getKnockbackY"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetKnockbackY(CallbackInfoReturnable cir) {
      if (this.modifiedKnockback != null) {
         cir.setReturnValue((float)this.modifiedKnockback.f_82480_);
      }

   }

   @Inject(
      method = {"getKnockbackZ"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetKnockbackZ(CallbackInfoReturnable cir) {
      if (this.modifiedKnockback != null) {
         cir.setReturnValue((float)this.modifiedKnockback.f_82481_);
      }

   }
}
