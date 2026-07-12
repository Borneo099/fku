package moze_intel.projecte.network.commands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Objects;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.network.commands.argument.NSSItemArgument;
import moze_intel.projecte.network.commands.parser.NSSItemParser;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SetEmcCMD {
   public static LiteralArgumentBuilder register(CommandBuildContext context) {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("setemc").requires(PEPermissions.COMMAND_SET_EMC)).then(((RequiredArgumentBuilder)Commands.m_82129_("emc", LongArgumentType.longArg(0L, Long.MAX_VALUE)).then(Commands.m_82129_("item", NSSItemArgument.nss(context)).executes((ctx) -> {
         return setEmc(ctx, NSSItemArgument.getNSS(ctx, "item"), LongArgumentType.getLong(ctx, "emc"));
      }))).executes((ctx) -> {
         return setEmc(ctx, RemoveEmcCMD.getHeldStack(ctx), LongArgumentType.getLong(ctx, "emc"));
      }));
   }

   private static int setEmc(CommandContext ctx, NSSItemParser.NSSItemResult stack, long emc) {
      String toSet = stack.getStringRepresentation();
      CustomEMCParser.addToFile(toSet, emc);
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return PELang.COMMAND_SET_SUCCESS.translate(new Object[]{toSet, emc});
      }, true);
      CommandSourceStack var10000 = (CommandSourceStack)ctx.getSource();
      PELang var10001 = PELang.RELOAD_NOTICE;
      Objects.requireNonNull(var10001);
      var10000.m_288197_(() -> {
         return var10001.translate(new Object[0]);
      }, true);
      return 1;
   }
}
