package fku.org.example.fku.features.structure_locator;

import com.mojang.datafixers.util.Either;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

public final class SeedBiomeSampler {
    private static volatile HolderLookup.Provider vanilla;
    private static final Map<String, Entry> CACHE;

    private SeedBiomeSampler() {
    }

    public static synchronized boolean ensureReady() {
        if (vanilla == null) {
            try {
                vanilla = VanillaRegistries.createLookup();
            }
            catch (Throwable t) {
                return false;
            }
        }
        return vanilla != null;
    }

    private static synchronized Entry get(long seed, Dim dim) {
        String key = seed + "/" + dim;
        Entry e = CACHE.get(key);
        if (e != null) {
            return e;
        }
        if (!SeedBiomeSampler.ensureReady()) {
            return null;
        }
        try {
            BiomeSource source;
            HolderLookup.RegistryLookup nss = vanilla.lookupOrThrow(Registries.NOISE_SETTINGS);
            HolderLookup.RegistryLookup noise = vanilla.lookupOrThrow(Registries.NOISE);
            HolderLookup.RegistryLookup biomesLookup = vanilla.lookupOrThrow(Registries.BIOME);
            HolderLookup.RegistryLookup params = vanilla.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
            RandomState rs = RandomState.create((NoiseGeneratorSettings)(switch (dim) {
                case NETHER -> {
                    Holder.Reference param = params.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
                    source = SeedBiomeSampler.createMultiNoiseSource(param);
                    yield (NoiseGeneratorSettings)nss.getOrThrow(NoiseGeneratorSettings.NETHER).value();
                }
                case END -> {
                    source = TheEndBiomeSource.create((HolderGetter)biomesLookup);
                    yield (NoiseGeneratorSettings)nss.getOrThrow(NoiseGeneratorSettings.END).value();
                }
                default -> {
                    Holder.Reference param = params.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
                    source = SeedBiomeSampler.createMultiNoiseSource(param);
                    yield (NoiseGeneratorSettings)nss.getOrThrow(NoiseGeneratorSettings.OVERWORLD).value();
                }
            }), (HolderGetter)noise, seed);
            e = new Entry((BiomeSource)source, rs.sampler());
            CACHE.put(key, e);
            return e;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static MultiNoiseBiomeSource createMultiNoiseSource(Holder<?> param) {
        try {
            Constructor<?> ctor = MultiNoiseBiomeSource.class.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            if (ctor.getParameterCount() == 1) {
                Class<?> ptype = ctor.getParameterTypes()[0];
                if (ptype == Either.class) {
                    return (MultiNoiseBiomeSource)ctor.newInstance(Either.right(param));
                }
                return (MultiNoiseBiomeSource)ctor.newInstance(param);
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return null;
    }

    public static ResourceKey<Biome> biomeAt(long seed, Dim dim, int blockX, int blockY, int blockZ) {
        Entry e = SeedBiomeSampler.get(seed, dim);
        if (e == null) {
            return null;
        }
        Holder holder = e.source.getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), e.climate);
        return (ResourceKey<Biome>)holder.unwrapKey().orElse(null);
    }

    static {
        CACHE = new HashMap<String, Entry>();
    }

    public static enum Dim {
        OVERWORLD,
        NETHER,
        END;

    }

    private static final class Entry {
        final BiomeSource source;
        final Climate.Sampler climate;

        Entry(BiomeSource s, Climate.Sampler c) {
            this.source = s;
            this.climate = c;
        }
    }
}

