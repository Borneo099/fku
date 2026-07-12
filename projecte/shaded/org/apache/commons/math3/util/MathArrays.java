package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import moze_intel.projecte.shaded.org.apache.commons.math3.Field;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.UniformIntegerDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.DimensionMismatchException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathInternalError;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NoDataException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NonMonotonicSequenceException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotANumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NullArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;

public class MathArrays {
   private MathArrays() {
   }

   public static double[] scale(double val, double[] arr) {
      double[] newArr = new double[arr.length];

      for(int i = 0; i < arr.length; ++i) {
         newArr[i] = arr[i] * val;
      }

      return newArr;
   }

   public static void scaleInPlace(double val, double[] arr) {
      for(int i = 0; i < arr.length; ++i) {
         arr[i] *= val;
      }

   }

   public static double[] ebeAdd(double[] a, double[] b) throws DimensionMismatchException {
      checkEqualLength(a, b);
      double[] result = (double[])a.clone();

      for(int i = 0; i < a.length; ++i) {
         result[i] += b[i];
      }

      return result;
   }

   public static double[] ebeSubtract(double[] a, double[] b) throws DimensionMismatchException {
      checkEqualLength(a, b);
      double[] result = (double[])a.clone();

      for(int i = 0; i < a.length; ++i) {
         result[i] -= b[i];
      }

      return result;
   }

   public static double[] ebeMultiply(double[] a, double[] b) throws DimensionMismatchException {
      checkEqualLength(a, b);
      double[] result = (double[])a.clone();

      for(int i = 0; i < a.length; ++i) {
         result[i] *= b[i];
      }

      return result;
   }

   public static double[] ebeDivide(double[] a, double[] b) throws DimensionMismatchException {
      checkEqualLength(a, b);
      double[] result = (double[])a.clone();

      for(int i = 0; i < a.length; ++i) {
         result[i] /= b[i];
      }

      return result;
   }

   public static double distance1(double[] p1, double[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         sum += FastMath.abs(p1[i] - p2[i]);
      }

      return sum;
   }

