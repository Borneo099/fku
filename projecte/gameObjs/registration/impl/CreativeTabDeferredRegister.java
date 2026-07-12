package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import moze_intel.projecte.gameObjs.registration.WrappedDeferredRegister;
import moze_intel.projecte.utils.text.ILangEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreativeTabDeferredRegister extends WrappedDeferredRegister {
   private final Consumer addToExistingTabs;
   private final String modid;

   public CreativeTabDeferredRegister(String modid, Consumer addToExistingTabs) {
      super(Registries.f_279569_, modid);
      this.modid = modid;
      this.addToExistingTabs = addToExistingTabs;
   }

   public void register(IEventBus bus) {
      super.register(bus);
      bus.addListener(this.addToExistingTabs);
   }

   public CreativeTabRegistryObject registerMain(ILangEntry title, ItemLike icon, UnaryOperator operator) {
      return this.register(this.modid, title, icon, operator);
   }

   public CreativeTabRegistryObject register(String name, ILangEntry title, ItemLike icon, UnaryOperator operator) {
      return (CreativeTabRegistryObject)this.register(name, () -> {
         CreativeModeTab.Builder builder = CreativeModeTab.builder().m_257941_(title.translate()).m_257737_(() -> {
            return icon.m_5456_().m_7968_();
         });
         return ((CreativeModeTab.Builder)operator.apply(builder)).m_257652_();
      }, CreativeTabRegistryObject::new);
   }
}
