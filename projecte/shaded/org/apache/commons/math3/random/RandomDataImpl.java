package moze_intel.projecte.shaded.org.apache.commons.math3.random;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.IntegerDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.RealDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotANumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotFiniteNumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;

/** @deprecated */
@Deprecated
public class RandomDataImpl implements RandomData, Serializable {
   private static final long serialVersionUID = -626730818244969716L;
   private final RandomDataGenerator delegate;

   public RandomDataImpl() {
      this.delegate = new RandomDataGenerator();
   }

   public RandomDataImpl(RandomGenerator rand) {
      this.delegate = new RandomDataGenerator(rand);
   }

   /** @deprecated */
   @Deprecated
   RandomDataGenerator getDelegate() {
      return this.delegate;
   }

   public String nextHexString(int len) throws NotStrictlyPositiveException {
      return this.delegate.nextHexString(len);
   }

   public int nextInt(int lower, int upper) throws NumberIsTooLargeException {
      return this.delegate.nextInt(lower, upper);
   }

   public long nextLong(long lower, long upper) throws NumberIsTooLargeException {
      return this.delegate.nextLong(lower, upper);
   }

   public String nextSecureHexString(int len) throws NotStrictlyPositiveException {
      return this.delegate.nextSecureHexString(len);
   }

   public int nextSecureInt(int lower, int upper) throws NumberIsTooLargeException {
      return this.delegate.nextSecureInt(lower, upper);
   }

   public long nextSecureLong(long lower, long upper) throws NumberIsTooLargeException {
      return this.delegate.nextSecureLong(lower, upper);
   }

   public long nextPoisson(double mean) throws NotStrictlyPositiveException {
      return this.delegate.nextPoisson(mean);
   }

   public double nextGaussian(double mu, double sigma) throws NotStrictlyPositiveException {
      return this.delegate.nextGaussian(mu, sigma);
   }

   public double nextExponential(double mean) throws NotStrictlyPositiveException {
      return this.delegate.nextExponential(mean);
   }

   public double nextUniform(double lower, double upper) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
      return this.delegate.nextUniform(lower, upper);
   }

   public double nextUniform(double lower, double upper, boolean lowerInclusive) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
      return this.delegate.nextUniform(lower, upper, lowerInclusive);
   }

   public double nextBeta(double alpha, double beta) {
      return this.delegate.nextBeta(alpha, beta);
   }

   public int nextBinomial(int numberOfTrials, double probabilityOfSuccess) {
      return this.delegate.nextBinomial(numberOfTrials, probabilityOfSuccess);
   }

   public double nextCauchy(double median, double scale) {
      return this.delegate.nextCauchy(median, scale);
   }

   public double nextChiSquare(double df) {
      return this.delegate.nextChiSquare(df);
   }

   public double nextF(double numeratorDf, double denominatorDf) throws NotStrictlyPositiveException {
      return this.delegate.nextF(numeratorDf, denominatorDf);
   }

   public double nextGamma(double shape, double scale) throws NotStrictlyPositiveException {
      return this.delegate.nextGamma(shape, scale);
   }

   public int nextHypergeometric(int populationSize, int numberOfSuccesses, int sampleSize) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException {
      return this.delegate.nextHypergeometric(populationSize, numberOfSuccesses, sampleSize);
   }

   public int nextPascal(int r, double p) throws NotStrictlyPositiveException, OutOfRangeException {
      return this.delegate.nextPascal(r, p);
   }

   public double nextT(double df) throws NotStrictlyPositiveException {
      return this.delegate.nextT(df);
   }

   public double nextWeibull(double shape, double scale) throws NotStrictlyPositiveException {
      return this.delegate.nextWeibull(shape, scale);
   }

   public int nextZipf(int numberOfElements, double exponent) throws NotStrictlyPositiveException {
      return this.delegate.nextZipf(numberOfElements, exponent);
   }

   public void reSeed(long seed) {
      this.delegate.reSeed(seed);
   }

   public void reSeedSecure() {
      this.delegate.reSeedSecure();
   }

   public void reSeedSecure(long seed) {
      this.delegate.reSeedSecure(seed);
   }

   public void reSeed() {
      this.delegate.reSeed();
   }

   public void setSecureAlgorithm(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
      this.delegate.setSecureAlgorithm(algorithm, provider);
   }

   public int[] nextPermutation(int n, int k) throws NotStrictlyPositiveException, NumberIsTooLargeException {
      return this.delegate.nextPermutation(n, k);
   }

   public Object[] nextSample(Collection c, int k) throws NotStrictlyPositiveException, NumberIsTooLargeException {
      return this.delegate.nextSample(c, k);
   }

   /** @deprecated */
   @Deprecated
   public double nextInversionDeviate(RealDistribution distribution) throws MathIllegalArgumentException {
      return distribution.inverseCumulativeProbability(this.nextUniform(0.0, 1.0));
   }

   /** @deprecated */
   @Deprecated
   public int nextInversionDeviate(IntegerDistribution distribution) throws MathIllegalArgumentException {
      return distribution.inverseCumulativeProbability(this.nextUniform(0.0, 1.0));
   }
}
