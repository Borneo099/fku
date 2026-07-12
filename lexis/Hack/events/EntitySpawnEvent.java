package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.entity.Entity;

public class EntitySpawnEvent extends Event {
   private final Entity entity;

   public EntitySpawnEvent(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         EntitySpawnListener listener = (EntitySpawnListener)var2.next();
         listener.onEntitySpawn(this);
      }

   }

   public Class getListenerType() {
      return EntitySpawnListener.class;
   }
}
