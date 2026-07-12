package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockBreakingProgressEvent extends Event {
   private final BlockPos blockPos;
   private final Direction direction;

   public BlockBreakingProgressEvent(BlockPos blockPos, Direction direction) {
      this.blockPos = blockPos;
      this.direction = direction;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         BlockBreakingProgressListener listener = (BlockBreakingProgressListener)var2.next();
         listener.onBlockBreakingProgress(this);
      }

   }

   public Class getListenerType() {
      return BlockBreakingProgressListener.class;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public Direction getDirection() {
      return this.direction;
   }
}
