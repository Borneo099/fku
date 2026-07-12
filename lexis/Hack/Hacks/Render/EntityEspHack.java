package lexis.Hack.Hacks.Render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.EntityEsp.EntitySelectScreen;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.FakeGlowManager;
import lexis.Hack.Utils.Render.RenderUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityEspHack extends Hack {
   private EspStyle style;
   private final Map entitySettings;
   private final Set trackedEntities;
   private HackConfig config;
   private static final String CONFIG_KEY = "实体透视";
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;

   public void removeEntityByType(String entityId) {
      if (mc.f_91073_ != null) {
         Iterator iterator = this.trackedEntities.iterator();

         while(iterator.hasNext()) {
            Entity entity = (Entity)iterator.next();
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.m_6095_());
            if (key != null && key.toString().equals(entityId)) {
               FakeGlowManager.setGlow(entity, "EntityEsp", false, 0, 0.0);
               iterator.remove();
               break;
            }
         }

      }
   }

   private void updateGlowEntities() {
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         Entity entity;
         while(var1.hasNext()) {
            entity = (Entity)var1.next();
            FakeGlowManager.setGlow(entity, "EntityEsp", false, 0, 0.0);
         }

         if (this.isEnabled()) {
            var1 = this.trackedEntities.iterator();

            while(var1.hasNext()) {
               entity = (Entity)var1.next();
               if (!(entity instanceof Player) && entity instanceof LivingEntity) {
                  ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.m_6095_());
                  if (key != null) {
                     EntitySettings settings = (EntitySettings)this.entitySettings.get(key.toString());
                     if (settings != null && settings.enabled && settings.fakeGlow && !((double)mc.f_91074_.m_20270_(entity) > settings.maxDistance)) {
                        FakeGlowManager.setGlow(entity, "EntityEsp", true, settings.glowColor, settings.maxDistance);
                     }
                  }
               }
            }

         }
      }
   }

   public EntityEspHack() {
      super("实体透视", "透视找实体", Hack.Category.RENDER, true);
      this.style = EntityEspHack.EspStyle.LINES_AND_BOXES;
      this.entitySettings = new ConcurrentHashMap();
      this.trackedEntities = ConcurrentHashMap.newKeySet();
      this.addSetting(new Hack.Setting("显示样式", "ESP显示样式", "连线+方框", new String[]{"仅方框", "仅连线", "连线+方框", "仅六面", "六面+方框", "六面+连线", "全部"}));
      this.addSetting(new Hack.Setting("透视实体", "选择要透视的实体", "选择添加可透视的实体", () -> {
         if (mc != null) {
            mc.m_91152_(new EntitySelectScreen(this, this.entitySettings));
         }

      }));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.loadEntitySettings();
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void loadConfig() {
      String styleStr = this.config.getStringSetting("实体透视", "显示样式", "连线+方框");
      this.style = this.getStyleFromString(styleStr);
   }

   public void loadEntitySettings() {
      try {
         if (!CONFIG_FILE.exists()) {
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         Map saveData = (Map)GSON.fromJson(reader, Map.class);
         reader.close();
         Iterator var4;
         if (saveData.containsKey("显示样式")) {
            String styleStr = (String)saveData.get("显示样式");
            this.style = this.getStyleFromString(styleStr);
            var4 = this.getSettings().iterator();

            while(var4.hasNext()) {
               Hack.Setting setting = (Hack.Setting)var4.next();
               if (setting.getName().equals("显示样式")) {
                  setting.setValue(styleStr);
                  break;
               }
            }
         }

         if (saveData.containsKey("entities")) {
            Map entities = (Map)saveData.get("entities");
            var4 = entities.entrySet().iterator();

            while(var4.hasNext()) {
               Map.Entry entry = (Map.Entry)var4.next();
               EntitySettings es = new EntitySettings();
               es.enabled = (Boolean)((Map)entry.getValue()).get("enabled");
               es.color = ((Double)((Map)entry.getValue()).get("color")).intValue();
               es.maxDistance = ((Map)entry.getValue()).containsKey("maxDistance") ? (Double)((Map)entry.getValue()).get("maxDistance") : 1024.0;
               es.fakeGlow = ((Map)entry.getValue()).containsKey("fakeGlow") ? (Boolean)((Map)entry.getValue()).get("fakeGlow") : false;
               es.glowColor = ((Map)entry.getValue()).containsKey("glowColor") ? ((Double)((Map)entry.getValue()).get("glowColor")).intValue() : 16777215;
               this.entitySettings.put((String)entry.getKey(), es);
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public void saveEntitySettings() {
      try {
         CONFIG_DIR.mkdirs();
         Map saveData = new HashMap();
         saveData.put("显示样式", this.style.toString());
         Map entities = new HashMap();
         Iterator var3 = this.entitySettings.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            Map entityData = new HashMap();
            entityData.put("enabled", ((EntitySettings)entry.getValue()).enabled);
            entityData.put("color", ((EntitySettings)entry.getValue()).color);
            entityData.put("maxDistance", ((EntitySettings)entry.getValue()).maxDistance);
            entityData.put("fakeGlow", ((EntitySettings)entry.getValue()).fakeGlow);
            entityData.put("glowColor", ((EntitySettings)entry.getValue()).glowColor);
            entities.put((String)entry.getKey(), entityData);
         }

         saveData.put("entities", entities);
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(saveData, writer);
         writer.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   private EspStyle getStyleFromString(String name) {
      EspStyle[] var2 = EntityEspHack.EspStyle.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EspStyle s = var2[var4];
         if (s.toString().equals(name)) {
            return s;
         }
      }

      return EntityEspHack.EspStyle.LINES_AND_BOXES;
   }

   public void onEnable() {
      this.trackedEntities.clear();
      if (mc.f_91073_ != null) {
         Iterator var1 = mc.f_91073_.m_104735_().iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            if (entity instanceof LivingEntity) {
               this.trackedEntities.add(entity);
            }
         }
      }

      this.updateGlowEntities();
   }

   public void onDisable() {
      this.trackedEntities.clear();
      FakeGlowManager.clearSource("EntityEsp");
   }

   @SubscribeEvent
   public void onEntityJoin(EntityJoinLevelEvent event) {
      if (event.getEntity() instanceof LivingEntity) {
         this.trackedEntities.add(event.getEntity());
      }

   }

   public void onUpdate() {
      this.trackedEntities.removeIf((e) -> {
         return !e.m_6084_() || e.m_213877_();
      });
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("显示样式")) {
            String newStyle = setting.getString();
            EspStyle newEspStyle = this.getStyleFromString(newStyle);
            if (this.style != newEspStyle) {
               this.style = newEspStyle;
               this.saveEntitySettings();
            }
            break;
         }
      }

      this.trackedEntities.removeIf((e) -> {
         return !e.m_6084_() || e.m_213877_();
      });
      this.updateGlowEntities();
   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && mc.f_91074_ != null && !this.trackedEntities.isEmpty()) {
         List renderEntities = new ArrayList();
         Iterator var4 = this.trackedEntities.iterator();

         while(var4.hasNext()) {
            Entity entity = (Entity)var4.next();
            if (entity instanceof LivingEntity) {
               String entityId = entity.m_6095_().m_204041_().m_205785_().m_135782_().toString();
               EntitySettings settings = (EntitySettings)this.entitySettings.get(entityId);
               if (settings != null && settings.enabled && !((double)mc.f_91074_.m_20270_(entity) > settings.maxDistance)) {
                  renderEntities.add(new RenderEntity(entity, settings));
               }
            }
         }

         List boxes = new ArrayList();
         List centers = new ArrayList();
         new HashMap();
         Iterator var13 = renderEntities.iterator();

         RenderEntity re;
         while(var13.hasNext()) {
            re = (RenderEntity)var13.next();
            AABB box = re.entity.m_20191_().m_82400_(0.1);
            boxes.add(box);
            centers.add(box.m_82399_());
         }

         if (this.style.hasSides()) {
            var13 = renderEntities.iterator();

            while(var13.hasNext()) {
               re = (RenderEntity)var13.next();
               int color = re.settings.color;
               List single = List.of(re.entity.m_20191_().m_82400_(0.1));
               RenderUtils.drawSolidBoxes(poseStack, single, color, false);
            }
         }

         List singleBox;
         if (this.style.hasLines()) {
            for(int i = 0; i < renderEntities.size(); ++i) {
               re = (RenderEntity)renderEntities.get(i);
               singleBox = List.of(re.entity.m_20191_().m_82399_());
               RenderUtils.drawTracers(poseStack, partialTicks, singleBox, re.settings.color, false);
            }
         }

         if (this.style.hasBoxes()) {
            var13 = renderEntities.iterator();

            while(var13.hasNext()) {
               re = (RenderEntity)var13.next();
               singleBox = List.of(re.entity.m_20191_().m_82400_(0.1));
               RenderUtils.drawOutlinedBoxes(poseStack, singleBox, re.settings.color, false);
            }
         }

      }
   }

   public void onClick() {
      this.toggle();
   }

   public void onCustomSettingsButtonClick() {
      mc.m_91152_(new EntitySelectScreen(this, this.entitySettings));
   }

   public Map getEntitySettings() {
      return this.entitySettings;
   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "entityesp.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   public static class EntitySettings {
      public boolean enabled = true;
      public int color = -16711936;
      public double maxDistance = 64.0;
      public boolean fakeGlow = false;
      public int glowColor = 16777215;

      public float[] getColorF() {
         return new float[]{(float)(this.color >> 16 & 255) / 255.0F, (float)(this.color >> 8 & 255) / 255.0F, (float)(this.color & 255) / 255.0F, (float)(this.color >> 24 & 255) / 255.0F};
      }
   }

   public static enum EspStyle {
      BOXES("仅方框", true, false, false),
      LINES("仅连线", false, true, false),
      LINES_AND_BOXES("连线+方框", true, true, false),
      SIDES_ONLY("仅六面", false, false, true),
      SIDES_AND_BOXES("六面+方框", true, false, true),
      SIDES_AND_LINES("六面+连线", false, true, true),
      ALL("全部", true, true, true);

      private final String name;
      private final boolean boxes;
      private final boolean lines;
      private final boolean sides;

      private EspStyle(String name, boolean boxes, boolean lines, boolean sides) {
         this.name = name;
         this.boxes = boxes;
         this.lines = lines;
         this.sides = sides;
      }

      public String toString() {
         return this.name;
      }

      public boolean hasBoxes() {
         return this.boxes;
      }

      public boolean hasLines() {
         return this.lines;
      }

      public boolean hasSides() {
         return this.sides;
      }

      // $FF: synthetic method
      private static EspStyle[] $values() {
         return new EspStyle[]{BOXES, LINES, LINES_AND_BOXES, SIDES_ONLY, SIDES_AND_BOXES, SIDES_AND_LINES, ALL};
      }
   }

   private static class RenderEntity {
      Entity entity;
      EntitySettings settings;

      RenderEntity(Entity entity, EntitySettings settings) {
         this.entity = entity;
         this.settings = settings;
      }
   }
}
