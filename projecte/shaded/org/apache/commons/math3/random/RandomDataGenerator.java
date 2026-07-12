package moze_intel.projecte.shaded.org.apache.commons.math3.random;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Collection;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.BetaDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.BinomialDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.CauchyDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.ChiSquaredDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.ExponentialDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.FDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.GammaDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.HypergeometricDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.PascalDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.PoissonDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.TDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.UniformIntegerDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.WeibullDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.distribution.ZipfDistribution;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathInternalError;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotANumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotFiniteNumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.MathArrays;

public class RandomDataGenerator implements RandomData, Serializable {
   private static final long serialVersionUID = -626730818244969716L;
   private RandomGenerator rand = null;
   private RandomGenerator secRand = null;

   public RandomDataGenerator() {
   }

   public RandomDataGenerator(RandomGenerator rand) {
      this.rand = rand;
   }

   public String nextHexString(int len) throws NotStrictlyPositiveException {
      if (len <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
      } else {
         RandomGenerator ran = this.getRandomGenerator();
         StringBuilder outBuffer = new StringBuilder();
         byte[] randomBytes = new byte[len / 2 + 1];
         ran.nextBytes(randomBytes);

         for(int i = 0; i < randomBytes.length; ++i) {
            Integer c = Integer.valueOf(randomBytes[i]);
            String hex = Integer.toHexString(c + 128);
            if (hex.length() == 1) {
               hex = "0" + hex;
            }

            outBuffer.append(hex);
         }

         return outBuffer.toString().substring(0, len);
      }
   }

   public int nextInt(int lower, int upper) throws NumberIsTooLargeException {
      return (new UniformIntegerDistribution(this.getRandomGenerator(), lower, upper)).sample();
   }

