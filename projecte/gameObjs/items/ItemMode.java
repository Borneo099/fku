package moze_intel.projecte.gameObjs.items;

import java.util.List;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.capability.ChargeItemCapabilityWrapper;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.utils.text.ILangEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemMode extends ItemPE implements IItemMode, IItemCharge, IBarHelper {
   private final int numCharge;
   private final ILangEntry[] modes;

   public ItemMode(Item.Properties props, int numCharge, ILangEntry... modeDescrp) {
      super(props);
      this.numCharge = numCharge;
      this.modes = modeDescrp;
      this.addItemCapability(ChargeItemCapabilityWrapper::new);
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modes;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
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

   public int getNumCharges(@NotNull ItemStack stack) {
      return this.numCharge;
   }
}
