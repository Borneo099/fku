package moze_intel.projecte.gameObjs;

import java.util.Collections;
import java.util.List;
import moze_intel.projecte.PECore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.TierSortingRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EnumMatterType implements StringRepresentable, Tier {
   DARK_MATTER("dark_matter", 3.0F, 14.0F, 12.0F, 4, PETags.Blocks.NEEDS_DARK_MATTER_TOOL, Tiers.NETHERITE, PECore.rl("red_matter"), MapColor.f_283927_),
   RED_MATTER("red_matter", 4.0F, 16.0F, 14.0F, 5, PETags.Blocks.NEEDS_RED_MATTER_TOOL, DARK_MATTER, (ResourceLocation)null, MapColor.f_283913_);

   private final String name;
   private final float attackDamage;
   private final float efficiency;
   private final float chargeModifier;
   private final int harvestLevel;
   private final TagKey neededTag;
   private final MapColor mapColor;

   private EnumMatterType(String name, float attackDamage, float efficiency, float chargeModifier, int harvestLevel, @Nullable TagKey neededTag, Tier previous, ResourceLocation next, MapColor mapColor) {
      this.name = name;
      this.attackDamage = attackDamage;
      this.efficiency = efficiency;
      this.chargeModifier = chargeModifier;
      this.harvestLevel = harvestLevel;
      this.neededTag = neededTag;
      this.mapColor = mapColor;
      TierSortingRegistry.registerTier(this, PECore.rl(name), List.of(previous), next == null ? Collections.emptyList() : List.of(next));
   }

   public @NotNull String m_7912_() {
      return this.name;
   }

   public String toString() {
      return this.m_7912_();
   }

   public int m_6609_() {
      return 0;
   }

   public float getChargeModifier() {
      return this.chargeModifier;
   }

   public float m_6624_() {
      return this.efficiency;
   }

   public float m_6631_() {
      return this.attackDamage;
   }

   public int m_6604_() {
      return this.harvestLevel;
   }

   public int m_6601_() {
      return 0;
   }

   public @NotNull Ingredient m_6282_() {
      return Ingredient.f_43901_;
   }

   public MapColor getMapColor() {
      return this.mapColor;
   }

   public int getMatterTier() {
      return this.ordinal();
   }

   public @NotNull TagKey getTag() {
      return this.neededTag;
   }

   // $FF: synthetic method
   private static EnumMatterType[] $values() {
      return new EnumMatterType[]{DARK_MATTER, RED_MATTER};
   }
}
