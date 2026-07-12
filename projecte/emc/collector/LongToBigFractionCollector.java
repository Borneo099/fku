package moze_intel.projecte.emc.collector;

import java.util.Map;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.mapper.collector.IExtendedMappingCollector;
import moze_intel.projecte.shaded.org.apache.commons.math3.fraction.BigFraction;

public class LongToBigFractionCollector extends AbstractMappingCollector {
   private final IExtendedMappingCollector inner;

   public LongToBigFractionCollector(IExtendedMappingCollector inner) {
      super(inner.getArithmetic());
      this.inner = inner;
   }

   public void setValueFromConversion(int outnumber, Object something, Map ingredientsWithAmount) {
      this.inner.setValueFromConversion(outnumber, something, ingredientsWithAmount);
   }

   public void addConversion(int outnumber, Object output, Map ingredientsWithAmount, IValueArithmetic arithmeticForConversion) {
      this.inner.addConversion(outnumber, output, ingredientsWithAmount, arithmeticForConversion);
   }

   public void setValueBefore(Object something, Long value) {
      this.inner.setValueBefore(something, new BigFraction(value));
   }

   public void setValueAfter(Object something, Long value) {
      this.inner.setValueAfter(something, new BigFraction(value));
   }
}
