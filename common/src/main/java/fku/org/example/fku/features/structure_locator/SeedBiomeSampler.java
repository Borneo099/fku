package fku.org.example.fku.features.structure_locator;

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

import java.util.HashMap;
import java.util.Map;

public final class SeedBiomeSampler {

    private static volatile HolderLookup.Provider vanilla;
    private static final Map<String, Entry> CACHE = new HashMap<>();

    private SeedBiomeSampler() {}

    public static synchronized boolean ensureReady() {
        if (vanilla == null) {
            try { vanilla = VanillaRegistries.createLookup(); }
            catch (Throwable t) { return false; }
        }
        return vanilla != null;
    }

    public enum Dim { OVERWORLD, NETHER, END }

    private static synchronized Entry get(long seed, Dim dim) {
        String key = seed + "/" + dim;
        Entry e = CACHE.get(key);
        if (e != null) return e;
        if (!ensureReady()) return null;
        try {
            var nss = vanilla.lookupOrThrow(Registries.NOISE_SETTINGS);
            var noise = vanilla.lookupOrThrow(Registries.NOISE);
            var biomesLookup = vanilla.lookupOrThrow(Registries.BIOME);
            var params = vanilla.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

            BiomeSource source;
            NoiseGeneratorSettings ns;
            switch (dim) {
                case NETHER -> {
                    var param = params.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
                    source = createMultiNoiseSource((Holder) param);
                    ns = nss.getOrThrow(NoiseGeneratorSettings.NETHER).value();
                }
                case END -> {
                    source = TheEndBiomeSource.create((HolderGetter<Biome>) biomesLookup);
                    ns = nss.getOrThrow(NoiseGeneratorSettings.END).value();
                }
                default -> {
                    var param = params.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
                    source = createMultiNoiseSource((Holder) param);
                    ns = nss.getOrThrow(NoiseGeneratorSettings.OVERWORLD).value();
                }
            }
            RandomState rs = RandomState.create(ns, (HolderGetter) noise, seed);
            e = new Entry(source, rs.sampler());
            CACHE.put(key, e);
            return e;
        } catch (Throwable t) { return null; }
    }

    @SuppressWarnings("unchecked")
    private static MultiNoiseBiomeSource createMultiNoiseSource(Holder<?> param) {
        try {
            var ctor = MultiNoiseBiomeSource.class.getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            if (ctor.getParameterCount() == 1) {
                Class<?> ptype = ctor.getParameterTypes()[0];
                if (ptype == com.mojang.datafixers.util.Either.class)
                    return (MultiNoiseBiomeSource) ctor.newInstance(com.mojang.datafixers.util.Either.right(param));
                return (MultiNoiseBiomeSource) ctor.newInstance(param);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static ResourceKey<Biome> biomeAt(long seed, Dim dim, int blockX, int blockY, int blockZ) {
        Entry e = get(seed, dim);
        if (e == null) return null;
        Holder<Biome> holder = e.source.getNoiseBiome(
                QuartPos.fromBlock(blockX), QuartPos.fromBlock(blockY), QuartPos.fromBlock(blockZ), e.climate);
        return holder.unwrapKey().orElse(null);
    }

    private static final class Entry {
        final BiomeSource source;
        final Climate.Sampler climate;
        Entry(BiomeSource s, Climate.Sampler c) { this.source = s; this.climate = c; }
    }
}
