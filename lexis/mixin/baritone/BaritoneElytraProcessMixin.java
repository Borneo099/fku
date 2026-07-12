package lexis.mixin.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import lexis.Hack.Hacks.Baritone.ElytraAnywhereHack;
import lexis.Hack.Utils.BaritoneBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   targets = {"baritone.dz"},
   remap = false
)
public class BaritoneElytraProcessMixin {
   @Shadow
   private native void a(BlockPos pos, boolean flag);

   @Overwrite(
      remap = false
   )
   public final void pathTo(Goal goal) {
      int x;
      int y;
      int z;
      if (goal instanceof GoalXZ gxz) {
         x = gxz.getX();
         y = 64;
         z = gxz.getZ();
      } else {
         if (!(goal instanceof GoalBlock)) {
            throw new IllegalArgumentException("The goal must be a GoalXZ or GoalBlock");
         }

         GoalBlock gb = (GoalBlock)goal;
         x = gb.x;
         y = gb.y;
         z = gb.z;
      }

      if (ElytraAnywhereHack.enabled) {
         if (y < 1) {
            y = 1;
         }

         if (y > 127) {
            y = 127;
         }

         ElytraAnywhereHack.setGoal(x, y, z);
      } else if (y <= 0 || y >= 128) {
         throw new IllegalArgumentException("The y of the goal is not between 0 and 128");
      }

      this.a(new BlockPos(x, y, z), false);
   }

   @Overwrite(
      remap = false
   )
   public final void pathTo(BlockPos pos) {
      if (ElytraAnywhereHack.enabled) {
         int x = pos.m_123341_();
         int y = Math.max(1, Math.min(127, pos.m_123342_()));
         int z = pos.m_123343_();
         ElytraAnywhereHack.setGoal(x, y, z);
         this.a(new BlockPos(x, y, z), false);
      } else {
         this.a(pos, false);
      }

   }

   @Inject(
      method = {"onTick"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private void onOnTickHead(boolean tick, boolean isStart, CallbackInfoReturnable cir) {
      if (ElytraAnywhereHack.enabled) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            if (!mc.f_91074_.m_6844_(EquipmentSlot.CHEST).m_150930_(Items.f_42741_)) {
               ElytraAnywhereHack.emergencyStop();
               BaritoneBridge.stop();
               cir.setReturnValue(new PathingCommand((Goal)null, PathingCommandType.REQUEST_PAUSE));
            }

         }
      }
   }

   @Inject(
      method = {"onTick"},
      at = {@At(
   value = "CONSTANT",
   args = {"stringValue=Path complete, picking a nearby safe landing spot..."}
)},
      cancellable = true,
      remap = false
   )
   private void beforeLandingScan(boolean tick, boolean isStart, CallbackInfoReturnable cir) {
      if (ElytraAnywhereHack.enabled) {
         BaritoneBridge.stop();
         ElytraAnywhereHack.emergencyStop();
         cir.setReturnValue(new PathingCommand((Goal)null, PathingCommandType.CANCEL_AND_SET_GOAL));
      }

   }

   @Inject(
      method = {"onTick"},
      at = {@At(
   value = "CONSTANT",
   args = {"stringValue=Emergency landing - almost out of elytra durability or fireworks"}
)},
      cancellable = true,
      remap = false
   )
   private void beforeEmergencyLanding(boolean tick, boolean isStart, CallbackInfoReturnable cir) {
      if (ElytraAnywhereHack.enabled) {
         BaritoneBridge.stop();
         ElytraAnywhereHack.emergencyStop();
         cir.setReturnValue(new PathingCommand((Goal)null, PathingCommandType.CANCEL_AND_SET_GOAL));
      }

   }

   @ModifyConstant(
      method = {"onTick"},
      constant = {@Constant(
   intValue = 128
)},
      remap = false
   )
   private int bypassOnTickMaxY(int original) {
      return ElytraAnywhereHack.enabled ? Integer.MAX_VALUE : original;
   }
}
