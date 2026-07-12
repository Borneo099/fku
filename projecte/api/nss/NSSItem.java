package moze_intel.projecte.api.nss;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NSSItem extends AbstractNBTNSSTag {
   private NSSItem(@NotNull ResourceLocation resourceLocation, boolean isTag, @Nullable CompoundTag nbt) {
      super(resourceLocation, isTag, nbt);
   }

   public static @NotNull NSSItem createItem(@NotNull ItemStack stack) {
      if (stack.m_41619_()) {
         throw new IllegalArgumentException("Can't make NSSItem with empty stack");
      } else {
         return stack.m_41763_() && stack.m_41782_() && stack.m_41784_().equals((new ItemStack(stack.m_41720_())).m_41783_()) ? createItem((ItemLike)stack.m_41720_(), (CompoundTag)null) : createItem((ItemLike)stack.m_41720_(), stack.m_41783_());
      }
   }

   public static @NotNull NSSItem createItem(@NotNull ItemLike itemProvider) {
      return createItem((ItemLike)itemProvider, (CompoundTag)null);
   }

   public static @NotNull NSSItem createItem(@NotNull ItemLike itemProvider, @Nullable CompoundTag nbt) {
      Item item = itemProvider.m_5456_();
      if (item == Items.f_41852_) {
         throw new IllegalArgumentException("Can't make NSSItem with empty stack");
      } else {
         ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
         if (registryName == null) {
            throw new IllegalArgumentException("Can't make an NSSItem with an unregistered item");
         } else {
            return createItem(registryName, nbt);
         }
      }
   }

   public static @NotNull NSSItem createItem(@NotNull ResourceLocation itemID) {
      return createItem((ResourceLocation)itemID, (CompoundTag)null);
   }

   public static @NotNull NSSItem createItem(@NotNull ResourceLocation itemID, @Nullable CompoundTag nbt) {
      return new NSSItem(itemID, false, nbt);
   }

   public static @NotNull NSSItem createTag(@NotNull ResourceLocation tagId) {
      return new NSSItem(tagId, true, (CompoundTag)null);
   }

   public static @NotNull NSSItem createTag(@NotNull TagKey tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSItem;
   }

   public @NotNull String getJsonPrefix() {
      return "";
   }

   public @NotNull String getType() {
      return "Item";
   }

   protected @NotNull Optional getTag() {
      return this.getTag(ForgeRegistries.ITEMS);
   }

   protected Function createNew() {
      return NSSItem::createItem;
   }
}
