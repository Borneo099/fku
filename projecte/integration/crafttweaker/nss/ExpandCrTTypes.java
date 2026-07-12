package moze_intel.projecte.integration.crafttweaker.nss;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Expansion;

public class ExpandCrTTypes {
   private ExpandCrTTypes() {
   }

   @ZenRegister
   @Expansion("crafttweaker.api.tag.type.KnownTag<crafttweaker.api.fluid.Fluid>")
   public static class FluidTagExpansion {
      private FluidTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag _this) {
         return CrTNSSResolver.fromFluidTag(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IFluidStack.class)
   public static class IFluidStackExpansion {
      private IFluidStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(IFluidStack _this) {
         return CrTNSSResolver.fromFluid(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(Fluid.class)
   public static class FluidExpansion {
      private FluidExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(Fluid _this) {
         return CrTNSSResolver.fromFluid(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.tag.type.KnownTag<crafttweaker.api.item.ItemDefinition>")
   public static class ItemTagExpansion {
      private ItemTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag _this) {
         return CrTNSSResolver.fromItemTag(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IItemStack.class)
   public static class IItemStackExpansion {
      private IItemStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(IItemStack _this) {
         return CrTNSSResolver.fromItem(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(Item.class)
   public static class ItemExpansion {
      private ItemExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(Item _this) {
         return CrTNSSResolver.fromItem(_this);
      }
   }
}
