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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityFireProjectile extends NoGravityThrowableProjectile {
   private boolean fromArcana = false;

   public EntityFireProjectile(EntityType type, Level level) {
      super(type, level);
   }

   public EntityFireProjectile(Player entity, boolean fromArcana, Level level) {
      super((EntityType)PEEntityTypes.FIRE_PROJECTILE.get(), entity, level);
      this.fromArcana = fromArcana;
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
            BlockPos pos = result.m_82425_();
            Block block = this.m_9236_().m_8055_(pos).m_60734_();
            if (block == Blocks.f_50080_) {
               this.m_9236_().m_46597_(pos, Blocks.f_49991_.m_49966_());
            } else if (block == Blocks.f_49992_) {
               BlockPos.m_121990_(pos.m_7918_(-2, -2, -2), pos.m_7918_(2, 2, 2)).forEach((currentPos) -> {
                  if (this.m_9236_().m_8055_(currentPos).m_60734_() == Blocks.f_49992_) {
                     PlayerHelper.checkedPlaceBlock(player, pos.m_7949_(), Blocks.f_50058_.m_49966_());
                  }

               });
            } else {
               BlockPos.m_121990_(pos.m_7918_(-1, -1, -1), pos.m_7918_(1, 1, 1)).forEach((currentPos) -> {
                  if (this.m_9236_().m_46859_(currentPos)) {
                     PlayerHelper.checkedPlaceBlock(player, currentPos.m_7949_(), Blocks.f_50083_.m_49966_());
                  }

               });
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
            ItemStack found = PlayerHelper.findFirstItem(player, this.fromArcana ? (Item)PEItems.ARCANA_RING.get() : (Item)PEItems.IGNITION_RING.get());
            if (!found.m_41619_() && ItemPE.consumeFuel(player, found, 32L, true)) {
               Entity ent = result.m_82443_();
               ent.m_20254_(5);
               ent.m_6469_(this.m_9236_().m_269111_().m_269387_(), 5.0F);
            }
         }
      }

   }

   protected void m_8097_() {
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
