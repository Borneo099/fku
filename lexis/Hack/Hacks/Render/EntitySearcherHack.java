package lexis.Hack.Hacks.Render;

import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.ConfigUtils;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.gui.screens.GenericEntitySelectScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;

public class EntitySearcherHack extends Hack {
   private static final String CONFIG_KEY = "搜索实体";
   private static final String GLOW_SOURCE = "EntitySearcher";
   private Set targetEntities = new HashSet();
   private int maxDistance = 64;
   private float rainbowSpeed = 2.0F;
   private HackConfig config = HackConfig.getInstance();
   private Map entityHueMap = new ConcurrentHashMap();

   public EntitySearcherHack() {
      super("搜索实体", "如果太看不到/找不到实体 可以这给你找到搜索实体 带有彩虹发光", Hack.Category.RENDER, true);
      this.addSetting(new Hack.Setting("最大距离", "发光实体最大显示距离", 64, 1, 256, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("彩虹速度", "颜色变化速度", 2.0, 0.2, 10.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("编辑实体列表", "选择要发光的实体类型", "编辑", this::openEntitySelector));
      this.loadConfig();
   }

   private void loadConfig() {
      this.maxDistance = this.config.getIntSetting("搜索实体", "最大距离", 64);
      this.rainbowSpeed = (float)this.config.getDoubleSetting("搜索实体", "彩虹速度", 2.0);
      File configFile = new File("C:/karucn/Lexis/config/hack/EntitySearcher_whitelist.json");
      Set loaded = (Set)ConfigUtils.readConfig(configFile, (new TypeToken() {
      }).getType());
      if (loaded != null) {
         this.targetEntities = loaded;
      } else {
         this.targetEntities = new HashSet();
      }

      Iterator var3 = this.getSettings().iterator();

      while(var3.hasNext()) {
         Hack.Setting s = (Hack.Setting)var3.next();
         switch (s.getName()) {
            case "最大距离":
               s.setValue((double)this.maxDistance);
               break;
            case "彩虹速度":
               s.setValue((double)this.rainbowSpeed);
         }
      }

   }

   private void saveConfig() {
      this.config.saveHackSettings("搜索实体", this.getSettings());
      File configFile = new File("C:/karucn/Lexis/config/hack/EntitySearcher_whitelist.json");
      ConfigUtils.saveConfig(configFile, this.targetEntities);
   }

   private void openEntitySelector() {
      if (mc != null) {
         mc.m_91152_(new GenericEntitySelectScreen(mc.f_91080_, "EntitySearcher_whitelist", this.targetEntities, (newSelected) -> {
            this.targetEntities.clear();
            this.targetEntities.addAll(newSelected);
            this.saveConfig();
         }));
      }
   }

   public void onEnable() {
      this.entityHueMap.clear();
   }

   public void onDisable() {
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         while(var1.hasNext()) {
            Entity e = (Entity)var1.next();
            FakeGlowManager.setGlow(e, "EntitySearcher", false, 0, 0.0);
         }
      }

      this.entityHueMap.clear();
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "最大距离":
               int newDist = s.getInt();
               if (newDist != this.maxDistance) {
                  this.maxDistance = newDist;
                  needSave = true;
               }
               break;
            case "彩虹速度":
               float newSpeed = (float)s.getDouble();
               if (Math.abs(newSpeed - this.rainbowSpeed) > 0.001F) {
                  this.rainbowSpeed = newSpeed;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.saveConfig();
      }

      if (this.isEnabled() && mc.f_91073_ != null && mc.f_91074_ != null) {
         Set currentTargets = new HashSet();
         Iterator var9 = mc.f_91073_.m_104735_().iterator();

         Entity e;
         while(var9.hasNext()) {
            e = (Entity)var9.next();
            if (e != mc.f_91074_) {
               String entityId = BuiltInRegistries.f_256780_.m_7981_(e.m_6095_()).toString();
               if (this.targetEntities.contains(entityId) && mc.f_91074_.m_20270_(e) <= (float)this.maxDistance) {
                  currentTargets.add(e);
               }
            }
         }

         var9 = currentTargets.iterator();

         while(var9.hasNext()) {
            e = (Entity)var9.next();
            UUID uuid = e.m_20148_();
            float hue = (Float)this.entityHueMap.getOrDefault(uuid, (float)Math.random());
            hue += this.rainbowSpeed / 20.0F;
            if (hue > 1.0F) {
               --hue;
            }

            this.entityHueMap.put(uuid, hue);
            int rainbowColor = Color.HSBtoRGB(hue, 1.0F, 1.0F) & 16777215;
            FakeGlowManager.setGlow(e, "EntitySearcher", true, rainbowColor, (double)this.maxDistance);
         }

         Set currentUuids = new HashSet();
         Iterator var12 = currentTargets.iterator();

         Entity e;
         while(var12.hasNext()) {
            e = (Entity)var12.next();
            currentUuids.add(e.m_20148_());
         }

         this.entityHueMap.keySet().removeIf((uuidx) -> {
            return !currentUuids.contains(uuidx);
         });
         var12 = mc.f_91073_.m_104735_().iterator();

         while(var12.hasNext()) {
            e = (Entity)var12.next();
            if (!currentTargets.contains(e)) {
               FakeGlowManager.setGlow(e, "EntitySearcher", false, 0, 0.0);
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }
}
