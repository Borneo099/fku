package lexis.Hack.Hacks.Combat;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.AnchorAura.AnchorAuraConfig;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.ESP.EntitySelectScreen;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AnchorAuraHack extends Hack {
   private double range = 6.0;
   private boolean autoPlace = true;
   private boolean autoCharge = true;
   private FacingMode facingMode;
   private boolean checkLOS;
   private TakeItemsFrom takeItemsFrom;
   private boolean fastMode;
   private boolean superFastMode;
   private int operationsPerTick;
   private int highlightColor;
   private boolean ignoreNamed;
   private boolean ignorePassive;
   private final Set whitelistEntities;
   private final AnchorAuraConfig anchorConfig;
   private final Map highlightedBlocks;
   private static final long HIGHLIGHT_DURATION_MS = 200L;
   private HackConfig config;
   private static final String CONFIG_KEY = "重生锚光环";

   public AnchorAuraHack() {
      super("重生锚光环", "充能引爆重生锚", Hack.Category.COMBAT, true);
      this.facingMode = AnchorAuraHack.FacingMode.OFF;
      this.checkLOS = false;
      this.takeItemsFrom = AnchorAuraHack.TakeItemsFrom.HOTBAR;
      this.fastMode = true;
      this.superFastMode = false;
      this.operationsPerTick = 10;
      this.highlightColor = -22016;
      this.ignoreNamed = false;
      this.ignorePassive = true;
      this.whitelistEntities = ConcurrentHashMap.newKeySet();
      this.anchorConfig = AnchorAuraConfig.getInstance();
      this.highlightedBlocks = new HashMap();
      this.config = HackConfig.getInstance();
      this.addSetting(new Hack.Setting("范围", "放置和引爆的距离", 6.0, 1.0, 6.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("自动放置", "自动放置重生锚", true));
      this.addSetting(new Hack.Setting("自动充能", "自动用荧石充重生锚", true));
      this.addSetting(new Hack.Setting("看向模式", "是否看向交互的位置(仅其地玩家能看到)", "关闭", new String[]{"关闭", "开启", "数据包模式"}));
      this.addSetting(new Hack.Setting("检查视线", "确保不穿过方块", false));
      this.addSetting(new Hack.Setting("取用物品", "物品来源自动取出", "快捷栏", new String[]{"快捷栏", "背包"}));
      this.addSetting(new Hack.Setting("快速模式", "每tick多个操作", true));
      this.addSetting(new Hack.Setting("超速模式", "取消速度限制", false));
      this.addSetting(new Hack.Setting("每tick操作数", "快速模式下的操作数", 10, 1, 50, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("高亮颜色", "放置锚点时的轮廓颜色", this.highlightColor));
      this.addSetting(new Hack.Setting("绕过命名", "不攻击有名字的实体", false));
      this.addSetting(new Hack.Setting("绕过被动", "不攻击被动实体(动物等)", true));
      this.addSetting(new Hack.Setting("晓过实体", "选择不攻击的实体", "晓过实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(mc.f_91080_, "anchora", this::reloadWhitelistFromConfig));
         }

      }));
      this.loadConfig();
      this.loadWhitelist();
   }

   private void loadConfig() {
      this.range = this.config.getDoubleSetting("重生锚光环", "范围", 6.0);
      this.autoPlace = this.config.getBooleanSetting("重生锚光环", "自动放置", true);
      this.autoCharge = this.config.getBooleanSetting("重生锚光环", "自动充能", true);
      String facingStr = this.config.getStringSetting("重生锚光环", "看向模式", "关闭");
      this.checkLOS = this.config.getBooleanSetting("重生锚光环", "检查视线", false);
      String takeStr = this.config.getStringSetting("重生锚光环", "取用物品", "快捷栏");
      this.fastMode = this.config.getBooleanSetting("重生锚光环", "快速模式", true);
      this.superFastMode = this.config.getBooleanSetting("重生锚光环", "超速模式", false);
      this.operationsPerTick = (int)this.config.getDoubleSetting("重生锚光环", "每tick操作数", 10.0);
      this.highlightColor = this.config.getIntSetting("重生锚光环", "高亮颜色", -22016);
      this.ignoreNamed = this.config.getBooleanSetting("重生锚光环", "绕过命名", false);
      this.ignorePassive = this.config.getBooleanSetting("重生锚光环", "绕过被动", true);
      FacingMode[] var3 = AnchorAuraHack.FacingMode.values();
      int var4 = var3.length;

      int var5;
      for(var5 = 0; var5 < var4; ++var5) {
         FacingMode m = var3[var5];
         if (m.toString().equals(facingStr)) {
            this.facingMode = m;
            break;
         }
      }

      TakeItemsFrom[] var7 = AnchorAuraHack.TakeItemsFrom.values();
      var4 = var7.length;

      for(var5 = 0; var5 < var4; ++var5) {
         TakeItemsFrom t = var7[var5];
         if (t.toString().equals(takeStr)) {
            this.takeItemsFrom = t;
            break;
         }
      }

      Iterator var8 = this.getSettings().iterator();

      while(var8.hasNext()) {
         Hack.Setting s = (Hack.Setting)var8.next();
         switch (s.getName()) {
            case "范围":
               s.setValue(this.range);
               break;
            case "自动放置":
               s.setValue(this.autoPlace);
               break;
            case "自动充能":
               s.setValue(this.autoCharge);
               break;
            case "看向模式":
               s.setValue(facingStr);
               break;
            case "检查视线":
               s.setValue(this.checkLOS);
               break;
            case "取用物品":
               s.setValue(takeStr);
               break;
            case "快速模式":
               s.setValue(this.fastMode);
               break;
            case "超速模式":
               s.setValue(this.superFastMode);
               break;
            case "每tick操作数":
               s.setValue((double)this.operationsPerTick);
               break;
            case "高亮颜色":
               s.setValue(this.highlightColor);
               break;
            case "绕过命名":
               s.setValue(this.ignoreNamed);
               break;
            case "绕过被动":
               s.setValue(this.ignorePassive);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("重生锚光环", this.getSettings());
   }

   private void loadWhitelist() {
      this.reloadWhitelistFromConfig();
   }

   private void reloadWhitelistFromConfig() {
      this.whitelistEntities.clear();
      File f = new File("C:/karucn/Lexis/config/hack/entity_select_anchora.json");
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
      this.anchorConfig.setWhitelist(newWhitelist);
   }

   public Set getWhitelist() {
      return this.whitelistEntities;
   }

   public void onEnable() {
      this.loadConfig();
      this.loadWhitelist();
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      MinecraftForge.EVENT_BUS.unregister(this);
      if (this.facingMode != AnchorAuraHack.FacingMode.OFF && HeadOnlyLook.isLooking()) {
         HeadOnlyLook.stopLooking();
      }

      this.highlightedBlocks.clear();
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            int op;
            switch (s.getName()) {
               case "范围":
                  double r = s.getDouble();
                  if (r != this.range) {
                     this.range = r;
                     needSave = true;
                  }
                  break;
               case "自动放置":
                  boolean ap = s.getBoolean();
                  if (ap != this.autoPlace) {
                     this.autoPlace = ap;
                     needSave = true;
                  }
                  break;
               case "自动充能":
                  boolean ac = s.getBoolean();
                  if (ac != this.autoCharge) {
                     this.autoCharge = ac;
                     needSave = true;
                  }
                  break;
               case "看向模式":
                  String fs = s.getString();
                  if (fs.equals(this.facingMode.toString())) {
                     break;
                  }

                  FacingMode[] var28 = AnchorAuraHack.FacingMode.values();
                  int var30 = var28.length;
                  int var34 = 0;

                  for(; var34 < var30; ++var34) {
                     FacingMode m = var28[var34];
                     if (m.toString().equals(fs)) {
                        this.facingMode = m;
                     }
                  }

                  needSave = true;
                  break;
               case "检查视线":
                  boolean los = s.getBoolean();
                  if (los != this.checkLOS) {
                     this.checkLOS = los;
                     needSave = true;
                  }
                  break;
               case "取用物品":
                  String tk = s.getString();
                  if (tk.equals(this.takeItemsFrom.toString())) {
                     break;
                  }

                  TakeItemsFrom[] var33 = AnchorAuraHack.TakeItemsFrom.values();
                  int var35 = var33.length;

                  for(op = 0; op < var35; ++op) {
                     TakeItemsFrom t = var33[op];
                     if (t.toString().equals(tk)) {
                        this.takeItemsFrom = t;
                     }
                  }

                  needSave = true;
                  break;
               case "快速模式":
                  boolean fm = s.getBoolean();
                  if (fm != this.fastMode) {
                     this.fastMode = fm;
                     needSave = true;
                  }
                  break;
               case "超速模式":
                  boolean sp = s.getBoolean();
                  if (sp != this.superFastMode) {
                     this.superFastMode = sp;
                     needSave = true;
                  }
                  break;
               case "每tick操作数":
                  op = (int)s.getDouble();
                  if (op != this.operationsPerTick) {
                     this.operationsPerTick = op;
                     needSave = true;
                  }
                  break;
               case "高亮颜色":
                  int col = s.getInt();
                  if (col != this.highlightColor) {
                     this.highlightColor = col;
                     needSave = true;
                  }
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
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         long now = System.currentTimeMillis();
         this.highlightedBlocks.entrySet().removeIf((e) -> {
            return now > (Long)e.getValue();
         });
         List targets = this.getNearbyTargets();
         if (!targets.isEmpty()) {
            List anchors = this.getNearbyAnchors();
            List charged = new ArrayList();
            List uncharged = new ArrayList();
            Iterator var22 = anchors.iterator();

            while(var22.hasNext()) {
               BlockPos pos = (BlockPos)var22.next();
               if (this.isAnchorCharged(pos)) {
                  charged.add(pos);
               } else {
                  uncharged.add(pos);
               }
            }

            int maxOps = this.superFastMode ? 999 : (this.fastMode ? this.operationsPerTick : 1);
            int ops = 0;
            Iterator var27;
            BlockPos pos;
            if (!charged.isEmpty() && ops < maxOps) {
               var27 = charged.iterator();

               while(var27.hasNext()) {
                  pos = (BlockPos)var27.next();
                  if (ops >= maxOps) {
                     break;
                  }

                  if (this.interactAnchor(pos, false)) {
                     ++ops;
                  }
               }
            }

            if (this.autoCharge && !uncharged.isEmpty() && this.hasGlowstone() && ops < maxOps) {
               var27 = uncharged.iterator();

               while(var27.hasNext()) {
                  pos = (BlockPos)var27.next();
                  if (ops >= maxOps) {
                     break;
                  }

                  if (this.interactAnchor(pos, true)) {
                     ++ops;
                  }
               }
            }

            if (this.autoPlace && this.hasRespawnAnchor() && ops < maxOps) {
               var27 = targets.iterator();

               while(var27.hasNext()) {
                  Entity target = (Entity)var27.next();
                  if (ops >= maxOps) {
                     break;
                  }

                  Iterator var32 = this.getFreeBlocksNear(target).iterator();

                  while(var32.hasNext()) {
                     BlockPos pos = (BlockPos)var32.next();
                     if (ops >= maxOps) {
                        break;
                     }

                     if (this.placeAnchor(pos)) {
                        ++ops;
                     }
                  }
               }
            }

         }
      }
   }

   private List getNearbyAnchors() {
      List anchors = new ArrayList();
      double rangeSq = this.range * this.range;
      Vec3 eyePos = mc.f_91074_.m_146892_();
      BlockPos center = mc.f_91074_.m_20183_();
      int r = (int)Math.ceil(this.range);

      for(int x = -r; x <= r; ++x) {
         for(int y = -r; y <= r; ++y) {
            for(int z = -r; z <= r; ++z) {
               BlockPos pos = center.m_7918_(x, y, z);
               if (!(eyePos.m_82557_(Vec3.m_82512_(pos)) > rangeSq) && mc.f_91073_.m_8055_(pos).m_60734_() == Blocks.f_50724_) {
                  anchors.add(pos);
               }
            }
         }
      }

      anchors.sort(Comparator.comparingDouble((p) -> {
         return -eyePos.m_82557_(Vec3.m_82512_(p));
      }));
      return anchors;
   }

   private boolean isAnchorCharged(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos);
      if (state.m_60734_() != Blocks.f_50724_) {
         return false;
      } else {
         return (Integer)state.m_61143_(RespawnAnchorBlock.f_55833_) > 0;
      }
   }

   private boolean interactAnchor(BlockPos pos, boolean charge) {
      if (charge) {
         if (!this.selectGlowstoneItem()) {
            return false;
         }
      } else if (!mc.f_91074_.m_21205_().m_41619_()) {
         int emptySlot = this.findEmptySlot();
         if (emptySlot == -1) {
            return false;
         }

         mc.f_91074_.m_150109_().f_35977_ = emptySlot;
      }

      BlockHitResult hit = this.getBlockHitResult(pos);
      if (hit == null) {
         return false;
      } else {
         mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hit);
         mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         if (this.facingMode != AnchorAuraHack.FacingMode.OFF) {
            HeadOnlyLook.startLookingAt(pos);
         }

         return true;
      }
   }

   private BlockHitResult getBlockHitResult(BlockPos pos) {
      Vec3 eyePos = mc.f_91074_.m_146892_();
      Vec3 targetCenter = Vec3.m_82512_(pos);
      Direction[] var4 = Direction.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Direction dir = var4[var6];
         Vec3 hitVec = targetCenter.m_82520_((double)dir.m_122429_() * 0.5, (double)dir.m_122430_() * 0.5, (double)dir.m_122431_() * 0.5);
         if (!(eyePos.m_82557_(hitVec) > this.range * this.range) && (!this.checkLOS || this.hasLineOfSight(eyePos, hitVec))) {
            return new BlockHitResult(hitVec, dir, pos, false);
         }
      }

      return null;
   }

   private boolean placeAnchor(BlockPos pos) {
      if (!this.selectRespawnAnchorItem()) {
         return false;
      } else if (!mc.f_91073_.m_8055_(pos).m_60795_()) {
         return false;
      } else if (!mc.f_91073_.m_8055_(pos.m_7495_()).m_280296_()) {
         return false;
      } else {
         Vec3 hitVec = Vec3.m_82512_(pos);
         if (this.checkLOS && !this.hasLineOfSight(mc.f_91074_.m_146892_(), hitVec)) {
            return false;
         } else {
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
            mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hit);
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
            if (this.facingMode != AnchorAuraHack.FacingMode.OFF) {
               HeadOnlyLook.startLookingAt(pos);
            }

            this.highlightedBlocks.put(pos, System.currentTimeMillis() + 200L);
            return true;
         }
      }
   }

   private List getFreeBlocksNear(Entity target) {
      List candidates = new ArrayList();
      Vec3 eyePos = mc.f_91074_.m_146892_();
      double rangeD = this.range;
      double rangeSq = Math.pow(rangeD + 0.5, 2.0);
      BlockPos center = target.m_20183_();
      AABB targetBB = target.m_20191_();

      for(int x = -2; x <= 2; ++x) {
         for(int z = -2; z <= 2; ++z) {
            BlockPos pos = center.m_7918_(x, 0, z);
            int y = (int)Math.floor(targetBB.f_82289_);
            pos = new BlockPos(pos.m_123341_(), y, pos.m_123343_());
            if (!(eyePos.m_82557_(Vec3.m_82512_(pos)) > rangeSq) && mc.f_91073_.m_8055_(pos).m_60795_() && mc.f_91073_.m_8055_(pos.m_7495_()).m_280296_() && !targetBB.m_82381_(new AABB(pos))) {
               candidates.add(pos);
            }
         }
      }

      Vec3 targetEye = target.m_146892_();
      candidates.sort(Comparator.comparingDouble((p) -> {
         return targetEye.m_82557_(Vec3.m_82512_(p));
      }));
      return candidates;
   }

   private boolean hasGlowstone() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42054_) {
            return true;
         }
      }

      return false;
   }

   private boolean selectGlowstoneItem() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42054_) {
            if (i >= 9) {
               this.swapToHotbar(i);
            } else {
               mc.f_91074_.m_150109_().f_35977_ = i;
            }

            return true;
         }
      }

      return false;
   }

   private boolean hasRespawnAnchor() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42767_) {
            return true;
         }
      }

      return false;
   }

   private boolean selectRespawnAnchorItem() {
      for(int i = 0; i < this.takeItemsFrom.getMaxInvSlot(); ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41720_() == Items.f_42767_) {
            if (i >= 9) {
               this.swapToHotbar(i);
            } else {
               mc.f_91074_.m_150109_().f_35977_ = i;
            }

            return true;
         }
      }

      return false;
   }

   private int findEmptySlot() {
      for(int i = 0; i < 9; ++i) {
         if (mc.f_91074_.m_150109_().m_8020_(i).m_41619_()) {
            return i;
         }
      }

      return -1;
   }

   private void swapToHotbar(int inventorySlot) {
      int hotbarSlot = this.findEmptySlot();
      if (hotbarSlot == -1) {
         hotbarSlot = mc.f_91074_.m_150109_().f_35977_;
      }

      mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, inventorySlot, hotbarSlot, ClickType.SWAP, mc.f_91074_);
      mc.f_91074_.m_150109_().f_35977_ = hotbarSlot;
   }

   private List getNearbyTargets() {
      List targets = new ArrayList();
      double rangeSq = this.range * this.range;
      Iterator var4 = mc.f_91073_.m_104735_().iterator();

      while(true) {
         Entity e;
         while(true) {
            String id;
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!var4.hasNext()) {
                                    targets.sort(Comparator.comparingDouble((ex) -> {
                                       return mc.f_91074_.m_20280_(ex);
                                    }));
                                    return targets;
                                 }

                                 e = (Entity)var4.next();
                              } while(e == mc.f_91074_);
                           } while(!(e instanceof LivingEntity));
                        } while(!((LivingEntity)e).m_6084_());
                     } while(mc.f_91074_.m_20280_(e) > rangeSq);
                  } while(e instanceof Player && FriendsManager.getInstance().isFriend((Player)e));

                  ResourceLocation key = BuiltInRegistries.f_256780_.m_7981_(e.m_6095_());
                  id = key.toString();
               } while(this.whitelistEntities.contains(id));
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

         targets.add(e);
      }
   }

   private boolean hasLineOfSight(Vec3 from, Vec3 to) {
      return mc.f_91073_.m_45547_(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.f_91074_)).m_6662_() == net.minecraft.world.phys.HitResult.Type.MISS;
   }

   @SubscribeEvent
   public void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
         if (!this.highlightedBlocks.isEmpty()) {
            List boxes = new ArrayList();
            Iterator var3 = this.highlightedBlocks.keySet().iterator();

            while(var3.hasNext()) {
               BlockPos pos = (BlockPos)var3.next();
               boxes.add(new AABB(pos));
            }

            PoseStack pose = event.getPoseStack();
            RenderUtils.drawOutlinedBoxes(pose, boxes, this.highlightColor, false);
         }
      }
   }

   public void onClick() {
      this.toggle();
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
