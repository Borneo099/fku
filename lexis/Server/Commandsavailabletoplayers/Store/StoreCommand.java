package lexis.Server.Commandsavailabletoplayers.Store;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import lexis.Server.Commandsavailabletoplayers.Menu.NoMenuCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class StoreCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final StoreConfig CONFIG = StoreConfig.getInstance();
   private static final SuggestionProvider SELLER_ITEM_SUGGESTIONS = (context, builder) -> {
      Entity patt1325$temp = ((CommandSourceStack)context.getSource()).m_81373_();
      if (patt1325$temp instanceof ServerPlayer player) {
         List itemNames = CONFIG.getPlayerItemNames(player.m_20148_());
         return SharedSuggestionProvider.m_82970_(itemNames, builder);
      } else {
         return builder.buildFuture();
      }
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("Lexis").then(((LiteralArgumentBuilder)Commands.m_82127_("Store").then(Commands.m_82127_("Sellitems").then(Commands.m_82129_("价格", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).then(Commands.m_82129_("次数", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).then(Commands.m_82129_("物品名称", StringArgumentType.greedyString()).executes((context) -> {
         Entity patt2528$temp = ((CommandSourceStack)context.getSource()).m_81373_();
         if (patt2528$temp instanceof ServerPlayer player) {
            int price = IntegerArgumentType.getInteger(context, "价格");
            int maxSales = IntegerArgumentType.getInteger(context, "次数");
            String itemName = StringArgumentType.getString(context, "物品名称");
            return sellItem(player, price, maxSales, itemName);
         } else {
            return 0;
         }
      })))))).then(Commands.m_82127_("Del").then(Commands.m_82129_("物品名称", StringArgumentType.greedyString()).suggests(SELLER_ITEM_SUGGESTIONS).executes((context) -> {
         Entity patt3960$temp = ((CommandSourceStack)context.getSource()).m_81373_();
         if (patt3960$temp instanceof ServerPlayer player) {
            String itemName = StringArgumentType.getString(context, "物品名称");
            return deleteItem(player, itemName);
         } else {
            return 0;
         }
      })))));
   }

   private static int sellItem(ServerPlayer player, int price, int maxSales, String itemName) {
      if (NoMenuCommand.isNoMenuEnabled()) {
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c对不起，商店功能 服务器已经禁止了"));
         return 0;
      } else {
         ItemStack handItem = player.m_21205_();
         if (handItem.m_41619_()) {
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你手上没有物品！"));
            return 0;
         } else if (CONFIG.getItems().containsKey(itemName)) {
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c物品名称已存在，请使用其他名称！"));
            return 0;
         } else {
            StoreConfig.StoreItem storeItem = new StoreConfig.StoreItem(itemName, player.m_20148_(), player.m_7755_().getString(), price, maxSales, handItem.m_41777_());
            CONFIG.addItem(itemName, storeItem);
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a成功上架物品: " + itemName));
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§e价格: " + price + " 金币"));
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§e可卖次数: " + maxSales));
            return 1;
         }
      }
   }

   private static int deleteItem(ServerPlayer player, String itemName) {
      if (NoMenuCommand.isNoMenuEnabled()) {
         player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c对不起，商店功能 服务器已经禁止了"));
         return 0;
      } else {
         StoreConfig.StoreItem item = (StoreConfig.StoreItem)CONFIG.getItems().get(itemName);
         if (item == null) {
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c找不到该物品！"));
            return 0;
         } else if (!item.sellerId.equals(player.m_20148_())) {
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你只能删除自己上架的物品！"));
            return 0;
         } else {
            CONFIG.removeItem(itemName);
            player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a已删除物品: " + itemName));
            return 1;
         }
      }
   }

   public static StoreConfig getConfig() {
      return CONFIG;
   }
}
