package moze_intel.projecte.gameObjs.customRecipes;

import com.google.gson.JsonObject;
import moze_intel.projecte.PECore;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class FullKleinStarsCondition implements ICondition {
   public static final FullKleinStarsCondition INSTANCE = new FullKleinStarsCondition();
   private static final ResourceLocation ID = PECore.rl("full_klein_stars");
   public static final IConditionSerializer SERIALIZER = new IConditionSerializer() {
      public void write(JsonObject json, FullKleinStarsCondition value) {
      }

      public FullKleinStarsCondition read(JsonObject json) {
         return FullKleinStarsCondition.INSTANCE;
      }

      public ResourceLocation getID() {
         return FullKleinStarsCondition.ID;
      }
   };

   private FullKleinStarsCondition() {
   }

   public ResourceLocation getID() {
      return ID;
   }

   public boolean test(ICondition.IContext context) {
      return ProjectEConfig.common.fullKleinStars.get();
   }
}
