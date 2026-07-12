package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusCollisionShapeEvent extends Event {
   private VoxelShape collisionShape;

   public VoxelShape getCollisionShape() {
      return this.collisionShape;
   }

   public void setCollisionShape(VoxelShape shape) {
      this.collisionShape = shape;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         CactusCollisionShapeListener listener = (CactusCollisionShapeListener)var2.next();
         listener.onCactusCollisionShape(this);
      }

   }

   public Class getListenerType() {
      return CactusCollisionShapeListener.class;
   }
}
