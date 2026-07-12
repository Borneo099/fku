package moze_intel.projecte;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.config.PEModConfig;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.json.NSSSerializer;
import moze_intel.projecte.emc.mappers.recipe.CraftingMapper;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.customRecipes.FullKleinStarsCondition;
import moze_intel.projecte.gameObjs.customRecipes.TomeEnabledCondition;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.items.rings.Arcana;
import moze_intel.projecte.gameObjs.registries.PEArgumentTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PECreativeTabs;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.IMCHandler;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.ThreadCheckUUID;
import moze_intel.projecte.network.ThreadCheckUpdate;
import moze_intel.projecte.network.commands.EMCCMD;
import moze_intel.projecte.network.commands.KnowledgeCMD;
import moze_intel.projecte.network.commands.RemoveEmcCMD;
import moze_intel.projecte.network.commands.ResetEmcCMD;
import moze_intel.projecte.network.commands.SetEmcCMD;
import moze_intel.projecte.network.commands.ShowBagCMD;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.utils.WorldTransmutations;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod("projecte")
@EventBusSubscriber(
   modid = "projecte"
)
public class PECore {
   public static final String MODID = "projecte";
   public static final String MODNAME = "ProjectE";
   public static final GameProfile FAKEPLAYER_GAMEPROFILE = new GameProfile(UUID.fromString("590e39c7-9fb6-471b-a4c2-c0e539b2423d"), "[ProjectE]");
   public static final Logger LOGGER = LogUtils.getLogger();
   public static final List uuids = new ArrayList();
   public static ModContainer MOD_CONTAINER;
   @Nullable
   private @Nullable EmcUpdateData emcUpdateResourceManager;

   public static void debugLog(String msg, Object... args) {
      if (FMLEnvironment.production && !ProjectEConfig.common.debugLogging.get()) {
         LOGGER.debug(msg, args);
      } else {
         LOGGER.info(msg, args);
      }

   }

   public static ResourceLocation rl(String path) {
      return new ResourceLocation("projecte", path);
   }

   public PECore() {
      MOD_CONTAINER = ModLoadingContext.get().getActiveContainer();
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(this::imcQueue);
      modEventBus.addListener(IMCHandler::handleMessages);
      modEventBus.addListener(this::onConfigLoad);
      modEventBus.addListener(this::registerCapabilities);
      modEventBus.addListener(this::registerRecipeSerializers);
      PEArgumentTypes.ARGUMENT_TYPES.register(modEventBus);
      PEBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
      PEBlocks.BLOCKS.register(modEventBus);
      PEContainerTypes.CONTAINER_TYPES.register(modEventBus);
      PECreativeTabs.CREATIVE_TABS.register(modEventBus);
      PEEntityTypes.ENTITY_TYPES.register(modEventBus);
      PEItems.ITEMS.register(modEventBus);
      PERecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
      PESoundEvents.SOUND_EVENTS.register(modEventBus);
      MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
      MinecraftForge.EVENT_BUS.addListener(this::tagsUpdated);
      MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
      MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
      MinecraftForge.EVENT_BUS.addListener(this::serverQuit);
      MinecraftForge.EVENT_BUS.addListener(PEPermissions::registerPermissionNodes);
      ProjectEConfig.register();
   }

   private void registerRecipeSerializers(RegisterEvent event) {
      event.register(Registries.f_256764_, (helper) -> {
         CraftingHelper.register(TomeEnabledCondition.SERIALIZER);
         CraftingHelper.register(FullKleinStarsCondition.SERIALIZER);
      });
   }

