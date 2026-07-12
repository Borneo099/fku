package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;

public interface RealDistribution {
   double probability(double var1);

   double density(double var1);

   double cumulativeProbability(double var1);

   /** @deprecated */
   @Deprecated
   double cumulativeProbability(double var1, double var3) throws NumberIsTooLargeException;

   double inverseCumulativeProbability(double var1) throws OutOfRangeException;

   double getNumericalMean();

   double getNumericalVariance();

   double getSupportLowerBound();

   double getSupportUpperBound();

   /** @deprecated */
   @Deprecated
   boolean isSupportLowerBoundInclusive();

   /** @deprecated */
   @Deprecated
   boolean isSupportUpperBoundInclusive();

   boolean isSupportConnected();

   void reseedRandomGenerator(long var1);

   double sample();

   double[] sample(int var1);
}
