package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooSmallException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;
import moze_intel.projecte.shaded.org.apache.commons.math3.special.Beta;
import moze_intel.projecte.shaded.org.apache.commons.math3.special.Gamma;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.Precision;

public class BetaDistribution extends AbstractRealDistribution {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = -1221965979403477668L;
   private final double alpha;
   private final double beta;
   private double z;
   private final double solverAbsoluteAccuracy;

   public BetaDistribution(double alpha, double beta) {
      this(alpha, beta, 1.0E-9);
   }

   public BetaDistribution(double alpha, double beta, double inverseCumAccuracy) {
      this(new Well19937c(), alpha, beta, inverseCumAccuracy);
   }

   public BetaDistribution(RandomGenerator rng, double alpha, double beta) {
      this(rng, alpha, beta, 1.0E-9);
   }

   public BetaDistribution(RandomGenerator rng, double alpha, double beta, double inverseCumAccuracy) {
      super(rng);
      this.alpha = alpha;
      this.beta = beta;
      this.z = Double.NaN;
      this.solverAbsoluteAccuracy = inverseCumAccuracy;
   }

   public double getAlpha() {
      return this.alpha;
   }

   public double getBeta() {
      return this.beta;
   }

   private void recomputeZ() {
      if (Double.isNaN(this.z)) {
         this.z = Gamma.logGamma(this.alpha) + Gamma.logGamma(this.beta) - Gamma.logGamma(this.alpha + this.beta);
      }

   }

   public double density(double x) {
      double logDensity = this.logDensity(x);
      return logDensity == Double.NEGATIVE_INFINITY ? 0.0 : FastMath.exp(logDensity);
   }

   public double logDensity(double x) {
      this.recomputeZ();
      if (!(x < 0.0) && !(x > 1.0)) {
         if (x == 0.0) {
            if (this.alpha < 1.0) {
               throw new NumberIsTooSmallException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_0_FOR_SOME_ALPHA, this.alpha, 1, false);
            } else {
               return Double.NEGATIVE_INFINITY;
            }
         } else if (x == 1.0) {
            if (this.beta < 1.0) {
               throw new NumberIsTooSmallException(LocalizedFormats.CANNOT_COMPUTE_BETA_DENSITY_AT_1_FOR_SOME_BETA, this.beta, 1, false);
            } else {
               return Double.NEGATIVE_INFINITY;
            }
         } else {
            double logX = FastMath.log(x);
            double log1mX = FastMath.log1p(-x);
            return (this.alpha - 1.0) * logX + (this.beta - 1.0) * log1mX - this.z;
         }
      } else {
         return Double.NEGATIVE_INFINITY;
      }
   }

   public double cumulativeProbability(double x) {
      if (x <= 0.0) {
         return 0.0;
      } else {
         return x >= 1.0 ? 1.0 : Beta.regularizedBeta(x, this.alpha, this.beta);
      }
   }

   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public double getNumericalMean() {
      double a = this.getAlpha();
      return a / (a + this.getBeta());
   }

   public double getNumericalVariance() {
      double a = this.getAlpha();
      double b = this.getBeta();
      double alphabetasum = a + b;
      return a * b / (alphabetasum * alphabetasum * (alphabetasum + 1.0));
   }

   public double getSupportLowerBound() {
      return 0.0;
   }

   public double getSupportUpperBound() {
      return 1.0;
   }

   public boolean isSupportLowerBoundInclusive() {
      return false;
   }

   public boolean isSupportUpperBoundInclusive() {
      return false;
   }

   public boolean isSupportConnected() {
      return true;
   }

   public double sample() {
      return BetaDistribution.ChengBetaSampler.sample(this.random, this.alpha, this.beta);
   }

   private static final class ChengBetaSampler {
      static double sample(RandomGenerator random, double alpha, double beta) {
         double a = FastMath.min(alpha, beta);
         double b = FastMath.max(alpha, beta);
         return a > 1.0 ? algorithmBB(random, alpha, a, b) : algorithmBC(random, alpha, b, a);
      }

      private static double algorithmBB(RandomGenerator random, double a0, double a, double b) {
         double alpha = a + b;
         double beta = FastMath.sqrt((alpha - 2.0) / (2.0 * a * b - alpha));
         double gamma = a + 1.0 / beta;

         double r;
         double w;
         double t;
         double s;
         do {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
            w = a * FastMath.exp(v);
            double z = u1 * u1 * u2;
            r = gamma * v - 1.3862944;
            s = a + r - w;
            if (s + 2.609438 >= 5.0 * z) {
               break;
            }

            t = FastMath.log(z);
         } while(!(s >= t) && r + alpha * (FastMath.log(alpha) - FastMath.log(b + w)) < t);

         w = FastMath.min(w, Double.MAX_VALUE);
         return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
      }

      private static double algorithmBC(RandomGenerator random, double a0, double a, double b) {
         double alpha = a + b;
         double beta = 1.0 / b;
         double delta = 1.0 + a - b;
         double k1 = delta * (0.0138889 + 0.0416667 * b) / (a * beta - 0.777778);
         double k2 = 0.25 + (0.5 + 0.25 / delta) * b;

         double w;
         while(true) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double y = u1 * u2;
            double z = u1 * y;
            double v;
            if (u1 < 0.5) {
               if (0.25 * u2 + z - y >= k1) {
                  continue;
               }
            } else {
               if (z <= 0.25) {
                  v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
                  w = a * FastMath.exp(v);
                  break;
               }

               if (z >= k2) {
                  continue;
               }
            }

            v = beta * (FastMath.log(u1) - FastMath.log1p(-u1));
            w = a * FastMath.exp(v);
            if (alpha * (FastMath.log(alpha) - FastMath.log(b + w) + v) - 1.3862944 >= FastMath.log(z)) {
               break;
            }
         }

         w = FastMath.min(w, Double.MAX_VALUE);
         return Precision.equals(a, a0) ? w / (b + w) : b / (b + w);
      }
   }
}
