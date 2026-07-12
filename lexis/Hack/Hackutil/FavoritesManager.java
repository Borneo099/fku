package lexis.Hack.Hackutil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FavoritesManager {
   private static FavoritesManager instance;
   private final Set favorites = new HashSet();
   private final List listeners = new ArrayList();
   private static final File CONFIG_DIR = new File("C:/karucn/Lexis/config/hack/");
   private static final File CONFIG_FILE;
   private static final Gson GSON;

   public static FavoritesManager getInstance() {
      if (instance == null) {
         instance = new FavoritesManager();
         instance.load();
      }

      return instance;
   }

   public void addListener(Runnable listener) {
      this.listeners.add(listener);
   }

   private void notifyListeners() {
      Iterator var1 = this.listeners.iterator();

      while(var1.hasNext()) {
         Runnable listener = (Runnable)var1.next();
         listener.run();
      }

   }

   public void addFavorite(String hackName) {
      this.favorites.add(hackName);
      this.save();
      this.notifyListeners();
   }

   public void removeFavorite(String hackName) {
      this.favorites.remove(hackName);
      this.save();
      this.notifyListeners();
   }

   public boolean isFavorite(String hackName) {
      return this.favorites.contains(hackName);
   }

   public Set getFavorites() {
      return new HashSet(this.favorites);
   }

   public void toggleFavorite(String hackName) {
      if (this.favorites.contains(hackName)) {
         this.favorites.remove(hackName);
      } else {
         this.favorites.add(hackName);
      }

      this.save();
      this.notifyListeners();
   }

   private void load() {
      try {
         if (!CONFIG_FILE.exists()) {
            this.save();
            return;
         }

         FileReader reader = new FileReader(CONFIG_FILE);
         FavoriteData data = (FavoriteData)GSON.fromJson(reader, FavoriteData.class);
         reader.close();
         if (data != null && data.favorites != null) {
            this.favorites.clear();
            this.favorites.addAll(data.favorites);
         } else {
            this.save();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         this.save();
      }

   }

   private void save() {
      try {
         CONFIG_DIR.mkdirs();
         FavoriteData data = new FavoriteData();
         data.favorites = new ArrayList(this.favorites);
         FileWriter writer = new FileWriter(CONFIG_FILE);
         GSON.toJson(data, writer);
         writer.close();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   static {
      CONFIG_FILE = new File(CONFIG_DIR, "favorites.json");
      GSON = (new GsonBuilder()).setPrettyPrinting().create();
   }

   private static class FavoriteData {
      List favorites;
   }
}
