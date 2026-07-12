package moze_intel.projecte.api.nss;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNBTNSSTag extends AbstractNSSTag implements NSSNBT {
   private final @Nullable CompoundTag nbt;

   protected AbstractNBTNSSTag(@NotNull ResourceLocation resourceLocation, boolean isTag, @Nullable CompoundTag nbt) {
      super(resourceLocation, isTag);
      this.nbt = nbt != null && nbt.m_128456_() ? null : nbt;
   }

   public @Nullable CompoundTag getNBT() {
      return this.nbt;
   }

   public String json() {
      String json = super.json();
      return this.hasNBT() ? json + this.nbt : json;
   }

   public String toString() {
      String string = super.toString();
      return this.hasNBT() ? string + this.nbt : string;
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else {
         return super.equals(o) && Objects.equals(this.nbt, ((AbstractNBTNSSTag)o).nbt);
      }
   }

   public int hashCode() {
      int code = super.hashCode();
      if (this.hasNBT()) {
         code = 31 * code + this.getNBT().hashCode();
      }

      return code;
   }
}
