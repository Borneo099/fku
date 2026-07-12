package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityLavaProjectile extends NoGravityThrowableProjectile {
   public EntityLavaProjectile(EntityType type, Level level) {
      super(type, level);
   }

   public EntityLavaProjectile(Player entity, Level level) {
      super((EntityType)PEEntityTypes.LAVA_PROJECTILE.get(), entity, level);
   }

   protected void m_8097_() {
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_ && this.m_6084_()) {
         Entity thrower = this.m_19749_();
         if (thrower instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)thrower;
            BlockPos.m_121990_(this.m_20183_().m_7918_(-3, -3, -3), this.m_20183_().m_7918_(3, 3, 3)).forEach((pos) -> {
               if (this.m_9236_().m_46749_(pos)) {
                  BlockState state = this.m_9236_().m_8055_(pos);
                  if (state.m_60819_().m_205070_(FluidTags.f_13131_)) {
                     pos = pos.m_7949_();
                     if (PlayerHelper.hasEditPermission(player, pos)) {
                        WorldHelper.drainFluid(this.m_9236_(), pos, state, Fluids.f_76193_);
                        this.m_9236_().m_5594_((Player)null, pos, SoundEvents.f_11937_, SoundSource.BLOCKS, 0.5F, 2.6F + (this.m_9236_().f_46441_.m_188501_() - this.m_9236_().f_46441_.m_188501_()) * 0.8F);
                     }
                  }
               }

            });
         }

         if (this.m_20186_() > 128.0) {
            LevelData worldInfo = this.m_9236_().m_6106_();
            worldInfo.m_5565_(false);
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
            ItemStack found = PlayerHelper.findFirstItem(player, (Item)PEItems.VOLCANITE_AMULET.get());
            if (!found.m_41619_() && ItemPE.consumeFuel(player, found, 32L, true)) {
               WorldHelper.placeFluid(player, this.m_9236_(), result.m_82425_(), result.m_82434_(), Fluids.f_76195_, false);
            }
         }
      }

   }

   protected void m_5790_(@NotNull EntityHitResult result) {
      super.m_5790_(result);
      if (!this.m_9236_().f_46443_) {
         Entity var3 = this.m_19749_();
         if (var3 instanceof Player) {
            Player player = (Player)var3;
            ItemStack found = PlayerHelper.findFirstItem(player, (Item)PEItems.VOLCANITE_AMULET.get());
            if (!found.m_41619_() && ItemPE.consumeFuel(player, found, 32L, true)) {
               Entity ent = result.m_82443_();
               ent.m_20254_(5);
               ent.m_6469_(this.m_9236_().m_269111_().m_269387_(), 5.0F);
            }
         }
      }

   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public boolean m_6128_() {
      return true;
   }
}
