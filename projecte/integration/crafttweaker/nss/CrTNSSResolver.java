package moze_intel.projecte.integration.crafttweaker.nss;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import com.google.gson.JsonParseException;
import moze_intel.projecte.api.nss.NSSFluid;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.emc.json.NSSSerializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Document("mods/ProjectE/NSSResolver")
@Name("mods.projecte.NSSResolver")
public class CrTNSSResolver {
   private CrTNSSResolver() {
   }

   @Method
   public static NormalizedSimpleStack deserialize(String representation) {
      try {
         return NSSSerializer.INSTANCE.deserialize(representation);
      } catch (JsonParseException var2) {
         throw new IllegalArgumentException("Error deserializing NSS string representation", var2);
      }
   }

   @Method
   public static NormalizedSimpleStack fromItem(Item item) {
      if (item == Items.f_41852_) {
         throw new IllegalArgumentException("Cannot make an NSS Representation from the empty item.");
      } else {
         return NSSItem.createItem((ItemLike)item);
      }
   }

   @Method
   public static NormalizedSimpleStack fromItem(IItemStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Cannot make an NSS Representation from an empty item stack.");
      } else {
         return NSSItem.createItem(stack.getInternal());
      }
   }

   @Method
   public static NormalizedSimpleStack fromItemTag(KnownTag tag) {
      if (tag.exists()) {
         return NSSItem.createTag(tag.id());
      } else {
         throw new IllegalArgumentException("Item tag " + tag.getCommandString() + " does not exist.");
      }
   }

   @Method
   public static NormalizedSimpleStack fromFluid(IFluidStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Cannot make an NSS Representation from an empty fluid stack.");
      } else {
         return NSSFluid.createFluid((FluidStack)stack.getInternal());
      }
   }

   @Method
   public static NormalizedSimpleStack fromFluid(Fluid fluid) {
      if (fluid == Fluids.f_76191_) {
         throw new IllegalArgumentException("Cannot make an NSS Representation from the empty fluid.");
      } else {
         return NSSFluid.createFluid(fluid);
      }
   }

   @Method
   public static NormalizedSimpleStack fromFluidTag(KnownTag tag) {
      if (tag.exists()) {
         return NSSFluid.createTag(tag.id());
      } else {
         throw new IllegalArgumentException("Fluid tag " + tag.getCommandString() + " does not exist.");
      }
   }
}
