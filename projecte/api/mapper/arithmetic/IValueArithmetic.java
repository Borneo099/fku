package moze_intel.projecte.api.mapper.arithmetic;

public interface IValueArithmetic {
   boolean isZero(Comparable var1);

   Comparable getZero();

   Comparable add(Comparable var1, Comparable var2);

   Comparable mul(long var1, Comparable var3);

   Comparable div(Comparable var1, long var2);

   Comparable getFree();

   boolean isFree(Comparable var1);
}
