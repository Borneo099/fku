package moze_intel.projecte.gameObjs.registration.impl;

import com.mojang.datafixers.types.Type;
import java.util.Objects;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTypeDeferredRegister extends WrappedDeferredRegister {
   public BlockEntityTypeDeferredRegister(String modid) {
      super(ForgeRegistries.BLOCK_ENTITY_TYPES, modid);
   }

   public BlockEntityTypeBuilder builder(BlockRegistryObject block, BlockEntityType.BlockEntitySupplier factory) {
      return new BlockEntityTypeBuilder(block, factory);
   }

   public class BlockEntityTypeBuilder {
      private final BlockRegistryObject block;
      private final BlockEntityType.BlockEntitySupplier factory;
      private @Nullable BlockEntityTicker clientTicker;
      private @Nullable BlockEntityTicker serverTicker;

      private BlockEntityTypeBuilder(BlockRegistryObject block, BlockEntityType.BlockEntitySupplier factory) {
         this.block = block;
         this.factory = factory;
      }

      public BlockEntityTypeBuilder clientTicker(BlockEntityTicker ticker) {
         if (this.clientTicker != null) {
            throw new IllegalStateException("Client ticker may only be set once.");
         } else {
            this.clientTicker = ticker;
            return this;
         }
      }

      public BlockEntityTypeBuilder serverTicker(BlockEntityTicker ticker) {
         if (this.serverTicker != null) {
            throw new IllegalStateException("Server ticker may only be set once.");
         } else {
            this.serverTicker = ticker;
            return this;
         }
      }

      public BlockEntityTypeBuilder commonTicker(BlockEntityTicker ticker) {
         return this.clientTicker(ticker).serverTicker(ticker);
      }

      public BlockEntityTypeRegistryObject build() {
         BlockEntityTypeRegistryObject registryObject = new BlockEntityTypeRegistryObject((RegistryObject)null);
         registryObject.clientTicker(this.clientTicker).serverTicker(this.serverTicker);
         BlockEntityTypeDeferredRegister var10000 = BlockEntityTypeDeferredRegister.this;
         String var10001 = this.block.getInternalRegistryName();
         Supplier var10002 = () -> {
            BlockRegistryObject patt2485$temp = this.block;
            Block[] validBlocks;
            if (patt2485$temp instanceof BlockRegistryObject.WallOrFloorBlockRegistryObject wallOrFloorBlock) {
               validBlocks = new Block[]{this.block.getBlock(), wallOrFloorBlock.getWallBlock()};
            } else {
               validBlocks = new Block[]{this.block.getBlock()};
            }

            return Builder.m_155273_(this.factory, validBlocks).m_58966_((Type)null);
         };
         Objects.requireNonNull(registryObject);
         return (BlockEntityTypeRegistryObject)var10000.register(var10001, var10002, registryObject::setRegistryObject);
      }
   }
}
