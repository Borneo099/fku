package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedRegistryObject;
import moze_intel.projecte.utils.RegistryUtils;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ItemRegistryObject extends WrappedRegistryObject implements ItemLike, IHasTranslationKey {
   public ItemRegistryObject(RegistryObject registryObject) {
      super(registryObject);
   }

   public @NotNull Item m_5456_() {
      return (Item)this.get();
   }

   public String getTranslationKey() {
      return ((Item)this.get()).m_5524_();
   }

   public ResourceLocation getRegistryName() {
      return RegistryUtils.getName((Item)this.get());
   }
}
