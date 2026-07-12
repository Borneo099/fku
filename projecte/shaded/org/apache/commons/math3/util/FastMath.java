package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.io.PrintStream;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class FastMath {
   public static final double PI = Math.PI;
   public static final double E = Math.E;
   static final int EXP_INT_TABLE_MAX_INDEX = 750;
   static final int EXP_INT_TABLE_LEN = 1500;
   static final int LN_MANT_LEN = 1024;
   static final int EXP_FRAC_TABLE_LEN = 1025;
   private static final double LOG_MAX_VALUE = StrictMath.log(Double.MAX_VALUE);
   private static final boolean RECOMPUTE_TABLES_AT_RUNTIME = false;
   private static final double LN_2_A = 0.6931470632553101;
   private static final double LN_2_B = 1.1730463525082348E-7;
   private static final double[][] LN_QUICK_COEF = new double[][]{{1.0, 5.669184079525E-24}, {-0.25, -0.25}, {0.3333333134651184, 1.986821492305628E-8}, {-0.25, -6.663542893624021E-14}, {0.19999998807907104, 1.1921056801463227E-8}, {-0.1666666567325592, -7.800414592973399E-9}, {0.1428571343421936, 5.650007086920087E-9}, {-0.12502530217170715, -7.44321345601866E-11}, {0.11113807559013367, 9.219544613762692E-9}};
   private static final double[][] LN_HI_PREC_COEF = new double[][]{{1.0, -6.032174644509064E-23}, {-0.25, -0.25}, {0.3333333134651184, 1.9868161777724352E-8}, {-0.2499999701976776, -2.957007209750105E-8}, {0.19999954104423523, 1.5830993332061267E-10}, {-0.16624879837036133, -2.6033824355191673E-8}};
   private static final int SINE_TABLE_LEN = 14;
   private static final double[] SINE_TABLE_A = new double[]{0.0, 0.1246747374534607, 0.24740394949913025, 0.366272509098053, 0.4794255495071411, 0.5850973129272461, 0.6816387176513672, 0.7675435543060303, 0.8414709568023682, 0.902267575263977, 0.9489846229553223, 0.9808930158615112, 0.9974949359893799, 0.9985313415527344};
   private static final double[] SINE_TABLE_B = new double[]{0.0, -4.068233003401932E-9, 9.755392680573412E-9, 1.9987994582857286E-8, -1.0902938113007961E-8, -3.9986783938944604E-8, 4.23719669792332E-8, -5.207000323380292E-8, 2.800552834259E-8, 1.883511811213715E-8, -3.5997360512765566E-9, 4.116164446561962E-8, 5.0614674548127384E-8, -1.0129027912496858E-9};
   private static final double[] COSINE_TABLE_A = new double[]{1.0, 0.9921976327896118, 0.9689123630523682, 0.9305076599121094, 0.8775825500488281, 0.8109631538391113, 0.7316888570785522, 0.6409968137741089, 0.5403022766113281, 0.4311765432357788, 0.3153223395347595, 0.19454771280288696, 0.07073719799518585, -0.05417713522911072};
   private static final double[] COSINE_TABLE_B = new double[]{0.0, 3.4439717236742845E-8, 5.865827662008209E-8, -3.7999795083850525E-8, 1.184154459111628E-8, -3.43338934259355E-8, 1.1795268640216787E-8, 4.438921624363781E-8, 2.925681159240093E-8, -2.6437112632041807E-8, 2.2860509143963117E-8, -4.813899778443457E-9, 3.6725170580355583E-9, 2.0217439756338078E-10};
   private static final double[] TANGENT_TABLE_A = new double[]{0.0, 0.1256551444530487, 0.25534194707870483, 0.3936265707015991, 0.5463024377822876, 0.7214844226837158, 0.9315965175628662, 1.1974215507507324, 1.5574076175689697, 2.092571258544922, 3.0095696449279785, 5.041914939880371, 14.101419448852539, -18.430862426757812};
   private static final double[] TANGENT_TABLE_B = new double[]{0.0, -7.877917738262007E-9, -2.5857668567479893E-8, 5.2240336371356666E-9, 5.206150291559893E-8, 1.8307188599677033E-8, -5.7618793749770706E-8, 7.848361555046424E-8, 1.0708593250394448E-7, 1.7827257129423813E-8, 2.893485277253286E-8, 3.1660099222737955E-7, 4.983191803254889E-7, -3.356118100840571E-7};
   private static final long[] RECIP_2PI = new long[]{2935890503282001226L, 9154082963658192752L, 3952090531849364496L, 9193070505571053912L, 7910884519577875640L, 113236205062349959L, 4577762542105553359L, -5034868814120038111L, 4208363204685324176L, 5648769086999809661L, 2819561105158720014L, -4035746434778044925L, -302932621132653753L, -2644281811660520851L, -3183605296591799669L, 6722166367014452318L, -3512299194304650054L, -7278142539171889152L};
   private static final long[] PI_O_4_BITS = new long[]{-3958705157555305932L, -4267615245585081135L};
   private static final double[] EIGHTHS = new double[]{0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25, 1.375, 1.5, 1.625};
   private static final double[] CBRTTWO = new double[]{0.6299605249474366, 0.7937005259840998, 1.0, 1.2599210498948732, 1.5874010519681994};
   private static final long HEX_40000000 = 1073741824L;
   private static final long MASK_30BITS = -1073741824L;
   private static final int MASK_NON_SIGN_INT = Integer.MAX_VALUE;
   private static final long MASK_NON_SIGN_LONG = Long.MAX_VALUE;
   private static final long MASK_DOUBLE_EXPONENT = 9218868437227405312L;
   private static final long MASK_DOUBLE_MANTISSA = 4503599627370495L;
   private static final long IMPLICIT_HIGH_BIT = 4503599627370496L;
   private static final double TWO_POWER_52 = 4.503599627370496E15;
   private static final double F_1_3 = 0.3333333333333333;
   private static final double F_1_5 = 0.2;
   private static final double F_1_7 = 0.14285714285714285;
   private static final double F_1_9 = 0.1111111111111111;
   private static final double F_1_11 = 0.09090909090909091;
   private static final double F_1_13 = 0.07692307692307693;
   private static final double F_1_15 = 0.06666666666666667;
   private static final double F_1_17 = 0.058823529411764705;
   private static final double F_3_4 = 0.75;
   private static final double F_15_16 = 0.9375;
   private static final double F_13_14 = 0.9285714285714286;
   private static final double F_11_12 = 0.9166666666666666;
   private static final double F_9_10 = 0.9;
   private static final double F_7_8 = 0.875;
   private static final double F_5_6 = 0.8333333333333334;
   private static final double F_1_2 = 0.5;
   private static final double F_1_4 = 0.25;

   private FastMath() {
   }

   private static double doubleHighPart(double d) {
      if (d > -Precision.SAFE_MIN && d < Precision.SAFE_MIN) {
         return d;
      } else {
         long xl = Double.doubleToRawLongBits(d);
         xl &= -1073741824L;
         return Double.longBitsToDouble(xl);
      }
   }

   public static double sqrt(double a) {
      return Math.sqrt(a);
   }

   public static double cosh(double x) {
      if (x != x) {
         return x;
      } else {
         double t;
         if (x > 20.0) {
            if (x >= LOG_MAX_VALUE) {
               t = exp(0.5 * x);
               return 0.5 * t * t;
            } else {
               return 0.5 * exp(x);
            }
         } else if (x < -20.0) {
            if (x <= -LOG_MAX_VALUE) {
               t = exp(-0.5 * x);
               return 0.5 * t * t;
            } else {
               return 0.5 * exp(-x);
            }
         } else {
            double[] hiPrec = new double[2];
            if (x < 0.0) {
               x = -x;
            }

            exp(x, 0.0, hiPrec);
            double ya = hiPrec[0] + hiPrec[1];
            double yb = -(ya - hiPrec[0] - hiPrec[1]);
            double temp = ya * 1.073741824E9;
            double yaa = ya + temp - temp;
            double yab = ya - yaa;
            double recip = 1.0 / ya;
            temp = recip * 1.073741824E9;
            double recipa = recip + temp - temp;
            double recipb = recip - recipa;
            recipb += (1.0 - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
            recipb += -yb * recip * recip;
            temp = ya + recipa;
            yb += -(temp - ya - recipa);
            ya = temp;
            temp += recipb;
            yb += -(temp - ya - recipb);
            double result = temp + yb;
            result *= 0.5;
            return result;
         }
      }
   }

   public static double sinh(double x) {
      boolean negate = false;
      if (x != x) {
         return x;
      } else {
         double result;
         if (x > 20.0) {
            if (x >= LOG_MAX_VALUE) {
               result = exp(0.5 * x);
               return 0.5 * result * result;
            } else {
               return 0.5 * exp(x);
            }
         } else if (x < -20.0) {
            if (x <= -LOG_MAX_VALUE) {
               result = exp(-0.5 * x);
               return -0.5 * result * result;
            } else {
               return -0.5 * exp(-x);
            }
         } else if (x == 0.0) {
            return x;
         } else {
            if (x < 0.0) {
               x = -x;
               negate = true;
            }

            double[] hiPrec;
            double ya;
            double yb;
            double temp;
            double yaa;
            double yab;
            double recip;
            double recipa;
            double recipb;
            if (x > 0.25) {
               hiPrec = new double[2];
               exp(x, 0.0, hiPrec);
               ya = hiPrec[0] + hiPrec[1];
               yb = -(ya - hiPrec[0] - hiPrec[1]);
               temp = ya * 1.073741824E9;
               yaa = ya + temp - temp;
               yab = ya - yaa;
               recip = 1.0 / ya;
               temp = recip * 1.073741824E9;
               recipa = recip + temp - temp;
               recipb = recip - recipa;
               recipb += (1.0 - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
               recipb += -yb * recip * recip;
               recipa = -recipa;
               recipb = -recipb;
               temp = ya + recipa;
               yb += -(temp - ya - recipa);
               ya = temp;
               temp += recipb;
               yb += -(temp - ya - recipb);
               result = temp + yb;
               result *= 0.5;
            } else {
               hiPrec = new double[2];
               expm1(x, hiPrec);
               ya = hiPrec[0] + hiPrec[1];
               yb = -(ya - hiPrec[0] - hiPrec[1]);
               temp = 1.0 + ya;
               yaa = 1.0 / temp;
               yab = -(temp - 1.0 - ya) + yb;
               recip = ya * yaa;
               recipa = recip * 1.073741824E9;
               recipb = recip + recipa - recipa;
               double rb = recip - recipb;
               recipa = temp * 1.073741824E9;
               double za = temp + recipa - recipa;
               double zb = temp - za;
               rb += (ya - za * recipb - za * rb - zb * recipb - zb * rb) * yaa;
               rb += yb * yaa;
               rb += -ya * yab * yaa * yaa;
               recipa = ya + recipb;
               yb += -(recipa - ya - recipb);
               ya = recipa;
               recipa += rb;
               yb += -(recipa - ya - rb);
               result = recipa + yb;
               result *= 0.5;
            }

            if (negate) {
               result = -result;
            }

            return result;
         }
      }
   }

   public static double tanh(double x) {
      boolean negate = false;
      if (x != x) {
         return x;
      } else if (x > 20.0) {
         return 1.0;
      } else if (x < -20.0) {
         return -1.0;
      } else if (x == 0.0) {
         return x;
      } else {
         if (x < 0.0) {
            x = -x;
            negate = true;
         }

         double result;
         double[] hiPrec;
         double ya;
         double yb;
         double temp;
         double da;
         double db;
         double daa;
         double dab;
         double ratio;
         double ratioa;
         double ratiob;
         if (x >= 0.5) {
            hiPrec = new double[2];
            exp(x * 2.0, 0.0, hiPrec);
            ya = hiPrec[0] + hiPrec[1];
            yb = -(ya - hiPrec[0] - hiPrec[1]);
            double na = -1.0 + ya;
            double nb = -(na + 1.0 - ya);
            temp = na + yb;
            nb += -(temp - na - yb);
            na = temp;
            da = 1.0 + ya;
            db = -(da - 1.0 - ya);
            temp = da + yb;
            db += -(temp - da - yb);
            da = temp;
            temp *= 1.073741824E9;
            daa = da + temp - temp;
            dab = da - daa;
            ratio = na / da;
            temp = ratio * 1.073741824E9;
            ratioa = ratio + temp - temp;
            ratiob = ratio - ratioa;
            ratiob += (na - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / da;
            ratiob += nb / da;
            ratiob += -db * na / da / da;
            result = ratioa + ratiob;
         } else {
            hiPrec = new double[2];
            expm1(x * 2.0, hiPrec);
            ya = hiPrec[0] + hiPrec[1];
            yb = -(ya - hiPrec[0] - hiPrec[1]);
            temp = 2.0 + ya;
            da = -(temp - 2.0 - ya);
            db = temp + yb;
            da += -(db - temp - yb);
            temp = db;
            db *= 1.073741824E9;
            daa = temp + db - db;
            dab = temp - daa;
            ratio = ya / temp;
            db = ratio * 1.073741824E9;
            ratioa = ratio + db - db;
            ratiob = ratio - ratioa;
            ratiob += (ya - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / temp;
            ratiob += yb / temp;
            ratiob += -da * ya / temp / temp;
            result = ratioa + ratiob;
         }

         if (negate) {
            result = -result;
         }

         return result;
      }
   }

   public static double acosh(double a) {
      return log(a + sqrt(a * a - 1.0));
   }

   public static double asinh(double a) {
      boolean negative = false;
      if (a < 0.0) {
         negative = true;
         a = -a;
      }

      double absAsinh;
      if (a > 0.167) {
         absAsinh = log(sqrt(a * a + 1.0) + a);
      } else {
         double a2 = a * a;
         if (a > 0.097) {
            absAsinh = a * (1.0 - a2 * (0.3333333333333333 - a2 * (0.2 - a2 * (0.14285714285714285 - a2 * (0.1111111111111111 - a2 * (0.09090909090909091 - a2 * (0.07692307692307693 - a2 * (0.06666666666666667 - a2 * 0.058823529411764705 * 0.9375) * 0.9285714285714286) * 0.9166666666666666) * 0.9) * 0.875) * 0.8333333333333334) * 0.75) * 0.5);
         } else if (a > 0.036) {
            absAsinh = a * (1.0 - a2 * (0.3333333333333333 - a2 * (0.2 - a2 * (0.14285714285714285 - a2 * (0.1111111111111111 - a2 * (0.09090909090909091 - a2 * 0.07692307692307693 * 0.9166666666666666) * 0.9) * 0.875) * 0.8333333333333334) * 0.75) * 0.5);
         } else if (a > 0.0036) {
            absAsinh = a * (1.0 - a2 * (0.3333333333333333 - a2 * (0.2 - a2 * (0.14285714285714285 - a2 * 0.1111111111111111 * 0.875) * 0.8333333333333334) * 0.75) * 0.5);
         } else {
            absAsinh = a * (1.0 - a2 * (0.3333333333333333 - a2 * 0.2 * 0.75) * 0.5);
         }
      }

      return negative ? -absAsinh : absAsinh;
   }

   public static double atanh(double a) {
      boolean negative = false;
      if (a < 0.0) {
         negative = true;
         a = -a;
      }

      double absAtanh;
      if (a > 0.15) {
         absAtanh = 0.5 * log((1.0 + a) / (1.0 - a));
      } else {
         double a2 = a * a;
         if (a > 0.087) {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * (0.2 + a2 * (0.14285714285714285 + a2 * (0.1111111111111111 + a2 * (0.09090909090909091 + a2 * (0.07692307692307693 + a2 * (0.06666666666666667 + a2 * 0.058823529411764705))))))));
         } else if (a > 0.031) {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * (0.2 + a2 * (0.14285714285714285 + a2 * (0.1111111111111111 + a2 * (0.09090909090909091 + a2 * 0.07692307692307693))))));
         } else if (a > 0.003) {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * (0.2 + a2 * (0.14285714285714285 + a2 * 0.1111111111111111))));
         } else {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * 0.2));
         }
      }

      return negative ? -absAtanh : absAtanh;
   }

   public static double signum(double a) {
      return a < 0.0 ? -1.0 : (a > 0.0 ? 1.0 : a);
   }

   public static float signum(float a) {
      return a < 0.0F ? -1.0F : (a > 0.0F ? 1.0F : a);
   }

   public static double nextUp(double a) {
      return nextAfter(a, Double.POSITIVE_INFINITY);
   }

   public static float nextUp(float a) {
      return nextAfter(a, Double.POSITIVE_INFINITY);
   }

   public static double nextDown(double a) {
      return nextAfter(a, Double.NEGATIVE_INFINITY);
   }

   public static float nextDown(float a) {
      return nextAfter(a, Double.NEGATIVE_INFINITY);
   }

   public static double random() {
      return Math.random();
   }

   public static double exp(double x) {
      return exp(x, 0.0, (double[])null);
   }

   private static double exp(double x, double extra, double[] hiPrec) {
      int intVal = (int)x;
      if (x < 0.0) {
         if (x < -746.0) {
            if (hiPrec != null) {
               hiPrec[0] = 0.0;
               hiPrec[1] = 0.0;
            }

            return 0.0;
         }

         double result;
         if (intVal < -709) {
            result = exp(x + 40.19140625, extra, hiPrec) / 2.8504009514401178E17;
            if (hiPrec != null) {
               hiPrec[0] /= 2.8504009514401178E17;
               hiPrec[1] /= 2.8504009514401178E17;
            }

            return result;
         }

         if (intVal == -709) {
            result = exp(x + 1.494140625, extra, hiPrec) / 4.455505956692757;
            if (hiPrec != null) {
               hiPrec[0] /= 4.455505956692757;
               hiPrec[1] /= 4.455505956692757;
            }

            return result;
         }

         --intVal;
      } else if (intVal > 709) {
         if (hiPrec != null) {
            hiPrec[0] = Double.POSITIVE_INFINITY;
            hiPrec[1] = 0.0;
         }

         return Double.POSITIVE_INFINITY;
      }

      double intPartA = FastMath.ExpIntTable.EXP_INT_TABLE_A[750 + intVal];
      double intPartB = FastMath.ExpIntTable.EXP_INT_TABLE_B[750 + intVal];
      int intFrac = (int)((x - (double)intVal) * 1024.0);
      double fracPartA = FastMath.ExpFracTable.EXP_FRAC_TABLE_A[intFrac];
      double fracPartB = FastMath.ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
      double epsilon = x - ((double)intVal + (double)intFrac / 1024.0);
      double z = 0.04168701738764507;
      z = z * epsilon + 0.1666666505023083;
      z = z * epsilon + 0.5000000000042687;
      z = z * epsilon + 1.0;
      z = z * epsilon + -3.940510424527919E-20;
      double tempA = intPartA * fracPartA;
      double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;
      double tempC = tempB + tempA;
      if (tempC == Double.POSITIVE_INFINITY) {
         return Double.POSITIVE_INFINITY;
      } else {
         double result;
         if (extra != 0.0) {
            result = tempC * extra * z + tempC * extra + tempC * z + tempB + tempA;
         } else {
            result = tempC * z + tempB + tempA;
         }

         if (hiPrec != null) {
            hiPrec[0] = tempA;
            hiPrec[1] = tempC * extra * z + tempC * extra + tempC * z + tempB;
         }

         return result;
      }
   }

   public static double expm1(double x) {
      return expm1(x, (double[])null);
   }

   private static double expm1(double x, double[] hiPrecOut) {
      if (x == x && x != 0.0) {
         if (!(x <= -1.0) && !(x >= 1.0)) {
            boolean negative = false;
            if (x < 0.0) {
               x = -x;
               negative = true;
            }

            int intFrac = (int)(x * 1024.0);
            double tempA = FastMath.ExpFracTable.EXP_FRAC_TABLE_A[intFrac] - 1.0;
            double tempB = FastMath.ExpFracTable.EXP_FRAC_TABLE_B[intFrac];
            double temp = tempA + tempB;
            tempB = -(temp - tempA - tempB);
            tempA = temp;
            temp *= 1.073741824E9;
            double baseA = tempA + temp - temp;
            double baseB = tempB + (tempA - baseA);
            double epsilon = x - (double)intFrac / 1024.0;
            double zb = 0.008336750013465571;
            zb = zb * epsilon + 0.041666663879186654;
            zb = zb * epsilon + 0.16666666666745392;
            zb = zb * epsilon + 0.49999999999999994;
            zb *= epsilon;
            zb *= epsilon;
            double temp = epsilon + zb;
            zb = -(temp - epsilon - zb);
            double za = temp;
            temp *= 1.073741824E9;
            temp = za + temp - temp;
            zb += za - temp;
            za = temp;
            double ya = temp * baseA;
            temp = ya + temp * baseB;
            double yb = -(temp - ya - za * baseB);
            ya = temp;
            temp += zb * baseA;
            yb += -(temp - ya - zb * baseA);
            ya = temp;
            temp += zb * baseB;
            yb += -(temp - ya - zb * baseB);
            ya = temp;
            temp += baseA;
            yb += -(temp - baseA - ya);
            ya = temp;
            temp += za;
            yb += -(temp - ya - za);
            ya = temp;
            temp += baseB;
            yb += -(temp - ya - baseB);
            ya = temp;
            temp += zb;
            yb += -(temp - ya - zb);
            ya = temp;
            if (negative) {
               double denom = 1.0 + temp;
               double denomr = 1.0 / denom;
               double denomb = -(denom - 1.0 - temp) + yb;
               double ratio = temp * denomr;
               temp = ratio * 1.073741824E9;
               double ra = ratio + temp - temp;
               double rb = ratio - ra;
               temp = denom * 1.073741824E9;
               za = denom + temp - temp;
               zb = denom - za;
               rb += (ya - za * ra - za * rb - zb * ra - zb * rb) * denomr;
               rb += yb * denomr;
               rb += -ya * denomb * denomr * denomr;
               ya = -ra;
               yb = -rb;
            }

            if (hiPrecOut != null) {
               hiPrecOut[0] = ya;
               hiPrecOut[1] = yb;
            }

            return ya + yb;
         } else {
            double[] hiPrec = new double[2];
            exp(x, 0.0, hiPrec);
            if (x > 0.0) {
               return -1.0 + hiPrec[0] + hiPrec[1];
            } else {
               double ra = -1.0 + hiPrec[0];
               double rb = -(ra + 1.0 - hiPrec[0]);
               rb += hiPrec[1];
               return ra + rb;
            }
         }
      } else {
         return x;
      }
   }

   public static double log(double x) {
      return log(x, (double[])null);
   }

   private static double log(double x, double[] hiPrec) {
      if (x == 0.0) {
         return Double.NEGATIVE_INFINITY;
      } else {
         long bits = Double.doubleToRawLongBits(x);
         if (((bits & Long.MIN_VALUE) != 0L || x != x) && x != 0.0) {
            if (hiPrec != null) {
               hiPrec[0] = Double.NaN;
            }

            return Double.NaN;
         } else if (x == Double.POSITIVE_INFINITY) {
            if (hiPrec != null) {
               hiPrec[0] = Double.POSITIVE_INFINITY;
            }

            return Double.POSITIVE_INFINITY;
         } else {
            int exp = (int)(bits >> 52) - 1023;
            if ((bits & 9218868437227405312L) == 0L) {
               if (x == 0.0) {
                  if (hiPrec != null) {
                     hiPrec[0] = Double.NEGATIVE_INFINITY;
                  }

                  return Double.NEGATIVE_INFINITY;
               }

               for(bits <<= 1; (bits & 4503599627370496L) == 0L; bits <<= 1) {
                  --exp;
               }
            }

            double ab;
            double xa;
            if ((exp == -1 || exp == 0) && x < 1.01 && x > 0.99 && hiPrec == null) {
               double xa = x - 1.0;
               double xb = xa - x + 1.0;
               double tmp = xa * 1.073741824E9;
               double aa = xa + tmp - tmp;
               double ab = xa - aa;
               xa = aa;
               xb = ab;
               double[] lnCoef_last = LN_QUICK_COEF[LN_QUICK_COEF.length - 1];
               ab = lnCoef_last[0];
               xa = lnCoef_last[1];

               for(int i = LN_QUICK_COEF.length - 2; i >= 0; --i) {
                  aa = ab * xa;
                  ab = ab * xb + xa * xa + xa * xb;
                  tmp = aa * 1.073741824E9;
                  ab = aa + tmp - tmp;
                  xa = aa - ab + ab;
                  double[] lnCoef_i = LN_QUICK_COEF[i];
                  aa = ab + lnCoef_i[0];
                  ab = xa + lnCoef_i[1];
                  tmp = aa * 1.073741824E9;
                  ab = aa + tmp - tmp;
                  xa = aa - ab + ab;
               }

               aa = ab * xa;
               ab = ab * xb + xa * xa + xa * xb;
               tmp = aa * 1.073741824E9;
               ab = aa + tmp - tmp;
               xa = aa - ab + ab;
               return ab + xa;
            } else {
               double[] lnm = FastMath.lnMant.LN_MANT[(int)((bits & 4499201580859392L) >> 42)];
               double epsilon = (double)(bits & 4398046511103L) / (4.503599627370496E15 + (double)(bits & 4499201580859392L));
               double lnza = 0.0;
               double lnzb = 0.0;
               double tmp;
               double aa;
               if (hiPrec != null) {
                  tmp = epsilon * 1.073741824E9;
                  aa = epsilon + tmp - tmp;
                  ab = epsilon - aa;
                  xa = aa;
                  double numer = (double)(bits & 4398046511103L);
                  double denom = 4.503599627370496E15 + (double)(bits & 4499201580859392L);
                  aa = numer - aa * denom - ab * denom;
                  double xb = ab + aa / denom;
                  double[] lnCoef_last = LN_HI_PREC_COEF[LN_HI_PREC_COEF.length - 1];
                  double ya = lnCoef_last[0];
                  double yb = lnCoef_last[1];

                  for(int i = LN_HI_PREC_COEF.length - 2; i >= 0; --i) {
                     aa = ya * xa;
                     ab = ya * xb + yb * xa + yb * xb;
                     tmp = aa * 1.073741824E9;
                     ya = aa + tmp - tmp;
                     yb = aa - ya + ab;
                     double[] lnCoef_i = LN_HI_PREC_COEF[i];
                     aa = ya + lnCoef_i[0];
                     ab = yb + lnCoef_i[1];
                     tmp = aa * 1.073741824E9;
                     ya = aa + tmp - tmp;
                     yb = aa - ya + ab;
                  }

                  aa = ya * xa;
                  ab = ya * xb + yb * xa + yb * xb;
                  lnza = aa + ab;
                  lnzb = -(lnza - aa - ab);
               } else {
                  lnza = -0.16624882440418567;
                  lnza = lnza * epsilon + 0.19999954120254515;
                  lnza = lnza * epsilon + -0.2499999997677497;
                  lnza = lnza * epsilon + 0.3333333333332802;
                  lnza = lnza * epsilon + -0.5;
                  lnza = lnza * epsilon + 1.0;
                  lnza *= epsilon;
               }

               tmp = 0.6931470632553101 * (double)exp;
               aa = 0.0;
               ab = tmp + lnm[0];
               xa = -(ab - tmp - lnm[0]);
               tmp = ab;
               aa += xa;
               ab += lnza;
               xa = -(ab - tmp - lnza);
               tmp = ab;
               aa += xa;
               ab += 1.1730463525082348E-7 * (double)exp;
               xa = -(ab - tmp - 1.1730463525082348E-7 * (double)exp);
               tmp = ab;
               aa += xa;
               ab += lnm[1];
               xa = -(ab - tmp - lnm[1]);
               tmp = ab;
               aa += xa;
               ab += lnzb;
               xa = -(ab - tmp - lnzb);
               aa += xa;
               if (hiPrec != null) {
                  hiPrec[0] = ab;
                  hiPrec[1] = aa;
               }

               return ab + aa;
            }
         }
      }
   }

   public static double log1p(double x) {
      if (x == -1.0) {
         return Double.NEGATIVE_INFINITY;
      } else if (x == Double.POSITIVE_INFINITY) {
         return Double.POSITIVE_INFINITY;
      } else {
         double xpa;
         if (!(x > 1.0E-6) && !(x < -1.0E-6)) {
            xpa = (x * 0.3333333333333333 - 0.5) * x + 1.0;
            return xpa * x;
         } else {
            xpa = 1.0 + x;
            double xpb = -(xpa - 1.0 - x);
            double[] hiPrec = new double[2];
            double lores = log(xpa, hiPrec);
            if (Double.isInfinite(lores)) {
               return lores;
            } else {
               double fx1 = xpb / xpa;
               double epsilon = 0.5 * fx1 + 1.0;
               return epsilon * fx1 + hiPrec[1] + hiPrec[0];
            }
         }
      }
   }

   public static double log10(double x) {
      double[] hiPrec = new double[2];
      double lores = log(x, hiPrec);
      if (Double.isInfinite(lores)) {
         return lores;
      } else {
         double tmp = hiPrec[0] * 1.073741824E9;
         double lna = hiPrec[0] + tmp - tmp;
         double lnb = hiPrec[0] - lna + hiPrec[1];
         double rln10a = 0.4342944622039795;
         double rln10b = 1.9699272335463627E-8;
         return 1.9699272335463627E-8 * lnb + 1.9699272335463627E-8 * lna + 0.4342944622039795 * lnb + 0.4342944622039795 * lna;
      }
   }

   public static double log(double base, double x) {
      return log(x) / log(base);
   }

   public static double pow(double x, double y) {
      if (y == 0.0) {
         return 1.0;
      } else {
         long yBits = Double.doubleToRawLongBits(y);
         int yRawExp = (int)((yBits & 9218868437227405312L) >> 52);
         long yRawMantissa = yBits & 4503599627370495L;
         long xBits = Double.doubleToRawLongBits(x);
         int xRawExp = (int)((xBits & 9218868437227405312L) >> 52);
         long xRawMantissa = xBits & 4503599627370495L;
         if (yRawExp <= 1085) {
            if (yRawExp >= 1023) {
               long yFullMantissa = 4503599627370496L | yRawMantissa;
               long integralMask;
               if (yRawExp >= 1075) {
                  integralMask = yFullMantissa << yRawExp - 1075;
                  return pow(x, y < 0.0 ? -integralMask : integralMask);
               }

               integralMask = -1L << 1075 - yRawExp;
               if ((yFullMantissa & integralMask) == yFullMantissa) {
                  long l = yFullMantissa >> 1075 - yRawExp;
                  return pow(x, y < 0.0 ? -l : l);
               }
            }

            if (x == 0.0) {
               return y < 0.0 ? Double.POSITIVE_INFINITY : 0.0;
            } else if (xRawExp == 2047) {
               if (xRawMantissa == 0L) {
                  return y < 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
               } else {
                  return Double.NaN;
               }
            } else if (x < 0.0) {
               return Double.NaN;
            } else {
               double tmp = y * 1.073741824E9;
               double ya = y + tmp - tmp;
               double yb = y - ya;
               double[] lns = new double[2];
               double lores = log(x, lns);
               if (Double.isInfinite(lores)) {
                  return lores;
               } else {
                  double lna = lns[0];
                  double lnb = lns[1];
                  double tmp1 = lna * 1.073741824E9;
                  double tmp2 = lna + tmp1 - tmp1;
                  lnb += lna - tmp2;
                  double aa = tmp2 * ya;
                  double ab = tmp2 * yb + lnb * ya + lnb * yb;
                  lna = aa + ab;
                  lnb = -(lna - aa - ab);
                  double z = 0.008333333333333333;
                  z = z * lnb + 0.041666666666666664;
                  z = z * lnb + 0.16666666666666666;
                  z = z * lnb + 0.5;
                  z = z * lnb + 1.0;
                  z *= lnb;
                  double result = exp(lna, z, (double[])null);
                  return result;
               }
            }
         } else if (yRawExp == 2047 && yRawMantissa != 0L || xRawExp == 2047 && xRawMantissa != 0L) {
            return Double.NaN;
         } else if (xRawExp == 1023 && xRawMantissa == 0L) {
            return yRawExp == 2047 ? Double.NaN : 1.0;
         } else {
            return y > 0.0 ^ xRawExp < 1023 ? Double.POSITIVE_INFINITY : 0.0;
         }
      }
   }

   public static double pow(double d, int e) {
      return pow(d, (long)e);
   }

   public static double pow(double d, long e) {
      if (e == 0L) {
         return 1.0;
      } else {
         return e > 0L ? (new Split(d)).pow(e).full : (new Split(d)).reciprocal().pow(-e).full;
      }
   }

   private static double polySine(double x) {
      double x2 = x * x;
      double p = 2.7553817452272217E-6;
      p = p * x2 + -1.9841269659586505E-4;
      p = p * x2 + 0.008333333333329196;
      p = p * x2 + -0.16666666666666666;
      p = p * x2 * x;
      return p;
   }

   private static double polyCosine(double x) {
      double x2 = x * x;
      double p = 2.479773539153719E-5;
      p = p * x2 + -0.0013888888689039883;
      p = p * x2 + 0.041666666666621166;
      p = p * x2 + -0.49999999999999994;
      p *= x2;
      return p;
   }

   private static double sinQ(double xa, double xb) {
      int idx = (int)(xa * 8.0 + 0.5);
      double epsilon = xa - EIGHTHS[idx];
      double sintA = SINE_TABLE_A[idx];
      double sintB = SINE_TABLE_B[idx];
      double costA = COSINE_TABLE_A[idx];
      double costB = COSINE_TABLE_B[idx];
      double sinEpsB = polySine(epsilon);
      double cosEpsA = 1.0;
      double cosEpsB = polyCosine(epsilon);
      double temp = epsilon * 1.073741824E9;
      double temp2 = epsilon + temp - temp;
      sinEpsB += epsilon - temp2;
      double a = 0.0;
      double b = 0.0;
      double c = a + sintA;
      double d = -(c - a - sintA);
      a = c;
      b += d;
      double t = costA * temp2;
      c += t;
      d = -(c - a - t);
      a = c;
      b += d;
      b = b + sintA * cosEpsB + costA * sinEpsB;
      b = b + sintB + costB * temp2 + sintB * cosEpsB + costB * sinEpsB;
      if (xb != 0.0) {
         t = ((costA + costB) * (1.0 + cosEpsB) - (sintA + sintB) * (temp2 + sinEpsB)) * xb;
         c += t;
         d = -(c - a - t);
         a = c;
         b += d;
      }

      double result = a + b;
      return result;
   }

   private static double cosQ(double xa, double xb) {
      double pi2a = 1.5707963267948966;
      double pi2b = 6.123233995736766E-17;
      double a = 1.5707963267948966 - xa;
      double b = -(a - 1.5707963267948966 + xa);
      b += 6.123233995736766E-17 - xb;
      return sinQ(a, b);
   }

   private static double tanQ(double xa, double xb, boolean cotanFlag) {
      int idx = (int)(xa * 8.0 + 0.5);
      double epsilon = xa - EIGHTHS[idx];
      double sintA = SINE_TABLE_A[idx];
      double sintB = SINE_TABLE_B[idx];
      double costA = COSINE_TABLE_A[idx];
      double costB = COSINE_TABLE_B[idx];
      double sinEpsB = polySine(epsilon);
      double cosEpsA = 1.0;
      double cosEpsB = polyCosine(epsilon);
      double temp = epsilon * 1.073741824E9;
      double temp2 = epsilon + temp - temp;
      sinEpsB += epsilon - temp2;
      double a = 0.0;
      double b = 0.0;
      double c = a + sintA;
      double d = -(c - a - sintA);
      a = c;
      b += d;
      double t = costA * temp2;
      c += t;
      d = -(c - a - t);
      b += d;
      b += sintA * cosEpsB + costA * sinEpsB;
      b += sintB + costB * temp2 + sintB * cosEpsB + costB * sinEpsB;
      double sina = c + b;
      double sinb = -(sina - c - b);
      d = 0.0;
      c = 0.0;
      b = 0.0;
      a = 0.0;
      t = costA * 1.0;
      c = a + t;
      d = -(c - a - t);
      a = c;
      b += d;
      t = -sintA * temp2;
      c += t;
      d = -(c - a - t);
      b += d;
      b += costB * 1.0 + costA * cosEpsB + costB * cosEpsB;
      b -= sintB * temp2 + sintA * sinEpsB + sintB * sinEpsB;
      double cosa = c + b;
      double cosb = -(cosa - c - b);
      double est;
      if (cotanFlag) {
         est = cosa;
         cosa = sina;
         sina = est;
         est = cosb;
         cosb = sinb;
         sinb = est;
      }

      est = sina / cosa;
      temp = est * 1.073741824E9;
      double esta = est + temp - temp;
      double estb = est - esta;
      temp = cosa * 1.073741824E9;
      double cosaa = cosa + temp - temp;
      double cosab = cosa - cosaa;
      double err = (sina - esta * cosaa - esta * cosab - estb * cosaa - estb * cosab) / cosa;
      err += sinb / cosa;
      err += -sina * cosb / cosa / cosa;
      if (xb != 0.0) {
         double xbadj = xb + est * est * xb;
         if (cotanFlag) {
            xbadj = -xbadj;
         }

         err += xbadj;
      }

      return est + err;
   }

   private static void reducePayneHanek(double x, double[] result) {
      long inbits = Double.doubleToRawLongBits(x);
      int exponent = (int)(inbits >> 52 & 2047L) - 1023;
      inbits &= 4503599627370495L;
      inbits |= 4503599627370496L;
      ++exponent;
      inbits <<= 11;
      int idx = exponent >> 6;
      int shift = exponent - (idx << 6);
      long shpi0;
      long shpiA;
      long shpiB;
      if (shift != 0) {
         shpi0 = idx == 0 ? 0L : RECIP_2PI[idx - 1] << shift;
         shpi0 |= RECIP_2PI[idx] >>> 64 - shift;
         shpiA = RECIP_2PI[idx] << shift | RECIP_2PI[idx + 1] >>> 64 - shift;
         shpiB = RECIP_2PI[idx + 1] << shift | RECIP_2PI[idx + 2] >>> 64 - shift;
      } else {
         shpi0 = idx == 0 ? 0L : RECIP_2PI[idx - 1];
         shpiA = RECIP_2PI[idx];
         shpiB = RECIP_2PI[idx + 1];
      }

      long a = inbits >>> 32;
      long b = inbits & 4294967295L;
      long c = shpiA >>> 32;
      long d = shpiA & 4294967295L;
      long ac = a * c;
      long bd = b * d;
      long bc = b * c;
      long ad = a * d;
      long prodB = bd + (ad << 32);
      long prodA = ac + (ad >>> 32);
      boolean bita = (bd & Long.MIN_VALUE) != 0L;
      boolean bitb = (ad & 2147483648L) != 0L;
      boolean bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      bita = (prodB & Long.MIN_VALUE) != 0L;
      bitb = (bc & 2147483648L) != 0L;
      prodB += bc << 32;
      prodA += bc >>> 32;
      bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      c = shpiB >>> 32;
      d = shpiB & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prodB & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prodB += ac;
      bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      c = shpi0 >>> 32;
      d = shpi0 & 4294967295L;
      bd = b * d;
      bc = b * c;
      ad = a * d;
      prodA += bd + (bc + ad << 32);
      int intPart = (int)(prodA >>> 62);
      prodA <<= 2;
      prodA |= prodB >>> 62;
      prodB <<= 2;
      a = prodA >>> 32;
      b = prodA & 4294967295L;
      c = PI_O_4_BITS[0] >>> 32;
      d = PI_O_4_BITS[0] & 4294967295L;
      ac = a * c;
      bd = b * d;
      bc = b * c;
      ad = a * d;
      long prod2B = bd + (ad << 32);
      long prod2A = ac + (ad >>> 32);
      bita = (bd & Long.MIN_VALUE) != 0L;
      bitb = (ad & 2147483648L) != 0L;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (bc & 2147483648L) != 0L;
      prod2B += bc << 32;
      prod2A += bc >>> 32;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      c = PI_O_4_BITS[1] >>> 32;
      d = PI_O_4_BITS[1] & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prod2B += ac;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      a = prodB >>> 32;
      b = prodB & 4294967295L;
      c = PI_O_4_BITS[0] >>> 32;
      d = PI_O_4_BITS[0] & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prod2B += ac;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      double tmpA = (double)(prod2A >>> 12) / 4.503599627370496E15;
      double tmpB = (double)(((prod2A & 4095L) << 40) + (prod2B >>> 24)) / 4.503599627370496E15 / 4.503599627370496E15;
      double sumA = tmpA + tmpB;
      double sumB = -(sumA - tmpA - tmpB);
      result[0] = (double)intPart;
      result[1] = sumA * 2.0;
      result[2] = sumB * 2.0;
   }

   public static double sin(double x) {
      boolean negative = false;
      int quadrant = 0;
      double xb = 0.0;
      double xa = x;
      if (x < 0.0) {
         negative = true;
         xa = -x;
      }

      if (xa == 0.0) {
         long bits = Double.doubleToRawLongBits(x);
         return bits < 0L ? -0.0 : 0.0;
      } else if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > 1.5707963267948966) {
            CodyWaite cw = new CodyWaite(xa);
            quadrant = cw.getK() & 3;
            xa = cw.getRemA();
            xb = cw.getRemB();
         }

         if (negative) {
            quadrant ^= 2;
         }

         switch (quadrant) {
            case 0:
               return sinQ(xa, xb);
            case 1:
               return cosQ(xa, xb);
            case 2:
               return -sinQ(xa, xb);
            case 3:
               return -cosQ(xa, xb);
            default:
               return Double.NaN;
         }
      } else {
         return Double.NaN;
      }
   }

   public static double cos(double x) {
      int quadrant = 0;
      double xa = x;
      if (x < 0.0) {
         xa = -x;
      }

      if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         double xb = 0.0;
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > 1.5707963267948966) {
            CodyWaite cw = new CodyWaite(xa);
            quadrant = cw.getK() & 3;
            xa = cw.getRemA();
            xb = cw.getRemB();
         }

         switch (quadrant) {
            case 0:
               return cosQ(xa, xb);
            case 1:
               return -sinQ(xa, xb);
            case 2:
               return -cosQ(xa, xb);
            case 3:
               return sinQ(xa, xb);
            default:
               return Double.NaN;
         }
      } else {
         return Double.NaN;
      }
   }

   public static double tan(double x) {
      boolean negative = false;
      int quadrant = 0;
      double xa = x;
      if (x < 0.0) {
         negative = true;
         xa = -x;
      }

      if (xa == 0.0) {
         long bits = Double.doubleToRawLongBits(x);
         return bits < 0L ? -0.0 : 0.0;
      } else if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         double xb = 0.0;
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > 1.5707963267948966) {
            CodyWaite cw = new CodyWaite(xa);
            quadrant = cw.getK() & 3;
            xa = cw.getRemA();
            xb = cw.getRemB();
         }

         double result;
         if (xa > 1.5) {
            result = 1.5707963267948966;
            double pi2b = 6.123233995736766E-17;
            double a = 1.5707963267948966 - xa;
            double b = -(a - 1.5707963267948966 + xa);
            b += 6.123233995736766E-17 - xb;
            xa = a + b;
            xb = -(xa - a - b);
            quadrant ^= 1;
            negative ^= true;
         }

         if ((quadrant & 1) == 0) {
            result = tanQ(xa, xb, false);
         } else {
            result = -tanQ(xa, xb, true);
         }

         if (negative) {
            result = -result;
         }

         return result;
      } else {
         return Double.NaN;
      }
   }

   public static double atan(double x) {
      return atan(x, 0.0, false);
   }

   private static double atan(double xa, double xb, boolean leftPlane) {
      if (xa == 0.0) {
         return leftPlane ? copySign(Math.PI, xa) : xa;
      } else {
         boolean negate;
         if (xa < 0.0) {
            xa = -xa;
            xb = -xb;
            negate = true;
         } else {
            negate = false;
         }

         if (xa > 1.633123935319537E16) {
            return negate ^ leftPlane ? -1.5707963267948966 : 1.5707963267948966;
         } else {
            int idx;
            double ttA;
            if (xa < 1.0) {
               idx = (int)((-1.7168146928204135 * xa * xa + 8.0) * xa + 0.5);
            } else {
               ttA = 1.0 / xa;
               idx = (int)(-((-1.7168146928204135 * ttA * ttA + 8.0) * ttA) + 13.07);
            }

            ttA = TANGENT_TABLE_A[idx];
            double ttB = TANGENT_TABLE_B[idx];
            double epsA = xa - ttA;
            double epsB = -(epsA - xa + ttA);
            epsB += xb - ttB;
            double temp = epsA + epsB;
            epsB = -(temp - epsA - epsB);
            epsA = temp;
            temp = xa * 1.073741824E9;
            double ya = xa + temp - temp;
            double yb = xb + xa - ya;
            xb += yb;
            double pia;
            double epsA2;
            double eighths;
            double za;
            double zb;
            double result;
            double resultb;
            if (idx == 0) {
               epsA2 = 1.0 / (1.0 + (ya + xb) * (ttA + ttB));
               ya = epsA * epsA2;
               yb = epsB * epsA2;
            } else {
               epsA2 = ya * ttA;
               eighths = 1.0 + epsA2;
               za = -(eighths - 1.0 - epsA2);
               epsA2 = xb * ttA + ya * ttB;
               temp = eighths + epsA2;
               za += -(temp - eighths - epsA2);
               eighths = temp;
               za += xb * ttB;
               ya = epsA / temp;
               temp = ya * 1.073741824E9;
               zb = ya + temp - temp;
               result = ya - zb;
               temp = eighths * 1.073741824E9;
               resultb = eighths + temp - temp;
               pia = eighths - resultb;
               yb = (epsA - zb * resultb - zb * pia - result * resultb - result * pia) / eighths;
               yb += -epsA * za / eighths / eighths;
               yb += epsB / eighths;
            }

            epsB = yb;
            epsA2 = ya * ya;
            yb = 0.07490822288864472;
            yb = yb * epsA2 - 0.09088450866185192;
            yb = yb * epsA2 + 0.11111095942313305;
            yb = yb * epsA2 - 0.1428571423679182;
            yb = yb * epsA2 + 0.19999999999923582;
            yb = yb * epsA2 - 0.33333333333333287;
            yb = yb * epsA2 * ya;
            temp = ya + yb;
            yb = -(temp - ya - yb);
            yb += epsB / (1.0 + ya * ya);
            eighths = EIGHTHS[idx];
            za = eighths + temp;
            zb = -(za - eighths - temp);
            temp = za + yb;
            zb += -(temp - za - yb);
            result = temp + zb;
            if (leftPlane) {
               resultb = -(result - temp - zb);
               pia = Math.PI;
               double pib = 1.2246467991473532E-16;
               za = Math.PI - result;
               zb = -(za - Math.PI + result);
               zb += 1.2246467991473532E-16 - resultb;
               result = za + zb;
            }

            if (negate ^ leftPlane) {
               result = -result;
            }

            return result;
         }
      }
   }

   public static double atan2(double y, double x) {
      if (x == x && y == y) {
         double r;
         double ra;
         double rb;
         if (y == 0.0) {
            r = x * y;
            ra = 1.0 / x;
            rb = 1.0 / y;
            if (ra == 0.0) {
               return x > 0.0 ? y : copySign(Math.PI, y);
            } else if (!(x < 0.0) && !(ra < 0.0)) {
               return r;
            } else {
               return !(y < 0.0) && !(rb < 0.0) ? Math.PI : -3.141592653589793;
            }
         } else if (y == Double.POSITIVE_INFINITY) {
            if (x == Double.POSITIVE_INFINITY) {
               return 0.7853981633974483;
            } else {
               return x == Double.NEGATIVE_INFINITY ? 2.356194490192345 : 1.5707963267948966;
            }
         } else if (y == Double.NEGATIVE_INFINITY) {
            if (x == Double.POSITIVE_INFINITY) {
               return -0.7853981633974483;
            } else {
               return x == Double.NEGATIVE_INFINITY ? -2.356194490192345 : -1.5707963267948966;
            }
         } else {
            if (x == Double.POSITIVE_INFINITY) {
               if (y > 0.0 || 1.0 / y > 0.0) {
                  return 0.0;
               }

               if (y < 0.0 || 1.0 / y < 0.0) {
                  return -0.0;
               }
            }

            if (x == Double.NEGATIVE_INFINITY) {
               if (y > 0.0 || 1.0 / y > 0.0) {
                  return Math.PI;
               }

               if (y < 0.0 || 1.0 / y < 0.0) {
                  return -3.141592653589793;
               }
            }

            if (x == 0.0) {
               if (y > 0.0 || 1.0 / y > 0.0) {
                  return 1.5707963267948966;
               }

               if (y < 0.0 || 1.0 / y < 0.0) {
                  return -1.5707963267948966;
               }
            }

            r = y / x;
            if (Double.isInfinite(r)) {
               return atan(r, 0.0, x < 0.0);
            } else {
               ra = doubleHighPart(r);
               rb = r - ra;
               double xa = doubleHighPart(x);
               double xb = x - xa;
               rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
               double temp = ra + rb;
               rb = -(temp - ra - rb);
               ra = temp;
               if (temp == 0.0) {
                  ra = copySign(0.0, y);
               }

               double result = atan(ra, rb, x < 0.0);
               return result;
            }
         }
      } else {
         return Double.NaN;
      }
   }

   public static double asin(double x) {
      if (x != x) {
         return Double.NaN;
      } else if (!(x > 1.0) && !(x < -1.0)) {
         if (x == 1.0) {
            return 1.5707963267948966;
         } else if (x == -1.0) {
            return -1.5707963267948966;
         } else if (x == 0.0) {
            return x;
         } else {
            double temp = x * 1.073741824E9;
            double xa = x + temp - temp;
            double xb = x - xa;
            double ya = xa * xa;
            double yb = xa * xb * 2.0 + xb * xb;
            ya = -ya;
            yb = -yb;
            double za = 1.0 + ya;
            double zb = -(za - 1.0 - ya);
            temp = za + yb;
            zb += -(temp - za - yb);
            za = temp;
            double y = sqrt(temp);
            temp = y * 1.073741824E9;
            ya = y + temp - temp;
            yb = y - ya;
            yb += (za - ya * ya - 2.0 * ya * yb - yb * yb) / (2.0 * y);
            double dx = zb / (2.0 * y);
            double r = x / y;
            temp = r * 1.073741824E9;
            double ra = r + temp - temp;
            double rb = r - ra;
            rb += (x - ra * ya - ra * yb - rb * ya - rb * yb) / y;
            rb += -x * dx / y / y;
            temp = ra + rb;
            rb = -(temp - ra - rb);
            return atan(temp, rb, false);
         }
      } else {
         return Double.NaN;
      }
   }

   public static double acos(double x) {
      if (x != x) {
         return Double.NaN;
      } else if (!(x > 1.0) && !(x < -1.0)) {
         if (x == -1.0) {
            return Math.PI;
         } else if (x == 1.0) {
            return 0.0;
         } else if (x == 0.0) {
            return 1.5707963267948966;
         } else {
            double temp = x * 1.073741824E9;
            double xa = x + temp - temp;
            double xb = x - xa;
            double ya = xa * xa;
            double yb = xa * xb * 2.0 + xb * xb;
            ya = -ya;
            yb = -yb;
            double za = 1.0 + ya;
            double zb = -(za - 1.0 - ya);
            temp = za + yb;
            zb += -(temp - za - yb);
            za = temp;
            double y = sqrt(temp);
            temp = y * 1.073741824E9;
            ya = y + temp - temp;
            yb = y - ya;
            yb += (za - ya * ya - 2.0 * ya * yb - yb * yb) / (2.0 * y);
            yb += zb / (2.0 * y);
            y = ya + yb;
            yb = -(y - ya - yb);
            double r = y / x;
            if (Double.isInfinite(r)) {
               return 1.5707963267948966;
            } else {
               double ra = doubleHighPart(r);
               double rb = r - ra;
               rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
               rb += yb / x;
               temp = ra + rb;
               rb = -(temp - ra - rb);
               return atan(temp, rb, x < 0.0);
            }
         }
      } else {
         return Double.NaN;
      }
   }

   public static double cbrt(double x) {
      long inbits = Double.doubleToRawLongBits(x);
      int exponent = (int)(inbits >> 52 & 2047L) - 1023;
      boolean subnormal = false;
      if (exponent == -1023) {
         if (x == 0.0) {
            return x;
         }

         subnormal = true;
         x *= 1.8014398509481984E16;
         inbits = Double.doubleToRawLongBits(x);
         exponent = (int)(inbits >> 52 & 2047L) - 1023;
      }

      if (exponent == 1024) {
         return x;
      } else {
         int exp3 = exponent / 3;
         double p2 = Double.longBitsToDouble(inbits & Long.MIN_VALUE | (long)(exp3 + 1023 & 2047) << 52);
         double mant = Double.longBitsToDouble(inbits & 4503599627370495L | 4607182418800017408L);
         double est = -0.010714690733195933;
         est = est * mant + 0.0875862700108075;
         est = est * mant + -0.3058015757857271;
         est = est * mant + 0.7249995199969751;
         est = est * mant + 0.5039018405998233;
         est *= CBRTTWO[exponent % 3 + 2];
         double xs = x / (p2 * p2 * p2);
         est += (xs - est * est * est) / (3.0 * est * est);
         est += (xs - est * est * est) / (3.0 * est * est);
         double temp = est * 1.073741824E9;
         double ya = est + temp - temp;
         double yb = est - ya;
         double za = ya * ya;
         double zb = ya * yb * 2.0 + yb * yb;
         temp = za * 1.073741824E9;
         double temp2 = za + temp - temp;
         zb += za - temp2;
         zb = temp2 * yb + ya * zb + zb * yb;
         za = temp2 * ya;
         double na = xs - za;
         double nb = -(na - xs + za);
         nb -= zb;
         est += (na + nb) / (3.0 * est * est);
         est *= p2;
         if (subnormal) {
            est *= 3.814697265625E-6;
         }

         return est;
      }
   }

   public static double toRadians(double x) {
      if (!Double.isInfinite(x) && x != 0.0) {
         double facta = 0.01745329052209854;
         double factb = 1.997844754509471E-9;
         double xa = doubleHighPart(x);
         double xb = x - xa;
         double result = xb * 1.997844754509471E-9 + xb * 0.01745329052209854 + xa * 1.997844754509471E-9 + xa * 0.01745329052209854;
         if (result == 0.0) {
            result *= x;
         }

         return result;
      } else {
         return x;
      }
   }

   public static double toDegrees(double x) {
      if (!Double.isInfinite(x) && x != 0.0) {
         double facta = 57.2957763671875;
         double factb = 3.145894820876798E-6;
         double xa = doubleHighPart(x);
         double xb = x - xa;
         return xb * 3.145894820876798E-6 + xb * 57.2957763671875 + xa * 3.145894820876798E-6 + xa * 57.2957763671875;
      } else {
         return x;
      }
   }

   public static int abs(int x) {
      int i = x >>> 31;
      return (x ^ ~i + 1) + i;
   }

   public static long abs(long x) {
      long l = x >>> 63;
      return (x ^ ~l + 1L) + l;
   }

   public static float abs(float x) {
      return Float.intBitsToFloat(Integer.MAX_VALUE & Float.floatToRawIntBits(x));
   }

   public static double abs(double x) {
      return Double.longBitsToDouble(Long.MAX_VALUE & Double.doubleToRawLongBits(x));
   }

   public static double ulp(double x) {
      return Double.isInfinite(x) ? Double.POSITIVE_INFINITY : abs(x - Double.longBitsToDouble(Double.doubleToRawLongBits(x) ^ 1L));
   }

   public static float ulp(float x) {
      return Float.isInfinite(x) ? Float.POSITIVE_INFINITY : abs(x - Float.intBitsToFloat(Float.floatToIntBits(x) ^ 1));
   }

   public static double scalb(double d, int n) {
      if (n > -1023 && n < 1024) {
         return d * Double.longBitsToDouble((long)(n + 1023) << 52);
      } else if (!Double.isNaN(d) && !Double.isInfinite(d) && d != 0.0) {
         if (n < -2098) {
            return d > 0.0 ? 0.0 : -0.0;
         } else if (n > 2097) {
            return d > 0.0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
         } else {
            long bits = Double.doubleToRawLongBits(d);
            long sign = bits & Long.MIN_VALUE;
            int exponent = (int)(bits >>> 52) & 2047;
            long mantissa = bits & 4503599627370495L;
            int scaledExponent = exponent + n;
            if (n < 0) {
               if (scaledExponent > 0) {
                  return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
               } else if (scaledExponent > -53) {
                  mantissa |= 4503599627370496L;
                  long mostSignificantLostBit = mantissa & 1L << -scaledExponent;
                  mantissa >>>= 1 - scaledExponent;
                  if (mostSignificantLostBit != 0L) {
                     ++mantissa;
                  }

                  return Double.longBitsToDouble(sign | mantissa);
               } else {
                  return sign == 0L ? 0.0 : -0.0;
               }
            } else if (exponent != 0) {
               if (scaledExponent < 2047) {
                  return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
               } else {
                  return sign == 0L ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
               }
            } else {
               while(mantissa >>> 52 != 1L) {
                  mantissa <<= 1;
                  --scaledExponent;
               }

               ++scaledExponent;
               mantissa &= 4503599627370495L;
               if (scaledExponent < 2047) {
                  return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
               } else {
                  return sign == 0L ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
               }
            }
         }
      } else {
         return d;
      }
   }

   public static float scalb(float f, int n) {
      if (n > -127 && n < 128) {
         return f * Float.intBitsToFloat(n + 127 << 23);
      } else if (!Float.isNaN(f) && !Float.isInfinite(f) && f != 0.0F) {
         if (n < -277) {
            return f > 0.0F ? 0.0F : -0.0F;
         } else if (n > 276) {
            return f > 0.0F ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
         } else {
            int bits = Float.floatToIntBits(f);
            int sign = bits & Integer.MIN_VALUE;
            int exponent = bits >>> 23 & 255;
            int mantissa = bits & 8388607;
            int scaledExponent = exponent + n;
            if (n < 0) {
               if (scaledExponent > 0) {
                  return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
               } else if (scaledExponent > -24) {
                  mantissa |= 8388608;
                  int mostSignificantLostBit = mantissa & 1 << -scaledExponent;
                  mantissa >>>= 1 - scaledExponent;
                  if (mostSignificantLostBit != 0) {
                     ++mantissa;
                  }

                  return Float.intBitsToFloat(sign | mantissa);
               } else {
                  return sign == 0 ? 0.0F : -0.0F;
               }
            } else if (exponent != 0) {
               if (scaledExponent < 255) {
                  return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
               } else {
                  return sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
               }
            } else {
               while(mantissa >>> 23 != 1) {
                  mantissa <<= 1;
                  --scaledExponent;
               }

               ++scaledExponent;
               mantissa &= 8388607;
               if (scaledExponent < 255) {
                  return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
               } else {
                  return sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
               }
            }
         }
      } else {
         return f;
      }
   }

   public static double nextAfter(double d, double direction) {
      if (!Double.isNaN(d) && !Double.isNaN(direction)) {
         if (d == direction) {
            return direction;
         } else if (Double.isInfinite(d)) {
            return d < 0.0 ? -1.7976931348623157E308 : Double.MAX_VALUE;
         } else if (d == 0.0) {
            return direction < 0.0 ? -4.9E-324 : Double.MIN_VALUE;
         } else {
            long bits = Double.doubleToRawLongBits(d);
            long sign = bits & Long.MIN_VALUE;
            return direction < d ^ sign == 0L ? Double.longBitsToDouble(sign | (bits & Long.MAX_VALUE) + 1L) : Double.longBitsToDouble(sign | (bits & Long.MAX_VALUE) - 1L);
         }
      } else {
         return Double.NaN;
      }
   }

   public static float nextAfter(float f, double direction) {
      if (!Double.isNaN((double)f) && !Double.isNaN(direction)) {
         if ((double)f == direction) {
            return (float)direction;
         } else if (Float.isInfinite(f)) {
            return f < 0.0F ? -3.4028235E38F : Float.MAX_VALUE;
         } else if (f == 0.0F) {
            return direction < 0.0 ? -1.4E-45F : Float.MIN_VALUE;
         } else {
            int bits = Float.floatToIntBits(f);
            int sign = bits & Integer.MIN_VALUE;
            return direction < (double)f ^ sign == 0 ? Float.intBitsToFloat(sign | (bits & Integer.MAX_VALUE) + 1) : Float.intBitsToFloat(sign | (bits & Integer.MAX_VALUE) - 1);
         }
      } else {
         return Float.NaN;
      }
   }

   public static double floor(double x) {
      if (x != x) {
         return x;
      } else if (!(x >= 4.503599627370496E15) && !(x <= -4.503599627370496E15)) {
         long y = (long)x;
         if (x < 0.0 && (double)y != x) {
            --y;
         }

         return y == 0L ? x * (double)y : (double)y;
      } else {
         return x;
      }
   }

   public static double ceil(double x) {
      if (x != x) {
         return x;
      } else {
         double y = floor(x);
         if (y == x) {
            return y;
         } else {
            ++y;
            return y == 0.0 ? x * y : y;
         }
      }
   }

   public static double rint(double x) {
      double y = floor(x);
      double d = x - y;
      if (d > 0.5) {
         return y == -1.0 ? -0.0 : y + 1.0;
      } else if (d < 0.5) {
         return y;
      } else {
         long z = (long)y;
         return (z & 1L) == 0L ? y : y + 1.0;
      }
   }

   public static long round(double x) {
      return (long)floor(x + 0.5);
   }

   public static int round(float x) {
      return (int)floor((double)(x + 0.5F));
   }

   public static int min(int a, int b) {
      return a <= b ? a : b;
   }

   public static long min(long a, long b) {
      return a <= b ? a : b;
   }

   public static float min(float a, float b) {
      if (a > b) {
         return b;
      } else if (a < b) {
         return a;
      } else if (a != b) {
         return Float.NaN;
      } else {
         int bits = Float.floatToRawIntBits(a);
         return bits == Integer.MIN_VALUE ? a : b;
      }
   }

   public static double min(double a, double b) {
      if (a > b) {
         return b;
      } else if (a < b) {
         return a;
      } else if (a != b) {
         return Double.NaN;
      } else {
         long bits = Double.doubleToRawLongBits(a);
         return bits == Long.MIN_VALUE ? a : b;
      }
   }

   public static int max(int a, int b) {
      return a <= b ? b : a;
   }

   public static long max(long a, long b) {
      return a <= b ? b : a;
   }

   public static float max(float a, float b) {
      if (a > b) {
         return a;
      } else if (a < b) {
         return b;
      } else if (a != b) {
         return Float.NaN;
      } else {
         int bits = Float.floatToRawIntBits(a);
         return bits == Integer.MIN_VALUE ? b : a;
      }
   }

   public static double max(double a, double b) {
      if (a > b) {
         return a;
      } else if (a < b) {
         return b;
      } else if (a != b) {
         return Double.NaN;
      } else {
         long bits = Double.doubleToRawLongBits(a);
         return bits == Long.MIN_VALUE ? b : a;
      }
   }

   public static double hypot(double x, double y) {
      if (!Double.isInfinite(x) && !Double.isInfinite(y)) {
         if (!Double.isNaN(x) && !Double.isNaN(y)) {
            int expX = getExponent(x);
            int expY = getExponent(y);
            if (expX > expY + 27) {
               return abs(x);
            } else if (expY > expX + 27) {
               return abs(y);
            } else {
               int middleExp = (expX + expY) / 2;
               double scaledX = scalb(x, -middleExp);
               double scaledY = scalb(y, -middleExp);
               double scaledH = sqrt(scaledX * scaledX + scaledY * scaledY);
               return scalb(scaledH, middleExp);
            }
         } else {
            return Double.NaN;
         }
      } else {
         return Double.POSITIVE_INFINITY;
      }
   }

   public static double IEEEremainder(double dividend, double divisor) {
      return StrictMath.IEEEremainder(dividend, divisor);
   }

   public static int toIntExact(long n) throws MathArithmeticException {
      if (n >= -2147483648L && n <= 2147483647L) {
         return (int)n;
      } else {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW, new Object[0]);
      }
   }

   public static int incrementExact(int n) throws MathArithmeticException {
      if (n == Integer.MAX_VALUE) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{n, 1});
      } else {
         return n + 1;
      }
   }

   public static long incrementExact(long n) throws MathArithmeticException {
      if (n == Long.MAX_VALUE) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{n, 1});
      } else {
         return n + 1L;
      }
   }

   public static int decrementExact(int n) throws MathArithmeticException {
      if (n == Integer.MIN_VALUE) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, new Object[]{n, 1});
      } else {
         return n - 1;
      }
   }

   public static long decrementExact(long n) throws MathArithmeticException {
      if (n == Long.MIN_VALUE) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, new Object[]{n, 1});
      } else {
         return n - 1L;
      }
   }

   public static int addExact(int a, int b) throws MathArithmeticException {
      int sum = a + b;
      if ((a ^ b) >= 0 && (sum ^ b) < 0) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{a, b});
      } else {
         return sum;
      }
   }

   public static long addExact(long a, long b) throws MathArithmeticException {
      long sum = a + b;
      if ((a ^ b) >= 0L && (sum ^ b) < 0L) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, new Object[]{a, b});
      } else {
         return sum;
      }
   }

   public static int subtractExact(int a, int b) {
      int sub = a - b;
      if ((a ^ b) < 0 && (sub ^ b) >= 0) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, new Object[]{a, b});
      } else {
         return sub;
      }
   }

   public static long subtractExact(long a, long b) {
      long sub = a - b;
      if ((a ^ b) < 0L && (sub ^ b) >= 0L) {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_SUBTRACTION, new Object[]{a, b});
      } else {
         return sub;
      }
   }

   public static int multiplyExact(int a, int b) {
      if ((b <= 0 || a <= Integer.MAX_VALUE / b && a >= Integer.MIN_VALUE / b) && (b >= -1 || a <= Integer.MIN_VALUE / b && a >= Integer.MAX_VALUE / b) && (b != -1 || a != Integer.MIN_VALUE)) {
         return a * b;
      } else {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_MULTIPLICATION, new Object[]{a, b});
      }
   }

   public static long multiplyExact(long a, long b) {
      if ((b <= 0L || a <= Long.MAX_VALUE / b && a >= Long.MIN_VALUE / b) && (b >= -1L || a <= Long.MIN_VALUE / b && a >= Long.MAX_VALUE / b) && (b != -1L || a != Long.MIN_VALUE)) {
         return a * b;
      } else {
         throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_MULTIPLICATION, new Object[]{a, b});
      }
   }

   public static int floorDiv(int a, int b) throws MathArithmeticException {
      if (b == 0) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         int m = a % b;
         return (a ^ b) < 0 && m != 0 ? a / b - 1 : a / b;
      }
   }

   public static long floorDiv(long a, long b) throws MathArithmeticException {
      if (b == 0L) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         long m = a % b;
         return (a ^ b) < 0L && m != 0L ? a / b - 1L : a / b;
      }
   }

   public static int floorMod(int a, int b) throws MathArithmeticException {
      if (b == 0) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         int m = a % b;
         return (a ^ b) < 0 && m != 0 ? b + m : m;
      }
   }

   public static long floorMod(long a, long b) {
      if (b == 0L) {
         throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR, new Object[0]);
      } else {
         long m = a % b;
         return (a ^ b) < 0L && m != 0L ? b + m : m;
      }
   }

   public static double copySign(double magnitude, double sign) {
      long m = Double.doubleToRawLongBits(magnitude);
      long s = Double.doubleToRawLongBits(sign);
      return (m ^ s) >= 0L ? magnitude : -magnitude;
   }

   public static float copySign(float magnitude, float sign) {
      int m = Float.floatToRawIntBits(magnitude);
      int s = Float.floatToRawIntBits(sign);
      return (m ^ s) >= 0 ? magnitude : -magnitude;
   }

   public static int getExponent(double d) {
      return (int)(Double.doubleToRawLongBits(d) >>> 52 & 2047L) - 1023;
   }

   public static int getExponent(float f) {
      return (Float.floatToRawIntBits(f) >>> 23 & 255) - 127;
   }

   public static void main(String[] a) {
      PrintStream out = System.out;
      FastMathCalc.printarray(out, "EXP_INT_TABLE_A", 1500, (double[])FastMath.ExpIntTable.EXP_INT_TABLE_A);
      FastMathCalc.printarray(out, "EXP_INT_TABLE_B", 1500, (double[])FastMath.ExpIntTable.EXP_INT_TABLE_B);
      FastMathCalc.printarray(out, "EXP_FRAC_TABLE_A", 1025, (double[])FastMath.ExpFracTable.EXP_FRAC_TABLE_A);
      FastMathCalc.printarray(out, "EXP_FRAC_TABLE_B", 1025, (double[])FastMath.ExpFracTable.EXP_FRAC_TABLE_B);
      FastMathCalc.printarray(out, "LN_MANT", 1024, (double[][])FastMath.lnMant.LN_MANT);
      FastMathCalc.printarray(out, "SINE_TABLE_A", 14, (double[])SINE_TABLE_A);
      FastMathCalc.printarray(out, "SINE_TABLE_B", 14, (double[])SINE_TABLE_B);
      FastMathCalc.printarray(out, "COSINE_TABLE_A", 14, (double[])COSINE_TABLE_A);
      FastMathCalc.printarray(out, "COSINE_TABLE_B", 14, (double[])COSINE_TABLE_B);
      FastMathCalc.printarray(out, "TANGENT_TABLE_A", 14, (double[])TANGENT_TABLE_A);
      FastMathCalc.printarray(out, "TANGENT_TABLE_B", 14, (double[])TANGENT_TABLE_B);
   }

   private static class CodyWaite {
      private final int finalK;
      private final double finalRemA;
      private final double finalRemB;

      CodyWaite(double xa) {
         int k = (int)(xa * 0.6366197723675814);

         while(true) {
            double a = (double)(-k) * 1.570796251296997;
            double remA = xa + a;
            double remB = -(remA - xa - a);
            a = (double)(-k) * 7.549789948768648E-8;
            double b = remA;
            remA += a;
            remB += -(remA - b - a);
            a = (double)(-k) * 6.123233995736766E-17;
            b = remA;
            remA += a;
            remB += -(remA - b - a);
            if (remA > 0.0) {
               this.finalK = k;
               this.finalRemA = remA;
               this.finalRemB = remB;
               return;
            }

            --k;
         }
      }

      int getK() {
         return this.finalK;
      }

      double getRemA() {
         return this.finalRemA;
      }

      double getRemB() {
         return this.finalRemB;
      }
   }

   private static class lnMant {
      private static final double[][] LN_MANT = FastMathLiteralArrays.loadLnMant();
   }

   private static class ExpFracTable {
      private static final double[] EXP_FRAC_TABLE_A = FastMathLiteralArrays.loadExpFracA();
      private static final double[] EXP_FRAC_TABLE_B = FastMathLiteralArrays.loadExpFracB();
   }

   private static class ExpIntTable {
      private static final double[] EXP_INT_TABLE_A = FastMathLiteralArrays.loadExpIntA();
      private static final double[] EXP_INT_TABLE_B = FastMathLiteralArrays.loadExpIntB();
   }

   private static class Split {
      public static final Split NAN = new Split(Double.NaN, 0.0);
      public static final Split POSITIVE_INFINITY = new Split(Double.POSITIVE_INFINITY, 0.0);
      public static final Split NEGATIVE_INFINITY = new Split(Double.NEGATIVE_INFINITY, 0.0);
      private final double full;
      private final double high;
      private final double low;

      Split(double x) {
         this.full = x;
         this.high = Double.longBitsToDouble(Double.doubleToRawLongBits(x) & -134217728L);
         this.low = x - this.high;
      }

      Split(double high, double low) {
         this(high == 0.0 ? (low == 0.0 && Double.doubleToRawLongBits(high) == Long.MIN_VALUE ? -0.0 : low) : high + low, high, low);
      }

      Split(double full, double high, double low) {
         this.full = full;
         this.high = high;
         this.low = low;
      }

      public Split multiply(Split b) {
         Split mulBasic = new Split(this.full * b.full);
         double mulError = this.low * b.low - (mulBasic.full - this.high * b.high - this.low * b.high - this.high * b.low);
         return new Split(mulBasic.high, mulBasic.low + mulError);
      }

      public Split reciprocal() {
         double approximateInv = 1.0 / this.full;
         Split splitInv = new Split(approximateInv);
         Split product = this.multiply(splitInv);
         double error = product.high - 1.0 + product.low;
         return Double.isNaN(error) ? splitInv : new Split(splitInv.high, splitInv.low - error / this.full);
      }

      private Split pow(long e) {
         Split result = new Split(1.0);
         Split d2p = new Split(this.full, this.high, this.low);

         for(long p = e; p != 0L; p >>>= 1) {
            if ((p & 1L) != 0L) {
               result = result.multiply(d2p);
            }

            d2p = d2p.multiply(d2p);
         }

         if (Double.isNaN(result.full)) {
            if (Double.isNaN(this.full)) {
               return NAN;
            } else if (FastMath.abs(this.full) < 1.0) {
               return new Split(FastMath.copySign(0.0, this.full), 0.0);
            } else if (this.full < 0.0 && (e & 1L) == 1L) {
               return NEGATIVE_INFINITY;
            } else {
               return POSITIVE_INFINITY;
            }
         } else {
            return result;
         }
      }
   }
}
