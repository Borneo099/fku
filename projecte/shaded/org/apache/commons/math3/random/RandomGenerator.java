package moze_intel.projecte.shaded.org.apache.commons.math3.random;

public interface RandomGenerator {
   void setSeed(int var1);

   void setSeed(int[] var1);

   void setSeed(long var1);

   void nextBytes(byte[] var1);

   int nextInt();

   int nextInt(int var1);

   long nextLong();

   boolean nextBoolean();

   float nextFloat();

   double nextDouble();

   double nextGaussian();
}
