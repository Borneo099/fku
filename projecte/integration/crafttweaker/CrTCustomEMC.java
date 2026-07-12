package moze_intel.projecte.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.actions.CustomEMCAction;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/ProjectE/CustomEMC")
@Name("mods.projecte.CustomEMC")
public class CrTCustomEMC {
   private CrTCustomEMC() {
   }

   @Method
   public static void setEMCValue(NormalizedSimpleStack stack, long emc) {
      if (emc < 0L) {
         throw new IllegalArgumentException("EMC cannot be set to a negative number. Was set to: " + emc);
      } else {
         CraftTweakerAPI.apply(new CustomEMCAction(stack, emc));
      }
   }

   @Method
   public static void removeEMCValue(NormalizedSimpleStack stack) {
      CraftTweakerAPI.apply(new CustomEMCAction(stack, 0L));
   }
}
