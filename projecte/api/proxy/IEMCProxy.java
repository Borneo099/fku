package moze_intel.projecte.api.proxy;

import java.util.Objects;
import java.util.ServiceLoader;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface IEMCProxy {
   IEMCProxy INSTANCE = (IEMCProxy)ServiceLoader.load(IEMCProxy.class).findFirst().orElseThrow(() -> {
      return new IllegalStateException("No valid ServiceImpl for IEMCProxy found, ProjectE may be absent, damaged, or outdated");
   });

   default boolean hasValue(@NotNull Block block) {
      return this.hasValue(((Block)Objects.requireNonNull(block)).m_5456_());
   }

   default boolean hasValue(@NotNull Item item) {
      return Objects.requireNonNull(item) != Items.f_41852_ && this.hasValue(ItemInfo.fromItem(item));
   }

   default boolean hasValue(@NotNull ItemStack stack) {
      return !((ItemStack)Objects.requireNonNull(stack)).m_41619_() && this.hasValue(ItemInfo.fromStack(stack));
   }

   default boolean hasValue(@NotNull ItemInfo info) {
      return this.getValue((ItemInfo)Objects.requireNonNull(info)) > 0L;
   }

   default @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getValue(@NotNull Block block) {
      return this.getValue(((Block)Objects.requireNonNull(block)).m_5456_());
   }

   default @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getValue(@NotNull Item item) {
      return Objects.requireNonNull(item) == Items.f_41852_ ? 0L : this.getValue(ItemInfo.fromItem(item));
   }

   default @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getValue(@NotNull ItemStack stack) {
      return ((ItemStack)Objects.requireNonNull(stack)).m_41619_() ? 0L : this.getValue(ItemInfo.fromStack(stack));
   }

   @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getValue(@NotNull ItemInfo var1);

   default @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getSellValue(@NotNull ItemStack stack) {
      return ((ItemStack)Objects.requireNonNull(stack)).m_41619_() ? 0L : this.getSellValue(ItemInfo.fromStack(stack));
   }

   @Range(
   from = 0L,
   to = Long.MAX_VALUE
) long getSellValue(@NotNull ItemInfo var1);

   @NotNull ItemInfo getPersistentInfo(@NotNull ItemInfo var1);
}
