package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.entity.Entity;

public class VelocityFromFluidEvent extends CancellableEvent {
   private final Entity entity;

   public VelocityFromFluidEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         VelocityFromFluidListener listener = (VelocityFromFluidListener)var2.next();
         listener.onVelocityFromFluid(this);
         if (this.isCancelled()) {
            break;
         }
      }

   }

   public Class getListenerType() {
      return VelocityFromFluidListener.class;
   }
}
