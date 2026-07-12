package moze_intel.projecte.api.mapper.collector;

import java.util.Map;

public interface IMappingCollector {
   void addConversion(int var1, Object var2, Map var3);

   void addConversion(int var1, Object var2, Iterable var3);

   void setValueBefore(Object var1, Comparable var2);

   void setValueAfter(Object var1, Comparable var2);

   void setValueFromConversion(int var1, Object var2, Iterable var3);

   void setValueFromConversion(int var1, Object var2, Map var3);

   void finishCollection();
}
