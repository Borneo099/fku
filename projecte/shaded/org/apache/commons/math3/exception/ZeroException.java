package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class ZeroException extends MathIllegalNumberException {
   private static final long serialVersionUID = -1960874856936000015L;

   public ZeroException() {
      this(LocalizedFormats.ZERO_NOT_ALLOWED);
   }

   public ZeroException(Localizable specific, Object... arguments) {
      super(specific, INTEGER_ZERO, arguments);
   }
}
