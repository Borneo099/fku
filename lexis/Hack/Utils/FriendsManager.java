package lexis.Hack.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.player.Player;

public class FriendsManager {
   private static final File FRIENDS_FILE = new File("C:/karucn/Lexis/config/hack/friends.json");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static FriendsManager instance;
   private final Set friends = new HashSet();

   private FriendsManager() {
      this.load();
   }

   public static FriendsManager getInstance() {
      if (instance == null) {
         instance = new FriendsManager();
      }

      return instance;
   }

   public boolean isFriend(Player player) {
      return player == null ? false : this.isFriend(player.m_7755_().getString());
   }

   public boolean isFriend(String playerName) {
      return this.findFriendCaseInsensitive(playerName) != null;
   }

   public void addFriend(Player player) {
      if (player != null) {
         this.addFriend(player.m_7755_().getString());
      }
   }

   public void addFriend(String name) {
      this.friends.add(name);
      this.save();
   }

   public void removeFriend(Player player) {
      if (player != null) {
         this.removeFriend(player.m_7755_().getString());
      }
   }

   public void removeFriend(String name) {
      String toRemove = this.findFriendCaseInsensitive(name);
      if (toRemove != null) {
         this.friends.remove(toRemove);
         this.save();
      }

   }

   public List getFriendNames() {
      List list = new ArrayList(this.friends);
      Collections.sort(list);
      return list;
   }

   private String findFriendCaseInsensitive(String name) {
      Iterator var2 = this.friends.iterator();

      String friend;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         friend = (String)var2.next();
      } while(!friend.equalsIgnoreCase(name));

      return friend;
   }

   private void load() {
      if (FRIENDS_FILE.exists()) {
         try {
            FileReader reader = new FileReader(FRIENDS_FILE);

            try {
               Type type = (new TypeToken() {
               }).getType();
               Set loaded = (Set)GSON.fromJson(reader, type);
               if (loaded != null) {
                  this.friends.clear();
                  this.friends.addAll(loaded);
               }
            } catch (Throwable var5) {
               try {
                  reader.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }

               throw var5;
            }

            reader.close();
         } catch (Exception var6) {
            var6.printStackTrace();
         }

      }
   }

   private void save() {
      FRIENDS_FILE.getParentFile().mkdirs();

      try {
         FileWriter writer = new FileWriter(FRIENDS_FILE);

         try {
            GSON.toJson(this.friends, writer);
         } catch (Throwable var5) {
            try {
               writer.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         writer.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }
}
