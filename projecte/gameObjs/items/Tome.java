package moze_intel.projecte.gameObjs.items;

import java.util.List;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tome extends ItemPE {
   public Tome(Item.Properties props) {
      super(props);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, @NotNull List tooltips, @NotNull TooltipFlag flags) {
      super.m_7373_(stack, level, tooltips, flags);
      tooltips.add(PELang.TOOLTIP_TOME.translate(new Object[0]));
   }
}
