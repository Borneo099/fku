package moze_intel.projecte.gameObjs.items.rings;

import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class PEToggleItem extends ItemPE implements IModeChanger {
   public PEToggleItem(Item.Properties props) {
      super(props);
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return false;
   }

   public byte getMode(@NotNull ItemStack stack) {
      return (byte)(ItemHelper.checkItemNBT(stack, "Active") ? 1 : 0);
   }

   public boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      CompoundTag nbt = stack.m_41784_();
      boolean isActive = nbt.m_128471_("Active");
      player.m_9236_().m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), isActive ? (SoundEvent)PESoundEvents.UNCHARGE.get() : (SoundEvent)PESoundEvents.HEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      nbt.m_128379_("Active", !isActive);
      return true;
   }
}
