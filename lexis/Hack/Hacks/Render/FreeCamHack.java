package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BaritoneBridge;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreeCamHack extends Hack implements UpdateListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private boolean active;
   private CameraType oldCameraType;
   private double x;
   private double y;
   private double z;
   private double prevX;
   private double prevY;
   private double prevZ;
   private float yRot;
   private float xRot;
   private float prevYRot;
   private float prevXRot;
   private Quaternionf rotation = new Quaternionf();
   private Vector3f forwards = new Vector3f();
   private Vector3f left = new Vector3f();
   private Vector3f up = new Vector3f();
   private double forwardVelocity;
   private double leftVelocity;
   private double upVelocity;
   private double maxSpeed = 50.0;
   private double smoothness = 20.0;
   private boolean showCrosshair = true;
   private boolean renderHands = false;
   private boolean controlPlayerMovement = false;
   private boolean baritoneGoto = false;
   private String clickMode = "单击";
   private boolean keepBaritone = false;
   private boolean cameraLock = false;
   private boolean eyeLock = false;
   private HackConfig config;
   private static final String CONFIG_KEY = "灵魂出窍";
   private long lastMoveTime = 0L;

   public FreeCamHack() {
      super("灵魂出窍", "移动相机，有联动baritone(需要安装baritone模组)", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("最大速度", "最大移动速度 (格/秒)", 50, 5, 500, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("平滑度", "速度变化平滑度 (越大越快)", 20, 1, 100, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("显示十字瞄星", "自由视角下显示准星", true));
      this.addSetting(new Hack.Setting("显示手", new String[]{"自由视角下显示渲染手", "§c与高清修复/光影 不兼容！"}, false));
      this.addSetting(new Hack.Setting("控制玩家移动", "灵魂出窍中通过↑↓←→按键控制真实玩家移动", false));
      if (BaritoneBridge.isAvailable()) {
         this.addSetting(new Hack.Setting("baritone执行goto", "自由视角下点击目标方块执行 #goto", false));
         this.addSetting(new Hack.Setting("切换模式", "单击或双击触发goto", "单击", new String[]{"单击", "双击"}));
         this.addSetting(new Hack.Setting("保留baritone运行中", "开灵魂出窍时baritone继续运行不暂停", false));
      }

      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxSpeed = this.config.getDoubleSetting("灵魂出窍", "最大速度", 50.0);
      this.smoothness = this.config.getDoubleSetting("灵魂出窍", "平滑度", 20.0);
      this.showCrosshair = this.config.getBooleanSetting("灵魂出窍", "显示十字瞄星", true);
      this.renderHands = this.config.getBooleanSetting("灵魂出窍", "显示手", false);
      this.controlPlayerMovement = this.config.getBooleanSetting("灵魂出窍", "控制玩家移动", false);
      if (BaritoneBridge.isAvailable()) {
         this.baritoneGoto = this.config.getBooleanSetting("灵魂出窍", "baritone执行goto", false);
         this.clickMode = this.config.getStringSetting("灵魂出窍", "切换模式", "单击");
         this.keepBaritone = this.config.getBooleanSetting("灵魂出窍", "保留baritone运行中", false);
      }

      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "最大速度":
               s.setValue(this.maxSpeed);
               break;
            case "平滑度":
               s.setValue(this.smoothness);
               break;
            case "显示十字瞄星":
               s.setValue(this.showCrosshair);
               break;
            case "显示手":
               s.setValue(this.renderHands);
               break;
            case "控制玩家移动":
               s.setValue(this.controlPlayerMovement);
               break;
            case "baritone执行goto":
               s.setValue(this.baritoneGoto);
               break;
            case "切换模式":
               s.setValue(this.clickMode);
               break;
            case "保留baritone运行中":
               s.setValue(this.keepBaritone);
         }
      }

   }

   public void onEnable() {
      if (!this.active) {
         if (mc.f_91074_ == null) {
            this.setEnabled(false);
         } else {
            this.active = true;
            this.cameraLock = false;
            this.eyeLock = false;
            this.oldCameraType = mc.f_91066_.m_92176_();
            mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
            KeyMapping.m_90837_(mc.f_91066_.f_92096_.getKey(), false);

            while(mc.f_91066_.f_92096_.m_90859_()) {
            }

            KeyMapping.m_90837_(mc.f_91066_.f_92095_.getKey(), false);

            while(mc.f_91066_.f_92095_.m_90859_()) {
            }

            Entity e = mc.m_91288_();
            if (e != null) {
               Vec3 pos = e.m_20318_(1.0F);
               this.x = this.prevX = pos.f_82479_;
               this.y = this.prevY = pos.f_82480_;
               this.z = this.prevZ = pos.f_82481_;
               this.yRot = this.prevYRot = e.m_146908_();
               this.xRot = this.prevXRot = e.m_146909_();
               this.calculateVectors();
               double distance = -0.05;
               this.x += (double)this.forwards.x() * distance;
               this.y += (double)this.forwards.y() * distance;
               this.z += (double)this.forwards.z() * distance;
            }

            this.forwardVelocity = this.leftVelocity = this.upVelocity = 0.0;
            this.lastMoveTime = System.nanoTime();
            EventManager.add(UpdateListener.class, this);
         }
      }
   }

   public void onDisable() {
      if (this.active) {
         this.active = false;
         mc.f_91066_.m_92157_(this.oldCameraType);
         EventManager.remove(UpdateListener.class, this);
      }
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "最大速度":
               if (s.getDouble() != this.maxSpeed) {
                  this.maxSpeed = s.getDouble();
                  needSave = true;
               }
               break;
            case "平滑度":
               if (s.getDouble() != this.smoothness) {
                  this.smoothness = s.getDouble();
                  needSave = true;
               }
               break;
            case "显示十字瞄星":
               if (s.getBoolean() != this.showCrosshair) {
                  this.showCrosshair = s.getBoolean();
                  needSave = true;
               }
               break;
            case "显示手":
               if (s.getBoolean() != this.renderHands) {
                  this.renderHands = s.getBoolean();
                  needSave = true;
               }
               break;
            case "控制玩家移动":
               if (s.getBoolean() != this.controlPlayerMovement) {
                  this.controlPlayerMovement = s.getBoolean();
                  needSave = true;
               }
               break;
            case "baritone执行goto":
               if (s.getBoolean() != this.baritoneGoto) {
                  this.baritoneGoto = s.getBoolean();
                  needSave = true;
               }
               break;
            case "切换模式":
               if (!s.getString().equals(this.clickMode)) {
                  this.clickMode = s.getString();
                  needSave = true;
               }
               break;
            case "保留baritone运行中":
               if (s.getBoolean() != this.keepBaritone) {
                  this.keepBaritone = s.getBoolean();
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("灵魂出窍", this.getSettings());
      }

      if (this.active && mc.f_91074_ != null) {
         if (mc.f_91066_.m_92176_() != CameraType.THIRD_PERSON_BACK) {
            mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
         }

         BlockPos camPos = BlockPos.m_274561_(this.x, this.y, this.z);
         if (mc.f_91073_ != null && mc.f_91073_.m_8055_(camPos).m_60828_(mc.f_91073_, camPos)) {
            KeyMapping.m_90837_(mc.f_91066_.f_92096_.getKey(), false);

            while(true) {
               if (mc.f_91066_.f_92096_.m_90859_()) {
                  continue;
               }
            }
         }

         this.calculateVectors();
         double targetForward;
         if (this.eyeLock) {
            Entity entity = mc.m_91288_();
            if (entity != null) {
               Vec3 playerPos = entity.m_20318_(1.0F);
               double dx = this.x - playerPos.f_82479_;
               double dy = this.y - playerPos.f_82480_;
               targetForward = this.z - playerPos.f_82481_;
               float pitch = (float)(Math.atan2(dy, Math.sqrt(dx * dx + targetForward * targetForward)) * 180.0 / Math.PI);
               float yaw = (float)(Math.atan2(targetForward, dx) * 180.0 / Math.PI + 90.0);
               this.xRot = pitch;
               this.yRot = yaw;
               this.calculateVectors();
            }
         }

         long now = System.nanoTime();
         float delta = (float)(now - this.lastMoveTime) / 1.0E9F;
         this.lastMoveTime = now;
         if (delta > 0.1F) {
            delta = 0.1F;
         }

         float forward = (float)((mc.f_91066_.f_92085_.m_90857_() ? 1 : 0) + (mc.f_91066_.f_92087_.m_90857_() ? -1 : 0));
         float strafe = (float)((mc.f_91066_.f_92086_.m_90857_() ? 1 : 0) + (mc.f_91066_.f_92088_.m_90857_() ? -1 : 0));
         float up = (float)((mc.f_91066_.f_92089_.m_90857_() ? 1 : 0) + (mc.f_91066_.f_92090_.m_90857_() ? -1 : 0));
         targetForward = (double)forward * this.maxSpeed;
         double targetStrafe = (double)strafe * this.maxSpeed;
         double targetUp = (double)up * this.maxSpeed;
         double factor = Math.min(1.0, (double)delta * this.smoothness);
         factor = 1.0 - Math.pow(1.0 - factor, 3.0);
         this.forwardVelocity += (targetForward - this.forwardVelocity) * factor;
         this.leftVelocity += (targetStrafe - this.leftVelocity) * factor;
         this.upVelocity += (targetUp - this.upVelocity) * factor;
         double inertia = Math.pow(0.98, (double)(delta * 20.0F));
         this.forwardVelocity *= inertia;
         this.leftVelocity *= inertia;
         this.upVelocity *= inertia;
         double dx = (double)this.forwards.x() * this.forwardVelocity + (double)this.left.x() * this.leftVelocity;
         double dy = (double)this.forwards.y() * this.forwardVelocity + this.upVelocity + (double)this.left.y() * this.leftVelocity;
         double dz = (double)this.forwards.z() * this.forwardVelocity + (double)this.left.z() * this.leftVelocity;
         dx *= (double)delta;
         dy *= (double)delta;
         dz *= (double)delta;
         this.prevX = this.x;
         this.prevY = this.y;
         this.prevZ = this.z;
         this.prevYRot = this.yRot;
         this.prevXRot = this.xRot;
         this.x += dx;
         this.y += dy;
         this.z += dz;
      }
   }

   public void onMouseTurn(double yawDelta, double pitchDelta) {
      if (this.active && !this.cameraLock && !this.eyeLock) {
         this.yRot += (float)yawDelta * 0.15F;
         this.xRot += (float)pitchDelta * 0.15F;
         this.xRot = Mth.m_14036_(this.xRot, -90.0F, 90.0F);
      }
   }

   private void calculateVectors() {
      this.rotation.rotationYXZ(-this.yRot * 3.1415927F / 180.0F, this.xRot * 3.1415927F / 180.0F, 0.0F);
      this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
      this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
      this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean isCameraLocked() {
      return this.cameraLock;
   }

   public boolean isEyeLocked() {
      return this.eyeLock;
   }

   public boolean shouldShowCrosshair() {
      return this.showCrosshair;
   }

   public boolean shouldRenderHands() {
      return this.renderHands;
   }

   public boolean isControlPlayerMovement() {
      return this.controlPlayerMovement;
   }

   public boolean isBaritoneGoto() {
      return this.baritoneGoto;
   }

   public String getClickMode() {
      return this.clickMode;
   }

   public boolean isKeepBaritone() {
      return this.keepBaritone;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public double getPrevX() {
      return this.prevX;
   }

   public double getPrevY() {
      return this.prevY;
   }

   public double getPrevZ() {
      return this.prevZ;
   }

   public float getXRot() {
      return this.xRot;
   }

   public float getYRot() {
      return this.yRot;
   }

   public float getPrevXRot() {
      return this.prevXRot;
   }

   public float getPrevYRot() {
      return this.prevYRot;
   }

   public void toggleCameraLock() {
      if (this.active) {
         this.cameraLock = !this.cameraLock;
      }

   }

   public void toggleEyeLock() {
      if (this.active) {
         this.eyeLock = !this.eyeLock;
      }

   }

   public void onBeforeGameRendererPick() {
   }

   public void onAfterGameRendererPick() {
   }

   public void onClick() {
      this.toggle();
   }
}
