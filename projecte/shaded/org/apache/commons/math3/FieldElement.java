package moze_intel.projecte.shaded.org.apache.commons.math3;

import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathArithmeticException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NullArgumentException;

public interface FieldElement {
   Object add(Object var1) throws NullArgumentException;

   Object subtract(Object var1) throws NullArgumentException;

   Object negate();

   Object multiply(int var1);

   Object multiply(Object var1) throws NullArgumentException;

   Object divide(Object var1) throws NullArgumentException, MathArithmeticException;

   Object reciprocal() throws MathArithmeticException;

   Field getField();
}
