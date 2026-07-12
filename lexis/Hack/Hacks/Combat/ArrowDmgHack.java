package lexis.Hack.Hacks.Combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ArrowDmgHack extends Hack {
   private static final Minecraft mc = Minecraft.m_91087_();
   private HackConfig config;
   private static final String CONFIG_KEY = "没敌机关枪";
   private int packets = 200;
   private boolean yeetTridents = false;
   private boolean vertical = true;
   private boolean fallProtect = true;
   private boolean smartStrength = true;
   private boolean autoShoot = true;
   private int charge = 4;
   private boolean onlyWhenHolding = true;
   private boolean totemBypass = false;
   private int bypassDelay = 4;
   private boolean isSecondShot = false;
   private int bypassTimer = -1;
   private boolean renderEnabled = true;
   private int renderMaxDistance = 0;
   private SettingColor boxColor = new SettingColor(255, 255, 0, 180);
   private SettingColor linesColor = new SettingColor(255, 0, 0, 180);
   private SettingColor sidesColor = new SettingColor(0, 255, 0, 180);
   private boolean glowEnabled = false;
   private int glowColor = 16777215;
   private boolean showBox = true;
   private boolean showLines = true;
   private boolean showSides = true;
   private boolean forcedPress = false;
   private Entity targetEntity = null;
   private static final String GLOW_SOURCE = "没敌机关枪";
   public static ArrowDmgHack INSTANCE;

   public ArrowDmgHack() {
      super("没敌机关枪", "弓箭伤害加强高配版", Hack.Category.COMBAT, true);
      INSTANCE = this;
      this.addSetting(new Hack.Setting("数据包数量", "发送的移动包数量", 200, 2, 7000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("三叉戟模式", "是否对三叉戟生效", false));
      this.addSetting(new Hack.Setting("垂直修正", "修正Y轴偏移，增强伤害", true));
      this.addSetting(new Hack.Setting("防摔", "防止自己摔死", true));
      this.addSetting(new Hack.Setting("智能空间检测", "避免卡入方块", true));
      this.addSetting(new Hack.Setting("开启连射", "自动蓄力射击", true));
      this.addSetting(new Hack.Setting("蓄力时间", "蓄力刻数", 4, 1, 20, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("仅右键连射", "只有按住右键才连射", true));
      this.addSetting(new Hack.Setting("图腾绕过", "双发破图腾", false));
      this.addSetting(new Hack.Setting("第二箭延迟", "两箭间隔刻数", 4, 1, 10, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("显示瞄准实体", "显示当前瞄准的实体", true));
      this.addSetting(new Hack.Setting("最大渲染距离", "实体渲染距离(0=无限)", 0, 0, 1024, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("方框颜色", "ESP方框颜色", this.boxColor.getPacked()));
      this.addSetting(new Hack.Setting("连线颜色", "ESP连线颜色", this.linesColor.getPacked()));
      this.addSetting(new Hack.Setting("六面颜色", "ESP六面填充颜色", this.sidesColor.getPacked()));
      this.addSetting(new Hack.Setting("显示方框", "是否显示方框", true));
      this.addSetting(new Hack.Setting("显示连线", "是否显示连线", true));
      this.addSetting(new Hack.Setting("显示六面", "是否显示六面填充", true));
      this.addSetting(new Hack.Setting("发光模式", "伪造原版发光效果", false));
      this.addSetting(new Hack.Setting("发光颜色", "发光颜色", this.glowColor));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.packets = this.config.getIntSetting("没敌机关枪", "数据包数量", 200);
      this.yeetTridents = this.config.getBooleanSetting("没敌机关枪", "三叉戟模式", false);
      this.vertical = this.config.getBooleanSetting("没敌机关枪", "垂直修正", true);
      this.fallProtect = this.config.getBooleanSetting("没敌机关枪", "防摔", true);
      this.smartStrength = this.config.getBooleanSetting("没敌机关枪", "智能空间检测", true);
      this.autoShoot = this.config.getBooleanSetting("没敌机关枪", "开启连射", true);
      this.charge = this.config.getIntSetting("没敌机关枪", "蓄力时间", 4);
      this.onlyWhenHolding = this.config.getBooleanSetting("没敌机关枪", "仅右键连射", true);
      this.totemBypass = this.config.getBooleanSetting("没敌机关枪", "图腾绕过", false);
      this.bypassDelay = this.config.getIntSetting("没敌机关枪", "第二箭延迟", 4);
      this.renderEnabled = this.config.getBooleanSetting("没敌机关枪", "显示瞄准实体", true);
      this.renderMaxDistance = this.config.getIntSetting("没敌机关枪", "最大渲染距离", 0);
      this.boxColor = new SettingColor(this.config.getIntSetting("没敌机关枪", "方框颜色", this.boxColor.getPacked()));
      this.linesColor = new SettingColor(this.config.getIntSetting("没敌机关枪", "连线颜色", this.linesColor.getPacked()));
      this.sidesColor = new SettingColor(this.config.getIntSetting("没敌机关枪", "六面颜色", this.sidesColor.getPacked()));
      this.showBox = this.config.getBooleanSetting("没敌机关枪", "显示方框", true);
      this.showLines = this.config.getBooleanSetting("没敌机关枪", "显示连线", true);
      this.showSides = this.config.getBooleanSetting("没敌机关枪", "显示六面", true);
      this.glowEnabled = this.config.getBooleanSetting("没敌机关枪", "发光模式", false);
      this.glowColor = this.config.getIntSetting("没敌机关枪", "发光颜色", 16777215);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "数据包数量":
               s.setValue((double)this.packets);
               break;
            case "三叉戟模式":
               s.setValue(this.yeetTridents);
               break;
            case "垂直修正":
               s.setValue(this.vertical);
               break;
            case "防摔":
               s.setValue(this.fallProtect);
               break;
            case "智能空间检测":
               s.setValue(this.smartStrength);
               break;
            case "开启连射":
               s.setValue(this.autoShoot);
               break;
            case "蓄力时间":
               s.setValue((double)this.charge);
               break;
            case "仅右键连射":
               s.setValue(this.onlyWhenHolding);
               break;
            case "图腾绕过":
               s.setValue(this.totemBypass);
               break;
            case "第二箭延迟":
               s.setValue((double)this.bypassDelay);
               break;
            case "显示瞄准实体":
               s.setValue(this.renderEnabled);
               break;
            case "最大渲染距离":
               s.setValue((double)this.renderMaxDistance);
               break;
            case "方框颜色":
               s.setValue(this.boxColor.getPacked());
               break;
            case "连线颜色":
               s.setValue(this.linesColor.getPacked());
               break;
            case "六面颜色":
               s.setValue(this.sidesColor.getPacked());
               break;
            case "显示方框":
               s.setValue(this.showBox);
               break;
            case "显示连线":
               s.setValue(this.showLines);
               break;
            case "显示六面":
               s.setValue(this.showSides);
               break;
            case "发光模式":
               s.setValue(this.glowEnabled);
               break;
            case "发光颜色":
               s.setValue(this.glowColor);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("没敌机关枪", this.getSettings());
   }

   public void onEnable() {
      MinecraftForge.EVENT_BUS.register(this);
      this.forcedPress = false;
      this.isSecondShot = false;
      this.bypassTimer = -1;
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
      if (this.forcedPress) {
         mc.f_91066_.f_92095_.m_7249_(false);
         this.forcedPress = false;
      }

      if (this.targetEntity != null) {
         FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", false, 0, 0.0);
         this.targetEntity = null;
      }

   }

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         if (mc.f_91074_ != null) {
            this.updateTargetEntity();
            if (this.totemBypass && this.bypassTimer > 0) {
               mc.f_91066_.f_92095_.m_7249_(true);
               --this.bypassTimer;
               if (this.bypassTimer == 0) {
                  if (mc.f_91074_.m_6117_() && this.isValidItem(mc.f_91074_.m_21211_())) {
                     mc.f_91072_.m_105277_(mc.f_91074_);
                  }

                  mc.f_91066_.f_92095_.m_7249_(false);
                  this.isSecondShot = false;
               }

            } else {
               boolean hasValidItem = this.isValidItem(mc.f_91074_.m_21205_()) || this.isValidItem(mc.f_91074_.m_21206_());
               if (this.autoShoot && hasValidItem) {
                  if (!this.onlyWhenHolding && !mc.f_91074_.m_6117_()) {
                     mc.f_91066_.f_92095_.m_7249_(true);
                     this.forcedPress = true;
                  }

                  if (mc.f_91074_.m_6117_() && this.isValidItem(mc.f_91074_.m_21211_()) && mc.f_91074_.m_21252_() >= this.charge) {
                     mc.f_91072_.m_105277_(mc.f_91074_);
                  }

               } else {
                  if (this.forcedPress) {
                     mc.f_91066_.f_92095_.m_7249_(false);
                     this.forcedPress = false;
                  }

               }
            }
         }
      }
   }

   private void doArrowDMG() {
      if (mc.f_91074_ != null) {
         mc.f_91074_.f_108617_.m_104955_(new ServerboundPlayerCommandPacket(mc.f_91074_, Action.START_SPRINTING));
         double x = mc.f_91074_.m_20185_();
         double y = mc.f_91074_.m_20186_();
         double z = mc.f_91074_.m_20189_();
         double step = 1.0E-10;
         if (!this.vertical) {
            for(int i = 0; i < this.packets; ++i) {
               mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y, z, true));
            }

         } else {
            double safeYOffset = step;
            if (this.smartStrength) {
               Vec3 start = mc.f_91074_.m_20182_();
               Vec3 endUp = start.m_82520_(0.0, step, 0.0);
               Vec3 endDown = start.m_82520_(0.0, -step, 0.0);
               ClipContext upCtx = new ClipContext(start, endUp, Block.COLLIDER, Fluid.NONE, mc.f_91074_);
               ClipContext downCtx = new ClipContext(start, endDown, Block.COLLIDER, Fluid.NONE, mc.f_91074_);
               if (mc.f_91073_.m_45547_(upCtx).m_6662_() == Type.BLOCK) {
                  safeYOffset = 0.0;
               } else if (mc.f_91073_.m_45547_(downCtx).m_6662_() == Type.BLOCK) {
                  safeYOffset = 0.0;
               }
            }

            for(int i = 0; i < this.packets / 2; ++i) {
               mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y - safeYOffset, z, true));
               mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y + safeYOffset, z, false));
            }

            if (this.fallProtect) {
               mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.Pos(x, y - 0.01, z, true));
            }

         }
      }
   }

   public void processShoot() {
      if (this.totemBypass) {
         if (!this.isSecondShot) {
            this.isSecondShot = true;
            this.bypassTimer = this.bypassDelay;
         } else {
            this.isSecondShot = false;
         }
      }

   }

   private boolean isValidItem(ItemStack stack) {
      if (stack.m_41619_()) {
         return false;
      } else {
         Item item = stack.m_41720_();
         if (this.yeetTridents && item == Items.f_42713_) {
            return true;
         } else {
            return item == Items.f_42411_;
         }
      }
   }

   private boolean isValidItem(Item item) {
      if (this.yeetTridents && item == Items.f_42713_) {
         return true;
      } else {
         return item == Items.f_42411_;
      }
   }

   private void updateTargetEntity() {
      if (this.isEnabled() && this.renderEnabled && mc.f_91074_ != null) {
         boolean hasValid = this.isValidItem(mc.f_91074_.m_21205_()) || this.isValidItem(mc.f_91074_.m_21206_());
         if (!hasValid) {
            if (this.targetEntity != null) {
               FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", false, 0, 0.0);
               this.targetEntity = null;
            }

         } else {
            Entity newTarget = this.getTargetEntityInfiniteRange();
            double maxDist;
            if (newTarget != this.targetEntity) {
               if (this.targetEntity != null) {
                  FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", false, 0, 0.0);
               }

               this.targetEntity = newTarget;
               if (this.targetEntity != null && this.glowEnabled) {
                  maxDist = this.renderMaxDistance <= 0 ? 1024.0 : (double)this.renderMaxDistance;
                  FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", true, this.glowColor, maxDist);
               }
            } else if (this.targetEntity != null && this.glowEnabled) {
               maxDist = this.renderMaxDistance <= 0 ? 1024.0 : (double)this.renderMaxDistance;
               FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", true, this.glowColor, maxDist);
            }

         }
      } else {
         if (this.targetEntity != null) {
            FakeGlowManager.setGlow(this.targetEntity, "没敌机关枪", false, 0, 0.0);
            this.targetEntity = null;
         }

      }
   }

   private Entity getTargetEntityInfiniteRange() {
      double maxDist = this.renderMaxDistance <= 0 ? 1024.0 : (double)this.renderMaxDistance;
      Entity closest = null;
      double bestDist = maxDist + 1.0;
      Vec3 eyePos = mc.f_91074_.m_146892_();
      Iterator var7 = mc.f_91073_.m_104735_().iterator();

      while(true) {
         Entity entity;
         double dist;
         double angle;
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var7.hasNext()) {
                                 return closest;
                              }

                              entity = (Entity)var7.next();
                           } while(!(entity instanceof LivingEntity));
                        } while(entity == mc.f_91074_);
                     } while(!entity.m_6084_());

                     dist = eyePos.m_82554_(entity.m_20182_());
                  } while(dist > maxDist);
               } while(!this.hasLineOfSight(eyePos, entity.m_20191_().m_82399_()));

               angle = this.getAngleToEntity(entity);
            } while(!(angle < 5.0));
         } while(closest != null && !(dist < bestDist));

         closest = entity;
         bestDist = dist;
      }
   }

   private boolean hasLineOfSight(Vec3 from, Vec3 to) {
      HitResult result = mc.f_91073_.m_45547_(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
      return result.m_6662_() == Type.MISS || result.m_82450_().m_82554_(to) < 0.1;
   }

   private double getAngleToEntity(Entity entity) {
      Vec3 eye = mc.f_91074_.m_146892_();
      Vec3 toEntity = entity.m_20191_().m_82399_().m_82546_(eye).m_82541_();
      Vec3 look = mc.f_91074_.m_20154_();
      return Math.toDegrees(Math.acos(look.m_82526_(toEntity)));
   }

   public void onUpdate() {
      if (this.isEnabled() && mc.f_91074_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "数据包数量":
                  int newPackets = s.getInt();
                  if (newPackets != this.packets) {
                     this.packets = newPackets;
                     needSave = true;
                  }
                  break;
               case "三叉戟模式":
                  boolean newYeet = s.getBoolean();
                  if (newYeet != this.yeetTridents) {
                     this.yeetTridents = newYeet;
                     needSave = true;
                  }
                  break;
               case "垂直修正":
                  boolean newVert = s.getBoolean();
                  if (newVert != this.vertical) {
                     this.vertical = newVert;
                     needSave = true;
                  }
                  break;
               case "防摔":
                  boolean newFall = s.getBoolean();
                  if (newFall != this.fallProtect) {
                     this.fallProtect = newFall;
                     needSave = true;
                  }
                  break;
               case "智能空间检测":
                  boolean newSmart = s.getBoolean();
                  if (newSmart != this.smartStrength) {
                     this.smartStrength = newSmart;
                     needSave = true;
                  }
                  break;
               case "开启连射":
                  boolean newAuto = s.getBoolean();
                  if (newAuto != this.autoShoot) {
                     this.autoShoot = newAuto;
                     needSave = true;
                  }
                  break;
               case "蓄力时间":
                  int newCharge = s.getInt();
                  if (newCharge != this.charge) {
                     this.charge = newCharge;
                     needSave = true;
                  }
                  break;
               case "仅右键连射":
                  boolean newOnly = s.getBoolean();
                  if (newOnly != this.onlyWhenHolding) {
                     this.onlyWhenHolding = newOnly;
                     needSave = true;
                  }
                  break;
               case "图腾绕过":
                  boolean newTotem = s.getBoolean();
                  if (newTotem != this.totemBypass) {
                     this.totemBypass = newTotem;
                     needSave = true;
                  }
                  break;
               case "第二箭延迟":
                  int newDelay = s.getInt();
                  if (newDelay != this.bypassDelay) {
                     this.bypassDelay = newDelay;
                     needSave = true;
                  }
                  break;
               case "显示瞄准实体":
                  boolean newRender = s.getBoolean();
                  if (newRender != this.renderEnabled) {
                     this.renderEnabled = newRender;
                     needSave = true;
                  }
                  break;
               case "最大渲染距离":
                  int newMaxDist = s.getInt();
                  if (newMaxDist != this.renderMaxDistance) {
                     this.renderMaxDistance = newMaxDist;
                     needSave = true;
                  }
                  break;
               case "方框颜色":
                  int newBox = s.getInt();
                  if (newBox != this.boxColor.getPacked()) {
                     this.boxColor = new SettingColor(newBox);
                     needSave = true;
                  }
                  break;
               case "连线颜色":
                  int newLines = s.getInt();
                  if (newLines != this.linesColor.getPacked()) {
                     this.linesColor = new SettingColor(newLines);
                     needSave = true;
                  }
                  break;
               case "六面颜色":
                  int newSides = s.getInt();
                  if (newSides != this.sidesColor.getPacked()) {
                     this.sidesColor = new SettingColor(newSides);
                     needSave = true;
                  }
                  break;
               case "显示方框":
                  boolean newShowBox = s.getBoolean();
                  if (newShowBox != this.showBox) {
                     this.showBox = newShowBox;
                     needSave = true;
                  }
                  break;
               case "显示连线":
                  boolean newShowLines = s.getBoolean();
                  if (newShowLines != this.showLines) {
                     this.showLines = newShowLines;
                     needSave = true;
                  }
                  break;
               case "显示六面":
                  boolean newShowSides = s.getBoolean();
                  if (newShowSides != this.showSides) {
                     this.showSides = newShowSides;
                     needSave = true;
                  }
                  break;
               case "发光模式":
                  boolean newGlow = s.getBoolean();
                  if (newGlow != this.glowEnabled) {
                     this.glowEnabled = newGlow;
                     needSave = true;
                  }
                  break;
               case "发光颜色":
                  int newGlowColor = s.getInt();
                  if (newGlowColor != this.glowColor) {
                     this.glowColor = newGlowColor;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         if (mc.f_91074_.m_6117_()) {
            Item usingItem = mc.f_91074_.m_21205_().m_41720_();
            if (this.isValidItem(usingItem)) {
               this.doArrowDMG();
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   @SubscribeEvent
   public void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_BLOCK_ENTITIES) {
         if (this.isEnabled() && this.renderEnabled) {
            if (this.targetEntity != null && this.targetEntity.m_6084_()) {
               if (this.isValidItem(mc.f_91074_.m_21205_()) || this.isValidItem(mc.f_91074_.m_21206_())) {
                  PoseStack poseStack = event.getPoseStack();
                  AABB box = this.targetEntity.m_20191_().m_82400_(0.05);
                  if (this.showSides) {
                     RenderUtils.drawSolidBoxes(poseStack, List.of(box), this.sidesColor.getPacked(), false);
                  }

                  if (this.showBox) {
                     RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), this.boxColor.getPacked(), false);
                  }

                  if (this.showLines) {
                     List centers = List.of(this.targetEntity.m_20191_().m_82399_());
                     RenderUtils.drawTracers(poseStack, event.getPartialTick(), centers, this.linesColor.getPacked(), false);
                  }

               }
            }
         }
      }
   }
}
