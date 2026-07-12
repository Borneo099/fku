package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.DoubleDeferredRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockDeferredRegister extends DoubleDeferredRegister {
   public BlockDeferredRegister(String modid) {
      super(ForgeRegistries.BLOCKS, ForgeRegistries.ITEMS, modid);
   }

   public BlockRegistryObject register(String name, BlockBehaviour.Properties properties) {
      return this.registerDefaultProperties(name, () -> {
         return new Block(properties);
      }, BlockItem::new);
   }

   public BlockRegistryObject register(String name, Supplier blockSupplier) {
      return this.registerDefaultProperties(name, blockSupplier, BlockItem::new);
   }

   public BlockRegistryObject.WallOrFloorBlockRegistryObject registerWallOrFloorItem(String name, Function blockSupplier, Function wallBlockSupplier, BlockBehaviour.Properties baseProperties) {
      RegistryObject primaryObject = this.primaryRegister.register(name, () -> {
         return (Block)blockSupplier.apply(baseProperties);
      });
      RegistryObject wallObject = this.primaryRegister.register("wall_" + name, () -> {
         return (Block)wallBlockSupplier.apply(baseProperties.lootFrom(primaryObject));
      });
      return new BlockRegistryObject.WallOrFloorBlockRegistryObject(primaryObject, wallObject, this.secondaryRegister.register(name, () -> {
         return new StandingAndWallBlockItem((Block)primaryObject.get(), (Block)wallObject.get(), new Item.Properties(), Direction.DOWN);
      }));
   }

   public BlockRegistryObject registerDefaultProperties(String name, Supplier blockSupplier, BiFunction itemCreator) {
      return this.register(name, blockSupplier, (block) -> {
         return (BlockItem)itemCreator.apply(block, new Item.Properties());
      });
   }

   public BlockRegistryObject register(String name, Supplier blockSupplier, Function itemCreator) {
      return (BlockRegistryObject)this.register(name, blockSupplier, itemCreator, BlockRegistryObject::new);
   }
}
