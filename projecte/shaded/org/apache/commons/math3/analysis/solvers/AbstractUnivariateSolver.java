package moze_intel.projecte.shaded.org.apache.commons.math3.analysis.solvers;

public abstract class AbstractUnivariateSolver extends BaseAbstractUnivariateSolver implements UnivariateSolver {
   protected AbstractUnivariateSolver(double absoluteAccuracy) {
      super(absoluteAccuracy);
   }

   protected AbstractUnivariateSolver(double relativeAccuracy, double absoluteAccuracy) {
      super(relativeAccuracy, absoluteAccuracy);
   }

   protected AbstractUnivariateSolver(double relativeAccuracy, double absoluteAccuracy, double functionValueAccuracy) {
      super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
   }
}
