package moze_intel.projecte.network.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.Optional;
import moze_intel.projecte.PEPermissions;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeClearPKT;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class KnowledgeCMD {
   public static ArgumentBuilder register(CommandBuildContext context) {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("knowledge").requires(PEPermissions.COMMAND_KNOWLEDGE)).then(subCommandClear())).then(((LiteralArgumentBuilder)Commands.m_82127_("learn").requires(PEPermissions.COMMAND_KNOWLEDGE_LEARN)).then(executeWithParameters(KnowledgeCMD.ActionType.LEARN, context)))).then(((LiteralArgumentBuilder)Commands.m_82127_("unlearn").requires(PEPermissions.COMMAND_KNOWLEDGE_UNLEARN)).then(executeWithParameters(KnowledgeCMD.ActionType.UNLEARN, context)))).then(((LiteralArgumentBuilder)Commands.m_82127_("test").requires(PEPermissions.COMMAND_KNOWLEDGE_TEST)).then(executeWithParameters(KnowledgeCMD.ActionType.TEST, context)));
   }

   private static ArgumentBuilder executeWithParameters(ActionType actionType, CommandBuildContext context) {
      return Commands.m_82129_("player", EntityArgument.m_91466_()).then(Commands.m_82129_("item", ItemArgument.m_235279_(context)).executes((ctx) -> {
         return handle(ctx, actionType);
      }));
   }

   private static @Nullable IKnowledgeProvider getProvider(ServerPlayer player) {
      Optional cap = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).resolve();
      return (IKnowledgeProvider)cap.orElse((Object)null);
   }

   private static ArgumentBuilder subCommandClear() {
      return ((LiteralArgumentBuilder)Commands.m_82127_("clear").requires(PEPermissions.COMMAND_KNOWLEDGE_CLEAR)).then(Commands.m_82129_("targets", EntityArgument.m_91470_()).executes((ctx) -> {
         CommandSourceStack source = (CommandSourceStack)ctx.getSource();
         int successCount = 0;
         Iterator var3 = EntityArgument.m_91477_(ctx, "targets").iterator();

         while(var3.hasNext()) {
            ServerPlayer player = (ServerPlayer)var3.next();
            IKnowledgeProvider provider = getProvider(player);
            if (provider == null) {
               source.m_81352_(PELang.COMMAND_PROVIDER_FAIL.translate(new Object[]{player.m_5446_()}));
            } else if (provider.getKnowledge().isEmpty()) {
               source.m_81352_(PELang.COMMAND_KNOWLEDGE_CLEAR_FAIL.translate(new Object[]{player.m_5446_()}));
            } else {
               provider.clearKnowledge();
               PacketHandler.sendTo(new KnowledgeClearPKT(), player);
               source.m_288197_(() -> {
                  return PELang.COMMAND_KNOWLEDGE_CLEAR_SUCCESS.translateColored(ChatFormatting.GREEN, new Object[]{player.m_5446_()});
               }, true);
               ++successCount;
            }
         }

         return successCount;
      }));
   }

   private static int handle(CommandContext ctx, ActionType action) throws CommandSyntaxException {
      ServerPlayer player = EntityArgument.m_91474_(ctx, "player");
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      IKnowledgeProvider provider = getProvider(player);
      if (provider == null) {
         source.m_81352_(PELang.COMMAND_PROVIDER_FAIL.translate(new Object[]{player.m_5446_()}));
         return 0;
      } else {
         ItemStack item = new ItemStack(ItemArgument.m_120963_(ctx, "item").m_120979_());
         if (!EMCHelper.doesItemHaveEmc(item)) {
            source.m_81352_(PELang.COMMAND_KNOWLEDGE_INVALID.translate(new Object[]{item.m_41611_()}));
            return 0;
         } else {
            switch (action) {
               case LEARN:
                  if (provider.hasKnowledge(item)) {
                     return failure(source, PELang.COMMAND_KNOWLEDGE_LEARN_FAIL, player, item);
                  }

                  provider.addKnowledge(item);
                  source.m_288197_(() -> {
                     return PELang.COMMAND_KNOWLEDGE_LEARN_SUCCESS.translateColored(ChatFormatting.GREEN, new Object[]{player.m_5446_(), item.m_41611_()});
                  }, true);
                  break;
               case UNLEARN:
                  if (!provider.hasKnowledge(item)) {
                     return failure(source, PELang.COMMAND_KNOWLEDGE_UNLEARN_FAIL, player, item);
                  }

                  provider.removeKnowledge(item);
                  source.m_288197_(() -> {
                     return PELang.COMMAND_KNOWLEDGE_UNLEARN_SUCCESS.translateColored(ChatFormatting.GREEN, new Object[]{player.m_5446_(), item.m_41611_()});
                  }, true);
                  break;
               case TEST:
                  if (provider.hasKnowledge(item)) {
                     source.m_288197_(() -> {
                        return PELang.COMMAND_KNOWLEDGE_TEST_SUCCESS.translateColored(ChatFormatting.GREEN, new Object[]{player.m_5446_(), item.m_41611_()});
                     }, true);
                     return 1;
                  }

                  return failure(source, PELang.COMMAND_KNOWLEDGE_TEST_FAIL, player, item);
            }

            provider.syncKnowledgeChange(player, NBTManager.getPersistentInfo(ItemInfo.fromStack(item)), action == KnowledgeCMD.ActionType.LEARN);
            return 1;
         }
      }
   }

   private static int failure(CommandSourceStack source, ILangEntry failureMessage, Player player, ItemStack item) {
      source.m_81352_(failureMessage.translate(player.m_5446_(), item.m_41611_()));
      return 0;
   }

   private static enum ActionType {
      LEARN,
      UNLEARN,
      TEST;

      // $FF: synthetic method
      private static ActionType[] $values() {
         return new ActionType[]{LEARN, UNLEARN, TEST};
      }
   }
}
