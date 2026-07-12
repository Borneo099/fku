package moze_intel.projecte.network.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Objects;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.network.commands.argument.NSSItemArgument;
import moze_intel.projecte.network.commands.parser.NSSItemParser;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ResetEmcCMD {
   public static LiteralArgumentBuilder register(CommandBuildContext context) {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("resetemc").requires(PEPermissions.COMMAND_RESET_EMC)).then(Commands.m_82129_("item", NSSItemArgument.nss(context)).executes((ctx) -> {
         return resetEmc(ctx, NSSItemArgument.getNSS(ctx, "item"));
      }))).executes((ctx) -> {
         return resetEmc(ctx, RemoveEmcCMD.getHeldStack(ctx));
      });
   }

   private static int resetEmc(CommandContext ctx, NSSItemParser.NSSItemResult stack) {
      String toReset = stack.getStringRepresentation();
      if (CustomEMCParser.removeFromFile(toReset)) {
         ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
            return PELang.COMMAND_RESET_SUCCESS.translate(new Object[]{toReset});
         }, true);
         CommandSourceStack var10000 = (CommandSourceStack)ctx.getSource();
         PELang var10001 = PELang.RELOAD_NOTICE;
         Objects.requireNonNull(var10001);
         var10000.m_288197_(() -> {
            return var10001.translate(new Object[0]);
         }, true);
         return 1;
      } else {
         throw new CommandRuntimeException(PELang.COMMAND_INVALID_ITEM.translate(new Object[]{toReset}));
      }
   }
}
