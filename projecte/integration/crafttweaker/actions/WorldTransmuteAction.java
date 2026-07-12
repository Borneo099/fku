package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import com.blamejared.crafttweaker.natives.block.ExpandBlockState;
import moze_intel.projecte.utils.WorldTransmutations;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class WorldTransmuteAction implements IUndoableAction {
   protected final BlockState input;
   protected final BlockState output;
   protected final @Nullable BlockState sneakOutput;

   private WorldTransmuteAction(BlockState input, BlockState output, @Nullable BlockState sneakOutput) {
      this.input = input;
      this.output = output;
      this.sneakOutput = sneakOutput;
   }

   protected void apply(boolean add) {
      if (add) {
         WorldTransmutations.register(this.input, this.output, this.sneakOutput);
      } else {
         WorldTransmutations.getWorldTransmutations().removeIf((entry) -> {
            BlockState altOutput = this.sneakOutput == null ? this.output : this.sneakOutput;
            return entry.origin() == this.input && entry.result() == this.output && entry.altResult() == altOutput;
         });
      }

   }

   public static class RemoveAll implements IUndoableAction {
      public void apply() {
         WorldTransmutations.getWorldTransmutations().clear();
      }

      public String describe() {
         return "Removing all world transmutation recipes";
      }

      public void undo() {
         WorldTransmutations.resetWorldTransmutations();
      }

      public String describeUndo() {
         return "Restored world transmutation recipes to default";
      }

      public String systemName() {
         return "ProjectE";
      }
   }

   public static class Remove extends WorldTransmuteAction {
      public Remove(BlockState input, BlockState output, @Nullable BlockState sneakOutput) {
         super(input, output, sneakOutput);
      }

      public void apply() {
         this.apply(false);
      }

      public String describe() {
         String var10000;
         if (this.sneakOutput == null) {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Removing world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output);
         } else {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Removing world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output) + " and secondary output: " + ExpandBlockState.getCommandString(this.sneakOutput);
         }
      }

      public void undo() {
         this.apply(true);
      }

      public String describeUndo() {
         String var10000;
         if (this.sneakOutput == null) {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Undoing removal of world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output);
         } else {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Undoing removal of world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output) + " and secondary output: " + ExpandBlockState.getCommandString(this.sneakOutput);
         }
      }

      public String systemName() {
         return "ProjectE";
      }
   }

   public static class Add extends WorldTransmuteAction {
      public Add(BlockState input, BlockState output, @Nullable BlockState sneakOutput) {
         super(input, output, sneakOutput);
      }

      public void apply() {
         this.apply(true);
      }

      public String describe() {
         String var10000;
         if (this.sneakOutput == null) {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Adding world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output);
         } else {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Adding world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output) + " and secondary output: " + ExpandBlockState.getCommandString(this.sneakOutput);
         }
      }

      public void undo() {
         this.apply(false);
      }

      public String describeUndo() {
         String var10000;
         if (this.sneakOutput == null) {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Undoing addition of world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output);
         } else {
            var10000 = ExpandBlockState.getCommandString(this.input);
            return "Undoing addition of world transmutation recipe for: " + var10000 + " with output: " + ExpandBlockState.getCommandString(this.output) + " and secondary output: " + ExpandBlockState.getCommandString(this.sneakOutput);
         }
      }

      public String systemName() {
         return "ProjectE";
      }
   }
}
