package lexis.Hack.Hacks.Fun;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.Options;
import net.minecraft.world.item.Items;

public class HelicopterElytraHack extends Hack {
   private static final String CONFIG_KEY = "直升机老大鞘翹";
   private HackConfig config;
   private double yawSpeed = 15.0;
   private boolean preventFall = true;
   private boolean freeMove = true;
   private double moveSpeed = 0.5;
   private double verticalSpeed = 0.4;
   private LookMode lookMode;
   private float currentYaw;
   private double lockedY;

   public HelicopterElytraHack() {
      super("直升机老大鞘翹", new String[]{"哇！太好了 是直升机老大！"}, Hack.Category.FUN, true);
      this.lookMode = HelicopterElytraHack.LookMode.MID;
      this.currentYaw = 0.0F;
      this.lockedY = Double.NaN;
      this.addSetting(new Hack.Setting("转头速度", "头部旋转速度", 15.0, 5.0, 90.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("防下落", "取消下落", true));
      this.addSetting(new Hack.Setting("自由移动", "WASD 移动 / 空格上升 / Shift 下降", true));
      this.addSetting(new Hack.Setting("移动速度", "WASD 移动速度", 0.5, 0.1, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("上下速度", "空格/Shift 升降速度", 0.4, 0.05, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("转头模式", "头部角度？", "中转头", new String[]{"上转头", "中转头", "下转头"}));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.yawSpeed = this.config.getDoubleSetting("直升机老大鞘翹", "转头速度", 15.0);
      this.preventFall = this.config.getBooleanSetting("直升机老大鞘翹", "防下落", true);
      this.freeMove = this.config.getBooleanSetting("直升机老大鞘翹", "自由移动", true);
      this.moveSpeed = this.config.getDoubleSetting("直升机老大鞘翹", "移动速度", 0.5);
      this.verticalSpeed = this.config.getDoubleSetting("直升机老大鞘翹", "上下速度", 0.4);
      String modeStr = this.config.getStringSetting("直升机老大鞘翹", "转头模式", "中转头");
      LookMode[] var2 = HelicopterElytraHack.LookMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         LookMode mode = var2[var4];
         if (mode.toString().equals(modeStr)) {
            this.lookMode = mode;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting s = (Hack.Setting)var6.next();
         switch (s.getName()) {
            case "转头速度":
               s.setValue(this.yawSpeed);
               break;
            case "防下落":
               s.setValue(this.preventFall);
               break;
            case "自由移动":
               s.setValue(this.freeMove);
               break;
            case "移动速度":
               s.setValue(this.moveSpeed);
               break;
            case "上下速度":
               s.setValue(this.verticalSpeed);
               break;
            case "转头模式":
               s.setValue(modeStr);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("直升机老大鞘翹", this.getSettings());
   }

   public void onEnable() {
      this.currentYaw = 0.0F;
      this.lockedY = Double.NaN;
   }

   public void onDisable() {
      if (HeadOnlyLook.isRotating()) {
         HeadOnlyLook.stopRotation();
      }

      this.lockedY = Double.NaN;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         double mz;
         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            boolean v;
            switch (s.getName()) {
               case "转头速度":
                  mz = s.getDouble();
                  if (mz != this.yawSpeed) {
                     this.yawSpeed = mz;
                     needSave = true;
                  }
                  break;
               case "防下落":
                  v = s.getBoolean();
                  if (v != this.preventFall) {
                     this.preventFall = v;
                     needSave = true;
                  }
                  break;
               case "自由移动":
                  v = s.getBoolean();
                  if (v != this.freeMove) {
                     this.freeMove = v;
                     needSave = true;
                  }
                  break;
               case "移动速度":
                  mz = s.getDouble();
                  if (mz != this.moveSpeed) {
                     this.moveSpeed = mz;
                     needSave = true;
                  }
                  break;
               case "上下速度":
                  mz = s.getDouble();
                  if (mz != this.verticalSpeed) {
                     this.verticalSpeed = mz;
                     needSave = true;
                  }
                  break;
               case "转头模式":
                  String v = s.getString();
                  LookMode nl = HelicopterElytraHack.LookMode.MID;
                  LookMode[] var8 = HelicopterElytraHack.LookMode.values();
                  int var9 = var8.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     LookMode mode = var8[var10];
                     if (mode.toString().equals(v)) {
                        nl = mode;
                        break;
                     }
                  }

                  if (nl != this.lookMode) {
                     this.lookMode = nl;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            boolean hasElytra = mc.f_91074_.m_150109_().m_8020_(38).m_41720_() == Items.f_42741_;
            boolean isFlying = mc.f_91074_.m_21255_();
            if (hasElytra && isFlying) {
               if (this.preventFall) {
                  if (Double.isNaN(this.lockedY)) {
                     this.lockedY = mc.f_91074_.m_20186_();
                  }

                  double mx = 0.0;
                  mz = 0.0;
                  if (this.freeMove) {
                     Options opt = mc.f_91066_;
                     boolean forward = opt.f_92085_.m_90857_();
                     boolean back = opt.f_92087_.m_90857_();
                     boolean left = opt.f_92086_.m_90857_();
                     boolean right = opt.f_92088_.m_90857_();
                     boolean jump = opt.f_92089_.m_90857_();
                     boolean sneak = opt.f_92090_.m_90857_();
                     double yawRad = Math.toRadians((double)mc.f_91074_.m_146908_());
                     double fx = -Math.sin(yawRad);
                     double fz = Math.cos(yawRad);
                     double rx = Math.cos(yawRad);
                     double rz = Math.sin(yawRad);
                     if (forward) {
                        mx += fx;
                        mz += fz;
                     }

                     if (back) {
                        mx -= fx;
                        mz -= fz;
                     }

                     if (right) {
                        mx -= rx;
                        mz -= rz;
                     }

                     if (left) {
                        mx += rx;
                        mz += rz;
                     }

                     double len = Math.sqrt(mx * mx + mz * mz);
                     if (len > 0.0) {
                        mx = mx / len * this.moveSpeed;
                        mz = mz / len * this.moveSpeed;
                     }

                     if (jump) {
                        this.lockedY += this.verticalSpeed;
                     }

                     if (sneak) {
                        this.lockedY -= this.verticalSpeed;
                     }
                  }

                  mc.f_91074_.m_6034_(mc.f_91074_.m_20185_() + mx, this.lockedY, mc.f_91074_.m_20189_() + mz);
                  mc.f_91074_.m_20334_(0.0, 0.0, 0.0);
                  mc.f_91074_.f_19789_ = 0.0F;
               } else {
                  this.lockedY = Double.NaN;
               }

               float step = (float)(this.yawSpeed / 2.0);
               this.currentYaw += step;
               this.currentYaw %= 360.0F;
               if (this.currentYaw < 0.0F) {
                  this.currentYaw += 360.0F;
               }

               float targetPitch = this.lookMode.getPitch();
               if (!HeadOnlyLook.isRotating()) {
                  HeadOnlyLook.startRotation(this.currentYaw, targetPitch);
               } else {
                  HeadOnlyLook.updateRotation(this.currentYaw, targetPitch);
               }

               return;
            }

            if (HeadOnlyLook.isRotating()) {
               HeadOnlyLook.stopRotation();
            }

            this.lockedY = Double.NaN;
            return;
         }

         return;
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum LookMode {
      UP("上转头", -90.0F),
      MID("中转头", 0.0F),
      DOWN("下转头", 90.0F);

      private final String name;
      private final float pitch;

      private LookMode(String name, float pitch) {
         this.name = name;
         this.pitch = pitch;
      }

      public String toString() {
         return this.name;
      }

      public float getPitch() {
         return this.pitch;
      }

      // $FF: synthetic method
      private static LookMode[] $values() {
         return new LookMode[]{UP, MID, DOWN};
      }
   }
}
