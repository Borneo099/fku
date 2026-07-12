package moze_intel.projecte.emc.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.api.mapper.generator.IValueGenerator;
import moze_intel.projecte.shaded.org.apache.commons.math3.fraction.BigFraction;

public class BigFractionToLongGenerator implements IValueGenerator {
   private final IValueGenerator inner;

   public BigFractionToLongGenerator(IValueGenerator inner) {
      this.inner = inner;
   }

   public Map generateValues() {
      Map innerResult = this.inner.generateValues();
      Map myResult = new HashMap();
      Iterator var3 = innerResult.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         BigFraction value = (BigFraction)entry.getValue();
         if (value.longValue() > 0L) {
            myResult.put(entry.getKey(), value.longValue());
         }
      }

      return myResult;
   }
}
