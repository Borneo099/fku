package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.imc.WorldTransmutationEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.InterModComms;
import org.jetbrains.annotations.Nullable;

public final class WorldTransmutations {
   private static List DEFAULT_ENTRIES = Collections.emptyList();
   private static List ENTRIES = Collections.emptyList();

   public static void init() {
      registerDefault(Blocks.f_50069_, Blocks.f_50652_, Blocks.f_50440_);
      registerDefault(Blocks.f_50652_, Blocks.f_50069_, Blocks.f_50440_);
      registerDefault(Blocks.f_50440_, Blocks.f_49992_, Blocks.f_50652_);
      registerDefault(Blocks.f_50493_, Blocks.f_49992_, Blocks.f_50652_);
      registerDefault(Blocks.f_49992_, Blocks.f_50440_, Blocks.f_50652_);
      registerBackAndForth(Blocks.f_49994_, Blocks.f_50062_);
      registerBackAndForth(Blocks.f_49990_, Blocks.f_50126_);
      registerBackAndForth(Blocks.f_49991_, Blocks.f_50080_);
      registerBackAndForth(Blocks.f_50186_, Blocks.f_50133_);
      registerDefault(Blocks.f_50122_, Blocks.f_50228_, Blocks.f_50334_);
      registerDefault(Blocks.f_50228_, Blocks.f_50334_, Blocks.f_50122_);
      registerDefault(Blocks.f_50334_, Blocks.f_50122_, Blocks.f_50228_);
      registerConsecutivePairsAllStates(Blocks.f_49999_, Blocks.f_50001_, Blocks.f_50000_, Blocks.f_50002_, Blocks.f_50003_, Blocks.f_50004_, Blocks.f_220832_, Blocks.f_271170_);
      registerConsecutivePairsAllStates(Blocks.f_50010_, Blocks.f_50006_, Blocks.f_50005_, Blocks.f_50007_, Blocks.f_50008_, Blocks.f_50009_, Blocks.f_220835_, Blocks.f_271326_);
      registerConsecutivePairsAllStates(Blocks.f_50011_, Blocks.f_50013_, Blocks.f_50012_, Blocks.f_50014_, Blocks.f_50015_, Blocks.f_50043_, Blocks.f_220836_, Blocks.f_271348_);
      registerConsecutivePairsAllStates(Blocks.f_50044_, Blocks.f_50046_, Blocks.f_50045_, Blocks.f_50047_, Blocks.f_50048_, Blocks.f_50049_, Blocks.f_220837_, Blocks.f_271145_);
      registerConsecutivePairsAllStates(Blocks.f_50050_, Blocks.f_50052_, Blocks.f_50051_, Blocks.f_50053_, Blocks.f_50054_, Blocks.f_50055_, Blocks.f_220838_, Blocks.f_271115_);
      registerConsecutivePairs(Blocks.f_50746_, Blocks.f_50748_, Blocks.f_50747_, Blocks.f_50749_, Blocks.f_50750_, Blocks.f_50751_, Blocks.f_220831_, Blocks.f_271334_);
      registerConsecutivePairs(Blocks.f_50705_, Blocks.f_50742_, Blocks.f_50741_, Blocks.f_50743_, Blocks.f_50744_, Blocks.f_50745_, Blocks.f_220865_, Blocks.f_271304_, Blocks.f_244477_);
      registerConsecutivePairsAllStates(Blocks.f_50398_, Blocks.f_50400_, Blocks.f_50399_, Blocks.f_50401_, Blocks.f_50402_, Blocks.f_50403_, Blocks.f_220851_, Blocks.f_271301_, Blocks.f_244004_);
      registerConsecutivePairsAllStates(Blocks.f_50086_, Blocks.f_50270_, Blocks.f_50269_, Blocks.f_50271_, Blocks.f_50372_, Blocks.f_50373_, Blocks.f_220848_, Blocks.f_271206_, Blocks.f_243755_);
      registerConsecutivePairsAllStates(Blocks.f_50132_, Blocks.f_50480_, Blocks.f_50479_, Blocks.f_50481_, Blocks.f_50482_, Blocks.f_50483_, Blocks.f_220852_, Blocks.f_271219_, Blocks.f_244641_);
      registerConsecutivePairs(Blocks.f_50167_, Blocks.f_50169_, Blocks.f_50168_, Blocks.f_50170_, Blocks.f_50171_, Blocks.f_50172_, Blocks.f_220840_, Blocks.f_271227_, Blocks.f_244183_);
      registerConsecutivePairs(Blocks.f_50542_, Blocks.f_50543_, Blocks.f_50544_, Blocks.f_50545_, Blocks.f_50494_, Blocks.f_50495_, Blocks.f_50496_, Blocks.f_50497_, Blocks.f_50498_, Blocks.f_50499_, Blocks.f_50500_, Blocks.f_50501_, Blocks.f_50502_, Blocks.f_50503_, Blocks.f_50504_, Blocks.f_50505_);
      registerConsecutivePairs(Blocks.f_50506_, Blocks.f_50507_, Blocks.f_50508_, Blocks.f_50509_, Blocks.f_50510_, Blocks.f_50511_, Blocks.f_50512_, Blocks.f_50513_, Blocks.f_50514_, Blocks.f_50515_, Blocks.f_50516_, Blocks.f_50517_, Blocks.f_50518_, Blocks.f_50519_, Blocks.f_50573_, Blocks.f_50574_);
      registerConsecutivePairs(Blocks.f_50336_, Blocks.f_50337_, Blocks.f_50338_, Blocks.f_50339_, Blocks.f_50340_, Blocks.f_50341_, Blocks.f_50342_, Blocks.f_50343_, Blocks.f_50344_, Blocks.f_50345_, Blocks.f_50346_, Blocks.f_50347_, Blocks.f_50348_, Blocks.f_50349_, Blocks.f_50350_, Blocks.f_50351_);
      registerConsecutivePairs(Blocks.f_50041_, Blocks.f_50042_, Blocks.f_50096_, Blocks.f_50097_, Blocks.f_50098_, Blocks.f_50099_, Blocks.f_50100_, Blocks.f_50101_, Blocks.f_50102_, Blocks.f_50103_, Blocks.f_50104_, Blocks.f_50105_, Blocks.f_50106_, Blocks.f_50107_, Blocks.f_50108_, Blocks.f_50109_);
      registerConsecutivePairs(Blocks.f_50287_, Blocks.f_50288_, Blocks.f_50289_, Blocks.f_50290_, Blocks.f_50291_, Blocks.f_50292_, Blocks.f_50293_, Blocks.f_50294_, Blocks.f_50295_, Blocks.f_50296_, Blocks.f_50297_, Blocks.f_50298_, Blocks.f_50299_, Blocks.f_50300_, Blocks.f_50301_, Blocks.f_50302_);
      registerConsecutivePairs(Blocks.f_50147_, Blocks.f_50148_, Blocks.f_50202_, Blocks.f_50203_, Blocks.f_50204_, Blocks.f_50205_, Blocks.f_50206_, Blocks.f_50207_, Blocks.f_50208_, Blocks.f_50209_, Blocks.f_50210_, Blocks.f_50211_, Blocks.f_50212_, Blocks.f_50213_, Blocks.f_50214_, Blocks.f_50215_);
      registerConsecutivePairsAllStates(Blocks.f_50303_, Blocks.f_50304_, Blocks.f_50305_, Blocks.f_50306_, Blocks.f_50307_, Blocks.f_50361_, Blocks.f_50362_, Blocks.f_50363_, Blocks.f_50364_, Blocks.f_50365_, Blocks.f_50366_, Blocks.f_50367_, Blocks.f_50368_, Blocks.f_50369_, Blocks.f_50370_, Blocks.f_50371_);
      registerBackAndForth(Blocks.f_50135_, Blocks.f_50136_);
      registerDefault(Blocks.f_50134_, Blocks.f_50699_, Blocks.f_50690_);
      registerDefault(Blocks.f_50699_, Blocks.f_50690_, Blocks.f_50134_);
      registerDefault(Blocks.f_50690_, Blocks.f_50699_, Blocks.f_50134_);
      registerBackAndForthAllStates(Blocks.f_50695_, Blocks.f_50686_);
      registerBackAndForthAllStates(Blocks.f_50696_, Blocks.f_50687_);
      registerBackAndForth(Blocks.f_50697_, Blocks.f_50688_);
      registerBackAndForth(Blocks.f_50698_, Blocks.f_50689_);
      registerBackAndForth(Blocks.f_50451_, Blocks.f_50692_);
      registerBackAndForth(Blocks.f_50700_, Blocks.f_50691_);
      registerBackAndForth(Blocks.f_50654_, Blocks.f_50693_);
      registerBackAndForth(Blocks.f_50655_, Blocks.f_50656_);
      registerBackAndForthAllStates(Blocks.f_50657_, Blocks.f_50658_);
      registerBackAndForthAllStates(Blocks.f_50667_, Blocks.f_50668_);
      registerBackAndForthAllStates(Blocks.f_50661_, Blocks.f_50662_);
      registerBackAndForthAllStates(Blocks.f_50659_, Blocks.f_50660_);
   }

