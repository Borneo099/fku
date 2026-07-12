package moze_intel.projecte.emc.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import moze_intel.projecte.api.imc.NSSCreatorInfo;
import moze_intel.projecte.api.nss.NSSCreator;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;

public class NSSSerializer implements JsonSerializer, JsonDeserializer {
   public static final NSSSerializer INSTANCE = new NSSSerializer();
   public static final NSSCreator fakeCreator = NSSFake::create;
   public static final NSSCreator itemCreator = (itemName) -> {
      if (itemName.startsWith("#")) {
         return NSSItem.createTag(getResourceLocation(itemName.substring(1), "item tag"));
      } else {
         int nbtStart = itemName.indexOf(123);
         ResourceLocation resourceLocation = getResourceLocation(nbtStart == -1 ? itemName : itemName.substring(0, nbtStart), "item");
         if (nbtStart == -1) {
            return NSSItem.createItem(resourceLocation);
         } else {
            String nbtAsString = itemName.substring(nbtStart);

            try {
               return NSSItem.createItem(resourceLocation, TagParser.m_129359_(nbtAsString));
            } catch (CommandSyntaxException var5) {
               throw new JsonParseException("Malformed NBT compound", var5);
            }
         }
      }
   };
   public static final NSSCreator fluidCreator = (fluidName) -> {
      if (fluidName.startsWith("#")) {
         return NSSFluid.createTag(getResourceLocation(fluidName.substring(1), "fluid tag"));
      } else {
         int nbtStart = fluidName.indexOf(123);
         ResourceLocation resourceLocation = getResourceLocation(nbtStart == -1 ? fluidName : fluidName.substring(0, nbtStart), "fluid");
         if (nbtStart == -1) {
            return NSSFluid.createFluid(resourceLocation);
         } else {
            String nbtAsString = fluidName.substring(nbtStart);

            try {
               return NSSFluid.createFluid(resourceLocation, TagParser.m_129359_(nbtAsString));
            } catch (CommandSyntaxException var5) {
               throw new JsonParseException("Malformed NBT compound", var5);
            }
         }
      }
   };
   private Map creators = Collections.emptyMap();

   private static ResourceLocation getResourceLocation(String s, String type) throws JsonParseException {
      try {
         return new ResourceLocation(s);
      } catch (ResourceLocationException var3) {
         throw new JsonParseException("Malformed " + type + " ID", var3);
      }
   }

   public void setCreators(Map creators) {
      this.creators = ImmutableMap.copyOf(creators);
   }

   public NormalizedSimpleStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return this.deserialize(json.getAsString());
   }

   public NormalizedSimpleStack deserialize(String s) {
      if (s.contains("|")) {
         String[] parts = s.split("\\|");
         String key = parts[0];
         if (this.creators.containsKey(key)) {
            return ((NSSCreator)this.creators.get(key)).create(parts[1]);
         }
      }

      return itemCreator.create(s);
   }

   public JsonElement serialize(NormalizedSimpleStack src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.json());
   }

   public static void init() {
      registerDefault("FAKE", fakeCreator);
      registerDefault("ITEM", itemCreator);
      registerDefault("FLUID", fluidCreator);
   }

   private static void registerDefault(String key, NSSCreator creator) {
      InterModComms.sendTo("projecte", "register_nss_serializer", () -> {
         return new NSSCreatorInfo(key, creator);
      });
   }
}
