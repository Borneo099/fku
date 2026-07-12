package moze_intel.projecte.events;

import java.util.Objects;
import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.capability.managing.BasicCapabilityResolver;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import moze_intel.projecte.handlers.CommonInternalAbilities;
import moze_intel.projecte.handlers.InternalAbilities;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.server.ServerLifecycleHooks;

@EventBusSubscriber(
   modid = "projecte"
)
public class PlayerEvents {
   @SubscribeEvent
   public static void cloneEvent(PlayerEvent.Clone event) {
      Player original = event.getOriginal();
      original.reviveCaps();
      original.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((old) -> {
         CompoundTag bags = (CompoundTag)old.serializeNBT();
         event.getEntity().getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((c) -> {
            c.deserializeNBT(bags);
         });
      });
      original.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((old) -> {
         CompoundTag knowledge = (CompoundTag)old.serializeNBT();
         event.getEntity().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((c) -> {
            c.deserializeNBT(knowledge);
         });
      });
      original.invalidateCaps();
   }

   @SubscribeEvent
   public static void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((c) -> {
            c.sync(player);
         });
         player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((c) -> {
            c.sync((DyeColor)null, player);
         });
      }

   }

   @SubscribeEvent
   public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((c) -> {
            c.sync(player);
         });
         player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((c) -> {
            c.sync((DyeColor)null, player);
         });
      }

      event.getEntity().getCapability(InternalAbilities.CAPABILITY).ifPresent(InternalAbilities::onDimensionChange);
   }

   @SubscribeEvent
   public static void attachCaps(AttachCapabilitiesEvent evt) {
      Object var2 = evt.getObject();
      if (var2 instanceof Player player) {
         attachCapability(evt, AlchBagImpl.Provider.NAME, new AlchBagImpl.Provider());
         attachCapability(evt, KnowledgeImpl.Provider.NAME, new KnowledgeImpl.Provider(player));
         attachCapability(evt, CommonInternalAbilities.NAME, new CommonInternalAbilities.Provider(player));
         if (player instanceof ServerPlayer serverPlayer) {
            attachCapability(evt, InternalTimers.NAME, new InternalTimers.Provider());
            attachCapability(evt, InternalAbilities.NAME, new InternalAbilities.Provider(serverPlayer));
         }
      }

   }

   private static void attachCapability(AttachCapabilitiesEvent evt, ResourceLocation name, BasicCapabilityResolver cap) {
      evt.addCapability(name, cap);
      Objects.requireNonNull(cap);
      evt.addListener(cap::invalidateAll);
   }

   @SubscribeEvent
   public static void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
      ServerPlayer player = (ServerPlayer)event.getEntity();
      PacketHandler.sendFragmentedEmcPacket(player);
      player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent((knowledge) -> {
         knowledge.sync(player);
         PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
      });
      player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).ifPresent((c) -> {
         c.sync((DyeColor)null, player);
      });
      PECore.debugLog("Sent knowledge and bag data to {}", player.m_7755_());
   }

   @SubscribeEvent
   public static void onConstruct(EntityEvent.EntityConstructing evt) {
      if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && evt.getEntity() instanceof Player && !(evt.getEntity() instanceof FakePlayer)) {
         TransmutationOffline.clear(evt.getEntity().m_20148_());
         PECore.debugLog("Clearing offline data cache in preparation to load online data");
      }

   }

   @SubscribeEvent
   public static void onHighAlchemistJoin(PlayerEvent.PlayerLoggedInEvent evt) {
      if (PECore.uuids.contains(evt.getEntity().m_20148_().toString())) {
         Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, new Object[]{ChatFormatting.GOLD, evt.getEntity().m_5446_()});
         ServerLifecycleHooks.getCurrentServer().m_6846_().m_240416_(joinMessage, false);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void pickupItem(EntityItemPickupEvent event) {
      Player player = event.getEntity();
      Level level = player.m_9236_();
      if (!level.f_46443_) {
         ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.m_150109_().f_35974_);
         if (!bag.m_41619_()) {
            Optional cap = player.getCapability(PECapabilities.ALCH_BAG_CAPABILITY).resolve();
            if (!cap.isEmpty()) {
               IItemHandler handler = ((IAlchBagProvider)cap.get()).getBag(((AlchemicalBag)bag.m_41720_()).color);
               ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, event.getItem().m_32055_(), false);
               if (remainder.m_41619_()) {
                  event.getItem().m_146870_();
                  level.m_6263_((Player)null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_12019_, SoundSource.PLAYERS, 0.2F, ((level.f_46441_.m_188501_() - level.f_46441_.m_188501_()) * 0.7F + 1.0F) * 2.0F);
                  ((ServerPlayer)player).f_8906_.m_9829_(new ClientboundTakeItemEntityPacket(event.getItem().m_19879_(), player.m_19879_(), 1));
               } else {
                  event.getItem().m_32045_(remainder);
               }

               event.setCanceled(true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onAttacked(LivingAttackEvent evt) {
      LivingEntity var2 = evt.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (evt.getSource().m_269533_(DamageTypeTags.f_268745_) && TickEvents.shouldPlayerResistFire(player)) {
            evt.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public static void onLivingHurt(LivingHurtEvent evt) {
      float damage = evt.getAmount();
      if (damage > 0.0F) {
         LivingEntity entityLiving = evt.getEntity();
         DamageSource source = evt.getSource();
         float totalPercentReduced = getReductionForSlot(entityLiving, source, EquipmentSlot.HEAD, damage) + getReductionForSlot(entityLiving, source, EquipmentSlot.CHEST, damage) + getReductionForSlot(entityLiving, source, EquipmentSlot.LEGS, damage) + getReductionForSlot(entityLiving, source, EquipmentSlot.FEET, damage);
         float damageAfter = totalPercentReduced >= 1.0F ? 0.0F : damage - damage * totalPercentReduced;
         if (damageAfter <= 0.0F) {
            evt.setCanceled(true);
         } else if (damage != damageAfter) {
            evt.setAmount(damageAfter);
         }
      }

   }

   private static float getReductionForSlot(LivingEntity entityLiving, DamageSource source, EquipmentSlot slot, float damage) {
      ItemStack armorStack = entityLiving.m_6844_(slot);
      Item var6 = armorStack.m_41720_();
      if (var6 instanceof PEArmor armorItem) {
         ArmorItem.Type type = armorItem.m_266204_();
         return type.m_266308_() != slot ? 0.0F : Math.max(armorItem.getFullSetBaseReduction(), armorItem.getMaxDamageAbsorb(type, source) / damage) * armorItem.getPieceEffectiveness(type);
      } else {
         return 0.0F;
      }
   }
}
