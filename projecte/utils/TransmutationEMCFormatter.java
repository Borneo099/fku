package moze_intel.projecte.utils;

import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

public class TransmutationEMCFormatter {
   private static final int MAX_POSTFIX_INDEX = 17;

   public static Component formatEMC(Number emc) {
      String emcAsString = emc.toString();
      int length = emcAsString.length();
      int splits = (length - 1) / 3;
      if (splits < 4) {
         return TextComponentUtil.getString(Constants.EMC_FORMATTER.format(emc));
      } else {
         int postfixIndex = splits - 4;
         if (postfixIndex > 17) {
            return PELang.EMC_TOO_MUCH.translate(new Object[0]);
         } else {
            int extraDigits = length % 3;
            String var10000;
            double value;
            if (extraDigits == 0) {
               var10000 = emcAsString.substring(0, 3);
               value = Double.parseDouble(var10000 + "." + emcAsString.substring(3, 5));
            } else if (extraDigits == 1) {
               char var8 = emcAsString.charAt(0);
               value = Double.parseDouble("" + var8 + "." + emcAsString.substring(1, 3));
            } else {
               var10000 = emcAsString.substring(0, 2);
               value = Double.parseDouble(var10000 + "." + emcAsString.substring(2, 4));
            }

            return TextComponentUtil.smartTranslate(Util.m_137492_("emc", PECore.rl("postfix." + postfixIndex)), Constants.EMC_FORMATTER.format(value));
         }
      }
   }
}
