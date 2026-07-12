package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import java.io.Serializable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathInternalError;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomDataImpl;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public abstract class AbstractIntegerDistribution implements IntegerDistribution, Serializable {
   private static final long serialVersionUID = -1146319659338487221L;
   /** @deprecated */
   @Deprecated
   protected final RandomDataImpl randomData = new RandomDataImpl();
   protected final RandomGenerator random;

   /** @deprecated */
   @Deprecated
   protected AbstractIntegerDistribution() {
      this.random = null;
   }

   protected AbstractIntegerDistribution(RandomGenerator rng) {
      this.random = rng;
   }

   public double cumulativeProbability(int x0, int x1) throws NumberIsTooLargeException {
      if (x1 < x0) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1, true);
      } else {
         return this.cumulativeProbability(x1) - this.cumulativeProbability(x0);
      }
   }

   public int inverseCumulativeProbability(double p) throws OutOfRangeException {
      if (!(p < 0.0) && !(p > 1.0)) {
         int lower = this.getSupportLowerBound();
         if (p == 0.0) {
            return lower;
         } else {
            if (lower == Integer.MIN_VALUE) {
               if (this.checkedCumulativeProbability(lower) >= p) {
                  return lower;
               }
            } else {
               --lower;
            }

            int upper = this.getSupportUpperBound();
            if (p == 1.0) {
               return upper;
            } else {
               double mu = this.getNumericalMean();
               double sigma = FastMath.sqrt(this.getNumericalVariance());
               boolean chebyshevApplies = !Double.isInfinite(mu) && !Double.isNaN(mu) && !Double.isInfinite(sigma) && !Double.isNaN(sigma) && sigma != 0.0;
               if (chebyshevApplies) {
                  double k = FastMath.sqrt((1.0 - p) / p);
                  double tmp = mu - k * sigma;
                  if (tmp > (double)lower) {
                     lower = (int)FastMath.ceil(tmp) - 1;
                  }

                  k = 1.0 / k;
                  tmp = mu + k * sigma;
                  if (tmp < (double)upper) {
                     upper = (int)FastMath.ceil(tmp) - 1;
                  }
               }

               return this.solveInverseCumulativeProbability(p, lower, upper);
            }
         }
      } else {
         throw new OutOfRangeException(p, 0, 1);
      }
   }

   protected int solveInverseCumulativeProbability(double p, int lower, int upper) {
      while(lower + 1 < upper) {
         int xm = (lower + upper) / 2;
         if (xm < lower || xm > upper) {
            xm = lower + (upper - lower) / 2;
         }

         double pm = this.checkedCumulativeProbability(xm);
         if (pm >= p) {
            upper = xm;
         } else {
            lower = xm;
         }
      }

      return upper;
   }

   public void reseedRandomGenerator(long seed) {
      this.random.setSeed(seed);
      this.randomData.reSeed(seed);
   }

   public int sample() {
      return this.inverseCumulativeProbability(this.random.nextDouble());
   }

   public int[] sample(int sampleSize) {
      if (sampleSize <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
      } else {
         int[] out = new int[sampleSize];

         for(int i = 0; i < sampleSize; ++i) {
            out[i] = this.sample();
         }

         return out;
      }
   }

   private double checkedCumulativeProbability(int argument) throws MathInternalError {
      double result = Double.NaN;
      result = this.cumulativeProbability(argument);
      if (Double.isNaN(result)) {
         throw new MathInternalError(LocalizedFormats.DISCRETE_CUMULATIVE_PROBABILITY_RETURNED_NAN, new Object[]{argument});
      } else {
         return result;
      }
   }

   public double logProbability(int x) {
      return FastMath.log(this.probability(x));
   }
}