   public static int distance1(int[] p1, int[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      int sum = 0;

      for(int i = 0; i < p1.length; ++i) {
         sum += FastMath.abs(p1[i] - p2[i]);
      }

      return sum;
   }

   public static double distance(double[] p1, double[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         double dp = p1[i] - p2[i];
         sum += dp * dp;
      }

      return FastMath.sqrt(sum);
   }

   public static double cosAngle(double[] v1, double[] v2) {
      return linearCombination(v1, v2) / (safeNorm(v1) * safeNorm(v2));
   }

   public static double distance(int[] p1, int[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      double sum = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         double dp = (double)(p1[i] - p2[i]);
         sum += dp * dp;
      }

      return FastMath.sqrt(sum);
   }

   public static double distanceInf(double[] p1, double[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      double max = 0.0;

      for(int i = 0; i < p1.length; ++i) {
         max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
      }

      return max;
   }

   public static int distanceInf(int[] p1, int[] p2) throws DimensionMismatchException {
      checkEqualLength(p1, p2);
      int max = 0;

      for(int i = 0; i < p1.length; ++i) {
         max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
      }

      return max;
   }

   public static boolean isMonotonic(Comparable[] val, OrderDirection dir, boolean strict) {
      Comparable previous = val[0];
      int max = val.length;

      for(int i = 1; i < max; ++i) {
         int comp;
         switch (dir) {
            case INCREASING:
               comp = previous.compareTo(val[i]);
               if (strict) {
                  if (comp >= 0) {
                     return false;
                  }
               } else if (comp > 0) {
                  return false;
               }
               break;
            case DECREASING:
               comp = val[i].compareTo(previous);
               if (strict) {
                  if (comp >= 0) {
                     return false;
                  }
               } else if (comp > 0) {
                  return false;
               }
               break;
            default:
               throw new MathInternalError();
         }

         previous = val[i];
      }

      return true;
   }

   public static boolean isMonotonic(double[] val, OrderDirection dir, boolean strict) {
      return checkOrder(val, dir, strict, false);
   }

   public static boolean checkEqualLength(double[] a, double[] b, boolean abort) {
      if (a.length == b.length) {
         return true;
      } else if (abort) {
         throw new DimensionMismatchException(a.length, b.length);
      } else {
         return false;
      }
   }

   public static void checkEqualLength(double[] a, double[] b) {
      checkEqualLength(a, b, true);
   }

   public static boolean checkEqualLength(int[] a, int[] b, boolean abort) {
      if (a.length == b.length) {
         return true;
      } else if (abort) {
         throw new DimensionMismatchException(a.length, b.length);
      } else {
         return false;
      }
   }

   public static void checkEqualLength(int[] a, int[] b) {
      checkEqualLength(a, b, true);
   }

   public static boolean checkOrder(double[] val, OrderDirection dir, boolean strict, boolean abort) throws NonMonotonicSequenceException {
      double previous = val[0];
      int max = val.length;

      int index;
      label40:
      for(index = 1; index < max; ++index) {
         switch (dir) {
            case INCREASING:
               if (strict) {
                  if (val[index] <= previous) {
                     break label40;
                  }
               } else if (val[index] < previous) {
                  break label40;
               }
               break;
            case DECREASING:
               if (strict) {
                  if (val[index] >= previous) {
                     break label40;
                  }
               } else if (val[index] > previous) {
                  break label40;
               }
               break;
            default:
               throw new MathInternalError();
         }

         previous = val[index];
      }

      if (index == max) {
         return true;
      } else if (abort) {
         throw new NonMonotonicSequenceException(val[index], previous, index, dir, strict);
      } else {
         return false;
      }
   }

   public static void checkOrder(double[] val, OrderDirection dir, boolean strict) throws NonMonotonicSequenceException {
      checkOrder(val, dir, strict, true);
   }

   public static void checkOrder(double[] val) throws NonMonotonicSequenceException {
      checkOrder(val, MathArrays.OrderDirection.INCREASING, true);
   }

   public static void checkRectangular(long[][] in) throws NullArgumentException, DimensionMismatchException {
      MathUtils.checkNotNull(in);

      for(int i = 1; i < in.length; ++i) {
         if (in[i].length != in[0].length) {
            throw new DimensionMismatchException(LocalizedFormats.DIFFERENT_ROWS_LENGTHS, in[i].length, in[0].length);
         }
      }

   }

   public static void checkPositive(double[] in) throws NotStrictlyPositiveException {
      for(int i = 0; i < in.length; ++i) {
         if (in[i] <= 0.0) {
            throw new NotStrictlyPositiveException(in[i]);
         }
      }

   }

   public static void checkNotNaN(double[] in) throws NotANumberException {
      for(int i = 0; i < in.length; ++i) {
         if (Double.isNaN(in[i])) {
            throw new NotANumberException();
         }
      }

   }

   public static void checkNonNegative(long[] in) throws NotPositiveException {
      for(int i = 0; i < in.length; ++i) {
         if (in[i] < 0L) {
            throw new NotPositiveException(in[i]);
         }
      }

   }

   public static void checkNonNegative(long[][] in) throws NotPositiveException {
      for(int i = 0; i < in.length; ++i) {
         for(int j = 0; j < in[i].length; ++j) {
            if (in[i][j] < 0L) {
               throw new NotPositiveException(in[i][j]);
            }
         }
      }

   }

   public static double safeNorm(double[] v) {
      double rdwarf = 3.834E-20;
      double rgiant = 1.304E19;
      double s1 = 0.0;
      double s2 = 0.0;
      double s3 = 0.0;
      double x1max = 0.0;
      double x3max = 0.0;
      double floatn = (double)v.length;
      double agiant = rgiant / floatn;

      for(int i = 0; i < v.length; ++i) {
         double xabs = FastMath.abs(v[i]);
         if (!(xabs < rdwarf) && !(xabs > agiant)) {
            s2 += xabs * xabs;
         } else {
            double r;
            if (xabs > rdwarf) {
               if (xabs > x1max) {
                  r = x1max / xabs;
                  s1 = 1.0 + s1 * r * r;
                  x1max = xabs;
               } else {
                  r = xabs / x1max;
                  s1 += r * r;
               }
            } else if (xabs > x3max) {
               r = x3max / xabs;
               s3 = 1.0 + s3 * r * r;
               x3max = xabs;
            } else if (xabs != 0.0) {
               r = xabs / x3max;
               s3 += r * r;
            }
         }
      }

      double norm;
      if (s1 != 0.0) {
         norm = x1max * Math.sqrt(s1 + s2 / x1max / x1max);
      } else if (s2 == 0.0) {
         norm = x3max * Math.sqrt(s3);
      } else if (s2 >= x3max) {
         norm = Math.sqrt(s2 * (1.0 + x3max / s2 * x3max * s3));
      } else {
         norm = Math.sqrt(x3max * (s2 / x3max + x3max * s3));
      }

      return norm;
   }

   public static void sortInPlace(double[] x, double[]... yList) throws DimensionMismatchException, NullArgumentException {
      sortInPlace(x, MathArrays.OrderDirection.INCREASING, yList);
   }

   public static void sortInPlace(double[] x, OrderDirection dir, double[]... yList) throws NullArgumentException, DimensionMismatchException {
      if (x == null) {
         throw new NullArgumentException();
      } else {
         int yListLen = yList.length;
         int len = x.length;

         for(int j = 0; j < yListLen; ++j) {
            double[] y = yList[j];
            if (y == null) {
               throw new NullArgumentException();
            }

            if (y.length != len) {
               throw new DimensionMismatchException(y.length, len);
            }
         }

         List list = new ArrayList(len);

         for(int i = 0; i < len; ++i) {
            list.add(new PairDoubleInteger(x[i], i));
         }

         Comparator comp = dir == MathArrays.OrderDirection.INCREASING ? new Comparator() {
            public int compare(PairDoubleInteger o1, PairDoubleInteger o2) {
               return Double.compare(o1.getKey(), o2.getKey());
            }
         } : new Comparator() {
            public int compare(PairDoubleInteger o1, PairDoubleInteger o2) {
               return Double.compare(o2.getKey(), o1.getKey());
            }
         };
         Collections.sort(list, comp);
         int[] indices = new int[len];

         int j;
         for(j = 0; j < len; ++j) {
            PairDoubleInteger e = (PairDoubleInteger)list.get(j);
            x[j] = e.getKey();
            indices[j] = e.getValue();
         }

         for(j = 0; j < yListLen; ++j) {
            double[] yInPlace = yList[j];
            double[] yOrig = (double[])yInPlace.clone();

            for(int i = 0; i < len; ++i) {
               yInPlace[i] = yOrig[indices[i]];
            }
         }

      }
   }

   public static int[] copyOf(int[] source) {
      return copyOf(source, source.length);
   }

   public static double[] copyOf(double[] source) {
      return copyOf(source, source.length);
   }

   public static int[] copyOf(int[] source, int len) {
      int[] output = new int[len];
      System.arraycopy(source, 0, output, 0, FastMath.min(len, source.length));
      return output;
   }

   public static double[] copyOf(double[] source, int len) {
      double[] output = new double[len];
      System.arraycopy(source, 0, output, 0, FastMath.min(len, source.length));
      return output;
   }

   public static double[] copyOfRange(double[] source, int from, int to) {
      int len = to - from;
      double[] output = new double[len];
      System.arraycopy(source, from, output, 0, FastMath.min(len, source.length - from));
      return output;
   }

   public static double linearCombination(double[] a, double[] b) throws DimensionMismatchException {
      checkEqualLength(a, b);
      int len = a.length;
      if (len == 1) {
         return a[0] * b[0];
      } else {
         double[] prodHigh = new double[len];
         double prodLowSum = 0.0;

         double result;
         for(int i = 0; i < len; ++i) {
            double ai = a[i];
            double aHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(ai) & -134217728L);
            double aLow = ai - aHigh;
            double bi = b[i];
            double bHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(bi) & -134217728L);
            result = bi - bHigh;
            prodHigh[i] = ai * bi;
            double prodLow = aLow * result - (prodHigh[i] - aHigh * bHigh - aLow * bHigh - aHigh * result);
            prodLowSum += prodLow;
         }

         double prodHighCur = prodHigh[0];
         double prodHighNext = prodHigh[1];
         double sHighPrev = prodHighCur + prodHighNext;
         double sPrime = sHighPrev - prodHighNext;
         double sLowSum = prodHighNext - (sHighPrev - sPrime) + (prodHighCur - sPrime);
         int lenMinusOne = len - 1;

         for(int i = 1; i < lenMinusOne; ++i) {
            prodHighNext = prodHigh[i + 1];
            double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += prodHighNext - (sHighCur - sPrime) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
         }

         result = sHighPrev + prodLowSum + sLowSum;
         if (Double.isNaN(result)) {
            result = 0.0;

            for(int i = 0; i < len; ++i) {
               result += a[i] * b[i];
            }
         }

         return result;
      }
   }

