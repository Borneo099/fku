package lexis.Hack.Hacks.Combat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.PacketOutputListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class AutoArmorHack extends Hack implements UpdateListener, PacketOutputListener {
   private HackConfig config;
   private static final String CONFIG_KEY = "自动盔甲";
   private boolean useEnchantments = true;
   private boolean swapWhileMoving = false;
   private int delay = 2;
   private int timer = 0;

   public AutoArmorHack() {
      super("自动盔甲", "自动穿上最好的盔甲", Hack.Category.COMBAT, true);
      this.addSetting(new Hack.Setting("使用附魔", "计算护甲强度时是否考虑保护附魔", true));
      this.addSetting(new Hack.Setting("移动时交换", "玩家移动时是否交换护甲部件 (可能引起怀疑)", false));
      this.addSetting(new Hack.Setting("延迟", "交换下一件护甲之前等待的刻数", 2.0, 0.0, 20.0, Hack.ValueDisplay.INTEGER));
      this.config = HackConfig.getInstance();
      this.loadConfig();
   }

   private void loadConfig() {
      this.useEnchantments = this.config.getBooleanSetting("自动盔甲", "使用附魔", true);
      this.swapWhileMoving = this.config.getBooleanSetting("自动盔甲", "移动时交换", false);
      this.delay = (int)this.config.getDoubleSetting("自动盔甲", "延迟", 2.0);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "使用附魔":
               setting.setValue(this.useEnchantments);
               break;
            case "移动时交换":
               setting.setValue(this.swapWhileMoving);
               break;
            case "延迟":
               setting.setValue((double)this.delay);
         }
      }

   }

   public void onEnable() {
      this.timer = 0;
      EventManager.add(UpdateListener.class, this);
      EventManager.add(PacketOutputListener.class, this);
   }

   public void onDisable() {
      EventManager.remove(UpdateListener.class, this);
      EventManager.remove(PacketOutputListener.class, this);
   }

   public void onUpdate() {
      boolean needSave = false;
      Iterator var2 = this.getSettings().iterator();

      int slot;
      while(var2.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var2.next();
         switch (setting.getName()) {
            case "使用附魔":
               if (setting.getBoolean() != this.useEnchantments) {
                  this.useEnchantments = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "移动时交换":
               if (setting.getBoolean() != this.swapWhileMoving) {
                  this.swapWhileMoving = setting.getBoolean();
                  needSave = true;
               }
               break;
            case "延迟":
               int newDelay = (int)setting.getDouble();
               if (newDelay != this.delay) {
                  this.delay = newDelay;
                  needSave = true;
               }
         }
      }

      if (needSave) {
         this.config.saveHackSettings("自动盔甲", this.getSettings());
      }

      if (this.timer > 0) {
         --this.timer;
      } else if (mc.f_91080_ == null || mc.f_91080_ instanceof InventoryScreen) {
         LocalPlayer player = mc.f_91074_;
         if (player != null) {
            if (this.swapWhileMoving || player.f_20900_ == 0.0F && player.f_20902_ == 0.0F) {
               int[] bestArmorSlots = new int[4];
               int[] bestArmorValues = new int[4];
               Arrays.fill(bestArmorSlots, -1);

               ArmorItem item;
               ItemStack stack;
               for(slot = 0; slot < 4; ++slot) {
                  stack = (ItemStack)player.m_150109_().f_35975_.get(slot);
                  if (!stack.m_41619_() && stack.m_41720_() instanceof ArmorItem) {
                     item = (ArmorItem)stack.m_41720_();
                     bestArmorValues[slot] = this.getArmorValue(item, stack);
                  }
               }

               int windowSlot;
               for(slot = 0; slot < 36; ++slot) {
                  stack = (ItemStack)player.m_150109_().f_35974_.get(slot);
                  if (!stack.m_41619_() && stack.m_41720_() instanceof ArmorItem) {
                     item = (ArmorItem)stack.m_41720_();
                     EquipmentSlot equipmentSlot = item.m_40402_();
                     int armorType = this.getArmorTypeIndex(equipmentSlot);
                     windowSlot = this.getArmorValue(item, stack);
                     if (windowSlot > bestArmorValues[armorType]) {
                        bestArmorSlots[armorType] = slot;
                        bestArmorValues[armorType] = windowSlot;
                     }
                  }
               }

               List types = Arrays.asList(0, 1, 2, 3);
               Collections.shuffle(types);
               Iterator var15 = types.iterator();

               while(var15.hasNext()) {
                  int type = (Integer)var15.next();
                  int slot = bestArmorSlots[type];
                  if (slot != -1) {
                     ItemStack oldArmor = (ItemStack)player.m_150109_().f_35975_.get(type);
                     if (oldArmor.m_41619_() || player.m_150109_().m_36062_() != -1) {
                        if (slot < 9) {
                           windowSlot = slot + 36;
                        } else {
                           windowSlot = slot;
                        }

                        if (!oldArmor.m_41619_()) {
                           this.quickMove(8 - type);
                        }

                        this.quickMove(windowSlot);
                        break;
                     }
                  }
               }

            }
         }
      }
   }

   public void onPacketOutput(PacketOutputListener.PacketOutputEvent event) {
      Packet packet = event.getPacket();
      if (packet instanceof ServerboundContainerClickPacket) {
         this.timer = this.delay;
      }

   }

   private int getArmorValue(ArmorItem item, ItemStack stack) {
      int armorPoints = item.m_40404_();
      int enchantPoints = 0;
      float toughness = item.m_40405_();
      ArmorMaterial material = item.m_40401_();
      int armorTypeValue = 0;
      if (material == ArmorMaterials.LEATHER) {
         armorTypeValue = 1;
      } else if (material == ArmorMaterials.CHAIN) {
         armorTypeValue = 2;
      } else if (material == ArmorMaterials.IRON) {
         armorTypeValue = 3;
      } else if (material == ArmorMaterials.GOLD) {
         armorTypeValue = 2;
      } else if (material == ArmorMaterials.DIAMOND) {
         armorTypeValue = 4;
      } else if (material == ArmorMaterials.NETHERITE) {
         armorTypeValue = 5;
      }

      if (this.useEnchantments) {
         int protLevel = EnchantmentHelper.m_44843_(Enchantments.f_44965_, stack);
         if (protLevel > 0) {
            enchantPoints = protLevel * 2;
         }
      }

      return armorPoints * 5 + enchantPoints * 3 + (int)(toughness * 2.0F) + armorTypeValue;
   }

   private int getArmorTypeIndex(EquipmentSlot slot) {
      switch (slot) {
         case HEAD:
            return 3;
         case CHEST:
            return 2;
         case LEGS:
            return 1;
         case FEET:
            return 0;
         default:
            return -1;
      }
   }

   private void quickMove(int slotIndex) {
      if (mc.f_91072_ != null && mc.f_91074_ != null) {
         mc.f_91072_.m_171799_(mc.f_91074_.f_36096_.f_38840_, slotIndex, 0, ClickType.QUICK_MOVE, mc.f_91074_);
      }
   }

   public void onClick() {
      this.toggle();
   }
}
