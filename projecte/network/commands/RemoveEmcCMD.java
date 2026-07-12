package moze_intel.projecte.network.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Objects;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.network.commands.argument.NSSItemArgument;
import moze_intel.projecte.network.commands.parser.NSSItemParser;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RemoveEmcCMD {
   private static final SimpleCommandExceptionType EMPTY_STACK;

   public static LiteralArgumentBuilder register(CommandBuildContext context) {
      return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("removeemc").requires(PEPermissions.COMMAND_REMOVE_EMC)).then(Commands.m_82129_("item", NSSItemArgument.nss(context)).executes((ctx) -> {
         return removeEmc(ctx, NSSItemArgument.getNSS(ctx, "item"));
      }))).executes((ctx) -> {
         return removeEmc(ctx, getHeldStack(ctx));
      });
   }

   private static int removeEmc(CommandContext ctx, NSSItemParser.NSSItemResult stack) {
      String toRemove = stack.getStringRepresentation();
      CustomEMCParser.addToFile(toRemove, 0L);
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return PELang.COMMAND_REMOVE_SUCCESS.translate(new Object[]{toRemove});
      }, true);
      CommandSourceStack var10000 = (CommandSourceStack)ctx.getSource();
      PELang var10001 = PELang.RELOAD_NOTICE;
      Objects.requireNonNull(var10001);
      var10000.m_288197_(() -> {
         return var10001.translate(new Object[0]);
      }, true);
      return 1;
   }

   public static NSSItemParser.NSSItemResult getHeldStack(CommandContext ctx) throws CommandSyntaxException {
      ServerPlayer player = ((CommandSourceStack)ctx.getSource()).m_81375_();
      ItemStack stack = player.m_21205_();
      if (stack.m_41619_()) {
         stack = player.m_21206_();
      }

      if (stack.m_41619_()) {
         throw EMPTY_STACK.create();
      } else {
         return NSSItemParser.resultOf(stack);
      }
   }

   static {
      EMPTY_STACK = new SimpleCommandExceptionType(PELang.COMMAND_NO_ITEM.translate(new Object[0]));
   }
}
