package moze_intel.projecte.shaded.org.apache.commons.math3.exception;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class TooManyEvaluationsException extends MaxCountExceededException {
   private static final long serialVersionUID = 4330003017885151975L;

   public TooManyEvaluationsException(Number max) {
      super(max);
      this.getContext().addMessage(LocalizedFormats.EVALUATIONS);
   }
}
