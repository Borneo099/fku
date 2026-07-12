package moze_intel.projecte;

import java.util.Iterator;
import java.util.Objects;
import mezz.jei.api.runtime.IRecipesGui;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.gui.AbstractCollectorScreen;
import moze_intel.projecte.gameObjs.gui.AbstractCondenserScreen;
import moze_intel.projecte.gameObjs.gui.AlchBagScreen;
import moze_intel.projecte.gameObjs.gui.AlchChestScreen;
import moze_intel.projecte.gameObjs.gui.GUIDMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIEternalDensity;
import moze_intel.projecte.gameObjs.gui.GUIMercurialEye;
import moze_intel.projecte.gameObjs.gui.GUIRMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIRelay;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import moze_intel.projecte.gameObjs.gui.PEContainerScreen;
import moze_intel.projecte.gameObjs.registration.impl.ContainerTypeRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.EntitySpriteRenderer;
import moze_intel.projecte.rendering.LayerYue;
import moze_intel.projecte.rendering.NovaRenderer;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.rendering.TransmutationRenderingOverlay;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientRegistration {
   public static final ResourceLocation ACTIVE_OVERRIDE = PECore.rl("active");
   public static final ResourceLocation MODE_OVERRIDE = PECore.rl("mode");

   @SubscribeEvent
   public static void registerContainers(RegisterEvent event) {
      event.register(Registries.f_256798_, (helper) -> {
         registerScreen(PEContainerTypes.RM_FURNACE_CONTAINER, GUIRMFurnace::new);
         registerScreen(PEContainerTypes.DM_FURNACE_CONTAINER, GUIDMFurnace::new);
         registerScreen(PEContainerTypes.CONDENSER_CONTAINER, AbstractCondenserScreen.MK1::new);
         registerScreen(PEContainerTypes.CONDENSER_MK2_CONTAINER, AbstractCondenserScreen.MK2::new);
         registerScreen(PEContainerTypes.ALCH_CHEST_CONTAINER, AlchChestScreen::new);
         registerScreen(PEContainerTypes.ALCH_BAG_CONTAINER, AlchBagScreen::new);
         registerScreen(PEContainerTypes.ETERNAL_DENSITY_CONTAINER, GUIEternalDensity::new);
         registerScreen(PEContainerTypes.TRANSMUTATION_CONTAINER, GUITransmutation::new);
         registerScreen(PEContainerTypes.RELAY_MK1_CONTAINER, GUIRelay.GUIRelayMK1::new);
         registerScreen(PEContainerTypes.RELAY_MK2_CONTAINER, GUIRelay.GUIRelayMK2::new);
         registerScreen(PEContainerTypes.RELAY_MK3_CONTAINER, GUIRelay.GUIRelayMK3::new);
         registerScreen(PEContainerTypes.COLLECTOR_MK1_CONTAINER, AbstractCollectorScreen.MK1::new);
         registerScreen(PEContainerTypes.COLLECTOR_MK2_CONTAINER, AbstractCollectorScreen.MK2::new);
         registerScreen(PEContainerTypes.COLLECTOR_MK3_CONTAINER, AbstractCollectorScreen.MK3::new);
         registerScreen(PEContainerTypes.MERCURIAL_EYE_CONTAINER, GUIMercurialEye::new);
      });
   }

   @SubscribeEvent
   public static void clientSetup(FMLClientSetupEvent evt) {
      if (ModList.get().isLoaded("jei")) {
         MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (event) -> {
            Screen patt5095$temp = event.getCurrentScreen();
            if (patt5095$temp instanceof PEContainerScreen screen) {
               if (event.getNewScreen() instanceof IRecipesGui) {
                  screen.switchingToJEI = true;
               }
            }

         });
      }

      evt.enqueueWork(() -> {
         addPropertyOverrides(ACTIVE_OVERRIDE, (stack, level, entity, seed) -> {
            return ItemHelper.checkItemNBT(stack, "Active") ? 1.0F : 0.0F;
         }, PEItems.GEM_OF_ETERNAL_DENSITY, PEItems.VOID_RING, PEItems.ARCANA_RING, PEItems.ARCHANGEL_SMITE, PEItems.BLACK_HOLE_BAND, PEItems.BODY_STONE, PEItems.HARVEST_GODDESS_BAND, PEItems.IGNITION_RING, PEItems.LIFE_STONE, PEItems.MIND_STONE, PEItems.SOUL_STONE, PEItems.WATCH_OF_FLOWING_TIME, PEItems.ZERO_RING);
         addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) -> {
            return stack.m_41782_() ? (float)stack.m_41784_().m_128451_("Mode") : 0.0F;
         }, PEItems.ARCANA_RING, PEItems.SWIFTWOLF_RENDING_GALE);
      });
   }

   @SubscribeEvent
   public static void registerKeybindings(RegisterKeyMappingsEvent event) {
      ClientKeyHelper.registerKeyBindings(event);
   }

   @SubscribeEvent
   public static void registerOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "transmutation_result", new TransmutationRenderingOverlay());
   }

   @SubscribeEvent
   public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)PEBlockEntityTypes.ALCHEMICAL_CHEST.get(), (context) -> {
         return new ChestRenderer(context, PECore.rl("textures/block/alchemical_chest.png"), () -> {
            return PEBlocks.ALCHEMICAL_CHEST;
         });
      });
      event.registerBlockEntityRenderer((BlockEntityType)PEBlockEntityTypes.CONDENSER.get(), (context) -> {
         return new ChestRenderer(context, PECore.rl("textures/block/condenser_mk1.png"), () -> {
            return PEBlocks.CONDENSER;
         });
      });
      event.registerBlockEntityRenderer((BlockEntityType)PEBlockEntityTypes.CONDENSER_MK2.get(), (context) -> {
         return new ChestRenderer(context, PECore.rl("textures/block/condenser_mk2.png"), () -> {
            return PEBlocks.CONDENSER_MK2;
         });
      });
      event.registerBlockEntityRenderer((BlockEntityType)PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);
      event.registerEntityRenderer((EntityType)PEEntityTypes.WATER_PROJECTILE.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/water_orb.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.LAVA_PROJECTILE.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/lava_orb.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.MOB_RANDOMIZER.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/randomizer.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.LENS_PROJECTILE.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/lens_explosive.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.FIRE_PROJECTILE.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/fireball.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.SWRG_PROJECTILE.get(), (context) -> {
         return new EntitySpriteRenderer(context, PECore.rl("textures/entity/lightning.png"));
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.NOVA_CATALYST_PRIMED.get(), (context) -> {
         ProjectETNT var10003 = (ProjectETNT)PEBlocks.NOVA_CATALYST.getBlock();
         Objects.requireNonNull(var10003);
         return new NovaRenderer(context, var10003::m_49966_);
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.NOVA_CATACLYSM_PRIMED.get(), (context) -> {
         ProjectETNT var10003 = (ProjectETNT)PEBlocks.NOVA_CATACLYSM.getBlock();
         Objects.requireNonNull(var10003);
         return new NovaRenderer(context, var10003::m_49966_);
      });
      event.registerEntityRenderer((EntityType)PEEntityTypes.HOMING_ARROW.get(), TippableArrowRenderer::new);
   }

   @SubscribeEvent
   public static void addLayers(EntityRenderersEvent.AddLayers event) {
      Iterator var1 = event.getSkins().iterator();

      while(var1.hasNext()) {
         String skinName = (String)var1.next();
         PlayerRenderer skin = (PlayerRenderer)event.getSkin(skinName);
         if (skin != null) {
            skin.m_115326_(new LayerYue(skin));
         }
      }

   }

   private static void addPropertyOverrides(ResourceLocation override, ItemPropertyFunction propertyGetter, ItemLike... itemProviders) {
      ItemLike[] var3 = itemProviders;
      int var4 = itemProviders.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemLike itemProvider = var3[var5];
         ItemProperties.register(itemProvider.m_5456_(), override, propertyGetter);
      }

   }

   private static void registerScreen(ContainerTypeRegistryObject type, MenuScreens.ScreenConstructor factory) {
      MenuScreens.m_96206_((MenuType)type.get(), factory);
   }
}
