package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.ExceptionContext;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.ExceptionContextProvider;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.Localizable;

public class MathIllegalArgumentException extends IllegalArgumentException implements ExceptionContextProvider {
   private static final long serialVersionUID = -6024911025449780478L;
   private final ExceptionContext context = new ExceptionContext(this);

   public MathIllegalArgumentException(Localizable pattern, Object... args) {
      this.context.addMessage(pattern, args);
   }

   public ExceptionContext getContext() {
      return this.context;
   }

   public String getMessage() {
      return this.context.getMessage();
   }

   public String getLocalizedMessage() {
      return this.context.getLocalizedMessage();
   }
}
