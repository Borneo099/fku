package moze_intel.projecte.gameObjs.customRecipes;

import com.google.gson.JsonObject;
import moze_intel.projecte.PECore;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class TomeEnabledCondition implements ICondition {
   public static final TomeEnabledCondition INSTANCE = new TomeEnabledCondition();
   private static final ResourceLocation ID = PECore.rl("tome_enabled");
   public static final IConditionSerializer SERIALIZER = new IConditionSerializer() {
      public void write(JsonObject json, TomeEnabledCondition value) {
      }

      public TomeEnabledCondition read(JsonObject json) {
         return TomeEnabledCondition.INSTANCE;
      }

      public ResourceLocation getID() {
         return TomeEnabledCondition.ID;
      }
   };

   private TomeEnabledCondition() {
   }

   public ResourceLocation getID() {
      return ID;
   }

   public boolean test(ICondition.IContext context) {
      return ProjectEConfig.common.craftableTome.get();
   }
}
