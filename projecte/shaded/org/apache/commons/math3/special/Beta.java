package moze_intel.projecte.shaded.org.apache.commons.math3.special;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooSmallException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.ContinuedFraction;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class Beta {
   private static final double DEFAULT_EPSILON = 1.0E-14;
   private static final double HALF_LOG_TWO_PI = 0.9189385332046727;
   private static final double[] DELTA = new double[]{0.08333333333333333, -2.777777777777778E-5, 7.936507936507937E-8, -5.952380952380953E-10, 8.417508417508329E-12, -1.917526917518546E-13, 6.410256405103255E-15, -2.955065141253382E-16, 1.7964371635940225E-17, -1.3922896466162779E-18, 1.338028550140209E-19, -1.542460098679661E-20, 1.9770199298095743E-21, -2.3406566479399704E-22, 1.713480149663986E-23};

   private Beta() {
   }

   public static double regularizedBeta(double x, double a, double b) {
      return regularizedBeta(x, a, b, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedBeta(double x, double a, double b, double epsilon) {
      return regularizedBeta(x, a, b, epsilon, Integer.MAX_VALUE);
   }

   public static double regularizedBeta(double x, double a, double b, int maxIterations) {
      return regularizedBeta(x, a, b, 1.0E-14, maxIterations);
   }

   public static double regularizedBeta(double x, final double a, final double b, double epsilon, int maxIterations) {
      double ret;
      if (!Double.isNaN(x) && !Double.isNaN(a) && !Double.isNaN(b) && !(x < 0.0) && !(x > 1.0) && !(a <= 0.0) && !(b <= 0.0)) {
         if (x > (a + 1.0) / (2.0 + b + a) && 1.0 - x <= (b + 1.0) / (2.0 + b + a)) {
            ret = 1.0 - regularizedBeta(1.0 - x, b, a, epsilon, maxIterations);
         } else {
            ContinuedFraction fraction = new ContinuedFraction() {
               protected double getB(int n, double x) {
                  double ret;
                  double m;
                  if (n % 2 == 0) {
                     m = (double)n / 2.0;
                     ret = m * (b - m) * x / ((a + 2.0 * m - 1.0) * (a + 2.0 * m));
                  } else {
                     m = ((double)n - 1.0) / 2.0;
                     ret = -((a + m) * (a + b + m) * x) / ((a + 2.0 * m) * (a + 2.0 * m + 1.0));
                  }

                  return ret;
               }

               protected double getA(int n, double x) {
                  return 1.0;
               }
            };
            ret = FastMath.exp(a * FastMath.log(x) + b * FastMath.log1p(-x) - FastMath.log(a) - logBeta(a, b)) * 1.0 / fraction.evaluate(x, epsilon, maxIterations);
         }
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   /** @deprecated */
   @Deprecated
   public static double logBeta(double a, double b, double epsilon, int maxIterations) {
      return logBeta(a, b);
   }

   private static double logGammaSum(double a, double b) throws OutOfRangeException {
      if (!(a < 1.0) && !(a > 2.0)) {
         if (!(b < 1.0) && !(b > 2.0)) {
            double x = a - 1.0 + (b - 1.0);
            if (x <= 0.5) {
               return Gamma.logGamma1p(1.0 + x);
            } else {
               return x <= 1.5 ? Gamma.logGamma1p(x) + FastMath.log1p(x) : Gamma.logGamma1p(x - 1.0) + FastMath.log(x * (1.0 + x));
            }
         } else {
            throw new OutOfRangeException(b, 1.0, 2.0);
         }
      } else {
         throw new OutOfRangeException(a, 1.0, 2.0);
      }
   }

   private static double logGammaMinusLogGammaSum(double a, double b) throws NumberIsTooSmallException {
      if (a < 0.0) {
         throw new NumberIsTooSmallException(a, 0.0, true);
      } else if (b < 10.0) {
         throw new NumberIsTooSmallException(b, 10.0, true);
      } else {
         double d;
         double w;
         if (a <= b) {
            d = b + (a - 0.5);
            w = deltaMinusDeltaSum(a, b);
         } else {
            d = a + (b - 0.5);
            w = deltaMinusDeltaSum(b, a);
         }

         double u = d * FastMath.log1p(a / b);
         double v = a * (FastMath.log(b) - 1.0);
         return u <= v ? w - u - v : w - v - u;
      }
   }

   private static double deltaMinusDeltaSum(double a, double b) throws OutOfRangeException, NumberIsTooSmallException {
      if (!(a < 0.0) && !(a > b)) {
         if (b < 10.0) {
            throw new NumberIsTooSmallException(b, 10, true);
         } else {
            double h = a / b;
            double p = h / (1.0 + h);
            double q = 1.0 / (1.0 + h);
            double q2 = q * q;
            double[] s = new double[DELTA.length];
            s[0] = 1.0;

            for(int i = 1; i < s.length; ++i) {
               s[i] = 1.0 + q + q2 * s[i - 1];
            }

            double sqrtT = 10.0 / b;
            double t = sqrtT * sqrtT;
            double w = DELTA[DELTA.length - 1] * s[s.length - 1];

            for(int i = DELTA.length - 2; i >= 0; --i) {
               w = t * w + DELTA[i] * s[i];
            }

            return w * p / b;
         }
      } else {
         throw new OutOfRangeException(a, 0, b);
      }
   }

   private static double sumDeltaMinusDeltaSum(double p, double q) {
      if (p < 10.0) {
         throw new NumberIsTooSmallException(p, 10.0, true);
      } else if (q < 10.0) {
         throw new NumberIsTooSmallException(q, 10.0, true);
      } else {
         double a = FastMath.min(p, q);
         double b = FastMath.max(p, q);
         double sqrtT = 10.0 / a;
         double t = sqrtT * sqrtT;
         double z = DELTA[DELTA.length - 1];

         for(int i = DELTA.length - 2; i >= 0; --i) {
            z = t * z + DELTA[i];
         }

         return z / a + deltaMinusDeltaSum(a, b);
      }
   }

   public static double logBeta(double p, double q) {
      if (!Double.isNaN(p) && !Double.isNaN(q) && !(p <= 0.0) && !(q <= 0.0)) {
         double a = FastMath.min(p, q);
         double b = FastMath.max(p, q);
         double prod;
         double ared;
         double prod2;
         double bred;
         if (a >= 10.0) {
            prod = sumDeltaMinusDeltaSum(a, b);
            ared = a / b;
            prod2 = ared / (1.0 + ared);
            bred = -(a - 0.5) * FastMath.log(prod2);
            double v = b * FastMath.log1p(ared);
            return bred <= v ? -0.5 * FastMath.log(b) + 0.9189385332046727 + prod - bred - v : -0.5 * FastMath.log(b) + 0.9189385332046727 + prod - v - bred;
         } else if (a > 2.0) {
            if (b > 1000.0) {
               int n = (int)FastMath.floor(a - 1.0);
               double prod = 1.0;
               double ared = a;

               for(int i = 0; i < n; ++i) {
                  --ared;
                  prod *= ared / (1.0 + ared / b);
               }

               return FastMath.log(prod) - (double)n * FastMath.log(b) + Gamma.logGamma(ared) + logGammaMinusLogGammaSum(ared, b);
            } else {
               prod = 1.0;

               for(ared = a; ared > 2.0; prod *= prod2 / (1.0 + prod2)) {
                  --ared;
                  prod2 = ared / b;
               }

               if (!(b < 10.0)) {
                  return FastMath.log(prod) + Gamma.logGamma(ared) + logGammaMinusLogGammaSum(ared, b);
               } else {
                  prod2 = 1.0;

                  for(bred = b; bred > 2.0; prod2 *= bred / (ared + bred)) {
                     --bred;
                  }

                  return FastMath.log(prod) + FastMath.log(prod2) + Gamma.logGamma(ared) + (Gamma.logGamma(bred) - logGammaSum(ared, bred));
               }
            }
         } else if (!(a >= 1.0)) {
            return b >= 10.0 ? Gamma.logGamma(a) + logGammaMinusLogGammaSum(a, b) : FastMath.log(Gamma.gamma(a) * Gamma.gamma(b) / Gamma.gamma(a + b));
         } else if (!(b > 2.0)) {
            return Gamma.logGamma(a) + Gamma.logGamma(b) - logGammaSum(a, b);
         } else if (!(b < 10.0)) {
            return Gamma.logGamma(a) + logGammaMinusLogGammaSum(a, b);
         } else {
            prod = 1.0;

            for(ared = b; ared > 2.0; prod *= ared / (a + ared)) {
               --ared;
            }

            return FastMath.log(prod) + Gamma.logGamma(a) + (Gamma.logGamma(ared) - logGammaSum(a, ared));
         }
      } else {
         return Double.NaN;
      }
   }
}
