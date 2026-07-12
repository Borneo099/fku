package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.math.BigDecimal;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class Precision {
   public static final double EPSILON = Double.longBitsToDouble(4368491638549381120L);
   public static final double SAFE_MIN = Double.longBitsToDouble(4503599627370496L);
   private static final long EXPONENT_OFFSET = 1023L;
   private static final long SGN_MASK = Long.MIN_VALUE;
   private static final int SGN_MASK_FLOAT = Integer.MIN_VALUE;
   private static final double POSITIVE_ZERO = 0.0;
   private static final long POSITIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(0.0);
   private static final long NEGATIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(-0.0);
   private static final int POSITIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(0.0F);
   private static final int NEGATIVE_ZERO_FLOAT_BITS = Float.floatToRawIntBits(-0.0F);

   private Precision() {
   }

   public static int compareTo(double x, double y, double eps) {
      if (equals(x, y, eps)) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static int compareTo(double x, double y, int maxUlps) {
      if (equals(x, y, maxUlps)) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static boolean equals(float x, float y) {
      return equals(x, y, 1);
   }

   public static boolean equalsIncludingNaN(float x, float y) {
      return x == x && y == y ? equals(x, y, 1) : !(x != x ^ y != y);
   }

   public static boolean equals(float x, float y, float eps) {
      return equals(x, y, 1) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equalsIncludingNaN(float x, float y, float eps) {
      return equalsIncludingNaN(x, y) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equals(float x, float y, int maxUlps) {
      int xInt = Float.floatToRawIntBits(x);
      int yInt = Float.floatToRawIntBits(y);
      boolean isEqual;
      if (((xInt ^ yInt) & Integer.MIN_VALUE) == 0) {
         isEqual = FastMath.abs(xInt - yInt) <= maxUlps;
      } else {
         int deltaPlus;
         int deltaMinus;
         if (xInt < yInt) {
            deltaPlus = yInt - POSITIVE_ZERO_FLOAT_BITS;
            deltaMinus = xInt - NEGATIVE_ZERO_FLOAT_BITS;
         } else {
            deltaPlus = xInt - POSITIVE_ZERO_FLOAT_BITS;
            deltaMinus = yInt - NEGATIVE_ZERO_FLOAT_BITS;
         }

         if (deltaPlus > maxUlps) {
            isEqual = false;
         } else {
            isEqual = deltaMinus <= maxUlps - deltaPlus;
         }
      }

      return isEqual && !Float.isNaN(x) && !Float.isNaN(y);
   }

   public static boolean equalsIncludingNaN(float x, float y, int maxUlps) {
      return x == x && y == y ? equals(x, y, maxUlps) : !(x != x ^ y != y);
   }

   public static boolean equals(double x, double y) {
      return equals(x, y, 1);
   }

   public static boolean equalsIncludingNaN(double x, double y) {
      return x == x && y == y ? equals(x, y, 1) : !(x != x ^ y != y);
   }

   public static boolean equals(double x, double y, double eps) {
      return equals(x, y, 1) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equalsWithRelativeTolerance(double x, double y, double eps) {
      if (equals(x, y, 1)) {
         return true;
      } else {
         double absoluteMax = FastMath.max(FastMath.abs(x), FastMath.abs(y));
         double relativeDifference = FastMath.abs((x - y) / absoluteMax);
         return relativeDifference <= eps;
      }
   }

   public static boolean equalsIncludingNaN(double x, double y, double eps) {
      return equalsIncludingNaN(x, y) || FastMath.abs(y - x) <= eps;
   }

   public static boolean equals(double x, double y, int maxUlps) {
      long xInt = Double.doubleToRawLongBits(x);
      long yInt = Double.doubleToRawLongBits(y);
      boolean isEqual;
      if (((xInt ^ yInt) & Long.MIN_VALUE) == 0L) {
         isEqual = FastMath.abs(xInt - yInt) <= (long)maxUlps;
      } else {
         long deltaPlus;
         long deltaMinus;
         if (xInt < yInt) {
            deltaPlus = yInt - POSITIVE_ZERO_DOUBLE_BITS;
            deltaMinus = xInt - NEGATIVE_ZERO_DOUBLE_BITS;
         } else {
            deltaPlus = xInt - POSITIVE_ZERO_DOUBLE_BITS;
            deltaMinus = yInt - NEGATIVE_ZERO_DOUBLE_BITS;
         }

         if (deltaPlus > (long)maxUlps) {
            isEqual = false;
         } else {
            isEqual = deltaMinus <= (long)maxUlps - deltaPlus;
         }
      }

      return isEqual && !Double.isNaN(x) && !Double.isNaN(y);
   }

   public static boolean equalsIncludingNaN(double x, double y, int maxUlps) {
      return x == x && y == y ? equals(x, y, maxUlps) : !(x != x ^ y != y);
   }

   public static double round(double x, int scale) {
      return round(x, scale, 4);
   }

   public static double round(double x, int scale, int roundingMethod) {
      try {
         double rounded = (new BigDecimal(Double.toString(x))).setScale(scale, roundingMethod).doubleValue();
         return rounded == 0.0 ? 0.0 * x : rounded;
      } catch (NumberFormatException var6) {
         return Double.isInfinite(x) ? x : Double.NaN;
      }
   }

   public static float round(float x, int scale) {
      return round(x, scale, 4);
   }

   public static float round(float x, int scale, int roundingMethod) throws MathArithmeticException, MathIllegalArgumentException {
      float sign = FastMath.copySign(1.0F, x);
      float factor = (float)FastMath.pow(10.0, scale) * sign;
      return (float)roundUnscaled((double)(x * factor), (double)sign, roundingMethod) / factor;
   }

   private static double roundUnscaled(double unscaled, double sign, int roundingMethod) throws MathArithmeticException, MathIllegalArgumentException {
      double fraction;
      switch (roundingMethod) {
         case 0:
            if (unscaled != FastMath.floor(unscaled)) {
               unscaled = FastMath.ceil(FastMath.nextAfter(unscaled, Double.POSITIVE_INFINITY));
            }
            break;
         case 1:
            unscaled = FastMath.floor(FastMath.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            break;
         case 2:
            if (sign == -1.0) {
               unscaled = FastMath.floor(FastMath.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            } else {
               unscaled = FastMath.ceil(FastMath.nextAfter(unscaled, Double.POSITIVE_INFINITY));
            }
            break;
         case 3:
            if (sign == -1.0) {
               unscaled = FastMath.ceil(FastMath.nextAfter(unscaled, Double.POSITIVE_INFINITY));
            } else {
               unscaled = FastMath.floor(FastMath.nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            }
            break;
         case 4:
            unscaled = FastMath.nextAfter(unscaled, Double.POSITIVE_INFINITY);
            fraction = unscaled - FastMath.floor(unscaled);
            if (fraction >= 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else {
               unscaled = FastMath.floor(unscaled);
            }
            break;
         case 5:
            unscaled = FastMath.nextAfter(unscaled, Double.NEGATIVE_INFINITY);
            fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else {
               unscaled = FastMath.floor(unscaled);
            }
            break;
         case 6:
            fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
               unscaled = FastMath.ceil(unscaled);
            } else if (fraction < 0.5) {
               unscaled = FastMath.floor(unscaled);
            } else if (FastMath.floor(unscaled) / 2.0 == FastMath.floor(FastMath.floor(unscaled) / 2.0)) {
               unscaled = FastMath.floor(unscaled);
            } else {
               unscaled = FastMath.ceil(unscaled);
            }
            break;
         case 7:
            if (unscaled != FastMath.floor(unscaled)) {
               throw new MathArithmeticException();
            }
            break;
         default:
            throw new MathIllegalArgumentException(LocalizedFormats.INVALID_ROUNDING_METHOD, new Object[]{roundingMethod, "ROUND_CEILING", 2, "ROUND_DOWN", 1, "ROUND_FLOOR", 3, "ROUND_HALF_DOWN", 5, "ROUND_HALF_EVEN", 6, "ROUND_HALF_UP", 4, "ROUND_UNNECESSARY", 7, "ROUND_UP", 0});
      }

      return unscaled;
   }

   public static double representableDelta(double x, double originalDelta) {
      return x + originalDelta - x;
   }
}
