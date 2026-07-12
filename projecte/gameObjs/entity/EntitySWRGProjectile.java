package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntitySWRGProjectile extends NoGravityThrowableProjectile {
   private boolean fromArcana = false;

   public EntitySWRGProjectile(EntityType type, Level level) {
      super(type, level);
   }

   public EntitySWRGProjectile(Player player, boolean fromArcana, Level level) {
      super((EntityType)PEEntityTypes.SWRG_PROJECTILE.get(), player, level);
      this.fromArcana = fromArcana;
   }

   protected void m_8097_() {
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.m_6084_()) {
         double inverse = 1.0 / (this.m_20069_() ? 0.8 : 0.99);
         this.m_20256_(this.m_20184_().m_82490_(inverse));
         if (!this.m_9236_().f_46443_ && this.m_6084_() && this.m_20186_() > (double)this.m_9236_().m_151558_() && this.m_9236_().m_46471_()) {
            LevelData var4 = this.m_9236_().m_6106_();
            if (var4 instanceof ServerLevelData) {
               ServerLevelData levelData = (ServerLevelData)var4;
               levelData.m_5557_(true);
            }

            this.m_146870_();
         }
      }

   }

   protected void m_6532_(@NotNull HitResult result) {
      super.m_6532_(result);
      this.m_146870_();
   }

   protected void m_8060_(@NotNull BlockHitResult result) {
      super.m_8060_(result);
      if (!this.m_9236_().f_46443_) {
         Entity var3 = this.m_19749_();
         if (var3 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var3;
            ItemStack found = PlayerHelper.findFirstItem(player, this.fromArcana ? (Item)PEItems.ARCANA_RING.get() : (Item)PEItems.SWIFTWOLF_RENDING_GALE.get());
            if (!found.m_41619_() && ItemPE.consumeFuel(player, found, 768L, true)) {
               BlockPos pos = result.m_82425_();
               LightningBolt lightning = (LightningBolt)EntityType.f_20465_.m_20615_(this.m_9236_());
               if (lightning != null) {
                  lightning.m_20219_(Vec3.m_82512_(pos));
                  lightning.m_20879_(player);
                  this.m_9236_().m_7967_(lightning);
               }

               if (this.m_9236_().m_46470_()) {
                  for(int i = 0; i < 3; ++i) {
                     LightningBolt bonus = (LightningBolt)EntityType.f_20465_.m_20615_(this.m_9236_());
                     if (bonus != null) {
                        bonus.m_6027_((double)pos.m_123341_() + 0.5 + this.m_9236_().f_46441_.m_188583_(), (double)pos.m_123342_() + 0.5 + this.m_9236_().f_46441_.m_188583_(), (double)pos.m_123343_() + 0.5 + this.m_9236_().f_46441_.m_188583_());
                        bonus.m_20879_(player);
                        this.m_9236_().m_7967_(bonus);
                     }
                  }
               }
            }
         }
      }

   }

   protected void m_5790_(@NotNull EntityHitResult result) {
      super.m_5790_(result);
      if (!this.m_9236_().f_46443_) {
         Entity var4 = result.m_82443_();
         if (var4 instanceof LivingEntity) {
            LivingEntity e = (LivingEntity)var4;
            var4 = this.m_19749_();
            if (var4 instanceof Player) {
               Player player = (Player)var4;
               ItemStack found = PlayerHelper.findFirstItem(player, this.fromArcana ? (Item)PEItems.ARCANA_RING.get() : (Item)PEItems.SWIFTWOLF_RENDING_GALE.get());
               if (!found.m_41619_() && ItemPE.consumeFuel(player, found, 64L, true)) {
                  e.m_6469_(this.m_9236_().m_269111_().m_269075_(player), 1.0F);
                  boolean oldOnGround = e.m_20096_();
                  e.m_6853_(true);
                  e.m_147240_(5.0, -this.m_20184_().m_7096_() * 0.25, -this.m_20184_().m_7094_() * 0.25);
                  e.m_6853_(oldOnGround);
                  e.m_20256_(e.m_20184_().m_82542_(1.0, 3.0, 1.0));
               }
            }
         }
      }

   }

   public void m_7378_(@NotNull CompoundTag compound) {
      super.m_7378_(compound);
      this.fromArcana = compound.m_128471_("fromArcana");
   }

   public void m_7380_(@NotNull CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128379_("fromArcana", this.fromArcana);
   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean m_6128_() {
      return true;
   }
}
