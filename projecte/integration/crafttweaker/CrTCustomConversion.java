package moze_intel.projecte.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.actions.CustomConversionAction;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/ProjectE/CustomConversion")
@Name("mods.projecte.CustomConversion")
public class CrTCustomConversion {
   private CrTCustomConversion() {
   }

   @Method
   public static void addConversion(NormalizedSimpleStack stack, int amount, Map ingredients) {
      addConversion(stack, amount, stack instanceof NSSTag, ingredients);
   }

   @Method
   public static void addConversion(NormalizedSimpleStack stack, int amount, boolean propagateTags, Map ingredients) {
      if (propagateTags && !(stack instanceof NSSTag)) {
         throw new IllegalArgumentException("Propagate Tags should always be false if the output is not a tag.");
      } else {
         CraftTweakerAPI.apply(new CustomConversionAction(stack, amount, propagateTags, false, ingredients));
      }
   }

   @Method
   public static void addConversion(NormalizedSimpleStack stack, int amount, NormalizedSimpleStack... ingredients) {
      addConversion(stack, amount, stack instanceof NSSTag, ingredients);
   }

   @Method
   public static void addConversion(NormalizedSimpleStack stack, int amount, boolean propagateTags, NormalizedSimpleStack... ingredients) {
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("No ingredients specified for conversion.");
      } else {
         addConversion(stack, amount, propagateTags, (Map)Arrays.stream(ingredients).collect(Collectors.toMap((ingredient) -> {
            return ingredient;
         }, (ingredient) -> {
            return 1;
         }, Integer::sum)));
      }
   }

   @Method
   public static void setConversion(NormalizedSimpleStack stack, int amount, Map ingredients) {
      setConversion(stack, amount, stack instanceof NSSTag, ingredients);
   }

   @Method
   public static void setConversion(NormalizedSimpleStack stack, int amount, boolean propagateTags, Map ingredients) {
      if (propagateTags && !(stack instanceof NSSTag)) {
         throw new IllegalArgumentException("Propagate Tags should always be false if the output is not a tag.");
      } else {
         CraftTweakerAPI.apply(new CustomConversionAction(stack, amount, propagateTags, true, ingredients));
      }
   }

   @Method
   public static void setConversion(NormalizedSimpleStack stack, int amount, NormalizedSimpleStack... ingredients) {
      setConversion(stack, amount, stack instanceof NSSTag, ingredients);
   }

   @Method
   public static void setConversion(NormalizedSimpleStack stack, int amount, boolean propagateTags, NormalizedSimpleStack... ingredients) {
      if (ingredients.length == 0) {
         throw new IllegalArgumentException("No ingredients specified for conversion.");
      } else {
         setConversion(stack, amount, propagateTags, (Map)Arrays.stream(ingredients).collect(Collectors.toMap((ingredient) -> {
            return ingredient;
         }, (ingredient) -> {
            return 1;
         }, Integer::sum)));
      }
   }
}