   public long nextLong(long lower, long upper) throws NumberIsTooLargeException {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         long max = upper - lower + 1L;
         if (max > 0L) {
            return max < 2147483647L ? lower + (long)this.getRandomGenerator().nextInt((int)max) : lower + nextLong(this.getRandomGenerator(), max);
         } else {
            RandomGenerator rng = this.getRandomGenerator();

            long r;
            do {
               do {
                  r = rng.nextLong();
               } while(r < lower);
            } while(r > upper);

            return r;
         }
      }
   }

   private static long nextLong(RandomGenerator rng, long n) throws IllegalArgumentException {
      if (n <= 0L) {
         throw new NotStrictlyPositiveException(n);
      } else {
         byte[] byteArray = new byte[8];

         long bits;
         long val;
         do {
            rng.nextBytes(byteArray);
            bits = 0L;
            byte[] arr$ = byteArray;
            int len$ = byteArray.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               byte b = arr$[i$];
               bits = bits << 8 | (long)b & 255L;
            }

            bits &= Long.MAX_VALUE;
            val = bits % n;
         } while(bits - val + (n - 1L) < 0L);

         return val;
      }
   }

   public String nextSecureHexString(int len) throws NotStrictlyPositiveException {
      if (len <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
      } else {
         RandomGenerator secRan = this.getSecRan();
         MessageDigest alg = null;

         try {
            alg = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException var12) {
            throw new MathInternalError(var12);
         }

         alg.reset();
         int numIter = len / 40 + 1;
         StringBuilder outBuffer = new StringBuilder();

         for(int iter = 1; iter < numIter + 1; ++iter) {
            byte[] randomBytes = new byte[40];
            secRan.nextBytes(randomBytes);
            alg.update(randomBytes);
            byte[] hash = alg.digest();

            for(int i = 0; i < hash.length; ++i) {
               Integer c = Integer.valueOf(hash[i]);
               String hex = Integer.toHexString(c + 128);
               if (hex.length() == 1) {
                  hex = "0" + hex;
               }

               outBuffer.append(hex);
            }
         }

         return outBuffer.toString().substring(0, len);
      }
   }

   public int nextSecureInt(int lower, int upper) throws NumberIsTooLargeException {
      return (new UniformIntegerDistribution(this.getSecRan(), lower, upper)).sample();
   }

   public long nextSecureLong(long lower, long upper) throws NumberIsTooLargeException {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else {
         RandomGenerator rng = this.getSecRan();
         long max = upper - lower + 1L;
         if (max > 0L) {
            return max < 2147483647L ? lower + (long)rng.nextInt((int)max) : lower + nextLong(rng, max);
         } else {
            long r;
            do {
               do {
                  r = rng.nextLong();
               } while(r < lower);
            } while(r > upper);

            return r;
         }
      }
   }

   public long nextPoisson(double mean) throws NotStrictlyPositiveException {
      return (long)(new PoissonDistribution(this.getRandomGenerator(), mean, 1.0E-12, 10000000)).sample();
   }

   public double nextGaussian(double mu, double sigma) throws NotStrictlyPositiveException {
      if (sigma <= 0.0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.STANDARD_DEVIATION, sigma);
      } else {
         return sigma * this.getRandomGenerator().nextGaussian() + mu;
      }
   }

   public double nextExponential(double mean) throws NotStrictlyPositiveException {
      return (new ExponentialDistribution(this.getRandomGenerator(), mean, 1.0E-9)).sample();
   }

   public double nextGamma(double shape, double scale) throws NotStrictlyPositiveException {
      return (new GammaDistribution(this.getRandomGenerator(), shape, scale, 1.0E-9)).sample();
   }

   public int nextHypergeometric(int populationSize, int numberOfSuccesses, int sampleSize) throws NotPositiveException, NotStrictlyPositiveException, NumberIsTooLargeException {
      return (new HypergeometricDistribution(this.getRandomGenerator(), populationSize, numberOfSuccesses, sampleSize)).sample();
   }

   public int nextPascal(int r, double p) throws NotStrictlyPositiveException, OutOfRangeException {
      return (new PascalDistribution(this.getRandomGenerator(), r, p)).sample();
   }

   public double nextT(double df) throws NotStrictlyPositiveException {
      return (new TDistribution(this.getRandomGenerator(), df, 1.0E-9)).sample();
   }

   public double nextWeibull(double shape, double scale) throws NotStrictlyPositiveException {
      return (new WeibullDistribution(this.getRandomGenerator(), shape, scale, 1.0E-9)).sample();
   }

   public int nextZipf(int numberOfElements, double exponent) throws NotStrictlyPositiveException {
      return (new ZipfDistribution(this.getRandomGenerator(), numberOfElements, exponent)).sample();
   }

   public double nextBeta(double alpha, double beta) {
      return (new BetaDistribution(this.getRandomGenerator(), alpha, beta, 1.0E-9)).sample();
   }

   public int nextBinomial(int numberOfTrials, double probabilityOfSuccess) {
      return (new BinomialDistribution(this.getRandomGenerator(), numberOfTrials, probabilityOfSuccess)).sample();
   }

   public double nextCauchy(double median, double scale) {
      return (new CauchyDistribution(this.getRandomGenerator(), median, scale, 1.0E-9)).sample();
   }

   public double nextChiSquare(double df) {
      return (new ChiSquaredDistribution(this.getRandomGenerator(), df, 1.0E-9)).sample();
   }

   public double nextF(double numeratorDf, double denominatorDf) throws NotStrictlyPositiveException {
      return (new FDistribution(this.getRandomGenerator(), numeratorDf, denominatorDf, 1.0E-9)).sample();
   }

   public double nextUniform(double lower, double upper) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
      return this.nextUniform(lower, upper, false);
   }

   public double nextUniform(double lower, double upper, boolean lowerInclusive) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper, false);
      } else if (Double.isInfinite(lower)) {
         throw new NotFiniteNumberException(LocalizedFormats.INFINITE_BOUND, lower, new Object[0]);
      } else if (Double.isInfinite(upper)) {
         throw new NotFiniteNumberException(LocalizedFormats.INFINITE_BOUND, upper, new Object[0]);
      } else if (!Double.isNaN(lower) && !Double.isNaN(upper)) {
         RandomGenerator generator = this.getRandomGenerator();

         double u;
         for(u = generator.nextDouble(); !lowerInclusive && u <= 0.0; u = generator.nextDouble()) {
         }

         return u * upper + (1.0 - u) * lower;
      } else {
         throw new NotANumberException();
      }
   }

   public int[] nextPermutation(int n, int k) throws NumberIsTooLargeException, NotStrictlyPositiveException {
      if (k > n) {
         throw new NumberIsTooLargeException(LocalizedFormats.PERMUTATION_EXCEEDS_N, k, n, true);
      } else if (k <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.PERMUTATION_SIZE, k);
      } else {
         int[] index = MathArrays.natural(n);
         MathArrays.shuffle(index, this.getRandomGenerator());
         return MathArrays.copyOf(index, k);
      }
   }

   public Object[] nextSample(Collection c, int k) throws NumberIsTooLargeException, NotStrictlyPositiveException {
      int len = c.size();
      if (k > len) {
         throw new NumberIsTooLargeException(LocalizedFormats.SAMPLE_SIZE_EXCEEDS_COLLECTION_SIZE, k, len, true);
      } else if (k <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES, k);
      } else {
         Object[] objects = c.toArray();
         int[] index = this.nextPermutation(len, k);
         Object[] result = new Object[k];

         for(int i = 0; i < k; ++i) {
            result[i] = objects[index[i]];
         }

         return result;
      }
   }

   public void reSeed(long seed) {
      this.getRandomGenerator().setSeed(seed);
   }

   public void reSeedSecure() {
      this.getSecRan().setSeed(System.currentTimeMillis());
   }

   public void reSeedSecure(long seed) {
      this.getSecRan().setSeed(seed);
   }

   public void reSeed() {
      this.getRandomGenerator().setSeed(System.currentTimeMillis() + (long)System.identityHashCode(this));
   }

   public void setSecureAlgorithm(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
      this.secRand = RandomGeneratorFactory.createRandomGenerator(SecureRandom.getInstance(algorithm, provider));
   }

   public RandomGenerator getRandomGenerator() {
      if (this.rand == null) {
         this.initRan();
      }

      return this.rand;
   }

   private void initRan() {
      this.rand = new Well19937c(System.currentTimeMillis() + (long)System.identityHashCode(this));
   }

   private RandomGenerator getSecRan() {
      if (this.secRand == null) {
         this.secRand = RandomGeneratorFactory.createRandomGenerator(new SecureRandom());
         this.secRand.setSeed(System.currentTimeMillis() + (long)System.identityHashCode(this));
      }

      return this.secRand;
   }
}
