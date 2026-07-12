package lexis.Hack.Hacks.Render;

import java.util.Iterator;
import java.util.UUID;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityOwnerHack extends Hack {
   private static boolean active = false;
   private double maxDistance = 64.0;
   private boolean showTypeName = true;
   private static final String CONFIG_KEY = "实体所有者";
   private final HackConfig config;

   public static boolean isActive() {
      return active;
   }

   public EntityOwnerHack() {
      super("实体所有者", new String[]{"显示驯服实体头上 主人名称", "显示是 <宠物名> - <主人名>", "猫 和 狗 和 鹦鹉 和 马等 都能看到主人"}, Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("最大距离", "显示范围", 64, 1, 1024, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("显示类型名", "无自定义名时显示实体类型(如: 狼)", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxDistance = (double)this.config.getIntSetting("实体所有者", "最大距离", (int)this.maxDistance);
      this.showTypeName = this.config.getBooleanSetting("实体所有者", "显示类型名", this.showTypeName);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "最大距离":
               s.setValue((int)this.maxDistance);
               break;
            case "显示类型名":
               s.setValue(this.showTypeName);
         }
      }

   }

   public void onEnable() {
      active = true;
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      active = false;
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "最大距离":
               double v = s.getDouble();
               if (v != this.maxDistance) {
                  this.maxDistance = v;
                  needSave = true;
               }
               break;
            case "显示类型名":
               boolean v = s.getBoolean();
               if (v != this.showTypeName) {
                  this.showTypeName = v;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("实体所有者", this.getSettings());
      }

   }

   @SubscribeEvent
   public void onRenderGui(RenderGuiEvent.Post event) {
      if (this.isEnabled()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            float pt = event.getPartialTick();
            GuiGraphics gfx = event.getGuiGraphics();
            Camera cam = mc.f_91063_.m_109153_();
            Vec3 camPos = cam.m_90583_();
            int sw = mc.m_91268_().m_85445_();
            int sh = mc.m_91268_().m_85446_();
            float camYaw = cam.m_90590_();
            float camPitch = cam.m_90589_();
            float cosYaw = Mth.m_14089_(-camYaw * 0.017453292F - 3.1415927F);
            float sinYaw = Mth.m_14031_(-camYaw * 0.017453292F - 3.1415927F);
            float cosPitch = -Mth.m_14089_(-camPitch * 0.017453292F);
            float sinPitch = Mth.m_14031_(-camPitch * 0.017453292F);
            float fx = sinYaw * cosPitch;
            float fy = sinPitch;
            float fz = cosYaw * cosPitch;
            float rx = cosYaw;
            float ry = 0.0F;
            float rz = -sinYaw;
            float ux = ry * fz - rz * sinPitch;
            float uy = rz * fx - cosYaw * fz;
            float uz = cosYaw * sinPitch - ry * fx;
            double fovTan = Math.tan(Math.toRadians((double)(Integer)mc.f_91066_.m_231837_().m_231551_() / 2.0));
            double fovScale = (double)sh / 2.0 / fovTan;
            Font font = mc.f_91062_;
            Iterator var29 = mc.f_91073_.m_104735_().iterator();

            while(var29.hasNext()) {
               Entity entity = (Entity)var29.next();
               if (entity != mc.f_91074_ && !((double)entity.m_20270_(mc.f_91074_) > this.maxDistance)) {
                  UUID ownerUUID = this.getOwnerUUID(entity);
                  if (ownerUUID != null) {
                     String ownerName = this.resolveOwnerName(ownerUUID);
                     if (ownerName != null) {
                        Vec3 pos = entity.m_20318_(pt).m_82520_(0.0, (double)entity.m_20206_() + 0.5, 0.0);
                        Vec3 rel = pos.m_82546_(camPos);
                        float rlx = (float)rel.f_82479_;
                        float rly = (float)rel.f_82480_;
                        float rlz = (float)rel.f_82481_;
                        double dotFwd = (double)(rlx * fx + rly * fy + rlz * fz);
                        if (!(dotFwd < 0.2)) {
                           double dotRight = (double)(rlx * rx + rly * ry + rlz * rz);
                           double dotUpf = (double)(rlx * ux + rly * uy + rlz * uz);
                           double scale = fovScale / dotFwd;
                           float sx = (float)((double)sw / 2.0 + dotRight * scale);
                           float sy = (float)((double)sh / 2.0 - dotUpf * scale);
                           String entityName = this.getEntityName(entity);
                           String text = entityName + " - " + ownerName;
                           int w = font.m_92895_(text);
                           gfx.m_280056_(font, text, (int)(sx - (float)w / 2.0F) + 1, (int)sy + 1, Integer.MIN_VALUE, false);
                           gfx.m_280056_(font, text, (int)(sx - (float)w / 2.0F), (int)sy, -22016, false);
                        }
                     }
                  }
               }
            }

         }
      }
   }

   private UUID getOwnerUUID(Entity entity) {
      if (entity instanceof TamableAnimal tamable) {
         return tamable.m_21805_();
      } else if (entity instanceof AbstractHorse horse) {
         return horse.m_21805_();
      } else if (entity instanceof OwnableEntity ownable) {
         return ownable.m_21805_();
      } else {
         return null;
      }
   }

   private String resolveOwnerName(UUID uuid) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91073_ == null) {
         return null;
      } else {
         Player owner = mc.f_91073_.m_46003_(uuid);
         if (owner != null) {
            return owner.m_36316_().getName();
         } else {
            if (mc.m_91403_() != null) {
               PlayerInfo info = mc.m_91403_().m_104949_(uuid);
               if (info != null) {
                  return info.m_105312_().getName();
               }
            }

            return null;
         }
      }
   }

   private String getEntityName(Entity entity) {
      if (entity.m_8077_()) {
         Component name = entity.m_7770_();
         if (name != null) {
            return name.getString();
         }
      }

      return !this.showTypeName ? null : entity.m_6095_().m_20676_().getString();
   }

   public void onClick() {
      this.toggle();
   }
}
