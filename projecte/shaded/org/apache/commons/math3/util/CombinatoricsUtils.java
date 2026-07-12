package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public final class CombinatoricsUtils {
   static final long[] FACTORIALS = new long[]{1L, 1L, 2L, 6L, 24L, 120L, 720L, 5040L, 40320L, 362880L, 3628800L, 39916800L, 479001600L, 6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L, 6402373705728000L, 121645100408832000L, 2432902008176640000L};
   static final AtomicReference STIRLING_S2 = new AtomicReference((Object)null);

   private CombinatoricsUtils() {
   }

   public static long binomialCoefficient(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      checkBinomial(n, k);
      if (n != k && k != 0) {
         if (k != 1 && k != n - 1) {
            if (k > n / 2) {
               return binomialCoefficient(n, n - k);
            } else {
               long result = 1L;
               int i;
               int j;
               if (n <= 61) {
                  i = n - k + 1;

                  for(j = 1; j <= k; ++j) {
                     result = result * (long)i / (long)j;
                     ++i;
                  }
               } else {
                  long d;
                  if (n <= 66) {
                     i = n - k + 1;

                     for(j = 1; j <= k; ++j) {
                        d = (long)ArithmeticUtils.gcd(i, j);
                        result = result / ((long)j / d) * ((long)i / d);
                        ++i;
                     }
                  } else {
                     i = n - k + 1;

                     for(j = 1; j <= k; ++j) {
                        d = (long)ArithmeticUtils.gcd(i, j);
                        result = ArithmeticUtils.mulAndCheck(result / ((long)j / d), (long)i / d);
                        ++i;
                     }
                  }
               }

               return result;
            }
         } else {
            return (long)n;
         }
      } else {
         return 1L;
      }
   }

   public static double binomialCoefficientDouble(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      checkBinomial(n, k);
      if (n != k && k != 0) {
         if (k != 1 && k != n - 1) {
            if (k > n / 2) {
               return binomialCoefficientDouble(n, n - k);
            } else if (n < 67) {
               return (double)binomialCoefficient(n, k);
            } else {
               double result = 1.0;

               for(int i = 1; i <= k; ++i) {
                  result *= (double)(n - k + i) / (double)i;
               }

               return FastMath.floor(result + 0.5);
            }
         } else {
            return (double)n;
         }
      } else {
         return 1.0;
      }
   }

   public static double binomialCoefficientLog(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      checkBinomial(n, k);
      if (n != k && k != 0) {
         if (k != 1 && k != n - 1) {
            if (n < 67) {
               return FastMath.log((double)binomialCoefficient(n, k));
            } else if (n < 1030) {
               return FastMath.log(binomialCoefficientDouble(n, k));
            } else if (k > n / 2) {
               return binomialCoefficientLog(n, n - k);
            } else {
               double logSum = 0.0;

               int i;
               for(i = n - k + 1; i <= n; ++i) {
                  logSum += FastMath.log((double)i);
               }

               for(i = 2; i <= k; ++i) {
                  logSum -= FastMath.log((double)i);
               }

               return logSum;
            }
         } else {
            return FastMath.log((double)n);
         }
      } else {
         return 0.0;
      }
   }

   public static long factorial(int n) throws NotPositiveException, MathArithmeticException {
      if (n < 0) {
         throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else if (n > 20) {
         throw new MathArithmeticException();
      } else {
         return FACTORIALS[n];
      }
   }

   public static double factorialDouble(int n) throws NotPositiveException {
      if (n < 0) {
         throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else {
         return n < 21 ? (double)FACTORIALS[n] : FastMath.floor(FastMath.exp(factorialLog(n)) + 0.5);
      }
   }

   public static double factorialLog(int n) throws NotPositiveException {
      if (n < 0) {
         throw new NotPositiveException(LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER, n);
      } else if (n < 21) {
         return FastMath.log((double)FACTORIALS[n]);
      } else {
         double logSum = 0.0;

         for(int i = 2; i <= n; ++i) {
            logSum += FastMath.log((double)i);
         }

         return logSum;
      }
   }

   public static long stirlingS2(int n, int k) throws NotPositiveException, NumberIsTooLargeException, MathArithmeticException {
      if (k < 0) {
         throw new NotPositiveException(k);
      } else if (k > n) {
         throw new NumberIsTooLargeException(k, n, true);
      } else {
         long[][] stirlingS2 = (long[][])STIRLING_S2.get();
         if (stirlingS2 == null) {
            int maxIndex = true;
            stirlingS2 = new long[26][];
            stirlingS2[0] = new long[]{1L};

            for(int i = 1; i < stirlingS2.length; ++i) {
               stirlingS2[i] = new long[i + 1];
               stirlingS2[i][0] = 0L;
               stirlingS2[i][1] = 1L;
               stirlingS2[i][i] = 1L;

               for(int j = 2; j < i; ++j) {
                  stirlingS2[i][j] = (long)j * stirlingS2[i - 1][j] + stirlingS2[i - 1][j - 1];
               }
            }

            STIRLING_S2.compareAndSet((Object)null, stirlingS2);
         }

         if (n < stirlingS2.length) {
            return stirlingS2[n][k];
         } else if (k == 0) {
            return 0L;
         } else if (k != 1 && k != n) {
            if (k == 2) {
               return (1L << n - 1) - 1L;
            } else if (k == n - 1) {
               return binomialCoefficient(n, 2);
            } else {
               long sum = 0L;
               long sign = (k & 1) == 0 ? 1L : -1L;

               for(int j = 1; j <= k; ++j) {
                  sign = -sign;
                  sum += sign * binomialCoefficient(k, j) * (long)ArithmeticUtils.pow(j, n);
                  if (sum < 0L) {
                     throw new MathArithmeticException(LocalizedFormats.ARGUMENT_OUTSIDE_DOMAIN, new Object[]{n, 0, stirlingS2.length - 1});
                  }
               }

               return sum / factorial(k);
            }
         } else {
            return 1L;
         }
      }
   }

   public static Iterator combinationsIterator(int n, int k) {
      return (new Combinations(n, k)).iterator();
   }

   public static void checkBinomial(int n, int k) throws NumberIsTooLargeException, NotPositiveException {
      if (n < k) {
         throw new NumberIsTooLargeException(LocalizedFormats.BINOMIAL_INVALID_PARAMETERS_ORDER, k, n, true);
      } else if (n < 0) {
         throw new NotPositiveException(LocalizedFormats.BINOMIAL_NEGATIVE_PARAMETER, n);
      }
   }
}
