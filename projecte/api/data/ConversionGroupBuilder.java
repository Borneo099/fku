package moze_intel.projecte.api.data;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConversionGroupBuilder implements CustomConversionNSSHelper {
   private final CustomConversionBuilder customConversionBuilder;
   private final List conversions = new ArrayList();
   private @Nullable String comment;

   ConversionGroupBuilder(CustomConversionBuilder customConversionBuilder) {
      this.customConversionBuilder = customConversionBuilder;
   }

   public ConversionGroupBuilder comment(String comment) {
      CustomConversionBuilder.validateComment(this.comment, comment, "Group");
      this.comment = comment;
      return this;
   }

   public GroupConversionBuilder conversion(NormalizedSimpleStack output, int amount) {
      if (amount < 1) {
         throw new IllegalArgumentException("Output amount for fixed value conversions must be at least one.");
      } else {
         GroupConversionBuilder builder = new GroupConversionBuilder(output, amount);
         this.conversions.add(builder);
         return builder;
      }
   }

   public CustomConversionBuilder end() {
      return this.customConversionBuilder;
   }

   boolean hasComment() {
      return this.comment != null;
   }

   JsonObject serialize() {
      JsonObject json = new JsonObject();
      if (this.comment != null) {
         json.addProperty("comment", this.comment);
      }

      if (!this.conversions.isEmpty()) {
         json.add("conversions", CustomConversionBuilder.serializeConversions(this.conversions));
      }

      return json;
   }

   public class GroupConversionBuilder extends ConversionBuilder {
      private GroupConversionBuilder(NormalizedSimpleStack output, int count) {
         super(output, count);
      }

      public ConversionGroupBuilder end() {
         this.validateIngredients();
         return ConversionGroupBuilder.this;
      }
   }
}
