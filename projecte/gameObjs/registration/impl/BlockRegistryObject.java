package moze_intel.projecte.gameObjs.registration.impl;

import javax.annotation.ParametersAreNonnullByDefault;
import moze_intel.projecte.gameObjs.registration.DoubleWrappedRegistryObject;
import moze_intel.projecte.utils.text.IHasTranslationKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
public class BlockRegistryObject extends DoubleWrappedRegistryObject implements ItemLike, IHasTranslationKey {
   public BlockRegistryObject(RegistryObject blockRegistryObject, RegistryObject itemRegistryObject) {
      super(blockRegistryObject, itemRegistryObject);
   }

   public @NotNull Block getBlock() {
      return (Block)this.getPrimary();
   }

   public @NotNull Item m_5456_() {
      return (Item)this.getSecondary();
   }

   public String getTranslationKey() {
      return this.getBlock().m_7705_();
   }

   public static class WallOrFloorBlockRegistryObject extends BlockRegistryObject {
      private final @NotNull RegistryObject wallRO;

      public WallOrFloorBlockRegistryObject(RegistryObject blockRegistryObject, RegistryObject wallBlockRegistryObject, RegistryObject itemRegistryObject) {
         super(blockRegistryObject, itemRegistryObject);
         this.wallRO = wallBlockRegistryObject;
      }

      public @NotNull Block getWallBlock() {
         return (Block)this.wallRO.get();
      }
   }
}
