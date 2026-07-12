package moze_intel.projecte;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionType;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.Nullable;

public class PEPermissions {
   private static final List NODES_TO_REGISTER = new ArrayList();
   private static final PermissionNode.PermissionResolver PLAYER_IS_OP = (player, uuid, context) -> {
      return player != null && player.m_20310_(2);
   };
   private static final PermissionNode.PermissionResolver ALWAYS_TRUE = (player, uuid, context) -> {
      return true;
   };
   public static final CommandPermissionNode COMMAND;
   public static final CommandPermissionNode COMMAND_REMOVE_EMC;
   public static final CommandPermissionNode COMMAND_RESET_EMC;
   public static final CommandPermissionNode COMMAND_SET_EMC;
   public static final CommandPermissionNode COMMAND_SHOW_BAG;
   public static final CommandPermissionNode COMMAND_EMC;
   public static final CommandPermissionNode COMMAND_EMC_ADD;
   public static final CommandPermissionNode COMMAND_EMC_REMOVE;
   public static final CommandPermissionNode COMMAND_EMC_SET;
   public static final CommandPermissionNode COMMAND_EMC_TEST;
   public static final CommandPermissionNode COMMAND_EMC_GET;
   public static final CommandPermissionNode COMMAND_KNOWLEDGE;
   public static final CommandPermissionNode COMMAND_KNOWLEDGE_CLEAR;
   public static final CommandPermissionNode COMMAND_KNOWLEDGE_LEARN;
   public static final CommandPermissionNode COMMAND_KNOWLEDGE_UNLEARN;
   public static final CommandPermissionNode COMMAND_KNOWLEDGE_TEST;

   private static CommandPermissionNode nodeOpCommand(String nodeName) {
      PermissionNode node = node("command." + nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_OP);
      return new CommandPermissionNode(node, 2);
   }

   private static CommandPermissionNode nodeSubCommand(CommandPermissionNode parent, String nodeName) {
      PermissionNode node = subNode(parent.node, nodeName, ALWAYS_TRUE);
      return new CommandPermissionNode(node, parent.fallbackLevel);
   }

   private static PermissionNode subNode(PermissionNode parent, String nodeName) {
      return subNode(parent, nodeName, (player, uuid, context) -> {
         return getPermission(player, uuid, parent, context);
      });
   }

   private static PermissionNode subNode(PermissionNode parent, String nodeName, ResultTransformer defaultRestrictionIncrease) {
      return subNode(parent, nodeName, (player, uuid, context) -> {
         Object result = getPermission(player, uuid, parent, context);
         return defaultRestrictionIncrease.transform(player, uuid, result, context);
      });
   }

   private static PermissionNode subNode(PermissionNode parent, String nodeName, PermissionNode.PermissionResolver defaultResolver) {
      String fullParentName = parent.getNodeName();
      String parentName = fullParentName.substring(fullParentName.indexOf(46) + 1);
      return node(parentName + "." + nodeName, parent.getType(), defaultResolver);
   }

   @SafeVarargs
   private static PermissionNode node(String nodeName, PermissionType type, PermissionNode.PermissionResolver defaultResolver, PermissionDynamicContextKey... dynamics) {
      PermissionNode node = new PermissionNode("projecte", nodeName, type, defaultResolver, dynamics);
      NODES_TO_REGISTER.add(node);
      return node;
   }

   public static void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
      event.addNodes(NODES_TO_REGISTER);
   }

   private static Object getPermission(@Nullable ServerPlayer player, UUID playerUUID, PermissionNode node, PermissionDynamicContext... context) {
      return player == null ? PermissionAPI.getOfflinePermission(playerUUID, node, context) : PermissionAPI.getPermission(player, node, context);
   }

   static {
      COMMAND = new CommandPermissionNode(node("command", PermissionTypes.BOOLEAN, (player, uuid, contexts) -> {
         return player != null && player.m_20310_(0);
      }), 0);
      COMMAND_REMOVE_EMC = nodeOpCommand("remove_emc");
      COMMAND_RESET_EMC = nodeOpCommand("reset_emc");
      COMMAND_SET_EMC = nodeOpCommand("set_emc");
      COMMAND_SHOW_BAG = nodeOpCommand("show_bag");
      COMMAND_EMC = nodeOpCommand("emc");
      COMMAND_EMC_ADD = nodeSubCommand(COMMAND_EMC, "add");
      COMMAND_EMC_REMOVE = nodeSubCommand(COMMAND_EMC, "remove");
      COMMAND_EMC_SET = nodeSubCommand(COMMAND_EMC, "set");
      COMMAND_EMC_TEST = nodeSubCommand(COMMAND_EMC, "test");
      COMMAND_EMC_GET = nodeSubCommand(COMMAND_EMC, "get");
      COMMAND_KNOWLEDGE = nodeOpCommand("knowledge");
      COMMAND_KNOWLEDGE_CLEAR = nodeSubCommand(COMMAND_KNOWLEDGE, "clear");
      COMMAND_KNOWLEDGE_LEARN = nodeSubCommand(COMMAND_KNOWLEDGE, "learn");
      COMMAND_KNOWLEDGE_UNLEARN = nodeSubCommand(COMMAND_KNOWLEDGE, "unlearn");
      COMMAND_KNOWLEDGE_TEST = nodeSubCommand(COMMAND_KNOWLEDGE, "test");
   }

   public static record CommandPermissionNode(PermissionNode node, int fallbackLevel) implements Predicate {
      public CommandPermissionNode(PermissionNode node, int fallbackLevel) {
         this.node = node;
         this.fallbackLevel = fallbackLevel;
      }

      public boolean test(CommandSourceStack source) {
         boolean var10000;
         if (!source.m_6761_(this.fallbackLevel)) {
            label26: {
               CommandSource var3 = source.f_81288_;
               if (var3 instanceof ServerPlayer) {
                  ServerPlayer player = (ServerPlayer)var3;
                  if ((Boolean)PermissionAPI.getPermission(player, this.node, new PermissionDynamicContext[0])) {
                     break label26;
                  }
               }

               var10000 = false;
               return var10000;
            }
         }

         var10000 = true;
         return var10000;
      }

      public PermissionNode node() {
         return this.node;
      }

      public int fallbackLevel() {
         return this.fallbackLevel;
      }
   }

   @FunctionalInterface
   private interface ResultTransformer {
      Object transform(@Nullable ServerPlayer var1, UUID var2, Object var3, PermissionDynamicContext... var4);
   }
}