   public static double linearCombination(double a1, double b1, double a2, double b2) {
      double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & -134217728L);
      double a1Low = a1 - a1High;
      double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & -134217728L);
      double b1Low = b1 - b1High;
      double prod1High = a1 * b1;
      double prod1Low = a1Low * b1Low - (prod1High - a1High * b1High - a1Low * b1High - a1High * b1Low);
      double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & -134217728L);
      double a2Low = a2 - a2High;
      double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & -134217728L);
      double b2Low = b2 - b2High;
      double prod2High = a2 * b2;
      double prod2Low = a2Low * b2Low - (prod2High - a2High * b2High - a2Low * b2High - a2High * b2Low);
      double s12High = prod1High + prod2High;
      double s12Prime = s12High - prod2High;
      double s12Low = prod2High - (s12High - s12Prime) + (prod1High - s12Prime);
      double result = s12High + prod1Low + prod2Low + s12Low;
      if (Double.isNaN(result)) {
         result = a1 * b1 + a2 * b2;
      }

      return result;
   }

   public static double linearCombination(double a1, double b1, double a2, double b2, double a3, double b3) {
      double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & -134217728L);
      double a1Low = a1 - a1High;
      double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & -134217728L);
      double b1Low = b1 - b1High;
      double prod1High = a1 * b1;
      double prod1Low = a1Low * b1Low - (prod1High - a1High * b1High - a1Low * b1High - a1High * b1Low);
      double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & -134217728L);
      double a2Low = a2 - a2High;
      double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & -134217728L);
      double b2Low = b2 - b2High;
      double prod2High = a2 * b2;
      double prod2Low = a2Low * b2Low - (prod2High - a2High * b2High - a2Low * b2High - a2High * b2Low);
      double a3High = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & -134217728L);
      double a3Low = a3 - a3High;
      double b3High = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & -134217728L);
      double b3Low = b3 - b3High;
      double prod3High = a3 * b3;
      double prod3Low = a3Low * b3Low - (prod3High - a3High * b3High - a3Low * b3High - a3High * b3Low);
      double s12High = prod1High + prod2High;
      double s12Prime = s12High - prod2High;
      double s12Low = prod2High - (s12High - s12Prime) + (prod1High - s12Prime);
      double s123High = s12High + prod3High;
      double s123Prime = s123High - prod3High;
      double s123Low = prod3High - (s123High - s123Prime) + (s12High - s123Prime);
      double result = s123High + prod1Low + prod2Low + prod3Low + s12Low + s123Low;
      if (Double.isNaN(result)) {
         result = a1 * b1 + a2 * b2 + a3 * b3;
      }

      return result;
   }

   public static double linearCombination(double a1, double b1, double a2, double b2, double a3, double b3, double a4, double b4) {
      double a1High = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & -134217728L);
      double a1Low = a1 - a1High;
      double b1High = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & -134217728L);
      double b1Low = b1 - b1High;
      double prod1High = a1 * b1;
      double prod1Low = a1Low * b1Low - (prod1High - a1High * b1High - a1Low * b1High - a1High * b1Low);
      double a2High = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & -134217728L);
      double a2Low = a2 - a2High;
      double b2High = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & -134217728L);
      double b2Low = b2 - b2High;
      double prod2High = a2 * b2;
      double prod2Low = a2Low * b2Low - (prod2High - a2High * b2High - a2Low * b2High - a2High * b2Low);
      double a3High = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & -134217728L);
      double a3Low = a3 - a3High;
      double b3High = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & -134217728L);
      double b3Low = b3 - b3High;
      double prod3High = a3 * b3;
      double prod3Low = a3Low * b3Low - (prod3High - a3High * b3High - a3Low * b3High - a3High * b3Low);
      double a4High = Double.longBitsToDouble(Double.doubleToRawLongBits(a4) & -134217728L);
      double a4Low = a4 - a4High;
      double b4High = Double.longBitsToDouble(Double.doubleToRawLongBits(b4) & -134217728L);
      double b4Low = b4 - b4High;
      double prod4High = a4 * b4;
      double prod4Low = a4Low * b4Low - (prod4High - a4High * b4High - a4Low * b4High - a4High * b4Low);
      double s12High = prod1High + prod2High;
      double s12Prime = s12High - prod2High;
      double s12Low = prod2High - (s12High - s12Prime) + (prod1High - s12Prime);
      double s123High = s12High + prod3High;
      double s123Prime = s123High - prod3High;
      double s123Low = prod3High - (s123High - s123Prime) + (s12High - s123Prime);
      double s1234High = s123High + prod4High;
      double s1234Prime = s1234High - prod4High;
      double s1234Low = prod4High - (s1234High - s1234Prime) + (s123High - s1234Prime);
      double result = s1234High + prod1Low + prod2Low + prod3Low + prod4Low + s12Low + s123Low + s1234Low;
      if (Double.isNaN(result)) {
         result = a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4;
      }

      return result;
   }

   public static boolean equals(float[] x, float[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!Precision.equals(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equalsIncludingNaN(float[] x, float[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!Precision.equalsIncludingNaN(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equals(double[] x, double[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!Precision.equals(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static boolean equalsIncludingNaN(double[] x, double[] y) {
      if (x != null && y != null) {
         if (x.length != y.length) {
            return false;
         } else {
            for(int i = 0; i < x.length; ++i) {
               if (!Precision.equalsIncludingNaN(x[i], y[i])) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return !(x == null ^ y == null);
      }
   }

   public static double[] normalizeArray(double[] values, double normalizedSum) throws MathIllegalArgumentException, MathArithmeticException {
      if (Double.isInfinite(normalizedSum)) {
         throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE, new Object[0]);
      } else if (Double.isNaN(normalizedSum)) {
         throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN, new Object[0]);
      } else {
         double sum = 0.0;
         int len = values.length;
         double[] out = new double[len];

         int i;
         for(i = 0; i < len; ++i) {
            if (Double.isInfinite(values[i])) {
               throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_ARRAY_ELEMENT, new Object[]{values[i], i});
            }

            if (!Double.isNaN(values[i])) {
               sum += values[i];
            }
         }

         if (sum == 0.0) {
            throw new MathArithmeticException(LocalizedFormats.ARRAY_SUMS_TO_ZERO, new Object[0]);
         } else {
            for(i = 0; i < len; ++i) {
               if (Double.isNaN(values[i])) {
                  out[i] = Double.NaN;
               } else {
                  out[i] = values[i] * normalizedSum / sum;
               }
            }

            return out;
         }
      }
   }

   public static Object[] buildArray(Field field, int length) {
      Object[] array = (Object[])((Object[])Array.newInstance(field.getRuntimeClass(), length));
      Arrays.fill(array, field.getZero());
      return array;
   }

   public static Object[][] buildArray(Field field, int rows, int columns) {
      Object[][] array;
      if (columns < 0) {
         Object[] dummyRow = buildArray(field, 0);
         array = (Object[][])((Object[][])Array.newInstance(dummyRow.getClass(), rows));
      } else {
         array = (Object[][])((Object[][])Array.newInstance(field.getRuntimeClass(), new int[]{rows, columns}));

         for(int i = 0; i < rows; ++i) {
            Arrays.fill(array[i], field.getZero());
         }
      }

      return array;
   }

   public static double[] convolve(double[] x, double[] h) throws NullArgumentException, NoDataException {
      MathUtils.checkNotNull(x);
      MathUtils.checkNotNull(h);
      int xLen = x.length;
      int hLen = h.length;
      if (xLen != 0 && hLen != 0) {
         int totalLength = xLen + hLen - 1;
         double[] y = new double[totalLength];

         for(int n = 0; n < totalLength; ++n) {
            double yn = 0.0;
            int k = FastMath.max(0, n + 1 - xLen);

            for(int j = n - k; k < hLen && j >= 0; yn += x[j--] * h[k++]) {
            }

            y[n] = yn;
         }

         return y;
      } else {
         throw new NoDataException();
      }
   }

   public static void shuffle(int[] list, int start, Position pos) {
      shuffle(list, start, pos, new Well19937c());
   }

   public static void shuffle(int[] list, int start, Position pos, RandomGenerator rng) {
      int i;
      int target;
      int temp;
      switch (pos) {
         case TAIL:
            for(i = list.length - 1; i >= start; --i) {
               if (i == start) {
                  target = start;
               } else {
                  target = (new UniformIntegerDistribution(rng, start, i)).sample();
               }

               temp = list[target];
               list[target] = list[i];
               list[i] = temp;
            }

            return;
         case HEAD:
            for(i = 0; i <= start; ++i) {
               if (i == start) {
                  target = start;
               } else {
                  target = (new UniformIntegerDistribution(rng, i, start)).sample();
               }

               temp = list[target];
               list[target] = list[i];
               list[i] = temp;
            }

            return;
         default:
            throw new MathInternalError();
      }
   }

   public static void shuffle(int[] list, RandomGenerator rng) {
      shuffle(list, 0, MathArrays.Position.TAIL, rng);
   }

   public static void shuffle(int[] list) {
      shuffle(list, new Well19937c());
   }

   public static int[] natural(int n) {
      return sequence(n, 0, 1);
   }

   public static int[] sequence(int size, int start, int stride) {
      int[] a = new int[size];

      for(int i = 0; i < size; ++i) {
         a[i] = start + i * stride;
      }

      return a;
   }

   public static boolean verifyValues(double[] values, int begin, int length) throws MathIllegalArgumentException {
      return verifyValues(values, begin, length, false);
   }

   public static boolean verifyValues(double[] values, int begin, int length, boolean allowEmpty) throws MathIllegalArgumentException {
      if (values == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY, new Object[0]);
      } else if (begin < 0) {
         throw new NotPositiveException(LocalizedFormats.START_POSITION, begin);
      } else if (length < 0) {
         throw new NotPositiveException(LocalizedFormats.LENGTH, length);
      } else if (begin + length > values.length) {
         throw new NumberIsTooLargeException(LocalizedFormats.SUBARRAY_ENDS_AFTER_ARRAY_END, begin + length, values.length, true);
      } else {
         return length != 0 || allowEmpty;
      }
   }

   public static boolean verifyValues(double[] values, double[] weights, int begin, int length) throws MathIllegalArgumentException {
      return verifyValues(values, weights, begin, length, false);
   }

   public static boolean verifyValues(double[] values, double[] weights, int begin, int length, boolean allowEmpty) throws MathIllegalArgumentException {
      if (weights != null && values != null) {
         checkEqualLength(weights, values);
         boolean containsPositiveWeight = false;

         for(int i = begin; i < begin + length; ++i) {
            double weight = weights[i];
            if (Double.isNaN(weight)) {
               throw new MathIllegalArgumentException(LocalizedFormats.NAN_ELEMENT_AT_INDEX, new Object[]{i});
            }

            if (Double.isInfinite(weight)) {
               throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_ARRAY_ELEMENT, new Object[]{weight, i});
            }

            if (weight < 0.0) {
               throw new MathIllegalArgumentException(LocalizedFormats.NEGATIVE_ELEMENT_AT_INDEX, new Object[]{i, weight});
            }

            if (!containsPositiveWeight && weight > 0.0) {
               containsPositiveWeight = true;
            }
         }

         if (!containsPositiveWeight) {
            throw new MathIllegalArgumentException(LocalizedFormats.WEIGHT_AT_LEAST_ONE_NON_ZERO, new Object[0]);
         } else {
            return verifyValues(values, begin, length, allowEmpty);
         }
      } else {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY, new Object[0]);
      }
   }

   public static double[] concatenate(double[]... x) {
      int combinedLength = 0;
      double[][] arr$ = x;
      int curLength = x.length;

      for(int i$ = 0; i$ < curLength; ++i$) {
         double[] a = arr$[i$];
         combinedLength += a.length;
      }

      int offset = 0;
      int curLength = false;
      double[] combined = new double[combinedLength];

      for(int i = 0; i < x.length; ++i) {
         curLength = x[i].length;
         System.arraycopy(x[i], 0, combined, offset, curLength);
         offset += curLength;
      }

      return combined;
   }

   public static double[] unique(double[] data) {
      TreeSet values = new TreeSet();

      int count;
      for(count = 0; count < data.length; ++count) {
         values.add(data[count]);
      }

      count = values.size();
      double[] out = new double[count];
      Iterator iterator = values.iterator();

      for(int i = 0; iterator.hasNext(); out[count - i] = (Double)iterator.next()) {
         ++i;
      }

      return out;
   }

   public static enum Position {
      HEAD,
      TAIL;
   }

   private static class PairDoubleInteger {
      private final double key;
      private final int value;

      PairDoubleInteger(double key, int value) {
         this.key = key;
         this.value = value;
      }

      public double getKey() {
         return this.key;
      }

      public int getValue() {
         return this.value;
      }
   }

   public static enum OrderDirection {
      INCREASING,
      DECREASING;
   }

   public interface Function {
      double evaluate(double[] var1);

      double evaluate(double[] var1, int var2, int var3);
   }
}
