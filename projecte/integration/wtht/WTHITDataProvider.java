package moze_intel.projecte.integration.wtht;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.world.level.ItemLike;

public class WTHITDataProvider implements IBlockComponentProvider {
   static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

   public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      if (ProjectEConfig.server.misc.hwylaTOPDisplay.get()) {
         long value = EMCHelper.getEmcValue((ItemLike)accessor.getBlock());
         if (value > 0L) {
            tooltip.addLine(EMCHelper.getEmcTextComponent(value, 1));
         }
      }

   }
}
