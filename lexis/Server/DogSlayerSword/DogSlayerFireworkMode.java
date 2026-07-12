package lexis.Server.DogSlayerSword;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DogSlayerFireworkMode {
   private static final String FIREWORK_TAG = "lexisfice";
   private static final Map flyingModeMap = new ConcurrentHashMap();
   private static final Map fireworkTimer = new ConcurrentHashMap();
   private static final Map particleActive = new ConcurrentHashMap();
   private static final Map particleHueMap = new ConcurrentHashMap();

   public static Boolean getFlyingState(UUID playerId) {
      return (Boolean)flyingModeMap.get(playerId);
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase != Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            UUID playerId = player.m_20148_();
            boolean isFlying = player.m_21255_();
            Boolean lastFlying = (Boolean)flyingModeMap.get(playerId);
            if (lastFlying == null) {
               lastFlying = false;
            }

            if (isFlying && !lastFlying) {
               replaceSwordWithFirework(player);
            } else if (!isFlying && lastFlying) {
               replaceFireworkWithSword(player);
               particleActive.remove(playerId);
               fireworkTimer.remove(playerId);
               particleHueMap.remove(playerId);
            }

            flyingModeMap.put(playerId, isFlying);
            if (particleActive.containsKey(playerId)) {
               int timer = (Integer)fireworkTimer.getOrDefault(playerId, 0);
               if (timer > 0) {
                  spawnRainbowDustParticle(player);
                  fireworkTimer.put(playerId, timer - 1);
               } else {
                  particleActive.remove(playerId);
                  fireworkTimer.remove(playerId);
                  particleHueMap.remove(playerId);
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
      if (!event.getLevel().f_46443_) {
         Player var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            ItemStack stack = player.m_21205_();
            if (isLexisFirework(stack) && player.m_21255_()) {
               launchFirework(player, stack);
               event.setCanceled(true);
            }

         }
      }
   }

   private static void replaceSwordWithFirework(ServerPlayer player) {
      for(int i = 0; i < player.m_150109_().m_6643_(); ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (DogSlayerSword.isDogSlayerSword(stack) || LexisBowSkill.isLexisBow(stack)) {
            ItemStack firework = createLexisFirework();
            player.m_150109_().m_6836_(i, firework);
            break;
         }
      }

   }

   private static void replaceFireworkWithSword(ServerPlayer player) {
      int currentMode = DogSlayerSkill.getCurrentMode(player.m_20148_());

      for(int i = 0; i < player.m_150109_().m_6643_(); ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (isLexisFirework(stack)) {
            ItemStack weapon;
            if (currentMode == 3) {
               weapon = LexisBowSkill.createLexisBow();
            } else {
               weapon = DogSlayerSword.createLexisSword();
            }

            player.m_150109_().m_6836_(i, weapon);
            break;
         }
      }

   }

   private static ItemStack createLexisFirework() {
      ItemStack firework = new ItemStack(Items.f_42688_);
      CompoundTag tag = new CompoundTag();
      tag.m_128344_("lexisfice", (byte)1);
      CompoundTag fireworksTag = new CompoundTag();
      fireworksTag.m_128344_("Flight", (byte)3);
      ListTag explosions = new ListTag();
      fireworksTag.m_128365_("Explosions", explosions);
      tag.m_128365_("Fireworks", fireworksTag);
      firework.m_41751_(tag);
      firework.m_41714_(Component.m_237113_("§d§l词汇烟花"));
      return firework;
   }

   private static ItemStack createLexisSword() {
      ItemStack sword = new ItemStack(Items.f_42430_);
      CompoundTag tag = sword.m_41784_();
      tag.m_128344_("lexissword", (byte)1);
      sword.m_41663_(Enchantments.f_44986_, 3);
      tag.m_128405_("HideFlags", 1);
      sword.m_41751_(tag);
      sword.m_41714_(Component.m_237113_("§6§l词汇神剑"));
      return sword;
   }

   private static boolean isLexisFirework(ItemStack stack) {
      if (!stack.m_41619_() && stack.m_41720_() == Items.f_42688_) {
         CompoundTag tag = stack.m_41783_();
         return tag != null && tag.m_128441_("lexisfice") && tag.m_128445_("lexisfice") == 1;
      } else {
         return false;
      }
   }

   private static void launchFirework(ServerPlayer player, ItemStack firework) {
      Level level = player.m_9236_();
      FireworkRocketEntity rocket = new FireworkRocketEntity(level, firework, player);
      level.m_7967_(rocket);
      particleActive.put(player.m_20148_(), true);
      fireworkTimer.put(player.m_20148_(), 60);
   }

   private static void spawnRainbowDustParticle(ServerPlayer player) {
      ServerLevel level = (ServerLevel)player.m_9236_();
      double x = player.m_20185_();
      double y = player.m_20186_();
      double z = player.m_20189_();
      UUID playerId = player.m_20148_();
      float hue = (Float)particleHueMap.getOrDefault(playerId, 0.0F);
      hue += 0.02F;
      if (hue > 1.0F) {
         hue = 0.0F;
      }

      particleHueMap.put(playerId, hue);
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      float r = (float)(rgb >> 16 & 255) / 255.0F;
      float g = (float)(rgb >> 8 & 255) / 255.0F;
      float b = (float)(rgb & 255) / 255.0F;
      String cmd = String.format("/particle dust %.2f %.2f %.2f 0.6 %f %f %f 1.9 1.9 1.9 0 250 force", r, g, b, x, y + 1.0, z);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
   }

   public static void clearPlayerData(UUID playerId) {
      flyingModeMap.remove(playerId);
      fireworkTimer.remove(playerId);
      particleActive.remove(playerId);
      particleHueMap.remove(playerId);
   }
}
