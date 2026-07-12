package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class ConvergenceException extends MathIllegalStateException {
   private static final long serialVersionUID = 4330003017885151975L;

   public ConvergenceException() {
      this(LocalizedFormats.CONVERGENCE_FAILED);
   }

   public ConvergenceException(Localizable pattern, Object... args) {
      this.getContext().addMessage(pattern, args);
   }
}
