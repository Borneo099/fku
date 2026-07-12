package lexis.mixin.accessor;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({WalkAnimationState.class})
public interface WalkAnimationStateAccessor {
   @Accessor("speedOld")
   void setSpeedOld(float speedOld);

   @Accessor("speed")
   void setSpeed(float speed);

   @Accessor("position")
   void setPosition(float position);

   @Accessor("speedOld")
   float getSpeedOld();

   @Accessor("speed")
   float getSpeed();

   @Accessor("position")
   float getPosition();
}
