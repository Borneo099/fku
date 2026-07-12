package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.ServerGUtils.TickRate;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.TpAuraCore;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketEvent;
import lexis.Hack.events.PacketReceiveListener;
import lexis.Hack.events.PacketSendListener;
import lexis.Hack.events.UpdateListener;
import lexis.mixinterface.IPlayerMoveC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TpAuraHack extends Hack implements UpdateListener, PacketReceiveListener, PacketSendListener {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String CONFIG_KEY = "传送光环(远杀)";
   private final HackConfig config;
   private Weapon weapon;
   private boolean autoSwitch;
   private boolean swapBack;
   private ShieldMode shieldMode;
   private boolean ignorePassive;
   private boolean ignoreTamed;
   private boolean ignoreNamed;
   private double tpRange;
   private boolean simpleMode;
   private double attackOffset;
   private boolean returnPos;
   private boolean s08Return;
   private double maxStep;
   private boolean airPath;
   private boolean tpsSync;
   private double hitDelayMult;
   private boolean pauseOnLag;
   private boolean noFall;
   private boolean dynamicTarget;
   private TpAuraCore core;
   private boolean swapped;
   private int previousSlot;
   private Vec3 originalPos;
   private boolean pendingS08Return;
   private int sinceLastAttack;
   private TpPhase phase;
   private List tpPath;
   private int tpIndex;
   private Entity tpTarget;
   private Vec3 tpAttackPos;
   private volatile boolean suppressAutoMove;
   private Vec3 lastSentPos;
   private int tpTicks;
   private int tpCooldown;
   private static final int MAX_TP_TICKS = 80;
   private final Set whitelistEntities;
   private volatile List renderPath;
   private volatile Vec3 renderAttackPos;
   private long renderExpireMs;
   private static final long RENDER_DURATION_MS = 800L;
   private static final int COLOR_PATH = -16711681;
   private static final int COLOR_BOX_OUTLINE = -22016;
   private static final int COLOR_BOX_FILL = 1073785599;

   public TpAuraHack() {
      super("传送光环(远杀)", new String[]{"没敌传送光环", "加强版。。。"}, Hack.Category.COMBAT, true);
      this.weapon = TpAuraHack.Weapon.All;
      this.autoSwitch = true;
      this.swapBack = true;
      this.shieldMode = TpAuraHack.ShieldMode.Break;
      this.ignorePassive = true;
      this.ignoreTamed = true;
      this.ignoreNamed = false;
      this.tpRange = 64.0;
      this.simpleMode = true;
      this.attackOffset = 2.5;
      this.returnPos = true;
      this.s08Return = false;
      this.maxStep = 2.5;
      this.airPath = true;
      this.tpsSync = true;
      this.hitDelayMult = 1.0;
      this.pauseOnLag = false;
      this.noFall = true;
      this.dynamicTarget = true;
      this.swapped = false;
      this.previousSlot = -1;
      this.originalPos = null;
      this.pendingS08Return = false;
      this.sinceLastAttack = 100;
      this.phase = TpAuraHack.TpPhase.IDLE;
      this.tpPath = null;
      this.tpIndex = 0;
      this.tpTarget = null;
      this.tpAttackPos = null;
      this.suppressAutoMove = false;
      this.lastSentPos = null;
      this.tpTicks = 0;
      this.tpCooldown = 0;
      this.whitelistEntities = ConcurrentHashMap.newKeySet();
      this.renderPath = null;
      this.renderAttackPos = null;
      this.renderExpireMs = 0L;
      this.addSetting(new Hack.Setting("简化模式", "不找路径直接 TP(这使用是绕过弱反作弊插件)", true));
      this.addSetting(new Hack.Setting("武器模式", "瞬移过去后优先使用的武器", this.weapon.toString(), new String[]{"Sword", "Axe", "All"}));
      this.addSetting(new Hack.Setting("自动切换武器", "自动寻找合适武器", this.autoSwitch));
      this.addSetting(new Hack.Setting("切回原位", "攻击后切回原物品", this.swapBack));
      this.addSetting(new Hack.Setting("破盾模式", "目标举盾时切斧头", this.shieldMode.toString(), new String[]{"Break", "Ignore", "None"}));
      this.addSetting(new Hack.Setting("绕过被动", "动物 / 非敌对中立怪等被动生物不作为攻击目标", this.ignorePassive));
      this.addSetting(new Hack.Setting("瞬移范围", "最大瞬移距离", this.tpRange, 5.0, 200.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("攻击距离", "距目标多远进行攻击", this.attackOffset, 1.5, 4.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("回传", "攻击后发包回原位", this.returnPos));
      this.addSetting(new Hack.Setting("防真传", "S08 拉回时紧急回原点", this.s08Return));
      this.addSetting(new Hack.Setting("防摔", "传送途中发包 onGround=false, 骗服务器不结算摔伤", this.noFall));
      this.addSetting(new Hack.Setting("动态玩家位置", "途中实时追踪目标坐标(追飞天作弊玩家), 进入攻击距离即命中并回传", this.dynamicTarget));
      this.addSetting(new Hack.Setting("单次步长", "每包最大距离", this.maxStep, 1.0, 10.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("垂直穿墙", "仅找路模式：允许垂直穿墙", this.airPath));
      this.addSetting(new Hack.Setting("TPS同步", "服务器延迟冷却", this.tpsSync));
      this.addSetting(new Hack.Setting("攻击倍率", "攻击冷却系数", this.hitDelayMult, 0.1, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("卡顿暂停", "延迟过高时暂停", this.pauseOnLag));
      this.addSetting(new Hack.Setting("跳过实体", "选择不攻击的实体", "打开选择界面", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "tpaura", this::reloadWhitelistFromConfig));
         }

      }));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.simpleMode = this.config.getBooleanSetting("传送光环(远杀)", "简化模式", true);
      String w = this.config.getStringSetting("传送光环(远杀)", "武器模式", "All");
      Weapon[] var2 = TpAuraHack.Weapon.values();
      int var3 = var2.length;

      int var4;
      for(var4 = 0; var4 < var3; ++var4) {
         Weapon wp = var2[var4];
         if (wp.toString().equals(w)) {
            this.weapon = wp;
         }
      }

      this.autoSwitch = this.config.getBooleanSetting("传送光环(远杀)", "自动切换武器", true);
      this.swapBack = this.config.getBooleanSetting("传送光环(远杀)", "切回原位", true);
      String s = this.config.getStringSetting("传送光环(远杀)", "破盾模式", "Break");
      ShieldMode[] var8 = TpAuraHack.ShieldMode.values();
      var4 = var8.length;

      for(int var11 = 0; var11 < var4; ++var11) {
         ShieldMode sm = var8[var11];
         if (sm.toString().equals(s)) {
            this.shieldMode = sm;
         }
      }

      this.ignorePassive = this.config.getBooleanSetting("传送光环(远杀)", "绕过被动", true);
      this.tpRange = this.config.getDoubleSetting("传送光环(远杀)", "瞬移范围", 64.0);
      this.attackOffset = this.config.getDoubleSetting("传送光环(远杀)", "攻击距离", 2.5);
      this.returnPos = this.config.getBooleanSetting("传送光环(远杀)", "回传", true);
      this.s08Return = this.config.getBooleanSetting("传送光环(远杀)", "防真传", false);
      this.noFall = this.config.getBooleanSetting("传送光环(远杀)", "防摔", true);
      this.dynamicTarget = this.config.getBooleanSetting("传送光环(远杀)", "动态玩家位置", true);
      this.maxStep = this.config.getDoubleSetting("传送光环(远杀)", "单次步长", 2.5);
      this.airPath = this.config.getBooleanSetting("传送光环(远杀)", "垂直穿墙", true);
      this.tpsSync = this.config.getBooleanSetting("传送光环(远杀)", "TPS同步", true);
      this.hitDelayMult = this.config.getDoubleSetting("传送光环(远杀)", "攻击倍率", 1.0);
      this.pauseOnLag = this.config.getBooleanSetting("传送光环(远杀)", "卡顿暂停", false);
      Iterator var9 = this.getSettings().iterator();

      while(var9.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var9.next();
         switch (setting.getName()) {
            case "简化模式":
               setting.setValue(this.simpleMode);
               break;
            case "武器模式":
               setting.setValue(this.weapon.toString());
               break;
            case "自动切换武器":
               setting.setValue(this.autoSwitch);
               break;
            case "切回原位":
               setting.setValue(this.swapBack);
               break;
            case "破盾模式":
               setting.setValue(this.shieldMode.toString());
               break;
            case "绕过被动":
               setting.setValue(this.ignorePassive);
               break;
            case "瞬移范围":
               setting.setValue(this.tpRange);
               break;
            case "攻击距离":
               setting.setValue(this.attackOffset);
               break;
            case "回传":
               setting.setValue(this.returnPos);
               break;
            case "防真传":
               setting.setValue(this.s08Return);
               break;
            case "防摔":
               setting.setValue(this.noFall);
               break;
            case "动态玩家位置":
               setting.setValue(this.dynamicTarget);
               break;
            case "单次步长":
               setting.setValue(this.maxStep);
               break;
            case "垂直穿墙":
               setting.setValue(this.airPath);
               break;
            case "TPS同步":
               setting.setValue(this.tpsSync);
               break;
            case "攻击倍率":
               setting.setValue(this.hitDelayMult);
               break;
            case "卡顿暂停":
               setting.setValue(this.pauseOnLag);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("传送光环(远杀)", this.getSettings());
   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      this.whitelistEntities.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_tpaura.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      if (loaded != null) {
         this.whitelistEntities.addAll(loaded);
      }

   }

   public void onEnable() {
      EventManager.add(UpdateListener.class, this);
      EventManager.add(PacketReceiveListener.class, this);
      EventManager.add(PacketSendListener.class, this);
      MinecraftForge.EVENT_BUS.register(this);
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         this.core = new TpAuraCore(mc.f_91073_, mc.f_91074_);
      }

      this.swapped = false;
      this.previousSlot = -1;
      this.sinceLastAttack = 100;
      this.originalPos = null;
      this.pendingS08Return = false;
      this.renderPath = null;
      this.renderAttackPos = null;
      this.resetTeleport();
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(PacketReceiveListener.class, this);
      EventManager.remove(PacketSendListener.class, this);
      MinecraftForge.EVENT_BUS.unregister(this);
      if (this.core != null) {
         this.core.cleanup();
         this.core = null;
      }

      if (this.swapBack && this.swapped && this.previousSlot != -1 && mc.f_91074_ != null) {
         mc.f_91074_.m_150109_().f_35977_ = this.previousSlot;
      }

      this.swapped = false;
      this.originalPos = null;
      this.pendingS08Return = false;
      this.renderPath = null;
      this.renderAttackPos = null;
      this.resetTeleport();
   }

   public void onUpdate() {
      this.syncSettingsBack();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         ++this.sinceLastAttack;
         if (this.tpCooldown > 0) {
            --this.tpCooldown;
         }

         if (this.pendingS08Return) {
            this.pendingS08Return = false;
            if (this.originalPos != null && mc.f_91074_.m_20182_().m_82554_(this.originalPos) > 3.0) {
               mc.f_91074_.m_6034_(this.originalPos.f_82479_, this.originalPos.f_82480_, this.originalPos.f_82481_);
            }

            this.originalPos = null;
         } else if (this.phase != TpAuraHack.TpPhase.IDLE) {
            this.advanceTeleport();
         } else if (this.tpCooldown <= 0) {
            if (!this.pauseOnLag || !(TickRate.INSTANCE.getTimeSinceLastTick() >= 1.5F)) {
               Entity target = this.findTarget();
               if (target == null) {
                  if (this.core != null) {
                     this.core.desyncPos = null;
                  }

               } else {
                  this.handleAutoSwitch(target);
                  float cooldown = 0.5F * (float)this.hitDelayMult;
                  if (this.tpsSync) {
                     float tps = TickRate.INSTANCE.getTickRate();
                     if (tps > 0.0F) {
                        cooldown /= tps / 20.0F;
                     }
                  }

                  if (!(mc.f_91074_.m_36403_(cooldown) < 1.0F)) {
                     this.startTeleport(target, mc.f_91074_.m_20182_());
                  }
               }
            }
         }
      }
   }

   private void startTeleport(Entity target, Vec3 localPos) {
      Vec3 targetPos = target.m_20182_();
      double dist = localPos.m_82554_(targetPos);
      if (dist <= 3.0) {
         mc.f_91072_.m_105223_(mc.f_91074_, target);
         mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         this.sinceLastAttack = 0;
      } else {
         this.originalPos = localPos;
         this.lastSentPos = localPos;
         this.tpTarget = target;
         this.tpIndex = 0;
         this.tpTicks = 0;
         this.sinceLastAttack = 0;
         Vec3 attackPos = this.computeAttackPos(target, localPos);
         this.tpAttackPos = attackPos;
         if (!this.simpleMode && !this.dynamicTarget) {
            if (this.core == null) {
               this.resetState();
               return;
            }

            this.core.setAirPath(this.airPath);
            this.core.setHClip(true);
            this.core.setAttackRange(3.0);
            this.core.updatePathfinding(localPos, target);
            List ep = this.core.getEfficientPath(this.maxStep);
            if (ep == null || ep.size() < 2) {
               this.resetState();
               return;
            }

            List waypoints = new ArrayList(ep);
            waypoints.set(0, localPos);
            waypoints.set(waypoints.size() - 1, attackPos);
            List path = this.densify(waypoints, this.maxStep);
            if (path.size() < 2) {
               this.resetState();
               return;
            }

            this.tpPath = path;
            this.markRender(path, attackPos);
         } else {
            this.tpPath = null;
            List line = new ArrayList();
            line.add(localPos);
            line.add(attackPos);
            this.markRender(line, attackPos);
         }

         this.phase = TpAuraHack.TpPhase.GOING;
         this.suppressAutoMove = true;
      }
   }

   private void resetState() {
      this.originalPos = null;
      this.lastSentPos = null;
      this.tpTarget = null;
      this.tpPath = null;
      this.tpAttackPos = null;
   }

   private void advanceTeleport() {
      if (mc.f_91074_ == null) {
         this.resetTeleport();
      } else {
         ++this.tpTicks;
         if (this.tpTicks > 80) {
            this.beginReturnOrReset();
         } else {
            Vec3 from;
            Vec3 attackPos;
            if (this.phase == TpAuraHack.TpPhase.GOING) {
               if (this.tpTarget == null || !this.tpTarget.m_6084_()) {
                  this.beginReturnOrReset();
                  return;
               }

               from = this.lastSentPos != null ? this.lastSentPos : this.originalPos;
               if (from == null) {
                  this.resetTeleport();
                  return;
               }

               if (this.tpPath != null) {
                  ++this.tpIndex;
                  if (this.tpIndex >= this.tpPath.size() - 1) {
                     this.sendPacket(this.tpAttackPos);
                     this.doAttack();
                     this.beginReturnOrReset();
                  } else {
                     this.sendPacket((Vec3)this.tpPath.get(this.tpIndex));
                  }

                  return;
               }

               attackPos = this.dynamicTarget ? this.computeAttackPos(this.tpTarget, from) : this.tpAttackPos;
               this.tpAttackPos = attackPos;
               this.renderAttackPos = attackPos;
               this.renderExpireMs = System.currentTimeMillis() + 800L;
               if (!(from.m_82554_(this.tpTarget.m_20182_()) <= this.attackOffset + 0.5) && !(from.m_82554_(attackPos) <= this.maxStep)) {
                  this.sendPacket(this.stepToward(from, attackPos, this.maxStep));
               } else {
                  this.sendPacket(attackPos);
                  this.doAttack();
                  this.beginReturnOrReset();
               }
            } else if (this.phase == TpAuraHack.TpPhase.RETURNING) {
               from = this.originalPos;
               if (from == null) {
                  this.resetTeleport();
                  return;
               }

               attackPos = this.lastSentPos != null ? this.lastSentPos : from;
               if (attackPos.m_82554_(from) <= this.maxStep) {
                  this.sendPacket(from);
                  this.resetTeleport();
               } else {
                  this.sendPacket(this.stepToward(attackPos, from, this.maxStep));
               }
            }

         }
      }
   }

   private void doAttack() {
      if (this.tpTarget != null && this.tpTarget.m_6084_()) {
         mc.f_91072_.m_105223_(mc.f_91074_, this.tpTarget);
         mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         this.sinceLastAttack = 0;
      }

   }

   private void beginReturnOrReset() {
      if (this.returnPos && this.originalPos != null) {
         this.phase = TpAuraHack.TpPhase.RETURNING;
      } else {
         this.resetTeleport();
      }

   }

   private Vec3 computeAttackPos(Entity target, Vec3 from) {
      Vec3 tp = target.m_20182_();
      Vec3 dir = from.m_82546_(tp);
      if (dir.m_82556_() < 1.0E-4) {
         dir = new Vec3(1.0, 0.0, 0.0);
      }

      dir = dir.m_82541_();
      Vec3 ap = tp.m_82549_(dir.m_82490_(this.attackOffset));
      return new Vec3(ap.f_82479_, target.m_20186_() + (double)target.m_20192_() - 0.2, ap.f_82481_);
   }

   private Vec3 stepToward(Vec3 from, Vec3 to, double step) {
      double d = from.m_82554_(to);
      if (!(d <= step) && !(d < 1.0E-6)) {
         double s = step / d;
         return new Vec3(from.f_82479_ + (to.f_82479_ - from.f_82479_) * s, from.f_82480_ + (to.f_82480_ - from.f_82480_) * s, from.f_82481_ + (to.f_82481_ - from.f_82481_) * s);
      } else {
         return to;
      }
   }

   private void resetTeleport() {
      this.phase = TpAuraHack.TpPhase.IDLE;
      this.tpPath = null;
      this.tpTarget = null;
      this.tpAttackPos = null;
      this.tpIndex = 0;
      this.tpTicks = 0;
      this.lastSentPos = null;
      this.suppressAutoMove = false;
      if (this.tpCooldown < 2) {
         this.tpCooldown = 2;
      }

   }

   private List interpolateSteps(Vec3 from, Vec3 to, double step) {
      List out = new ArrayList();
      double dist = from.m_82554_(to);
      int n = (int)Math.ceil(dist / Math.max(0.1, step));
      if (n < 1) {
         n = 1;
      }

      for(int i = 1; i < n; ++i) {
         double t = (double)i / (double)n;
         out.add(new Vec3(from.f_82479_ + (to.f_82479_ - from.f_82479_) * t, from.f_82480_ + (to.f_82480_ - from.f_82480_) * t, from.f_82481_ + (to.f_82481_ - from.f_82481_) * t));
      }

      return out;
   }

   private List densify(List waypoints, double step) {
      List out = new ArrayList();
      if (waypoints.isEmpty()) {
         return out;
      } else {
         out.add((Vec3)waypoints.get(0));

         for(int i = 1; i < waypoints.size(); ++i) {
            out.addAll(this.interpolateSteps((Vec3)waypoints.get(i - 1), (Vec3)waypoints.get(i), step));
            out.add((Vec3)waypoints.get(i));
         }

         return out;
      }
   }

   private void markRender(List path, Vec3 attackPos) {
      if (path != null) {
         this.renderPath = new ArrayList(path);
      }

      this.renderAttackPos = attackPos;
      this.renderExpireMs = System.currentTimeMillis() + 800L;
   }

   @SubscribeEvent
   public void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES) {
         long remaining = this.renderExpireMs - System.currentTimeMillis();
         if (remaining <= 0L) {
            this.renderPath = null;
            this.renderAttackPos = null;
         } else {
            float fade = Math.min(1.0F, (float)remaining / 800.0F);
            int pathColor = applyAlpha(-16711681, fade);
            int outlineColor = applyAlpha(-22016, fade);
            int fillColor = applyAlpha(1073785599, fade);
            PoseStack pose = event.getPoseStack();
            if (this.renderPath != null && this.renderPath.size() >= 2) {
               RenderUtils.drawLines(pose, this.renderPath, pathColor, false);
            }

            if (this.renderAttackPos != null && mc.f_91074_ != null) {
               double halfW = (double)mc.f_91074_.m_20205_() / 2.0;
               double h = (double)mc.f_91074_.m_20206_();
               AABB box = new AABB(this.renderAttackPos.f_82479_ - halfW, this.renderAttackPos.f_82480_, this.renderAttackPos.f_82481_ - halfW, this.renderAttackPos.f_82479_ + halfW, this.renderAttackPos.f_82480_ + h, this.renderAttackPos.f_82481_ + halfW);
               List boxes = Collections.singletonList(box);
               RenderUtils.drawSolidBoxes(pose, boxes, fillColor, false);
               RenderUtils.drawOutlinedBoxes(pose, boxes, outlineColor, false);
            }

         }
      }
   }

   private static int applyAlpha(int argb, float mul) {
      int a = argb >>> 24 & 255;
      int newA = Math.max(0, Math.min(255, (int)((float)a * mul)));
      return newA << 24 | argb & 16777215;
   }

   private void sendPacket(Vec3 pos) {
      if (mc.m_91403_() != null && mc.f_91074_ != null) {
         boolean onGround = this.noFall ? false : this.core != null && this.core.isOnGround(pos);
         ServerboundMovePlayerPacket.Pos packet = new ServerboundMovePlayerPacket.Pos(pos.f_82479_, pos.f_82480_, pos.f_82481_, onGround);
         ((IPlayerMoveC2SPacket)packet).lexis$setTag(1337);
         mc.m_91403_().m_104955_(packet);
         this.lastSentPos = pos;
      }
   }

   private void handleAutoSwitch(Entity target) {
      if (this.autoSwitch) {
         Predicate pred = (s) -> {
            return s.m_204117_(ItemTags.f_271388_) || s.m_41720_() instanceof AxeItem;
         };
         if (this.weapon == TpAuraHack.Weapon.Sword) {
            pred = (s) -> {
               return s.m_204117_(ItemTags.f_271388_);
            };
         } else if (this.weapon == TpAuraHack.Weapon.Axe) {
            pred = (s) -> {
               return s.m_41720_() instanceof AxeItem;
            };
         }

         int slot = this.findSlot(pred);
         if (target instanceof Player) {
            Player p = (Player)target;
            if (p.m_21254_() && this.shieldMode == TpAuraHack.ShieldMode.Break) {
               int axeSlot = this.findSlot((s) -> {
                  return s.m_41720_() instanceof AxeItem;
               });
               if (axeSlot != -1) {
                  slot = axeSlot;
               }
            }
         }

         if (slot != -1) {
            if (!this.swapped) {
               this.previousSlot = mc.f_91074_.m_150109_().f_35977_;
               this.swapped = true;
            }

            mc.f_91074_.m_150109_().f_35977_ = slot;
         }

      }
   }

   private int findSlot(Predicate p) {
      for(int i = 0; i < 9; ++i) {
         if (p.test(mc.f_91074_.m_150109_().m_8020_(i))) {
            return i;
         }
      }

      return -1;
   }

   private Entity findTarget() {
      Entity best = null;
      double bestSq = Double.MAX_VALUE;
      Iterator var4 = mc.f_91073_.m_104735_().iterator();

      while(var4.hasNext()) {
         Entity e = (Entity)var4.next();
         if (e instanceof LivingEntity && e != mc.f_91074_ && ((LivingEntity)e).m_6084_()) {
            double dSq = mc.f_91074_.m_20280_(e);
            if (!(dSq > this.tpRange * this.tpRange) && this.isValidTarget(e) && dSq < bestSq) {
               bestSq = dSq;
               best = e;
            }
         }
      }

      return best;
   }

   private boolean isValidTarget(Entity e) {
      String entityId = BuiltInRegistries.f_256780_.m_7981_(e.m_6095_()).toString();
      if (this.whitelistEntities.contains(entityId)) {
         return false;
      } else if (this.ignoreNamed && e.m_8077_()) {
         return false;
      } else {
         if (this.ignoreTamed && e instanceof TamableAnimal) {
            TamableAnimal ta = (TamableAnimal)e;
            if (ta.m_269323_() == mc.f_91074_) {
               return false;
            }
         }

         if (this.ignorePassive) {
            if (e instanceof Animal) {
               return false;
            }

            if (e instanceof NeutralMob && e instanceof Mob) {
               Mob mob2 = (Mob)e;
               if (!mob2.m_5912_()) {
                  return false;
               }
            }
         }

         if (e instanceof Player) {
            Player p = (Player)e;
            if (p.m_7500_() || p.m_5833_()) {
               return false;
            }

            if (FriendsManager.getInstance().isFriend(p)) {
               return false;
            }
         }

         return true;
      }
   }

   private void syncSettingsBack() {
      boolean dirty = false;
      Iterator var2 = this.getSettings().iterator();

      while(true) {
         label150:
         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            int var7;
            int var8;
            switch (s.getName()) {
               case "简化模式":
                  if (s.getBoolean() != this.simpleMode) {
                     this.simpleMode = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "武器模式":
                  Weapon[] var10 = TpAuraHack.Weapon.values();
                  var7 = var10.length;
                  var8 = 0;

                  while(true) {
                     if (var8 >= var7) {
                        continue label150;
                     }

                     Weapon w = var10[var8];
                     if (w.toString().equals(s.getString()) && this.weapon != w) {
                        this.weapon = w;
                        dirty = true;
                     }

                     ++var8;
                  }
               case "自动切换武器":
                  if (s.getBoolean() != this.autoSwitch) {
                     this.autoSwitch = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "切回原位":
                  if (s.getBoolean() != this.swapBack) {
                     this.swapBack = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "破盾模式":
                  ShieldMode[] var6 = TpAuraHack.ShieldMode.values();
                  var7 = var6.length;
                  var8 = 0;

                  while(true) {
                     if (var8 >= var7) {
                        continue label150;
                     }

                     ShieldMode sm = var6[var8];
                     if (sm.toString().equals(s.getString()) && this.shieldMode != sm) {
                        this.shieldMode = sm;
                        dirty = true;
                     }

                     ++var8;
                  }
               case "绕过被动":
                  if (s.getBoolean() != this.ignorePassive) {
                     this.ignorePassive = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "瞬移范围":
                  if (s.getDouble() != this.tpRange) {
                     this.tpRange = s.getDouble();
                     dirty = true;
                  }
                  break;
               case "攻击距离":
                  if (s.getDouble() != this.attackOffset) {
                     this.attackOffset = s.getDouble();
                     dirty = true;
                  }
                  break;
               case "回传":
                  if (s.getBoolean() != this.returnPos) {
                     this.returnPos = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "防真传":
                  if (s.getBoolean() != this.s08Return) {
                     this.s08Return = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "防摔":
                  if (s.getBoolean() != this.noFall) {
                     this.noFall = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "动态玩家位置":
                  if (s.getBoolean() != this.dynamicTarget) {
                     this.dynamicTarget = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "单次步长":
                  if (s.getDouble() != this.maxStep) {
                     this.maxStep = s.getDouble();
                     dirty = true;
                  }
                  break;
               case "垂直穿墙":
                  if (s.getBoolean() != this.airPath) {
                     this.airPath = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "TPS同步":
                  if (s.getBoolean() != this.tpsSync) {
                     this.tpsSync = s.getBoolean();
                     dirty = true;
                  }
                  break;
               case "攻击倍率":
                  if (s.getDouble() != this.hitDelayMult) {
                     this.hitDelayMult = s.getDouble();
                     dirty = true;
                  }
                  break;
               case "卡顿暂停":
                  if (s.getBoolean() != this.pauseOnLag) {
                     this.pauseOnLag = s.getBoolean();
                     dirty = true;
                  }
            }
         }

         if (dirty) {
            this.saveConfig();
         }

         return;
      }
   }

   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.packet instanceof ClientboundPlayerPositionPacket) {
         if (this.phase != TpAuraHack.TpPhase.IDLE) {
            if (this.originalPos != null && mc.f_91074_ != null) {
               mc.f_91074_.m_6034_(this.originalPos.f_82479_, this.originalPos.f_82480_, this.originalPos.f_82481_);
            }

            this.resetTeleport();
            this.tpCooldown = Math.max(this.tpCooldown, 6);
         }

         if (this.core != null) {
            this.core.desyncPos = null;
            if (this.s08Return && this.originalPos != null && this.sinceLastAttack < 40) {
               this.pendingS08Return = true;
            }
         }
      }

   }

   public void onPacketSend(PacketEvent.Send event) {
      if (this.suppressAutoMove) {
         if (event.packet instanceof ServerboundMovePlayerPacket && ((IPlayerMoveC2SPacket)event.packet).lexis$getTag() != 1337) {
            event.cancel();
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public Set getWhitelist() {
      return this.whitelistEntities;
   }

   public static enum Weapon {
      Sword,
      Axe,
      All;

      // $FF: synthetic method
      private static Weapon[] $values() {
         return new Weapon[]{Sword, Axe, All};
      }
   }

   public static enum ShieldMode {
      Break,
      Ignore,
      None;

      // $FF: synthetic method
      private static ShieldMode[] $values() {
         return new ShieldMode[]{Break, Ignore, None};
      }
   }

   private static enum TpPhase {
      IDLE,
      GOING,
      RETURNING;

      // $FF: synthetic method
      private static TpPhase[] $values() {
         return new TpPhase[]{IDLE, GOING, RETURNING};
      }
   }
}
