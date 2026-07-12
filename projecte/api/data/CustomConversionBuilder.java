package moze_intel.projecte.api.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomConversionBuilder implements CustomConversionBuilderNSSHelper {
   private static final long FREE_ARITHMETIC_VALUE = Long.MIN_VALUE;
   private final Map groups = new LinkedHashMap();
   private final Map fixedValueBefore = new LinkedHashMap();
   private final Map fixedValueAfter = new LinkedHashMap();
   private final List fixedValueConversions = new ArrayList();
   private final ResourceLocation id;
   private boolean replace;
   private @Nullable String comment;

   CustomConversionBuilder(ResourceLocation id) {
      this.id = id;
   }

   public CustomConversionBuilder comment(String comment) {
      validateComment(this.comment, comment, "Custom Conversion");
      this.comment = comment;
      return this;
   }

   public CustomConversionBuilder replace() {
      if (this.replace) {
         throw new RuntimeException("Replace has already been set, remove unnecessary call.");
      } else {
         this.replace = true;
         return this;
      }
   }

   public ConversionGroupBuilder group(String groupName) {
      Objects.requireNonNull(groupName, "Group name cannot be null.");
      if (this.groups.containsKey(groupName)) {
         throw new RuntimeException("Group with name '" + groupName + "' already exists.");
      } else {
         ConversionGroupBuilder builder = new ConversionGroupBuilder(this);
         this.groups.put(groupName, builder);
         return builder;
      }
   }

   public CustomConversionBuilder before(NormalizedSimpleStack stack, long emc) {
      return this.fixedValue(stack, emc, this.fixedValueBefore, "before");
   }

   public CustomConversionBuilder before(NormalizedSimpleStack stack) {
      return this.fixedValue(stack, Long.MIN_VALUE, this.fixedValueBefore, "before");
   }

   public CustomConversionBuilder after(NormalizedSimpleStack stack, long emc) {
      return this.fixedValue(stack, emc, this.fixedValueAfter, "after");
   }

   public CustomConversionBuilder after(NormalizedSimpleStack stack) {
      return this.fixedValue(stack, Long.MIN_VALUE, this.fixedValueAfter, "after");
   }

   private CustomConversionBuilder fixedValue(NormalizedSimpleStack stack, long emc, Map fixedValues, String type) {
      Objects.requireNonNull(stack, "Normalized Simple Stack cannot be null.");
      if (emc < 1L && emc != Long.MIN_VALUE) {
         throw new IllegalArgumentException("EMC value must be at least one.");
      } else if (fixedValues.containsKey(stack)) {
         throw new RuntimeException("Fixed value " + type + " already set for '" + stack + "'.");
      } else {
         fixedValues.put(stack, emc);
         return this;
      }
   }

   public FixedValueConversionBuilder conversion(NormalizedSimpleStack output, int amount) {
      if (amount < 1) {
         throw new IllegalArgumentException("Output amount for fixed value conversions must be at least one.");
      } else {
         FixedValueConversionBuilder builder = new FixedValueConversionBuilder(output, amount);
         this.fixedValueConversions.add(builder);
         return builder;
      }
   }

   JsonObject serialize() {
      JsonObject json = new JsonObject();
      if (this.comment != null) {
         json.addProperty("comment", this.comment);
      }

      if (this.replace) {
         json.addProperty("replace", true);
      }

      JsonObject fixedValues;
      if (!this.groups.isEmpty()) {
         fixedValues = new JsonObject();
         Iterator var3 = this.groups.entrySet().iterator();

         while(var3.hasNext()) {
            Map.Entry entry = (Map.Entry)var3.next();
            String groupName = (String)entry.getKey();
            ConversionGroupBuilder group = (ConversionGroupBuilder)entry.getValue();
            JsonObject groupJson = group.serialize();
            validateNonEmpty(groupJson, group.hasComment(), "Group", groupName);
            fixedValues.add(groupName, groupJson);
         }

         json.add("groups", fixedValues);
      }

      if (!this.fixedValueBefore.isEmpty() || !this.fixedValueAfter.isEmpty() || !this.fixedValueConversions.isEmpty()) {
         fixedValues = new JsonObject();
         if (!this.fixedValueBefore.isEmpty()) {
            fixedValues.add("before", serializeFixedValues(this.fixedValueBefore));
         }

         if (!this.fixedValueAfter.isEmpty()) {
            fixedValues.add("after", serializeFixedValues(this.fixedValueAfter));
         }

         if (!this.fixedValueConversions.isEmpty()) {
            fixedValues.add("conversion", serializeConversions(this.fixedValueConversions));
         }

         json.add("values", fixedValues);
      }

      validateNonEmpty(json, this.comment != null, "Custom conversion", this.id.toString());
      return json;
   }

   private static void validateNonEmpty(JsonObject json, boolean hasComment, String type, String name) {
      int elements = json.size();
      if (elements == 0) {
         throw new RuntimeException(type + " '" + name + "' is empty and should be removed.");
      } else if (elements == 1 && hasComment) {
         throw new RuntimeException(type + " '" + name + "' consists only of a comment and should be removed.");
      }
   }

   private static JsonObject serializeFixedValues(Map fixedValues) {
      JsonObject json = new JsonObject();
      Iterator var2 = fixedValues.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         String key = ((NormalizedSimpleStack)entry.getKey()).json();
         long emc = (Long)entry.getValue();
         if (emc == Long.MIN_VALUE) {
            json.addProperty(key, "free");
         } else {
            json.addProperty(key, emc);
         }
      }

      return json;
   }

   static JsonArray serializeConversions(List conversions) {
      Set addedConversions = new HashSet();
      JsonArray jsonConversions = new JsonArray();
      Iterator var3 = conversions.iterator();

      while(var3.hasNext()) {
         ConversionBuilder conversion = (ConversionBuilder)var3.next();
         JsonObject jsonConversion = conversion.serialize();
         if (!addedConversions.add(jsonConversion)) {
            throw new RuntimeException("Duplicate conversion: " + conversion + ". This is likely a copy paste error and should be removed.");
         }

         jsonConversions.add(jsonConversion);
      }

      return jsonConversions;
   }

   static void validateComment(@Nullable String currentComment, String comment, String location) {
      Objects.requireNonNull(comment, "Comment defaults to null, remove unnecessary call.");
      if (currentComment != null) {
         throw new RuntimeException(location + " Builder already has a comment declared.");
      }
   }

   public class FixedValueConversionBuilder extends ConversionBuilder {
      private FixedValueConversionBuilder(NormalizedSimpleStack output, int count) {
         super(output, count);
      }

      public CustomConversionBuilder end() {
         this.validateIngredients();
         return CustomConversionBuilder.this;
      }
   }
}
