package moze_intel.projecte.shaded.org.apache.commons.math3.analysis.solvers;

import moze_intel.projecte.shaded.org.apache.commons.math3.analysis.UnivariateFunction;

public interface BracketedUnivariateSolver extends BaseUnivariateSolver {
   double solve(int var1, UnivariateFunction var2, double var3, double var5, AllowedSolution var7);

   double solve(int var1, UnivariateFunction var2, double var3, double var5, double var7, AllowedSolution var9);
}
