package moze_intel.projecte.capability;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCapabilityWrapper implements ICapabilitySerializable {
   private final ItemCapability[] capabilities;
   private final ItemStack itemStack;

   public ItemCapabilityWrapper(ItemStack stack, List capabilities) {
      this.itemStack = stack;
      this.capabilities = new ItemCapability[capabilities.size()];

      for(int i = 0; i < capabilities.size(); ++i) {
         ItemCapability cap = (ItemCapability)((Supplier)capabilities.get(i)).get();
         this.capabilities[i] = cap;
         cap.setWrapper(this);
      }

   }

   public ItemCapabilityWrapper(ItemStack stack, ItemCapability... capabilities) {
      this.itemStack = stack;
      this.capabilities = capabilities;
      ItemCapability[] var3 = this.capabilities;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemCapability cap = var3[var5];
         cap.setWrapper(this);
      }

   }

   protected ItemStack getItemStack() {
      return this.itemStack;
   }

   public @NotNull LazyOptional getCapability(@NotNull Capability capability, @Nullable Direction side) {
      ItemCapability[] var3 = this.capabilities;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemCapability cap = var3[var5];
         if (capability == cap.getCapability()) {
            return cap.getLazyCapability().cast();
         }
      }

      return LazyOptional.empty();
   }

   public CompoundTag serializeNBT() {
      CompoundTag serializedNBT = new CompoundTag();
      ItemCapability[] var2 = this.capabilities;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemCapability cap = var2[var4];
         if (cap instanceof IItemCapabilitySerializable serializableCap) {
            serializedNBT.m_128365_(serializableCap.getStorageKey(), serializableCap.serializeNBT());
         }
      }

      return serializedNBT;
   }

   public void deserializeNBT(CompoundTag nbt) {
      ItemCapability[] var2 = this.capabilities;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemCapability cap = var2[var4];
         if (cap instanceof IItemCapabilitySerializable serializableCap) {
            if (nbt.m_128441_(serializableCap.getStorageKey())) {
               serializableCap.deserializeNBT(nbt.m_128423_(serializableCap.getStorageKey()));
            }
         }
      }

   }
}