   public static @Nullable BlockState getWorldTransmutation(BlockState current, boolean isSneaking) {
      Iterator var2 = ENTRIES.iterator();

      WorldTransmutationEntry e;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         e = (WorldTransmutationEntry)var2.next();
      } while(e.origin() != current);

      return isSneaking ? e.altResult() : e.result();
   }

   public static List getWorldTransmutations() {
      return ENTRIES;
   }

   public static void setWorldTransmutation(List entries) {
      DEFAULT_ENTRIES = ImmutableList.copyOf(entries);
      resetWorldTransmutations();
   }

   public static void resetWorldTransmutations() {
      ENTRIES = new ArrayList(DEFAULT_ENTRIES);
   }

   public static void register(BlockState from, BlockState result, @Nullable BlockState altResult) {
      ENTRIES.add(new WorldTransmutationEntry(from, result, altResult));
   }

   private static void registerIMC(BlockState from, BlockState result, @Nullable BlockState altResult) {
      InterModComms.sendTo("projecte", "register_world_transmutation", () -> {
         return new WorldTransmutationEntry(from, result, altResult);
      });
   }

   private static void registerDefault(Block from, Block result, @Nullable Block altResult) {
      registerIMC(from.m_49966_(), result.m_49966_(), (BlockState)null);
   }

   private static void registerAllStates(Block from, Block result, @Nullable Block altResult) {
      StateDefinition stateContainer = from.m_49965_();
      ImmutableList validStates = stateContainer.m_61056_();
      UnmodifiableIterator var5 = validStates.iterator();

      while(var5.hasNext()) {
         BlockState validState = (BlockState)var5.next();

         try {
            BlockState resultState = copyProperties(validState, result.m_49966_());
            BlockState altResultState = altResult == null ? null : copyProperties(validState, altResult.m_49966_());
            registerIMC(validState, resultState, altResultState);
         } catch (IllegalArgumentException var9) {
            PECore.LOGGER.error("Something went wrong registering conversions for {}", RegistryUtils.getName(from), var9);
         }
      }

   }

   private static BlockState copyProperties(BlockState source, BlockState target) {
      ImmutableMap values = source.m_61148_();

      Map.Entry entry;
      for(UnmodifiableIterator var3 = values.entrySet().iterator(); var3.hasNext(); target = applyProperty(target, (Property)entry.getKey(), (Comparable)entry.getValue())) {
         entry = (Map.Entry)var3.next();
      }

      return target;
   }

   private static BlockState applyProperty(BlockState target, Property property, Comparable value) {
      return (BlockState)target.m_61124_(property, value);
   }

   private static void registerBackAndForth(Block first, Block second) {
      registerDefault(first, second, (Block)null);
      registerDefault(second, first, (Block)null);
   }

   private static void registerBackAndForthAllStates(Block first, Block second) {
      registerAllStates(first, second, (Block)null);
      registerAllStates(second, first, (Block)null);
   }

   private static void registerConsecutivePairs(RegisterBlock registerMethod, Block... blocks) {
      for(int i = 0; i < blocks.length; ++i) {
         Block prev = i == 0 ? blocks[blocks.length - 1] : blocks[i - 1];
         Block cur = blocks[i];
         Block next = i == blocks.length - 1 ? blocks[0] : blocks[i + 1];
         registerMethod.register(cur, next, prev);
      }

   }

   private static void registerConsecutivePairs(Block... blocks) {
      registerConsecutivePairs(WorldTransmutations::registerDefault, blocks);
   }

   private static void registerConsecutivePairsAllStates(Block... blocks) {
      registerConsecutivePairs(WorldTransmutations::registerAllStates, blocks);
   }

   @FunctionalInterface
   private interface RegisterBlock {
      void register(Block var1, Block var2, @Nullable Block var3);
   }
}
