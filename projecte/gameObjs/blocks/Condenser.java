package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

public class Condenser extends AlchemicalChest {
   public Condenser(BlockBehaviour.Properties props) {
      super(props);
   }

   public @Nullable BlockEntityTypeRegistryObject getType() {
      return PEBlockEntityTypes.CONDENSER;
   }
}
