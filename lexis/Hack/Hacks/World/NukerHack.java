package lexis.Hack.Hacks.World;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BlockBreaker;
import lexis.Hack.Utils.BlockUtils;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class NukerHack extends Hack {
   private double range = 5.0;
   private boolean flatMode = false;
   private boolean lookAtBlock = true;
   private boolean superSpeedMode = false;
   private int breakSpeed = 5;
   private boolean swingAnimation = true;
   private static BlockPos currentTarget = null;
   private HackConfig config;

   public NukerHack() {
      super("Nuker", "范围挖掘", Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("范围", "挖掘范围", 5.0, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("平地模式", "只挖掘脚下 > 以上的方块", false));
      this.addSetting(new Hack.Setting("看向方块", "看向目标方块", true));
      this.addSetting(new Hack.Setting("超速破坏", "超速破坏模式(网易opmod会检测 和 发包很多)", false));
      this.addSetting(new Hack.Setting("破坏速度", "普通模式每tick破坏的方块数", 5, 1, 50, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("撸手动画", "破坏时是否显示挥动手臂动画", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("Nuker", "范围", 5.0);
      this.flatMode = this.config.getBooleanSetting("Nuker", "平地模式", false);
      this.lookAtBlock = this.config.getBooleanSetting("Nuker", "看向方块", true);
      this.superSpeedMode = this.config.getBooleanSetting("Nuker", "超速破坏", false);
      this.breakSpeed = (int)this.config.getDoubleSetting("Nuker", "破坏速度", 5.0);
      this.swingAnimation = this.config.getBooleanSetting("Nuker", "撸手动画", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "范围":
               setting.setValue(this.range);
               break;
            case "平地模式":
               setting.setValue(this.flatMode);
               break;
            case "看向方块":
               setting.setValue(this.lookAtBlock);
               break;
            case "超速破坏":
               setting.setValue(this.superSpeedMode);
               break;
            case "破坏速度":
               setting.setValue((double)this.breakSpeed);
               break;
            case "撸手动画":
               setting.setValue(this.swingAnimation);
         }
      }

   }

   public void onEnable() {
   }

   public void onDisable() {
      if (this.lookAtBlock) {
         HeadOnlyLook.stopLooking();
      }

      currentTarget = null;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "范围":
                  double newRange = setting.getDouble();
                  if (newRange != this.range) {
                     this.range = newRange;
                     needSave = true;
                  }
                  break;
               case "平地模式":
                  boolean newFlat = setting.getBoolean();
                  if (newFlat != this.flatMode) {
                     this.flatMode = newFlat;
                     needSave = true;
                  }
                  break;
               case "看向方块":
                  boolean newLook = setting.getBoolean();
                  if (newLook != this.lookAtBlock) {
                     this.lookAtBlock = newLook;
                     needSave = true;
                  }
                  break;
               case "超速破坏":
                  boolean newSuper = setting.getBoolean();
                  if (newSuper != this.superSpeedMode) {
                     this.superSpeedMode = newSuper;
                     needSave = true;
                  }
                  break;
               case "破坏速度":
                  int newSpeed = (int)setting.getDouble();
                  if (newSpeed != this.breakSpeed) {
                     this.breakSpeed = newSpeed;
                     needSave = true;
                  }
                  break;
               case "撸手动画":
                  boolean newSwing = setting.getBoolean();
                  if (newSwing != this.swingAnimation) {
                     this.swingAnimation = newSwing;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("Nuker", this.getSettings());
         }

         Vec3 eyesPos = mc.f_91074_.m_146892_();
         BlockPos eyesBlock = BlockPos.m_274446_(eyesPos);
         double rangeSq = this.range * this.range;
         int blockRange = (int)Math.ceil(this.range);
         List allBlocks = BlockUtils.getAllInBox(eyesBlock, blockRange);
         List targets = (List)allBlocks.stream().filter((pos) -> {
            return eyesPos.m_82557_(Vec3.m_82512_(pos)) <= rangeSq;
         }).filter(BlockUtils::canBeClicked).filter(this::isValidBlock).sorted(Comparator.comparingDouble((pos) -> {
            return eyesPos.m_82557_(Vec3.m_82512_(pos));
         })).collect(Collectors.toList());
         if (targets.isEmpty()) {
            currentTarget = null;
         } else {
            BlockPos first;
            if (this.superSpeedMode) {
               Iterator var18 = targets.iterator();

               while(var18.hasNext()) {
                  BlockPos target = (BlockPos)var18.next();
                  BlockBreaker.breakOneBlock(target, this.swingAnimation);
               }

               if (this.lookAtBlock) {
                  first = (BlockPos)targets.get(0);
                  if (currentTarget == null || !currentTarget.equals(first)) {
                     currentTarget = first;
                     HeadOnlyLook.startLookingAt(currentTarget);
                  }
               }
            } else {
               first = (BlockPos)targets.get(0);
               if (this.lookAtBlock && (currentTarget == null || !currentTarget.equals(first))) {
                  currentTarget = first;
                  HeadOnlyLook.startLookingAt(currentTarget);
               }

               int destroyed = 0;
               Iterator var22 = targets.iterator();

               while(var22.hasNext()) {
                  BlockPos target = (BlockPos)var22.next();
                  if (destroyed >= this.breakSpeed) {
                     break;
                  }

                  if (BlockBreaker.breakOneBlock(target, this.swingAnimation)) {
                     ++destroyed;
                  }
               }
            }

         }
      }
   }

   private boolean isValidBlock(BlockPos pos) {
      if (this.flatMode) {
         return (double)pos.m_123342_() >= mc.f_91074_.m_20186_();
      } else {
         return true;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static BlockPos getCurrentTarget() {
      return currentTarget;
   }
}
