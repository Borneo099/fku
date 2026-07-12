package moze_intel.projecte.api.block_entity;

import net.minecraft.world.phys.AABB;

public interface IDMPedestal {
   int getActivityCooldown();

   void setActivityCooldown(int var1);

   void decrementActivityCooldown();

   AABB getEffectBounds();
}
