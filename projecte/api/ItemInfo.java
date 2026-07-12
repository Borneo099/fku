package moze_intel.projecte.api;

import java.util.Objects;
import moze_intel.projecte.api.nss.NSSItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemInfo {
   private final @NotNull Item item;
   private final @Nullable CompoundTag nbt;

   private ItemInfo(@NotNull ItemLike item, @Nullable CompoundTag nbt) {
      this.item = item.m_5456_();
      this.nbt = nbt != null && nbt.m_128456_() ? null : nbt;
   }

   public static ItemInfo fromItem(@NotNull ItemLike item, @Nullable CompoundTag nbt) {
      return new ItemInfo(item, nbt);
   }

   public static ItemInfo fromItem(@NotNull ItemLike item) {
      return fromItem(item, (CompoundTag)null);
   }

   public static ItemInfo fromStack(@NotNull ItemStack stack) {
      return fromItem(stack.m_41720_(), stack.m_41783_());
   }

   public static @Nullable ItemInfo fromNSS(@NotNull NSSItem stack) {
      if (stack.representsTag()) {
         return null;
      } else {
         Item item = (Item)ForgeRegistries.ITEMS.getValue(stack.getResourceLocation());
         return item == null ? null : fromItem(item, stack.getNBT());
      }
   }

   public static @Nullable ItemInfo read(@NotNull CompoundTag nbt) {
      if (nbt.m_128425_("item", 8)) {
         ResourceLocation registryName = ResourceLocation.m_135820_(nbt.m_128461_("item"));
         if (registryName == null) {
            return null;
         } else {
            Item item = (Item)ForgeRegistries.ITEMS.getValue(registryName);
            if (item == null) {
               return null;
            } else {
               return nbt.m_128425_("nbt", 10) ? fromItem(item, nbt.m_128469_("nbt")) : fromItem(item, (CompoundTag)null);
            }
         }
      } else {
         return null;
      }
   }

   public @NotNull Item getItem() {
      return this.item;
   }

   public @Nullable CompoundTag getNBT() {
      return this.nbt == null ? null : this.nbt.m_6426_();
   }

   public boolean hasNBT() {
      return this.nbt != null;
   }

   public boolean is(TagKey tag) {
      return this.getItem().m_204114_().m_203656_(tag);
   }

   public ItemStack createStack() {
      ItemStack stack = new ItemStack(this.item);
      CompoundTag nbt = this.getNBT();
      if (nbt != null) {
         stack.m_41751_(nbt);
      }

      return stack;
   }

   public CompoundTag write(@NotNull CompoundTag nbt) {
      nbt.m_128359_("item", this.getRegistryName().toString());
      if (this.nbt != null) {
         nbt.m_128365_("nbt", this.nbt);
      }

      return nbt;
   }

   public int hashCode() {
      int code = this.item.hashCode();
      if (this.nbt != null) {
         code = 31 * code + this.nbt.hashCode();
      }

      return code;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ItemInfo)) {
         return false;
      } else {
         ItemInfo other = (ItemInfo)o;
         return this.item == other.item && Objects.equals(this.nbt, other.nbt);
      }
   }

   public String toString() {
      if (this.nbt != null) {
         ResourceLocation var10000 = this.getRegistryName();
         return "" + var10000 + " " + this.nbt;
      } else {
         return this.getRegistryName().toString();
      }
   }

   private ResourceLocation getRegistryName() {
      return ForgeRegistries.ITEMS.getKey(this.item);
   }
}
