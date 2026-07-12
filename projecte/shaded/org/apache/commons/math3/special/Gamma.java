package moze_intel.projecte.shaded.org.apache.commons.math3.special;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MaxCountExceededException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooSmallException;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.ContinuedFraction;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class Gamma {
   public static final double GAMMA = 0.5772156649015329;
   public static final double LANCZOS_G = 4.7421875;
   private static final double DEFAULT_EPSILON = 1.0E-14;
   private static final double[] LANCZOS = new double[]{0.9999999999999971, 57.15623566586292, -59.59796035547549, 14.136097974741746, -0.4919138160976202, 3.399464998481189E-5, 4.652362892704858E-5, -9.837447530487956E-5, 1.580887032249125E-4, -2.1026444172410488E-4, 2.1743961811521265E-4, -1.643181065367639E-4, 8.441822398385275E-5, -2.6190838401581408E-5, 3.6899182659531625E-6};
   private static final double HALF_LOG_2_PI = 0.5 * FastMath.log(6.283185307179586);
   private static final double SQRT_TWO_PI = 2.5066282746310007;
   private static final double C_LIMIT = 49.0;
   private static final double S_LIMIT = 1.0E-5;
   private static final double INV_GAMMA1P_M1_A0 = 6.116095104481416E-9;
   private static final double INV_GAMMA1P_M1_A1 = 6.247308301164655E-9;
   private static final double INV_GAMMA1P_M1_B1 = 0.203610414066807;
   private static final double INV_GAMMA1P_M1_B2 = 0.026620534842894922;
   private static final double INV_GAMMA1P_M1_B3 = 4.939449793824468E-4;
   private static final double INV_GAMMA1P_M1_B4 = -8.514194324403149E-6;
   private static final double INV_GAMMA1P_M1_B5 = -6.4304548177935305E-6;
   private static final double INV_GAMMA1P_M1_B6 = 9.926418406727737E-7;
   private static final double INV_GAMMA1P_M1_B7 = -6.077618957228252E-8;
   private static final double INV_GAMMA1P_M1_B8 = 1.9575583661463974E-10;
   private static final double INV_GAMMA1P_M1_P0 = 6.116095104481416E-9;
   private static final double INV_GAMMA1P_M1_P1 = 6.8716741130671986E-9;
   private static final double INV_GAMMA1P_M1_P2 = 6.820161668496171E-10;
   private static final double INV_GAMMA1P_M1_P3 = 4.686843322948848E-11;
   private static final double INV_GAMMA1P_M1_P4 = 1.5728330277104463E-12;
   private static final double INV_GAMMA1P_M1_P5 = -1.2494415722763663E-13;
   private static final double INV_GAMMA1P_M1_P6 = 4.343529937408594E-15;
   private static final double INV_GAMMA1P_M1_Q1 = 0.3056961078365221;
   private static final double INV_GAMMA1P_M1_Q2 = 0.054642130860422966;
   private static final double INV_GAMMA1P_M1_Q3 = 0.004956830093825887;
   private static final double INV_GAMMA1P_M1_Q4 = 2.6923694661863613E-4;
   private static final double INV_GAMMA1P_M1_C = -0.42278433509846713;
   private static final double INV_GAMMA1P_M1_C0 = 0.5772156649015329;
   private static final double INV_GAMMA1P_M1_C1 = -0.6558780715202539;
   private static final double INV_GAMMA1P_M1_C2 = -0.04200263503409524;
   private static final double INV_GAMMA1P_M1_C3 = 0.16653861138229148;
   private static final double INV_GAMMA1P_M1_C4 = -0.04219773455554433;
   private static final double INV_GAMMA1P_M1_C5 = -0.009621971527876973;
   private static final double INV_GAMMA1P_M1_C6 = 0.0072189432466631;
   private static final double INV_GAMMA1P_M1_C7 = -0.0011651675918590652;
   private static final double INV_GAMMA1P_M1_C8 = -2.1524167411495098E-4;
   private static final double INV_GAMMA1P_M1_C9 = 1.280502823881162E-4;
   private static final double INV_GAMMA1P_M1_C10 = -2.013485478078824E-5;
   private static final double INV_GAMMA1P_M1_C11 = -1.2504934821426706E-6;
   private static final double INV_GAMMA1P_M1_C12 = 1.133027231981696E-6;
   private static final double INV_GAMMA1P_M1_C13 = -2.056338416977607E-7;

   private Gamma() {
   }

   public static double logGamma(double x) {
      double ret;
      if (!Double.isNaN(x) && !(x <= 0.0)) {
         if (x < 0.5) {
            return logGamma1p(x) - FastMath.log(x);
         }

         if (x <= 2.5) {
            return logGamma1p(x - 0.5 - 0.5);
         }

         if (x <= 8.0) {
            int n = (int)FastMath.floor(x - 1.5);
            double prod = 1.0;

            for(int i = 1; i <= n; ++i) {
               prod *= x - (double)i;
            }

            return logGamma1p(x - (double)(n + 1)) + FastMath.log(prod);
         }

         double sum = lanczos(x);
         double tmp = x + 4.7421875 + 0.5;
         ret = (x + 0.5) * FastMath.log(tmp) - tmp + HALF_LOG_2_PI + FastMath.log(sum / x);
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   public static double regularizedGammaP(double a, double x) {
      return regularizedGammaP(a, x, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedGammaP(double a, double x, double epsilon, int maxIterations) {
      double ret;
      if (!Double.isNaN(a) && !Double.isNaN(x) && !(a <= 0.0) && !(x < 0.0)) {
         if (x == 0.0) {
            ret = 0.0;
         } else if (x >= a + 1.0) {
            ret = 1.0 - regularizedGammaQ(a, x, epsilon, maxIterations);
         } else {
            double n = 0.0;
            double an = 1.0 / a;

            double sum;
            for(sum = an; FastMath.abs(an / sum) > epsilon && n < (double)maxIterations && sum < Double.POSITIVE_INFINITY; sum += an) {
               ++n;
               an *= x / (a + n);
            }

            if (n >= (double)maxIterations) {
               throw new MaxCountExceededException(maxIterations);
            }

            if (Double.isInfinite(sum)) {
               ret = 1.0;
            } else {
               ret = FastMath.exp(-x + a * FastMath.log(x) - logGamma(a)) * sum;
            }
         }
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   public static double regularizedGammaQ(double a, double x) {
      return regularizedGammaQ(a, x, 1.0E-14, Integer.MAX_VALUE);
   }

   public static double regularizedGammaQ(final double a, double x, double epsilon, int maxIterations) {
      double ret;
      if (!Double.isNaN(a) && !Double.isNaN(x) && !(a <= 0.0) && !(x < 0.0)) {
         if (x == 0.0) {
            ret = 1.0;
         } else if (x < a + 1.0) {
            ret = 1.0 - regularizedGammaP(a, x, epsilon, maxIterations);
         } else {
            ContinuedFraction cf = new ContinuedFraction() {
               protected double getA(int n, double x) {
                  return 2.0 * (double)n + 1.0 - a + x;
               }

               protected double getB(int n, double x) {
                  return (double)n * (a - (double)n);
               }
            };
            ret = 1.0 / cf.evaluate(x, epsilon, maxIterations);
            ret = FastMath.exp(-x + a * FastMath.log(x) - logGamma(a)) * ret;
         }
      } else {
         ret = Double.NaN;
      }

      return ret;
   }

   public static double digamma(double x) {
      if (!Double.isNaN(x) && !Double.isInfinite(x)) {
         if (x > 0.0 && x <= 1.0E-5) {
            return -0.5772156649015329 - 1.0 / x;
         } else if (x >= 49.0) {
            double inv = 1.0 / (x * x);
            return FastMath.log(x) - 0.5 / x - inv * (0.08333333333333333 + inv * (0.008333333333333333 - inv / 252.0));
         } else {
            return digamma(x + 1.0) - 1.0 / x;
         }
      } else {
         return x;
      }
   }

   public static double trigamma(double x) {
      if (!Double.isNaN(x) && !Double.isInfinite(x)) {
         if (x > 0.0 && x <= 1.0E-5) {
            return 1.0 / (x * x);
         } else if (x >= 49.0) {
            double inv = 1.0 / (x * x);
            return 1.0 / x + inv / 2.0 + inv / x * (0.16666666666666666 - inv * (0.03333333333333333 + inv / 42.0));
         } else {
            return trigamma(x + 1.0) + 1.0 / (x * x);
         }
      } else {
         return x;
      }
   }

   public static double lanczos(double x) {
      double sum = 0.0;

      for(int i = LANCZOS.length - 1; i > 0; --i) {
         sum += LANCZOS[i] / (x + (double)i);
      }

      return sum + LANCZOS[0];
   }

   public static double invGamma1pm1(double x) {
      if (x < -0.5) {
         throw new NumberIsTooSmallException(x, -0.5, true);
      } else if (x > 1.5) {
         throw new NumberIsTooLargeException(x, 1.5, true);
      } else {
         double t = x <= 0.5 ? x : x - 0.5 - 0.5;
         double ret;
         double a;
         double b;
         double c;
         if (t < 0.0) {
            a = 6.116095104481416E-9 + t * 6.247308301164655E-9;
            b = 1.9575583661463974E-10;
            b = -6.077618957228252E-8 + t * b;
            b = 9.926418406727737E-7 + t * b;
            b = -6.4304548177935305E-6 + t * b;
            b = -8.514194324403149E-6 + t * b;
            b = 4.939449793824468E-4 + t * b;
            b = 0.026620534842894922 + t * b;
            b = 0.203610414066807 + t * b;
            b = 1.0 + t * b;
            c = -2.056338416977607E-7 + t * (a / b);
            c = 1.133027231981696E-6 + t * c;
            c = -1.2504934821426706E-6 + t * c;
            c = -2.013485478078824E-5 + t * c;
            c = 1.280502823881162E-4 + t * c;
            c = -2.1524167411495098E-4 + t * c;
            c = -0.0011651675918590652 + t * c;
            c = 0.0072189432466631 + t * c;
            c = -0.009621971527876973 + t * c;
            c = -0.04219773455554433 + t * c;
            c = 0.16653861138229148 + t * c;
            c = -0.04200263503409524 + t * c;
            c = -0.6558780715202539 + t * c;
            c = -0.42278433509846713 + t * c;
            if (x > 0.5) {
               ret = t * c / x;
            } else {
               ret = x * (c + 0.5 + 0.5);
            }
         } else {
            a = 4.343529937408594E-15;
            a = -1.2494415722763663E-13 + t * a;
            a = 1.5728330277104463E-12 + t * a;
            a = 4.686843322948848E-11 + t * a;
            a = 6.820161668496171E-10 + t * a;
            a = 6.8716741130671986E-9 + t * a;
            a = 6.116095104481416E-9 + t * a;
            b = 2.6923694661863613E-4;
            b = 0.004956830093825887 + t * b;
            b = 0.054642130860422966 + t * b;
            b = 0.3056961078365221 + t * b;
            b = 1.0 + t * b;
            c = -2.056338416977607E-7 + a / b * t;
            c = 1.133027231981696E-6 + t * c;
            c = -1.2504934821426706E-6 + t * c;
            c = -2.013485478078824E-5 + t * c;
            c = 1.280502823881162E-4 + t * c;
            c = -2.1524167411495098E-4 + t * c;
            c = -0.0011651675918590652 + t * c;
            c = 0.0072189432466631 + t * c;
            c = -0.009621971527876973 + t * c;
            c = -0.04219773455554433 + t * c;
            c = 0.16653861138229148 + t * c;
            c = -0.04200263503409524 + t * c;
            c = -0.6558780715202539 + t * c;
            c = 0.5772156649015329 + t * c;
            if (x > 0.5) {
               ret = t / x * (c - 0.5 - 0.5);
            } else {
               ret = x * c;
            }
         }

         return ret;
      }
   }

   public static double logGamma1p(double x) throws NumberIsTooSmallException, NumberIsTooLargeException {
      if (x < -0.5) {
         throw new NumberIsTooSmallException(x, -0.5, true);
      } else if (x > 1.5) {
         throw new NumberIsTooLargeException(x, 1.5, true);
      } else {
         return -FastMath.log1p(invGamma1pm1(x));
      }
   }

   public static double gamma(double x) {
      if (x == FastMath.rint(x) && x <= 0.0) {
         return Double.NaN;
      } else {
         double absX = FastMath.abs(x);
         double ret;
         double prod;
         double t;
         if (absX <= 20.0) {
            if (x >= 1.0) {
               prod = 1.0;

               for(t = x; t > 2.5; prod *= t) {
                  --t;
               }

               ret = prod / (1.0 + invGamma1pm1(t - 1.0));
            } else {
               prod = x;

               for(t = x; t < -0.5; prod *= t) {
                  ++t;
               }

               ret = 1.0 / (prod * (1.0 + invGamma1pm1(t)));
            }
         } else {
            prod = absX + 4.7421875 + 0.5;
            t = 2.5066282746310007 / absX * FastMath.pow(prod, absX + 0.5) * FastMath.exp(-prod) * lanczos(absX);
            if (x > 0.0) {
               ret = t;
            } else {
               ret = -3.141592653589793 / (x * FastMath.sin(Math.PI * x) * t);
            }
         }

         return ret;
      }
   }
}
