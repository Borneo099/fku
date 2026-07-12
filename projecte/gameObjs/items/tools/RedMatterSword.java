package moze_intel.projecte.gameObjs.items.tools;

import java.util.List;
import moze_intel.projecte.capability.ModeChangerItemCapabilityWrapper;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.gameObjs.items.IItemMode;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedMatterSword extends PESword implements IItemMode {
   private final ILangEntry[] modeDesc;

   public RedMatterSword(Item.Properties props) {
      super(EnumMatterType.RED_MATTER, 3, 12, props);
      this.modeDesc = new ILangEntry[]{PELang.MODE_RED_SWORD_1, PELang.MODE_RED_SWORD_2};
      this.addItemCapability(ModeChangerItemCapabilityWrapper::new);
   }

   protected boolean slayAll(@NotNull ItemStack stack) {
      return this.getMode(stack) == 1;
   }

   public ILangEntry[] getModeLangEntries() {
      return this.modeDesc;
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(this.getToolTip(stack));
   }
}
