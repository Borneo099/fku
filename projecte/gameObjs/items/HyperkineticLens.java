package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.entity.EntityLensProjectile;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.Constants;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HyperkineticLens extends ItemPE implements IProjectileShooter, IItemCharge, IBarHelper {
   public HyperkineticLens(Item.Properties props) {
      super(props);
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
   }

   public @NotNull InteractionResultHolder m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         this.shootProjectile(player, stack, hand);
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      Level level = player.m_9236_();
      long requiredEmc = Constants.EXPLOSIVE_LENS_COST[this.getCharge(stack)];
      if (!consumeFuel(player, stack, requiredEmc, true)) {
         return false;
      } else {
         level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESoundEvents.POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
         EntityLensProjectile ent = new EntityLensProjectile(player, this.getCharge(stack), level);
         ent.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
         level.m_7967_(ent);
         return true;
      }
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return 3;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public float getWidthForBar(ItemStack stack) {
      return 1.0F - this.getChargePercent(stack);
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return this.getScaledBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return this.getColorForBar(stack);
   }
}
