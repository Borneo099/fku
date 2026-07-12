package lexis.Hack.events;

import net.minecraft.world.entity.Entity;

public interface IEntityRenderState {
   Entity getEntity();

   void setEntity(Entity entity);
}
