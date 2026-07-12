package moze_intel.projecte.emc.collector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.mapper.collector.IExtendedMappingCollector;

public abstract class AbstractMappingCollector implements IExtendedMappingCollector {
   private final IValueArithmetic defaultArithmetic;

   AbstractMappingCollector(IValueArithmetic defaultArithmetic) {
      this.defaultArithmetic = defaultArithmetic;
   }

   public void addConversion(int outnumber, Object output, Iterable ingredients) {
      this.addConversion(outnumber, output, this.listToMapOfCounts(ingredients));
   }

   public void addConversion(int outnumber, Object output, Iterable ingredients, IValueArithmetic arithmeticForConversion) {
      this.addConversion(outnumber, output, this.listToMapOfCounts(ingredients), arithmeticForConversion);
   }

   private Map listToMapOfCounts(Iterable iterable) {
      Map map = new HashMap();
      Iterator var3 = iterable.iterator();

      while(var3.hasNext()) {
         Object ingredient = var3.next();
         map.merge(ingredient, 1, Integer::sum);
      }

      return map;
   }

   public void setValueFromConversion(int outnumber, Object something, Iterable ingredients) {
      this.setValueFromConversion(outnumber, something, this.listToMapOfCounts(ingredients));
   }

   public abstract void setValueFromConversion(int var1, Object var2, Map var3);

   public void addConversion(int outnumber, Object output, Map ingredientsWithAmount) {
      this.addConversion(outnumber, output, ingredientsWithAmount, this.getArithmetic());
   }

   public IValueArithmetic getArithmetic() {
      return this.defaultArithmetic;
   }

   public void finishCollection() {
   }
}
