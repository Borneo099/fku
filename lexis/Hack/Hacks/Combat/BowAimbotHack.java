package lexis.Hack.Hacks.Combat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FriendsManager;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BowAimbotHack extends Hack {
   private Priority priority;
   private double predictMovement;
   private int espColor;
   private Entity target;
   private float velocity;
   private HackConfig config;
   private static final String CONFIG_KEY = "自瞄";

   public BowAimbotHack() {
      super("自瞄", new String[]{"弓箭自动瞄准", "可支持 弓+弩"}, Hack.Category.COMBAT, true);
      this.priority = BowAimbotHack.Priority.ANGLE_DIST;
      this.predictMovement = 0.2;
      this.espColor = -65536;
      this.target = null;
      this.velocity = 0.0F;
      this.addSetting(new Hack.Setting("优先级", "目标选择方式", "角度+距离", new String[]{"距离", "角度", "角度+距离", "血量"}));
      this.addSetting(new Hack.Setting("预测移动", "移动预测强度", 0.2, 0.0, 2.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("ESP颜色", "目标方框颜色", -65536));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      String priorityStr = this.config.getStringSetting("自瞄", "优先级", "角度+距离");
      this.predictMovement = this.config.getDoubleSetting("自瞄", "预测移动", 0.2);
      this.espColor = this.config.getIntSetting("自瞄", "ESP颜色", -65536);
      Priority[] var2 = BowAimbotHack.Priority.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Priority p = var2[var4];
         if (p.toString().equals(priorityStr)) {
            this.priority = p;
            break;
         }
      }

      Iterator var6 = this.getSettings().iterator();

      while(var6.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var6.next();
         switch (setting.getName()) {
            case "优先级":
               setting.setValue(priorityStr);
               break;
            case "预测移动":
               setting.setValue(this.predictMovement);
               break;
            case "ESP颜色":
               setting.setValue(this.espColor);
         }
      }

   }

   private static double getAngleToEntity(Entity entity) {
      Vec3 eyesPos = mc.f_91074_.m_146892_();
      Vec3 targetPos = entity.m_20191_().m_82399_();
      Vec3 toTarget = targetPos.m_82546_(eyesPos).m_82541_();
      Vec3 lookVec = mc.f_91074_.m_20154_();
      return Math.toDegrees(Math.acos(lookVec.m_82526_(toTarget)));
   }

   public void onEnable() {
      this.target = null;
   }

   public void onDisable() {
      HeadOnlyLook.stopRotation();
      this.target = null;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         label89:
         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "优先级":
                  String priorityStr = setting.getString();
                  Priority[] var6 = BowAimbotHack.Priority.values();
                  int var7 = var6.length;
                  int var8 = 0;

                  while(true) {
                     if (var8 >= var7) {
                        continue label89;
                     }

                     Priority p = var6[var8];
                     if (p.toString().equals(priorityStr)) {
                        this.priority = p;
                        continue label89;
                     }

                     ++var8;
                  }
               case "预测移动":
                  this.predictMovement = setting.getDouble();
                  break;
               case "ESP颜色":
                  this.espColor = (Integer)setting.getValue();
            }
         }

         LocalPlayer player = mc.f_91074_;
         ItemStack stack = player.m_21205_();
         if (!(stack.m_41720_() instanceof BowItem) && !(stack.m_41720_() instanceof CrossbowItem)) {
            this.target = null;
         } else if (stack.m_41720_() instanceof BowItem && !mc.f_91066_.f_92095_.m_90857_() && !player.m_6117_()) {
            this.target = null;
         } else if (stack.m_41720_() instanceof CrossbowItem && !CrossbowItem.m_40932_(stack)) {
            this.target = null;
         } else {
            if (this.target == null || !this.target.m_6084_() || mc.f_91074_.m_20270_(this.target) > 64.0F) {
               this.target = this.findBestTarget();
            }

            if (this.target != null) {
               int useTime = player.m_21252_();
               this.velocity = (float)(72000 - useTime) / 20.0F;
               this.velocity = (this.velocity * this.velocity + this.velocity * 2.0F) / 3.0F;
               if (this.velocity > 1.0F) {
                  this.velocity = 1.0F;
               }

               double ticks = (double)mc.f_91074_.m_20270_(this.target) * this.predictMovement / 10.0;
               double posX = this.target.m_20185_() + (this.target.m_20185_() - this.target.f_19854_) * ticks - player.m_20185_();
               double posY = this.target.m_20186_() + (this.target.m_20186_() - this.target.f_19855_) * ticks + (double)this.target.m_20206_() * 0.5 - player.m_20186_() - (double)player.m_20192_();
               double posZ = this.target.m_20189_() + (this.target.m_20189_() - this.target.f_19856_) * ticks - player.m_20189_();
               double hDistance = Math.sqrt(posX * posX + posZ * posZ);
               float yaw = (float)Math.toDegrees(Math.atan2(posZ, posX)) - 90.0F;
               float pitch = this.calculatePitch(posX, posY, posZ, hDistance);
               yaw = this.normalizeAngle(yaw);
               pitch = Mth.m_14036_(pitch, -90.0F, 90.0F);
               mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.m_20096_()));
               if (!HeadOnlyLook.isRotating()) {
                  HeadOnlyLook.startRotation(yaw, pitch);
               } else {
                  HeadOnlyLook.updateRotation(yaw, pitch);
               }
            } else {
               HeadOnlyLook.stopRotation();
            }

         }
      }
   }

   private float calculatePitch(double posX, double posY, double posZ, double hDistance) {
      if (hDistance < 0.1) {
         return 0.0F;
      } else {
         float g = 0.006F;
         float velocitySq = this.velocity * this.velocity;
         float velocityPow4 = velocitySq * velocitySq;
         double sqrt = Math.sqrt((double)velocityPow4 - (double)g * ((double)g * hDistance * hDistance + 2.0 * posY * (double)velocitySq));
         float pitch = (float)(-Math.toDegrees(Math.atan(((double)velocitySq - sqrt) / ((double)g * hDistance))));
         if (Float.isNaN(pitch)) {
            Vec3 targetCenter = this.target.m_20191_().m_82399_();
            Vec3 dir = targetCenter.m_82546_(mc.f_91074_.m_146892_()).m_82541_();
            pitch = (float)(-Math.toDegrees(Math.atan2(dir.f_82480_, Math.sqrt(dir.f_82479_ * dir.f_82479_ + dir.f_82481_ * dir.f_82481_))));
         }

         return pitch;
      }
   }

   private Entity findBestTarget() {
      Stream stream = StreamSupport.stream(mc.f_91073_.m_104735_().spliterator(), true).filter((e) -> {
         return e instanceof LivingEntity;
      }).filter((e) -> {
         return e != mc.f_91074_;
      }).filter((e) -> {
         return e.m_6084_();
      }).filter((e) -> {
         return mc.f_91074_.m_20270_(e) <= 64.0F;
      }).filter((e) -> {
         if (e instanceof Player) {
            return !FriendsManager.getInstance().isFriend((Player)e);
         } else {
            return true;
         }
      });
      return (Entity)stream.min(this.priority.getComparator()).orElse((Object)null);
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

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && this.target != null && mc.f_91074_ != null) {
         AABB box = this.target.m_20191_().m_82400_(0.1);
         RenderUtils.drawSolidBoxes(poseStack, List.of(box), this.espColor, false);
         RenderUtils.drawOutlinedBoxes(poseStack, List.of(box), this.espColor, false);
      }
   }

   public Entity getTarget() {
      return this.target;
   }

   public float getVelocity() {
      return this.velocity;
   }

   public int getEspColor() {
      return this.espColor;
   }

   public void onClick() {
      this.toggle();
   }

   public static enum Priority {
      DISTANCE("距离", (e) -> {
         return Hack.mc.f_91074_.m_20280_(e);
      }),
      ANGLE("角度", (e) -> {
         return BowAimbotHack.getAngleToEntity(e);
      }),
      ANGLE_DIST("角度+距离", (e) -> {
         return Math.pow(BowAimbotHack.getAngleToEntity(e), 2.0) + Hack.mc.f_91074_.m_20280_(e) / 10.0;
      }),
      HEALTH("血量", (e) -> {
         return e instanceof LivingEntity ? (double)((LivingEntity)e).m_21223_() : 3.4028234663852886E38;
      });

      private final String name;
      private final Comparator comparator;

      private Priority(String name, ToDoubleFunction keyExtractor) {
         this.name = name;
         this.comparator = Comparator.comparingDouble(keyExtractor);
      }

      public String toString() {
         return this.name;
      }

      public Comparator getComparator() {
         return this.comparator;
      }

      // $FF: synthetic method
      private static Priority[] $values() {
         return new Priority[]{DISTANCE, ANGLE, ANGLE_DIST, HEALTH};
      }
   }
}
