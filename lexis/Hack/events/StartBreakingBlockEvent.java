package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class StartBreakingBlockEvent extends CancellableEvent {
   private static final StartBreakingBlockEvent INSTANCE = new StartBreakingBlockEvent();
   public BlockPos blockPos;
   public Direction direction;

   private StartBreakingBlockEvent() {
   }

   public static StartBreakingBlockEvent get(BlockPos blockPos, Direction direction) {
      INSTANCE.blockPos = blockPos;
      INSTANCE.direction = direction;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         StartBreakingBlockListener listener = (StartBreakingBlockListener)var2.next();
         listener.onStartBreakingBlock(this);
         if (this.isCancelled()) {
            break;
         }
      }

   }

   public Class getListenerType() {
      return StartBreakingBlockListener.class;
   }
}
