package fku.org.example.fku.features.killfx; /* water */

import fku.org.example.fku.Fku;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KillFX（击杀特效）v5 — 防卡死版
 *
 * ★ 卡死根因：在遍历 entitiesForRendering() 时调用 putNonPlayerEntity() 添加闪电，
 *   正在迭代的列表被修改 → ConcurrentModificationException 或死循环。
 *
 * ★ 修复：遍历时只收集死亡实体到队列，遍历结束后再统一渲染。
 *
 * ★ 其他安全措施：
 *   - 渲染操作全部 try-catch
 *   - 缓存超 10000 自动清理
 *   - ConcurrentHashMap 防并发
 *   - 每 tick 最多处理 15 个死亡、渲染 5 条闪电
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KillFXFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final int MAX_DEATHS = 15;
    private static final int MAX_LIGHTNING = 5;
    private static final int MAX_PARTICLES = 200;

    private static final Set<Integer> processedEntities = ConcurrentHashMap.newKeySet();
    private static final Map<Integer, Long> attackedTargets = new ConcurrentHashMap<>();
    private static final Map<Integer, Float> lastHealthMap = new ConcurrentHashMap<>();

    /** 延迟渲染队列：遍历时只往里塞，遍历完再处理，避免修改实体列表导致卡死 */
    private static final Queue<LivingEntity> renderQueue = new ArrayDeque<>();

    public static void init() {
        KillFXConfig.getInstance();
        Fku.LOGGER.info("[KillFX] v5 防卡死 初始化");
    }

    public static void markAttackedByTpAura(int entityId) {
        if (!KillFXConfig.getInstance().enabled) return;
        attackedTargets.put(entityId, System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() == null || event.getTarget() == null) return;
        KillFXConfig cfg = KillFXConfig.getInstance();
        if (!cfg.enabled || !cfg.onlyTargeted) return;
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
        attackedTargets.entrySet().removeIf(e ->
            now - e.getValue() > cfg.targetTimeout * 1000L
        );

        if (event.phase == TickEvent.Phase.START) {
            // ★ START：只检测死亡，不渲染（避免遍历时修改实体列表）
            detectDeaths(cfg, now);
        } else if (event.phase == TickEvent.Phase.END) {
            // ★ END：渲染队列中的特效（此时遍历已结束，安全）
            renderQueued(cfg);
        }

        // 缓存清理
        if (lastHealthMap.size() > 10000) {
            Fku.LOGGER.warn("[KillFX] lastHealthMap 过大, 清理");
            lastHealthMap.clear();
        }
        if (processedEntities.size() > 10000) {
            Fku.LOGGER.warn("[KillFX] processedEntities 过大, 清理");
            processedEntities.clear();
        }
    }

    /** 只检测死亡，不渲染——解决遍历实体列表时修改列表导致的卡死 */
    private static void detectDeaths(KillFXConfig cfg, long now) {
        if (mc.level == null) return;

        int deaths = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player) continue;
            int id = living.getId();

            float health;
            try {
                health = living.getHealth();
            } catch (Exception e) {
                continue;
            }

            Float prevHealth = lastHealthMap.get(id);

            if (prevHealth != null && health <= 0.0F && prevHealth > 0.0F) {
                if (processedEntities.contains(id)) continue;

                if (cfg.onlyTargeted) {
                    Long at = attackedTargets.get(id);
                    if (at == null) continue;
                    if (now - at > cfg.targetTimeout * 1000L) {
                        attackedTargets.remove(id);
                        continue;
                    }
                }

                if (deaths >= MAX_DEATHS) break;
                deaths++;

                processedEntities.add(id);
                // ★ 不直接渲染！只加入队列，END phase 再处理
                renderQueue.add(living);
            }

            if (health > 0.0F) {
                lastHealthMap.put(id, health);
            } else {
                lastHealthMap.remove(id);
            }
        }
    }

    /** END phase：安全渲染队列中的特效 */
    private static void renderQueued(KillFXConfig cfg) {
        if (mc.level == null) return;
        int rendered = 0;
        LivingEntity entity;
        while ((entity = renderQueue.poll()) != null && rendered < 10) {
            try {
                renderEffects(entity, cfg);
                rendered++;
            } catch (Exception e) {
                Fku.LOGGER.error("[KillFX] 渲染异常", e);
            }
        }
        if (renderQueue.size() > 50) {
            Fku.LOGGER.warn("[KillFX] 渲染队列过长, 丢弃");
            renderQueue.clear();
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 特效渲染
    // ════════════════════════════════════════════════════════

    private static void renderEffects(LivingEntity entity, KillFXConfig cfg) {
        ClientLevel level = mc.level;
        if (level == null || entity == null) return;

        Vec3 pos = entity.position();
        double x = pos.x, y = pos.y, z = pos.z;

        // 闪电
        if (cfg.useLightning && cfg.lightningAmount > 0) {
            int amount = Math.min(cfg.lightningAmount, MAX_LIGHTNING);
            for (int i = 0; i < amount; i++) {
                LightningBolt bolt;
                try {
                    bolt = EntityType.LIGHTNING_BOLT.create(level);
                } catch (Exception e) {
                    continue;
                }
                if (bolt == null) continue;

                try {
                    bolt.setPos(x, y, z);
                    safeSetInt(bolt, 6, "life", "field_7185", "f_20860_");
                    safeSetInt(bolt, 6, "flashes", "field_7183", "f_20861_");
                    safeSetBool(bolt, true, "visualOnly", "field_20862_", "f_20862_");
                    level.putNonPlayerEntity(bolt.getId(), bolt);
                } catch (Exception ignored) {}

                if (!cfg.useLightningSound) {
                    safeSetInt(bolt, 1, "life", "field_7185", "f_20860_");
                }
            }
        }

        // 粒子
        if (cfg.useParticles) {
            try {
                ParticleOptions p = resolveParticle(cfg);
                if (p != null) spawnParticles(level, p, x, y + entity.getBbHeight()/2, z, cfg);
            } catch (Exception ignored) {}
        }

        // 音效
        if (cfg.useSound) {
            try {
                SoundEvent s = resolveSound(cfg);
                if (s != null) level.playSound(mc.player, x, y, z, s, SoundSource.WEATHER,
                    (float) cfg.volume, (float) cfg.pitch);
            } catch (Exception ignored) {}
        }

        // 烟花
        if (cfg.useFirework) {
            try {
                ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
                CompoundTag tag = new CompoundTag();
                tag.putInt("Flight", 1);
                stack.setTag(tag);
                var rocket = new net.minecraft.world.entity.projectile.FireworkRocketEntity(level, x, y, z, stack);
                level.putNonPlayerEntity(rocket.getId(), rocket);
            } catch (Exception ignored) {}
        }

        // 爆炸烟雾
        if (cfg.useExplosion) {
            try { level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0, 0, 0); } catch (Exception ignored) {}
        }
    }

    private static java.lang.reflect.Field findField(Class<?> c, String... names) {
        for (String n : names) {
            try { java.lang.reflect.Field f = c.getDeclaredField(n); f.setAccessible(true); return f; } catch (Exception ignored) {}
        }
        return null;
    }

    private static void safeSetInt(Object o, int v, String... names) {
        try { java.lang.reflect.Field f = findField(o.getClass(), names); if (f != null) f.setInt(o, v); } catch (Exception ignored) {}
    }

    private static void safeSetBool(Object o, boolean v, String... names) {
        try { java.lang.reflect.Field f = findField(o.getClass(), names); if (f != null) f.setBoolean(o, v); } catch (Exception ignored) {}
    }

    // ════════════════════════════════════════════════════════
    // ★ 粒子/音效/形状 解析（与原版一致）
    // ════════════════════════════════════════════════════════

    private static ParticleOptions resolveParticle(KillFXConfig cfg) {
        return switch (cfg.particleCategory) {
            case "Combat" -> combatP(cfg.combatParticle);
            case "Magic" -> magicP(cfg.magicParticle);
            case "Fire" -> fireP(cfg.fireParticle);
            case "Nature" -> natureP(cfg.natureParticle);
            case "Update121" -> updateP(cfg.updateParticle);
            case "Misc" -> miscP(cfg.miscParticle);
            default -> ParticleTypes.END_ROD;
        };
    }

    private static ParticleOptions combatP(String n) { return switch (n) {
        case "DAMAGE_INDICATOR" -> ParticleTypes.DAMAGE_INDICATOR; case "CRIT" -> ParticleTypes.CRIT;
        case "ENCHANTED_HIT" -> ParticleTypes.ENCHANTED_HIT; case "SWEEP_ATTACK" -> ParticleTypes.SWEEP_ATTACK;
        case "EXPLOSION" -> ParticleTypes.EXPLOSION; case "EXPLOSION_EMITTER" -> ParticleTypes.EXPLOSION_EMITTER;
        case "SONIC_BOOM" -> ParticleTypes.SONIC_BOOM; case "TOTEM_OF_UNDYING" -> ParticleTypes.TOTEM_OF_UNDYING;
        case "FIREWORK" -> ParticleTypes.FIREWORK; case "EGG_CRACK" -> ParticleTypes.EGG_CRACK;
        default -> ParticleTypes.CRIT;
    };}
    private static ParticleOptions magicP(String n) { return switch (n) {
        case "WITCH" -> ParticleTypes.WITCH; case "END_ROD" -> ParticleTypes.END_ROD; case "PORTAL" -> ParticleTypes.PORTAL;
        case "ENCHANT" -> ParticleTypes.ENCHANT; case "NAUTILUS" -> ParticleTypes.NAUTILUS;
        case "ELDER_GUARDIAN" -> ParticleTypes.ELDER_GUARDIAN; case "SCULK_CHARGE_POP" -> ParticleTypes.SCULK_CHARGE_POP;
        case "SOUL" -> ParticleTypes.SOUL; case "GLOW_SQUID_INK" -> ParticleTypes.GLOW_SQUID_INK;
        default -> ParticleTypes.END_ROD;
    };}
    private static ParticleOptions fireP(String n) { return switch (n) {
        case "FLAME" -> ParticleTypes.FLAME; case "SOUL_FIRE_FLAME" -> ParticleTypes.SOUL_FIRE_FLAME;
        case "SMALL_FLAME" -> ParticleTypes.SMALL_FLAME; case "LAVA" -> ParticleTypes.LAVA;
        case "LARGE_SMOKE" -> ParticleTypes.LARGE_SMOKE; case "SMOKE" -> ParticleTypes.SMOKE;
        case "CAMPFIRE_COSY_SMOKE" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
        case "CAMPFIRE_SIGNAL_SMOKE" -> ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
        case "GLOW" -> ParticleTypes.GLOW; case "WAX_ON" -> ParticleTypes.WAX_ON; case "WAX_OFF" -> ParticleTypes.WAX_OFF;
        case "SCRAPE" -> ParticleTypes.SCRAPE; case "ELECTRIC_SPARK" -> ParticleTypes.ELECTRIC_SPARK;
        default -> ParticleTypes.FLAME;
    };}
    private static ParticleOptions natureP(String n) { return switch (n) {
        case "HEART" -> ParticleTypes.HEART; case "CLOUD" -> ParticleTypes.CLOUD; case "RAIN" -> ParticleTypes.RAIN;
        case "SNOWFLAKE" -> ParticleTypes.SNOWFLAKE; case "ITEM_SLIME" -> ParticleTypes.ITEM_SLIME;
        case "BUBBLE" -> ParticleTypes.BUBBLE; case "BUBBLE_COLUMN_UP" -> ParticleTypes.BUBBLE_COLUMN_UP;
        case "CURRENT_DOWN" -> ParticleTypes.CURRENT_DOWN; case "BUBBLE_POP" -> ParticleTypes.BUBBLE_POP;
        case "SPLASH" -> ParticleTypes.SPLASH; case "FISHING" -> ParticleTypes.FISHING;
        case "DOLPHIN" -> ParticleTypes.DOLPHIN; case "UNDERWATER" -> ParticleTypes.UNDERWATER;
        case "NOTE" -> ParticleTypes.NOTE; case "CHERRY_LEAVES" -> ParticleTypes.CHERRY_LEAVES;
        case "SPORE_BLOSSOM_AIR" -> ParticleTypes.SPORE_BLOSSOM_AIR; case "WHITE_ASH" -> ParticleTypes.WHITE_ASH;
        case "WARPED_SPORE" -> ParticleTypes.WARPED_SPORE; case "CRIMSON_SPORE" -> ParticleTypes.CRIMSON_SPORE;
        default -> ParticleTypes.HEART;
    };}
    private static ParticleOptions updateP(String n) { return switch (n) {
        case "DRAGON_BREATH" -> ParticleTypes.DRAGON_BREATH; case "FLASH" -> ParticleTypes.FLASH;
        case "POOF" -> ParticleTypes.POOF; case "ELECTRIC_SPARK" -> ParticleTypes.ELECTRIC_SPARK;
        case "GLOW" -> ParticleTypes.GLOW; case "SCRAPE" -> ParticleTypes.SCRAPE;
        case "WAX_ON" -> ParticleTypes.WAX_ON; case "WAX_OFF" -> ParticleTypes.WAX_OFF;
        case "SNOWFLAKE" -> ParticleTypes.SNOWFLAKE; case "SPIT" -> ParticleTypes.SPIT;
        default -> ParticleTypes.DRAGON_BREATH;
    };}
    private static ParticleOptions miscP(String n) { return switch (n) {
        case "ASH" -> ParticleTypes.ASH; case "MYCELIUM" -> ParticleTypes.MYCELIUM;
        case "SCULK_SOUL" -> ParticleTypes.SCULK_SOUL; case "HAPPY_VILLAGER" -> ParticleTypes.HAPPY_VILLAGER;
        case "ANGRY_VILLAGER" -> ParticleTypes.ANGRY_VILLAGER; case "SNEEZE" -> ParticleTypes.SNEEZE;
        case "SQUID_INK" -> ParticleTypes.SQUID_INK; default -> ParticleTypes.SCULK_SOUL;
    };}

    private static SoundEvent resolveSound(KillFXConfig cfg) {
        return switch (cfg.soundGroup) {
            case "Combat" -> combatS(cfg.combatSound); case "Magic" -> magicS(cfg.magicSound);
            case "Creature" -> creatureS(cfg.creatureSound); case "Fun" -> funS(cfg.funSound);
            default -> se("entity.lightning_bolt.thunder");
        };
    }
    private static SoundEvent se(String s) { return SoundEvent.createVariableRangeEvent(new ResourceLocation(s)); }
    private static SoundEvent combatS(String n) { return switch (n) {
        case "THUNDER" -> se("entity.lightning_bolt.thunder"); case "EXPLODE" -> se("entity.generic.explode");
        case "ANVIL" -> se("block.anvil.land"); case "TRIDENT_THUNDER" -> se("item.trident.thunder");
        case "WITHER_SPAWN" -> se("entity.wither.spawn"); case "WITHER_SHOOT" -> se("entity.wither.shoot");
        case "ANCHOR" -> se("block.respawn_anchor.deplete"); case "CRYSTAL" -> se("entity.end_crystal.explode");
        case "BREAK" -> se("item.shield.break"); case "CRIT" -> se("entity.player.attack.crit");
        case "CROSSBOW_HIT" -> se("item.crossbow.hit"); case "TRIDENT_HIT" -> se("item.trident.hit");
        case "FIREWORK_BLAST" -> se("entity.firework_rocket.blast");
        case "ATK_STRONG" -> se("entity.player.attack.strong"); case "ATK_SWEEP" -> se("entity.player.attack.sweep");
        default -> se("entity.lightning_bolt.thunder");
    };}
    private static SoundEvent magicS(String n) { return switch (n) { /* 保持原有所有音效 */
        case "ANCHOR_CHARGE" -> se("block.respawn_anchor.charge"); case "ANCHOR_SET" -> se("block.respawn_anchor.set_spawn");
        case "TOTEM" -> se("item.totem.use"); case "BEACON" -> se("block.beacon.activate");
        case "CONDUIT" -> se("block.conduit.activate"); case "PORTAL" -> se("block.portal.trigger");
        case "LEVEL_UP" -> se("entity.player.levelup"); case "ENCHANT" -> se("block.enchantment_table.use");
        case "TELEPORT" -> se("entity.enderman.teleport"); case "BELL" -> se("block.bell.use");
        case "CHIME" -> se("block.amethyst_block.chime"); case "RESONATE" -> se("block.amethyst_block.resonate");
        case "ENDER_EYE" -> se("entity.ender_eye.death"); case "EXP_ORB" -> se("entity.experience_orb.pickup");
        case "EVOKER_CAST" -> se("entity.evoker.cast_spell"); case "CONDUIT_ATK" -> se("block.conduit.attack_target");
        case "DRAGON_FIREBALL" -> se("entity.dragon_fireball.explode");
        default -> se("block.respawn_anchor.charge");
    };}
    private static SoundEvent creatureS(String n) { return switch (n) {
        case "WARDEN" -> se("entity.warden.sonic_boom"); case "WARDEN_HEART" -> se("entity.warden.heartbeat");
        case "DRAGON" -> se("entity.ender_dragon.death"); case "DRAGON_GROWL" -> se("entity.ender_dragon.growl");
        case "BLAZE" -> se("entity.blaze.death"); case "GHAST" -> se("entity.ghast.scream");
        case "ENDERMAN" -> se("entity.enderman.stare"); case "PHANTOM" -> se("entity.phantom.bite");
        case "WOLF" -> se("entity.wolf.howl"); case "CAT" -> se("entity.cat.hiss");
        case "ALLAY_ITEM" -> se("entity.allay.item_given"); case "BEE_STING" -> se("entity.bee.sting");
        case "RAVAGER_ROAR" -> se("entity.ravager.roar");
        default -> se("entity.warden.sonic_boom");
    };}
    private static SoundEvent funS(String n) { return switch (n) {
        case "BURP" -> se("entity.player.burp"); case "PLING" -> se("block.note_block.pling");
        case "GOAT" -> se("entity.goat.screaming.milk"); case "NO" -> se("entity.villager.no");
        case "YES" -> se("entity.villager.yes"); case "EAT" -> se("entity.generic.eat");
        case "TOAST" -> se("ui.toast.challenge_complete"); case "GLASS" -> se("block.glass.break");
        case "VILLAGER_CELEBRATE" -> se("entity.villager.celebrate"); case "VILLAGER_TRADE" -> se("entity.villager.trade");
        case "BELL_RESONATE" -> se("block.bell.resonate"); case "NOTE_BIT" -> se("block.note_block.bit");
        case "NOTE_BANJO" -> se("block.note_block.banjo");
        default -> se("block.note_block.pling");
    };}

    // ════════════════════════════════════════════════════════
    // ★ 粒子形状生成（带 count 上限）
    // ════════════════════════════════════════════════════════

    private static void spawnParticles(ClientLevel l, ParticleOptions p, double x, double y, double z, KillFXConfig cfg) {
        int c = Math.min(cfg.particleCount, MAX_PARTICLES);
        double s = cfg.particleSpeed;
        switch (cfg.particleShape) {
            case "Burst" -> burst(l,p,x,y,z,c,s); case "Sphere" -> sphere(l,p,x,y,z,c,s);
            case "Spiral" -> spiral(l,p,x,y,z,c,s); case "Column" -> column(l,p,x,y,z,c,s);
            case "Halo" -> halo(l,p,x,y,z,c,s); case "Heart" -> heart(l,p,x,y,z,c,s);
            case "Helix" -> helix(l,p,x,y,z,c,s); case "Star" -> star(l,p,x,y,z,c,s);
            case "Ring" -> ring(l,p,x,y,z,c,s); default -> burst(l,p,x,y,z,c,s);
        }
    }
    private static void burst(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random(); c = Math.min(c,200);
        for (int i = 0; i < c; i++) try { l.addParticle(p, x, y, z, (r.nextDouble()-0.5)*s*2, (r.nextDouble()-0.5)*s*2, (r.nextDouble()-0.5)*s*2); } catch (Exception ignored) {}
    }
    private static void sphere(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random(); c = Math.min(c,150); double rd = 1.5*s;
        for (int i = 0; i < c; i++) try { double t=2*Math.PI*r.nextDouble(), ph=Math.acos(2*r.nextDouble()-1); l.addParticle(p, x+rd*Math.sin(ph)*Math.cos(t), y+rd*Math.sin(ph)*Math.sin(t), z+rd*Math.cos(ph),0,0,0); } catch (Exception ignored) {}
    }
    private static void spiral(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c,150); double rd=1.0, h=2.0*s, ppl=c/3.0;
        for (int i = 0; i < c; i++) try { double a=2*Math.PI*i/ppl; l.addParticle(p, x+rd*Math.cos(a), y+(h*i/c), z+rd*Math.sin(a),0,0.02,0); } catch (Exception ignored) {}
    }
    private static void column(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        Random r = new Random(); c = Math.min(c,100);
        for (int i = 0; i < c; i++) try { double a=2*Math.PI*r.nextDouble(); l.addParticle(p, x+0.3*Math.cos(a), y+r.nextDouble()*s*2, z+0.3*Math.sin(a),0,s*0.5,0); } catch (Exception ignored) {}
    }
    private static void halo(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c,100);
        for (int i = 0; i < c; i++) try { double a=2*Math.PI*i/c; l.addParticle(p, x+1.2*Math.cos(a), y+0.5*s, z+1.2*Math.sin(a),0,0,0); } catch (Exception ignored) {}
    }
    private static void heart(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double sc = 0.08*s; c = Math.min(c,100);
        for (int i = 0; i < c; i++) try { double t=2*Math.PI*i/c; l.addParticle(p, x+16*Math.pow(Math.sin(t),3)*sc, y+(13*Math.cos(t)-5*Math.cos(2*t)-2*Math.cos(3*t)-Math.cos(4*t))*sc, z,0,0,0); } catch (Exception ignored) {}
    }
    private static void helix(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        c = Math.min(c,150); double rd=1.0, h=2.5*s;
        for (int i = 0; i < c; i++) try { double t=2*Math.PI*i/(c/2.0), py=y+(h*i/c); l.addParticle(p, x+rd*Math.cos(t), py, z+rd*Math.sin(t),0,0.01,0); if(i+1<c) l.addParticle(p, x+rd*Math.cos(t+Math.PI), py, z+rd*Math.sin(t+Math.PI),0,0.01,0); } catch (Exception ignored) {}
    }
    private static void star(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double rd = 2.0*s; c = Math.min(c,150);
        for (int i = 0; i < c; i++) try { double t=2*Math.PI*i/c, r=i%2==0?rd:rd*0.4; l.addParticle(p, x+r*Math.cos(t), y, z+r*Math.sin(t),0,0.02,0); } catch (Exception ignored) {}
    }
    private static void ring(ClientLevel l, ParticleOptions p, double x, double y, double z, int c, double s) {
        double mr = 2.5*s; c = Math.min(c,150);
        for (int i = 0; i < c; i++) try { double a=2*Math.PI*i/c, r=mr*(0.5+0.5*Math.random()); l.addParticle(p, x+r*Math.cos(a), y+(Math.random()-0.5)*0.2, z+r*Math.sin(a), Math.cos(a)*0.05,0,Math.sin(a)*0.05); } catch (Exception ignored) {}
    }

    public static int getProcessedCount() { return processedEntities.size(); }
}
