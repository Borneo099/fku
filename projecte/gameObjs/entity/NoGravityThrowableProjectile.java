package moze_intel.projecte.gameObjs.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class NoGravityThrowableProjectile extends ThrowableProjectile {
   protected NoGravityThrowableProjectile(EntityType type, Level level) {
      super(type, level);
   }

   protected NoGravityThrowableProjectile(EntityType type, LivingEntity shooter, Level level) {
      super(type, shooter, level);
   }

   public float m_7139_() {
      return 0.0F;
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_ && (this.f_19797_ > 400 || this.m_20184_().equals(Vec3.f_82478_) || !this.m_9236_().m_46749_(this.m_20183_()))) {
         this.m_146870_();
      }

   }
}
