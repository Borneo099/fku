package moze_intel.projecte.emc.mappers.customConversions.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.mappers.customConversions.CustomConversionMapper;

public class CustomConversionDeserializer implements JsonDeserializer {
   public CustomConversion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      CustomConversion out = new CustomConversion();
      JsonObject o = json.getAsJsonObject();
      boolean foundOutput = false;
      boolean foundIngredients = false;
      Iterator var8 = o.entrySet().iterator();

      while(true) {
         while(var8.hasNext()) {
            Map.Entry entry = (Map.Entry)var8.next();
            JsonElement element = (JsonElement)entry.getValue();
            if ("count".equalsIgnoreCase((String)entry.getKey())) {
               out.count = element.getAsInt();
            } else if ("output".equals(entry.getKey())) {
               if (foundOutput) {
                  throw new JsonParseException("Multiple values for output field");
               }

               foundOutput = true;
               out.output = (NormalizedSimpleStack)context.deserialize(new JsonPrimitive(element.getAsString()), NormalizedSimpleStack.class);
            } else if (!"ingredients".equals(entry.getKey())) {
               if (!((String)entry.getKey()).equalsIgnoreCase("propagateTags")) {
                  throw new JsonParseException(String.format("Unknown Key: %s in Conversion with value %s", entry.getKey(), element));
               }

               out.propagateTags = element.getAsBoolean();
            } else {
               if (foundIngredients) {
                  throw new JsonParseException("Multiple values for ingredient field");
               }

               foundIngredients = true;
               if (!element.isJsonArray()) {
                  if (!element.isJsonObject()) {
                     throw new JsonParseException("Could not parse ingredients!");
                  }

                  out.ingredients = (Map)CustomConversionMapper.GSON.fromJson(element, (new TypeToken() {
                  }).getType());
               } else {
                  Map outMap = new HashMap();
                  Iterator var12 = element.getAsJsonArray().iterator();

                  while(var12.hasNext()) {
                     JsonElement e = (JsonElement)var12.next();
                     NormalizedSimpleStack v = (NormalizedSimpleStack)context.deserialize(new JsonPrimitive(e.getAsString()), NormalizedSimpleStack.class);
                     outMap.merge(v, 1, Integer::sum);
                  }

                  out.ingredients = outMap;
               }
            }
         }

         if (!foundOutput) {
            throw new JsonParseException("No output declared");
         }

         if (!foundIngredients) {
            throw new JsonParseException("No ingredients declared");
         }

         if (out.count < 1) {
            throw new JsonParseException("Output count must be at least one");
         }

         return out;
      }
   }
}
