package moze_intel.projecte.gameObjs.registration.impl;

import moze_intel.projecte.gameObjs.registration.WrappedRegistryObject;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTypeRegistryObject extends WrappedRegistryObject {
   private @Nullable BlockEntityTicker clientTicker;
   private @Nullable BlockEntityTicker serverTicker;

   public BlockEntityTypeRegistryObject(RegistryObject registryObject) {
      super(registryObject);
   }

   BlockEntityTypeRegistryObject setRegistryObject(RegistryObject registryObject) {
      this.registryObject = registryObject;
      return this;
   }

   BlockEntityTypeRegistryObject clientTicker(BlockEntityTicker ticker) {
      this.clientTicker = ticker;
      return this;
   }

   BlockEntityTypeRegistryObject serverTicker(BlockEntityTicker ticker) {
      this.serverTicker = ticker;
      return this;
   }

   public @Nullable BlockEntityTicker getTicker(boolean isClient) {
      return isClient ? this.clientTicker : this.serverTicker;
   }
}
