package lexis.Hack.Hackutil.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockListSetting extends Hack.Setting {
   private final List blockNames = new ArrayList();
   private final String[] defaultNames;

   public BlockListSetting(String name, String description, String... defaultBlocks) {
      super(name, description, Hack.Setting.SettingType.BLOCK_LIST);
      String[] var4 = defaultBlocks;
      int var5 = defaultBlocks.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String id = var4[var6];
         Block block = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
         if (block != null) {
            String registryName = ForgeRegistries.BLOCKS.getKey(block).toString();
            if (!this.blockNames.contains(registryName)) {
               this.blockNames.add(registryName);
            }
         }
      }

      Collections.sort(this.blockNames);
      this.defaultNames = (String[])this.blockNames.toArray(new String[0]);
   }

   public List getBlockNames() {
      return Collections.unmodifiableList(this.blockNames);
   }

   public void add(Block block) {
      String name = ForgeRegistries.BLOCKS.getKey(block).toString();
      if (!this.blockNames.contains(name)) {
         this.blockNames.add(name);
         Collections.sort(this.blockNames);
         if (this.getHack() != null && this.getHack().isEnabled()) {
            this.getHack().autoSave();
         }
      }

   }

   public void remove(int index) {
      if (index >= 0 && index < this.blockNames.size()) {
         this.blockNames.remove(index);
         if (this.getHack() != null && this.getHack().isEnabled()) {
            this.getHack().autoSave();
         }
      }

   }

   public void resetToDefaults() {
      this.blockNames.clear();
      String[] var1 = this.defaultNames;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String name = var1[var3];
         this.blockNames.add(name);
      }

      Collections.sort(this.blockNames);
      if (this.getHack() != null && this.getHack().isEnabled()) {
         this.getHack().autoSave();
      }

   }

   public Object getValue() {
      return new ArrayList(this.blockNames);
   }

   public void setValue(Object value) {
      if (value instanceof List) {
         this.blockNames.clear();
         Iterator var2 = ((List)value).iterator();

         while(var2.hasNext()) {
            Object obj = var2.next();
            if (obj instanceof String) {
               this.blockNames.add((String)obj);
            }
         }

         Collections.sort(this.blockNames);
      }

   }
}
