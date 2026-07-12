package moze_intel.projecte.emc.mappers.customConversions.json;

import java.util.HashMap;
import java.util.Map;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

public class CustomConversion {
   public int count = 1;
   public NormalizedSimpleStack output;
   public Map ingredients;
   public transient boolean propagateTags = false;

   public static CustomConversion getFor(int count, NormalizedSimpleStack output, Map ingredients) {
      CustomConversion conversion = new CustomConversion();
      conversion.count = count;
      conversion.output = output;
      conversion.ingredients = new HashMap();
      conversion.ingredients.putAll(ingredients);
      return conversion;
   }

   public String toString() {
      int var10000 = this.count;
      return "{" + var10000 + " * " + this.output + " = " + this.ingredients.toString() + "}";
   }
}
