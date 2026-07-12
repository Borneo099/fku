package moze_intel.projecte.gameObjs.gui;

import java.math.BigInteger;
import java.util.Locale;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GUITransmutation extends PEContainerScreen {
   private static final ResourceLocation texture = PECore.rl("textures/gui/transmute.png");
   private final TransmutationInventory inv;
   private EditBox textBoxFilter;

   public GUITransmutation(TransmutationContainer container, Inventory invPlayer, Component title) {
      super(container, invPlayer, title);
      this.inv = container.transmutationInventory;
      this.f_97726_ = 228;
      this.f_97727_ = 196;
      this.f_97728_ = 6;
      this.f_97729_ = 8;
   }

   public void m_7856_() {
      super.m_7856_();
      this.textBoxFilter = (EditBox)this.m_7787_(new EditBox(this.f_96547_, this.f_97735_ + 88, this.f_97736_ + 8, 45, 10, Component.m_237119_()));
      this.textBoxFilter.m_94144_(this.inv.filter);
      this.textBoxFilter.m_94151_(this::updateFilter);
      this.m_142416_(Button.m_253074_(Component.m_237113_("<"), (b) -> {
         if (this.inv.searchpage != 0) {
            --this.inv.searchpage;
         }

         this.inv.filter = this.textBoxFilter.m_94155_().toLowerCase(Locale.ROOT);
         this.inv.updateClientTargets();
      }).m_252794_(this.f_97735_ + 125, this.f_97736_ + 100).m_253046_(14, 14).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_(">"), (b) -> {
         if (this.inv.getKnowledgeSize() > 12) {
            ++this.inv.searchpage;
         }

         this.inv.filter = this.textBoxFilter.m_94155_().toLowerCase(Locale.ROOT);
         this.inv.updateClientTargets();
      }).m_252794_(this.f_97735_ + 193, this.f_97736_ + 100).m_253046_(14, 14).m_253136_());
   }

   protected void m_7286_(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      graphics.m_280218_(texture, this.f_97735_, this.f_97736_, 0, 0, this.f_97726_, this.f_97727_);
      this.textBoxFilter.m_88315_(graphics, mouseX, mouseY, partialTicks);
   }

   protected void m_280003_(@NotNull GuiGraphics graphics, int x, int y) {
      graphics.m_280614_(this.f_96547_, this.f_96539_, this.f_97728_, this.f_97729_, 4210752, false);
      BigInteger emcAmount = this.inv.getAvailableEmc();
      graphics.m_280614_(this.f_96547_, PELang.EMC_TOOLTIP.translate(new Object[]{""}), 6, this.f_97727_ - 104, 4210752, false);
      Component emc = TransmutationEMCFormatter.formatEMC(emcAmount);
      graphics.m_280614_(this.f_96547_, emc, 6, this.f_97727_ - 94, 4210752, false);
      if (this.inv.learnFlag > 0) {
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_1.translate(new Object[0]), 98, 30, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_2.translate(new Object[0]), 99, 38, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_3.translate(new Object[0]), 100, 46, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_4.translate(new Object[0]), 101, 54, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_5.translate(new Object[0]), 102, 62, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_6.translate(new Object[0]), 103, 70, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_7.translate(new Object[0]), 104, 78, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_LEARNED_8.translate(new Object[0]), 107, 86, 4210752, false);
         --this.inv.learnFlag;
      }

      if (this.inv.unlearnFlag > 0) {
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_1.translate(new Object[0]), 97, 22, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_2.translate(new Object[0]), 98, 30, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_3.translate(new Object[0]), 99, 38, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_4.translate(new Object[0]), 100, 46, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_5.translate(new Object[0]), 101, 54, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_6.translate(new Object[0]), 102, 62, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_7.translate(new Object[0]), 103, 70, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_8.translate(new Object[0]), 104, 78, 4210752, false);
         graphics.m_280614_(this.f_96547_, PELang.TRANSMUTATION_UNLEARNED_9.translate(new Object[0]), 107, 86, 4210752, false);
         --this.inv.unlearnFlag;
      }

   }

   protected void m_181908_() {
      super.m_181908_();
      this.textBoxFilter.m_94120_();
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.textBoxFilter.m_93696_()) {
         if (keyCode == 256) {
            this.textBoxFilter.m_93692_(false);
            return true;
         } else {
            return this.textBoxFilter.m_7933_(keyCode, scanCode, modifiers);
         }
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }

   private void updateFilter(String text) {
      String search = text.toLowerCase(Locale.ROOT);
      if (!this.inv.filter.equals(search)) {
         this.inv.filter = search;
         this.inv.searchpage = 0;
         this.inv.updateClientTargets();
      }

   }

   public boolean m_6375_(double x, double y, int mouseButton) {
      if (this.textBoxFilter.m_5953_(x, y)) {
         if (mouseButton == 1) {
            this.textBoxFilter.m_94144_("");
         }
      } else if (this.textBoxFilter.m_93696_() && (this.f_97734_ == null || !this.f_97734_.m_6657_() && ((TransmutationContainer)this.f_97732_).m_142621_().m_41619_())) {
         this.textBoxFilter.m_93692_(false);
      }

      return super.m_6375_(x, y, mouseButton);
   }

   public void m_7861_() {
      super.m_7861_();
      this.inv.learnFlag = 0;
      this.inv.unlearnFlag = 0;
   }

   protected void m_280072_(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
      BigInteger emcAmount = this.inv.getAvailableEmc();
      if (emcAmount.compareTo(Constants.MAX_EXACT_TRANSMUTATION_DISPLAY) < 0) {
         super.m_280072_(graphics, mouseX, mouseY);
      } else {
         int emcLeft = this.f_97735_;
         int emcRight = emcLeft + 82;
         int emcTop = 95 + this.f_97736_;
         int emcBottom = emcTop + 15;
         if (mouseX > emcLeft && mouseX < emcRight && mouseY > emcTop && mouseY < emcBottom) {
            this.m_257404_(PELang.EMC_TOOLTIP.translate(new Object[]{Constants.EMC_FORMATTER.format(emcAmount)}));
         } else {
            super.m_280072_(graphics, mouseX, mouseY);
         }

      }
   }
}
