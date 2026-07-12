package lexis.Hack.Hacks.Items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.settings.ItemListSetting;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class AutoDropHack extends Hack implements UpdateListener {
   private final ItemListSetting items;
   private final String renderName = (new Random()).nextDouble() < 0.01 ? "AutoLinus" : null;
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "自动丢垃圾";

   public AutoDropHack() {
      super("自动丢垃圾", "自动丢弃指定的物品", Hack.Category.ITEMS, true);
      List defaultItems = List.of("minecraft:allium", "minecraft:azure_bluet", "minecraft:blue_orchid", "minecraft:cornflower", "minecraft:dandelion", "minecraft:lilac", "minecraft:lily_of_the_valley", "minecraft:orange_tulip", "minecraft:oxeye_daisy", "minecraft:peony", "minecraft:pink_tulip", "minecraft:poisonous_potato", "minecraft:poppy", "minecraft:red_tulip", "minecraft:rose_bush", "minecraft:rotten_flesh", "minecraft:sunflower", "minecraft:wheat_seeds", "minecraft:white_tulip");
      List savedItems = this.loadItemList();
      if (((List)savedItems).isEmpty()) {
         savedItems = new ArrayList(defaultItems);
      }

      this.items = new ItemListSetting("物品", "要自动丢弃的物品列表", (String[])((List)savedItems).toArray(new String[0]));
      this.addSetting(this.items);
      if (((List)savedItems).isEmpty()) {
         this.saveItemList();
      }

   }

   private List loadItemList() {
      Object value = this.config.getHackSettings("自动丢垃圾").get("物品");
      return (List)(value instanceof List ? (List)value : new ArrayList());
   }

   private void saveItemList() {
      List currentItems = this.items.getItemNames();
      this.config.getHackSettings("自动丢垃圾").put("物品", currentItems);
      this.config.saveSettings();
   }

   public String getDisplayName() {
      return this.renderName != null ? this.renderName : super.getDisplayName();
   }

   public void onEnable() {
      List loaded = this.loadItemList();
      if (!loaded.isEmpty() && !loaded.equals(this.items.getItemNames())) {
         this.items.setValue(loaded);
      }

      EventManager.add(UpdateListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.f_91072_ != null) {
         this.saveItemList();
         if (mc.f_91080_ != null) {
            if (mc.f_91080_ instanceof CreativeModeInventoryScreen) {
               return;
            }

            if (!(mc.f_91080_ instanceof AbstractContainerScreen)) {
               return;
            }
         }

         List unwanted = this.items.getItemNames();
         if (!unwanted.isEmpty()) {
            for(int slot = 0; slot < 36; ++slot) {
               ItemStack stack = mc.f_91074_.m_150109_().m_8020_(slot);
               if (!stack.m_41619_()) {
                  String registryName = stack.m_41720_().m_204114_().m_205785_().m_135782_().toString();
                  if (unwanted.contains(registryName)) {
                     this.dropItem(slot);
                     return;
                  }
               }
            }

         }
      }
   }

   private void dropItem(int slot) {
      if (mc.f_91074_ != null && mc.f_91072_ != null && mc.m_91403_() != null) {
         int windowSlot;
         if (slot < 9) {
            windowSlot = 36 + slot;
         } else {
            if (slot < 9 || slot >= 36) {
               return;
            }

            windowSlot = slot;
         }

         int stateId = mc.f_91074_.f_36096_.m_182424_();
         Int2ObjectMap changedSlots = new Int2ObjectOpenHashMap();
         int button = 1;
         mc.m_91403_().m_104955_(new ServerboundContainerClickPacket(mc.f_91074_.f_36096_.f_38840_, stateId, windowSlot, button, ClickType.THROW, ItemStack.f_41583_, changedSlots));
      }
   }

   public void onClick() {
      this.toggle();
   }
}
