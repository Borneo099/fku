package lexis.Client.Goto;

import net.minecraft.core.BlockPos;

public class PathPos extends BlockPos {
   private final boolean jumping;

   public PathPos(BlockPos pos) {
      this(pos, false);
   }

   public PathPos(BlockPos pos, boolean jumping) {
      super(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
      this.jumping = jumping;
   }

   public boolean isJumping() {
      return this.jumping;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof PathPos)) {
         return false;
      } else {
         PathPos node = (PathPos)obj;
         return this.m_123341_() == node.m_123341_() && this.m_123342_() == node.m_123342_() && this.m_123343_() == node.m_123343_() && this.jumping == node.jumping;
      }
   }

   public int hashCode() {
      return super.hashCode() * 2 + (this.jumping ? 1 : 0);
   }
}
