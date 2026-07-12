package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.CrystalAura.CrystalAuraConfig;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalAuraHack extends Hack {
   private static boolean noExplosionParticlesEnabled = false;
   private float attackRange = 6.0F;
   private boolean autoPlace = true;
   private FacingMode facingMode;
   private boolean checkLOS;
   private TakeItemsFrom takeItemsFrom;
   private boolean fastMode;
   private boolean superFastMode;
   private int operationsPerTick;
   private boolean autoPlaceObsidian;
   private boolean noExplosionParticles;
   private boolean ignoreNamed;
   private boolean ignorePassive;
   private final Set whitelistEntities;
   private HackConfig config;
   private CrystalAuraConfig crystalAuraConfig;
   private static final String CONFIG_KEY = "自动点爆水晶";

   public CrystalAuraHack() {
      super("自动点爆水晶", "自动放置和引爆末地水晶", Hack.Category.COMBAT, true);
      this.facingMode = CrystalAuraHack.FacingMode.OFF;
      this.checkLOS = false;
      this.takeItemsFrom = CrystalAuraHack.TakeItemsFrom.HOTBAR;
      this.fastMode = true;
      this.superFastMode = false;
      this.operationsPerTick = 10;
      this.autoPlaceObsidian = false;
      this.noExplosionParticles = false;
      this.ignoreNamed = false;
      this.ignorePassive = true;
      this.whitelistEntities = ConcurrentHashMap.newKeySet();
      this.addSetting(new Hack.Setting("攻击距离", "放置和引爆水晶的距离", 6.0, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("自动放置", "自动在目标附近放置水晶", true));
      this.addSetting(new Hack.Setting("面向模式", "是否面向水晶放置/攻击", "关闭", new String[]{"关闭", "开启", "数据包模式"}));
      this.addSetting(new Hack.Setting("检查视线", "确保不穿过方块", false));
      this.addSetting(new Hack.Setting("取用物品", "从哪里取用水晶", "快捷栏", new String[]{"快捷栏", "背包"}));
      this.addSetting(new Hack.Setting("快速模式", "每 tick 操作快", true));
      this.addSetting(new Hack.Setting("超速模式", "无视速度限制 (可能卡服)", false));
      this.addSetting(new Hack.Setting("每tick操作数", "快速模式下的操作数量", 10.0, 1.0, 50.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("自动放置黑曜石", "自动在目标周围放置黑曜石", false));
      this.addSetting(new Hack.Setting("无爆炸粒子", "屏蔽水晶爆炸粒子效果", false));
      this.addSetting(new Hack.Setting("绕过命名", "不攻击有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不攻击被动实体(动物等)", true));
      this.addSetting(new Hack.Setting("晓过实体", "不点爆的实体", "晓过实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "crystalaura", this::reloadWhitelistFromConfig));
         }

      }));
      this.config = HackConfig.getInstance();
      this.crystalAuraConfig = CrystalAuraConfig.getInstance();
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.attackRange = (float)this.config.getDoubleSetting("自动点爆水晶", "攻击距离", 6.0);
      this.autoPlace = this.config.getBooleanSetting("自动点爆水晶", "自动放置", true);
      String facingStr = this.config.getStringSetting("自动点爆水晶", "面向模式", "关闭");
      this.checkLOS = this.config.getBooleanSetting("自动点爆水晶", "检查视线", false);
      String takeStr = this.config.getStringSetting("自动点爆水晶", "取用物品", "快捷栏");
      this.fastMode = this.config.getBooleanSetting("自动点爆水晶", "快速模式", true);
      this.superFastMode = this.config.getBooleanSetting("自动点爆水晶", "超速模式", false);
      this.operationsPerTick = (int)this.config.getDoubleSetting("自动点爆水晶", "每 tick 操作数", 10.0);
      this.autoPlaceObsidian = this.config.getBooleanSetting("自动点爆水晶", "自动放置黑曜石", false);
      this.noExplosionParticles = this.config.getBooleanSetting("自动点爆水晶", "无爆炸粒子", false);
      this.ignoreNamed = this.config.getBooleanSetting("自动点爆水晶", "绕过命名", false);
      this.ignorePassive = this.config.getBooleanSetting("自动点爆水晶", "绕过被动", true);
      FacingMode[] var3 = CrystalAuraHack.FacingMode.values();
      int var4 = var3.length;

      int var5;
      for(var5 = 0; var5 < var4; ++var5) {
         FacingMode mode = var3[var5];
         if (mode.toString().equals(facingStr)) {
            this.facingMode = mode;
            break;
         }
      }

      TakeItemsFrom[] var7 = CrystalAuraHack.TakeItemsFrom.values();
      var4 = var7.length;

      for(var5 = 0; var5 < var4; ++var5) {
         TakeItemsFrom take = var7[var5];
         if (take.toString().equals(takeStr)) {
            this.takeItemsFrom = take;
            break;
         }
      }

      Iterator var8 = this.getSettings().iterator();

      while(var8.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var8.next();
         switch (setting.getName()) {
            case "攻击距离":
               setting.setValue((double)this.attackRange);
               break;
            case "自动放置":
               setting.setValue(this.autoPlace);
               break;
            case "面向模式":
               setting.setValue(facingStr);
               break;
            case "检查视线":
               setting.setValue(this.checkLOS);
               break;
            case "取用物品":
               setting.setValue(takeStr);
               break;
            case "快速模式":
               setting.setValue(this.fastMode);
               break;
            case "超速模式":
               setting.setValue(this.superFastMode);
               break;
            case "每 tick 操作数":
               setting.setValue((double)this.operationsPerTick);
               break;
            case "自动放置黑曜石":
               setting.setValue(this.autoPlaceObsidian);
               break;
            case "无爆炸粒子":
               setting.setValue(this.noExplosionParticles);
               break;
            case "绕过命名":
               setting.setValue(this.ignoreNamed);
               break;
            case "绕过被动":
               setting.setValue(this.ignorePassive);
         }
      }

      noExplosionParticlesEnabled = this.noExplosionParticles;
   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      this.whitelistEntities.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_crystalaura.json");
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
      this.crystalAuraConfig.setWhitelist(newWhitelist);
   }

   public void onEnable() {
      noExplosionParticlesEnabled = this.noExplosionParticles;
   }

   public void onDisable() {
      noExplosionParticlesEnabled = false;
      if (this.facingMode != CrystalAuraHack.FacingMode.OFF && HeadOnlyLook.isLooking()) {
         HeadOnlyLook.stopLooking();
      }

   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         label205:
         while(var2.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var2.next();
            int newOps;
            switch (setting.getName()) {
               case "攻击距离":
                  float newRange = (float)setting.getDouble();
                  if (newRange != this.attackRange) {
                     this.attackRange = newRange;
                     needSave = true;
                  }
                  break;
               case "自动放置":
                  boolean newPlace = setting.getBoolean();
                  if (newPlace != this.autoPlace) {
                     this.autoPlace = newPlace;
                     needSave = true;
                  }
                  break;
               case "面向模式":
                  String newFacing = setting.getString();
                  FacingMode[] var26 = CrystalAuraHack.FacingMode.values();
                  int var29 = var26.length;
                  int var32 = 0;

                  while(true) {
                     if (var32 >= var29) {
                        continue label205;
                     }

                     FacingMode mode = var26[var32];
                     if (mode.toString().equals(newFacing) && this.facingMode != mode) {
                        this.facingMode = mode;
                        needSave = true;
                        continue label205;
                     }

                     ++var32;
                  }
               case "检查视线":
                  boolean newLos = setting.getBoolean();
                  if (newLos != this.checkLOS) {
                     this.checkLOS = newLos;
                     needSave = true;
                  }
                  break;
               case "取用物品":
                  String newTake = setting.getString();
                  TakeItemsFrom[] var31 = CrystalAuraHack.TakeItemsFrom.values();
                  int var33 = var31.length;
                  newOps = 0;

                  while(true) {
                     if (newOps >= var33) {
                        continue label205;
                     }

                     TakeItemsFrom take = var31[newOps];
                     if (take.toString().equals(newTake) && this.takeItemsFrom != take) {
                        this.takeItemsFrom = take;
                        needSave = true;
                        continue label205;
                     }

                     ++newOps;
                  }
               case "快速模式":
                  boolean newFast = setting.getBoolean();
                  if (newFast != this.fastMode) {
                     this.fastMode = newFast;
                     needSave = true;
                  }
                  break;
               case "超速模式":
                  boolean newSuper = setting.getBoolean();
                  if (newSuper != this.superFastMode) {
                     this.superFastMode = newSuper;
                     needSave = true;
                  }
                  break;
               case "每 tick 操作数":
                  newOps = (int)setting.getDouble();
                  if (newOps != this.operationsPerTick) {
                     this.operationsPerTick = newOps;
                     needSave = true;
                  }
                  break;
               case "自动放置黑曜石":
                  boolean newObs = setting.getBoolean();
                  if (newObs != this.autoPlaceObsidian) {
                     this.autoPlaceObsidian = newObs;
                     needSave = true;
                  }
                  break;
               case "无爆炸粒子":
                  boolean newNoPart = setting.getBoolean();
                  if (newNoPart != this.noExplosionParticles) {
                     this.noExplosionParticles = newNoPart;
                     noExplosionParticlesEnabled = this.noExplosionParticles;
                     needSave = true;
                  }
                  break;
               case "绕过命名":
                  boolean newNamed = setting.getBoolean();
                  if (newNamed != this.ignoreNamed) {
                     this.ignoreNamed = newNamed;
                     needSave = true;
                  }
                  break;
               case "绕过被动":
                  boolean newPassive = setting.getBoolean();
                  if (newPassive != this.ignorePassive) {
                     this.ignorePassive = newPassive;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.config.saveHackSettings("自动点爆水晶", this.getSettings());
         }

         List crystals = this.getNearbyCrystals();
         List targets = this.getNearbyTargets();
         boolean hasTargets = !targets.isEmpty();
         boolean hasCrystalItems = this.hasCrystals();
         if (this.autoPlaceObsidian && hasTargets) {
            Iterator var21 = targets.iterator();

            while(var21.hasNext()) {
               Entity target = (Entity)var21.next();
               if (this.tryPlaceObsidianNear(target)) {
                  break;
               }
            }
         }

         int operations = 0;
         int maxOps = this.superFastMode ? 999 : (this.fastMode ? this.operationsPerTick : 1);
         if (this.facingMode != CrystalAuraHack.FacingMode.OFF && !crystals.isEmpty()) {
            this.lookAtCrystal((Entity)crystals.get(0));
         }

         Iterator var25;
         Entity target;
         for(var25 = crystals.iterator(); var25.hasNext(); ++operations) {
            target = (Entity)var25.next();
            if (operations >= maxOps) {
               break;
            }

            mc.f_91072_.m_105223_(mc.f_91074_, target);
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         }

         if (this.autoPlace && hasCrystalItems && hasTargets && operations < maxOps) {
            var25 = targets.iterator();

            while(var25.hasNext()) {
               target = (Entity)var25.next();
               if (operations >= maxOps) {
                  break;
               }

               Iterator var30 = this.getFreeBlocksNear(target).iterator();

               while(var30.hasNext()) {
                  BlockPos pos = (BlockPos)var30.next();
                  if (operations >= maxOps) {
                     break;
                  }

                  if (this.placeCrystal(pos)) {
                     ++operations;
                  }
               }
            }
         }

      }
   }

   private boolean tryPlaceObsidianNear(Entity target) {
      BlockPos targetPos = target.m_20183_();
      List candidates = new ArrayList();

      for(int dx = -1; dx <= 1; ++dx) {
         for(int dz = -1; dz <= 1; ++dz) {
            if (dx != 0 || dz != 0) {
               BlockPos pos = targetPos.m_7918_(dx, 0, dz);
               if (mc.f_91073_.m_8055_(pos).m_60795_() && !mc.f_91073_.m_8055_(pos.m_7495_()).m_60795_()) {
                  candidates.add(pos);
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return false;
      } else {
         candidates.sort(Comparator.comparingDouble((p) -> {
            return mc.f_91074_.m_20238_(Vec3.m_82512_(p));
         }));
         BlockPos placePos = (BlockPos)candidates.get(0);
         if (!this.selectObsidianItem()) {
            return false;
         } else {
            Vec3 hitVec = Vec3.m_82512_(placePos);
            mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, Direction.UP, placePos, false));
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
            return true;
         }
      }
   }

   private boolean selectObsidianItem() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_41999_) {
            if (i >= 9) {
               int targetSlot = mc.f_91074_.m_150109_().f_35977_;
               mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, i, targetSlot, ClickType.SWAP, mc.f_91074_);
               mc.f_91074_.m_150109_().f_35977_ = targetSlot;
            } else {
               mc.f_91074_.m_150109_().f_35977_ = i;
            }

            return true;
         }
      }

      return false;
   }

   private void lookAtCrystal(Entity crystal) {
      HeadOnlyLook.startLookingAt(crystal.m_20183_());
   }

   private List getNearbyCrystals() {
      List crystals = new ArrayList();
      double rangeSq = (double)(this.attackRange * this.attackRange);
      Iterator var4 = mc.f_91073_.m_104735_().iterator();

      while(var4.hasNext()) {
         Entity entity = (Entity)var4.next();
         if (entity instanceof EndCrystal && entity.m_6084_() && mc.f_91074_.m_20280_(entity) <= rangeSq) {
            crystals.add(entity);
         }
      }

      crystals.sort(Comparator.comparingDouble((a) -> {
         return mc.f_91074_.m_20280_(a);
      }));
      return crystals;
   }

   private List getNearbyTargets() {
      List targets = new ArrayList();
      double rangeSq = (double)(this.attackRange * this.attackRange);
      Iterator var4 = mc.f_91073_.m_104735_().iterator();

      while(true) {
         Entity entity;
         while(true) {
            String entityId;
            do {
               do {
                  do {
                     LivingEntity living;
                     do {
                        do {
                           do {
                              do {
                                 if (!var4.hasNext()) {
                                    targets.sort(Comparator.comparingDouble((a) -> {
                                       return mc.f_91074_.m_20280_(a);
                                    }));
                                    return targets;
                                 }

                                 entity = (Entity)var4.next();
                              } while(!(entity instanceof LivingEntity));

                              living = (LivingEntity)entity;
                           } while(entity == mc.f_91074_);
                        } while(!living.m_6084_());
                     } while(mc.f_91074_.m_20280_(entity) > rangeSq);
                  } while(entity instanceof Player && FriendsManager.getInstance().isFriend((Player)entity));

                  entityId = entity.m_6095_().m_204041_().m_205785_().m_135782_().toString();
               } while(this.whitelistEntities.contains(entityId));
            } while(this.ignoreNamed && entity.m_8077_());

            if (!this.ignorePassive) {
               break;
            }

            if (!(entity instanceof Animal)) {
               if (!(entity instanceof NeutralMob) || !(entity instanceof Mob)) {
                  break;
               }

               Mob mob2 = (Mob)entity;
               if (mob2.m_5912_()) {
                  break;
               }
            }
         }

         targets.add(entity);
      }
   }

   private boolean hasCrystals() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42729_) {
            return true;
         }
      }

      return false;
   }

   private boolean placeCrystal(BlockPos pos) {
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      double rangeSq = (double)(this.attackRange * this.attackRange);
      Vec3 posVec = Vec3.m_82512_(pos);
      Direction[] var6 = Direction.values();
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Direction side = var6[var8];
         BlockPos neighbor = pos.m_121945_(side);
         if (!mc.f_91073_.m_8055_(neighbor).m_60795_()) {
            Vec3 dirVec = new Vec3((double)side.m_122429_(), (double)side.m_122430_(), (double)side.m_122431_());
            Vec3 hitVec = posVec.m_82549_(dirVec.m_82490_(0.5));
            if (eyesPos.m_82557_(hitVec) <= rangeSq && (!this.checkLOS || this.hasLineOfSight(eyesPos, hitVec)) && this.selectCrystalItem()) {
               mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side.m_122424_(), neighbor, false));
               mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
               return true;
            }
         }
      }

      return false;
   }

   private boolean selectCrystalItem() {
      int i;
      if (this.takeItemsFrom == CrystalAuraHack.TakeItemsFrom.INVENTORY) {
         for(i = 0; i < 36; ++i) {
            if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42729_) {
               if (i >= 9) {
                  int targetSlot = -1;

                  for(int j = 0; j < 9; ++j) {
                     if (mc.f_91074_.m_150109_().m_8020_(j).m_41619_()) {
                        targetSlot = j;
                        break;
                     }
                  }

                  if (targetSlot == -1) {
                     targetSlot = mc.f_91074_.m_150109_().f_35977_;
                  }

                  mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, i, targetSlot, ClickType.SWAP, mc.f_91074_);
                  mc.f_91074_.m_150109_().f_35977_ = targetSlot;
               } else {
                  mc.f_91074_.m_150109_().f_35977_ = i;
               }

               return true;
            }
         }
      } else {
         for(i = 0; i < 9; ++i) {
            if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42729_) {
               mc.f_91074_.m_150109_().f_35977_ = i;
               return true;
            }
         }
      }

      return false;
   }

   private List getFreeBlocksNear(Entity target) {
      List blocks = new ArrayList();
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      double rangeD = (double)this.attackRange;
      double rangeSq = Math.pow(rangeD + 0.5, 2.0);
      BlockPos center = target.m_20183_();
      AABB targetBB = target.m_20191_();

      for(int x = -2; x <= 2; ++x) {
         for(int y = -2; y <= 2; ++y) {
            for(int z = -2; z <= 2; ++z) {
               BlockPos pos = center.m_7918_(x, y, z);
               if (!(eyesPos.m_82557_(Vec3.m_82512_(pos)) > rangeSq) && mc.f_91073_.m_8055_(pos).m_60795_() && this.hasCrystalBase(pos) && !targetBB.m_82381_(new AABB(pos))) {
                  blocks.add(pos.m_7949_());
               }
            }
         }
      }

      Vec3 targetEyes = target.m_146892_();
      blocks.sort(Comparator.comparingDouble((p) -> {
         return targetEyes.m_82557_(Vec3.m_82512_(p));
      }));
      return blocks;
   }

   private boolean hasCrystalBase(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos.m_7495_());
      return state.m_60734_() == Blocks.f_50080_ || state.m_60734_() == Blocks.f_50752_;
   }

   private boolean hasLineOfSight(Vec3 from, Vec3 to) {
      return mc.f_91073_.m_45547_(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.f_91074_)).m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS;
   }

   private void saveConfig() {
      this.config.saveHackSettings("自动点爆水晶", this.getSettings());
   }

   public void onClick() {
      this.toggle();
   }

   public Set getWhitelist() {
      return this.whitelistEntities;
   }

   public static boolean isNoExplosionParticlesEnabled() {
      return noExplosionParticlesEnabled;
   }

   public static enum FacingMode {
      OFF("关闭"),
      ON("开启"),
      PACKET_SPAM("数据包模式");

      private final String displayName;

      private FacingMode(String name) {
         this.displayName = name;
      }

      public String toString() {
         return this.displayName;
      }

      // $FF: synthetic method
      private static FacingMode[] $values() {
         return new FacingMode[]{OFF, ON, PACKET_SPAM};
      }
   }

   public static enum TakeItemsFrom {
      HOTBAR("快捷栏", 9),
      INVENTORY("背包", 36);

      private final String displayName;
      private final int maxInvSlot;

      private TakeItemsFrom(String name, int slot) {
         this.displayName = name;
         this.maxInvSlot = slot;
      }

      public String toString() {
         return this.displayName;
      }

      public int getMaxInvSlot() {
         return this.maxInvSlot;
      }

      // $FF: synthetic method
      private static TakeItemsFrom[] $values() {
         return new TakeItemsFrom[]{HOTBAR, INVENTORY};
      }
   }
}
