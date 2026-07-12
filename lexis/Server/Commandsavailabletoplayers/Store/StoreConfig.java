package lexis.Server.Commandsavailabletoplayers.Store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lexis.Hack.Hackutil.config.ConfigUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public class StoreConfig {
   private static final String CONFIG_PATH = "C:/karucn/Lexis/Server/Store/config.json";
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static StoreConfig instance;
   private Map items = new ConcurrentHashMap();
   private Map playerMoney = new ConcurrentHashMap();
   private Map playerOnlineTime = new ConcurrentHashMap();

   public static StoreConfig getInstance() {
      if (instance == null) {
         instance = load();
      }

      return instance;
   }

   public Map getItems() {
      return this.items;
   }

   public Map getPlayerMoney() {
      return this.playerMoney;
   }

   public Map getPlayerOnlineTime() {
      return this.playerOnlineTime;
   }

   public long getPlayerOnlineTime(UUID playerId) {
      return (Long)this.playerOnlineTime.getOrDefault(playerId, 0L);
   }

   public void addPlayerOnlineTime(UUID playerId, long seconds) {
      this.playerOnlineTime.put(playerId, this.getPlayerOnlineTime(playerId) + seconds);
      this.save();
   }

   public int getPlayerMoney(UUID playerId) {
      return (Integer)this.playerMoney.getOrDefault(playerId, 0);
   }

   public void addPlayerMoney(UUID playerId, int amount) {
      this.playerMoney.put(playerId, this.getPlayerMoney(playerId) + amount);
      this.save();
   }

   public void removePlayerMoney(UUID playerId, int amount) {
      int current = this.getPlayerMoney(playerId);
      if (current >= amount) {
         this.playerMoney.put(playerId, current - amount);
         this.save();
      }

   }

   public void addItem(String name, StoreItem item) {
      this.items.put(name, item);
      this.save();
   }

   public void removeItem(String name) {
      this.items.remove(name);
      this.save();
   }

   public List getPlayerItemNames(UUID sellerId) {
      List names = new ArrayList();
      Iterator var3 = this.items.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry entry = (Map.Entry)var3.next();
         if (((StoreItem)entry.getValue()).sellerId.equals(sellerId)) {
            names.add((String)entry.getKey());
         }
      }

      return names;
   }

   private static StoreConfig load() {
      File configFile = new File("C:/karucn/Lexis/Server/Store/config.json");
      StoreConfig loaded = (StoreConfig)ConfigUtils.readConfig(configFile, StoreConfig.class);
      if (loaded != null) {
         return loaded;
      } else {
         StoreConfig defaultConfig = new StoreConfig();
         defaultConfig.save();
         return defaultConfig;
      }
   }

   public void save() {
      File configFile = new File("C:/karucn/Lexis/Server/Store/config.json");
      ConfigUtils.saveConfig(configFile, this);
   }

   public static class StoreItem {
      public String itemName;
      public UUID sellerId;
      public String sellerName;
      public int price;
      public int maxSales;
      public int currentSales;
      public String itemNbt;

      public StoreItem(String itemName, UUID sellerId, String sellerName, int price, int maxSales, ItemStack stack) {
         this.itemName = itemName;
         this.sellerId = sellerId;
         this.sellerName = sellerName;
         this.price = price;
         this.maxSales = maxSales;
         this.currentSales = 0;
         CompoundTag tag = new CompoundTag();
         stack.m_41739_(tag);
         this.itemNbt = tag.toString();
      }

      public boolean canBuy() {
         return this.currentSales < this.maxSales;
      }

      public void buy() {
         if (this.canBuy()) {
            ++this.currentSales;
         }

      }

      public ItemStack toItemStack() {
         try {
            CompoundTag tag = TagParser.m_129359_(this.itemNbt);
            return ItemStack.m_41712_(tag);
         } catch (Exception var2) {
            return ItemStack.f_41583_;
         }
      }
   }
}
