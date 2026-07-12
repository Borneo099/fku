package moze_intel.projecte.shaded.org.apache.commons.math3.fraction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import moze_intel.projecte.shaded.org.apache.commons.math3.FieldElement;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NullArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.ZeroException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.ArithmeticUtils;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.MathUtils;

public class BigFraction extends Number implements FieldElement, Comparable, Serializable {
   public static final BigFraction TWO = new BigFraction(2);
   public static final BigFraction ONE = new BigFraction(1);
   public static final BigFraction ZERO = new BigFraction(0);
   public static final BigFraction MINUS_ONE = new BigFraction(-1);
   public static final BigFraction FOUR_FIFTHS = new BigFraction(4, 5);
   public static final BigFraction ONE_FIFTH = new BigFraction(1, 5);
   public static final BigFraction ONE_HALF = new BigFraction(1, 2);
   public static final BigFraction ONE_QUARTER = new BigFraction(1, 4);
   public static final BigFraction ONE_THIRD = new BigFraction(1, 3);
   public static final BigFraction THREE_FIFTHS = new BigFraction(3, 5);
   public static final BigFraction THREE_QUARTERS = new BigFraction(3, 4);
   public static final BigFraction TWO_FIFTHS = new BigFraction(2, 5);
   public static final BigFraction TWO_QUARTERS = new BigFraction(2, 4);
   public static final BigFraction TWO_THIRDS = new BigFraction(2, 3);
   private static final long serialVersionUID = -5630213147331578515L;
   private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100L);
   private final BigInteger numerator;
   private final BigInteger denominator;

   public BigFraction(BigInteger num) {
      this(num, BigInteger.ONE);
   }

   public BigFraction(BigInteger num, BigInteger den) {
      MathUtils.checkNotNull(num, LocalizedFormats.NUMERATOR);
      MathUtils.checkNotNull(den, LocalizedFormats.DENOMINATOR);
      if (den.signum() == 0) {
         throw new ZeroException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         if (num.signum() == 0) {
            this.numerator = BigInteger.ZERO;
            this.denominator = BigInteger.ONE;
         } else {
            BigInteger gcd = num.gcd(den);
            if (BigInteger.ONE.compareTo(gcd) < 0) {
               num = num.divide(gcd);
               den = den.divide(gcd);
            }

            if (den.signum() == -1) {
               num = num.negate();
               den = den.negate();
            }

            this.numerator = num;
            this.denominator = den;
         }

      }
   }

   public BigFraction(double value) throws MathIllegalArgumentException {
      if (Double.isNaN(value)) {
         throw new MathIllegalArgumentException(LocalizedFormats.NAN_VALUE_CONVERSION, new Object[0]);
      } else if (Double.isInfinite(value)) {
         throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_VALUE_CONVERSION, new Object[0]);
      } else {
         long bits = Double.doubleToLongBits(value);
         long sign = bits & Long.MIN_VALUE;
         long exponent = bits & 9218868437227405312L;
         long m = bits & 4503599627370495L;
         if (exponent != 0L) {
            m |= 4503599627370496L;
         }

         if (sign != 0L) {
            m = -m;
         }

         int k;
         for(k = (int)(exponent >> 52) - 1075; (m & 9007199254740990L) != 0L && (m & 1L) == 0L; ++k) {
            m >>= 1;
         }

         if (k < 0) {
            this.numerator = BigInteger.valueOf(m);
            this.denominator = BigInteger.ZERO.flipBit(-k);
         } else {
            this.numerator = BigInteger.valueOf(m).multiply(BigInteger.ZERO.flipBit(k));
            this.denominator = BigInteger.ONE;
         }

      }
   }

   public BigFraction(double value, double epsilon, int maxIterations) throws FractionConversionException {
      this(value, epsilon, Integer.MAX_VALUE, maxIterations);
   }

   private BigFraction(double value, double epsilon, int maxDenominator, int maxIterations) throws FractionConversionException {
      long overflow = 2147483647L;
      double r0 = value;
      long a0 = (long)FastMath.floor(value);
      if (FastMath.abs(a0) > overflow) {
         throw new FractionConversionException(value, a0, 1L);
      } else if (FastMath.abs((double)a0 - value) < epsilon) {
         this.numerator = BigInteger.valueOf(a0);
         this.denominator = BigInteger.ONE;
      } else {
         long p0 = 1L;
         long q0 = 0L;
         long p1 = a0;
         long q1 = 1L;
         long p2 = 0L;
         long q2 = 1L;
         int n = 0;
         boolean stop = false;

         while(true) {
            ++n;
            double r1 = 1.0 / (r0 - (double)a0);
            long a1 = (long)FastMath.floor(r1);
            p2 = a1 * p1 + p0;
            q2 = a1 * q1 + q0;
            if (p2 <= overflow && q2 <= overflow) {
               double convergent = (double)p2 / (double)q2;
               if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < (long)maxDenominator) {
                  p0 = p1;
                  p1 = p2;
                  q0 = q1;
                  q1 = q2;
                  a0 = a1;
                  r0 = r1;
               } else {
                  stop = true;
               }

               if (!stop) {
                  continue;
               }
               break;
            }

            if (epsilon != 0.0 || FastMath.abs(q1) >= (long)maxDenominator) {
               throw new FractionConversionException(value, p2, q2);
            }
            break;
         }

         if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
         } else {
            if (q2 < (long)maxDenominator) {
               this.numerator = BigInteger.valueOf(p2);
               this.denominator = BigInteger.valueOf(q2);
            } else {
               this.numerator = BigInteger.valueOf(p1);
               this.denominator = BigInteger.valueOf(q1);
            }

         }
      }
   }

   public BigFraction(double value, int maxDenominator) throws FractionConversionException {
      this(value, 0.0, maxDenominator, 100);
   }

   public BigFraction(int num) {
      this(BigInteger.valueOf((long)num), BigInteger.ONE);
   }

   public BigFraction(int num, int den) {
      this(BigInteger.valueOf((long)num), BigInteger.valueOf((long)den));
   }

   public BigFraction(long num) {
      this(BigInteger.valueOf(num), BigInteger.ONE);
   }

   public BigFraction(long num, long den) {
      this(BigInteger.valueOf(num), BigInteger.valueOf(den));
   }

   public static BigFraction getReducedFraction(int numerator, int denominator) {
      return numerator == 0 ? ZERO : new BigFraction(numerator, denominator);
   }

   public BigFraction abs() {
      return this.numerator.signum() == 1 ? this : this.negate();
   }

   public BigFraction add(BigInteger bg) throws NullArgumentException {
      MathUtils.checkNotNull(bg);
      if (this.numerator.signum() == 0) {
         return new BigFraction(bg);
      } else {
         return bg.signum() == 0 ? this : new BigFraction(this.numerator.add(this.denominator.multiply(bg)), this.denominator);
      }
   }

   public BigFraction add(int i) {
      return this.add(BigInteger.valueOf((long)i));
   }

   public BigFraction add(long l) {
      return this.add(BigInteger.valueOf(l));
   }

   public BigFraction add(BigFraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
      } else if (fraction.numerator.signum() == 0) {
         return this;
      } else if (this.numerator.signum() == 0) {
         return fraction;
      } else {
         BigInteger num = null;
         BigInteger den = null;
         if (this.denominator.equals(fraction.denominator)) {
            num = this.numerator.add(fraction.numerator);
            den = this.denominator;
         } else {
            num = this.numerator.multiply(fraction.denominator).add(fraction.numerator.multiply(this.denominator));
            den = this.denominator.multiply(fraction.denominator);
         }

         return num.signum() == 0 ? ZERO : new BigFraction(num, den);
      }
   }

   public BigDecimal bigDecimalValue() {
      return (new BigDecimal(this.numerator)).divide(new BigDecimal(this.denominator));
   }

   public BigDecimal bigDecimalValue(int roundingMode) {
      return (new BigDecimal(this.numerator)).divide(new BigDecimal(this.denominator), roundingMode);
   }

   public BigDecimal bigDecimalValue(int scale, int roundingMode) {
      return (new BigDecimal(this.numerator)).divide(new BigDecimal(this.denominator), scale, roundingMode);
   }

   public int compareTo(BigFraction object) {
      int lhsSigNum = this.numerator.signum();
      int rhsSigNum = object.numerator.signum();
      if (lhsSigNum != rhsSigNum) {
         return lhsSigNum > rhsSigNum ? 1 : -1;
      } else if (lhsSigNum == 0) {
         return 0;
      } else {
         BigInteger nOd = this.numerator.multiply(object.denominator);
         BigInteger dOn = this.denominator.multiply(object.numerator);
         return nOd.compareTo(dOn);
      }
   }

   public BigFraction divide(BigInteger bg) {
      if (bg == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
      } else if (bg.signum() == 0) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         return this.numerator.signum() == 0 ? ZERO : new BigFraction(this.numerator, this.denominator.multiply(bg));
      }
   }

   public BigFraction divide(int i) {
      return this.divide(BigInteger.valueOf((long)i));
   }

   public BigFraction divide(long l) {
      return this.divide(BigInteger.valueOf(l));
   }

   public BigFraction divide(BigFraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
      } else if (fraction.numerator.signum() == 0) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         return this.numerator.signum() == 0 ? ZERO : this.multiply(fraction.reciprocal());
      }
   }

   public double doubleValue() {
      double result = this.numerator.doubleValue() / this.denominator.doubleValue();
      if (Double.isNaN(result)) {
         int shift = FastMath.max(this.numerator.bitLength(), this.denominator.bitLength()) - FastMath.getExponent(Double.MAX_VALUE);
         result = this.numerator.shiftRight(shift).doubleValue() / this.denominator.shiftRight(shift).doubleValue();
      }

      return result;
   }

   public boolean equals(Object other) {
      boolean ret = false;
      if (this == other) {
         ret = true;
      } else if (other instanceof BigFraction) {
         BigFraction rhs = ((BigFraction)other).reduce();
         BigFraction thisOne = this.reduce();
         ret = thisOne.numerator.equals(rhs.numerator) && thisOne.denominator.equals(rhs.denominator);
      }

      return ret;
   }

   public float floatValue() {
      float result = this.numerator.floatValue() / this.denominator.floatValue();
      if (Double.isNaN((double)result)) {
         int shift = FastMath.max(this.numerator.bitLength(), this.denominator.bitLength()) - FastMath.getExponent(Float.MAX_VALUE);
         result = this.numerator.shiftRight(shift).floatValue() / this.denominator.shiftRight(shift).floatValue();
      }

      return result;
   }

   public BigInteger getDenominator() {
      return this.denominator;
   }

   public int getDenominatorAsInt() {
      return this.denominator.intValue();
   }

   public long getDenominatorAsLong() {
      return this.denominator.longValue();
   }

   public BigInteger getNumerator() {
      return this.numerator;
   }

   public int getNumeratorAsInt() {
      return this.numerator.intValue();
   }

   public long getNumeratorAsLong() {
      return this.numerator.longValue();
   }

   public int hashCode() {
      return 37 * (629 + this.numerator.hashCode()) + this.denominator.hashCode();
   }

   public int intValue() {
      return this.numerator.divide(this.denominator).intValue();
   }

   public long longValue() {
      return this.numerator.divide(this.denominator).longValue();
   }

   public BigFraction multiply(BigInteger bg) {
      if (bg == null) {
         throw new NullArgumentException();
      } else {
         return this.numerator.signum() != 0 && bg.signum() != 0 ? new BigFraction(bg.multiply(this.numerator), this.denominator) : ZERO;
      }
   }

   public BigFraction multiply(int i) {
      return i != 0 && this.numerator.signum() != 0 ? this.multiply(BigInteger.valueOf((long)i)) : ZERO;
   }

   public BigFraction multiply(long l) {
      return l != 0L && this.numerator.signum() != 0 ? this.multiply(BigInteger.valueOf(l)) : ZERO;
   }

   public BigFraction multiply(BigFraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
      } else {
         return this.numerator.signum() != 0 && fraction.numerator.signum() != 0 ? new BigFraction(this.numerator.multiply(fraction.numerator), this.denominator.multiply(fraction.denominator)) : ZERO;
      }
   }

   public BigFraction negate() {
      return new BigFraction(this.numerator.negate(), this.denominator);
   }

   public double percentageValue() {
      return this.multiply(ONE_HUNDRED).doubleValue();
   }

   public BigFraction pow(int exponent) {
      if (exponent == 0) {
         return ONE;
      } else if (this.numerator.signum() == 0) {
         return this;
      } else {
         return exponent < 0 ? new BigFraction(this.denominator.pow(-exponent), this.numerator.pow(-exponent)) : new BigFraction(this.numerator.pow(exponent), this.denominator.pow(exponent));
      }
   }

   public BigFraction pow(long exponent) {
      if (exponent == 0L) {
         return ONE;
      } else if (this.numerator.signum() == 0) {
         return this;
      } else {
         return exponent < 0L ? new BigFraction(ArithmeticUtils.pow(this.denominator, -exponent), ArithmeticUtils.pow(this.numerator, -exponent)) : new BigFraction(ArithmeticUtils.pow(this.numerator, exponent), ArithmeticUtils.pow(this.denominator, exponent));
      }
   }

   public BigFraction pow(BigInteger exponent) {
      if (exponent.signum() == 0) {
         return ONE;
      } else if (this.numerator.signum() == 0) {
         return this;
      } else if (exponent.signum() == -1) {
         BigInteger eNeg = exponent.negate();
         return new BigFraction(ArithmeticUtils.pow(this.denominator, eNeg), ArithmeticUtils.pow(this.numerator, eNeg));
      } else {
         return new BigFraction(ArithmeticUtils.pow(this.numerator, exponent), ArithmeticUtils.pow(this.denominator, exponent));
      }
   }

   public double pow(double exponent) {
      return FastMath.pow(this.numerator.doubleValue(), exponent) / FastMath.pow(this.denominator.doubleValue(), exponent);
   }

   public BigFraction reciprocal() {
      return new BigFraction(this.denominator, this.numerator);
   }

   public BigFraction reduce() {
      BigInteger gcd = this.numerator.gcd(this.denominator);
      return BigInteger.ONE.compareTo(gcd) < 0 ? new BigFraction(this.numerator.divide(gcd), this.denominator.divide(gcd)) : this;
   }

   public BigFraction subtract(BigInteger bg) {
      if (bg == null) {
         throw new NullArgumentException();
      } else if (bg.signum() == 0) {
         return this;
      } else {
         return this.numerator.signum() == 0 ? new BigFraction(bg.negate()) : new BigFraction(this.numerator.subtract(this.denominator.multiply(bg)), this.denominator);
      }
   }

   public BigFraction subtract(int i) {
      return this.subtract(BigInteger.valueOf((long)i));
   }

   public BigFraction subtract(long l) {
      return this.subtract(BigInteger.valueOf(l));
   }

   public BigFraction subtract(BigFraction fraction) {
      if (fraction == null) {
         throw new NullArgumentException(LocalizedFormats.FRACTION, new Object[0]);
      } else if (fraction.numerator.signum() == 0) {
         return this;
      } else if (this.numerator.signum() == 0) {
         return fraction.negate();
      } else {
         BigInteger num = null;
         BigInteger den = null;
         if (this.denominator.equals(fraction.denominator)) {
            num = this.numerator.subtract(fraction.numerator);
            den = this.denominator;
         } else {
            num = this.numerator.multiply(fraction.denominator).subtract(fraction.numerator.multiply(this.denominator));
            den = this.denominator.multiply(fraction.denominator);
         }

         return new BigFraction(num, den);
      }
   }

   public String toString() {
      String str = null;
      if (BigInteger.ONE.equals(this.denominator)) {
         str = this.numerator.toString();
      } else if (BigInteger.ZERO.equals(this.numerator)) {
         str = "0";
      } else {
         str = this.numerator + " / " + this.denominator;
      }

      return str;
   }

   public BigFractionField getField() {
      return BigFractionField.getInstance();
   }
}
