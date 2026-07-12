package moze_intel.projecte.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import moze_intel.projecte.integration.crafttweaker.actions.WorldTransmuteAction;
import net.minecraft.world.level.block.state.BlockState;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;

@ZenRegister
@Document("mods/ProjectE/WorldTransmutation")
@Name("mods.projecte.WorldTransmutation")
public class WorldTransmutation {
   private WorldTransmutation() {
   }

   @Method
   public static void add(BlockState input, BlockState output, @Optional BlockState sneakOutput) {
      CraftTweakerAPI.apply(new WorldTransmuteAction.Add(input, output, sneakOutput));
   }

   @Method
   public static void remove(BlockState input, BlockState output, @Optional BlockState sneakOutput) {
      CraftTweakerAPI.apply(new WorldTransmuteAction.Remove(input, output, sneakOutput));
   }

   @Method
   public static void removeAll() {
      CraftTweakerAPI.apply(new WorldTransmuteAction.RemoveAll());
   }
}
