package moze_intel.projecte.network.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.network.commands.parser.NSSItemParser;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

public class NSSItemArgument implements ArgumentType {
   private static final Collection EXAMPLES = Arrays.asList("stick", "minecraft:stick", "minecraft:stick{foo=bar}", "#minecraft:wool");
   private final HolderLookup items;

   private NSSItemArgument(CommandBuildContext context) {
      this.items = context.m_227133_(Registries.f_256913_);
   }

   public static NSSItemArgument nss(CommandBuildContext context) {
      return new NSSItemArgument(context);
   }

   public NSSItemParser.NSSItemResult parse(StringReader reader) throws CommandSyntaxException {
      return NSSItemParser.parseResult(this.items, reader);
   }

   public static NSSItemParser.NSSItemResult getNSS(CommandContext context, String name) {
      return (NSSItemParser.NSSItemResult)context.getArgument(name, NSSItemParser.NSSItemResult.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return NSSItemParser.fillSuggestions(this.items, builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }
}
