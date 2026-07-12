package lexis.Hack.Hacks.Render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class CapeHack extends Hack {
   private String selectedCape = "无披风";
   private ResourceLocation currentCape = null;
   private HackConfig config;
   private static final String CONFIG_KEY = "披风";
   private final Map capes = new HashMap();

   public CapeHack() {
      super("本地披风", "仅自己可看见披风 注：如果你是房主(服务端)，要让客户端玩家安装这mod 可以双方可看见！如果客户端不安装就看不到", Hack.Category.RENDER, true);
      this.capes.put("无披风", (Object)null);
      this.capes.put("猎杀迁移者披风-紫(有鞘翹)", new ResourceLocation("lexis", "textures/cape/le.png"));
      this.capes.put("崩坏-星穹铁道长夜月(有鞘翹)", new ResourceLocation("lexis", "textures/cape/le2.png"));
      this.capes.put("很好的米塔(有鞘翹)", new ResourceLocation("lexis", "textures/cape/le3.png"));
      this.capes.put("帽子的米塔(有鞘翹)", new ResourceLocation("lexis", "textures/cape/le4.png"));
      this.capes.put("高木同学(有鞘翹)", new ResourceLocation("lexis", "textures/cape/le5.png"));
      this.capes.put("传说之下(有好看鞘翹)", new ResourceLocation("lexis", "textures/cape/le6.png"));
      this.capes.put("MeteorXBarricon(无鞘翹)", new ResourceLocation("lexis", "textures/cape/le7.png"));
      String[] capeOptions = (String[])this.capes.keySet().toArray(new String[0]);
      this.addSetting(new Hack.Setting("披风样式", "选择披风样式", "无披风", capeOptions));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void loadConfig() {
      this.selectedCape = this.config.getStringSetting("披风", "披风样式", "无披风");
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("披风样式")) {
            setting.setValue(this.selectedCape);
            break;
         }
      }

      this.updateCapeTexture();
   }

   private void saveConfig() {
      try {
         this.config.saveHackSettings("披风", this.getSettings());
      } catch (Exception var2) {
      }

   }

   private void updateCapeTexture() {
      this.currentCape = (ResourceLocation)this.capes.get(this.selectedCape);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         if (setting.getName().equals("披风样式")) {
            String newCape = setting.getString();
            if (!newCape.equals(this.selectedCape)) {
               this.selectedCape = newCape;
               this.updateCapeTexture();
               this.saveConfig();
            }
            break;
         }
      }

   }

   public void onClick() {
      this.toggle();
   }

   public ResourceLocation getCurrentCape() {
      return this.isEnabled() ? this.currentCape : null;
   }
}
