package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.Random;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.chat.Component;

public final class MathUtils {
   public static int parseInteger(String string) {
      try {
         return Integer.parseInt(string);
      } catch (NumberFormatException var2) {
         return -1;
      }
   }

   public static int randomIntInRange(int min, int max) {
      Random rand = new Random();
      return rand.nextInt(max - min + 1) + min;
   }

   public static int scaleToRedstone(long currentAmount, long max) {
      double proportion = (double)currentAmount / (double)max;
      if (currentAmount <= 0L) {
         return 0;
      } else {
         return currentAmount >= max ? 15 : (int)Math.round(proportion * 13.0 + 1.0);
      }
   }

   public static double tickToSec(int ticks) {
      return (double)ticks / 20.0;
   }

   public static Component tickToSecFormatted(int ticks) {
      double result = tickToSec(ticks);
      return result == 0.0 ? PELang.EVERY_TICK.translate(new Object[]{result}) : PELang.SECONDS.translate(new Object[]{result});
   }

   public static int secToTicks(double secs) {
      return (int)Math.round(secs * 20.0);
   }

   public static long clampToLong(BigInteger bigInt) {
      return bigInt.compareTo(Constants.MAX_LONG) > 0 ? Long.MAX_VALUE : bigInt.longValueExact();
   }
}
