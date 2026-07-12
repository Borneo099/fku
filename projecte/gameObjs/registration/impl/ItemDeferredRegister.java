package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemDeferredRegister extends WrappedDeferredRegister {
   public ItemDeferredRegister(String modid) {
      super(ForgeRegistries.ITEMS, modid);
   }

   public ItemRegistryObject register(String name) {
      return this.register(name, Item::new);
   }

   public ItemRegistryObject registerFireImmune(String name) {
      return this.registerFireImmune(name, Item::new);
   }

   public ItemRegistryObject register(String name, Function sup) {
      return this.register(name, sup, UnaryOperator.identity());
   }

   public ItemRegistryObject registerFireImmune(String name, Function sup) {
      return this.register(name, sup, Item.Properties::m_41486_);
   }

   public ItemRegistryObject registerNoStack(String name, Function sup) {
      return this.register(name, sup, (properties) -> {
         return properties.m_41487_(1);
      });
   }

   public ItemRegistryObject registerNoStackFireImmune(String name, Function sup) {
      return this.register(name, sup, (properties) -> {
         return properties.m_41487_(1).m_41486_();
      });
   }

   public ItemRegistryObject register(String name, Function sup, UnaryOperator propertyModifier) {
      return this.register(name, () -> {
         return (Item)sup.apply((Item.Properties)propertyModifier.apply(new Item.Properties()));
      });
   }

   public ItemRegistryObject register(String name, Supplier sup) {
      return (ItemRegistryObject)this.register(name, sup, ItemRegistryObject::new);
   }
}
