package moze_intel.projecte.gameObjs.sound;

import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MovingSoundSWRG extends AbstractTickableSoundInstance {
   private final EntitySWRGProjectile swrgProjectile;
   private float distance = 0.0F;

   public MovingSoundSWRG(EntitySWRGProjectile swrgProjectile, RandomSource random) {
      super((SoundEvent)PESoundEvents.WIND_MAGIC.get(), SoundSource.WEATHER, random);
      this.swrgProjectile = swrgProjectile;
      this.f_119575_ = this.swrgProjectile.m_20185_();
      this.f_119576_ = this.swrgProjectile.m_20186_();
      this.f_119577_ = this.swrgProjectile.m_20189_();
      this.f_119573_ = 0.6F;
   }

   public void m_7788_() {
      if (this.swrgProjectile.m_213877_()) {
         this.m_119609_();
      } else {
         this.f_119575_ = this.swrgProjectile.m_20185_();
         this.f_119576_ = this.swrgProjectile.m_20186_();
         this.f_119577_ = this.swrgProjectile.m_20189_();
         float f = (float)this.swrgProjectile.m_20184_().m_165924_();
         if (f >= 0.01F) {
            this.distance = Mth.m_14036_(this.distance + 0.0025F, 0.0F, 1.0F);
            this.f_119573_ = Mth.m_14179_(Mth.m_14036_(f, 0.0F, 0.5F), 0.0F, 0.7F);
         } else {
            this.distance = 0.0F;
            this.f_119573_ = 0.0F;
         }
      }

   }
}
