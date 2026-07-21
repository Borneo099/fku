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
                vanilla = VanillaRegistries.m_255371_();
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
            TheEndBiomeSource source;
            HolderLookup.RegistryLookup nss = vanilla.m_255025_(Registries.f_256932_);
            HolderLookup.RegistryLookup noise = vanilla.m_255025_(Registries.f_256865_);
            HolderLookup.RegistryLookup biomesLookup = vanilla.m_255025_(Registries.f_256952_);
            HolderLookup.RegistryLookup params = vanilla.m_255025_(Registries.f_273919_);
            RandomState rs = RandomState.m_255302_((NoiseGeneratorSettings)(switch (dim) {
                case Dim.NETHER -> {
                    Holder.Reference param = params.m_255043_(MultiNoiseBiomeSourceParameterLists.f_273830_);
                    source = SeedBiomeSampler.createMultiNoiseSource(param);
                    yield (NoiseGeneratorSettings)nss.m_255043_(NoiseGeneratorSettings.f_64434_).m_203334_();
                }
                case Dim.END -> {
                    source = TheEndBiomeSource.m_254978_((HolderGetter)biomesLookup);
                    yield (NoiseGeneratorSettings)nss.m_255043_(NoiseGeneratorSettings.f_64435_).m_203334_();
                }
                default -> {
                    Holder.Reference param = params.m_255043_(MultiNoiseBiomeSourceParameterLists.f_273878_);
                    source = SeedBiomeSampler.createMultiNoiseSource(param);
                    yield (NoiseGeneratorSettings)nss.m_255043_(NoiseGeneratorSettings.f_64432_).m_203334_();
                }
            }), (HolderGetter)noise, seed);
            e = new Entry((BiomeSource)source, rs.m_224579_());
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
        Holder holder = e.source.m_203407_(QuartPos.m_175400_(blockX), QuartPos.m_175400_(blockY), QuartPos.m_175400_(blockZ), e.climate);
        return holder.m_203543_().orElse(null);
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

