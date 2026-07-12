package lexis.Server.DogSlayerSword;

import java.awt.Color;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DogSlayerAutoEquip {
   private static final Map lastHandState = new ConcurrentHashMap();
   private static final Map nameHueMap = new ConcurrentHashMap();
   private static final Set equippedPlayers = ConcurrentHashMap.newKeySet();
   private static final Set transitioningPlayers = ConcurrentHashMap.newKeySet();

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase != Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            UUID playerId = player.m_20148_();
            ItemStack mainHand = player.m_21205_();
            boolean hasSword = DogSlayerSword.isDogSlayerSword(mainHand);
            boolean hasBow = LexisBowSkill.isLexisBow(mainHand);
            boolean hasWeapon = hasSword || hasBow;
            boolean isFlying = player.m_21255_();
            Boolean lastHasWeapon = (Boolean)lastHandState.get(playerId);
            if (lastHasWeapon == null) {
               lastHasWeapon = false;
            }

            if (hasWeapon && !lastHasWeapon && !isFlying) {
               equipArmor(player);
               spawnEquipEffect(player);
               equippedPlayers.add(playerId);
            } else if (!hasWeapon && lastHasWeapon && !isFlying && !transitioningPlayers.contains(playerId)) {
               clearArmor(player);
               spawnUnequipEffect(player);
               equippedPlayers.remove(playerId);
            }

            if (hasWeapon) {
               if (hasSword) {
                  updateRainbowName(player, mainHand, "词汇神剑");
               } else if (hasBow) {
                  updateRainbowName(player, mainHand, "词汇仙弓");
                  spawnStarParticle(player);
               }
            }

            lastHandState.put(playerId, hasWeapon);
         }
      }
   }

   private static void spawnStarParticle(ServerPlayer player) {
      if (player.f_19797_ % 5 == 0) {
         ServerLevel level = (ServerLevel)player.m_9236_();
         double x = player.m_20185_();
         double y = player.m_20186_();
         double z = player.m_20189_();
         int starPoints = 5;
         int pointsPerEdge = 10;
         double time = (double)player.f_19797_ * 0.05;
         double speed = 0.02;

         int i;
         double angle;
         double radius;
         double px;
         double pz;
         for(i = 0; i < starPoints; ++i) {
            angle = (double)i * Math.PI * 2.0 / (double)starPoints;
            radius = (double)(i + 1) * Math.PI * 2.0 / (double)starPoints;
            px = x + Math.cos(angle) * 1.8;
            pz = z + Math.sin(angle) * 1.8;
            double x2 = x + Math.cos(angle + Math.PI / (double)starPoints) * 0.8;
            double z2 = z + Math.sin(angle + Math.PI / (double)starPoints) * 0.8;

            int nextIdx;
            double nextAngle;
            double x3;
            double z3;
            for(nextIdx = 0; nextIdx <= pointsPerEdge; ++nextIdx) {
               nextAngle = (double)nextIdx / (double)pointsPerEdge;
               x3 = px + (x2 - px) * nextAngle;
               z3 = pz + (z2 - pz) * nextAngle;
               double rotAngle = (double)player.f_19797_ * speed;
               double rx = x + (x3 - x) * Math.cos(rotAngle) - (z3 - z) * Math.sin(rotAngle);
               double rz = z + (x3 - x) * Math.sin(rotAngle) + (z3 - z) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)nextIdx * 0.05) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, y + 0.1, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
            }

            nextIdx = (i + 1) % starPoints;
            nextAngle = (double)nextIdx * Math.PI * 2.0 / (double)starPoints;
            x3 = x + Math.cos(nextAngle) * 1.8;
            z3 = z + Math.sin(nextAngle) * 1.8;

            for(int j = 0; j <= pointsPerEdge; ++j) {
               double t = (double)j / (double)pointsPerEdge;
               double px = x2 + (x3 - x2) * t;
               double pz = z2 + (z3 - z2) * t;
               double rotAngle = (double)player.f_19797_ * speed;
               double rx = x + (px - x) * Math.cos(rotAngle) - (pz - z) * Math.sin(rotAngle);
               double rz = z + (px - x) * Math.sin(rotAngle) + (pz - z) * Math.cos(rotAngle);
               float hue = (float)((time + (double)i * 0.2 + (double)j * 0.05 + 0.5) % 1.0);
               int rgb = Color.HSBtoRGB(hue, 0.9F, 1.0F);
               float r = (float)(rgb >> 16 & 255) / 255.0F;
               float g = (float)(rgb >> 8 & 255) / 255.0F;
               float b = (float)(rgb & 255) / 255.0F;
               String dustCommand = String.format("particle dust %.2f %.2f %.2f 0.27 %f %f %f 0 0 0 0 1 force", r, g, b, rx, y + 0.15, rz);
               level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
            }
         }

         int rgb;
         float g;
         float hue;
         float r;
         float b;
         String dustCommand;
         for(i = 0; i < 32; ++i) {
            angle = (double)player.f_19797_ * speed * 0.5 + (double)i * Math.PI * 2.0 / 32.0;
            radius = 2.2;
            px = x + Math.cos(angle) * radius;
            pz = z + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.03) % 1.0);
            rgb = Color.HSBtoRGB(hue, 0.8F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            dustCommand = String.format("particle dust %.2f %.2f %.2f 0.4 %f %f %f 0 0 0 0 1 force", r, g, b, px, y + 0.05, pz);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }

         for(i = 0; i < 5; ++i) {
            angle = (double)player.f_19797_ * speed * 1.5 + (double)i * Math.PI * 2.0 / 5.0;
            radius = 1.0;
            px = x + Math.cos(angle) * radius;
            pz = z + Math.sin(angle) * radius;
            hue = (float)((time + (double)i * 0.2 + 0.3) % 1.0);
            rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
            r = (float)(rgb >> 16 & 255) / 255.0F;
            g = (float)(rgb >> 8 & 255) / 255.0F;
            b = (float)(rgb & 255) / 255.0F;
            dustCommand = String.format("particle dust %.2f %.2f %.2f 0.6 %f %f %f 0 0 0 0 1 force", r, g, b, px, y + 0.2, pz);
            level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), dustCommand);
         }

      }
   }

   private static void updateRainbowName(ServerPlayer player, ItemStack item, String name) {
      UUID playerId = player.m_20148_();
      float hue = (Float)nameHueMap.getOrDefault(playerId, 0.0F);
      hue += 0.01F;
      if (hue > 1.0F) {
         hue = 0.0F;
      }

      nameHueMap.put(playerId, hue);
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      String hexColor = String.format("#%06X", rgb & 16777215);
      Component rainbowName = Component.m_237113_(name).m_6270_(Style.f_131099_.m_131148_(TextColor.m_131268_(hexColor)).m_131136_(true));
      item.m_41714_(rainbowName);
   }

   private static void equipArmor(ServerPlayer player) {
      ItemStack helmet = createArmorPiece(Items.f_42464_, "词汇头盔");
      player.m_8061_(EquipmentSlot.HEAD, helmet);
      ItemStack chestplate = createArmorPiece(Items.f_42741_, "词汇神翹");
      player.m_8061_(EquipmentSlot.CHEST, chestplate);
      ItemStack leggings = createArmorPiece(Items.f_42466_, "词汇护腿");
      player.m_8061_(EquipmentSlot.LEGS, leggings);
      ItemStack boots = createArmorPiece(Items.f_42467_, "词汇靴子");
      player.m_8061_(EquipmentSlot.FEET, boots);
   }

   private static ItemStack createArmorPiece(Item item, String name) {
      ItemStack stack = new ItemStack(item);
      stack.m_41663_(Enchantments.f_44975_, 1);
      stack.m_41663_(Enchantments.f_44963_, 1);
      stack.m_41663_(Enchantments.f_44986_, 10);
      CompoundTag tag = stack.m_41784_();
      tag.m_128379_("Unbreakable", true);
      tag.m_128405_("HideFlags", 1);
      stack.m_41751_(tag);
      stack.m_41714_(Component.m_237113_(name));
      return stack;
   }

   private static void clearArmor(ServerPlayer player) {
      player.m_8061_(EquipmentSlot.HEAD, ItemStack.f_41583_);
      player.m_8061_(EquipmentSlot.CHEST, ItemStack.f_41583_);
      player.m_8061_(EquipmentSlot.LEGS, ItemStack.f_41583_);
      player.m_8061_(EquipmentSlot.FEET, ItemStack.f_41583_);
   }

   private static void updateRainbowNames(ServerPlayer player) {
      UUID playerId = player.m_20148_();
      float hue = (Float)nameHueMap.getOrDefault(playerId, 0.0F);
      hue += 0.01F;
      if (hue > 1.0F) {
         hue = 0.0F;
      }

      nameHueMap.put(playerId, hue);
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      String hexColor = String.format("#%06X", rgb & 16777215);
      updateItemRainbowName(player.m_6844_(EquipmentSlot.HEAD), "词汇头盔", hexColor);
      updateItemRainbowName(player.m_6844_(EquipmentSlot.CHEST), "词汇神翹", hexColor);
      updateItemRainbowName(player.m_6844_(EquipmentSlot.LEGS), "词汇护腿", hexColor);
      updateItemRainbowName(player.m_6844_(EquipmentSlot.FEET), "词汇靴子", hexColor);
   }

   private static void updateItemRainbowName(ItemStack stack, String baseName, String hexColor) {
      if (!stack.m_41619_()) {
         Component rainbowName = Component.m_237113_(baseName).m_6270_(Style.f_131099_.m_131148_(TextColor.m_131268_(hexColor)).m_131136_(true));
         stack.m_41714_(rainbowName);
      }
   }

   private static void spawnEquipEffect(ServerPlayer player) {
      ServerLevel level = (ServerLevel)player.m_9236_();
      double x = player.m_20185_();
      double y = player.m_20186_();
      double z = player.m_20189_();
      Random rand = new Random();
      float r = rand.nextFloat();
      float g = rand.nextFloat();
      float b = rand.nextFloat();
      String cmd = String.format("particle dust %.2f %.2f %.2f 0.31 %f %f %f 0.29 0.54 0.29 0 1400 force", r, g, b, x, y + 1.0, z);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
   }

   private static void spawnUnequipEffect(ServerPlayer player) {
      ServerLevel level = (ServerLevel)player.m_9236_();
      double x = player.m_20185_();
      double y = player.m_20186_();
      double z = player.m_20189_();
      Random rand = new Random();
      float r = rand.nextFloat();
      float g = rand.nextFloat();
      float b = rand.nextFloat();
      String cmd = String.format("particle dust %.2f %.2f %.2f 0.31 %f %f %f 0.29 0.54 0.29 0 1900 force", r, g, b, x, y + 1.0, z);
      level.m_7654_().m_129892_().m_230957_(level.m_7654_().m_129893_().m_81324_(), cmd);
   }
}
