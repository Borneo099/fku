package moze_intel.projecte.integration.jei.world_transmute;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.utils.WorldTransmutations;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class WorldTransmuteRecipeCategory implements IRecipeCategory {
   public static final RecipeType RECIPE_TYPE = new RecipeType(PECore.rl("world_transmutation"), WorldTransmuteEntry.class);
   private final IDrawable background;
   private final IDrawable arrow;
   private final IDrawable icon;

   public WorldTransmuteRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(135, 48);
      this.arrow = guiHelper.drawableBuilder(PECore.rl("textures/gui/arrow.png"), 0, 0, 22, 15).setTextureSize(32, 32).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PEItems.PHILOSOPHERS_STONE));
   }

   public @NotNull RecipeType getRecipeType() {
      return RECIPE_TYPE;
   }

   public @NotNull Component getTitle() {
      return PELang.WORLD_TRANSMUTE.translate(new Object[0]);
   }

   public @NotNull IDrawable getBackground() {
      return this.background;
   }

   public @NotNull IDrawable getIcon() {
      return this.icon;
   }

   public void draw(@NotNull WorldTransmuteEntry recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
      this.arrow.draw(graphics, 55, 18);
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull WorldTransmuteEntry recipe, @NotNull IFocusGroup focuses) {
      recipe.getInput().ifPresent((recipeInput) -> {
         recipeInput.ifLeft((input) -> {
            builder.addSlot(RecipeIngredientRole.INPUT, 16, 16).addItemStack(input);
         }).ifRight((input) -> {
            ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 16, 16).addIngredient(ForgeTypes.FLUID_STACK, input)).setFluidRenderer(1000L, false, 16, 16);
         });
      });
      int xPos = 96;

      for(Iterator var5 = recipe.getOutput().iterator(); var5.hasNext(); xPos += 16) {
         Either output = (Either)var5.next();
         IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, xPos, 16);
         Objects.requireNonNull(slot);
         output.ifLeft(slot::addItemStack).ifRight((input) -> {
            ((IRecipeSlotBuilder)slot.addIngredient(ForgeTypes.FLUID_STACK, input)).setFluidRenderer(1000L, false, 16, 16);
         });
      }

   }

   public @NotNull List getTooltipStrings(@NotNull WorldTransmuteEntry recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      return mouseX > 67.0 && mouseX < 107.0 && mouseY > 18.0 && mouseY < 38.0 ? Collections.singletonList(PELang.WORLD_TRANSMUTE_DESCRIPTION.translate(new Object[0])) : Collections.emptyList();
   }

   public static List getAllTransmutations() {
      List allWorldTransmutations = WorldTransmutations.getWorldTransmutations();
      List visible = new ArrayList();
      allWorldTransmutations.forEach((entry) -> {
         WorldTransmuteEntry e = new WorldTransmuteEntry(entry);
         if (e.isRenderable()) {
            FluidStack inputFluid = e.getInputFluid();
            boolean alreadyHas;
            if (inputFluid.isEmpty()) {
               ItemStack inputItem = e.getInputItem();
               alreadyHas = visible.stream().map(WorldTransmuteEntry::getInputItem).anyMatch((otherInputItem) -> {
                  return !otherInputItem.m_41619_() && ItemStack.m_150942_(inputItem, otherInputItem);
               });
            } else {
               alreadyHas = visible.stream().map(WorldTransmuteEntry::getInputFluid).anyMatch((otherInputFluid) -> {
                  return !otherInputFluid.isEmpty() && inputFluid.isFluidEqual(otherInputFluid);
               });
            }

            if (!alreadyHas) {
               visible.add(e);
            }
         }

      });
      return visible;
   }
}
