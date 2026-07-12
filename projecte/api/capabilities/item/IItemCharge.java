package moze_intel.projecte.api.capabilities.item;

import moze_intel.projecte.api.PESounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItemCharge {
   String KEY = "Charge";

   int getNumCharges(@NotNull ItemStack var1);

   default float getChargePercent(@NotNull ItemStack stack) {
      return (float)this.getCharge(stack) / (float)this.getNumCharges(stack);
   }

   default int getCharge(@NotNull ItemStack stack) {
      return stack.m_41784_().m_128451_("Charge");
   }

   default boolean changeCharge(@NotNull Player player, @NotNull ItemStack stack, @Nullable InteractionHand hand) {
      int currentCharge = this.getCharge(stack);
      int numCharges = this.getNumCharges(stack);
      if (player.m_36341_()) {
         if (currentCharge > 0) {
            player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESounds.UNCHARGE.get(), SoundSource.PLAYERS, 1.0F, 0.5F + 0.5F / (float)numCharges * (float)currentCharge);
            stack.m_41784_().m_128405_("Charge", currentCharge - 1);
            return true;
         }
      } else if (currentCharge < numCharges) {
         player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)PESounds.CHARGE.get(), SoundSource.PLAYERS, 1.0F, 0.5F + 0.5F / (float)numCharges * (float)currentCharge);
         stack.m_41784_().m_128405_("Charge", currentCharge + 1);
         return true;
      }

      return false;
   }
}
