package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class NullArgumentException extends MathIllegalArgumentException {
   private static final long serialVersionUID = -6024911025449780478L;

   public NullArgumentException() {
      this(LocalizedFormats.NULL_NOT_ALLOWED);
   }

   public NullArgumentException(Localizable pattern, Object... arguments) {
      super(pattern, arguments);
   }
}
