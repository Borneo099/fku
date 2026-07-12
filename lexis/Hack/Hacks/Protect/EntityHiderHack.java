package lexis.Hack.Hacks.Protect;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class EntityHiderHack extends Hack {
   private HackConfig config;
   private static final String CONFIG_KEY = "实体隐藏保护";
   private int maxEntityCount = 200;
   private boolean autoHide = true;
   private boolean isHiding = false;
   private int lastEntityCount = 0;

   public EntityHiderHack() {
      super("实体隐藏保护", "检测实体数量过多自动隐藏实体(不隐藏玩家)", Hack.Category.PROTECT, true);
      this.addSetting(new Hack.Setting("最大实体数", "超过数量自动隐藏实体 (10-1000)", 200, 10, 5000, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("自动隐藏", "检测到过多实体时自动隐藏(关闭了只要让你手动隐藏实体吧)", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxEntityCount = (int)this.config.getDoubleSetting("实体隐藏保护", "最大实体数", 200.0);
      this.autoHide = this.config.getBooleanSetting("实体隐藏保护", "自动隐藏", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "最大实体数":
               setting.setValue((double)this.maxEntityCount);
               break;
            case "自动隐藏":
               setting.setValue(this.autoHide);
         }
      }

   }

   public void onEnable() {
      this.isHiding = false;
   }

   public void onDisable() {
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            entity.m_6842_(false);
         }
      }

      this.isHiding = false;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "最大实体数":
                  this.maxEntityCount = (int)setting.getDouble();
                  break;
               case "自动隐藏":
                  this.autoHide = setting.getBoolean();
            }
         }

         int entityCount = 0;
         Iterator var6 = mc.f_91073_.m_104735_().iterator();

         Entity entity;
         while(var6.hasNext()) {
            entity = (Entity)var6.next();
            if (!(entity instanceof Player)) {
               ++entityCount;
            }
         }

         this.lastEntityCount = entityCount;
         if (this.autoHide) {
            if (entityCount > this.maxEntityCount && !this.isHiding) {
               this.hideAllEntities();
               NotificationManager.warning("实体隐藏保护：", "检测到 " + entityCount + " 个实体，超过数量上限 " + this.maxEntityCount + "，已自动处理隐藏！", 4);
               this.isHiding = true;
            } else if (entityCount <= this.maxEntityCount && this.isHiding) {
               this.showAllEntities();
               NotificationManager.info("实体隐藏保护：", "实体数量已恢复正常 (" + entityCount + ")", 4);
               this.isHiding = false;
            }
         }

         if (this.isHiding) {
            var6 = mc.f_91073_.m_104735_().iterator();

            while(var6.hasNext()) {
               entity = (Entity)var6.next();
               if (!(entity instanceof Player)) {
                  entity.m_6842_(true);
                  entity.m_20011_(new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
               }
            }
         }

      }
   }

   private void hideAllEntities() {
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            if (!(entity instanceof Player)) {
               entity.m_6842_(true);
               entity.m_20011_(new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
            }
         }

      }
   }

   private void showAllEntities() {
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            if (!(entity instanceof Player)) {
               entity.m_6842_(false);
               entity.m_20011_(entity.m_20191_());
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public int getLastEntityCount() {
      return this.lastEntityCount;
   }
}
