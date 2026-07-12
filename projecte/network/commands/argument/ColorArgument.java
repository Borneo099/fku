package moze_intel.projecte.network.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.item.DyeColor;

public class ColorArgument implements ArgumentType {
   private static final List EXAMPLES = Arrays.asList("red", "brown", "light_gray");

   private ColorArgument() {
   }

   public static ColorArgument color() {
      return new ColorArgument();
   }

   public DyeColor parse(StringReader reader) throws CommandSyntaxException {
      String s = reader.readUnquotedString();
      DyeColor[] var3 = DyeColor.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         DyeColor c = var3[var5];
         if (c.m_7912_().equals(s)) {
            return c;
         }
      }

      throw net.minecraft.commands.arguments.ColorArgument.f_85459_.create(s);
   }

   public static DyeColor getColor(CommandContext context, String name) {
      return (DyeColor)context.getArgument(name, DyeColor.class);
   }

   public CompletableFuture listSuggestions(CommandContext context, SuggestionsBuilder builder) {
      return SharedSuggestionProvider.m_82981_(Arrays.stream(DyeColor.values()).map(DyeColor::m_7912_), builder);
   }

   public Collection getExamples() {
      return EXAMPLES;
   }
}
