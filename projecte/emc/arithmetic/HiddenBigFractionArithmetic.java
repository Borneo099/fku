package moze_intel.projecte.emc.arithmetic;

import moze_intel.projecte.shaded.org.apache.commons.math3.fraction.BigFraction;

public class HiddenBigFractionArithmetic extends FullBigFractionArithmetic {
   public BigFraction div(BigFraction a, long b) {
      BigFraction result = super.div(a, b);
      return BigFraction.ZERO.compareTo(result) <= 0 && result.compareTo(BigFraction.ONE) < 0 ? result : new BigFraction(result.longValue());
   }
}
