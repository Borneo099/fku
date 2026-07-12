package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.math.BigInteger;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public final class ArithmeticUtils {
   private ArithmeticUtils() {
   }

   public static int addAndCheck(int x, int y) throws MathArithmeticException {
      long s = (long)x + (long)y;
      if (s >= -2147483648L && s <= 2147483647L) {
         return (int)s;
      } else {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{x, y});
      }
   }

   public static long addAndCheck(long a, long b) throws MathArithmeticException {
      return addAndCheck(a, b, LocalizedFormats.OVERFLOW_IN_ADDITION);
   }

   /** @deprecated */
   @Deprecated
   public static long binomialCoefficient(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      return CombinatoricsUtils.binomialCoefficient(n, k);
   }

   /** @deprecated */
   @Deprecated
   public static double binomialCoefficientDouble(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      return CombinatoricsUtils.binomialCoefficientDouble(n, k);
   }

   /** @deprecated */
   @Deprecated
   public static double binomialCoefficientLog(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      return CombinatoricsUtils.binomialCoefficientLog(n, k);
   }

   /** @deprecated */
   @Deprecated
   public static long factorial(int n) throws NotPositiveException, MathArithmeticException {
      return CombinatoricsUtils.factorial(n);
   }

   /** @deprecated */
   @Deprecated
   public static double factorialDouble(int n) throws NotPositiveException {
      return CombinatoricsUtils.factorialDouble(n);
   }

   /** @deprecated */
   @Deprecated
   public static double factorialLog(int n) throws NotPositiveException {
      return CombinatoricsUtils.factorialLog(n);
   }

   public static int gcd(int p, int q) throws MathArithmeticException {
      int a = p;
      int b = q;
      if (p != 0 && q != 0) {
         long al = (long)p;
         long bl = (long)q;
         boolean useLong = false;
         if (p < 0) {
            if (Integer.MIN_VALUE == p) {
               useLong = true;
            } else {
               a = -p;
            }

            al = -al;
         }

         if (q < 0) {
            if (Integer.MIN_VALUE == q) {
               useLong = true;
            } else {
               b = -q;
            }

            bl = -bl;
         }

         if (useLong) {
            if (al == bl) {
               throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, new Object[]{p, q});
            }

            long blbu = bl;
            bl = al;
            al = blbu % al;
            if (al == 0L) {
               if (bl > 2147483647L) {
                  throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, new Object[]{p, q});
               }

               return (int)bl;
            }

            b = (int)al;
            a = (int)(bl % al);
         }

         return gcdPositive(a, b);
      } else if (p != Integer.MIN_VALUE && q != Integer.MIN_VALUE) {
         return FastMath.abs(p + q);
      } else {
         throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_32_BITS, new Object[]{p, q});
      }
   }

   private static int gcdPositive(int a, int b) {
      if (a == 0) {
         return b;
      } else if (b == 0) {
         return a;
      } else {
         int aTwos = Integer.numberOfTrailingZeros(a);
         a >>= aTwos;
         int bTwos = Integer.numberOfTrailingZeros(b);
         b >>= bTwos;

         int shift;
         for(shift = FastMath.min(aTwos, bTwos); a != b; a >>= Integer.numberOfTrailingZeros(a)) {
            int delta = a - b;
            b = Math.min(a, b);
            a = Math.abs(delta);
         }

         return a << shift;
      }
   }

   public static long gcd(long p, long q) throws MathArithmeticException {
      long u = p;
      long v = q;
      if (p != 0L && q != 0L) {
         if (p > 0L) {
            u = -p;
         }

         if (q > 0L) {
            v = -q;
         }

         int k;
         for(k = 0; (u & 1L) == 0L && (v & 1L) == 0L && k < 63; ++k) {
            u /= 2L;
            v /= 2L;
         }

         if (k == 63) {
            throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, new Object[]{p, q});
         } else {
            long t = (u & 1L) == 1L ? v : -(u / 2L);

            while(true) {
               while((t & 1L) != 0L) {
                  if (t > 0L) {
                     u = -t;
                  } else {
                     v = t;
                  }

                  t = (v - u) / 2L;
                  if (t == 0L) {
                     return -u * (1L << k);
                  }
               }

               t /= 2L;
            }
         }
      } else if (p != Long.MIN_VALUE && q != Long.MIN_VALUE) {
         return FastMath.abs(p) + FastMath.abs(q);
      } else {
         throw new MathArithmeticException(LocalizedFormats.GCD_OVERFLOW_64_BITS, new Object[]{p, q});
      }
   }

   public static int lcm(int a, int b) throws MathArithmeticException {
      if (a != 0 && b != 0) {
         int lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
         if (lcm == Integer.MIN_VALUE) {
            throw new MathArithmeticException(LocalizedFormats.LCM_OVERFLOW_32_BITS, new Object[]{a, b});
         } else {
            return lcm;
         }
      } else {
         return 0;
      }
   }

   public static long lcm(long a, long b) throws MathArithmeticException {
      if (a != 0L && b != 0L) {
         long lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
         if (lcm == Long.MIN_VALUE) {
            throw new MathArithmeticException(LocalizedFormats.LCM_OVERFLOW_64_BITS, new Object[]{a, b});
         } else {
            return lcm;
         }
      } else {
         return 0L;
      }
   }

   public static int mulAndCheck(int x, int y) throws MathArithmeticException {
      long m = (long)x * (long)y;
      if (m >= -2147483648L && m <= 2147483647L) {
         return (int)m;
      } else {
         throw new MathArithmeticException();
      }
   }

   public static long mulAndCheck(long a, long b) throws MathArithmeticException {
      long ret;
      if (a > b) {
         ret = mulAndCheck(b, a);
      } else if (a < 0L) {
         if (b < 0L) {
            if (a < Long.MAX_VALUE / b) {
               throw new MathArithmeticException();
            }

            ret = a * b;
         } else if (b > 0L) {
            if (Long.MIN_VALUE / b > a) {
               throw new MathArithmeticException();
            }

            ret = a * b;
         } else {
            ret = 0L;
         }
      } else if (a > 0L) {
         if (a > Long.MAX_VALUE / b) {
            throw new MathArithmeticException();
         }

         ret = a * b;
      } else {
         ret = 0L;
      }

      return ret;
   }

   public static int subAndCheck(int x, int y) throws MathArithmeticException {
      long s = (long)x - (long)y;
      if (s >= -2147483648L && s <= 2147483647L) {
         return (int)s;
      } else {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, new Object[]{x, y});
      }
   }

   public static long subAndCheck(long a, long b) throws MathArithmeticException {
      long ret;
      if (b == Long.MIN_VALUE) {
         if (a >= 0L) {
            throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{a, -b});
         }

         ret = a - b;
      } else {
         ret = addAndCheck(a, -b, LocalizedFormats.OVERFLOW_IN_ADDITION);
      }

      return ret;
   }

   public static int pow(int k, int e) throws NotPositiveException, MathArithmeticException {
      if (e < 0) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         try {
            int exp = e;
            int result = 1;
            int k2p = k;

            while(true) {
               if ((exp & 1) != 0) {
                  result = mulAndCheck(result, k2p);
               }

               exp >>= 1;
               if (exp == 0) {
                  return result;
               }

               k2p = mulAndCheck(k2p, k2p);
            }
         } catch (MathArithmeticException var5) {
            var5.getContext().addMessage(LocalizedFormats.OVERFLOW);
            var5.getContext().addMessage(LocalizedFormats.BASE, k);
            var5.getContext().addMessage(LocalizedFormats.EXPONENT, e);
            throw var5;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public static int pow(int k, long e) throws NotPositiveException {
      if (e < 0L) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         int result = 1;

         for(int k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static long pow(long k, int e) throws NotPositiveException, MathArithmeticException {
      if (e < 0) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         try {
            int exp = e;
            long result = 1L;
            long k2p = k;

            while(true) {
               if ((exp & 1) != 0) {
                  result = mulAndCheck(result, k2p);
               }

               exp >>= 1;
               if (exp == 0) {
                  return result;
               }

               k2p = mulAndCheck(k2p, k2p);
            }
         } catch (MathArithmeticException var8) {
            var8.getContext().addMessage(LocalizedFormats.OVERFLOW);
            var8.getContext().addMessage(LocalizedFormats.BASE, k);
            var8.getContext().addMessage(LocalizedFormats.EXPONENT, e);
            throw var8;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public static long pow(long k, long e) throws NotPositiveException {
      if (e < 0L) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         long result = 1L;

         for(long k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result *= k2p;
            }

            k2p *= k2p;
         }

         return result;
      }
   }

   public static BigInteger pow(BigInteger k, int e) throws NotPositiveException {
      if (e < 0) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         return k.pow(e);
      }
   }

   public static BigInteger pow(BigInteger k, long e) throws NotPositiveException {
      if (e < 0L) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         BigInteger result = BigInteger.ONE;

         for(BigInteger k2p = k; e != 0L; e >>= 1) {
            if ((e & 1L) != 0L) {
               result = result.multiply(k2p);
            }

            k2p = k2p.multiply(k2p);
         }

         return result;
      }
   }

   public static BigInteger pow(BigInteger k, BigInteger e) throws NotPositiveException {
      if (e.compareTo(BigInteger.ZERO) < 0) {
         throw new NotPositiveException(LocalizedFormats.EXPONENT, e);
      } else {
         BigInteger result = BigInteger.ONE;

         for(BigInteger k2p = k; !BigInteger.ZERO.equals(e); e = e.shiftRight(1)) {
            if (e.testBit(0)) {
               result = result.multiply(k2p);
            }

            k2p = k2p.multiply(k2p);
         }

         return result;
      }
   }

   /** @deprecated */
   @Deprecated
   public static long stirlingS2(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      return CombinatoricsUtils.stirlingS2(n, k);
   }

   private static long addAndCheck(long a, long b, Localizable pattern) throws MathArithmeticException {
      long result = a + b;
      if (!((a ^ b) < 0L | (a ^ result) >= 0L)) {
         throw new MathArithmeticException(pattern, new Object[]{a, b});
      } else {
         return result;
      }
   }

   public static boolean isPowerOfTwo(long n) {
      return n > 0L && (n & n - 1L) == 0L;
   }
}
