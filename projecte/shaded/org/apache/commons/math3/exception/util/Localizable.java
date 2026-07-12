package moze_intel.projecte.shaded.org.apache.commons.math3.exception.util;

import java.io.Serializable;
import java.util.Locale;

public interface Localizable extends Serializable {
   String getSourceString();

   String getLocalizedString(Locale var1);
}
