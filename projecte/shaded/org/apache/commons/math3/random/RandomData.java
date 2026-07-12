package moze_intel.projecte.shaded.org.apache.commons.math3.random;

import java.util.Collection;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotANumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotFiniteNumberException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;

/** @deprecated */
@Deprecated
public interface RandomData {
   String nextHexString(int var1) throws NotStrictlyPositiveException;

   int nextInt(int var1, int var2) throws NumberIsTooLargeException;

   long nextLong(long var1, long var3) throws NumberIsTooLargeException;

   String nextSecureHexString(int var1) throws NotStrictlyPositiveException;

   int nextSecureInt(int var1, int var2) throws NumberIsTooLargeException;

   long nextSecureLong(long var1, long var3) throws NumberIsTooLargeException;

   long nextPoisson(double var1) throws NotStrictlyPositiveException;

   double nextGaussian(double var1, double var3) throws NotStrictlyPositiveException;

   double nextExponential(double var1) throws NotStrictlyPositiveException;

   double nextUniform(double var1, double var3) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException;

   double nextUniform(double var1, double var3, boolean var5) throws NumberIsTooLargeException, NotFiniteNumberException, NotANumberException;

   int[] nextPermutation(int var1, int var2) throws NumberIsTooLargeException, NotStrictlyPositiveException;

   Object[] nextSample(Collection var1, int var2) throws NumberIsTooLargeException, NotStrictlyPositiveException;
}
