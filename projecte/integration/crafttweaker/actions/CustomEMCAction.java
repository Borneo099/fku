package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTCustomEMCMapper;
import moze_intel.projecte.utils.Constants;
import org.jetbrains.annotations.NotNull;

public class CustomEMCAction implements IUndoableAction {
   private final @NotNull NormalizedSimpleStack stack;
   private final long emc;

   public CustomEMCAction(@NotNull NormalizedSimpleStack stack, long emc) {
      this.stack = stack;
      this.emc = emc;
   }

   public void apply() {
      CrTCustomEMCMapper.registerCustomEMC(this.stack, this.emc);
   }

   public String describe() {
      String var10000 = Constants.EMC_FORMATTER.format(this.emc);
      return "Registered emc value of '" + var10000 + "' for: " + this.stack;
   }

   public void undo() {
      CrTCustomEMCMapper.unregisterNSS(this.stack);
   }

   public String describeUndo() {
      return "Undoing emc registration for: " + this.stack;
   }

   public String systemName() {
      return "ProjectE";
   }
}
