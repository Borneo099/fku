package moze_intel.projecte.shaded.org.apache.commons.math3.random;

import java.util.Random;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;

public class RandomGeneratorFactory {
   private RandomGeneratorFactory() {
   }

   public static RandomGenerator createRandomGenerator(final Random rng) {
      return new RandomGenerator() {
         public void setSeed(int seed) {
            rng.setSeed((long)seed);
         }

         public void setSeed(int[] seed) {
            rng.setSeed(RandomGeneratorFactory.convertToLong(seed));
         }

         public void setSeed(long seed) {
            rng.setSeed(seed);
         }

         public void nextBytes(byte[] bytes) {
            rng.nextBytes(bytes);
         }

         public int nextInt() {
            return rng.nextInt();
         }

         public int nextInt(int n) {
            if (n <= 0) {
               throw new NotStrictlyPositiveException(n);
            } else {
               return rng.nextInt(n);
            }
         }

         public long nextLong() {
            return rng.nextLong();
         }

         public boolean nextBoolean() {
            return rng.nextBoolean();
         }

         public float nextFloat() {
            return rng.nextFloat();
         }

         public double nextDouble() {
            return rng.nextDouble();
         }

         public double nextGaussian() {
            return rng.nextGaussian();
         }
      };
   }

   public static long convertToLong(int[] seed) {
      long prime = 4294967291L;
      long combined = 0L;
      int[] arr$ = seed;
      int len$ = seed.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int s = arr$[i$];
         combined = combined * 4294967291L + (long)s;
      }

      return combined;
   }
}
