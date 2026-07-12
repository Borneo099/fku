package lexis.Hack.Hacks.World;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityControlHack extends Hack {
   private static final String CONFIG_KEY = "实体控制";
   private static boolean active = false;
   private static double horizontalSpeed = 10.0;
   private static boolean flightMode = false;
   private static double verticalSpeed = 6.0;
   private static boolean lockYaw = true;
   private static boolean antiKick = true;
   private static float antiKickDistance = 0.05F;
   private static int antiKickInterval = 30;
   private HackConfig config = HackConfig.getInstance();
   private int tickCounter = 0;

   public EntityControlHack() {
      super("实体控制", new String[]{"骑乘加速飞行", "强大的功能 能控制模组的实体"}, Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("水平速度", "前后左右移动速度", 10.0, 0.0, 50.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("飞行模式", "空格上升", false));
      this.addSetting(new Hack.Setting("上升速度", "飞行时上升速度", 6.0, 0.0, 20.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("锁定视角方向", "强制实体面向玩家方向", true));
      this.addSetting(new Hack.Setting("反踢出", "防止服务器踢出", true));
      this.addSetting(new Hack.Setting("反踢距离", "反踢上下移动距离", 0.05, 0.01, 3.5, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("反踢间隔", "反踢检测间隔（tick）", 30, 1, 100, Hack.ValueDisplay.INTEGER));
      this.loadConfig();
   }

   private void loadConfig() {
      horizontalSpeed = this.config.getDoubleSetting("实体控制", "水平速度", 10.0);
      flightMode = this.config.getBooleanSetting("实体控制", "飞行模式", false);
      verticalSpeed = this.config.getDoubleSetting("实体控制", "上升速度", 6.0);
      lockYaw = this.config.getBooleanSetting("实体控制", "锁定视角方向", true);
      antiKick = this.config.getBooleanSetting("实体控制", "反踢出", true);
      antiKickDistance = (float)this.config.getDoubleSetting("实体控制", "反踢距离", 0.05);
      antiKickInterval = this.config.getIntSetting("实体控制", "反踢间隔", 30);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "水平速度":
               s.setValue(horizontalSpeed);
               break;
            case "飞行模式":
               s.setValue(flightMode);
               break;
            case "上升速度":
               s.setValue(verticalSpeed);
               break;
            case "锁定视角方向":
               s.setValue(lockYaw);
               break;
            case "反踢出":
               s.setValue(antiKick);
               break;
            case "反踢距离":
               s.setValue((double)antiKickDistance);
               break;
            case "反踢间隔":
               s.setValue(antiKickInterval);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("实体控制", this.getSettings());
   }

   public void onEnable() {
      active = true;
      this.loadConfig();
      this.tickCounter = 0;
   }

   public void onDisable() {
      active = false;
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "水平速度":
               double h = s.getDouble();
               if (Math.abs(h - horizontalSpeed) > 0.001) {
                  horizontalSpeed = h;
                  needSave = true;
               }
               break;
            case "飞行模式":
               boolean f = s.getBoolean();
               if (f != flightMode) {
                  flightMode = f;
                  needSave = true;
               }
               break;
            case "上升速度":
               double v = s.getDouble();
               if (Math.abs(v - verticalSpeed) > 0.001) {
                  verticalSpeed = v;
                  needSave = true;
               }
               break;
            case "锁定视角方向":
               boolean y = s.getBoolean();
               if (y != lockYaw) {
                  lockYaw = y;
                  needSave = true;
               }
               break;
            case "反踢出":
               boolean ak = s.getBoolean();
               if (ak != antiKick) {
                  antiKick = ak;
                  needSave = true;
               }
               break;
            case "反踢距离":
               float akd = (float)s.getDouble();
               if ((double)Math.abs(akd - antiKickDistance) > 0.001) {
                  antiKickDistance = akd;
                  needSave = true;
               }
               break;
            case "反踢间隔":
               int aki = s.getInt();
               if (aki != antiKickInterval) {
                  antiKickInterval = aki;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

      if (active) {
         Minecraft mc = Minecraft.m_91087_();
         Player player = mc.f_91074_;
         if (player != null) {
            Entity vehicle = player.m_20202_();
            if (vehicle != null) {
               Options opts = mc.f_91066_;
               float forward = 0.0F;
               float strafe = 0.0F;
               float vertical = 0.0F;
               if (opts.f_92085_.m_90857_()) {
                  ++forward;
               }

               if (opts.f_92087_.m_90857_()) {
                  --forward;
               }

               if (opts.f_92086_.m_90857_()) {
                  ++strafe;
               }

               if (opts.f_92088_.m_90857_()) {
                  --strafe;
               }

               if (flightMode && opts.f_92089_.m_90857_()) {
                  ++vertical;
               }

               Vec3 movement = this.calculateMovement(forward, strafe, (float)horizontalSpeed);
               if (flightMode) {
                  if (vertical != 0.0F) {
                     movement = new Vec3(movement.f_82479_, verticalSpeed, movement.f_82481_);
                  } else {
                     movement = new Vec3(movement.f_82479_, 0.0, movement.f_82481_);
                  }
               } else {
                  movement = new Vec3(movement.f_82479_, vehicle.m_20184_().f_82480_, movement.f_82481_);
               }

               vehicle.m_20256_(movement);
               if (lockYaw) {
                  vehicle.m_146922_(player.m_146908_());
               }

               if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
                  mc.f_91074_.f_108617_.m_104955_(new ServerboundMoveVehiclePacket(vehicle));
               }

               if (antiKick && flightMode) {
                  ++this.tickCounter;
                  if (this.tickCounter > antiKickInterval + 1) {
                     this.tickCounter = 0;
                  }

                  if (this.tickCounter == 0) {
                     vehicle.m_6034_(vehicle.m_20185_(), vehicle.m_20186_() - (double)antiKickDistance, vehicle.m_20189_());
                  } else if (this.tickCounter == 1) {
                     vehicle.m_6034_(vehicle.m_20185_(), vehicle.m_20186_() + (double)antiKickDistance, vehicle.m_20189_());
                  }

                  if ((this.tickCounter == 0 || this.tickCounter == 1) && mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
                     mc.f_91074_.f_108617_.m_104955_(new ServerboundMoveVehiclePacket(vehicle));
                  }
               } else {
                  this.tickCounter = 0;
               }

            }
         }
      }
   }

   private Vec3 calculateMovement(float forward, float strafe, float speed) {
      Minecraft mc = Minecraft.m_91087_();
      Player player = mc.f_91074_;
      if (player == null) {
         return Vec3.f_82478_;
      } else if (forward == 0.0F && strafe == 0.0F) {
         return Vec3.f_82478_;
      } else {
         float yaw = player.m_146908_();
         double rad = Math.toRadians((double)yaw);
         double moveX = 0.0;
         double moveZ = 0.0;
         if (forward != 0.0F) {
            moveX -= Math.sin(rad) * (double)forward;
            moveZ += Math.cos(rad) * (double)forward;
         }

         if (strafe != 0.0F) {
            moveX -= Math.sin(rad - 1.5707963267948966) * (double)strafe;
            moveZ += Math.cos(rad - 1.5707963267948966) * (double)strafe;
         }

         double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
         if (len > 0.0) {
            moveX = moveX / len * (double)speed;
            moveZ = moveZ / len * (double)speed;
         }

         return new Vec3(moveX, 0.0, moveZ);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
