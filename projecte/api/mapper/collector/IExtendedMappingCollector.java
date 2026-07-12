package moze_intel.projecte.api.mapper.collector;

import java.util.Map;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;

public interface IExtendedMappingCollector extends IMappingCollector {
   void addConversion(int var1, Object var2, Map var3, IValueArithmetic var4);

   void addConversion(int var1, Object var2, Iterable var3, IValueArithmetic var4);

   IValueArithmetic getArithmetic();
}
