package lexis.Hack.Hacks.World;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.BlockUtils;
import lexis.Hack.Utils.Color;
import lexis.Hack.Utils.InventoryUtils;
import lexis.Hack.Utils.Timer;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class AutoTorchHack extends Hack implements RenderListener {
   private static final String CONFIG_KEY = "自动火把";
   private final Timer placeTimer = new Timer();
   private List candidates = new ArrayList();
   private int delay = 50;
   private int renderRange = 10;
   private int range = 5;
   private boolean onlyRender = true;
   private boolean throughWall = false;
   private int checkLightLevel = 7;
   private SettingColor renderColor = new SettingColor(255, 0, 0, 255);
   private boolean lookAtTarget = true;

   public AutoTorchHack() {
      super("自动火把", new String[]{"在亮度不足时自动放置火把", "这可以使用来是防止刷怪物？"}, Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("延迟(ms)", "放置间隔(毫秒)", 10.0, 0.0, 4000.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("渲染范围", "显示预览的范围(格)", 10.0, 0.0, 16.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("放置范围", "自动放置的范围(格)", 5.0, 0.0, 6.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("仅渲染", "只显示预览，不自动放置", true));
      this.addSetting(new Hack.Setting("穿墙", "忽略墙壁阻挡（仅影响预览和放置）", false));
      this.addSetting(new Hack.Setting("亮度阈值", "低于此亮度才放置", 7.0, 0.0, 15.0, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("渲染颜色", "预览火把位置的颜色", this.renderColor.getPacked()));
      this.addSetting(new Hack.Setting("旋转", "放置时是否旋转方块", true));
      this.loadConfig();
   }

   private void loadConfig() {
      HackConfig config = HackConfig.getInstance();
      this.delay = config.getIntSetting("自动火把", "延迟(ms)", 50);
      this.renderRange = config.getIntSetting("自动火把", "渲染范围", 10);
      this.range = config.getIntSetting("自动火把", "放置范围", 5);
      this.onlyRender = config.getBooleanSetting("自动火把", "仅渲染", true);
      this.throughWall = config.getBooleanSetting("自动火把", "穿墙", false);
      this.checkLightLevel = config.getIntSetting("自动火把", "亮度阈值", 7);
      this.lookAtTarget = config.getBooleanSetting("自动火把", "旋转", true);
      int colorPacked = config.getIntSetting("自动火把", "渲染颜色", this.renderColor.getPacked());
      this.renderColor = new SettingColor(colorPacked);
      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting s = (Hack.Setting)var3.next();
         switch (s.getName()) {
            case "延迟(ms)":
               s.setValue((double)this.delay);
               break;
            case "渲染范围":
               s.setValue((double)this.renderRange);
               break;
            case "放置范围":
               s.setValue((double)this.range);
               break;
            case "仅渲染":
               s.setValue(this.onlyRender);
               break;
            case "穿墙":
               s.setValue(this.throughWall);
               break;
            case "亮度阈值":
               s.setValue((double)this.checkLightLevel);
               break;
            case "渲染颜色":
               s.setValue(this.renderColor.getPacked());
               break;
            case "旋转":
               s.setValue(this.lookAtTarget);
         }
      }

   }

   public void onEnable() {
      EventManager.add(RenderListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      this.candidates.clear();
      if (this.lookAtTarget) {
         HeadOnlyLook.stopLooking();
      }

   }

   public void onUpdate() {
      if (mc.f_91074_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         int placed;
         int playerY;
         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "延迟(ms)":
                  int newDelay = (int)s.getDouble();
                  if (newDelay != this.delay) {
                     this.delay = newDelay;
                     needSave = true;
                  }
                  break;
               case "渲染范围":
                  int newRenderRange = (int)s.getDouble();
                  if (newRenderRange != this.renderRange) {
                     this.renderRange = newRenderRange;
                     needSave = true;
                  }
                  break;
               case "放置范围":
                  playerY = (int)s.getDouble();
                  if (playerY != this.range) {
                     this.range = playerY;
                     needSave = true;
                  }
                  break;
               case "仅渲染":
                  boolean newOnlyRender = s.getBoolean();
                  if (newOnlyRender != this.onlyRender) {
                     this.onlyRender = newOnlyRender;
                     needSave = true;
                  }
                  break;
               case "穿墙":
                  boolean newThroughWall = s.getBoolean();
                  if (newThroughWall != this.throughWall) {
                     this.throughWall = newThroughWall;
                     needSave = true;
                  }
                  break;
               case "亮度阈值":
                  int newLight = (int)s.getDouble();
                  if (newLight != this.checkLightLevel) {
                     this.checkLightLevel = newLight;
                     needSave = true;
                  }
                  break;
               case "渲染颜色":
                  int newColorPacked = (Integer)s.getValue();
                  if (newColorPacked != this.renderColor.getPacked()) {
                     this.renderColor = new SettingColor(newColorPacked);
                     needSave = true;
                  }
                  break;
               case "旋转":
                  boolean newLook = s.getBoolean();
                  if (newLook != this.lookAtTarget) {
                     this.lookAtTarget = newLook;
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.autoSave();
         }

         if (!this.onlyRender) {
            if (this.placeTimer.passedMs((long)this.delay)) {
               int torchSlot = this.getTorchSlot();
               if (torchSlot != -1) {
                  int oldSlot = mc.f_91074_.m_150109_().f_35977_;
                  List positions = BlockUtils.getSphere(mc.f_91074_.m_146892_(), (double)this.range);
                  placed = 0;
                  Iterator var17 = positions.iterator();

                  while(var17.hasNext()) {
                     BlockPos pos = (BlockPos)var17.next();
                     if (placed >= 1) {
                        break;
                     }

                     playerY = mc.f_91074_.m_146904_();
                     if (pos.m_123342_() >= playerY - 1 && pos.m_123342_() <= playerY + 2 && (!this.throughWall || !this.isBehindWall(pos)) && mc.f_91073_.m_45517_(LightLayer.BLOCK, pos) <= this.checkLightLevel) {
                        Direction side = this.getPlaceSideForTorch(pos);
                        if (side != null && this.canPlaceTorch(pos, side)) {
                           InventoryUtils.switchToSlot(torchSlot);
                           this.placeTorch(pos, side);
                           ++placed;
                           InventoryUtils.switchToSlot(oldSlot);
                        }
                     }
                  }

                  this.placeTimer.reset();
               }
            }
         }
      }
   }

   public void onRender(PoseStack poseStack, float partialTick) {
      if (mc.f_91074_ != null) {
         this.candidates.clear();
         int playerY = mc.f_91074_.m_146904_();
         Iterator var4 = BlockUtils.getSphere(mc.f_91074_.m_146892_(), (double)this.renderRange).iterator();

         while(true) {
            BlockPos pos;
            do {
               do {
                  do {
                     if (!var4.hasNext()) {
                        int col = this.renderColor.getPacked();
                        Iterator var14 = this.candidates.iterator();

                        while(var14.hasNext()) {
                           BlockPos pos = (BlockPos)var14.next();
                           double x = (double)pos.m_123341_();
                           double y = (double)pos.m_123342_();
                           double z = (double)pos.m_123343_();
                           RenderUtils.drawLine(poseStack, x, y, z, x + 1.0, y, z + 1.0, new Color(col));
                           RenderUtils.drawLine(poseStack, x + 1.0, y, z, x, y, z + 1.0, new Color(col));
                        }

                        return;
                     }

                     pos = (BlockPos)var4.next();
                  } while(pos.m_123342_() < playerY - 1);
               } while(pos.m_123342_() > playerY + 2);
            } while(this.throughWall && this.isBehindWall(pos));

            if (mc.f_91073_.m_45517_(LightLayer.BLOCK, pos) <= this.checkLightLevel && this.getPlaceSideForTorch(pos) != null) {
               this.candidates.add(pos);
            }
         }
      }
   }

   private int getTorchSlot() {
      return InventoryUtils.indexOf((stack) -> {
         return stack.m_41720_() == Items.f_42000_;
      });
   }

   private boolean canPlaceTorch(BlockPos pos, Direction side) {
      if (!mc.f_91073_.m_8055_(pos).m_60795_() && !mc.f_91073_.m_8055_(pos).m_247087_()) {
         return false;
      } else if (this.hasEntity(pos, false)) {
         return false;
      } else {
         BlockPos attachPos;
         if (side == Direction.UP) {
            attachPos = pos.m_7495_();
         } else {
            attachPos = pos.m_121945_(side);
         }

         return mc.f_91073_.m_8055_(attachPos).m_280296_() && BlockUtils.getClickSide(attachPos) != null;
      }
   }

   private Direction getPlaceSideForTorch(BlockPos pos) {
      BlockPos down = pos.m_7495_();
      if (mc.f_91073_.m_8055_(down).m_280296_() && BlockUtils.getClickSide(down) != null) {
         return Direction.UP;
      } else {
         Direction best = null;
         double bestDist = Double.MAX_VALUE;
         Vec3 eye = mc.f_91074_.m_146892_();
         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction dir = var7[var9];
            if (dir != Direction.UP && dir != Direction.DOWN) {
               BlockPos neighbor = pos.m_121945_(dir);
               if (mc.f_91073_.m_8055_(neighbor).m_280296_() && BlockUtils.getClickSide(neighbor) != null) {
                  double dist = eye.m_82557_(Vec3.m_82512_(neighbor));
                  if (dist < bestDist) {
                     bestDist = dist;
                     best = dir;
                  }
               }
            }
         }

         return best;
      }
   }

   private boolean hasEntity(BlockPos pos, boolean ignoreCrystals) {
      AABB box = new AABB(pos);
      List entities = mc.f_91073_.m_6443_(Entity.class, box, (ex) -> {
         return ex.m_6084_();
      });
      Iterator var5 = entities.iterator();

      Entity e;
      do {
         do {
            do {
               do {
                  if (!var5.hasNext()) {
                     return false;
                  }

                  e = (Entity)var5.next();
               } while(e instanceof ItemEntity);
            } while(e instanceof ExperienceOrb);
         } while(e instanceof Arrow);
      } while(ignoreCrystals && e instanceof EndCrystal);

      return true;
   }

   private boolean isBehindWall(BlockPos pos) {
      Vec3 eye = mc.f_91074_.m_146892_();
      Vec3 target = Vec3.m_82512_(pos);
      BlockHitResult hit = mc.f_91073_.m_45547_(new ClipContext(eye, target, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
      return hit.m_6662_() != Type.MISS && !hit.m_82450_().m_82509_(target, 0.1);
   }

   private void placeTorch(BlockPos pos, Direction side) {
      BlockPos targetBlock;
      Direction clickSide;
      if (side == Direction.UP) {
         targetBlock = pos.m_7495_();
         clickSide = Direction.UP;
      } else {
         targetBlock = pos.m_121945_(side);
         clickSide = side.m_122424_();
      }

      if (this.lookAtTarget) {
         HeadOnlyLook.startLookingAt(targetBlock, 200L);
      }

      Vec3 hitVec = Vec3.m_82512_(targetBlock).m_82520_((double)clickSide.m_122429_() * 0.5, (double)clickSide.m_122430_() * 0.5, (double)clickSide.m_122431_() * 0.5);
      mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, clickSide, targetBlock, false));
      mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
   }

   public void onClick() {
      this.toggle();
   }
}
