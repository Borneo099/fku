package moze_intel.projecte.network.commands.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import moze_intel.projecte.utils.RegistryUtils;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NSSItemParser {
   private static final DynamicCommandExceptionType UNKNOWN_ITEM;
   private static final DynamicCommandExceptionType UNKNOWN_TAG;
   private static final Function SUGGEST_NOTHING;
   private static final char SYNTAX_START_NBT = '{';
   private static final char SYNTAX_TAG = '#';
   private final HolderLookup items;
   private final StringReader reader;
   private Either result;
   private @Nullable CompoundTag nbt;
   private Function suggestions;

   public NSSItemParser(HolderLookup items, StringReader readerIn) {
      this.suggestions = SUGGEST_NOTHING;
      this.items = items;
      this.reader = readerIn;
   }

   public static NSSItemResult parseResult(HolderLookup items, StringReader reader) throws CommandSyntaxException {
      int cursor = reader.getCursor();

      try {
         NSSItemParser nssItemParser = new NSSItemParser(items, reader);
         nssItemParser.parse();
         return (NSSItemResult)nssItemParser.result.map((item) -> {
            return new ItemResult(item, nssItemParser.nbt);
         }, TagResult::new);
      } catch (CommandSyntaxException var4) {
         reader.setCursor(cursor);
         throw var4;
      }
   }

   public static CompletableFuture fillSuggestions(HolderLookup items, SuggestionsBuilder builder) {
      StringReader reader = new StringReader(builder.getInput());
      reader.setCursor(builder.getStart());
      NSSItemParser parser = new NSSItemParser(items, reader);

      try {
         parser.parse();
      } catch (CommandSyntaxException var5) {
      }

      return (CompletableFuture)parser.suggestions.apply(builder.createOffset(reader.getCursor()));
   }

   private void parse() throws CommandSyntaxException {
      this.suggestions = this::suggestTagOrItem;
      int cursor = this.reader.getCursor();
      ResourceLocation name;
      Optional item;
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.reader.expect('#');
         this.suggestions = this::suggestTag;
         name = ResourceLocation.m_135818_(this.reader);
         item = this.items.m_254901_(TagKey.m_203882_(Registries.f_256913_, name));
         item.orElseThrow(() -> {
            this.reader.setCursor(cursor);
            return UNKNOWN_TAG.createWithContext(this.reader, name);
         });
         this.result = Either.right(name);
      } else {
         name = ResourceLocation.m_135818_(this.reader);
         item = this.items.m_254902_(ResourceKey.m_135785_(Registries.f_256913_, name));
         this.result = Either.left((Holder)item.orElseThrow(() -> {
            this.reader.setCursor(cursor);
            return UNKNOWN_ITEM.createWithContext(this.reader, name);
         }));
         this.suggestions = this::suggestOpenNbt;
         if (this.reader.canRead() && this.reader.peek() == '{') {
            this.suggestions = SUGGEST_NOTHING;
            this.nbt = (new TagParser(this.reader)).m_129373_();
         }
      }

   }

   private CompletableFuture suggestOpenNbt(SuggestionsBuilder builder) {
      if (builder.getRemaining().isEmpty()) {
         builder.suggest(String.valueOf('{'));
      }

      return builder.buildFuture();
   }

   private CompletableFuture suggestTag(SuggestionsBuilder builder) {
      return SharedSuggestionProvider.m_205106_(this.items.m_214063_().map((reference) -> {
         return reference.m_205839_().f_203868_();
      }), builder, String.valueOf('#'));
   }

   private CompletableFuture suggestItem(SuggestionsBuilder builder) {
      return SharedSuggestionProvider.m_82957_(this.items.m_214062_().map((reference) -> {
         return reference.m_205785_().m_135782_();
      }), builder);
   }

   private CompletableFuture suggestTagOrItem(SuggestionsBuilder builder) {
      this.suggestTag(builder);
      return this.suggestItem(builder);
   }

   public static NSSItemResult resultOf(ItemStack stack) {
      return new ItemResult(stack.m_41720_(), stack.m_41783_());
   }

   static {
      PELang var10002 = PELang.UNKNOWN_ITEM;
      Objects.requireNonNull(var10002);
      UNKNOWN_ITEM = new DynamicCommandExceptionType((xva$0) -> {
         return var10002.translate(new Object[]{xva$0});
      });
      var10002 = PELang.UNKNOWN_TAG;
      Objects.requireNonNull(var10002);
      UNKNOWN_TAG = new DynamicCommandExceptionType((xva$0) -> {
         return var10002.translate(new Object[]{xva$0});
      });
      SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
   }

   public interface NSSItemResult {
      String getStringRepresentation();
   }

   private static record ItemResult(Item item, @Nullable CompoundTag nbt) implements NSSItemResult {
      public ItemResult(Holder item, @Nullable CompoundTag nbt) {
         this((Item)item.get(), nbt);
      }

      private ItemResult(Item item, @Nullable CompoundTag nbt) {
         this.item = item;
         this.nbt = nbt;
      }

      public String getStringRepresentation() {
         String registryName = RegistryUtils.getName(this.item).toString();
         return this.nbt == null ? registryName : registryName + this.nbt;
      }

      public Item item() {
         return this.item;
      }

      public @Nullable CompoundTag nbt() {
         return this.nbt;
      }
   }

   private static record TagResult(ResourceLocation tagName) implements NSSItemResult {
      private TagResult(ResourceLocation tagName) {
         this.tagName = tagName;
      }

      public String getStringRepresentation() {
         return "#" + this.tagName.toString();
      }

      public ResourceLocation tagName() {
         return this.tagName;
      }
   }
}
