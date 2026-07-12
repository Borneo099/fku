package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;
import moze_intel.projecte.shaded.org.apache.commons.math3.special.Gamma;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.CombinatoricsUtils;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class PoissonDistribution extends AbstractIntegerDistribution {
   public static final int DEFAULT_MAX_ITERATIONS = 10000000;
   public static final double DEFAULT_EPSILON = 1.0E-12;
   private static final long serialVersionUID = -3349935121172596109L;
   private final NormalDistribution normal;
   private final ExponentialDistribution exponential;
   private final double mean;
   private final int maxIterations;
   private final double epsilon;

   public PoissonDistribution(double p) throws NotStrictlyPositiveException {
      this(p, 1.0E-12, 10000000);
   }

   public PoissonDistribution(double p, double epsilon, int maxIterations) throws NotStrictlyPositiveException {
      this(new Well19937c(), p, epsilon, maxIterations);
   }

   public PoissonDistribution(RandomGenerator rng, double p, double epsilon, int maxIterations) throws NotStrictlyPositiveException {
      super(rng);
      if (p <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, p);
      } else {
         this.mean = p;
         this.epsilon = epsilon;
         this.maxIterations = maxIterations;
         this.normal = new NormalDistribution(rng, p, FastMath.sqrt(p), 1.0E-9);
         this.exponential = new ExponentialDistribution(rng, 1.0, 1.0E-9);
      }
   }

   public PoissonDistribution(double p, double epsilon) throws NotStrictlyPositiveException {
      this(p, epsilon, 10000000);
   }

   public PoissonDistribution(double p, int maxIterations) {
      this(p, 1.0E-12, maxIterations);
   }

   public double getMean() {
      return this.mean;
   }

   public double probability(int x) {
      double logProbability = this.logProbability(x);
      return logProbability == Double.NEGATIVE_INFINITY ? 0.0 : FastMath.exp(logProbability);
   }

   public double logProbability(int x) {
      double ret;
      if (x >= 0 && x != Integer.MAX_VALUE) {
         if (x == 0) {
            ret = -this.mean;
         } else {
            ret = -SaddlePointExpansion.getStirlingError((double)x) - SaddlePointExpansion.getDeviancePart((double)x, this.mean) - 0.5 * FastMath.log(6.283185307179586) - 0.5 * FastMath.log((double)x);
         }
      } else {
         ret = Double.NEGATIVE_INFINITY;
      }

      return ret;
   }

   public double cumulativeProbability(int x) {
      if (x < 0) {
         return 0.0;
      } else {
         return x == Integer.MAX_VALUE ? 1.0 : Gamma.regularizedGammaQ((double)x + 1.0, this.mean, this.epsilon, this.maxIterations);
      }
   }

   public double normalApproximateProbability(int x) {
      return this.normal.cumulativeProbability((double)x + 0.5);
   }

   public double getNumericalMean() {
      return this.getMean();
   }

   public double getNumericalVariance() {
      return this.getMean();
   }

   public int getSupportLowerBound() {
      return 0;
   }

   public int getSupportUpperBound() {
      return Integer.MAX_VALUE;
   }

   public boolean isSupportConnected() {
      return true;
   }

   public int sample() {
      return (int)FastMath.min(this.nextPoisson(this.mean), 2147483647L);
   }

   private long nextPoisson(double meanPoisson) {
      double pivot = 40.0;
      double lambda;
      double logLambda;
      double logLambdaFactorial;
      if (meanPoisson < 40.0) {
         lambda = FastMath.exp(-meanPoisson);
         long n = 0L;
         logLambda = 1.0;

         for(logLambdaFactorial = 1.0; (double)n < 1000.0 * meanPoisson; ++n) {
            logLambdaFactorial = this.random.nextDouble();
            logLambda *= logLambdaFactorial;
            if (!(logLambda >= lambda)) {
               return n;
            }
         }

         return n;
      } else {
         lambda = FastMath.floor(meanPoisson);
         double lambdaFractional = meanPoisson - lambda;
         logLambda = FastMath.log(lambda);
         logLambdaFactorial = CombinatoricsUtils.factorialLog((int)lambda);
         long y2 = lambdaFractional < Double.MIN_VALUE ? 0L : this.nextPoisson(lambdaFractional);
         double delta = FastMath.sqrt(lambda * FastMath.log(32.0 * lambda / Math.PI + 1.0));
         double halfDelta = delta / 2.0;
         double twolpd = 2.0 * lambda + delta;
         double a1 = FastMath.sqrt(Math.PI * twolpd) * FastMath.exp(1.0 / (8.0 * lambda));
         double a2 = twolpd / delta * FastMath.exp(-delta * (1.0 + delta) / twolpd);
         double aSum = a1 + a2 + 1.0;
         double p1 = a1 / aSum;
         double p2 = a2 / aSum;
         double c1 = 1.0 / (8.0 * lambda);
         double x = 0.0;
         double y = 0.0;
         double v = 0.0;
         int a = false;
         double t = 0.0;
         double qr = 0.0;
         double qa = 0.0;

         while(true) {
            double u = this.random.nextDouble();
            if (u <= p1) {
               double n = this.random.nextGaussian();
               x = n * FastMath.sqrt(lambda + halfDelta) - 0.5;
               if (x > delta || x < -lambda) {
                  continue;
               }

               y = x < 0.0 ? FastMath.floor(x) : FastMath.ceil(x);
               double e = this.exponential.sample();
               v = -e - n * n / 2.0 + c1;
            } else {
               if (u > p1 + p2) {
                  y = lambda;
                  break;
               }

               x = delta + twolpd / delta * this.exponential.sample();
               y = FastMath.ceil(x);
               v = -this.exponential.sample() - delta * (x + 1.0) / twolpd;
            }

            int a = x < 0.0 ? 1 : 0;
            t = y * (y + 1.0) / (2.0 * lambda);
            if (v < -t && a == 0) {
               y += lambda;
               break;
            }

            qr = t * ((2.0 * y + 1.0) / (6.0 * lambda) - 1.0);
            qa = qr - t * t / (3.0 * (lambda + (double)a * (y + 1.0)));
            if (v < qa) {
               y += lambda;
               break;
            }

            if (!(v > qr) && v < y * logLambda - CombinatoricsUtils.factorialLog((int)(y + lambda)) + logLambdaFactorial) {
               y += lambda;
               break;
            }
         }

         return y2 + (long)y;
      }
   }
}
