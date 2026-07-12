package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;
import moze_intel.projecte.shaded.org.apache.commons.math3.special.Beta;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class BinomialDistribution extends AbstractIntegerDistribution {
   private static final long serialVersionUID = 6751309484392813623L;
   private final int numberOfTrials;
   private final double probabilityOfSuccess;

   public BinomialDistribution(int trials, double p) {
      this(new Well19937c(), trials, p);
   }

   public BinomialDistribution(RandomGenerator rng, int trials, double p) {
      super(rng);
      if (trials < 0) {
         throw new NotPositiveException(LocalizedFormats.NUMBER_OF_TRIALS, trials);
      } else if (!(p < 0.0) && !(p > 1.0)) {
         this.probabilityOfSuccess = p;
         this.numberOfTrials = trials;
      } else {
         throw new OutOfRangeException(p, 0, 1);
      }
   }

   public int getNumberOfTrials() {
      return this.numberOfTrials;
   }

   public double getProbabilityOfSuccess() {
      return this.probabilityOfSuccess;
   }

   public double probability(int x) {
      double logProbability = this.logProbability(x);
      return logProbability == Double.NEGATIVE_INFINITY ? 0.0 : FastMath.exp(logProbability);
   }

   public double logProbability(int x) {
      if (this.numberOfTrials == 0) {
         return x == 0 ? 0.0 : Double.NEGATIVE_INFINITY;
      } else {
         double ret;
         if (x >= 0 && x <= this.numberOfTrials) {
            ret = SaddlePointExpansion.logBinomialProbability(x, this.numberOfTrials, this.probabilityOfSuccess, 1.0 - this.probabilityOfSuccess);
         } else {
            ret = Double.NEGATIVE_INFINITY;
         }

         return ret;
      }
   }

   public double cumulativeProbability(int x) {
      double ret;
      if (x < 0) {
         ret = 0.0;
      } else if (x >= this.numberOfTrials) {
         ret = 1.0;
      } else {
         ret = 1.0 - Beta.regularizedBeta(this.probabilityOfSuccess, (double)x + 1.0, (double)(this.numberOfTrials - x));
      }

      return ret;
   }

   public double getNumericalMean() {
      return (double)this.numberOfTrials * this.probabilityOfSuccess;
   }

   public double getNumericalVariance() {
      double p = this.probabilityOfSuccess;
      return (double)this.numberOfTrials * p * (1.0 - p);
   }

   public int getSupportLowerBound() {
      return this.probabilityOfSuccess < 1.0 ? 0 : this.numberOfTrials;
   }

   public int getSupportUpperBound() {
      return this.probabilityOfSuccess > 0.0 ? this.numberOfTrials : 0;
   }

   public boolean isSupportConnected() {
      return true;
   }
}
