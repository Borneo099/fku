package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class NumberIsTooSmallException extends MathIllegalNumberException {
   private static final long serialVersionUID = -6100997100383932834L;
   private final Number min;
   private final boolean boundIsAllowed;

   public NumberIsTooSmallException(Number wrong, Number min, boolean boundIsAllowed) {
      this(boundIsAllowed ? LocalizedFormats.NUMBER_TOO_SMALL : LocalizedFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED, wrong, min, boundIsAllowed);
   }

   public NumberIsTooSmallException(Localizable specific, Number wrong, Number min, boolean boundIsAllowed) {
      super(specific, wrong, min);
      this.min = min;
      this.boundIsAllowed = boundIsAllowed;
   }

   public boolean getBoundIsAllowed() {
      return this.boundIsAllowed;
   }

   public Number getMin() {
      return this.min;
   }
}
