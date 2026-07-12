package moze_intel.projecte.shaded.org.apache.commons.math3.analysis.solvers;

import moze_intel.projecte.shaded.org.apache.commons.math3.analysis.UnivariateFunction;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.TooManyEvaluationsException;

public interface BaseUnivariateSolver {
   int getMaxEvaluations();

   int getEvaluations();

   double getAbsoluteAccuracy();

   double getRelativeAccuracy();

   double getFunctionValueAccuracy();

   double solve(int var1, UnivariateFunction var2, double var3, double var5) throws MathIllegalArgumentException, TooManyEvaluationsException;

   double solve(int var1, UnivariateFunction var2, double var3, double var5, double var7) throws MathIllegalArgumentException, TooManyEvaluationsException;

   double solve(int var1, UnivariateFunction var2, double var3);
}
