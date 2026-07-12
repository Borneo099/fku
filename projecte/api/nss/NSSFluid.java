package moze_intel.projecte.api.nss;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NSSFluid extends AbstractNBTNSSTag {
   private NSSFluid(@NotNull ResourceLocation resourceLocation, boolean isTag, @Nullable CompoundTag nbt) {
      super(resourceLocation, isTag, nbt);
   }

   public static @NotNull NSSFluid createFluid(@NotNull FluidStack stack) {
      return createFluid(stack.getFluid(), stack.getTag());
   }

   public static @NotNull NSSFluid createFluid(@NotNull Fluid fluid) {
      return createFluid((Fluid)fluid, (CompoundTag)null);
   }

   public static @NotNull NSSFluid createFluid(@NotNull Fluid fluid, @Nullable CompoundTag nbt) {
      if (fluid == Fluids.f_76191_) {
         throw new IllegalArgumentException("Can't make NSSFluid with an empty fluid");
      } else {
         ResourceLocation registryName = ForgeRegistries.FLUIDS.getKey(fluid);
         if (registryName == null) {
            throw new IllegalArgumentException("Can't make an NSSFluid with an unregistered fluid");
         } else {
            return createFluid(registryName, nbt);
         }
      }
   }

   public static @NotNull NSSFluid createFluid(@NotNull ResourceLocation fluidID) {
      return createFluid((ResourceLocation)fluidID, (CompoundTag)null);
   }

   public static @NotNull NSSFluid createFluid(@NotNull ResourceLocation fluidID, @Nullable CompoundTag nbt) {
      return new NSSFluid(fluidID, false, nbt);
   }

   public static @NotNull NSSFluid createTag(@NotNull ResourceLocation tagId) {
      return new NSSFluid(tagId, true, (CompoundTag)null);
   }

   public static @NotNull NSSFluid createTag(@NotNull TagKey tag) {
      return createTag(tag.f_203868_());
   }

   protected boolean isInstance(AbstractNSSTag o) {
      return o instanceof NSSFluid;
   }

   public @NotNull String getJsonPrefix() {
      return "FLUID|";
   }

   public @NotNull String getType() {
      return "Fluid";
   }

   protected @NotNull Optional getTag() {
      return this.getTag(ForgeRegistries.FLUIDS);
   }

   protected Function createNew() {
      return NSSFluid::createFluid;
   }
}
