package moze_intel.projecte.capability;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectileShooterItemCapabilityWrapper extends BasicItemCapability implements IProjectileShooter {
   public Capability getCapability() {
      return PECapabilities.PROJECTILE_SHOOTER_ITEM_CAPABILITY;
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand) {
      return ((IProjectileShooter)this.getItem()).shootProjectile(player, stack, hand);
   }
}
