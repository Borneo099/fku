package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class HypergeometricDistribution extends AbstractIntegerDistribution {
   private static final long serialVersionUID = -436928820673516179L;
   private final int numberOfSuccesses;
   private final int populationSize;
   private final int sampleSize;
   private double numericalVariance;
   private boolean numericalVarianceIsCalculated;

   public HypergeometricDistribution(int populationSize, int numberOfSuccesses, int sampleSize) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException {
      this(new Well19937c(), populationSize, numberOfSuccesses, sampleSize);
   }

   public HypergeometricDistribution(RandomGenerator rng, int populationSize, int numberOfSuccesses, int sampleSize) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException {
      super(rng);
      this.numericalVariance = Double.NaN;
      this.numericalVarianceIsCalculated = false;
      if (populationSize <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.POPULATION_SIZE, populationSize);
      } else if (numberOfSuccesses < 0) {
         throw new NotPositiveException(LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
      } else if (sampleSize < 0) {
         throw new NotPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
      } else if (numberOfSuccesses > populationSize) {
         throw new NumberIsTooLargeException(LocalizedFormats.NUMBER_OF_SUCCESS_LARGER_THAN_POPULATION_SIZE, numberOfSuccesses, populationSize, true);
      } else if (sampleSize > populationSize) {
         throw new NumberIsTooLargeException(LocalizedFormats.SAMPLE_SIZE_LARGER_THAN_POPULATION_SIZE, sampleSize, populationSize, true);
      } else {
         this.numberOfSuccesses = numberOfSuccesses;
         this.populationSize = populationSize;
         this.sampleSize = sampleSize;
      }
   }

   public double cumulativeProbability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x < domain[0]) {
         ret = 0.0;
      } else if (x >= domain[1]) {
         ret = 1.0;
      } else {
         ret = this.innerCumulativeProbability(domain[0], x, 1);
      }

      return ret;
   }

   private int[] getDomain(int n, int m, int k) {
      return new int[]{this.getLowerDomain(n, m, k), this.getUpperDomain(m, k)};
   }

   private int getLowerDomain(int n, int m, int k) {
      return FastMath.max(0, m - (n - k));
   }

   public int getNumberOfSuccesses() {
      return this.numberOfSuccesses;
   }

   public int getPopulationSize() {
      return this.populationSize;
   }

   public int getSampleSize() {
      return this.sampleSize;
   }

   private int getUpperDomain(int m, int k) {
      return FastMath.min(k, m);
   }

   public double probability(int x) {
      double logProbability = this.logProbability(x);
      return logProbability == Double.NEGATIVE_INFINITY ? 0.0 : FastMath.exp(logProbability);
   }

   public double logProbability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x >= domain[0] && x <= domain[1]) {
         double p = (double)this.sampleSize / (double)this.populationSize;
         double q = (double)(this.populationSize - this.sampleSize) / (double)this.populationSize;
         double p1 = SaddlePointExpansion.logBinomialProbability(x, this.numberOfSuccesses, p, q);
         double p2 = SaddlePointExpansion.logBinomialProbability(this.sampleSize - x, this.populationSize - this.numberOfSuccesses, p, q);
         double p3 = SaddlePointExpansion.logBinomialProbability(this.sampleSize, this.populationSize, p, q);
         ret = p1 + p2 - p3;
      } else {
         ret = Double.NEGATIVE_INFINITY;
      }

      return ret;
   }

   public double upperCumulativeProbability(int x) {
      int[] domain = this.getDomain(this.populationSize, this.numberOfSuccesses, this.sampleSize);
      double ret;
      if (x <= domain[0]) {
         ret = 1.0;
      } else if (x > domain[1]) {
         ret = 0.0;
      } else {
         ret = this.innerCumulativeProbability(domain[1], x, -1);
      }

      return ret;
   }

   private double innerCumulativeProbability(int x0, int x1, int dx) {
      double ret;
      for(ret = this.probability(x0); x0 != x1; ret += this.probability(x0)) {
         x0 += dx;
      }

      return ret;
   }

   public double getNumericalMean() {
      return (double)this.getSampleSize() * ((double)this.getNumberOfSuccesses() / (double)this.getPopulationSize());
   }

   public double getNumericalVariance() {
      if (!this.numericalVarianceIsCalculated) {
         this.numericalVariance = this.calculateNumericalVariance();
         this.numericalVarianceIsCalculated = true;
      }

      return this.numericalVariance;
   }

   protected double calculateNumericalVariance() {
      double N = (double)this.getPopulationSize();
      double m = (double)this.getNumberOfSuccesses();
      double n = (double)this.getSampleSize();
      return n * m * (N - n) * (N - m) / (N * N * (N - 1.0));
   }

   public int getSupportLowerBound() {
      return FastMath.max(0, this.getSampleSize() + this.getNumberOfSuccesses() - this.getPopulationSize());
   }

   public int getSupportUpperBound() {
      return FastMath.min(this.getNumberOfSuccesses(), this.getSampleSize());
   }

   public boolean isSupportConnected() {
      return true;
   }
}
