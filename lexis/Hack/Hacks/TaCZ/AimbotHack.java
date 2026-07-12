package lexis.Hack.Hacks.TaCZ;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.IGuiRenderable;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AimbotHack extends Hack implements UpdateListener, IGuiRenderable {
   private static AimbotHack instance;
   private static Field taczDataField = null;
   private static Field taczAimingField = null;
   private static boolean taczReflectionFailed = false;
   private int circleSize = 100;
   private float rotationSpeed = 30.0F;
   private int circleColor = -65536;
   private int lockColor = -16711936;
   private int fovColor = 1073807104;
   private boolean bypassFriends = true;
   private boolean bypassNamed = false;
   private boolean bypassPassive = true;
   private boolean onlyWhenAiming = true;
   private boolean allowThroughWalls = false;
   private String bodyPart = "身体";
   private final Set entityWhitelist = ConcurrentHashMap.newKeySet();
   private LivingEntity currentTarget = null;
   private boolean hasTarget = false;
   private long lastUpdateTime = 0L;
   private HackConfig config;
   private static final String CONFIG_KEY = "子弹自瞄";

   public AimbotHack() {
      super("子弹自瞄", "这真神是自瞄。。。", Hack.Category.TACZ, true);
      this.addSetting(new Hack.Setting("自瞄范围", "圆圈半径（像素）", 100, 20, 500, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("旋转速度", "旋转速度（度/秒）", 30.0, 1.0, 180.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("圆圈颜色", "未锁定时圆圈颜色", -65536));
      this.addSetting(new Hack.Setting("锁定颜色", "锁定时圆圈颜色", -16711936));
      this.addSetting(new Hack.Setting("FOV颜色", "圆圈填充颜色", 1073807104));
      this.addSetting(new Hack.Setting("在开镜锁定", "仅在使用物品/开镜瞄准时才锁定目标", true));
      this.addSetting(new Hack.Setting("允许穿墙锁定", "关闭时会检测视线是否被方块阻挡", false));
      this.addSetting(new Hack.Setting("锁定部位", "瞄准目标的哪个身体部位（自动=选离准星最近的部位）", "身体", new String[]{"头", "身体", "腿", "脚", "自动"}));
      this.addSetting(new Hack.Setting("绕过好友", "不瞄准好友", true));
      this.addSetting(new Hack.Setting("绕过命名", "不瞄准有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不瞄准被动实体（动物等）", true));
      this.addSetting(new Hack.Setting("晓过实体", "实体白名单，不瞄准这些实体", "晓过实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "aimbot_tacz", this::reloadWhitelistFromConfig));
         }

      }));
      instance = this;
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.circleSize = this.config.getIntSetting("子弹自瞄", "自瞄范围", 100);
      this.rotationSpeed = (float)this.config.getDoubleSetting("子弹自瞄", "旋转速度", 30.0);
      this.circleColor = this.config.getIntSetting("子弹自瞄", "圆圈颜色", -65536);
      this.lockColor = this.config.getIntSetting("子弹自瞄", "锁定颜色", -16711936);
      this.fovColor = this.config.getIntSetting("子弹自瞄", "FOV颜色", 1073807104);
      this.onlyWhenAiming = this.config.getBooleanSetting("子弹自瞄", "在开镜锁定", true);
      this.allowThroughWalls = this.config.getBooleanSetting("子弹自瞄", "允许穿墙锁定", false);
      this.bodyPart = this.config.getStringSetting("子弹自瞄", "锁定部位", "身体");
      this.bypassFriends = this.config.getBooleanSetting("子弹自瞄", "绕过好友", true);
      this.bypassNamed = this.config.getBooleanSetting("子弹自瞄", "绕过命名", false);
      this.bypassPassive = this.config.getBooleanSetting("子弹自瞄", "绕过被动", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "自瞄范围":
               setting.setValue(this.circleSize);
               break;
            case "旋转速度":
               setting.setValue((double)this.rotationSpeed);
               break;
            case "圆圈颜色":
               setting.setValue(this.circleColor);
               break;
            case "锁定颜色":
               setting.setValue(this.lockColor);
               break;
            case "FOV颜色":
               setting.setValue(this.fovColor);
               break;
            case "在开镜锁定":
               setting.setValue(this.onlyWhenAiming);
               break;
            case "允许穿墙锁定":
               setting.setValue(this.allowThroughWalls);
               break;
            case "锁定部位":
               setting.setValue(this.bodyPart);
               break;
            case "绕过好友":
               setting.setValue(this.bypassFriends);
               break;
            case "绕过命名":
               setting.setValue(this.bypassNamed);
               break;
            case "绕过被动":
               setting.setValue(this.bypassPassive);
         }
      }

      this.reloadWhitelistFromConfig();
   }

   private void saveConfig() {
      this.config.saveHackSettings("子弹自瞄", this.getSettings());
   }

   private void reloadWhitelistFromConfig() {
      this.entityWhitelist.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_aimbot_tacz.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      if (loaded != null) {
         this.entityWhitelist.addAll(loaded);
      }

   }

   public void onEnable() {
      this.currentTarget = null;
      this.hasTarget = false;
      this.lastUpdateTime = System.currentTimeMillis();
      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      this.currentTarget = null;
      this.hasTarget = false;
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            switch (setting.getName()) {
               case "自瞄范围":
                  if (setting.getInt() != this.circleSize) {
                     this.circleSize = setting.getInt();
                     needSave = true;
                  }
                  break;
               case "旋转速度":
                  if ((float)setting.getDouble() != this.rotationSpeed) {
                     this.rotationSpeed = (float)setting.getDouble();
                     needSave = true;
                  }
                  break;
               case "圆圈颜色":
                  if ((Integer)setting.getValue() != this.circleColor) {
                     this.circleColor = (Integer)setting.getValue();
                     needSave = true;
                  }
                  break;
               case "锁定颜色":
                  if ((Integer)setting.getValue() != this.lockColor) {
                     this.lockColor = (Integer)setting.getValue();
                     needSave = true;
                  }
                  break;
               case "FOV颜色":
                  if ((Integer)setting.getValue() != this.fovColor) {
                     this.fovColor = (Integer)setting.getValue();
                     needSave = true;
                  }
                  break;
               case "在开镜锁定":
                  if (setting.getBoolean() != this.onlyWhenAiming) {
                     this.onlyWhenAiming = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "允许穿墙锁定":
                  if (setting.getBoolean() != this.allowThroughWalls) {
                     this.allowThroughWalls = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "锁定部位":
                  if (!setting.getString().equals(this.bodyPart)) {
                     this.bodyPart = setting.getString();
                     needSave = true;
                  }
                  break;
               case "绕过好友":
                  if (setting.getBoolean() != this.bypassFriends) {
                     this.bypassFriends = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "绕过命名":
                  if (setting.getBoolean() != this.bypassNamed) {
                     this.bypassNamed = setting.getBoolean();
                     needSave = true;
                  }
                  break;
               case "绕过被动":
                  if (setting.getBoolean() != this.bypassPassive) {
                     this.bypassPassive = setting.getBoolean();
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         if (this.onlyWhenAiming && !isPlayerAiming(mc.f_91074_)) {
            if (this.hasTarget) {
               this.hasTarget = false;
               this.currentTarget = null;
            }

         } else {
            LocalPlayer player = mc.f_91074_;
            List targets = this.getValidTargets();
            LivingEntity bestTarget = this.findBestTarget(targets, player);
            if (bestTarget == null) {
               if (this.hasTarget) {
                  this.hasTarget = false;
                  this.currentTarget = null;
               }

            } else {
               Vec3 targetPos = this.getAimPoint(bestTarget);
               Vec3 eye = player.m_146892_();
               Vec3 dir = targetPos.m_82546_(eye);
               double distXZ = Math.sqrt(dir.f_82479_ * dir.f_82479_ + dir.f_82481_ * dir.f_82481_);
               float targetYaw = (float)Math.toDegrees(Math.atan2(dir.f_82481_, dir.f_82479_)) - 90.0F;
               float targetPitch = Mth.m_14036_((float)(-Math.toDegrees(Math.atan2(dir.f_82480_, distXZ))), -90.0F, 90.0F);
               targetYaw = this.normalizeAngle(targetYaw);
               float currentYaw = player.m_146908_();
               float currentPitch = player.m_146909_();
               float yawDiff = this.normalizeAngle(targetYaw - currentYaw);
               float pitchDiff = targetPitch - currentPitch;
               long now = System.currentTimeMillis();
               float deltaSec = (float)(now - this.lastUpdateTime) / 1000.0F;
               this.lastUpdateTime = now;
               if (deltaSec <= 0.0F || deltaSec > 1.0F) {
                  deltaSec = 0.05F;
               }

               float maxRot = this.rotationSpeed * deltaSec;
               float stepYaw = Mth.m_14036_(yawDiff, -maxRot, maxRot);
               float stepPitch = Mth.m_14036_(pitchDiff, -maxRot, maxRot);
               float newYaw = this.normalizeAngle(currentYaw + stepYaw);
               float newPitch = Mth.m_14036_(currentPitch + stepPitch, -90.0F, 90.0F);
               player.m_146922_(newYaw);
               player.m_146926_(newPitch);
               mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(newYaw, newPitch, player.m_20096_()));
               this.hasTarget = true;
               this.currentTarget = bestTarget;
            }
         }
      }
   }

   private LivingEntity findBestTarget(List targets, LocalPlayer player) {
      if (targets.isEmpty()) {
         return null;
      } else {
         Vec3 eyePos = player.m_146892_();
         Vec3 lookVec = player.m_20154_();
         float lookYaw = player.m_146908_();
         float lookPitch = player.m_146909_();
         double fov = (double)(Integer)mc.f_91066_.m_231837_().m_231551_();
         double halfH = (double)mc.m_91268_().m_85446_() / 2.0;
         double halfFovRad = Math.toRadians(fov / 2.0);
         double projScale = halfH / Math.tan(halfFovRad);
         double circleAngleRad = Math.atan((double)this.circleSize / projScale);
         LivingEntity best = null;
         double bestDist = Double.MAX_VALUE;
         Iterator var20 = targets.iterator();

         while(true) {
            LivingEntity entity;
            Vec3 targetPos;
            Vec3 toTarget;
            do {
               double distance;
               do {
                  do {
                     do {
                        if (!var20.hasNext()) {
                           return best;
                        }

                        entity = (LivingEntity)var20.next();
                     } while(!entity.m_6084_());

                     targetPos = entity.m_20191_().m_82399_();
                     toTarget = targetPos.m_82546_(eyePos);
                     distance = toTarget.m_82553_();
                  } while(distance < 0.01);
               } while(toTarget.m_82526_(lookVec) < 0.0);
            } while(!this.allowThroughWalls && !this.hasLineOfSight(eyePos, targetPos));

            double totalAngle = Math.acos(Mth.m_14008_(lookVec.m_82526_(toTarget.m_82541_()), -1.0, 1.0));
            if (!(totalAngle > circleAngleRad * 1.5)) {
               double distXZ = Math.sqrt(toTarget.f_82479_ * toTarget.f_82479_ + toTarget.f_82481_ * toTarget.f_82481_);
               double targetYaw = Math.toDegrees(Math.atan2(toTarget.f_82481_, toTarget.f_82479_)) - 90.0;
               double targetPitch = -Math.toDegrees(Math.atan2(toTarget.f_82480_, distXZ));
               double yawDiff = Math.toRadians((double)this.normalizeAngle((float)(targetYaw - (double)lookYaw)));
               double pitchDiff = Math.toRadians(targetPitch - (double)lookPitch);
               yawDiff = Mth.m_14008_(yawDiff, -1.55, 1.55);
               pitchDiff = Mth.m_14008_(pitchDiff, -1.55, 1.55);
               double screenX = Math.tan(yawDiff) * projScale;
               double screenY = Math.tan(pitchDiff) * projScale;
               double screenDist = Math.sqrt(screenX * screenX + screenY * screenY);
               if (screenDist <= (double)this.circleSize && screenDist < bestDist) {
                  bestDist = screenDist;
                  best = entity;
               }
            }
         }
      }
   }

   private boolean hasLineOfSight(Vec3 from, Vec3 to) {
      if (mc.f_91073_ == null) {
         return true;
      } else {
         HitResult result = mc.f_91073_.m_45547_(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
         return result.m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS;
      }
   }

   private List getValidTargets() {
      List targets = new ArrayList();
      LocalPlayer player = mc.f_91074_;
      if (player != null && mc.f_91073_ != null) {
         Iterator var3 = mc.f_91073_.m_104735_().iterator();

         while(true) {
            LivingEntity living;
            String entityId;
            do {
               Entity entity;
               do {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var3.hasNext()) {
                                    return targets;
                                 }

                                 entity = (Entity)var3.next();
                              } while(!(entity instanceof LivingEntity));

                              living = (LivingEntity)entity;
                           } while(entity == player);
                        } while(!living.m_6084_());
                     } while(this.bypassFriends && entity instanceof Player && FriendsManager.getInstance().isFriend((Player)entity));
                  } while(this.bypassNamed && entity.m_8077_());
               } while(this.bypassPassive && living instanceof Animal);

               entityId = this.getEntityTypeId(entity);
            } while(entityId != null && this.entityWhitelist.contains(entityId));

            targets.add(living);
         }
      } else {
         return targets;
      }
   }

   private String getEntityTypeId(Entity entity) {
      ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_());
      return key != null ? key.toString() : null;
   }

   private Vec3 getAimPoint(LivingEntity e) {
      Vec3 c = e.m_20191_().m_82399_();
      double feetY = e.m_20186_();
      double h = (double)e.m_20206_();
      double y;
      switch (this.bodyPart) {
         case "头":
            y = feetY + (double)e.m_20192_();
            break;
         case "腿":
            y = feetY + h * 0.28;
            break;
         case "脚":
            y = feetY + h * 0.08;
            break;
         case "自动":
            return this.getAutoAimPoint(e, c);
         case "身体":
         default:
            y = feetY + h * 0.5;
      }

      return new Vec3(c.f_82479_, y, c.f_82481_);
   }

   private Vec3 getAutoAimPoint(LivingEntity e, Vec3 c) {
      if (mc.f_91074_ == null) {
         return new Vec3(c.f_82479_, c.f_82480_, c.f_82481_);
      } else {
         double feetY = e.m_20186_();
         double h = (double)e.m_20206_();
         double[] candidateY = new double[]{feetY + (double)e.m_20192_(), feetY + h * 0.5, feetY + h * 0.28, feetY + h * 0.08};
         Vec3 eye = mc.f_91074_.m_146892_();
         float curYaw = mc.f_91074_.m_146908_();
         float curPitch = mc.f_91074_.m_146909_();
         Vec3 bestPoint = new Vec3(c.f_82479_, c.f_82480_, c.f_82481_);
         double bestAngle = Double.MAX_VALUE;
         double[] var14 = candidateY;
         int var15 = candidateY.length;

         for(int var16 = 0; var16 < var15; ++var16) {
            double y = var14[var16];
            Vec3 p = new Vec3(c.f_82479_, y, c.f_82481_);
            Vec3 d = p.m_82546_(eye);
            double distXZ = Math.sqrt(d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_);
            if (!(distXZ < 1.0E-4)) {
               float yaw = this.normalizeAngle((float)(Math.toDegrees(Math.atan2(d.f_82481_, d.f_82479_)) - 90.0));
               float pitch = (float)(-Math.toDegrees(Math.atan2(d.f_82480_, distXZ)));
               double angle = (double)(Math.abs(this.normalizeAngle(yaw - curYaw)) + Math.abs(pitch - curPitch));
               if (angle < bestAngle) {
                  bestAngle = angle;
                  bestPoint = p;
               }
            }
         }

         return bestPoint;
      }
   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   private static boolean isPlayerAiming(LocalPlayer player) {
      if (!taczReflectionFailed) {
         try {
            if (taczDataField == null) {
               Field[] var1 = player.getClass().getDeclaredFields();
               int var2 = var1.length;

               for(int var3 = 0; var3 < var2; ++var3) {
                  Field f = var1[var3];
                  if (f.getType().getName().equals("com.tacz.guns.client.gameplay.LocalPlayerDataHolder")) {
                     f.setAccessible(true);
                     taczDataField = f;
                     break;
                  }
               }

               if (taczDataField == null) {
                  taczReflectionFailed = true;
                  return player.m_6117_();
               }
            }

            Object data = taczDataField.get(player);
            if (data == null) {
               return false;
            }

            if (taczAimingField == null) {
               taczAimingField = data.getClass().getDeclaredField("clientIsAiming");
               taczAimingField.setAccessible(true);
            }

            return (Boolean)taczAimingField.get(data);
         } catch (Exception var5) {
            taczReflectionFailed = true;
         }
      }

      return player.m_6117_();
   }

   public void onRenderGui(GuiGraphics guiGraphics, float partialTick) {
      if (mc.f_91074_ != null) {
         int screenW = mc.m_91268_().m_85445_();
         int screenH = mc.m_91268_().m_85446_();
         int cx = screenW / 2;
         int cy = screenH / 2;
         int drawColor = this.hasTarget ? this.lockColor : this.circleColor;
         float r = (float)(drawColor >> 16 & 255) / 255.0F;
         float g = (float)(drawColor >> 8 & 255) / 255.0F;
         float b = (float)(drawColor & 255) / 255.0F;
         float a = (float)(drawColor >> 24 & 255) / 255.0F;
         float fovR = (float)(this.fovColor >> 16 & 255) / 255.0F;
         float fovG = (float)(this.fovColor >> 8 & 255) / 255.0F;
         float fovB = (float)(this.fovColor & 255) / 255.0F;
         float fovA = (float)(this.fovColor >> 24 & 255) / 255.0F;
         PoseStack poseStack = guiGraphics.m_280168_();
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder buffer = tesselator.m_85915_();
         int segments = 64;
         buffer.m_166779_(Mode.TRIANGLE_FAN, DefaultVertexFormat.f_85815_);
         buffer.m_252986_(matrix, (float)cx, (float)cy, 0.0F).m_85950_(fovR, fovG, fovB, fovA).m_5752_();

         int i;
         double angle;
         float x;
         float y;
         for(i = 0; i <= segments; ++i) {
            angle = 6.283185307179586 * (double)i / (double)segments;
            x = (float)cx + (float)((double)this.circleSize * Math.cos(angle));
            y = (float)cy + (float)((double)this.circleSize * Math.sin(angle));
            buffer.m_252986_(matrix, x, y, 0.0F).m_85950_(fovR, fovG, fovB, fovA).m_5752_();
         }

         tesselator.m_85914_();
         RenderSystem.lineWidth(2.0F);
         buffer.m_166779_(Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.f_85815_);

         for(i = 0; i <= segments; ++i) {
            angle = 6.283185307179586 * (double)i / (double)segments;
            x = (float)cx + (float)((double)this.circleSize * Math.cos(angle));
            y = (float)cy + (float)((double)this.circleSize * Math.sin(angle));
            buffer.m_252986_(matrix, x, y, 0.0F).m_85950_(r, g, b, a).m_5752_();
         }

         tesselator.m_85914_();
         RenderSystem.lineWidth(1.0F);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static AimbotHack getInstance() {
      return instance;
   }
}
