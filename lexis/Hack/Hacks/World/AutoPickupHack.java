package lexis.Hack.Hacks.World;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.Colors.SettingColor;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class AutoPickupHack extends Hack implements RenderListener {
   private double maxDistance = 6.0;
   private boolean throughWalls = false;
   private long lastPickupTime = 0L;
   private static final long COOLDOWN_MS = 200L;
   private static final String CONFIG_KEY = "自动吸圈物品";
   private HackConfig config = HackConfig.getInstance();
   private ItemEntity targetItem = null;
   private SettingColor boxColor = new SettingColor(255, 255, 255, 255);
   private SettingColor sidesColor = new SettingColor(255, 170, 0, 68);

   public AutoPickupHack() {
      super("自动吸圈物品", new String[]{"自动捡起地上掉落物"}, Hack.Category.WORLD, true);
      this.addSetting(new Hack.Setting("最大捡取距离", "自动捡取该距离内的物品", 6.0, 1.0, 32.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("允许穿墙", "开启后无视传送点是否有方块阻挡", false));
      this.addSetting(new Hack.Setting("方框颜色", "物品高亮边框颜色", this.boxColor.getPacked()));
      this.addSetting(new Hack.Setting("六面颜色", "物品半透明填充颜色", this.sidesColor.getPacked()));
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxDistance = this.config.getDoubleSetting("自动吸圈物品", "最大捡取距离", 6.0);
      this.throughWalls = this.config.getBooleanSetting("自动吸圈物品", "允许穿墙", false);
      this.boxColor = new SettingColor(this.config.getIntSetting("自动吸圈物品", "方框颜色", this.boxColor.getPacked()));
      this.sidesColor = new SettingColor(this.config.getIntSetting("自动吸圈物品", "六面颜色", this.sidesColor.getPacked()));
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "最大捡取距离":
               s.setValue(this.maxDistance);
               break;
            case "允许穿墙":
               s.setValue(this.throughWalls);
               break;
            case "方框颜色":
               s.setValue(this.boxColor.getPacked());
               break;
            case "六面颜色":
               s.setValue(this.sidesColor.getPacked());
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("自动吸圈物品", this.getSettings());
   }

   public void onEnable() {
      this.loadConfig();
      this.lastPickupTime = 0L;
      EventManager.add(RenderListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(RenderListener.class, this);
      this.targetItem = null;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         boolean needSave = false;
         Iterator var2 = this.getSettings().iterator();

         double distSq;
         while(var2.hasNext()) {
            Hack.Setting s = (Hack.Setting)var2.next();
            switch (s.getName()) {
               case "最大捡取距离":
                  distSq = s.getDouble();
                  if (Math.abs(distSq - this.maxDistance) > 0.01) {
                     this.maxDistance = distSq;
                     needSave = true;
                  }
                  break;
               case "允许穿墙":
                  boolean newWalls = s.getBoolean();
                  if (newWalls != this.throughWalls) {
                     this.throughWalls = newWalls;
                     needSave = true;
                  }
                  break;
               case "方框颜色":
                  int newBox = (Integer)s.getValue();
                  if (newBox != this.boxColor.getPacked()) {
                     this.boxColor = new SettingColor(newBox);
                     needSave = true;
                  }
                  break;
               case "六面颜色":
                  int newSides = (Integer)s.getValue();
                  if (newSides != this.sidesColor.getPacked()) {
                     this.sidesColor = new SettingColor(newSides);
                     needSave = true;
                  }
            }
         }

         if (needSave) {
            this.saveConfig();
         }

         long now = System.currentTimeMillis();
         if (now - this.lastPickupTime >= 200L) {
            Optional closest = mc.f_91073_.m_6443_(ItemEntity.class, mc.f_91074_.m_20191_().m_82400_(this.maxDistance), (e) -> {
               return true;
            }).stream().min(Comparator.comparingDouble((e) -> {
               return mc.f_91074_.m_20280_(e);
            }));
            if (closest.isEmpty()) {
               this.targetItem = null;
            } else {
               ItemEntity target = (ItemEntity)closest.get();
               distSq = mc.f_91074_.m_20280_(target);
               if (distSq > this.maxDistance * this.maxDistance) {
                  this.targetItem = null;
               } else if (!this.throughWalls && !this.hasLineOfSight(target)) {
                  this.targetItem = null;
               } else {
                  this.targetItem = target;
                  this.teleportToItem(target);
                  this.lastPickupTime = now;
               }
            }
         }
      }
   }

   private boolean hasLineOfSight(ItemEntity item) {
      Vec3 eyePos = mc.f_91074_.m_146892_();
      Vec3 itemPos = item.m_20182_();
      HitResult hitResult = mc.f_91073_.m_45547_(new ClipContext(eyePos, itemPos, Block.COLLIDER, Fluid.NONE, mc.f_91074_));
      return hitResult.m_6662_() == Type.MISS || hitResult.m_82450_().m_82557_(itemPos) < 0.1;
   }

   private void teleportToItem(ItemEntity target) {
      if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
         double targetX = target.m_20185_();
         double targetY = target.m_20186_() + 0.25;
         double targetZ = target.m_20189_();
         mc.f_91074_.f_108617_.m_104955_(new ServerboundMovePlayerPacket.PosRot(targetX, targetY, targetZ, mc.f_91074_.m_146908_(), mc.f_91074_.m_146909_(), mc.f_91074_.m_20096_()));
      }
   }

   public void onRender(PoseStack poseStack, float partialTick) {
      if (this.isEnabled() && this.targetItem != null && mc.f_91074_ != null) {
         AABB box = this.targetItem.m_20191_().m_82400_(0.2);
         RenderUtils.drawSolidBoxes(poseStack, List.of(box), this.sidesColor.getPacked(), false);
         RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), this.boxColor.getPacked(), false);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
