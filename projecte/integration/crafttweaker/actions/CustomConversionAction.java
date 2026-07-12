package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTConversionEMCMapper;

public class CustomConversionAction implements IUndoableAction {
   private final CrTConversionEMCMapper.CrTConversion conversion;

   public CustomConversionAction(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map ingredients) {
      this.conversion = new CrTConversionEMCMapper.CrTConversion(output, amount, propagateTags, set, ingredients);
   }

   public void apply() {
      CrTConversionEMCMapper.addConversion(this.conversion);
   }

   public String describe() {
      StringBuilder inputString = new StringBuilder();

      Map.Entry entry;
      for(Iterator var2 = this.conversion.ingredients().entrySet().iterator(); var2.hasNext(); inputString.append(entry.getKey())) {
         entry = (Map.Entry)var2.next();
         if (!inputString.isEmpty()) {
            inputString.append(", ");
         }

         int amount = (Integer)entry.getValue();
         if (amount > 1) {
            inputString.append(amount).append(" ");
         }
      }

      int var10000 = this.conversion.amount();
      String description = "Added custom conversion creating '" + var10000 + "' of " + this.conversion.output() + ", from: " + inputString;
      if (this.conversion.propagateTags() && this.conversion.output() instanceof NSSTag) {
         description = description + "; propagating to elements of " + this.conversion.output();
      }

      return description;
   }

   public void undo() {
      CrTConversionEMCMapper.removeConversion(this.conversion);
   }

   public String describeUndo() {
      int var10000 = this.conversion.amount();
      return "Undoing adding of custom conversion creating '" + var10000 + "' of " + this.conversion.output();
   }

   public String systemName() {
      return "ProjectE";
   }
}
