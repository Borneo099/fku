package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientEnchantCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";
   private static final SuggestionProvider ENCHANTMENT_SUGGESTIONS = (context, builder) -> {
      List enchantments = (List)BuiltInRegistries.f_256876_.m_123024_().map((e) -> {
         return BuiltInRegistries.f_256876_.m_7981_(e).toString();
      }).collect(Collectors.toList());
      return SharedSuggestionProvider.m_82970_(enchantments, builder);
   };

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      CommandBuildContext buildContext = event.getBuildContext();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("enchant").then(Commands.m_82127_("give").then(Commands.m_82129_("enchantment", ResourceArgument.m_247102_(buildContext, Registries.f_256762_)).suggests(ENCHANTMENT_SUGGESTIONS).then(Commands.m_82129_("level", IntegerArgumentType.integer(0, 127)).executes(ClientEnchantCommand::giveEnchant))))).then(Commands.m_82127_("giveall").then(Commands.m_82129_("level", IntegerArgumentType.integer(0, 127)).executes((ctx) -> {
         return giveAllEnchants(ctx, false);
      })))).then(Commands.m_82127_("giveallnoCurse").then(Commands.m_82129_("level", IntegerArgumentType.integer(0, 127)).executes((ctx) -> {
         return giveAllEnchants(ctx, true);
      })))).then(Commands.m_82127_("allitemsgive").then(Commands.m_82129_("enchantment", ResourceArgument.m_247102_(buildContext, Registries.f_256762_)).suggests(ENCHANTMENT_SUGGESTIONS).then(Commands.m_82129_("level", IntegerArgumentType.integer(0, 127)).executes(ClientEnchantCommand::allItemsGiveEnchant))))).then(Commands.m_82127_("allitemsgivenoCurse").then(Commands.m_82129_("level", IntegerArgumentType.integer(0, 127)).executes((ctx) -> {
         return allItemsGiveAllEnchants(ctx, true);
      }))))));
   }

   private static boolean checkCreativeMode(CommandContext ctx) {
      if (mc.f_91074_ != null && mc.f_91074_.m_150110_().f_35937_) {
         return true;
      } else {
         sendMessage(ctx, "指令仅能在创造模式下使用");
         return false;
      }
   }

   private static void sendMessage(CommandContext ctx, String msg) {
      ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
         return Component.m_237113_("§d[§6Lexis§d] §f" + msg);
      }, false);
   }

   private static void updateSlot(int slot, ItemStack newStack) {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         int networkSlot;
         if (slot < 9) {
            networkSlot = 36 + slot;
         } else if (slot < 36) {
            networkSlot = slot;
         } else {
            if (slot != 40) {
               return;
            }

            networkSlot = 45;
         }

         mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(networkSlot, newStack));
         if (slot < 36) {
            mc.f_91074_.m_150109_().f_35974_.set(slot, newStack);
         } else if (slot == 40) {
            mc.f_91074_.m_150109_().f_35976_.set(0, newStack);
         }

      }
   }

   private static int getCurrentSlot() {
      if (!mc.f_91074_.m_21205_().m_41619_()) {
         return mc.f_91074_.m_150109_().f_35977_;
      } else {
         return !mc.f_91074_.m_21206_().m_41619_() ? 40 : -1;
      }
   }

   private static boolean isCurse(Enchantment e) {
      String name = BuiltInRegistries.f_256876_.m_7981_(e).toString();
      return name.contains("vanishing_curse") || name.contains("binding_curse");
   }

   private static int giveEnchant(CommandContext ctx) throws CommandSyntaxException {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         Holder holder = ResourceArgument.m_245369_(ctx, "enchantment");
         Enchantment enchant = (Enchantment)holder.m_203334_();
         int level = IntegerArgumentType.getInteger(ctx, "level");
         int slot = getCurrentSlot();
         if (slot == -1) {
            sendMessage(ctx, "主手和副手都没有物品");
            return 0;
         } else {
            ItemStack stack = slot == 40 ? mc.f_91074_.m_21206_() : mc.f_91074_.m_21205_();
            ItemStack result = stack.m_41777_();
            result.m_41663_(enchant, level);
            result = Utils.fixGhostItem(result);
            updateSlot(slot, result);
            String pos = slot == 40 ? "副手" : "主手";
            sendMessage(ctx, "已为" + pos + "物品 " + stack.m_41786_().getString() + " 添加附魔 " + enchant.m_44700_(level).getString());
            return 1;
         }
      }
   }

   private static int giveAllEnchants(CommandContext ctx, boolean excludeCurse) {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         int level = IntegerArgumentType.getInteger(ctx, "level");
         int slot = getCurrentSlot();
         if (slot == -1) {
            sendMessage(ctx, "主手和副手都没有物品");
            return 0;
         } else {
            ItemStack stack = slot == 40 ? mc.f_91074_.m_21206_() : mc.f_91074_.m_21205_();
            List valid = (List)BuiltInRegistries.f_256876_.m_123024_().filter((ex) -> {
               return !excludeCurse || !isCurse(ex);
            }).collect(Collectors.toList());
            ItemStack result = stack.m_41777_();
            Iterator var7 = valid.iterator();

            while(var7.hasNext()) {
               Enchantment e = (Enchantment)var7.next();
               result.m_41663_(e, level);
            }

            result = Utils.fixGhostItem(result);
            updateSlot(slot, result);
            String pos = slot == 40 ? "副手" : "主手";
            sendMessage(ctx, "已为" + pos + "物品添加 " + valid.size() + " 个附魔(等级 " + level + ")");
            return 1;
         }
      }
   }

   private static int allItemsGiveEnchant(CommandContext ctx) throws CommandSyntaxException {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         Holder holder = ResourceArgument.m_245369_(ctx, "enchantment");
         Enchantment enchant = (Enchantment)holder.m_203334_();
         int level = IntegerArgumentType.getInteger(ctx, "level");
         int count = 0;

         for(int slot = 0; slot < 36; ++slot) {
            ItemStack stack = mc.f_91074_.m_150109_().m_8020_(slot);
            if (!stack.m_41619_()) {
               ItemStack enchanted = stack.m_41777_();
               enchanted.m_41663_(enchant, level);
               enchanted = Utils.fixGhostItem(enchanted);
               updateSlot(slot, enchanted);
               ++count;
            }
         }

         sendMessage(ctx, "已为 " + count + " 个物品添加附魔 " + enchant.m_44700_(level).getString());
         return 1;
      }
   }

   private static int allItemsGiveAllEnchants(CommandContext ctx, boolean excludeCurse) {
      if (!checkCreativeMode(ctx)) {
         return 0;
      } else {
         int level = IntegerArgumentType.getInteger(ctx, "level");
         List allEnchants = (List)BuiltInRegistries.f_256876_.m_123024_().filter((ex) -> {
            return !excludeCurse || !isCurse(ex);
         }).collect(Collectors.toList());
         int totalItems = 0;

         for(int slot = 0; slot < 36; ++slot) {
            ItemStack stack = mc.f_91074_.m_150109_().m_8020_(slot);
            if (!stack.m_41619_()) {
               ItemStack enchanted = stack.m_41777_();
               Iterator var8 = allEnchants.iterator();

               while(var8.hasNext()) {
                  Enchantment e = (Enchantment)var8.next();
                  enchanted.m_41663_(e, level);
               }

               enchanted = Utils.fixGhostItem(enchanted);
               updateSlot(slot, enchanted);
               ++totalItems;
            }
         }

         sendMessage(ctx, "已为 " + totalItems + " 个物品添加 " + allEnchants.size() + " 个附魔(等级 " + level + ")");
         return 1;
      }
   }
}
