package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class PlaceBlockEvent extends CancellableEvent {
   private static final PlaceBlockEvent INSTANCE = new PlaceBlockEvent();
   public BlockPos blockPos;
   public Block block;

   public static PlaceBlockEvent get(BlockPos blockPos, Block block) {
      INSTANCE.setCancelled(false);
      INSTANCE.blockPos = blockPos;
      INSTANCE.block = block;
      return INSTANCE;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         PlaceBlockListener listener = (PlaceBlockListener)var2.next();
         listener.onPlaceBlock(this);
         if (this.isCancelled()) {
            break;
         }
      }

   }

   public Class getListenerType() {
      return PlaceBlockListener.class;
   }
}
