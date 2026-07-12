package moze_intel.projecte.api.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CustomConversionProvider implements DataProvider {
   private final Map customConversions = new LinkedHashMap();
   private final CompletableFuture lookupProvider;
   private final Path outputFolder;

   protected CustomConversionProvider(PackOutput output, CompletableFuture lookupProvider) {
      this.outputFolder = output.m_247566_(Target.DATA_PACK);
      this.lookupProvider = lookupProvider;
   }

   public CompletableFuture m_213708_(CachedOutput output) {
      return this.lookupProvider.thenApply((registries) -> {
         this.customConversions.clear();
         this.addCustomConversions(registries);
         return registries;
      }).thenCompose((registries) -> {
         List futures = new ArrayList();
         Iterator var4 = this.customConversions.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry entry = (Map.Entry)var4.next();
            ResourceLocation customConversion = (ResourceLocation)entry.getKey();
            Path var10000 = this.outputFolder;
            String var10001 = customConversion.m_135827_();
            Path path = var10000.resolve(var10001 + "/pe_custom_conversions/" + customConversion.m_135815_() + ".json");
            futures.add(DataProvider.m_253162_(output, ((CustomConversionBuilder)entry.getValue()).serialize(), path));
         }

         return CompletableFuture.allOf((CompletableFuture[])futures.toArray((x$0) -> {
            return new CompletableFuture[x$0];
         }));
      });
   }

   protected abstract void addCustomConversions(HolderLookup.Provider var1);

   protected CustomConversionBuilder createConversionBuilder(ResourceLocation id) {
      Objects.requireNonNull(id, "Custom Conversion Builder ID cannot be null.");
      if (this.customConversions.containsKey(id)) {
         throw new RuntimeException("Custom conversion '" + id + "' has already been registered.");
      } else {
         CustomConversionBuilder conversionBuilder = new CustomConversionBuilder(id);
         this.customConversions.put(id, conversionBuilder);
         return conversionBuilder;
      }
   }

   public String m_6055_() {
      return "Custom EMC Conversions";
   }
}
