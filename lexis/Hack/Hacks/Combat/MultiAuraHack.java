package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MultiAuraHack extends Hack implements RenderListener {
   private static final String CONFIG_KEY = "多重光环";
   private HackConfig config;
   private double range = 6.0;
   private int maxTargetsPerTick = 3;
   private boolean throughWalls = false;
   private boolean delayBetweenTargets = true;
   private PriorityMode priority;
   private boolean ignoreNamed;
   private boolean ignorePassive;
   private boolean renderSides;
   private boolean renderBox;
   private SettingColor sidesColor;
   private SettingColor boxColor;
   private Set blacklistEntities;
   private final Map entityLastAttack;

   public MultiAuraHack() {
      super("多重光环", "同时攻击多个目标", Hack.Category.COMBAT, true);
      this.priority = MultiAuraHack.PriorityMode.DISTANCE;
      this.ignoreNamed = false;
      this.ignorePassive = true;
      this.renderSides = true;
      this.renderBox = true;
      this.sidesColor = new SettingColor(255, 100, 0, 80);
      this.boxColor = new SettingColor(255, 255, 255, 255);
      this.blacklistEntities = new HashSet();
      this.entityLastAttack = new ConcurrentHashMap();
      this.config = HackConfig.getInstance();
      this.addSetting(new Hack.Setting("攻击范围", "选择目标的距离", 6.0, 1.0, 12.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("每tick目标数", "每最多攻击的目标数量", 3, 1, 32, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("穿墙", "无视方块阻挡", false));
      this.addSetting(new Hack.Setting("目标间延迟", "攻击不同目标间加入延迟(50ms)防止封包", false));
      this.addSetting(new Hack.Setting("优先级", "选择攻击目标", "距离最近", new String[]{"距离最近", "血量最低", "角度最小"}));
      this.addSetting(new Hack.Setting("绕过命名", "不攻击有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不攻击被动实体(动物等)", true));
      this.addSetting(new Hack.Setting("显示六面", "渲染填充", true));
      this.addSetting(new Hack.Setting("显示方框", "渲染白色边线框", true));
      this.addSetting(new Hack.Setting("六面颜色", "填充颜色", this.sidesColor.getPacked()));
      this.addSetting(new Hack.Setting("方框颜色", "边线框颜色", this.boxColor.getPacked()));
      this.addSetting(new Hack.Setting("编辑实体列表", "选择不攻击的实体类型(黑名单)", "编辑", this::openEntitySelector));
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("多重光环", "攻击范围", 6.0);
      this.maxTargetsPerTick = (int)this.config.getDoubleSetting("多重光环", "每tick目标数", 3.0);
      this.throughWalls = this.config.getBooleanSetting("多重光环", "穿墙", false);
      this.delayBetweenTargets = this.config.getBooleanSetting("多重光环", "目标间延迟", true);
      String prioStr = this.config.getStringSetting("多重光环", "优先级", "距离最近");
      PriorityMode[] var2 = MultiAuraHack.PriorityMode.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         PriorityMode mode = var2[var4];
         if (mode.toString().equals(prioStr)) {
            this.priority = mode;
            break;
         }
      }

      this.ignoreNamed = this.config.getBooleanSetting("多重光环", "绕过命名", false);
      this.ignorePassive = this.config.getBooleanSetting("多重光环", "绕过被动", true);
      this.renderSides = this.config.getBooleanSetting("多重光环", "显示六面", true);
      this.renderBox = this.config.getBooleanSetting("多重光环", "显示方框", true);
      this.sidesColor = new SettingColor(this.config.getIntSetting("多重光环", "六面颜色", this.sidesColor.getPacked()));
      this.boxColor = new SettingColor(this.config.getIntSetting("多重光环", "方框颜色", this.boxColor.getPacked()));
      this.syncSettings();
   }

   private void syncSettings() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "攻击范围":
               s.setValue(this.range);
               break;
            case "每tick目标数":
               s.setValue((double)this.maxTargetsPerTick);
               break;
            case "穿墙":
               s.setValue(this.throughWalls);
               break;
            case "目标间延迟":
               s.setValue(this.delayBetweenTargets);
               break;
            case "优先级":
               s.setValue(this.priority.toString());
               break;
            case "绕过命名":
               s.setValue(this.ignoreNamed);
               break;
            case "绕过被动":
               s.setValue(this.ignorePassive);
               break;
            case "显示六面":
               s.setValue(this.renderSides);
               break;
            case "显示方框":
               s.setValue(this.renderBox);
               break;
            case "六面颜色":
               s.setValue(this.sidesColor.getPacked());
               break;
            case "方框颜色":
               s.setValue(this.boxColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("多重光环", this.getSettings());
   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_multiaura.json");
      Type type = (new TypeToken() {
      }).getType();
      List loaded = (List)ConfigUtils.readConfig(f, type);
      this.blacklistEntities.clear();
      if (loaded != null) {
         this.blacklistEntities.addAll(loaded);
      }

   }

   private void openEntitySelector() {
      if (mc != null) {
         mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "multiaura", this::reloadWhitelistFromConfig));
      }
   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
      this.entityLastAttack.clear();
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      this.entityLastAttack.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      int attacked;
      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "攻击范围":
               double r = s.getDouble();
               if (r != this.range) {
                  this.range = r;
                  needSave = true;
               }
               break;
            case "每tick目标数":
               int t = (int)s.getDouble();
               if (t != this.maxTargetsPerTick) {
                  this.maxTargetsPerTick = t;
                  needSave = true;
               }
               break;
            case "穿墙":
               boolean w = s.getBoolean();
               if (w != this.throughWalls) {
                  this.throughWalls = w;
                  needSave = true;
               }
               break;
            case "目标间延迟":
               boolean del = s.getBoolean();
               if (del != this.delayBetweenTargets) {
                  this.delayBetweenTargets = del;
                  needSave = true;
               }
               break;
            case "优先级":
               String pri = s.getString();
               if (pri.equals(this.priority.toString())) {
                  break;
               }

               PriorityMode[] var22 = MultiAuraHack.PriorityMode.values();
               int var23 = var22.length;

               for(int var24 = 0; var24 < var23; ++var24) {
                  PriorityMode m = var22[var24];
                  if (m.toString().equals(pri)) {
                     this.priority = m;
                     break;
                  }
               }

               needSave = true;
               break;
            case "绕过命名":
               boolean nn = s.getBoolean();
               if (nn != this.ignoreNamed) {
                  this.ignoreNamed = nn;
                  needSave = true;
               }
               break;
            case "绕过被动":
               boolean np = s.getBoolean();
               if (np != this.ignorePassive) {
                  this.ignorePassive = np;
                  needSave = true;
               }
               break;
            case "显示六面":
               boolean sd = s.getBoolean();
               if (sd != this.renderSides) {
                  this.renderSides = sd;
                  needSave = true;
               }
               break;
            case "显示方框":
               boolean bx = s.getBoolean();
               if (bx != this.renderBox) {
                  this.renderBox = bx;
                  needSave = true;
               }
               break;
            case "六面颜色":
               int sc = (Integer)s.getValue();
               if (sc != this.sidesColor.getPacked()) {
                  this.sidesColor = new SettingColor(sc);
                  needSave = true;
               }
               break;
            case "方框颜色":
               int bc = (Integer)s.getValue();
               if (bc != this.boxColor.getPacked()) {
                  this.boxColor = new SettingColor(bc);
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (!(mc.f_91074_.m_36403_(0.5F) < 1.0F)) {
            List targets = this.getAttackableTargets();
            if (!targets.isEmpty()) {
               long now = System.currentTimeMillis();
               attacked = 0;
               Iterator var20 = targets.iterator();

               while(var20.hasNext()) {
                  Entity target = (Entity)var20.next();
                  if (attacked >= this.maxTargetsPerTick) {
                     break;
                  }

                  if (this.delayBetweenTargets) {
                     Long lastAttack = (Long)this.entityLastAttack.get(target.m_20148_());
                     if (lastAttack != null && now - lastAttack < 50L) {
                        continue;
                     }
                  }

                  this.attackEntity(target);
                  this.entityLastAttack.put(target.m_20148_(), now);
                  ++attacked;
               }

            }
         }
      }
   }

   private void attackEntity(Entity target) {
      if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
         ServerboundInteractPacket attackPacket = ServerboundInteractPacket.m_179605_(target, mc.f_91074_.m_6144_());
         mc.f_91074_.f_108617_.m_104955_(attackPacket);
         mc.f_91074_.f_108617_.m_104955_(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
         mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         mc.f_91074_.m_36334_();
      }
   }

   private List getAttackableTargets() {
      List list = new ArrayList();
      double sqRange = this.range * this.range;
      Vec3 eyePos = mc.f_91074_.m_146892_();
      Iterator var5 = mc.f_91073_.m_104735_().iterator();

      while(true) {
         Entity e;
         while(true) {
            do {
               do {
                  String id;
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var5.hasNext()) {
                                    if (this.priority == MultiAuraHack.PriorityMode.DISTANCE) {
                                       list.sort(Comparator.comparingDouble((ex) -> {
                                          return mc.f_91074_.m_20280_(ex);
                                       }));
                                    } else if (this.priority == MultiAuraHack.PriorityMode.HEALTH) {
                                       list.sort(Comparator.comparingDouble((ex) -> {
                                          return (double)((LivingEntity)ex).m_21223_();
                                       }));
                                    } else if (this.priority == MultiAuraHack.PriorityMode.ANGLE) {
                                       list.sort(Comparator.comparingDouble(this::getAngleToEntity));
                                    }

                                    return list;
                                 }

                                 e = (Entity)var5.next();
                              } while(!(e instanceof LivingEntity));
                           } while(e == mc.f_91074_);
                        } while(e instanceof Player && FriendsManager.getInstance().isFriend((Player)e));

                        id = BuiltInRegistries.f_256780_.m_7981_(e.m_6095_()).toString();
                     } while(this.blacklistEntities.contains(id));
                  } while(mc.f_91074_.m_20280_(e) > sqRange);
               } while(!this.throughWalls && !this.hasLineOfSight(e, eyePos));
            } while(this.ignoreNamed && e.m_8077_());

            if (!this.ignorePassive) {
               break;
            }

            if (!(e instanceof Animal)) {
               if (!(e instanceof NeutralMob) || !(e instanceof Mob)) {
                  break;
               }

               Mob mob2 = (Mob)e;
               if (mob2.m_5912_()) {
                  break;
               }
            }
         }

         list.add(e);
      }
   }

   private double getAngleToEntity(Entity entity) {
      Vec3 eyes = mc.f_91074_.m_146892_();
      Vec3 targetPos = entity.m_20191_().m_82399_();
      Vec3 toTarget = targetPos.m_82546_(eyes).m_82541_();
      Vec3 look = mc.f_91074_.m_20154_();
      return Math.toDegrees(Math.acos(look.m_82526_(toTarget)));
   }

   private boolean hasLineOfSight(Entity target, Vec3 eyePos) {
      if (target == null) {
         return true;
      } else {
         Vec3 targetPos = target.m_146892_();
         HitResult hit = mc.f_91073_.m_45547_(new ClipContext(eyePos, targetPos, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
         return hit.m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS || hit.m_82450_().m_82557_(targetPos) < 0.1;
      }
   }

   public void onRender(PoseStack poseStack, float partialTick) {
      if (this.isEnabled() && mc.f_91074_ != null) {
         List boxes = new ArrayList();
         Iterator var4 = this.getAttackableTargets().iterator();

         while(var4.hasNext()) {
            Entity e = (Entity)var4.next();
            boxes.add(e.m_20191_().m_82400_(0.1));
         }

         if (!boxes.isEmpty()) {
            if (this.renderSides) {
               RenderUtils.drawSolidBoxes(poseStack, boxes, this.sidesColor.getPacked(), false);
            }

            if (this.renderBox) {
               var4 = boxes.iterator();

               while(var4.hasNext()) {
                  AABB box = (AABB)var4.next();
                  RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), this.boxColor.getPacked(), false);
               }
            }

         }
      }
   }

   public void onClick() {
      this.toggle();
   }

   public static enum PriorityMode {
      DISTANCE("距离最近"),
      HEALTH("血量最低"),
      ANGLE("角度最小");

      private final String name;

      private PriorityMode(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      // $FF: synthetic method
      private static PriorityMode[] $values() {
         return new PriorityMode[]{DISTANCE, HEALTH, ANGLE};
      }
   }
}
