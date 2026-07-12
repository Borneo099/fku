package moze_intel.projecte.capability;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModeChangerItemCapabilityWrapper extends BasicItemCapability implements IModeChanger {
   public Capability getCapability() {
      return PECapabilities.MODE_CHANGER_ITEM_CAPABILITY;
   }

   public byte getMode(@NotNull ItemStack stack) {
      return ((IModeChanger)this.getItem()).getMode(stack);
   }

   public boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand) {
      return ((IModeChanger)this.getItem()).changeMode(player, stack, hand);
   }
}
