package moze_intel.projecte.emc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.mapper.generator.IValueGenerator;
import moze_intel.projecte.emc.collector.MappingCollector;

public class SimpleGraphMapper extends MappingCollector implements IValueGenerator {
   private static final boolean OVERWRITE_FIXED_VALUES = false;
   private final Comparable ZERO;
   private static boolean logFoundExploits = true;

   public SimpleGraphMapper(IValueArithmetic arithmetic) {
      super(arithmetic);
      this.ZERO = arithmetic.getZero();
   }

   private static boolean hasSmallerOrEqual(Map m, Object key, Comparable value) {
      return m.containsKey(key) && ((Comparable)m.get(key)).compareTo(value) <= 0;
   }

   private static boolean hasSmaller(Map m, Object key, Comparable value) {
      return m.containsKey(key) && ((Comparable)m.get(key)).compareTo(value) < 0;
   }

   static void setLogFoundExploits(boolean log) {
      logFoundExploits = log;
   }

   private static boolean updateMapWithMinimum(Map m, Object key, Comparable value) {
      if (!hasSmaller(m, key, value)) {
         m.put(key, value);
         return true;
      } else {
         return false;
      }
   }

   private boolean canOverride(Object something, Comparable value) {
      if (this.fixValueBeforeInherit.containsKey(something)) {
         return ((Comparable)this.fixValueBeforeInherit.get(something)).compareTo(value) == 0;
      } else {
         return true;
      }
   }

   public Map generateValues() {
      Map values = new HashMap();
      Map changedValues = new HashMap();
      Map reasonForChange = new HashMap();
      Iterator var4 = this.fixValueBeforeInherit.entrySet().iterator();

      Map.Entry entry;
      while(var4.hasNext()) {
         entry = (Map.Entry)var4.next();
         changedValues.put(entry.getKey(), (Comparable)entry.getValue());
         reasonForChange.put(entry.getKey(), "fixValueBefore");
      }

      label142:
      while(!changedValues.isEmpty()) {
         Iterator var7;
         MappingCollector.Conversion conversion;
         Comparable ingredientValue;
         HashMap nextChangedValues;
         label100:
         for(; !changedValues.isEmpty(); changedValues = nextChangedValues) {
            nextChangedValues = new HashMap();
            debugPrintln("Loop");
            Iterator var13 = changedValues.entrySet().iterator();

            label97:
            while(true) {
               Map.Entry entry;
               do {
                  do {
                     if (!var13.hasNext()) {
                        continue label100;
                     }

                     entry = (Map.Entry)var13.next();
                  } while(!this.canOverride(entry.getKey(), (Comparable)entry.getValue()));
               } while(!updateMapWithMinimum(values, entry.getKey(), (Comparable)entry.getValue()));

               debugFormat("Set Value for {} to {} because {}", new Object[]{entry.getKey(), entry.getValue(), reasonForChange.get(entry.getKey())});
               var7 = this.getUsesFor(entry.getKey()).iterator();

               while(true) {
                  do {
                     do {
                        if (!var7.hasNext()) {
                           continue label97;
                        }

                        conversion = (MappingCollector.Conversion)var7.next();
                     } while(this.overwriteConversion.containsKey(conversion.output) && this.overwriteConversion.get(conversion.output) != conversion);

                     ingredientValue = conversion.arithmeticForConversion.div(this.valueForConversion(values, conversion), (long)conversion.outnumber);
                  } while(ingredientValue.compareTo(this.ZERO) <= 0 && !conversion.arithmeticForConversion.isFree(ingredientValue));

                  if (!hasSmallerOrEqual(values, conversion.output, ingredientValue) && updateMapWithMinimum(nextChangedValues, conversion.output, ingredientValue)) {
                     reasonForChange.put(conversion.output, entry.getKey());
                  }
               }
            }
         }

         var4 = this.conversionsFor.entrySet().iterator();

         while(true) {
            Comparable minConversionValue;
            label138:
            do {
               if (!var4.hasNext()) {
                  continue label142;
               }

               entry = (Map.Entry)var4.next();
               minConversionValue = null;
               var7 = ((Set)entry.getValue()).iterator();

               while(true) {
                  while(true) {
                     Comparable resultValueConversion;
                     Comparable resultValueActual;
                     do {
                        do {
                           if (!var7.hasNext()) {
                              continue label138;
                           }

                           conversion = (MappingCollector.Conversion)var7.next();
                           ingredientValue = this.valueForConversion(values, conversion);
                           resultValueConversion = conversion.arithmeticForConversion.div(ingredientValue, (long)conversion.outnumber);
                           resultValueActual = (Comparable)values.getOrDefault(entry.getKey(), this.ZERO);
                           if ((resultValueConversion.compareTo(this.ZERO) > 0 || conversion.arithmeticForConversion.isFree(resultValueConversion)) && (minConversionValue == null || minConversionValue.compareTo(resultValueConversion) > 0)) {
                              minConversionValue = resultValueConversion;
                           }
                        } while(this.ZERO.compareTo(ingredientValue) >= 0);
                     } while(resultValueConversion.compareTo(resultValueActual) >= 0);

                     if (this.overwriteConversion.containsKey(conversion.output) && this.overwriteConversion.get(conversion.output) != conversion) {
                        if (logFoundExploits) {
                           PECore.LOGGER.warn("EMC Exploit: \"{}\" ingredient cost: {} value of result: {} setValueFromConversion: {}", new Object[]{conversion, ingredientValue, resultValueActual, this.overwriteConversion.get(conversion.output)});
                        }
                     } else if (this.canOverride(entry.getKey(), this.ZERO)) {
                        debugFormat("Setting {} to 0 because result ({}) > cost ({}): {}", new Object[]{entry.getKey(), resultValueActual, ingredientValue, conversion});
                        changedValues.put(conversion.output, this.ZERO);
                        reasonForChange.put(conversion.output, "exploit recipe");
                     } else if (logFoundExploits) {
                        PECore.LOGGER.warn("EMC Exploit: ingredients ({}) cost {} but output value is {}", new Object[]{conversion, ingredientValue, resultValueActual});
                     }
                  }
               }
            } while(minConversionValue != null && !minConversionValue.equals(this.ZERO));

            if (values.containsKey(entry.getKey()) && !((Comparable)values.get(entry.getKey())).equals(this.ZERO) && this.canOverride(entry.getKey(), this.ZERO) && !hasSmaller(values, entry.getKey(), this.ZERO)) {
               debugFormat("Removing Value for {} because it does not have any nonzero-conversions anymore.", new Object[]{entry.getKey()});
               changedValues.put(entry.getKey(), this.ZERO);
               reasonForChange.put(entry.getKey(), "all conversions dead");
            }
         }
      }

      debugPrintln("");
      values.putAll(this.fixValueAfterInherit);
      values.entrySet().removeIf((something) -> {
         return this.arithmetic.isFree((Comparable)something.getValue());
      });
      return values;
   }

