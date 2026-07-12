package moze_intel.projecte.network.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.math.BigInteger;
import java.util.Optional;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class EMCCMD {
   public static ArgumentBuilder register(CommandBuildContext context) {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("emc").requires(PEPermissions.COMMAND_EMC)).then(((LiteralArgumentBuilder)Commands.m_82127_("add").requires(PEPermissions.COMMAND_EMC_ADD)).then(executeWithParameters(EMCCMD.ActionType.ADD)))).then(((LiteralArgumentBuilder)Commands.m_82127_("remove").requires(PEPermissions.COMMAND_EMC_REMOVE)).then(executeWithParameters(EMCCMD.ActionType.REMOVE)))).then(((LiteralArgumentBuilder)Commands.m_82127_("set").requires(PEPermissions.COMMAND_EMC_SET)).then(executeWithParameters(EMCCMD.ActionType.SET)))).then(((LiteralArgumentBuilder)Commands.m_82127_("test").requires(PEPermissions.COMMAND_EMC_TEST)).then(executeWithParameters(EMCCMD.ActionType.TEST)))).then(((LiteralArgumentBuilder)Commands.m_82127_("get").requires(PEPermissions.COMMAND_EMC_GET)).then(Commands.m_82129_("player", EntityArgument.m_91466_()).executes((ctx) -> {
         return handle(ctx, EMCCMD.ActionType.GET);
      })));
   }

   private static ArgumentBuilder executeWithParameters(ActionType actionType) {
      return Commands.m_82129_("player", EntityArgument.m_91466_()).then(Commands.m_82129_("value", StringArgumentType.string()).executes((ctx) -> {
         return handle(ctx, actionType);
      }));
   }

   private static MutableComponent formatEMC(BigInteger emc) {
      return TextComponentUtil.build(ChatFormatting.GRAY, TransmutationEMCFormatter.formatEMC(emc));
   }

   private static int handle(CommandContext ctx, ActionType action) throws CommandSyntaxException {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      ServerPlayer player = EntityArgument.m_91474_(ctx, "player");
      Optional cap = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).resolve();
      if (cap.isEmpty()) {
         source.m_81352_(PELang.COMMAND_PROVIDER_FAIL.translate(new Object[]{player.m_5446_()}));
         return 0;
      } else {
         IKnowledgeProvider provider = (IKnowledgeProvider)cap.get();
         if (action == EMCCMD.ActionType.GET) {
            source.m_288197_(() -> {
               return PELang.COMMAND_EMC_GET_SUCCESS.translate(new Object[]{player.m_5446_(), formatEMC(provider.getEmc())});
            }, true);
            return 1;
         } else {
            String val = StringArgumentType.getString(ctx, "value");
            BigInteger value = null;

            try {
               value = new BigInteger(val);
               if (value.compareTo(BigInteger.ZERO) < 0) {
                  switch (action) {
                     case ADD:
                     case REMOVE:
                        action = action == EMCCMD.ActionType.ADD ? EMCCMD.ActionType.REMOVE : EMCCMD.ActionType.ADD;
                        value = value.abs();
                        break;
                     case SET:
                     case TEST:
                        value = null;
                  }
               }
            } catch (NumberFormatException var10) {
            }

            if (value == null) {
               source.m_81352_(PELang.COMMAND_EMC_INVALID.translate(new Object[]{val}));
               return 0;
            } else {
               BigInteger newEMC = provider.getEmc();
               MutableComponent message;
               switch (action) {
                  case ADD:
                     newEMC = newEMC.add(value);
                     message = PELang.COMMAND_EMC_ADD_SUCCESS.translate(new Object[]{formatEMC(value), player.m_5446_(), formatEMC(newEMC)});
                     source.m_288197_(() -> {
                        return message;
                     }, true);
                     break;
                  case REMOVE:
                     newEMC = newEMC.subtract(value);
                     if (newEMC.compareTo(BigInteger.ZERO) < 0) {
                        source.m_81352_(PELang.COMMAND_EMC_NEGATIVE.translate(new Object[]{formatEMC(value), player.m_5446_()}));
                        return 0;
                     }

                     message = PELang.COMMAND_EMC_REMOVE_SUCCESS.translate(new Object[]{formatEMC(value), player.m_5446_(), formatEMC(newEMC)});
                     source.m_288197_(() -> {
                        return message;
                     }, true);
                     break;
                  case SET:
                     newEMC = value;
                     message = PELang.COMMAND_EMC_SET_SUCCESS.translate(new Object[]{player.m_5446_(), formatEMC(value)});
                     source.m_288197_(() -> {
                        return message;
                     }, true);
                     break;
                  case TEST:
                     if (newEMC.compareTo(value) >= 0) {
                        message = PELang.COMMAND_EMC_TEST_SUCCESS.translateColored(ChatFormatting.GREEN, new Object[]{player.m_5446_(), formatEMC(value)});
                        source.m_288197_(() -> {
                           return message;
                        }, true);
                        return 1;
                     }

                     source.m_81352_(PELang.COMMAND_EMC_TEST_FAIL.translate(new Object[]{player.m_5446_(), formatEMC(value)}));
                     return 0;
               }

               provider.setEmc(newEMC);
               provider.syncEmc(player);
               return 1;
            }
         }
      }
   }

   private static enum ActionType {
      ADD,
      REMOVE,
      SET,
      GET,
      TEST;

      // $FF: synthetic method
      private static ActionType[] $values() {
         return new ActionType[]{ADD, REMOVE, SET, GET, TEST};
      }
   }
}
