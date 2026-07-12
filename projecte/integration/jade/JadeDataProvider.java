package moze_intel.projecte.integration.jade;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeDataProvider implements IBlockComponentProvider {
   static final JadeDataProvider INSTANCE = new JadeDataProvider();

   public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
      if (ProjectEConfig.server.misc.hwylaTOPDisplay.get()) {
         long value = EMCHelper.getEmcValue((ItemLike)accessor.getBlock());
         if (value > 0L) {
            tooltip.add(EMCHelper.getEmcTextComponent(value, 1));
         }
      }

   }

   public ResourceLocation getUid() {
      return PEJadeConstants.EMC_PROVIDER;
   }
}
