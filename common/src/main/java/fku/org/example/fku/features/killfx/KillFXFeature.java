package fku.org.example.fku.features.killfx;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.killfx.KillFXConfig;
import fku.org.example.fku.features.killfx.KillFXShaderManager;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class KillFXFeature {
    private static final int MAX_DEATHS = 15;
    private static final int MAX_LIGHTNING = 5;
    private static final int MAX_PARTICLES = 200;
    private static final Set<Integer> processedEntities = ConcurrentHashMap.newKeySet();
    private static final Map<Integer, Long> attackedTargets = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Integer, Float> lastHealthMap = new ConcurrentHashMap<Integer, Float>();
    private static final Queue<LivingEntity> renderQueue = new ArrayDeque<LivingEntity>();

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        KillFXConfig.getInstance();
        Fku.LOGGER.info("[KillFX] v5 \u9632\u5361\u6b7b \u521d\u59cb\u5316");
    }

    public static void markAttackedByTpAura(int entityId) {
        if (!KillFXConfig.getInstance().enabled) {
            return;
        }
        attackedTargets.put(entityId, System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() == null || event.getTarget() == null) {
            return;
        }
        KillFXConfig cfg = KillFXConfig.getInstance();
        if (!cfg.enabled || !cfg.onlyTargeted) {
            return;
        }
        attackedTargets.put(event.getTarget().getId(), System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        KillFXConfig cfg = KillFXConfig.getInstance();
        if (!cfg.enabled) {
            processedEntities.clear();
            attackedTargets.clear();
            lastHealthMap.clear();
            renderQueue.clear();
            return;
        }
        long now = System.currentTimeMillis();
        attackedTargets.entrySet().removeIf(e -> (now - (Long)e.getValue()) > cfg.targetTimeout * 1000.0);
        if (event.phase == TickEvent.Phase.START) {
            KillFXFeature.detectDeaths(cfg, now);
        } else if (event.phase == TickEvent.Phase.END) {
            KillFXFeature.renderQueued(cfg);
            KillFXShaderManager.tick();
        }
        if (lastHealthMap.size() > 10000) {
            Fku.LOGGER.warn("[KillFX] lastHealthMap \u8fc7\u5927, \u6e05\u7406");
            lastHealthMap.clear();
        }
        if (processedEntities.size() > 10000) {
            Fku.LOGGER.warn("[KillFX] processedEntities \u8fc7\u5927, \u6e05\u7406");
            processedEntities.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        KillFXShaderManager.renderEffects(event.getPoseStack(), event.getPartialTick());
    }

    private static void detectDeaths(KillFXConfig cfg, long now) {
        Minecraft mc = KillFXFeature.getMc();
        if (mc == null || mc.level == null) {
            return;
        }
        int deaths = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            float health;
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (entity == mc.player) continue;
            int id = living.getId();
            try {
                health = living.getHealth();
            }
            catch (Exception e) {
                continue;
            }
            Float prevHealth = lastHealthMap.get(id);
            if (prevHealth != null && health <= 0.0f && prevHealth.floatValue() > 0.0f) {
                if (processedEntities.contains(id)) continue;
                if (cfg.onlyTargeted) {
                    Long at = attackedTargets.get(id);
                    if (at == null) continue;
                    if ((now - at) > cfg.targetTimeout * 1000.0) {
                        attackedTargets.remove(id);
                        continue;
                    }
                }
                if (deaths >= 15) break;
                ++deaths;
                processedEntities.add(id);
                renderQueue.add(living);
            }
            if (health > 0.0f) {
                lastHealthMap.put(id, health);
                continue;
            }
            lastHealthMap.remove(id);
        }
    }

    private static void renderQueued(KillFXConfig cfg) {
        LivingEntity entity;
        Minecraft mc = KillFXFeature.getMc();
        if (mc == null || mc.level == null) {
            return;
        }
        int rendered = 0;
        while ((entity = renderQueue.poll()) != null && rendered < 10) {
            try {
                KillFXFeature.renderEffects(entity, cfg);
                ++rendered;
                if (!cfg.useShader || cfg.shaderType.equals("\u65e0")) continue;
                KillFXShaderManager.ShaderType shaderType = KillFXFeature.shaderTypeFromChinese(cfg.shaderType);
                String extra = "";
                float intensity = (float)cfg.shaderIntensity;
                switch (shaderType) {
                    case CRYSTAL: {
                        extra = String.format("%s,%s,%.1f,%.1f,%.1f,%s", KillFXFeature.mapCrystalStyle(cfg.crystalStyle), cfg.crystalTintColor, cfg.crystalRadius, cfg.crystalGlowIntensity, cfg.crystalRotationSpeed, cfg.crystalPulse);
                        intensity = (float)cfg.crystalGlowIntensity;
                        break;
                    }
                    case BLACKHOLE: {
                        extra = String.format("%.1f", cfg.blackholeScale);
                        break;
                    }
                    case SKY_BEAM: {
                        extra = String.format("BEAM,%.1f", cfg.shaderIntensity);
                        break;
                    }
                    case SKY_RING: {
                        extra = String.format("RING,%.1f", cfg.shaderIntensity);
                    }
                }
                KillFXShaderManager.trigger(shaderType, entity.position(), intensity, cfg.shaderDuration, extra);
            }
            catch (Exception e) {
                Fku.LOGGER.error("[KillFX] \u6e32\u67d3\u5f02\u5e38", (Throwable)e);
            }
        }
        if (renderQueue.size() > 50) {
            Fku.LOGGER.warn("[KillFX] \u6e32\u67d3\u961f\u5217\u8fc7\u957f, \u4e22\u5f03");
            renderQueue.clear();
        }
    }

    private static void renderEffects(LivingEntity entity, KillFXConfig cfg) {
        ClientLevel level;
        Minecraft mc = KillFXFeature.getMc();
        ClientLevel clientLevel = level = mc != null ? mc.level : null;
        if (level == null || entity == null) {
            return;
        }
        Vec3 pos = entity.position();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        if (cfg.useLightning && cfg.lightningAmount > 0) {
            int amount = Math.min(cfg.lightningAmount, 5);
            for (int i = 0; i < amount; ++i) {
                LightningBolt bolt;
                try {
                    bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create((Level)level);
                }
                catch (Exception e) {
                    continue;
                }
                if (bolt == null) continue;
                try {
                    bolt.setPos(x, y, z);
                    KillFXFeature.safeSetInt(bolt, 6, "life", "field_7185", "life");
                    KillFXFeature.safeSetInt(bolt, 6, "flashes", "field_7183", "flashes");
                    KillFXFeature.safeSetBool(bolt, true, "visualOnly", "field_20862_", "visualOnly");
                    level.putNonPlayerEntity(bolt.getId(), (Entity)bolt);
                }
                catch (Exception exception) {
                    // ignored
                }
                if (cfg.useLightningSound) continue;
                KillFXFeature.safeSetInt(bolt, 1, "life", "field_7185", "life");
            }
        }
        if (cfg.useParticles) {
            try {
                ParticleOptions p = KillFXFeature.resolveParticle(cfg);
                if (p != null) {
                    KillFXFeature.spawnParticles(level, p, x, y + (entity.getBbHeight() / 2.0f), z, cfg);
                }
            }
            catch (Exception p) {
                // ignored
            }
        }
        if (cfg.useSound) {
            try {
                SoundEvent s = KillFXFeature.resolveSound(cfg);
                if (s != null) {
                    level.playSound((Player)mc.player, x, y, z, s, SoundSource.WEATHER, (float)cfg.volume, (float)cfg.pitch);
                }
            }
            catch (Exception s) {
                // ignored
            }
        }
        if (cfg.useFirework) {
            try {
                ItemStack stack = new ItemStack((ItemLike)Items.FIREWORK_ROCKET);
                CompoundTag tag = new CompoundTag();
                tag.putInt("Flight", 1);
                stack.setTag(tag);
                FireworkRocketEntity rocket = new FireworkRocketEntity((Level)level, x, y, z, stack);
                level.putNonPlayerEntity(rocket.getId(), (Entity)rocket);
            }
            catch (Exception exception) {
                // ignored
            }
        }
        if (cfg.useExplosion) {
            try {
                level.addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0, 0.0, 0.0);
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static Field findField(Class<?> c, String... names) {
        for (String n : names) {
            try {
                Field f = c.getDeclaredField(n);
                f.setAccessible(true);
                return f;
            }
            catch (Exception exception) {
            // ignored
        }
        }
        return null;
    }

    private static void safeSetInt(Object o, int v, String... names) {
        try {
            Field f = KillFXFeature.findField(o.getClass(), names);
            if (f != null) {
                f.setInt(o, v);
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private static void safeSetBool(Object o, boolean v, String... names) {
        try {
            Field f = KillFXFeature.findField(o.getClass(), names);
            if (f != null) {
                f.setBoolean(o, v);
            }
        }
        catch (Exception exception) {
            // ignored
        }
    }

    private static ParticleOptions resolveParticle(KillFXConfig cfg) {
        return switch (cfg.particleCategory) {
            case "Combat" -> KillFXFeature.combatP(cfg.combatParticle);
            case "Magic" -> KillFXFeature.magicP(cfg.magicParticle);
            case "Fire" -> KillFXFeature.fireP(cfg.fireParticle);
            case "Nature" -> KillFXFeature.natureP(cfg.natureParticle);
            case "Update121" -> KillFXFeature.updateP(cfg.updateParticle);
            case "Misc" -> KillFXFeature.miscP(cfg.miscParticle);
            default -> ParticleTypes.END_ROD;
        };
    }

    private static ParticleOptions combatP(String n) {
        return switch (n) {
            case "DAMAGE_INDICATOR" -> ParticleTypes.DAMAGE_INDICATOR;
            case "CRIT" -> ParticleTypes.CRIT;
            case "ENCHANTED_HIT" -> ParticleTypes.ENCHANTED_HIT;
            case "SWEEP_ATTACK" -> ParticleTypes.SWEEP_ATTACK;
            case "EXPLOSION" -> ParticleTypes.EXPLOSION;
            case "EXPLOSION_EMITTER" -> ParticleTypes.EXPLOSION_EMITTER;
            case "SONIC_BOOM" -> ParticleTypes.SONIC_BOOM;
            case "TOTEM_OF_UNDYING" -> ParticleTypes.TOTEM_OF_UNDYING;
            case "FIREWORK" -> ParticleTypes.FIREWORK;
            case "EGG_CRACK" -> ParticleTypes.EGG_CRACK;
            default -> ParticleTypes.CRIT;
        };
    }

    private static ParticleOptions magicP(String n) {
        return switch (n) {
            case "WITCH" -> ParticleTypes.WITCH;
            case "END_ROD" -> ParticleTypes.END_ROD;
            case "PORTAL" -> ParticleTypes.PORTAL;
            case "ENCHANT" -> ParticleTypes.ENCHANT;
            case "NAUTILUS" -> ParticleTypes.NAUTILUS;
            case "ELDER_GUARDIAN" -> ParticleTypes.ELDER_GUARDIAN;
            case "SCULK_CHARGE_POP" -> ParticleTypes.SCULK_CHARGE_POP;
            case "SOUL" -> ParticleTypes.SOUL;
            case "GLOW_SQUID_INK" -> ParticleTypes.GLOW_SQUID_INK;
            default -> ParticleTypes.END_ROD;
        };
    }

    private static ParticleOptions fireP(String n) {
        return switch (n) {
            case "FLAME" -> ParticleTypes.FLAME;
            case "SOUL_FIRE_FLAME" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "SMALL_FLAME" -> ParticleTypes.SMALL_FLAME;
            case "LAVA" -> ParticleTypes.LAVA;
            case "LARGE_SMOKE" -> ParticleTypes.LARGE_SMOKE;
            case "SMOKE" -> ParticleTypes.SMOKE;
            case "CAMPFIRE_COSY_SMOKE" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case "CAMPFIRE_SIGNAL_SMOKE" -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
            case "GLOW" -> ParticleTypes.GLOW;
            case "WAX_ON" -> ParticleTypes.WAX_ON;
            case "WAX_OFF" -> ParticleTypes.WAX_OFF;
            case "SCRAPE" -> ParticleTypes.SCRAPE;
            case "ELECTRIC_SPARK" -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.FLAME;
        };
    }

    private static ParticleOptions natureP(String n) {
        return switch (n) {
            case "HEART" -> ParticleTypes.HEART;
            case "CLOUD" -> ParticleTypes.CLOUD;
            case "RAIN" -> ParticleTypes.RAIN;
            case "SNOWFLAKE" -> ParticleTypes.SNOWFLAKE;
            case "ITEM_SLIME" -> ParticleTypes.ITEM_SLIME;
            case "BUBBLE" -> ParticleTypes.BUBBLE;
            case "BUBBLE_COLUMN_UP" -> ParticleTypes.BUBBLE_COLUMN_UP;
            case "CURRENT_DOWN" -> ParticleTypes.CURRENT_DOWN;
            case "BUBBLE_POP" -> ParticleTypes.BUBBLE_POP;
            case "SPLASH" -> ParticleTypes.SPLASH;
            case "FISHING" -> ParticleTypes.FISHING;
            case "DOLPHIN" -> ParticleTypes.DOLPHIN;
            case "UNDERWATER" -> ParticleTypes.UNDERWATER;
            case "NOTE" -> ParticleTypes.NOTE;
            case "CHERRY_LEAVES" -> ParticleTypes.CHERRY_LEAVES;
            case "SPORE_BLOSSOM_AIR" -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case "WHITE_ASH" -> ParticleTypes.WHITE_ASH;
            case "WARPED_SPORE" -> ParticleTypes.WARPED_SPORE;
            case "CRIMSON_SPORE" -> ParticleTypes.CRIMSON_SPORE;
            default -> ParticleTypes.HEART;
        };
    }

    private static ParticleOptions updateP(String n) {
        return switch (n) {
            case "DRAGON_BREATH" -> ParticleTypes.DRAGON_BREATH;
            case "FLASH" -> ParticleTypes.FLASH;
            case "POOF" -> ParticleTypes.POOF;
            case "ELECTRIC_SPARK" -> ParticleTypes.ELECTRIC_SPARK;
            case "GLOW" -> ParticleTypes.GLOW;
            case "SCRAPE" -> ParticleTypes.SCRAPE;
            case "WAX_ON" -> ParticleTypes.WAX_ON;
            case "WAX_OFF" -> ParticleTypes.WAX_OFF;
            case "SNOWFLAKE" -> ParticleTypes.SNOWFLAKE;
            case "SPIT" -> ParticleTypes.SPIT;
            default -> ParticleTypes.DRAGON_BREATH;
        };
    }

    private static ParticleOptions miscP(String n) {
        return switch (n) {
            case "ASH" -> ParticleTypes.ASH;
            case "MYCELIUM" -> ParticleTypes.MYCELIUM;
            case "SCULK_SOUL" -> ParticleTypes.SCULK_SOUL;
            case "HAPPY_VILLAGER" -> ParticleTypes.HAPPY_VILLAGER;
            case "ANGRY_VILLAGER" -> ParticleTypes.ANGRY_VILLAGER;
            case "SNEEZE" -> ParticleTypes.SNEEZE;
            case "SQUID_INK" -> ParticleTypes.SQUID_INK;
            default -> ParticleTypes.SCULK_SOUL;
        };
    }

    private static SoundEvent resolveSound(KillFXConfig cfg) {
        return switch (cfg.soundGroup) {
            case "Combat" -> KillFXFeature.combatS(cfg.combatSound);
            case "Magic" -> KillFXFeature.magicS(cfg.magicSound);
            case "Creature" -> KillFXFeature.creatureS(cfg.creatureSound);
            case "Fun" -> KillFXFeature.funS(cfg.funSound);
            default -> KillFXFeature.se("entity.lightning_bolt.thunder");
        };
    }

    private static SoundEvent se(String s) {
        return SoundEvent.createVariableRangeEvent((ResourceLocation)new ResourceLocation(s));
    }

    private static SoundEvent combatS(String n) {
        return switch (n) {
            case "THUNDER" -> KillFXFeature.se("entity.lightning_bolt.thunder");
            case "EXPLODE" -> KillFXFeature.se("entity.generic.explode");
            case "ANVIL" -> KillFXFeature.se("block.anvil.land");
            case "TRIDENT_THUNDER" -> KillFXFeature.se("item.trident.thunder");
            case "WITHER_SPAWN" -> KillFXFeature.se("entity.wither.spawn");
            case "WITHER_SHOOT" -> KillFXFeature.se("entity.wither.shoot");
            case "ANCHOR" -> KillFXFeature.se("block.respawn_anchor.deplete");
            case "CRYSTAL" -> KillFXFeature.se("entity.end_crystal.explode");
            case "BREAK" -> KillFXFeature.se("item.shield.break");
            case "CRIT" -> KillFXFeature.se("entity.player.attack.crit");
            case "CROSSBOW_HIT" -> KillFXFeature.se("item.crossbow.hit");
            case "TRIDENT_HIT" -> KillFXFeature.se("item.trident.hit");
            case "FIREWORK_BLAST" -> KillFXFeature.se("entity.firework_rocket.blast");
            case "ATK_STRONG" -> KillFXFeature.se("entity.player.attack.strong");
            case "ATK_SWEEP" -> KillFXFeature.se("entity.player.attack.sweep");
            default -> KillFXFeature.se("entity.lightning_bolt.thunder");
        };
    }

    private static SoundEvent magicS(String n) {
        return switch (n) {
            case "ANCHOR_CHARGE" -> KillFXFeature.se("block.respawn_anchor.charge");
            case "ANCHOR_SET" -> KillFXFeature.se("block.respawn_anchor.set_spawn");
            case "TOTEM" -> KillFXFeature.se("item.totem.use");
            case "BEACON" -> KillFXFeature.se("block.beacon.activate");
            case "CONDUIT" -> KillFXFeature.se("block.conduit.activate");
            case "PORTAL" -> KillFXFeature.se("block.portal.trigger");
            case "LEVEL_UP" -> KillFXFeature.se("entity.player.levelup");
            case "ENCHANT" -> KillFXFeature.se("block.enchantment_table.use");
            case "TELEPORT" -> KillFXFeature.se("entity.enderman.teleport");
            case "BELL" -> KillFXFeature.se("block.bell.use");
            case "CHIME" -> KillFXFeature.se("block.amethyst_block.chime");
            case "RESONATE" -> KillFXFeature.se("block.amethyst_block.resonate");
            case "ENDER_EYE" -> KillFXFeature.se("entity.ender_eye.death");
            case "EXP_ORB" -> KillFXFeature.se("entity.experience_orb.pickup");
            case "EVOKER_CAST" -> KillFXFeature.se("entity.evoker.cast_spell");
            case "CONDUIT_ATK" -> KillFXFeature.se("block.conduit.attack_target");
            case "DRAGON_FIREBALL" -> KillFXFeature.se("entity.dragon_fireball.explode");
            default -> KillFXFeature.se("block.respawn_anchor.charge");
        };
    }

    private static SoundEvent creatureS(String n) {
        return switch (n) {
            case "WARDEN" -> KillFXFeature.se("entity.warden.sonic_boom");
            case "WARDEN_HEART" -> KillFXFeature.se("entity.warden.heartbeat");
            case "DRAGON" -> KillFXFeature.se("entity.ender_dragon.death");
            case "DRAGON_GROWL" -> KillFXFeature.se("entity.ender_dragon.growl");
            case "BLAZE" -> KillFXFeature.se("entity.blaze.death");
            case "GHAST" -> KillFXFeature.se("entity.ghast.scream");
            case "ENDERMAN" -> KillFXFeature.se("entity.enderman.stare");
            case "PHANTOM" -> KillFXFeature.se("entity.phantom.bite");
            case "WOLF" -> KillFXFeature.se("entity.wolf.howl");
            case "CAT" -> KillFXFeature.se("entity.cat.hiss");
            case "ALLAY_ITEM" -> KillFXFeature.se("entity.allay.item_given");
            case "BEE_STING" -> KillFXFeature.se("entity.bee.sting");
            case "RAVAGER_ROAR" -> KillFXFeature.se("entity.ravager.roar");
            default -> KillFXFeature.se("entity.warden.sonic_boom");
        };
    }

    private static SoundEvent funS(String n) {
        return switch (n) {
            case "BURP" -> KillFXFeature.se("entity.player.burp");
            case "PLING" -> KillFXFeature.se("block.note_block.pling");
            case "GOAT" -> KillFXFeature.se("entity.goat.screaming.milk");
            case "NO" -> KillFXFeature.se("entity.villager.no");
            case "YES" -> KillFXFeature.se("entity.villager.yes");
            case "EAT" -> KillFXFeature.se("entity.generic.eat");
            case "TOAST" -> KillFXFeature.se("ui.toast.challenge_complete");
            case "GLASS" -> KillFXFeature.se("block.glass.break");
            case "VILLAGER_CELEBRATE" -> KillFXFeature.se("entity.villager.celebrate");
            case "VILLAGER_TRADE" -> KillFXFeature.se("entity.villager.trade");
            case "BELL_RESONATE" -> KillFXFeature.se("block.bell.resonate");
            case "NOTE_BIT" -> KillFXFeature.se("block.note_block.bit");
            case "NOTE_BANJO" -> KillFXFeature.se("block.note_block.banjo");
            default -> KillFXFeature.se("block.note_block.pling");
        };
    }

    private static void spawnParticles(ClientLevel l, ParticleOptions p, double x, double y, double z, KillFXConfig cfg) {
        int c = Math.min(cfg.particleCount, 200);
        double s = cfg.particleSpeed;
        switch (cfg.particleShape) {
            case "Burst": {
                KillFXFeature.burst(l, p, x, y, z, c, s);
                break;
            }
            case "Sphere": {
                KillFXFeature.sphere(l, p, x, y, z, c, s);
                break;
            }
            case "Spiral": {
                KillFXFeature.spiral(l, p, x, y, z, c, s);
                break;
            }
            case "Column": {
                KillFXFeature.column(l, p, x, y, z, c, s);
                break;
            }
            case "Halo": {
                KillFXFeature.halo(l, p, x, y, z, c, s);
                break;
            }
            case "Heart": {
                KillFXFeature.heart(l, p, x, y, z, c, s);
                break;
            }
            case "Helix": {
                KillFXFeature.helix(l, p, x, y, z, c, s);
                break;
            }
            case "Star": {
                KillFXFeature.star(l, p, x, y, z, c, s);
                break;
            }
            case "Ring": {
                KillFXFeature.ring(l, p, x, y, z, c, s);
                break;
            }
            default: {
                KillFXFeature.burst(l, p, x, y, z, c, s);
            }
        }
    }

    private static void burst(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random();
        c = Math.min(c, 200);
        for (int i = 0; i < c; ++i) {
            try {
                l.addParticle(p, x, y, z, (r.nextDouble() - 0.5) * s * 2.0, (r.nextDouble() - 0.5) * s * 2.0, (r.nextDouble() - 0.5) * s * 2.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void sphere(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random();
        c = Math.min(c, 150);
        double rd = 1.5 * s;
        for (int i = 0; i < c; ++i) {
            try {
                double t = Math.PI * 2 * r.nextDouble();
                double ph = Math.acos(2.0 * r.nextDouble() - 1.0);
                l.addParticle(p, x + rd * Math.sin(ph) * Math.cos(t), y + rd * Math.sin(ph) * Math.sin(t), z + rd * Math.cos(ph), 0.0, 0.0, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void spiral(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c, 150);
        double rd = 1.0;
        double h = 2.0 * s;
        double ppl = c / 3.0;
        for (int i = 0; i < c; ++i) {
            try {
                double a = Math.PI * 2 * i / ppl;
                l.addParticle(p, x + rd * Math.cos(a), y + h * i / c, z + rd * Math.sin(a), 0.0, 0.02, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void column(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random();
        c = Math.min(c, 100);
        for (int i = 0; i < c; ++i) {
            try {
                double a = Math.PI * 2 * r.nextDouble();
                l.addParticle(p, x + 0.3 * Math.cos(a), y + r.nextDouble() * s * 2.0, z + 0.3 * Math.sin(a), 0.0, s * 0.5, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void halo(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c, 100);
        for (int i = 0; i < c; ++i) {
            try {
                double a = Math.PI * 2 * i / c;
                l.addParticle(p, x + 1.2 * Math.cos(a), y + 0.5 * s, z + 1.2 * Math.sin(a), 0.0, 0.0, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void heart(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double sc = 0.08 * s;
        c = Math.min(c, 100);
        for (int i = 0; i < c; ++i) {
            try {
                double t = Math.PI * 2 * i / c;
                l.addParticle(p, x + 16.0 * Math.pow(Math.sin(t), 3.0) * sc, y + (13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t)) * sc, z, 0.0, 0.0, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void helix(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c, 150);
        double rd = 1.0;
        double h = 2.5 * s;
        for (int i = 0; i < c; ++i) {
            try {
                double t = Math.PI * 2 * i / (c / 2.0);
                double py = y + h * i / c;
                l.addParticle(p, x + rd * Math.cos(t), py, z + rd * Math.sin(t), 0.0, 0.01, 0.0);
                if (i + 1 >= c) continue;
                l.addParticle(p, x + rd * Math.cos(t + Math.PI), py, z + rd * Math.sin(t + Math.PI), 0.0, 0.01, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void star(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double rd = 2.0 * s;
        c = Math.min(c, 150);
        for (int i = 0; i < c; ++i) {
            try {
                double t = Math.PI * 2 * i / c;
                double r = i % 2 == 0 ? rd : rd * 0.4;
                l.addParticle(p, x + r * Math.cos(t), y, z + r * Math.sin(t), 0.0, 0.02, 0.0);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    private static void ring(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double mr = 2.5 * s;
        c = Math.min(c, 150);
        for (int i = 0; i < c; ++i) {
            try {
                double a = Math.PI * 2 * i / c;
                double r = mr * (0.5 + 0.5 * Math.random());
                l.addParticle(p, x + r * Math.cos(a), y + (Math.random() - 0.5) * 0.2, z + r * Math.sin(a), Math.cos(a) * 0.05, 0.0, Math.sin(a) * 0.05);
                continue;
            }
            catch (Exception exception) {
                // ignored
            }
        }
    }

    public static int getProcessedCount() {
        return processedEntities.size();
    }

    public static KillFXShaderManager.ShaderType shaderTypeFromChinese(String cn) {
        return switch (cn) {
            case "\u9ed1\u6d1e" -> KillFXShaderManager.ShaderType.BLACKHOLE;
            case "\u6c34\u6676" -> KillFXShaderManager.ShaderType.CRYSTAL;
            case "\u5929\u5149\u5149\u675f" -> KillFXShaderManager.ShaderType.SKY_BEAM;
            case "\u5929\u5149\u73af" -> KillFXShaderManager.ShaderType.SKY_RING;
            case "\u8d85\u65b0\u661f" -> KillFXShaderManager.ShaderType.HYPERNOVA;
            case "\u5149\u7ebf\u7206\u53d1" -> KillFXShaderManager.ShaderType.RAY_BURST;
            default -> KillFXShaderManager.ShaderType.NONE;
        };
    }

    private static String mapCrystalStyle(String cn) {
        return switch (cn) {
            case "\u53d1\u5149" -> "BLOOM";
            case "\u73bb\u7483\u6298\u5c04" -> "GLASS";
            case "\u6781\u5149" -> "AURORA";
            default -> "CRYSTAL";
        };
    }

    public static String chineseFromShaderType(KillFXShaderManager.ShaderType type) {
        return switch (type) {
            case BLACKHOLE -> "\u9ed1\u6d1e";
            case CRYSTAL -> "\u6c34\u6676";
            case SKY_BEAM -> "\u5929\u5149\u5149\u675f";
            case SKY_RING -> "\u5929\u5149\u73af";
            case HYPERNOVA -> "\u8d85\u65b0\u661f";
            case RAY_BURST -> "\u5149\u7ebf\u7206\u53d1";
            default -> "\u65e0";
        };
    }
}

