package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Fun.DerpHack;
import lexis.Hack.Hacks.Fun.StareAtPlayerHack;
import lexis.Hack.Hackutil.HackManager;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.config.KillauraConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class KillauraHack extends Hack {
   private float attackRange = 6.0F;
   private AttackMode attackMode;
   private final Set whitelistEntities;
   private final Map lastAttackTime;
   private int currentTargetIndex;
   private final List currentTargets;
   private long lastTargetSwitchTime;
   private static final long TARGET_SWITCH_DELAY = 0L;
   private LivingEntity currentTarget;
   private boolean hasTarget;
   private Entity renderTarget;
   private boolean showDamageIndicator;
   private Map healthAnimations;
   private static final long ANIMATION_DURATION = 1000L;
   private HackConfig config;
   private KillauraConfig killauraConfig;
   private static final String CONFIG_KEY = "杀圈光环";
   private Priority priority;
   private boolean ignoreNamed;
   private boolean ignorePassive;

   public KillauraHack() {
      super("杀圈光环", "杀圈光环自带旋转", Hack.Category.COMBAT, true);
      this.attackMode = KillauraHack.AttackMode.MULTI_TARGET;
      this.whitelistEntities = ConcurrentHashMap.newKeySet();
      this.lastAttackTime = new HashMap();
      this.currentTargetIndex = 0;
      this.currentTargets = new ArrayList();
      this.lastTargetSwitchTime = 0L;
      this.currentTarget = null;
      this.hasTarget = false;
      this.renderTarget = null;
      this.showDamageIndicator = true;
      this.healthAnimations = new ConcurrentHashMap();
      this.priority = KillauraHack.Priority.ANGLE;
      this.ignoreNamed = false;
      this.ignorePassive = true;
      this.addSetting(new Hack.Setting("攻击距离", "攻击范围", 6.0, 1.0, 512.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("攻击模式", "攻击方式", "多目标", new String[]{"单目标", "多目标"}));
      this.addSetting(new Hack.Setting("优先级", "目标选择优先级", "角度", new String[]{"距离", "角度", "血量"}));
      this.addSetting(new Hack.Setting("显示血量", "攻击时显示3D血量动画", true));
      this.addSetting(new Hack.Setting("绕过命名", "不攻击有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不攻击被动实体(动物等)", true));
      this.addSetting(new Hack.Setting("晓过实体", "晓过白名单实体，不攻击实体设置多个实体", "晓过实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "killaura", this::reloadWhitelistFromConfig));
         }

      }));
      this.config = HackConfig.getInstance();
      this.killauraConfig = KillauraConfig.getInstance();
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.attackRange = (float)this.config.getDoubleSetting("杀圈光环", "攻击距离", 6.0);
      String modeStr = this.config.getStringSetting("杀圈光环", "攻击模式", "多目标");
      String priorityStr = this.config.getStringSetting("杀圈光环", "优先级", "角度");
      this.showDamageIndicator = this.config.getBooleanSetting("杀圈光环", "显示血量", true);
      this.ignoreNamed = this.config.getBooleanSetting("杀圈光环", "绕过命名", false);
      this.ignorePassive = this.config.getBooleanSetting("杀圈光环", "绕过被动", true);
      this.attackMode = modeStr.equals("单目标") ? KillauraHack.AttackMode.SINGLE_TARGET : KillauraHack.AttackMode.MULTI_TARGET;
      Priority[] var3 = KillauraHack.Priority.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Priority p = var3[var5];
         if (p.toString().equals(priorityStr)) {
            this.priority = p;
            break;
         }
      }

      Iterator var7 = this.getSettings().iterator();

      while(var7.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var7.next();
         switch (setting.getName()) {
            case "攻击距离":
               setting.setValue((double)this.attackRange);
               break;
            case "攻击模式":
               setting.setValue(modeStr);
               break;
            case "优先级":
               setting.setValue(priorityStr);
               break;
            case "显示血量":
               setting.setValue(this.showDamageIndicator);
               break;
            case "绕过命名":
               setting.setValue(this.ignoreNamed);
               break;
            case "绕过被动":
               setting.setValue(this.ignorePassive);
         }
      }

   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      this.whitelistEntities.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_killaura.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      if (loaded != null) {
         this.whitelistEntities.addAll(loaded);
      }

   }

   public void saveWhitelist(Set newWhitelist) {
      this.whitelistEntities.clear();
      this.whitelistEntities.addAll(newWhitelist);
      this.killauraConfig.setWhitelist(newWhitelist);
   }

   public void onEnable() {
      this.resetMultiTargetState();
      this.currentTarget = null;
      this.renderTarget = null;
      this.hasTarget = false;
      this.healthAnimations.clear();
   }

   public void onDisable() {
      this.resetMultiTargetState();
      this.currentTarget = null;
      this.renderTarget = null;
      this.hasTarget = false;
      this.healthAnimations.clear();
      if (!this.isAnyHeadRotationActive()) {
         HeadOnlyLook.stopRotation();
      }

   }

   private boolean isAnyHeadRotationActive() {
      Iterator var1 = HackManager.getInstance().getHacks().iterator();

      Hack hack;
      do {
         do {
            do {
               if (!var1.hasNext()) {
                  return false;
               }

               hack = (Hack)var1.next();
            } while(!hack.isEnabled());
         } while(hack == this);
      } while(!(hack instanceof DerpHack) && !(hack instanceof StareAtPlayerHack));

      return true;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         this.healthAnimations.entrySet().removeIf((entry) -> {
            return !((HealthAnimation)entry.getValue()).shouldRender();
         });
         Iterator var1 = this.getSettings().iterator();

         label87:
         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "攻击距离":
                  this.attackRange = (float)setting.getDouble();
                  break;
               case "攻击模式":
                  this.attackMode = setting.getString().equals("单目标") ? KillauraHack.AttackMode.SINGLE_TARGET : KillauraHack.AttackMode.MULTI_TARGET;
                  break;
               case "优先级":
                  String priorityStr = setting.getString();
                  Priority[] var6 = KillauraHack.Priority.values();
                  int var7 = var6.length;
                  int var8 = 0;

                  while(true) {
                     if (var8 >= var7) {
                        continue label87;
                     }

                     Priority p = var6[var8];
                     if (p.toString().equals(priorityStr)) {
                        this.priority = p;
                        continue label87;
                     }

                     ++var8;
                  }
               case "显示血量":
                  this.showDamageIndicator = setting.getBoolean();
                  break;
               case "绕过命名":
                  this.ignoreNamed = setting.getBoolean();
                  break;
               case "绕过被动":
                  this.ignorePassive = setting.getBoolean();
            }
         }

         if (!(mc.f_91074_.m_36403_(0.5F) < 1.0F)) {
            if (this.attackMode == KillauraHack.AttackMode.SINGLE_TARGET) {
               this.handleSingleTargetMode();
            } else {
               this.handleMultiTargetMode();
            }

         } else {
            if (this.hasTarget && this.currentTarget != null && this.currentTarget.m_6084_() && mc.f_91074_.m_20270_(this.currentTarget) <= this.attackRange) {
               this.lookAtTarget(this.currentTarget);
               this.renderTarget = this.currentTarget;
            } else {
               this.hasTarget = false;
               this.currentTarget = null;
               this.renderTarget = null;
               if (!this.isAnyHeadRotationActive()) {
                  HeadOnlyLook.stopRotation();
               }
            }

         }
      }
   }

   private void handleSingleTargetMode() {
      List targets = this.getValidTargets();
      if (targets.isEmpty()) {
         this.hasTarget = false;
         this.currentTarget = null;
         this.renderTarget = null;
         if (!this.isAnyHeadRotationActive()) {
         }

      } else {
         LivingEntity target = this.selectTargetByPriority(targets);
         if (target != null && mc.f_91074_.m_20270_(target) <= this.attackRange) {
            this.lookAndAttack(target);
            this.currentTarget = target;
            this.renderTarget = target;
            this.hasTarget = true;
         } else {
            this.hasTarget = false;
            this.currentTarget = null;
            this.renderTarget = null;
            if (!this.isAnyHeadRotationActive()) {
            }
         }

      }
   }

   private void handleMultiTargetMode() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastTargetSwitchTime > 0L || this.currentTargets.isEmpty()) {
         this.updateMultiTargetList();
         this.lastTargetSwitchTime = currentTime;
      }

      if (this.currentTargets.isEmpty()) {
         this.hasTarget = false;
         this.currentTarget = null;
         this.renderTarget = null;
         if (!this.isAnyHeadRotationActive()) {
         }

      } else {
         this.attackMultiTargets();
      }
   }

   private void updateMultiTargetList() {
      List validTargets = this.getValidTargets();
      this.currentTargets.clear();
      Iterator var2 = validTargets.iterator();

      while(var2.hasNext()) {
         LivingEntity target = (LivingEntity)var2.next();
         this.currentTargets.add(target.m_20148_());
      }

      if (this.currentTargetIndex >= this.currentTargets.size()) {
         this.currentTargetIndex = 0;
      }

   }

   private void attackMultiTargets() {
      if (this.currentTargets.isEmpty()) {
         this.hasTarget = false;
         this.currentTarget = null;
         this.renderTarget = null;
         if (!this.isAnyHeadRotationActive()) {
         }

      } else {
         UUID targetId = (UUID)this.currentTargets.get(this.currentTargetIndex);
         LivingEntity target = this.findEntityByUUID(targetId);
         if (target != null && target.m_6084_() && !(mc.f_91074_.m_20270_(target) > this.attackRange)) {
            this.lookAndAttack(target);
            this.currentTarget = target;
            this.renderTarget = target;
            this.hasTarget = true;
            ++this.currentTargetIndex;
            if (this.currentTargetIndex >= this.currentTargets.size()) {
               this.currentTargetIndex = 0;
            }

         } else {
            this.currentTargets.remove(this.currentTargetIndex);
            if (this.currentTargetIndex >= this.currentTargets.size()) {
               this.currentTargetIndex = 0;
            }

            if (this.currentTargets.isEmpty()) {
               this.hasTarget = false;
               this.currentTarget = null;
               this.renderTarget = null;
               if (!this.isAnyHeadRotationActive()) {
               }
            }

         }
      }
   }

   private LivingEntity selectTargetByPriority(List targets) {
      if (targets.isEmpty()) {
         return null;
      } else {
         switch (this.priority) {
            case DISTANCE:
               return (LivingEntity)targets.stream().min(Comparator.comparingDouble((e) -> {
                  return mc.f_91074_.m_20280_(e);
               })).orElse((Object)null);
            case ANGLE:
               return (LivingEntity)targets.stream().min(Comparator.comparingDouble((e) -> {
                  return this.getAngleToEntity(e);
               })).orElse((Object)null);
            case HEALTH:
               return (LivingEntity)targets.stream().min(Comparator.comparingDouble((e) -> {
                  return (double)e.m_21223_();
               })).orElse((Object)null);
            default:
               return (LivingEntity)targets.get(0);
         }
      }
   }

   private double getAngleToEntity(LivingEntity entity) {
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      Vec3 targetPos = entity.m_20191_().m_82399_();
      Vec3 toTarget = targetPos.m_82546_(eyesPos).m_82541_();
      Vec3 lookVec = mc.f_91074_.m_20154_();
      return Math.toDegrees(Math.acos(lookVec.m_82526_(toTarget)));
   }

   private LivingEntity findEntityByUUID(UUID uuid) {
      Iterator var2 = mc.f_91073_.m_104735_().iterator();

      Entity entity;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         entity = (Entity)var2.next();
      } while(!(entity instanceof LivingEntity) || !entity.m_20148_().equals(uuid));

      return (LivingEntity)entity;
   }

   private void lookAndAttack(LivingEntity target) {
      this.lookAtTarget(target);
      this.attackEntity(target);
   }

   private void lookAtTarget(LivingEntity target) {
      LocalPlayer player = mc.f_91074_;
      Vec3 playerPos = player.m_146892_();
      Vec3 targetPos = target.m_20191_().m_82399_();
      Vec3 direction = targetPos.m_82546_(playerPos);
      double distanceXZ = Math.sqrt(direction.f_82479_ * direction.f_82479_ + direction.f_82481_ * direction.f_82481_);
      float yaw = (float)Math.toDegrees(Math.atan2(direction.f_82481_, direction.f_82479_)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(direction.f_82480_, distanceXZ)));
      yaw = this.normalizeAngle(yaw);
      pitch = Mth.m_14036_(pitch, -90.0F, 90.0F);
      mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.m_20096_()));
      if (!this.isAnyHeadRotationActive()) {
         if (!HeadOnlyLook.isRotating()) {
            HeadOnlyLook.startRotation(yaw, pitch);
         } else {
            HeadOnlyLook.updateRotation(yaw, pitch);
         }
      }

   }

   private List getValidTargets() {
      List targets = new ArrayList();
      LocalPlayer player = mc.f_91074_;
      Iterator var3 = mc.f_91073_.m_104735_().iterator();

      while(true) {
         LivingEntity living;
         while(true) {
            Entity entity;
            do {
               String entityId;
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
                        } while(player.m_20270_(entity) > this.attackRange);
                     } while(entity instanceof Player && FriendsManager.getInstance().isFriend((Player)entity));

                     entityId = this.getEntityTypeId(entity);
                  } while(entityId != null && this.whitelistEntities.contains(entityId));
               } while(!living.m_6084_());
            } while(this.ignoreNamed && entity.m_8077_());

            if (!this.ignorePassive) {
               break;
            }

            if (!(living instanceof Animal)) {
               if (!(living instanceof NeutralMob) || !(living instanceof Mob)) {
                  break;
               }

               Mob mob2 = (Mob)living;
               if (mob2.m_5912_()) {
                  break;
               }
            }
         }

         targets.add(living);
      }
   }

   private String getEntityTypeId(Entity entity) {
      ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_());
      return key != null ? key.toString() : null;
   }

   private void attackEntity(LivingEntity target) {
      LocalPlayer player = mc.f_91074_;
      ServerboundInteractPacket attackPacket = ServerboundInteractPacket.m_179605_(target, mc.f_91074_.m_6144_());
      mc.m_91403_().m_104955_(attackPacket);
      mc.m_91403_().m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
      mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
      player.m_36334_();
      this.lastAttackTime.put(target.m_20148_(), System.currentTimeMillis());
      if (this.showDamageIndicator) {
         HealthAnimation anim = (HealthAnimation)this.healthAnimations.get(target.m_20148_());
         if (anim == null) {
            this.healthAnimations.put(target.m_20148_(), new HealthAnimation(target));
         } else {
            anim.update();
         }
      }

      this.cleanupAttackTimes();
   }

   private void resetMultiTargetState() {
      this.currentTargets.clear();
      this.currentTargetIndex = 0;
      this.lastTargetSwitchTime = 0L;
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

   private void cleanupAttackTimes() {
      long currentTime = System.currentTimeMillis();
      this.lastAttackTime.entrySet().removeIf((entry) -> {
         return currentTime - (Long)entry.getValue() > 1000L;
      });
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && this.showDamageIndicator && !this.healthAnimations.isEmpty() && mc.f_91074_ != null) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.depthMask(false);
         RenderSystem.lineWidth(3.0F);
         Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
         poseStack.m_85836_();
         poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
         Iterator var4 = this.healthAnimations.values().iterator();

         while(var4.hasNext()) {
            HealthAnimation anim = (HealthAnimation)var4.next();
            if (anim.active && anim.entity != null && anim.entity.m_6084_()) {
               double x = anim.entity.m_20185_();
               double y = anim.entity.m_20186_() + (double)anim.entity.m_20206_() + 1.2;
               double z = anim.entity.m_20189_();
               float healthPercent = anim.getHealthPercent();
               float red = 1.0F - healthPercent;
               poseStack.m_85836_();
               poseStack.m_85837_(x, y, z);
               Vec3 toCamera = (new Vec3(cameraPos.f_82479_ - x, 0.0, cameraPos.f_82481_ - z)).m_82541_();
               float yaw = (float)Math.toDegrees(Math.atan2(toCamera.f_82481_, toCamera.f_82479_)) - 90.0F;
               poseStack.m_252781_(Axis.f_252392_.m_252977_(yaw));
               this.render3DHealthBar(poseStack, 0.0, 0.0, 0.0, 1.5F, 0.3F, 0.2F, red, healthPercent, 0.0F, 1.0F, healthPercent);
               poseStack.m_85849_();
            }
         }

         poseStack.m_85849_();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.lineWidth(1.0F);
      }
   }

   private void render3DHealthBar(PoseStack poseStack, double x, double y, double z, float width, float height, float depth, float r, float g, float b, float a, float percent) {
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder buffer = tesselator.m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      float halfWidth = width / 2.0F;
      float halfDepth = depth / 2.0F;
      float fillWidth = width * percent;
      float fillHalf = fillWidth / 2.0F;
      buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y + height, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y + height, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y + height, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y + height, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + halfWidth, (float)y + height, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y + height, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y + height, (float)z + halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - halfWidth, (float)y + height, (float)z - halfDepth).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();

      for(int i = 0; i < 4; ++i) {
         float x1 = i != 0 && i != 3 ? halfWidth : -halfWidth;
         float z1 = i < 2 ? -halfDepth : halfDepth;
         buffer.m_252986_(matrix, (float)x + x1, (float)y, (float)z + z1).m_85950_(0.3F, 0.3F, 0.3F, a * 0.5F).m_5752_();
         buffer.m_252986_(matrix, (float)x + x1, (float)y + height, (float)z + z1).m_85950_(0.3F, 0.3F, 0.3F, a).m_5752_();
      }

      tesselator.m_85914_();
      buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.3F).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.3F).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.3F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.3F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x - fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z - halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z - halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y + height, (float)z + halfDepth).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(matrix, (float)x + fillHalf, (float)y, (float)z + halfDepth).m_85950_(r, g, b, a * 0.5F).m_5752_();
      tesselator.m_85914_();
   }

   public void onClick() {
      this.toggle();
   }

   public Set getWhitelist() {
      return this.whitelistEntities;
   }

   public static enum AttackMode {
      SINGLE_TARGET("单目标"),
      MULTI_TARGET("多目标");

      private final String displayName;

      private AttackMode(String displayName) {
         this.displayName = displayName;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static AttackMode[] $values() {
         return new AttackMode[]{SINGLE_TARGET, MULTI_TARGET};
      }
   }

   public static enum Priority {
      DISTANCE("距离"),
      ANGLE("角度"),
      HEALTH("血量");

      private final String displayName;

      private Priority(String displayName) {
         this.displayName = displayName;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static Priority[] $values() {
         return new Priority[]{DISTANCE, ANGLE, HEALTH};
      }
   }

   private static class HealthAnimation {
      LivingEntity entity;
      float health;
      float maxHealth;
      long lastUpdateTime;
      boolean active = true;

      HealthAnimation(LivingEntity entity) {
         this.entity = entity;
         this.health = entity.m_21223_();
         this.maxHealth = entity.m_21233_();
         this.lastUpdateTime = System.currentTimeMillis();
      }

      void update() {
         this.health = this.entity.m_21223_();
         this.lastUpdateTime = System.currentTimeMillis();
      }

      float getHealthPercent() {
         return this.health / this.maxHealth;
      }

      boolean shouldRender() {
         return System.currentTimeMillis() - this.lastUpdateTime < 3000L;
      }
   }
}
