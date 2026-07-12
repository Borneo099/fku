package moze_intel.projecte.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ItemInfoHelper {
   public static Map getEnchantments(ItemInfo info) {
      CompoundTag tag = info.getNBT();
      if (tag == null) {
         return Collections.emptyMap();
      } else {
         String location = getEnchantTagLocation(info);
         if (!tag.m_128425_(location, 9)) {
            return Collections.emptyMap();
         } else {
            Map map = new LinkedHashMap();
            ListTag enchantments = tag.m_128437_(location, 10);

            for(int i = 0; i < enchantments.size(); ++i) {
               CompoundTag enchantNBT = enchantments.m_128728_(i);
               ResourceLocation enchantmentID = ResourceLocation.m_135820_(enchantNBT.m_128461_("id"));
               if (enchantmentID != null) {
                  Enchantment enchantment = (Enchantment)ForgeRegistries.ENCHANTMENTS.getValue(enchantmentID);
                  if (enchantment != null) {
                     map.put(enchantment, enchantNBT.m_128451_("lvl"));
                  }
               }
            }

            return map;
         }
      }
   }

   public static String getEnchantTagLocation(@NotNull ItemInfo info) {
      return info.getItem() == Items.f_42690_ ? "StoredEnchantments" : "Enchantments";
   }

   public static ItemInfo makeWithPotion(ItemInfo info, Potion potion) {
      CompoundTag nbt = info.getNBT();
      if (potion == Potions.f_43598_) {
         if (nbt != null && nbt.m_128441_("Potion")) {
            nbt.m_128473_("Potion");
            if (nbt.m_128456_()) {
               nbt = null;
            }
         }
      } else {
         if (nbt == null) {
            nbt = new CompoundTag();
         }

         nbt.m_128359_("Potion", RegistryUtils.getName(potion).toString());
      }

      return ItemInfo.fromItem(info.getItem(), nbt);
   }
}
