package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.INamedEntry;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerTypeDeferredRegister extends WrappedDeferredRegister {
   public ContainerTypeDeferredRegister(String modid) {
      super(ForgeRegistries.MENU_TYPES, modid);
   }

   public ContainerTypeRegistryObject register(INamedEntry nameProvider, Class blockEntityClass, IBlockEntityContainerFactory factory) {
      return this.register(nameProvider, (id, inv, buf) -> {
         return factory.create(id, inv, getBlockEntityFromBuf(buf, blockEntityClass));
      });
   }

   public ContainerTypeRegistryObject register(INamedEntry nameProvider, IContainerFactory factory) {
      return this.register(nameProvider.getInternalRegistryName(), factory);
   }

   public ContainerTypeRegistryObject register(String name, IContainerFactory factory) {
      return (ContainerTypeRegistryObject)this.register(name, () -> {
         return new MenuType(factory, FeatureFlags.f_244377_);
      }, ContainerTypeRegistryObject::new);
   }

   private static BlockEntity getBlockEntityFromBuf(FriendlyByteBuf buf, Class type) {
      if (buf == null) {
         throw new IllegalArgumentException("Null packet buffer");
      } else {
         return (BlockEntity)DistExecutor.unsafeRunForDist(() -> {
            return () -> {
               BlockPos pos = buf.m_130135_();
               BlockEntity blockEntity = WorldHelper.getBlockEntity(type, Minecraft.m_91087_().f_91073_, pos);
               if (blockEntity == null) {
                  throw new IllegalStateException("Client could not locate block entity at " + pos + " for block entity container. This is likely caused by a mod breaking client side block entity lookup");
               } else {
                  return blockEntity;
               }
            };
         }, () -> {
            return () -> {
               throw new RuntimeException("Shouldn't be called on server!");
            };
         });
      }
   }

   @FunctionalInterface
   public interface IBlockEntityContainerFactory {
      AbstractContainerMenu create(int var1, Inventory var2, BlockEntity var3);
   }
}