   private Comparable valueForConversion(Map values, MappingCollector.Conversion conversion) {
      try {
         return this.valueForConversionUnsafe(values, conversion);
      } catch (ArithmeticException var4) {
         PECore.LOGGER.warn("Could not calculate value for {}: {}", conversion.toString(), var4.toString());
         return this.ZERO;
      } catch (Exception var5) {
         PECore.LOGGER.warn("Could not calculate value for {}: {}", new Object[]{conversion.toString(), var5, var5});
         return this.ZERO;
      }
   }

   private Comparable valueForConversionUnsafe(Map values, MappingCollector.Conversion conversion) {
      Comparable value = conversion.value;
      boolean allIngredientsAreFree = true;
      boolean hasPositiveIngredientValues = false;
      Iterator var6 = conversion.ingredientsWithAmount.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         if (!values.containsKey(entry.getKey())) {
            return this.ZERO;
         }

         Comparable ingredientValue = conversion.arithmeticForConversion.mul((long)(Integer)entry.getValue(), (Comparable)values.get(entry.getKey()));
         if (ingredientValue.compareTo(this.ZERO) == 0) {
            return this.ZERO;
         }

         if (!conversion.arithmeticForConversion.isFree(ingredientValue)) {
            value = conversion.arithmeticForConversion.add(value, ingredientValue);
            if (ingredientValue.compareTo(this.ZERO) > 0 && (Integer)entry.getValue() > 0) {
               hasPositiveIngredientValues = true;
            }

            allIngredientsAreFree = false;
         }
      }

      return !allIngredientsAreFree && (!hasPositiveIngredientValues || value.compareTo(this.ZERO) > 0) ? value : conversion.arithmeticForConversion.getFree();
   }
}
