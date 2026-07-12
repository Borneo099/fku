package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class EntityNovaCatalystPrimed extends PrimedTnt {
   public EntityNovaCatalystPrimed(EntityType type, Level level) {
      super(type, level);
      this.m_32085_(this.m_32100_() / 4);
   }

   public EntityNovaCatalystPrimed(Level level, double x, double y, double z, LivingEntity placer) {
      super(level, x, y, z, placer);
      this.m_32085_(this.m_32100_() / 4);
      this.f_19850_ = true;
   }

   public @NotNull EntityType m_6095_() {
      return (EntityType)PEEntityTypes.NOVA_CATALYST_PRIMED.get();
   }

   protected void m_32103_() {
      WorldHelper.createNovaExplosion(this.m_9236_(), this, this.m_20185_(), this.m_20186_(), this.m_20189_(), 16.0F);
   }

   public @NotNull Packet m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public ItemStack getPickedResult(HitResult target) {
      return new ItemStack(PEBlocks.NOVA_CATALYST);
   }
}
