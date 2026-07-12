package moze_intel.projecte.gameObjs.blocks;

import moze_intel.projecte.gameObjs.EnumMatterType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MatterBlock extends Block implements IMatterBlock {
   public final EnumMatterType matterType;

   public MatterBlock(BlockBehaviour.Properties props, EnumMatterType type) {
      super(props);
      this.matterType = type;
   }

   public EnumMatterType getMatterType() {
      return this.matterType;
   }
}