   private void registerCapabilities(RegisterCapabilitiesEvent event) {
      event.register(IAlchBagProvider.class);
      event.register(IKnowledgeProvider.class);
      event.register(InternalTimers.class);
      event.register(InternalAbilities.class);
      event.register(CommonInternalAbilities.class);
      event.register(IAlchBagItem.class);
      event.register(IAlchChestItem.class);
      event.register(IExtraFunction.class);
      event.register(IItemCharge.class);
      event.register(IItemEmcHolder.class);
      event.register(IModeChanger.class);
      event.register(IPedestalItem.class);
      event.register(IProjectileShooter.class);
      event.register(IEmcStorage.class);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      (new ThreadCheckUpdate()).start();
      EMCMappingHandler.loadMappers();
      CraftingMapper.loadMappers();
      NBTManager.loadProcessors();
      event.enqueueWork(() -> {
         PETags.init();
         PacketHandler.register();
         registerDispenseBehavior(new ShearsDispenseItemBehavior(), PEItems.DARK_MATTER_SHEARS, PEItems.RED_MATTER_SHEARS, PEItems.RED_MATTER_KATAR);
         DispenserBlock.m_52672_(PEBlocks.NOVA_CATALYST, ((ProjectETNT)PEBlocks.NOVA_CATALYST.getBlock()).createDispenseItemBehavior());
         DispenserBlock.m_52672_(PEBlocks.NOVA_CATACLYSM, ((ProjectETNT)PEBlocks.NOVA_CATACLYSM.getBlock()).createDispenseItemBehavior());
         registerDispenseBehavior(new OptionalDispenseItemBehavior() {
            protected @NotNull ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
               Item var4 = stack.m_41720_();
               if (var4 instanceof Arcana item) {
                  if (item.getMode(stack) != 1) {
                     this.m_123573_(false);
                     return super.m_7498_(source, stack);
                  }
               }

               Level level = source.m_7727_();
               this.m_123573_(true);
               Direction direction = (Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_);
               BlockPos pos = source.m_7961_().m_121945_(direction);
               BlockState state = level.m_8055_(pos);
               if (BaseFireBlock.m_49255_(level, pos, direction)) {
                  level.m_46597_(pos, BaseFireBlock.m_49245_(level, pos));
               } else if (CampfireBlock.m_51321_(state)) {
                  level.m_46597_(pos, (BlockState)state.m_61124_(BlockStateProperties.f_61443_, true));
               } else if (state.isFlammable(level, pos, direction.m_122424_())) {
                  state.onCaughtFire(level, pos, direction.m_122424_(), (LivingEntity)null);
                  if (state.m_60734_() instanceof TntBlock) {
                     level.m_7471_(pos, false);
                  }
               } else {
                  this.m_123573_(false);
               }

               return stack;
            }
         }, PEItems.IGNITION_RING, PEItems.ARCANA_RING);
         DispenserBlock.m_52672_(PEItems.EVERTIDE_AMULET, new DefaultDispenseItemBehavior() {
            public @NotNull ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
               Level level = source.m_7727_();
               Direction direction = (Direction)source.m_6414_().m_61143_(DispenserBlock.f_52659_);
               BlockPos pos = source.m_7961_().m_121945_(direction);
               BlockEntity blockEntity = WorldHelper.getBlockEntity(level, pos);
               Direction sideHit = direction.m_122424_();
               if (blockEntity != null) {
                  Optional capability = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, sideHit).resolve();
                  if (capability.isPresent()) {
                     ((IFluidHandler)capability.get()).fill(new FluidStack(Fluids.f_76193_, 1000), FluidAction.EXECUTE);
                     return stack;
                  }
               }

               BlockState state = level.m_8055_(pos);
               if (state.m_60734_() == Blocks.f_50256_) {
                  level.m_46597_(pos, (BlockState)Blocks.f_152476_.m_49966_().m_61124_(LayeredCauldronBlock.f_153514_, 1));
                  return stack;
               } else if (state.m_60734_() == Blocks.f_152476_) {
                  if (!((LayeredCauldronBlock)state.m_60734_()).m_142596_(state)) {
                     level.m_46597_(pos, (BlockState)state.m_61124_(LayeredCauldronBlock.f_153514_, (Integer)state.m_61143_(LayeredCauldronBlock.f_153514_) + 1));
                     return stack;
                  } else {
                     return super.m_7498_(source, stack);
                  }
               } else {
                  WorldHelper.placeFluid((ServerPlayer)null, level, pos, Fluids.f_76193_, !ProjectEConfig.server.items.opEvertide.get());
                  level.m_6263_((Player)null, (double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_(), (SoundEvent)PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                  return stack;
               }
            }
         });
         CauldronInteraction.f_175606_.put((Item)PEItems.EVERTIDE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
            if (!level.f_46443_) {
               level.m_46597_(pos, (BlockState)Blocks.f_152476_.m_49966_().m_61124_(LayeredCauldronBlock.f_153514_, 1));
            }

            return InteractionResult.m_19078_(level.f_46443_);
         });
         CauldronInteraction.f_175607_.put((Item)PEItems.EVERTIDE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
            if (((LayeredCauldronBlock)state.m_60734_()).m_142596_(state)) {
               return InteractionResult.PASS;
            } else {
               if (!level.f_46443_) {
                  level.m_46597_(pos, (BlockState)state.m_61124_(LayeredCauldronBlock.f_153514_, (Integer)state.m_61143_(LayeredCauldronBlock.f_153514_) + 1));
               }

               return InteractionResult.m_19078_(level.f_46443_);
            }
         });
         CauldronInteraction.f_175606_.put((Item)PEItems.VOLCANITE_AMULET.get(), (state, level, pos, player, hand, stack) -> {
            if (!level.f_46443_ && ItemPE.consumeFuel(player, stack, 32L, true)) {
               level.m_46597_(pos, Blocks.f_152477_.m_49966_());
            }

            return InteractionResult.m_19078_(level.f_46443_);
         });
      });
   }

   private static void registerDispenseBehavior(DispenseItemBehavior behavior, ItemLike... items) {
      ItemLike[] var2 = items;
      int var3 = items.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemLike item = var2[var4];
         DispenserBlock.m_52672_(item, behavior);
      }

   }

   private void imcQueue(InterModEnqueueEvent event) {
      WorldTransmutations.init();
      NSSSerializer.init();
      IntegrationHelper.sendIMCMessages(event);
   }

   private void onConfigLoad(ModConfigEvent configEvent) {
      ModConfig config = configEvent.getConfig();
      if (config.getModId().equals("projecte") && config instanceof PEModConfig peConfig) {
         peConfig.clearCache(configEvent);
      }

   }

   private void tagsUpdated(TagsUpdatedEvent event) {
      if (this.emcUpdateResourceManager != null) {
         long start = System.currentTimeMillis();
         AbstractNSSTag.clearCreatedTags();
         CustomEMCParser.init();

         try {
            EMCMappingHandler.map(this.emcUpdateResourceManager.serverResources(), this.emcUpdateResourceManager.registryAccess(), this.emcUpdateResourceManager.resourceManager());
            LOGGER.info("Registered {} EMC values. (took {} ms)", EMCMappingHandler.getEmcMapSize(), System.currentTimeMillis() - start);
            PacketHandler.sendFragmentedEmcPacketToAll();
         } catch (Throwable var5) {
            LOGGER.error("Error calculating EMC values", var5);
         }

         this.emcUpdateResourceManager = null;
      }

   }

   private void addReloadListeners(AddReloadListenerEvent event) {
      event.addListener((manager) -> {
         this.emcUpdateResourceManager = new EmcUpdateData(event.getServerResources(), event.getRegistryAccess(), manager);
      });
   }

   private void registerCommands(RegisterCommandsEvent event) {
      CommandBuildContext context = event.getBuildContext();
      event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("projecte").requires(PEPermissions.COMMAND)).then(RemoveEmcCMD.register(context))).then(ResetEmcCMD.register(context))).then(SetEmcCMD.register(context))).then(ShowBagCMD.register(context))).then(EMCCMD.register(context))).then(KnowledgeCMD.register(context)));
   }

   private void serverStarting(ServerStartingEvent event) {
      if (!ThreadCheckUUID.hasRunServer()) {
         (new ThreadCheckUUID(true)).start();
      }

   }

   private void serverQuit(ServerStoppedEvent event) {
      CustomEMCParser.flush();
      TransmutationOffline.cleanAll();
      EMCMappingHandler.clearEmcMap();
   }

   private static record EmcUpdateData(ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
      private EmcUpdateData(ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
         this.serverResources = serverResources;
         this.registryAccess = registryAccess;
         this.resourceManager = resourceManager;
      }

      public ReloadableServerResources serverResources() {
         return this.serverResources;
      }

      public RegistryAccess registryAccess() {
         return this.registryAccess;
      }

      public ResourceManager resourceManager() {
         return this.resourceManager;
      }
   }
}
