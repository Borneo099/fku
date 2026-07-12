package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IItemMode extends IModeChanger {
   ILangEntry[] getModeLangEntries();

   default byte getModeCount() {
      return (byte)this.getModeLangEntries().length;
   }

   default ILangEntry getModeLangEntry(ItemStack stack) {
      ILangEntry[] langEntries = this.getModeLangEntries();
      byte mode = this.getMode(stack);
      return (ILangEntry)(mode >= 0 && mode < langEntries.length ? langEntries[mode] : PELang.INVALID_MODE);
   }

   default byte getMode(@NotNull ItemStack stack) {
      return stack.m_41782_() ? stack.m_41784_().m_128445_("Mode") : 0;
   }

   default boolean changeMode(@NotNull Player player, @NotNull ItemStack stack, InteractionHand hand) {
      byte numModes = this.getModeCount();
      if (numModes < 2) {
         return false;
      } else {
         stack.m_41784_().m_128344_("Mode", (byte)((this.getMode(stack) + 1) % numModes));
         player.m_213846_(this.getModeSwitchEntry().translate(this.getModeLangEntry(stack)));
         return true;
      }
   }

   default ILangEntry getModeSwitchEntry() {
      return PELang.MODE_SWITCH;
   }

   default Component getToolTip(ItemStack stack) {
      return PELang.CURRENT_MODE.translate(new Object[]{ChatFormatting.AQUA, this.getModeLangEntry(stack)});
   }
}
