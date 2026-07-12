package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;

public class UniformIntegerDistribution extends AbstractIntegerDistribution {
   private static final long serialVersionUID = 20120109L;
   private final int lower;
   private final int upper;

   public UniformIntegerDistribution(int lower, int upper) throws NumberIsTooLargeException {
      this(new Well19937c(), lower, upper);
   }

   public UniformIntegerDistribution(RandomGenerator rng, int lower, int upper) throws NumberIsTooLargeException {
      super(rng);
      if (lower > upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, true);
      } else {
         this.lower = lower;
         this.upper = upper;
      }
   }

   public double probability(int x) {
      return x >= this.lower && x <= this.upper ? 1.0 / (double)(this.upper - this.lower + 1) : 0.0;
   }

   public double cumulativeProbability(int x) {
      if (x < this.lower) {
         return 0.0;
      } else {
         return x > this.upper ? 1.0 : ((double)(x - this.lower) + 1.0) / ((double)(this.upper - this.lower) + 1.0);
      }
   }

   public double getNumericalMean() {
      return 0.5 * (double)(this.lower + this.upper);
   }

   public double getNumericalVariance() {
      double n = (double)(this.upper - this.lower + 1);
      return (n * n - 1.0) / 12.0;
   }

   public int getSupportLowerBound() {
      return this.lower;
   }

   public int getSupportUpperBound() {
      return this.upper;
   }

   public boolean isSupportConnected() {
      return true;
   }

   public int sample() {
      int max = this.upper - this.lower + 1;
      if (max > 0) {
         return this.lower + this.random.nextInt(max);
      } else {
         int r;
         do {
            do {
               r = this.random.nextInt();
            } while(r < this.lower);
         } while(r > this.upper);

         return r;
      }
   }
}
