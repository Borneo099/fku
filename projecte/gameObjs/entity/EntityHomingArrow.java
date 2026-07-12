package moze_intel.projecte.gameObjs.entity;

import java.util.Comparator;
import java.util.List;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityHomingArrow extends Arrow {
   private static final EntityDataAccessor DW_TARGET_ID;
   private static final int NO_TARGET = -1;
   private int newTargetCooldown = 0;

   public EntityHomingArrow(EntityType type, Level level) {
      super(type, level);
   }

   public EntityHomingArrow(Level level, LivingEntity shooter, float damage) {
      super(level, shooter);
      this.m_36781_((double)damage);
      this.f_36705_ = Pickup.CREATIVE_ONLY;
   }

   public @NotNull EntityType m_6095_() {
      return (EntityType)PEEntityTypes.HOMING_ARROW.get();
   }

   public void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(DW_TARGET_ID, -1);
   }

   protected void m_7761_(@NotNull LivingEntity living) {
      super.m_7761_(living);
      living.f_19802_ = 0;
   }

   public void m_8119_() {
      if (!this.m_9236_().f_46443_ && this.f_19797_ > 3) {
         if (this.hasTarget() && (!this.getTarget().m_6084_() || this.f_36703_)) {
            this.f_19804_.m_135381_(DW_TARGET_ID, -1);
         }

         if (!this.hasTarget() && !this.f_36703_ && this.newTargetCooldown <= 0) {
            this.findNewTarget();
         } else {
            --this.newTargetCooldown;
         }
      }

      if (this.f_19797_ > 3 && this.hasTarget() && !this.f_36703_) {
         double mX = this.m_20184_().m_7096_();
         double mY = this.m_20184_().m_7098_();
         double mZ = this.m_20184_().m_7094_();
         this.m_9236_().m_7106_(ParticleTypes.f_123744_, this.m_20185_() + mX / 4.0, this.m_20186_() + mY / 4.0, this.m_20189_() + mZ / 4.0, -mX / 2.0, -mY / 2.0 + 0.2, -mZ / 2.0);
         this.m_9236_().m_7106_(ParticleTypes.f_123744_, this.m_20185_() + mX / 4.0, this.m_20186_() + mY / 4.0, this.m_20189_() + mZ / 4.0, -mX / 2.0, -mY / 2.0 + 0.2, -mZ / 2.0);
         Entity target = this.getTarget();
         Vec3 arrowLoc = new Vec3(this.m_20185_(), this.m_20186_(), this.m_20189_());
         Vec3 targetLoc = new Vec3(target.m_20185_(), target.m_20186_() + (double)(target.m_20206_() / 2.0F), target.m_20189_());
         Vec3 lookVec = targetLoc.m_82546_(arrowLoc);
         Vec3 arrowMotion = new Vec3(mX, mY, mZ);
         double theta = this.wrap180Radian(this.angleBetween(arrowMotion, lookVec));
         theta = this.clampAbs(theta, 1.5707963267948966);
         Vec3 crossProduct = arrowMotion.m_82537_(lookVec).m_82541_();
         Vec3 adjustedLookVec = this.transform(crossProduct, theta, arrowMotion);
         this.m_6686_(adjustedLookVec.f_82479_, adjustedLookVec.f_82480_, adjustedLookVec.f_82481_, 1.0F, 0.0F);
      }

      super.m_8119_();
   }

   private Vec3 transform(Vec3 axis, double angle, Vec3 normal) {
      double m00 = 1.0;
      double m01 = 0.0;
      double m02 = 0.0;
      double m10 = 0.0;
      double m11 = 1.0;
      double m12 = 0.0;
      double m20 = 0.0;
      double m21 = 0.0;
      double m22 = 1.0;
      double mag = Math.sqrt(axis.f_82479_ * axis.f_82479_ + axis.f_82480_ * axis.f_82480_ + axis.f_82481_ * axis.f_82481_);
      if (mag >= 1.0E-10) {
         mag = 1.0 / mag;
         double ax = axis.f_82479_ * mag;
         double ay = axis.f_82480_ * mag;
         double az = axis.f_82481_ * mag;
         double sinTheta = Math.sin(angle);
         double cosTheta = Math.cos(angle);
         double t = 1.0 - cosTheta;
         double xz = ax * az;
         double xy = ax * ay;
         double yz = ay * az;
         m00 = t * ax * ax + cosTheta;
         m01 = t * xy - sinTheta * az;
         m02 = t * xz + sinTheta * ay;
         m10 = t * xy + sinTheta * az;
         m11 = t * ay * ay + cosTheta;
         m12 = t * yz - sinTheta * ax;
         m20 = t * xz - sinTheta * ay;
         m21 = t * yz + sinTheta * ax;
         m22 = t * az * az + cosTheta;
      }

      return new Vec3(m00 * normal.f_82479_ + m01 * normal.f_82480_ + m02 * normal.f_82481_, m10 * normal.f_82479_ + m11 * normal.f_82480_ + m12 * normal.f_82481_, m20 * normal.f_82479_ + m21 * normal.f_82480_ + m22 * normal.f_82481_);
   }

   protected @NotNull ItemStack m_7941_() {
      return new ItemStack(Items.f_42412_);
   }

   private void findNewTarget() {
      List candidates = this.m_9236_().m_45976_(Mob.class, this.m_20191_().m_82377_(8.0, 8.0, 8.0));
      if (!candidates.isEmpty()) {
         candidates.sort(Comparator.comparing(this::m_20280_, Double::compare));
         this.f_19804_.m_135381_(DW_TARGET_ID, ((Mob)candidates.get(0)).m_19879_());
      }

      this.newTargetCooldown = 5;
   }

   private Mob getTarget() {
      return (Mob)this.m_9236_().m_6815_((Integer)this.f_19804_.m_135370_(DW_TARGET_ID));
   }

   private boolean hasTarget() {
      return this.getTarget() != null;
   }

   private double angleBetween(Vec3 v1, Vec3 v2) {
      double vDot = v1.m_82526_(v2) / (v1.m_82553_() * v2.m_82553_());
      if (vDot < -1.0) {
         vDot = -1.0;
      }

      if (vDot > 1.0) {
         vDot = 1.0;
      }

      return Math.acos(vDot);
   }

   private double wrap180Radian(double radian) {
      for(radian %= 6.283185307179586; radian >= Math.PI; radian -= 6.283185307179586) {
      }

      while(radian < -3.141592653589793) {
         radian += 6.283185307179586;
      }

      return radian;
   }

   private double clampAbs(double param, double maxMagnitude) {
      if (Math.abs(param) > maxMagnitude) {
         if (param < 0.0) {
            param = -Math.abs(maxMagnitude);
         } else {
            param = Math.abs(maxMagnitude);
         }
      }

      return param;
   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean m_6128_() {
      return true;
   }

   static {
      DW_TARGET_ID = SynchedEntityData.m_135353_(EntityHomingArrow.class, EntityDataSerializers.f_135028_);
   }
}
