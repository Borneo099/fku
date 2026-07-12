package lexis.Server.IllegalEntity;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber
public class IllegalEntityCommand {
   private static final String PREFIX = "§c[§6Lexis-Server§c] §f";
   private static final Set detectedEntities = Collections.newSetFromMap(new ConcurrentHashMap());
   private static Set spawnEggs = new HashSet();
   private static boolean isEnabled = true;
   private static final int MAX_SLIME_SIZE = 16;
   private static final int MAX_ENTITY_NAME_LENGTH = 128;
   private static final int MAX_TEXT_DISPLAY_LENGTH = 256;
   private static final double MAX_VELOCITY = 1000.0;
   private static final double MAX_MOTION = 1000.0;

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("server").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("illegalEntity").requires((source) -> {
         Entity patt2838$temp = source.m_81373_();
         if (!(patt2838$temp instanceof ServerPlayer player)) {
            return false;
         } else {
            return player.m_20194_() != null && player.m_20194_().m_7779_(player.m_36316_());
         }
      })).then(Commands.m_82127_("on").executes((context) -> {
         isEnabled = true;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f异常实体检测 开启");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("off").executes((context) -> {
         isEnabled = false;
         ((CommandSourceStack)context.getSource()).m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f异常实体检测 关闭");
         }, true);
         return 1;
      }))).then(Commands.m_82127_("scan").executes((context) -> {
         scanAllEntities((CommandSourceStack)context.getSource());
         return 1;
      }))).then(Commands.m_82127_("clearall").executes((context) -> {
         clearAllIllegalEntities((CommandSourceStack)context.getSource());
         return 1;
      })))));
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase != Phase.END) {
         if (isEnabled) {
            Iterator var1 = event.getServer().m_129785_().iterator();

            while(var1.hasNext()) {
               ServerLevel level = (ServerLevel)var1.next();
               List toRemove = new ArrayList();
               level.m_8583_().forEach((entityx) -> {
                  if (!(entityx instanceof Player)) {
                     String text;
                     if (entityx instanceof Slime) {
                        Slime slime = (Slime)entityx;
                        int size = slime.m_33632_();
                        if (size > 16) {
                           toRemove.add(entityx);
                           text = String.format("史莱姆过大: %d > %d", size, 16);
                           broadcastIllegalEntity(entityx, text, (ServerPlayer)null, level);
                           return;
                        }
                     }

                     String name;
                     if (hasExcessiveMotion(entityx)) {
                        toRemove.add(entityx);
                        name = getMotionReason(entityx);
                        broadcastIllegalEntity(entityx, name, (ServerPlayer)null, level);
                     } else {
                        name = entityx.m_7755_().getString();
                        if (name.length() > 128) {
                           toRemove.add(entityx);
                           String reasonx = String.format("实体名称过长: %d字 > %d", name.length(), 128);
                           broadcastIllegalEntity(entityx, reasonx, (ServerPlayer)null, level);
                        } else {
                           if (entityx.m_6095_().toString().contains("text_display")) {
                              CompoundTag tag = new CompoundTag();
                              entityx.m_20240_(tag);
                              if (tag.m_128441_("text")) {
                                 text = tag.m_128461_("text");
                                 String reason;
                                 if (text.length() > 256) {
                                    toRemove.add(entityx);
                                    reason = String.format("文本显示过长: %d字 > %d", text.length(), 256);
                                    broadcastIllegalEntity(entityx, reason, (ServerPlayer)null, level);
                                    return;
                                 }

                                 if (countOccurrences(text, "@e") > 3) {
                                    toRemove.add(entityx);
                                    reason = "文本显示过多@e选择器";
                                    broadcastIllegalEntity(entityx, reason, (ServerPlayer)null, level);
                                    return;
                                 }
                              }
                           }

                           if (isIllegalEntity(entityx)) {
                              toRemove.add(entityx);
                           }

                        }
                     }
                  }
               });
               Iterator var4 = toRemove.iterator();

               while(var4.hasNext()) {
                  Entity entity = (Entity)var4.next();
                  entity.m_142687_(RemovalReason.DISCARDED);
                  detectedEntities.add(entity.m_20148_());
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().f_46443_) {
         if (isEnabled) {
            Entity entity = event.getEntity();
            if (!(entity instanceof Player)) {
               String text;
               if (entity instanceof Slime) {
                  Slime slime = (Slime)entity;
                  int size = slime.m_33632_();
                  if (size > 16) {
                     event.setCanceled(true);
                     text = String.format("史莱姆过大: %d > %d", size, 16);
                     broadcastIllegalEntity(entity, text, (ServerPlayer)null, (ServerLevel)event.getLevel());
                     return;
                  }
               }

               String name;
               if (hasExcessiveMotion(entity)) {
                  event.setCanceled(true);
                  name = getMotionReason(entity);
                  broadcastIllegalEntity(entity, name, (ServerPlayer)null, (ServerLevel)event.getLevel());
               } else {
                  name = entity.m_7755_().getString();
                  if (name.length() > 128) {
                     event.setCanceled(true);
                     String reason = String.format("实体名称过长: %d字 > %d", name.length(), 128);
                     broadcastIllegalEntity(entity, reason, (ServerPlayer)null, (ServerLevel)event.getLevel());
                  } else {
                     if (entity.m_6095_().toString().contains("text_display")) {
                        CompoundTag tag = new CompoundTag();
                        entity.m_20240_(tag);
                        if (tag.m_128441_("text")) {
                           text = tag.m_128461_("text");
                           String reason;
                           if (text.length() > 256) {
                              event.setCanceled(true);
                              reason = String.format("文本显示过长: %d字 > %d", text.length(), 256);
                              broadcastIllegalEntity(entity, reason, (ServerPlayer)null, (ServerLevel)event.getLevel());
                              return;
                           }

                           if (countOccurrences(text, "@e") > 3) {
                              event.setCanceled(true);
                              reason = "文本显示过多@e选择器";
                              broadcastIllegalEntity(entity, reason, (ServerPlayer)null, (ServerLevel)event.getLevel());
                              return;
                           }
                        }
                     }

                     if (isIllegalEntity(entity)) {
                        event.setCanceled(true);
                        broadcastIllegalEntity(entity, getIllegalReason(entity), (ServerPlayer)null, (ServerLevel)event.getLevel());
                     }

                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerInteractItem(PlayerInteractEvent.RightClickItem event) {
      if (!event.getLevel().f_46443_) {
         ServerPlayer player = (ServerPlayer)event.getEntity();
         if (isEnabled) {
            ItemStack stack = event.getItemStack();
            if (spawnEggs.contains(stack.m_41720_()) && stack.m_41782_()) {
               CompoundTag tag = stack.m_41783_();
               if (tag.m_128425_("EntityTag", 10)) {
                  CompoundTag entityTag = tag.m_128469_("EntityTag");
                  if (hasExcessiveMotionTag(entityTag)) {
                     event.setCanceled(true);
                     BlockPos pos = player.m_20183_();
                     String reason = getMotionTagReason(entityTag);
                     player.m_150109_().m_36057_(stack);
                     Component msg = Component.m_237113_(String.format("%s§c拦止玩家 %s 使用异常实体蛋 §a[%d, %d, %d] §7- %s", "§c[§6Lexis-Server§c] §f", player.m_7755_().getString(), pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), reason)).m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, String.format("/tp %d %d %d", pos.m_123341_(), pos.m_123342_(), pos.m_123343_()))).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("点击传送"))));
                     Iterator var14 = player.f_8924_.m_6846_().m_11314_().iterator();

                     while(var14.hasNext()) {
                        ServerPlayer p = (ServerPlayer)var14.next();
                        p.m_213846_(msg);
                     }

                     return;
                  }
               }

               if (isIllegalEntityTag(stack.m_41783_())) {
                  event.setCanceled(true);
                  BlockPos pos = player.m_20183_();
                  String reason = getIllegalTagReason(stack.m_41783_());
                  player.m_150109_().m_36057_(stack);
                  Component msg = Component.m_237113_(String.format("%s§c拦止玩家 %s 使用异常实体蛋 §a[%d, %d, %d] §7- %s", "§c[§6Lexis-Server§c] §f", player.m_7755_().getString(), pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), reason)).m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, String.format("/tp %d %d %d", pos.m_123341_(), pos.m_123342_(), pos.m_123343_()))).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("点击传送"))));
                  Iterator var7 = player.f_8924_.m_6846_().m_11314_().iterator();

                  while(var7.hasNext()) {
                     ServerPlayer p = (ServerPlayer)var7.next();
                     p.m_213846_(msg);
                  }
               }
            }

         }
      }
   }

   private static boolean hasExcessiveMotion(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         double motionX = Math.abs(entity.m_20184_().f_82479_);
         double motionY = Math.abs(entity.m_20184_().f_82480_);
         double motionZ = Math.abs(entity.m_20184_().f_82481_);
         return motionX > 1000.0 || motionY > 1000.0 || motionZ > 1000.0;
      }
   }

   private static boolean hasExcessiveMotionTag(CompoundTag tag) {
      if (tag == null) {
         return false;
      } else {
         ListTag velocity;
         int i;
         double value;
         if (tag.m_128425_("Motion", 9)) {
            velocity = tag.m_128437_("Motion", 6);

            for(i = 0; i < velocity.size(); ++i) {
               value = Math.abs(velocity.m_128772_(i));
               if (value > 1000.0) {
                  return true;
               }
            }
         }

         if (tag.m_128425_("power", 9)) {
            velocity = tag.m_128437_("power", 6);

            for(i = 0; i < velocity.size(); ++i) {
               value = Math.abs(velocity.m_128772_(i));
               if (value > 1000.0) {
                  return true;
               }
            }
         }

         if (tag.m_128425_("velocity", 9)) {
            velocity = tag.m_128437_("velocity", 6);

            for(i = 0; i < velocity.size(); ++i) {
               value = Math.abs(velocity.m_128772_(i));
               if (value > 1000.0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static String getMotionReason(Entity entity) {
      double motionX = Math.abs(entity.m_20184_().f_82479_);
      double motionY = Math.abs(entity.m_20184_().f_82480_);
      double motionZ = Math.abs(entity.m_20184_().f_82481_);
      return !(motionX > 1000.0) && !(motionY > 1000.0) && !(motionZ > 1000.0) ? "运动异常" : String.format("运动向量过多: %.0f, %.0f, %.0f", motionX, motionY, motionZ);
   }

   private static String getMotionTagReason(CompoundTag tag) {
      ListTag power;
      if (tag.m_128425_("Motion", 9)) {
         power = tag.m_128437_("Motion", 6);
         return String.format("Motion过多: %.0f, %.0f, %.0f", power.m_128772_(0), power.m_128772_(1), power.m_128772_(2));
      } else if (tag.m_128425_("power", 9)) {
         power = tag.m_128437_("power", 6);
         return String.format("Power过多: %.0f, %.0f, %.0f", power.m_128772_(0), power.m_128772_(1), power.m_128772_(2));
      } else {
         return "运动异常";
      }
   }

   private static void scanAllEntities(CommandSourceStack source) {
      ServerLevel level = source.m_81372_();
      List illegalEntities = new ArrayList();
      level.m_8583_().forEach((entityx) -> {
         if (!(entityx instanceof Player)) {
            if (isIllegalEntity(entityx)) {
               illegalEntities.add(entityx);
            }

         }
      });
      if (illegalEntities.isEmpty()) {
         source.m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f未发现异常实体");
         }, true);
      } else {
         source.m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f发现 " + illegalEntities.size() + " 个异常实体:");
         }, true);
         Iterator var3 = illegalEntities.iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            BlockPos pos = entity.m_20183_();
            Component msg = Component.m_237113_(String.format(" §7[%d, %d, %d] §f%s §7- %s", pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), entity.m_7755_().getString(), getIllegalReason(entity))).m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, String.format("/tp %d %d %d", pos.m_123341_(), pos.m_123342_(), pos.m_123343_()))).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§a点击传送"))));
            source.m_288197_(() -> {
               return msg;
            }, true);
            entity.m_142687_(RemovalReason.DISCARDED);
            detectedEntities.add(entity.m_20148_());
         }

         source.m_288197_(() -> {
            return Component.m_237113_("§c[§6Lexis-Server§c] §f已清除上异常实体了");
         }, true);
      }
   }

   private static void clearAllIllegalEntities(CommandSourceStack source) {
      int count = 0;
      Iterator var2 = source.m_81377_().m_129785_().iterator();

      while(var2.hasNext()) {
         ServerLevel level = (ServerLevel)var2.next();
         List toRemove = new ArrayList();
         level.m_8583_().forEach((entityx) -> {
            if (!(entityx instanceof Player)) {
               if (isIllegalEntity(entityx)) {
                  toRemove.add(entityx);
               }

            }
         });

         for(Iterator var5 = toRemove.iterator(); var5.hasNext(); ++count) {
            Entity entity = (Entity)var5.next();
            entity.m_142687_(RemovalReason.DISCARDED);
            detectedEntities.add(entity.m_20148_());
         }
      }

      source.m_288197_(() -> {
         return Component.m_237113_("§c[§6Lexis-Server§c] §f已清除 " + count + " 个异常实体");
      }, true);
   }

   private static void broadcastIllegalEntity(Entity entity, String reason, ServerPlayer source, ServerLevel level) {
      BlockPos pos = entity.m_20183_();
      String sourceName = source != null ? source.m_7755_().getString() : "未知";
      Component msg = Component.m_237113_(String.format("%s§c清除异常实体 §a[%d, %d, %d] §7- %s", "§c[§6Lexis-Server§c] §f", pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), sourceName, reason)).m_6270_(Style.f_131099_.m_131142_(new ClickEvent(Action.RUN_COMMAND, String.format("/tp %d %d %d", pos.m_123341_(), pos.m_123342_(), pos.m_123343_()))).m_131144_(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.f_130831_, Component.m_237113_("§a点击传送"))));
      Iterator var7 = level.m_6907_().iterator();

      while(var7.hasNext()) {
         ServerPlayer player = (ServerPlayer)var7.next();
         player.m_213846_(msg);
      }

   }

   private static boolean isIllegalEntity(Entity entity) {
      if (entity != null && !detectedEntities.contains(entity.m_20148_())) {
         if (entity instanceof Player) {
            return false;
         } else {
            try {
               if (hasExcessiveMotion(entity)) {
                  return true;
               } else if (entity instanceof LivingEntity) {
                  LivingEntity living = (LivingEntity)entity;
                  float health = living.m_21223_();
                  if (!Float.isNaN(health) && !Float.isInfinite(health)) {
                     if (health > 1000000.0F) {
                        return true;
                     } else {
                        float maxHealth = living.m_21233_();
                        if (!Float.isNaN(maxHealth) && !Float.isInfinite(maxHealth)) {
                           if (maxHealth > 1000000.0F) {
                              return true;
                           } else {
                              int deathTime = living.f_20919_;
                              if (deathTime >= 0 && deathTime <= 200) {
                                 CompoundTag tag = new CompoundTag();
                                 living.m_20240_(tag);
                                 if (tag.m_128425_("Attributes", 9)) {
                                    ListTag attributes = tag.m_128437_("Attributes", 10);

                                    for(int i = 0; i < attributes.size(); ++i) {
                                       CompoundTag attr = attributes.m_128728_(i);
                                       if (attr.m_128441_("Base")) {
                                          double base = attr.m_128459_("Base");
                                          if (Double.isNaN(base) || Double.isInfinite(base)) {
                                             return true;
                                          }

                                          if (base > 1000000.0) {
                                             return true;
                                          }
                                       }
                                    }
                                 }

                                 return false;
                              } else {
                                 return true;
                              }
                           }
                        } else {
                           return true;
                        }
                     }
                  } else {
                     return true;
                  }
               } else {
                  return false;
               }
            } catch (Exception var11) {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean isIllegalEntityTag(CompoundTag tag) {
      if (tag == null) {
         return false;
      } else {
         try {
            CompoundTag entityTag;
            String text;
            int i;
            ListTag attributes;
            if (tag.m_128425_("EntityTag", 10)) {
               entityTag = tag.m_128469_("EntityTag");
               if (hasExcessiveMotionTag(entityTag)) {
                  return true;
               }

               if (entityTag.m_128441_("CustomName")) {
                  text = entityTag.m_128461_("CustomName");
                  if (text.length() > 128) {
                     return true;
                  }
               }

               if (entityTag.m_128441_("id") && entityTag.m_128461_("id").contains("text_display") && entityTag.m_128441_("text")) {
                  text = entityTag.m_128461_("text");
                  if (text.length() > 256) {
                     return true;
                  }

                  if (countOccurrences(text, "@e") > 10) {
                     return true;
                  }
               }

               if (entityTag.m_128441_("Health")) {
                  float health = entityTag.m_128457_("Health");
                  if (Float.isNaN(health) || Float.isInfinite(health)) {
                     return true;
                  }

                  if (health > 1000000.0F) {
                     return true;
                  }
               }

               if (entityTag.m_128425_("Attributes", 9)) {
                  attributes = entityTag.m_128437_("Attributes", 10);

                  for(i = 0; i < attributes.size(); ++i) {
                     CompoundTag attr = attributes.m_128728_(i);
                     if (attr.m_128441_("Base")) {
                        double base = attr.m_128459_("Base");
                        if (Double.isNaN(base) || Double.isInfinite(base)) {
                           return true;
                        }

                        if (base > 1000000.0) {
                           return true;
                        }
                     }
                  }
               }
            }

            if (tag.m_128425_("display", 10)) {
               entityTag = tag.m_128469_("display");
               if (entityTag.m_128441_("Name")) {
                  text = entityTag.m_128461_("Name");
                  if (text.length() > 128) {
                     return true;
                  }
               }

               if (entityTag.m_128425_("Lore", 9)) {
                  attributes = entityTag.m_128437_("Lore", 8);

                  for(i = 0; i < attributes.size(); ++i) {
                     String line = attributes.m_128778_(i);
                     if (line.length() > 128) {
                        return true;
                     }
                  }
               }
            }

            return false;
         } catch (Exception var7) {
            return true;
         }
      }
   }

   private static String getIllegalTagReason(CompoundTag tag) {
      if (tag == null) {
         return "未知异常";
      } else {
         try {
            CompoundTag entityTag;
            String text;
            if (tag.m_128425_("EntityTag", 10)) {
               entityTag = tag.m_128469_("EntityTag");
               if (hasExcessiveMotionTag(entityTag)) {
                  return getMotionTagReason(entityTag);
               }

               if (entityTag.m_128441_("CustomName")) {
                  text = entityTag.m_128461_("CustomName");
                  if (text.length() > 128) {
                     return "实体名称过长";
                  }
               }

               if (entityTag.m_128441_("text")) {
                  text = entityTag.m_128461_("text");
                  if (text.length() > 256) {
                     return "文本显示过长";
                  }

                  if (countOccurrences(text, "@e") > 10) {
                     return "文本过多@e";
                  }
               }
            }

            if (tag.m_128425_("display", 256)) {
               entityTag = tag.m_128469_("display");
               if (entityTag.m_128441_("Name")) {
                  text = entityTag.m_128461_("Name");
                  if (text.length() > 128) {
                     return "物品名称过长";
                  }
               }
            }

            return "NBT异常";
         } catch (Exception var3) {
            return "NBT解析异常";
         }
      }
   }

   private static int countOccurrences(String text, String find) {
      int count = 0;

      for(int index = 0; (index = text.indexOf(find, index)) != -1; index += find.length()) {
         ++count;
      }

      return count;
   }

   private static String getIllegalReason(Entity entity) {
      if (hasExcessiveMotion(entity)) {
         return getMotionReason(entity);
      } else if (entity instanceof LivingEntity) {
         LivingEntity living = (LivingEntity)entity;
         if (Float.isNaN(living.m_21223_())) {
            return "血量=NaN";
         } else if (Float.isInfinite(living.m_21223_())) {
            return "血量=Infinite";
         } else if (living.m_21223_() > 1000000.0F) {
            return "血量过多";
         } else if (living.f_20919_ > 200) {
            return "死亡时间异常";
         } else {
            if (entity instanceof Slime) {
               Slime slime = (Slime)entity;
               int size = slime.m_33632_();
               if (size > 16) {
                  return String.format("史莱姆过大: %d", size);
               }
            }

            String name = entity.m_7755_().getString();
            return name.length() > 128 ? String.format("名称过长: %d字", name.length()) : "NBT异常";
         }
      } else {
         return "未知实体";
      }
   }

   static {
      Iterator var0 = ForgeRegistries.ITEMS.iterator();

      while(var0.hasNext()) {
         Item item = (Item)var0.next();
         String itemId = ForgeRegistries.ITEMS.getKey(item).toString();
         if (itemId.contains("spawn_egg")) {
            spawnEggs.add(item);
         }
      }

   }
}
