package lexis.Hack.Hackutil.music;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "lexis",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class DynamicIslandOverlay {
   public static final IGuiOverlay HUD_ISLAND = (gui, gfx, partialTick, w, h) -> {
      DynamicIsland.render(gfx);
   };

   @SubscribeEvent
   public static void register(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("dynamic_island", HUD_ISLAND);
   }
}
