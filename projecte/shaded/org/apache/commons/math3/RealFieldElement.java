package moze_intel.projecte.shaded.org.apache.commons.math3;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.DimensionMismatchException;

public interface RealFieldElement extends FieldElement {
   double getReal();

   Object add(double var1);

   Object subtract(double var1);

   Object multiply(double var1);

   Object divide(double var1);

   Object remainder(double var1);

   Object remainder(Object var1) throws DimensionMismatchException;

   Object abs();

   Object ceil();

   Object floor();

   Object rint();

   long round();

   Object signum();

   Object copySign(Object var1);

   Object copySign(double var1);

   Object scalb(int var1);

   Object hypot(Object var1) throws DimensionMismatchException;

   Object reciprocal();

   Object sqrt();

   Object cbrt();

   Object rootN(int var1);

   Object pow(double var1);

   Object pow(int var1);

   Object pow(Object var1) throws DimensionMismatchException;

   Object exp();

   Object expm1();

   Object log();

   Object log1p();

   Object cos();

   Object sin();

   Object tan();

   Object acos();

   Object asin();

   Object atan();

   Object atan2(Object var1) throws DimensionMismatchException;

   Object cosh();

   Object sinh();

   Object tanh();

   Object acosh();

   Object asinh();

   Object atanh();

   Object linearCombination(Object[] var1, Object[] var2) throws DimensionMismatchException;

   Object linearCombination(double[] var1, Object[] var2) throws DimensionMismatchException;

   Object linearCombination(Object var1, Object var2, Object var3, Object var4);

   Object linearCombination(double var1, Object var3, double var4, Object var6);

   Object linearCombination(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6);

   Object linearCombination(double var1, Object var3, double var4, Object var6, double var7, Object var9);

   Object linearCombination(Object var1, Object var2, Object var3, Object var4, Object var5, Object var6, Object var7, Object var8);

   Object linearCombination(double var1, Object var3, double var4, Object var6, double var7, Object var9, double var10, Object var12);
}
