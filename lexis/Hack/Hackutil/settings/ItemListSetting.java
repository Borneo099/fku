package lexis.Hack.Hackutil.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemListSetting extends Hack.Setting {
   private final List itemNames = new ArrayList();
   private final String[] defaultNames;

   public ItemListSetting(String name, String description, String... defaultItems) {
      super(name, description, Hack.Setting.SettingType.ITEM_LIST);
      String[] var4 = defaultItems;
      int var5 = defaultItems.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String id = var4[var6];
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
         if (item != null) {
            String registryName = ForgeRegistries.ITEMS.getKey(item).toString();
            if (!this.itemNames.contains(registryName)) {
               this.itemNames.add(registryName);
            }
         }
      }

      Collections.sort(this.itemNames);
      this.defaultNames = (String[])this.itemNames.toArray(new String[0]);
   }

   public List getItemNames() {
      return Collections.unmodifiableList(this.itemNames);
   }

   public void add(Item item) {
      String name = ForgeRegistries.ITEMS.getKey(item).toString();
      if (!this.itemNames.contains(name)) {
         this.itemNames.add(name);
         Collections.sort(this.itemNames);
         if (this.getHack() != null && this.getHack().isEnabled()) {
            this.getHack().autoSave();
         }
      }

   }

   public void remove(int index) {
      if (index >= 0 && index < this.itemNames.size()) {
         this.itemNames.remove(index);
         if (this.getHack() != null && this.getHack().isEnabled()) {
            this.getHack().autoSave();
         }
      }

   }

   public void resetToDefaults() {
      this.itemNames.clear();
      String[] var1 = this.defaultNames;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String name = var1[var3];
         this.itemNames.add(name);
      }

      Collections.sort(this.itemNames);
      if (this.getHack() != null && this.getHack().isEnabled()) {
         this.getHack().autoSave();
      }

   }

   public Object getValue() {
      return new ArrayList(this.itemNames);
   }

   public void setValue(Object value) {
      if (value instanceof List) {
         this.itemNames.clear();
         Iterator var2 = ((List)value).iterator();

         while(var2.hasNext()) {
            Object obj = var2.next();
            if (obj instanceof String) {
               this.itemNames.add((String)obj);
            }
         }

         Collections.sort(this.itemNames);
      }

   }
}
