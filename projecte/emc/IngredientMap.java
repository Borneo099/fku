package moze_intel.projecte.emc;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;

public class IngredientMap {
   private final Map ingredients = new HashMap();

   public void addIngredient(Object thing, int amount) {
      this.ingredients.merge(thing, amount, Integer::sum);
   }

   public Map getMap() {
      return Maps.newHashMap(this.ingredients);
   }

   public String toString() {
      return this.ingredients.toString();
   }
}
