package moze_intel.projecte.integration.jei.collectors;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectorRecipeCategory implements IRecipeCategory {
   public static final RecipeType RECIPE_TYPE = new RecipeType(PECore.rl("collector"), FuelUpgradeRecipe.class);
   private final IDrawable background;
   private final IDrawable arrow;
   private final IDrawable icon;

   public CollectorRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createBlankDrawable(135, 48);
      this.arrow = guiHelper.drawableBuilder(PECore.rl("textures/gui/arrow.png"), 0, 0, 22, 15).setTextureSize(32, 32).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(PEBlocks.COLLECTOR));
   }

   public @NotNull RecipeType getRecipeType() {
      return RECIPE_TYPE;
   }

   public @NotNull Component getTitle() {
      return PELang.JEI_COLLECTOR.translate(new Object[0]);
   }

   public @NotNull IDrawable getBackground() {
      return this.background;
   }

   public @NotNull IDrawable getIcon() {
      return this.icon;
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull FuelUpgradeRecipe recipe, @NotNull IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 16, 16).addItemStack(recipe.input());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 16).addItemStack(recipe.output());
   }

   public void draw(FuelUpgradeRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
      Component emc = PELang.EMC.translate(new Object[]{recipe.upgradeEMC()});
      Font fontRenderer = Minecraft.m_91087_().f_91062_;
      int stringWidth = fontRenderer.m_92852_(emc);
      graphics.drawString(fontRenderer, emc.m_7532_(), (float)(this.getBackground().getWidth() - stringWidth) / 2.0F, 5.0F, 8421504, false);
      this.arrow.draw(graphics, 55, 18);
   }
}
