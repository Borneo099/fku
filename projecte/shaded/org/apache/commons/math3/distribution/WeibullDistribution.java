package moze_intel.projecte.shaded.org.apache.commons.math3.distribution;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.RandomGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.random.Well19937c;
import moze_intel.projecte.shaded.org.apache.commons.math3.special.Gamma;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class WeibullDistribution extends AbstractRealDistribution {
   public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1.0E-9;
   private static final long serialVersionUID = 8589540077390120676L;
   private final double shape;
   private final double scale;
   private final double solverAbsoluteAccuracy;
   private double numericalMean;
   private boolean numericalMeanIsCalculated;
   private double numericalVariance;
   private boolean numericalVarianceIsCalculated;

   public WeibullDistribution(double alpha, double beta) throws NotStrictlyPositiveException {
      this(alpha, beta, 1.0E-9);
   }

   public WeibullDistribution(double alpha, double beta, double inverseCumAccuracy) {
      this(new Well19937c(), alpha, beta, inverseCumAccuracy);
   }

   public WeibullDistribution(RandomGenerator rng, double alpha, double beta) throws NotStrictlyPositiveException {
      this(rng, alpha, beta, 1.0E-9);
   }

   public WeibullDistribution(RandomGenerator rng, double alpha, double beta, double inverseCumAccuracy) throws NotStrictlyPositiveException {
      super(rng);
      this.numericalMean = Double.NaN;
      this.numericalMeanIsCalculated = false;
      this.numericalVariance = Double.NaN;
      this.numericalVarianceIsCalculated = false;
      if (alpha <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.SHAPE, alpha);
      } else if (beta <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.SCALE, beta);
      } else {
         this.scale = beta;
         this.shape = alpha;
         this.solverAbsoluteAccuracy = inverseCumAccuracy;
      }
   }

   public double getShape() {
      return this.shape;
   }

   public double getScale() {
      return this.scale;
   }

   public double density(double x) {
      if (x < 0.0) {
         return 0.0;
      } else {
         double xscale = x / this.scale;
         double xscalepow = FastMath.pow(xscale, this.shape - 1.0);
         double xscalepowshape = xscalepow * xscale;
         return this.shape / this.scale * xscalepow * FastMath.exp(-xscalepowshape);
      }
   }

   public double logDensity(double x) {
      if (x < 0.0) {
         return Double.NEGATIVE_INFINITY;
      } else {
         double xscale = x / this.scale;
         double logxscalepow = FastMath.log(xscale) * (this.shape - 1.0);
         double xscalepowshape = FastMath.exp(logxscalepow) * xscale;
         return FastMath.log(this.shape / this.scale) + logxscalepow - xscalepowshape;
      }
   }

   public double cumulativeProbability(double x) {
      double ret;
      if (x <= 0.0) {
         ret = 0.0;
      } else {
         ret = 1.0 - FastMath.exp(-FastMath.pow(x / this.scale, this.shape));
      }

      return ret;
   }

   public double inverseCumulativeProbability(double p) {
      if (!(p < 0.0) && !(p > 1.0)) {
         double ret;
         if (p == 0.0) {
            ret = 0.0;
         } else if (p == 1.0) {
            ret = Double.POSITIVE_INFINITY;
         } else {
            ret = this.scale * FastMath.pow(-FastMath.log1p(-p), 1.0 / this.shape);
         }

         return ret;
      } else {
         throw new OutOfRangeException(p, 0.0, 1.0);
      }
   }

   protected double getSolverAbsoluteAccuracy() {
      return this.solverAbsoluteAccuracy;
   }

   public double getNumericalMean() {
      if (!this.numericalMeanIsCalculated) {
         this.numericalMean = this.calculateNumericalMean();
         this.numericalMeanIsCalculated = true;
      }

      return this.numericalMean;
   }

   protected double calculateNumericalMean() {
      double sh = this.getShape();
      double sc = this.getScale();
      return sc * FastMath.exp(Gamma.logGamma(1.0 + 1.0 / sh));
   }

   public double getNumericalVariance() {
      if (!this.numericalVarianceIsCalculated) {
         this.numericalVariance = this.calculateNumericalVariance();
         this.numericalVarianceIsCalculated = true;
      }

      return this.numericalVariance;
   }

   protected double calculateNumericalVariance() {
      double sh = this.getShape();
      double sc = this.getScale();
      double mn = this.getNumericalMean();
      return sc * sc * FastMath.exp(Gamma.logGamma(1.0 + 2.0 / sh)) - mn * mn;
   }

   public double getSupportLowerBound() {
      return 0.0;
   }

   public double getSupportUpperBound() {
      return Double.POSITIVE_INFINITY;
   }

   public boolean isSupportLowerBoundInclusive() {
      return true;
   }

   public boolean isSupportUpperBoundInclusive() {
      return false;
   }

   public boolean isSupportConnected() {
      return true;
   }
}
