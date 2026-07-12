package moze_intel.projecte.emc.collector;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;

public abstract class MappingCollector extends AbstractMappingCollector {
   private static final boolean DEBUG_GRAPHMAPPER = false;
   protected final IValueArithmetic arithmetic;
   protected final Map overwriteConversion = new HashMap();
   protected final Map conversionsFor = new HashMap();
   private final Map usedIn = new HashMap();
   protected final Map fixValueBeforeInherit = new HashMap();
   protected final Map fixValueAfterInherit = new HashMap();

   protected MappingCollector(IValueArithmetic arithmetic) {
      super(arithmetic);
      this.arithmetic = arithmetic;
   }

   protected static void debugFormat(String format, Object... args) {
   }

   protected static void debugPrintln(String s) {
      debugFormat(s);
   }

   private Set getConversionsFor(Object something) {
      return (Set)this.conversionsFor.computeIfAbsent(something, (t) -> {
         return new LinkedHashSet();
      });
   }

   protected Set getUsesFor(Object something) {
      return (Set)this.usedIn.computeIfAbsent(something, (t) -> {
         return new LinkedHashSet();
      });
   }

   private void addConversionToIngredientUsages(Conversion conversion) {
      Iterator var2 = conversion.ingredientsWithAmount.keySet().iterator();

      while(var2.hasNext()) {
         Object ingredient = var2.next();
         Set usesForIngredient = this.getUsesFor(ingredient);
         usesForIngredient.add(conversion);
      }

   }

   public void addConversion(int outnumber, Object output, Map ingredientsWithAmount, IValueArithmetic arithmeticForConversion) {
      if (output != null && !ingredientsWithAmount.containsKey((Object)null)) {
         if (outnumber <= 0) {
            throw new IllegalArgumentException("outnumber has to be > 0!");
         } else {
            Conversion conversion = new Conversion(output, outnumber, ingredientsWithAmount, arithmeticForConversion, this.arithmetic.getZero());
            if (this.getConversionsFor(output).add(conversion)) {
               this.addConversionToIngredientUsages(conversion);
            }
         }
      } else {
         PECore.debugLog("Ignoring Recipe because of invalid ingredient or output: {} -> {}x{}", ingredientsWithAmount, outnumber, output);
      }
   }

   public void setValueBefore(Object something, Comparable value) {
      if (something != null) {
         if (this.fixValueBeforeInherit.containsKey(something)) {
            PECore.debugLog("Overwriting fixValueBeforeInherit for {} from: {} to {}", something, this.fixValueBeforeInherit.get(something), value);
         }

         this.fixValueBeforeInherit.put(something, value);
         this.fixValueAfterInherit.remove(something);
      }
   }

   public void setValueAfter(Object something, Comparable value) {
      if (something != null) {
         if (this.fixValueAfterInherit.containsKey(something)) {
            PECore.debugLog("Overwriting fixValueAfterInherit for {} from: {} to {}", something, this.fixValueAfterInherit.get(something), value);
         }

         this.fixValueAfterInherit.put(something, value);
      }
   }

   public void setValueFromConversion(int outnumber, Object something, Map ingredientsWithAmount) {
      if (something != null && !ingredientsWithAmount.containsKey((Object)null)) {
         if (outnumber <= 0) {
            throw new IllegalArgumentException("outnumber has to be > 0!");
         } else {
            Conversion conversion = new Conversion(something, outnumber, ingredientsWithAmount, this.arithmetic);
            if (this.overwriteConversion.containsKey(something)) {
               Conversion oldConversion = (Conversion)this.overwriteConversion.get(something);
               PECore.debugLog("Overwriting setValueFromConversion {} with {}", this.overwriteConversion.get(something), conversion);
               Iterator var6 = oldConversion.ingredientsWithAmount.keySet().iterator();

               while(var6.hasNext()) {
                  Object ingredient = var6.next();
                  this.getUsesFor(ingredient).remove(oldConversion);
               }
            }

            this.addConversionToIngredientUsages(conversion);
            this.overwriteConversion.put(something, conversion);
         }
      } else {
         PECore.debugLog("Ignoring setValueFromConversion because of invalid ingredient or output: {} -> {}x{}", ingredientsWithAmount, outnumber, something);
      }
   }

   protected class Conversion {
      public final Object output;
      public final int outnumber;
      public final Comparable value;
      public final Map ingredientsWithAmount;
      public final IValueArithmetic arithmeticForConversion;

      Conversion(Object output, int outnumber, Map ingredientsWithAmount, IValueArithmetic arithmeticForConversion) {
         this(output, outnumber, ingredientsWithAmount, arithmeticForConversion, MappingCollector.this.arithmetic.getZero());
      }

      Conversion(Object output, int outnumber, Map ingredientsWithAmount, IValueArithmetic arithmeticForConversion, Comparable value) {
         this.output = output;
         this.outnumber = outnumber;
         if (ingredientsWithAmount != null && !ingredientsWithAmount.isEmpty()) {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            Iterator var8 = ingredientsWithAmount.entrySet().iterator();

            while(var8.hasNext()) {
               Map.Entry ingredient = (Map.Entry)var8.next();
               Integer amount = (Integer)ingredient.getValue();
               if (amount == null) {
                  throw new IllegalArgumentException("ingredient amount value has to be != null");
               }

               if (amount != 0) {
                  builder.put(ingredient.getKey(), amount);
               }
            }

            this.ingredientsWithAmount = builder.build();
         } else {
            this.ingredientsWithAmount = Collections.emptyMap();
         }

         this.arithmeticForConversion = arithmeticForConversion;
         this.value = value;
      }

      public String toString() {
         Comparable var10000 = this.value;
         return "" + var10000 + " + " + this.ingredientsToString() + " => " + this.outnumber + "*" + this.output;
      }

      private String ingredientsToString() {
         return this.ingredientsWithAmount.isEmpty() ? "nothing" : (String)this.ingredientsWithAmount.entrySet().stream().map((e) -> {
            Object var10000 = e.getValue();
            return "" + var10000 + "*" + e.getKey();
         }).collect(Collectors.joining(" + "));
      }

      public boolean equals(Object o) {
         boolean var10000;
         if (o instanceof Conversion other) {
            if (Objects.equals(this.output, other.output) && Objects.equals(this.value, other.value) && Objects.equals(this.ingredientsWithAmount, other.ingredientsWithAmount)) {
               var10000 = true;
               return var10000;
            }
         }

         var10000 = false;
         return var10000;
      }

      public int hashCode() {
         return Objects.hash(new Object[]{this.output, this.value, this.ingredientsWithAmount});
      }
   }
}
