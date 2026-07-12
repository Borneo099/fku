package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;

public class NotPositiveException extends NumberIsTooSmallException {
   private static final long serialVersionUID = -2250556892093726375L;

   public NotPositiveException(Number value) {
      super(value, INTEGER_ZERO, true);
   }

   public NotPositiveException(Localizable specific, Number value) {
      super(specific, value, INTEGER_ZERO, true);
   }
}
