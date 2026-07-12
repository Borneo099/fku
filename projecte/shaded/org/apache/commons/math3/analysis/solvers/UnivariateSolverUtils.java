package moze_intel.projecte.shaded.org.apache.commons.math3.analysis.solvers;

import moze_intel.projecte.shaded.org.apache.commons.math3.analysis.UnivariateFunction;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NoBracketingException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NullArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooLargeException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;
import moze_intel.projecte.shaded.org.apache.commons.math3.util.FastMath;

public class UnivariateSolverUtils {
   private UnivariateSolverUtils() {
   }

   public static double solve(UnivariateFunction function, double x0, double x1) throws NullArgumentException, NoBracketingException {
      if (function == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION, new Object[0]);
      } else {
         UnivariateSolver solver = new BrentSolver();
         return solver.solve(Integer.MAX_VALUE, function, x0, x1);
      }
   }

   public static double solve(UnivariateFunction function, double x0, double x1, double absoluteAccuracy) throws NullArgumentException, NoBracketingException {
      if (function == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION, new Object[0]);
      } else {
         UnivariateSolver solver = new BrentSolver(absoluteAccuracy);
         return solver.solve(Integer.MAX_VALUE, function, x0, x1);
      }
   }

   public static double forceSide(int maxEval, UnivariateFunction f, BracketedUnivariateSolver bracketing, double baseRoot, double min, double max, AllowedSolution allowedSolution) throws NoBracketingException {
      if (allowedSolution == AllowedSolution.ANY_SIDE) {
         return baseRoot;
      } else {
         double step = FastMath.max(bracketing.getAbsoluteAccuracy(), FastMath.abs(baseRoot * bracketing.getRelativeAccuracy()));
         double xLo = FastMath.max(min, baseRoot - step);
         double fLo = f.value(xLo);
         double xHi = FastMath.min(max, baseRoot + step);
         double fHi = f.value(xHi);
         int remainingEval = maxEval - 2;

         while(remainingEval > 0) {
            if (fLo >= 0.0 && fHi <= 0.0 || fLo <= 0.0 && fHi >= 0.0) {
               return bracketing.solve(remainingEval, f, xLo, xHi, baseRoot, allowedSolution);
            }

            boolean changeLo = false;
            boolean changeHi = false;
            if (fLo < fHi) {
               if (fLo >= 0.0) {
                  changeLo = true;
               } else {
                  changeHi = true;
               }
            } else if (fLo > fHi) {
               if (fLo <= 0.0) {
                  changeLo = true;
               } else {
                  changeHi = true;
               }
            } else {
               changeLo = true;
               changeHi = true;
            }

            if (changeLo) {
               xLo = FastMath.max(min, xLo - step);
               fLo = f.value(xLo);
               --remainingEval;
            }

            if (changeHi) {
               xHi = FastMath.min(max, xHi + step);
               fHi = f.value(xHi);
               --remainingEval;
            }
         }

         throw new NoBracketingException(LocalizedFormats.FAILED_BRACKETING, xLo, xHi, fLo, fHi, new Object[]{maxEval - remainingEval, maxEval, baseRoot, min, max});
      }
   }

   public static double[] bracket(UnivariateFunction function, double initial, double lowerBound, double upperBound) throws NullArgumentException, NotStrictlyPositiveException, NoBracketingException {
      return bracket(function, initial, lowerBound, upperBound, 1.0, 1.0, Integer.MAX_VALUE);
   }

   public static double[] bracket(UnivariateFunction function, double initial, double lowerBound, double upperBound, int maximumIterations) throws NullArgumentException, NotStrictlyPositiveException, NoBracketingException {
      return bracket(function, initial, lowerBound, upperBound, 1.0, 1.0, maximumIterations);
   }

   public static double[] bracket(UnivariateFunction function, double initial, double lowerBound, double upperBound, double q, double r, int maximumIterations) throws NoBracketingException {
      if (function == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION, new Object[0]);
      } else if (q <= 0.0) {
         throw new NotStrictlyPositiveException(q);
      } else if (maximumIterations <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.INVALID_MAX_ITERATIONS, maximumIterations);
      } else {
         verifySequence(lowerBound, initial, upperBound);
         double a = initial;
         double b = initial;
         double fa = Double.NaN;
         double fb = Double.NaN;
         double delta = 0.0;

         for(int numIterations = 0; numIterations < maximumIterations && (a > lowerBound || b < upperBound); ++numIterations) {
            double previousA = a;
            double previousFa = fa;
            double previousB = b;
            double previousFb = fb;
            delta = r * delta + q;
            a = FastMath.max(initial - delta, lowerBound);
            b = FastMath.min(initial + delta, upperBound);
            fa = function.value(a);
            fb = function.value(b);
            if (numIterations == 0) {
               if (fa * fb <= 0.0) {
                  return new double[]{a, b};
               }
            } else {
               if (fa * previousFa <= 0.0) {
                  return new double[]{a, previousA};
               }

               if (fb * previousFb <= 0.0) {
                  return new double[]{previousB, b};
               }
            }
         }

         throw new NoBracketingException(a, b, fa, fb);
      }
   }

   public static double midpoint(double a, double b) {
      return (a + b) * 0.5;
   }

   public static boolean isBracketing(UnivariateFunction function, double lower, double upper) throws NullArgumentException {
      if (function == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION, new Object[0]);
      } else {
         double fLo = function.value(lower);
         double fHi = function.value(upper);
         return fLo >= 0.0 && fHi <= 0.0 || fLo <= 0.0 && fHi >= 0.0;
      }
   }

   public static boolean isSequence(double start, double mid, double end) {
      return start < mid && mid < end;
   }

   public static void verifyInterval(double lower, double upper) throws NumberIsTooLargeException {
      if (lower >= upper) {
         throw new NumberIsTooLargeException(LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL, lower, upper, false);
      }
   }

   public static void verifySequence(double lower, double initial, double upper) throws NumberIsTooLargeException {
      verifyInterval(lower, initial);
      verifyInterval(initial, upper);
   }

   public static void verifyBracketing(UnivariateFunction function, double lower, double upper) throws NullArgumentException, NoBracketingException {
      if (function == null) {
         throw new NullArgumentException(LocalizedFormats.FUNCTION, new Object[0]);
      } else {
         verifyInterval(lower, upper);
         if (!isBracketing(function, lower, upper)) {
            throw new NoBracketingException(lower, upper, function.value(lower), function.value(upper));
         }
      }
   }
}
