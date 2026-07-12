package lexis.mixin.mixins;

import lexis.Hack.Hacks.Render.GodViewHack;
import lexis.mixin.accessor.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   protected abstract void m_90572_(float yRot, float xRot);

   @Shadow
   public abstract void m_90581_(Vec3 pos);

   @Inject(
      method = {"setup"},
      at = {@At("RETURN")}
   )
   private void onSetupReturn(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
      if (GodViewHack.isEnabledStatic()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            double interpX = mc.f_91074_.f_19854_ + (mc.f_91074_.m_20185_() - mc.f_91074_.f_19854_) * (double)partialTick;
            double interpY = mc.f_91074_.f_19855_ + (mc.f_91074_.m_20186_() - mc.f_91074_.f_19855_) * (double)partialTick;
            double interpZ = mc.f_91074_.f_19856_ + (mc.f_91074_.m_20189_() - mc.f_91074_.f_19856_) * (double)partialTick;
            double height = GodViewHack.getHeight();
            double y = interpY + height;
            CameraAccessor acc = (CameraAccessor)this;
            acc.setPosition(new Vec3(interpX, y, interpZ));
            acc.setYRot(0.0F);
            acc.setXRot(90.0F);
         }
      }
   }
}
