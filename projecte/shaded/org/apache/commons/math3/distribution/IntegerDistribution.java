package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;

public interface IntegerDistribution {
   double probability(int var1);

   double cumulativeProbability(int var1);

   double cumulativeProbability(int var1, int var2) throws NumberIsTooLargeException;

   int inverseCumulativeProbability(double var1) throws OutOfRangeException;

   double getNumericalMean();

   double getNumericalVariance();

   int getSupportLowerBound();

   int getSupportUpperBound();

   boolean isSupportConnected();

   void reseedRandomGenerator(long var1);

   int sample();

   int[] sample(int var1);
}
