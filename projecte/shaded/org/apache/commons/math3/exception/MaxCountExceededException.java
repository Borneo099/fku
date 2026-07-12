package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class MaxCountExceededException extends MathIllegalStateException {
   private static final long serialVersionUID = 4330003017885151975L;
   private final Number max;

   public MaxCountExceededException(Number max) {
      this(LocalizedFormats.MAX_COUNT_EXCEEDED, max);
   }

   public MaxCountExceededException(Localizable specific, Number max, Object... args) {
      this.getContext().addMessage(specific, max, args);
      this.max = max;
   }

   public Number getMax() {
      return this.max;
   }
}
