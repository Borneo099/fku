package fku.org.example.fku.features.dynamicisland;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DynamicIslandFeature implements IDynamicIslandNotifier {
   public static void init() {
      DynamicIslandConfig.getInstance();
   }

   public String getFeatureId() {
      return "dynamic_island";
   }

   public String getFeatureDisplayName() {
      return "灵动岛";
   }
}
