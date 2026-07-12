package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.capability.ProjectileShooterItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CataliticLens extends DestructionCatalyst implements IProjectileShooter {
   public CataliticLens(Item.Properties props) {
      super(props);
      this.addItemCapability(ProjectileShooterItemCapabilityWrapper::new);
   }

   public boolean shootProjectile(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      return ((HyperkineticLens)PEItems.HYPERKINETIC_LENS.get()).shootProjectile(player, stack, hand);
   }

   public int getNumCharges(@NotNull ItemStack stack) {
      return 7;
   }
}
