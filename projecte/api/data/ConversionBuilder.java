package moze_intel.projecte.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public class ConversionBuilder implements ConversionBuilderNSSHelper {
   private final Map ingredients = new LinkedHashMap();
   private final NormalizedSimpleStack output;
   private final int outputAmount;
   private boolean propagateTags;

   ConversionBuilder(@NotNull NormalizedSimpleStack output, int outputAmount) {
      this.output = output;
      this.outputAmount = outputAmount;
   }

   public String toString() {
      return this.output + " " + this.outputAmount;
   }

   private ConversionBuilder getThis() {
      return this;
   }

   public ConversionBuilder propagateTags() {
      if (this.propagateTags) {
         throw new RuntimeException("Propagate tags has already been set, remove unnecessary call.");
      } else {
         NormalizedSimpleStack var2 = this.output;
         if (var2 instanceof NSSTag) {
            NSSTag nssTag = (NSSTag)var2;
            if (!nssTag.representsTag()) {
               throw new RuntimeException("Propagate tags can only be enabled for conversion outputs that are tags.");
            }
         }

         this.propagateTags = true;
         return this.getThis();
      }
   }

   public ConversionBuilder ingredient(@NotNull NormalizedSimpleStack input, int amount) {
      if (this.ingredients.containsKey(input)) {
         throw new RuntimeException("Conversion already contains ingredient '" + input + "', merge identical ingredients.");
      } else if (amount == 0) {
         throw new RuntimeException("Conversion for empty ingredient '" + input + "' should be removed.");
      } else {
         this.ingredients.put(input, amount);
         return this.getThis();
      }
   }

   protected void validateIngredients() {
      if (this.ingredients.isEmpty()) {
         throw new RuntimeException("Conversion does not contain any ingredients.");
      }
   }

   JsonObject serialize() {
      this.validateIngredients();
      JsonObject json = new JsonObject();
      if (this.propagateTags) {
         json.addProperty("propagateTags", true);
      }

      json.addProperty("output", this.output.json());
      if (this.outputAmount != 1) {
         json.addProperty("count", this.outputAmount);
      }

      Iterator var3;
      if (this.ingredients.values().stream().allMatch((value) -> {
         return value == 1;
      })) {
         JsonArray jsonIngredients = new JsonArray();
         var3 = this.ingredients.keySet().iterator();

         while(var3.hasNext()) {
            NormalizedSimpleStack stack = (NormalizedSimpleStack)var3.next();
            jsonIngredients.add(stack.json());
         }

         json.add("ingredients", jsonIngredients);
      } else {
         JsonObject jsonIngredients = new JsonObject();
         var3 = this.ingredients.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            jsonIngredients.addProperty(((NormalizedSimpleStack)entry.getKey()).json(), (Number)entry.getValue());
         }

         json.add("ingredients", jsonIngredients);
      }

      return json;
   }
}
