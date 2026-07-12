package lexis.Server.DogSlayerSword;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DogSlayerSword {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final String SWORD_TAG = "lexissword";
   private static final byte SWORD_ACTIVE = 1;
   private static boolean isEnabled = false;
   private static final Set swordEntities = new HashSet();
   private static final Map heartTasks = new ConcurrentHashMap();
   private static final Set fastMovePlayers = ConcurrentHashMap.newKeySet();
   private static final Map nameHueMap = new ConcurrentHashMap();

   public static boolean isInFastMove(UUID playerId) {
      return fastMovePlayers.contains(playerId);
   }

   public static void setFastMoveState(UUID playerId, boolean state) {
      if (state) {
         fastMovePlayers.add(playerId);
      } else {
         fastMovePlayers.remove(playerId);
      }

   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("dogslayer").requires((source) -> {
         Entity patt3013$temp = source.m_81373_();
         if (!(patt3013$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82127_("on").executes((context) -> {
         isEnabled = true;
         ServerPlayer player = (ServerPlayer)((CommandSourceStack)context.getSource()).m_81373_();
         giveDogSlayerSword(player);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f词汇神剑开启，词汇神剑已给予！其他玩家无法拿起");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("off").executes((context) -> {
         isEnabled = false;
         int count = removeAllDogSlayerSwords(((CommandSourceStack)context.getSource()).m_81377_());
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f词汇神剑关闭，已清除 " + count + " 个词汇神剑");
         }, true);
         return 1;
      })))));
   }

   private static void giveDogSlayerSword(ServerPlayer player) {
      ItemStack sword = new ItemStack(Items.f_42430_);
      CompoundTag tag = sword.m_41784_();
      tag.m_128344_("lexissword", (byte)1);
      sword.m_41714_(Component.m_237113_("§6§l词汇神剑"));
      sword.m_41663_(Enchantments.f_44977_, 10);
      sword.m_41663_(Enchantments.f_44981_, 2);
      sword.m_41663_(Enchantments.f_44980_, 3);
      sword.m_41663_(Enchantments.f_44986_, 3);
      CompoundTag display = new CompoundTag();
      ListTag loreList = new ListTag();
      loreList.add(StringTag.m_129297_("§c§l你有病吧你怎么看到这lore？你是不是安装ibe+指令data 偷看？"));
      display.m_128365_("Lore", loreList);
      tag.m_128365_("display", display);
      tag.m_128405_("HideFlags", 1);
      tag.m_128379_("Glowing", true);
      sword.m_41751_(tag);
      if (!player.m_150109_().m_36054_(sword)) {
         player.m_36176_(sword, false);
      }

      player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§a你取得了词汇神剑！"));
   }

   public static ItemStack createLexisSword() {
      ItemStack sword = new ItemStack(Items.f_42430_);
      CompoundTag tag = sword.m_41784_();
      tag.m_128344_("lexissword", (byte)1);
      sword.m_41663_(Enchantments.f_44977_, 10);
      sword.m_41663_(Enchantments.f_44981_, 2);
      sword.m_41663_(Enchantments.f_44980_, 3);
      sword.m_41663_(Enchantments.f_44986_, 3);
      tag.m_128405_("HideFlags", 1);
      tag.m_128379_("Glowing", true);
      sword.m_41751_(tag);
      sword.m_41714_(Component.m_237113_("§6§l词汇神剑"));
      return sword;
   }

   private static int removeAllDogSlayerSwords(MinecraftServer server) {
      int count = 0;
      Iterator var2 = server.m_129785_().iterator();

      while(var2.hasNext()) {
         ServerLevel level = (ServerLevel)var2.next();
         AABB infiniteAABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
         Iterator var5 = level.m_45976_(ItemEntity.class, infiniteAABB).iterator();

         while(var5.hasNext()) {
            ItemEntity itemEntity = (ItemEntity)var5.next();
            if (isDogSlayerSword(itemEntity.m_32055_())) {
               itemEntity.m_146870_();
               ++count;
            }
         }

         var5 = level.m_6907_().iterator();

         while(var5.hasNext()) {
            ServerPlayer player = (ServerPlayer)var5.next();

            for(int i = 0; i < player.m_150109_().m_6643_(); ++i) {
               ItemStack stack = player.m_150109_().m_8020_(i);
               if (isDogSlayerSword(stack)) {
                  player.m_150109_().m_6836_(i, ItemStack.f_41583_);
                  ++count;
               }
            }
         }
      }

      return count;
   }

   static boolean isDogSlayerSword(ItemStack stack) {
      if (!stack.m_41619_() && stack.m_41720_() == Items.f_42430_) {
         CompoundTag tag = stack.m_41783_();
         return tag != null && tag.m_128441_("lexissword") && tag.m_128445_("lexissword") == 1;
      } else {
         return false;
      }
   }

   private static boolean isOwner(ServerPlayer player) {
      return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
   }

   @SubscribeEvent
   public static void onAttackEntity(AttackEntityEvent event) {
      if (isEnabled) {
         Player player = event.getEntity();
         if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (!isOwner(serverPlayer)) {
               if (isDogSlayerSword(player.m_21205_())) {
                  serverPlayer.m_6469_(serverPlayer.m_269291_().m_269425_(), Float.MAX_VALUE);
                  serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你不是房主，无法使用词汇神剑！"));
                  ItemStack sword = player.m_21205_().m_41777_();
                  player.m_21205_().m_41764_(0);
                  player.m_36176_(sword, true);
               }

            } else if (isDogSlayerSword(player.m_21205_())) {
               Entity target = event.getTarget();
               if (target != null) {
                  if (!target.m_9236_().f_46443_) {
                     ServerLevel level = (ServerLevel)target.m_9236_();
                     BlockPos pos = target.m_20183_();
                     if (target instanceof ServerPlayer) {
                        ServerPlayer targetPlayer = (ServerPlayer)target;
                        forceKillPlayer(targetPlayer, serverPlayer);
                     } else {
                        forceKillAnyEntity(target);
                     }

                     drawHeartEffect(level, pos, serverPlayer);
                     drawRecommendedKillEffect(level, pos);
                     level.m_5594_((Player)null, pos, SoundEvents.f_11860_, SoundSource.PLAYERS, 1.0F, 1.0F);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onEntityItemPickup(EntityItemPickupEvent event) {
      if (isEnabled) {
         Player var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            ItemStack var3 = event.getItem().m_32055_();
            if (isDogSlayerSword(var3)) {
               if (!isOwner(player)) {
                  event.setCanceled(true);
                  player.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c只有房主才能拿起词汇神剑！"));
                  event.getItem().m_146915_(true);
                  event.getItem().m_32062_();
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onItemEntityTick(TickEvent.LevelTickEvent event) {
      if (isEnabled) {
         if (event.phase != Phase.END) {
            Level var2 = event.level;
            if (var2 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var2;
               AABB infiniteAABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
               Iterator var3 = level.m_45976_(ItemEntity.class, infiniteAABB).iterator();

               while(true) {
                  ItemEntity itemEntity;
                  do {
                     if (!var3.hasNext()) {
                        return;
                     }

                     itemEntity = (ItemEntity)var3.next();
                  } while(!isDogSlayerSword(itemEntity.m_32055_()));

                  itemEntity.m_146915_(true);
                  itemEntity.m_149678_();
                  Iterator var5 = level.m_6907_().iterator();

                  while(var5.hasNext()) {
                     ServerPlayer player = (ServerPlayer)var5.next();
                     if (player.m_20280_(itemEntity) < 16.0 && !isOwner(player)) {
                        itemEntity.m_32062_();
                     }
                  }

                  swordEntities.add(itemEntity.m_20148_());
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (isEnabled) {
         if (event.phase != Phase.END) {
            Player var2 = event.player;
            if (var2 instanceof ServerPlayer) {
               ServerPlayer serverPlayer = (ServerPlayer)var2;
               ItemStack mainHand = serverPlayer.m_21205_();
               if (isOwner(serverPlayer) && isDogSlayerSword(mainHand)) {
                  updateRainbowName(serverPlayer, mainHand);
               } else if (isDogSlayerSword(mainHand)) {
                  serverPlayer.m_6469_(serverPlayer.m_269291_().m_269425_(), Float.MAX_VALUE);
                  serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你不是房主，禁止使用词汇神剑技能了"));
                  ItemStack sword = mainHand.m_41777_();
                  mainHand.m_41764_(0);
                  serverPlayer.m_36176_(sword, true);
               }

               for(int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
                  ItemStack stack = serverPlayer.m_150109_().m_8020_(i);
                  if (isDogSlayerSword(stack) && !isOwner(serverPlayer)) {
                     serverPlayer.m_150109_().m_6836_(i, ItemStack.f_41583_);
                     serverPlayer.m_213846_(Component.m_237113_("§c[§6Lexis-Server§c] §f§c你的背包词汇神剑已经被自动扔了 你无法使用"));
                     ItemStack droppedSword = stack.m_41777_();
                     serverPlayer.m_9236_().m_7967_(new ItemEntity(serverPlayer.m_9236_(), serverPlayer.m_20185_(), serverPlayer.m_20186_(), serverPlayer.m_20189_(), droppedSword));
                  }
               }

            }
         }
      }
   }

   private static void updateRainbowName(ServerPlayer player, ItemStack sword) {
      UUID playerId = player.m_20148_();
      float hue = (Float)nameHueMap.getOrDefault(playerId, 0.0F);
      hue += 0.01F;
      if (hue > 1.0F) {
         hue = 0.0F;
      }

      nameHueMap.put(playerId, hue);
      int rgb = Color.HSBtoRGB(hue, 1.0F, 1.0F);
      String hexColor = String.format("#%06X", rgb & 16777215);
      Component rainbowName = Component.m_237113_("词汇神剑").m_6270_(Style.f_131099_.m_131148_(TextColor.m_131268_(hexColor)).m_131136_(true));
      sword.m_41714_(rainbowName);
   }

   private static void forceKillAnyEntity(Entity target) {
      if (target instanceof LivingEntity living) {
         living.m_21153_(0.0F);
         living.m_6469_(new DamageSource(target.m_9236_().m_269111_().m_269264_().m_269150_()), Float.MAX_VALUE);
         living.m_6667_(new DamageSource(target.m_9236_().m_269111_().m_269264_().m_269150_()));
         if (!living.m_21224_()) {
            living.m_6074_();
         }
      } else {
         target.m_142687_(RemovalReason.KILLED);
      }

   }

   private static void forceKillPlayer(ServerPlayer target, ServerPlayer killer) {
      target.m_21153_(0.0F);
      DamageSource damageSource = new DamageSource(target.m_9236_().m_269111_().m_269264_().m_269150_(), killer);
      target.m_6469_(damageSource, Float.MAX_VALUE);
      target.m_6667_(damageSource);
      if (!target.m_21224_()) {
         target.m_6074_();
      }

      PlayerList var10000 = target.m_9236_().m_7654_().m_6846_();
      String var10001 = target.m_7755_().getString();
      var10000.m_240416_(Component.m_237113_("§c" + var10001 + " 被 §6" + killer.m_7755_().getString() + " §c用词汇神剑斩杀了！"), false);
   }

   private static void drawHeartEffect(ServerLevel level, BlockPos pos, ServerPlayer player) {
      double x = (double)pos.m_123341_() + 0.5;
      double y = (double)pos.m_123342_() + 1.0;
      double z = (double)pos.m_123343_() + 0.5;
      CommandSourceStack silentSource = level.m_7654_().m_129893_().m_81324_();
      String summonCommand = String.format("summon minecraft:snowball %f %f %f {Glowing:1b,Item:{Count:1b,id:\"minecraft:nether_star\"},NoGravity:true,Tags:[\"lexis_heart\"]}", x, y + 1.5, z);
      level.m_7654_().m_129892_().m_230957_(silentSource, summonCommand);
      HeartParticleTask task = new HeartParticleTask(level, player, x, y, z);
      heartTasks.put(player.m_20148_(), task);
      task.start();
   }

   private static void drawRecommendedKillEffect(ServerLevel level, BlockPos pos) {
      double x = (double)pos.m_123341_() + 0.5;
      double y = (double)pos.m_123342_() + 1.0;
      double z = (double)pos.m_123343_() + 0.5;

      int i;
      for(i = 0; i < 24; ++i) {
         double angle = (double)i * Math.PI * 2.0 / 24.0;
         double radius = 2.0;
         double dx = Math.cos(angle) * radius;
         double dz = Math.sin(angle) * radius;
         level.m_8767_(ParticleTypes.f_123810_, x + dx, y, z + dz, 1, 0.0, 0.0, 0.0, 0.0);
      }

      for(i = -4; i <= 4; ++i) {
         level.m_8767_(ParticleTypes.f_123766_, x + (double)i * 0.6, y, z, 1, 0.0, 0.0, 0.0, 0.0);
         level.m_8767_(ParticleTypes.f_123766_, x, y + (double)i * 0.4, z, 1, 0.0, 0.0, 0.0, 0.0);
         level.m_8767_(ParticleTypes.f_123766_, x, y, z + (double)i * 0.6, 1, 0.0, 0.0, 0.0, 0.0);
      }

      level.m_8767_(ParticleTypes.f_123815_, x, y + 0.5, z, 20, 0.5, 0.5, 0.5, 0.2);
      level.m_8767_(ParticleTypes.f_123813_, x, y + 0.5, z, 1, 0.0, 0.0, 0.0, 0.0);
   }

   @SubscribeEvent
   public static void onLivingHurt(LivingHurtEvent event) {
      if (isEnabled) {
         LivingEntity var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            if (isOwner(player)) {
               ItemStack mainHand = player.m_21205_();
               ItemStack offHand = player.m_21206_();
               if (isDogSlayerSword(mainHand) || isDogSlayerSword(offHand)) {
                  event.setCanceled(true);
                  player.m_9236_().m_46796_(2003, player.m_20183_(), 0);
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onLivingDeath(LivingDeathEvent event) {
      if (isEnabled) {
         LivingEntity var2 = event.getEntity();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            if (isOwner(player)) {
               ItemStack mainHand = player.m_21205_();
               ItemStack offHand = player.m_21206_();
               if (isDogSlayerSword(mainHand) || isDogSlayerSword(offHand)) {
                  event.setCanceled(true);
                  player.m_21153_(player.m_21233_());
                  player.m_9236_().m_46796_(2003, player.m_20183_(), 0);
               }

            }
         }
      }
   }

   private static class HeartParticleTask {
      private final ServerLevel level;
      private final ServerPlayer player;
      private final double heartX;
      private final double heartY;
      private final double heartZ;
      private Timer timer;
      private int ticks = 0;

      public HeartParticleTask(ServerLevel level, ServerPlayer player, double x, double y, double z) {
         this.level = level;
         this.player = player;
         this.heartX = x;
         this.heartY = y;
         this.heartZ = z;
      }

      public void start() {
         this.timer = new Timer();
         this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
               if (HeartParticleTask.this.ticks >= 60) {
                  HeartParticleTask.this.end();
               } else {
                  HeartParticleTask.this.level.m_7654_().execute(() -> {
                     HeartParticleTask.this.update();
                  });
                  ++HeartParticleTask.this.ticks;
               }
            }
         }, 0L, 50L);
      }

      private void update() {
         CommandSourceStack silentSource = this.level.m_7654_().m_129893_().m_81324_();
         float r = (float)Math.random();
         float g = (float)Math.random();
         float b = (float)Math.random();
         double playerX = this.player.m_20185_();
         double playerY = this.player.m_20186_() + 1.0;
         double playerZ = this.player.m_20189_();
         double dx = playerX - this.heartX;
         double dy = playerY - (this.heartY + 1.5);
         double dz = playerZ - this.heartZ;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         int steps = Math.max(10, (int)(distance * 3.0));

         int i;
         double angle;
         double radius;
         for(i = 1; i <= steps; ++i) {
            angle = (double)i / (double)steps;
            radius = this.heartX + dx * angle;
            double py = this.heartY + 1.5 + dy * angle;
            double pz = this.heartZ + dz * angle;
            String particleCommand = String.format("particle dust %.2f %.2f %.2f 0.4 %f %f %f 0 0 0 0 1 force", r, g, b, radius, py, pz);
            this.level.m_7654_().m_129892_().m_230957_(silentSource, particleCommand);
         }

         for(i = 0; i < 4; ++i) {
            angle = ((double)this.ticks * 0.2 + (double)i * Math.PI / 2.0) % 6.283185307179586;
            radius = 1.0;
            String glowCommand = String.format("particle dust %.2f %.2f %.2f 0.6 %f %f %f 0 0 0 0 1 force", r, g, b, this.heartX + Math.cos(angle) * radius, this.heartY + 1.5 + Math.sin(angle) * radius * 0.5, this.heartZ + Math.sin(angle) * radius);
            this.level.m_7654_().m_129892_().m_230957_(silentSource, glowCommand);
         }

      }

      private void end() {
         CommandSourceStack silentSource = this.level.m_7654_().m_129893_().m_81324_();
         String clearCommand = String.format("execute positioned %f %f %f run kill @e[type=minecraft:snowball,tag=lexis_heart,distance=..2]", this.heartX, this.heartY + 1.5, this.heartZ);
         this.level.m_7654_().m_129892_().m_230957_(silentSource, clearCommand);
         String extraClear = "kill @e[type=minecraft:snowball,tag=lexis_heart]";
         this.level.m_7654_().m_129892_().m_230957_(silentSource, extraClear);
         String totemCommand = String.format("particle minecraft:totem_of_undying %f %f %f 0 0 0 0.5 64 force", this.heartX, this.heartY + 1.5, this.heartZ);
         this.level.m_7654_().m_129892_().m_230957_(silentSource, totemCommand);
         String explosionCommand = String.format("particle minecraft:explosion %f %f %f 0.5 0.5 0.5 0.1 10 force", this.heartX, this.heartY + 1.5, this.heartZ);
         this.level.m_7654_().m_129892_().m_230957_(silentSource, explosionCommand);
         this.timer.cancel();
         DogSlayerSword.heartTasks.remove(this.player.m_20148_());
      }
   }
}
