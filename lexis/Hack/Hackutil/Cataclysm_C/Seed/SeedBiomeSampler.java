package lexis.Hack.Hackutil.Cataclysm_C.Seed;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

public final class SeedBiomeSampler {
   private static volatile HolderLookup.Provider vanilla;
   private static final Map CACHE = new HashMap();

   private SeedBiomeSampler() {
   }

   public static synchronized boolean ensureReady() {
      if (vanilla == null) {
         try {
            vanilla = VanillaRegistries.m_255371_();
         } catch (Throwable var1) {
            return false;
         }
      }

      return vanilla != null;
   }

   private static synchronized Entry get(long seed, Dim dim) {
      String key = "" + seed + "/" + String.valueOf(dim);
      Entry e = (Entry)CACHE.get(key);
      if (e != null) {
         return e;
      } else if (!ensureReady()) {
         return null;
      } else {
         try {
            HolderGetter nss = vanilla.m_255025_(Registries.f_256932_);
            HolderGetter noise = vanilla.m_255025_(Registries.f_256865_);
            HolderGetter biomes = vanilla.m_255025_(Registries.f_256952_);
            HolderGetter params = vanilla.m_255025_(Registries.f_273919_);
            Object source;
            ResourceKey nsKey;
            switch (dim) {
               case NETHER:
                  source = MultiNoiseBiomeSource.m_274591_(params.m_255043_(MultiNoiseBiomeSourceParameterLists.f_273830_));
                  nsKey = NoiseGeneratorSettings.f_64434_;
                  break;
               case END:
                  source = TheEndBiomeSource.m_254978_(biomes);
                  nsKey = NoiseGeneratorSettings.f_64435_;
                  break;
               case OVERWORLD:
               default:
                  source = MultiNoiseBiomeSource.m_274591_(params.m_255043_(MultiNoiseBiomeSourceParameterLists.f_273878_));
                  nsKey = NoiseGeneratorSettings.f_64432_;
            }

            NoiseGeneratorSettings ns = (NoiseGeneratorSettings)nss.m_255043_(nsKey).m_203334_();
            RandomState rs = RandomState.m_255302_(ns, noise, seed);
            e = new Entry((BiomeSource)source, rs.m_224579_());
            CACHE.put(key, e);
            return e;
         } catch (Throwable var13) {
            return null;
         }
      }
   }

   public static ResourceKey biomeAt(long seed, Dim dim, int blockX, int blockY, int blockZ) {
      Entry e = get(seed, dim);
      if (e == null) {
         return null;
      } else {
         Holder h = e.source.m_203407_(QuartPos.m_175400_(blockX), QuartPos.m_175400_(blockY), QuartPos.m_175400_(blockZ), e.climate);
         return (ResourceKey)h.m_203543_().orElse((Object)null);
      }
   }

   private static final class Entry {
      final BiomeSource source;
      final Climate.Sampler climate;

      Entry(BiomeSource s, Climate.Sampler c) {
         this.source = s;
         this.climate = c;
      }
   }

   public static enum Dim {
      OVERWORLD,
      NETHER,
      END;

      // $FF: synthetic method
      private static Dim[] $values() {
         return new Dim[]{OVERWORLD, NETHER, END};
      }
   }
}
