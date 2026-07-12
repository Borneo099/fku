package lexis.mixin.tacz;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import java.util.function.Supplier;
import lexis.Hack.Hacks.TaCZ_Server.BoltActionFullAutoHack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   targets = {"com.tacz.guns.entity.shooter.LivingEntityBolt"},
   remap = false
)
public class TaczBoltBypassMixin {
   @Shadow
   @Final
   private ShooterDataHolder data;
   @Shadow
   @Final
   private LivingEntity shooter;

   @Inject(
      method = {"bolt"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void lexis$bypassBolt(CallbackInfo ci) {
      if (BoltActionFullAutoHack.boltActionFullAutoActive) {
         if (this.shooter != null) {
            try {
               Minecraft mc = Minecraft.m_91087_();
               if (mc.f_91074_ != null && this.shooter.m_20148_().equals(mc.f_91074_.m_20148_())) {
                  Supplier gunSupplier = this.data.currentGunItem;
                  if (gunSupplier == null) {
                     return;
                  }

                  ItemStack stack = (ItemStack)gunSupplier.get();
                  if (stack == null || stack.m_41619_()) {
                     return;
                  }

                  Item var6 = stack.m_41720_();
                  if (var6 instanceof IGun) {
                     IGun gun = (IGun)var6;
                     gun.setBulletInBarrel(stack, true);
                     this.data.isBolting = false;
                     ci.cancel();
                  }
               }
            } catch (Throwable var7) {
            }

         }
      }
   }
}
