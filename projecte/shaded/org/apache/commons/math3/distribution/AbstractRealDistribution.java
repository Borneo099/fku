package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import java.io.Serializable;
import moze_intel.projecte.shaded.org.apache.commons.math3.analysis.UnivariateFunction;
import moze_intel.projecte.shaded.org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomDataImpl;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public abstract class AbstractRealDistribution implements RealDistribution, Serializable {
   public static final double SOLVER_DEFAULT_ABSOLUTE_ACCURACY = 1.0E-6;
   private static final long serialVersionUID = -38038050983108802L;
   /** @deprecated */
   @Deprecated
   protected RandomDataImpl randomData = new RandomDataImpl();
   protected final RandomGenerator random;
   private double solverAbsoluteAccuracy = 1.0E-6;

   /** @deprecated */
   @Deprecated
   protected AbstractRealDistribution() {
      this.random = null;
   }

   protected AbstractRealDistribution(RandomGenerator rng) {
      this.random = rng;
   }

   /** @deprecated */
   @Deprecated
   public double cumulativeProbability(double x0, double x1) throws NumberIsTooLargeException {
      return this.probability(x0, x1);
   }

   public double probability(double x0, double x1) {
      if (x0 > x1) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1, true);
      } else {
         return this.cumulativeProbability(x1) - this.cumulativeProbability(x0);
      }
   }

   public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
      if (!(p < 0.0) && !(p > 1.0)) {
         double lowerBound = this.getSupportLowerBound();
         if (p == 0.0) {
            return lowerBound;
         } else {
            double upperBound = this.getSupportUpperBound();
            if (p == 1.0) {
               return upperBound;
            } else {
               double mu = this.getNumericalMean();
               double sig = FastMath.sqrt(this.getNumericalVariance());
               boolean chebyshevApplies = !Double.isInfinite(mu) && !Double.isNaN(mu) && !Double.isInfinite(sig) && !Double.isNaN(sig);
               if (lowerBound == Double.NEGATIVE_INFINITY) {
                  if (chebyshevApplies) {
                     lowerBound = mu - sig * FastMath.sqrt((1.0 - p) / p);
                  } else {
                     for(lowerBound = -1.0; this.cumulativeProbability(lowerBound) >= p; lowerBound *= 2.0) {
                     }
                  }
               }

               if (upperBound == Double.POSITIVE_INFINITY) {
                  if (chebyshevApplies) {
                     upperBound = mu + sig * FastMath.sqrt(p / (1.0 - p));
                  } else {
                     for(upperBound = 1.0; this.cumulativeProbability(upperBound) < p; upperBound *= 2.0) {
                     }
                  }
               }

               UnivariateFunction toSolve = new UnivariateFunction() {
                  public double value(double x) {
                     return AbstractRealDistribution.this.cumulativeProbability(x) - p;
                  }
               };
               double x = UnivariateSolverUtils.solve(toSolve, lowerBound, upperBound, this.getSolverAbsoluteAccuracy());
               if (!this.isSupportConnected()) {
                  double dx = this.getSolverAbsoluteAccuracy();
                  if (x - dx >= this.getSupportLowerBound()) {
                     double px = this.cumulativeProbability(x);
                     if (this.cumulativeProbability(x - dx) == px) {
                        upperBound = x;

                        while(upperBound - lowerBound > dx) {
                           double midPoint = 0.5 * (lowerBound + upperBound);
                           if (this.cumulativeProbability(midPoint) < px) {
                              lowerBound = midPoint;
                           } else {
                              upperBound = midPoint;
                           }
                        }

                        return upperBound;
                     }
                  }
               }

               return x;
            }
         }
      } else {
         throw new OutOfRangeException(p, 0, 1);
      }
   }

   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public void reseedRandomGenerator(long seed) {
      this.random.setSeed(seed);
      this.randomData.reSeed(seed);
   }

   public double sample() {
      return this.inverseCumulativeProbability(this.random.nextDouble());
   }

   public double[] sample(int sampleSize) {
      if (sampleSize <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
      } else {
         double[] out = new double[sampleSize];

         for(int i = 0; i < sampleSize; ++i) {
            out[i] = this.sample();
         }

         return out;
      }
   }

   public double probability(double x) {
      return 0.0;
   }

   public double logDensity(double x) {
      return FastMath.log(this.density(x));
   }
}
