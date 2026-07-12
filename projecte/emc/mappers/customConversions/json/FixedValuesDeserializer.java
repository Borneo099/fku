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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

public class FixedValuesDeserializer implements JsonDeserializer {
   public FixedValues deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      FixedValues fixed = new FixedValues();
      JsonObject o = json.getAsJsonObject();
      Iterator var6 = o.entrySet().iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         switch ((String)entry.getKey()) {
            case "before":
               fixed.setValueBefore = this.parseSetValueMap(((JsonElement)entry.getValue()).getAsJsonObject(), context);
               break;
            case "after":
               fixed.setValueAfter = this.parseSetValueMap(((JsonElement)entry.getValue()).getAsJsonObject(), context);
               break;
            case "conversion":
               fixed.conversion = (List)context.deserialize(((JsonElement)entry.getValue()).getAsJsonArray(), (new TypeToken() {
               }).getType());
               break;
            default:
               throw new JsonParseException(String.format("Can not parse \"%s\":%s in fixedValues", key, entry.getValue()));
         }
      }

      return fixed;
   }

   private Map parseSetValueMap(JsonObject o, JsonDeserializationContext context) {
      Map out = new HashMap();
      Iterator var4 = o.entrySet().iterator();

      while(true) {
         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            JsonPrimitive primitive = ((JsonElement)entry.getValue()).getAsJsonPrimitive();
            if (!primitive.isNumber()) {
               if (!primitive.isString() || !primitive.getAsString().toLowerCase(Locale.ROOT).equals("free")) {
                  throw new JsonParseException("Could not parse " + o + " into 'free' or integer.");
               }

               out.put((NormalizedSimpleStack)context.deserialize(new JsonPrimitive((String)entry.getKey()), NormalizedSimpleStack.class), Long.MIN_VALUE);
            } else {
               long value = primitive.getAsLong();
               if (value < 1L) {
                  throw new JsonParseException("EMC value must be at least one.");
               }

               out.put((NormalizedSimpleStack)context.deserialize(new JsonPrimitive((String)entry.getKey()), NormalizedSimpleStack.class), value);
            }
         }

         return out;
      }
   }
}
