package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class NoDataException extends MathIllegalArgumentException {
   private static final long serialVersionUID = -3629324471511904459L;

   public NoDataException() {
      this(LocalizedFormats.NO_DATA);
   }

   public NoDataException(Localizable specific) {
      super(specific);
   }
}
