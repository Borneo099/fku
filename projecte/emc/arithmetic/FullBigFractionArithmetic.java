package moze_intel.projecte.emc.arithmetic;

import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.shaded.org.apache.commons.math3.fraction.BigFraction;
import moze_intel.projecte.utils.Constants;

public class FullBigFractionArithmetic implements IValueArithmetic {
   public boolean isZero(BigFraction value) {
      return BigFraction.ZERO.equals(value);
   }

   public BigFraction getZero() {
      return BigFraction.ZERO;
   }

   public BigFraction add(BigFraction a, BigFraction b) {
      if (this.isFree(a)) {
         return b;
      } else {
         return this.isFree(b) ? a : a.add(b);
      }
   }

   public BigFraction mul(long a, BigFraction b) {
      return this.isFree(b) ? this.getFree() : b.multiply(a);
   }

   public BigFraction div(BigFraction a, long b) {
      if (this.isFree(a)) {
         return this.getFree();
      } else if (b == 0L) {
         return BigFraction.ZERO;
      } else {
         BigFraction result = a.divide(b);
         return result.getNumerator().compareTo(Constants.MAX_LONG) <= 0 && result.getDenominator().compareTo(Constants.MAX_LONG) <= 0 ? result : BigFraction.ZERO;
      }
   }

   public BigFraction getFree() {
      return new BigFraction(Long.MIN_VALUE);
   }

   public boolean isFree(BigFraction value) {
      return value.getNumeratorAsLong() == Long.MIN_VALUE;
   }
}
