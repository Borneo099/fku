package lexis.Hack.Hacks.Blocks;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AirPlaceHack extends Hack {
   private double range = 5.0;
   private boolean showRender = true;
   private BlockPos currentTarget = null;
   private Direction currentSide;
   private HackConfig config;
   private static boolean isPlacing = false;

   public AirPlaceHack() {
      super("空气放置", "在空气中放置方块", Hack.Category.BLOCKS, true);
      this.currentSide = Direction.UP;
      this.addSetting(new Hack.Setting("范围", "放置范围", 5.0, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("显示渲染", "显示放置位置", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("空气放置", "范围", 5.0);
      this.showRender = this.config.getBooleanSetting("空气放置", "显示渲染", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "范围":
               setting.setValue(this.range);
               break;
            case "显示渲染":
               setting.setValue(this.showRender);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
      this.currentTarget = null;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "范围":
                  this.range = setting.getDouble();
                  break;
               case "显示渲染":
                  this.showRender = setting.getBoolean();
            }
         }

         Vec3 eyePos = mc.f_91074_.m_146892_();
         Vec3 lookVec = mc.f_91074_.m_20154_();
         Vec3 targetPos = eyePos.m_82549_(lookVec.m_82490_(this.range));
         this.currentTarget = BlockPos.m_274446_(targetPos);
         double dx = targetPos.f_82479_ - (double)this.currentTarget.m_123341_() - 0.5;
         double dy = targetPos.f_82480_ - (double)this.currentTarget.m_123342_() - 0.5;
         double dz = targetPos.f_82481_ - (double)this.currentTarget.m_123343_() - 0.5;
         double absX = Math.abs(dx);
         double absY = Math.abs(dy);
         double absZ = Math.abs(dz);
         if (absY > absX && absY > absZ) {
            this.currentSide = dy > 0.0 ? Direction.UP : Direction.DOWN;
         } else if (absX > absZ) {
            this.currentSide = dx > 0.0 ? Direction.EAST : Direction.WEST;
         } else {
            this.currentSide = dz > 0.0 ? Direction.SOUTH : Direction.NORTH;
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public void onRightClick() {
      if (!isPlacing) {
         if (this.isEnabled() && mc.f_91074_ != null && this.currentTarget != null) {
            isPlacing = true;

            try {
               BlockHitResult hitResult = new BlockHitResult(Vec3.m_82512_(this.currentTarget), this.currentSide, this.currentTarget, false);
               mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult);
               mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
            } finally {
               isPlacing = false;
            }

         }
      }
   }

   public BlockPos getCurrentTarget() {
      return this.currentTarget;
   }

   public Direction getCurrentSide() {
      return this.currentSide;
   }

   public boolean shouldRender() {
      return this.isEnabled() && this.showRender && this.currentTarget != null;
   }
}
