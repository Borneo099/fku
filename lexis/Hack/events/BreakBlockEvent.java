package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;

public class BreakBlockEvent extends CancellableEvent {
   private static final BreakBlockEvent INSTANCE = new BreakBlockEvent();
   public BlockPos blockPos;

   public static BreakBlockEvent get(BlockPos blockPos) {
      INSTANCE.setCancelled(false);
      INSTANCE.blockPos = blockPos;
      return INSTANCE;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         BreakBlockListener listener = (BreakBlockListener)var2.next();
         listener.onBreakBlock(this);
         if (this.isCancelled()) {
            break;
         }
      }

   }

   public Class getListenerType() {
      return BreakBlockListener.class;
   }
}
