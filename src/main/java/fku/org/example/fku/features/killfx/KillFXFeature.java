package fku.org.example.fku.features.killfx; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

/**
 * KillFX（击杀特效）核心逻辑类
 *
 * ★ 职责：
 *   1. 监听 LivingDeathEvent 检测生物死亡，触发特效
 *   2. 监听 AttackEntityEvent 记录玩家攻击过的目标
 *   3. 生成闪电、粒子特效（多种形状）、音效、烟花、爆炸烟雾
 *
 * ★ 设计思想：
 *   使用事件驱动（LivingDeathEvent）替代原参考代码的 Tick 轮询，
 *   大幅降低 CPU 占用。粒子形状生成逻辑直接移植自 Meteor Client 的 KillFX 模组，
 *   采用数学公式计算粒子位置。
 *
 * ★ 参考来源：
 *   KillFX.java (Meteor Client Addon) - 粒子形状算法、音效/闪电生成逻辑
 *   原代码位于 prompt.txt
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KillFXFeature {

    private static final Minecraft mc = Minecraft.getInstance();

    /** 已处理实体ID集合（避免重复触发） */
    private static final Set<Integer> processedEntities = new HashSet<>();
    /** 攻击记录映射：实体ID → 攻击时间戳 */
    private static final Map<Integer, Long> attackedTargets = new HashMap<>();
    /** 健康度追踪映射：实体ID → 上一帧血量（用于客户端死亡检测） */
    private static final Map<Integer, Float> lastHealthMap = new HashMap<>();

    // ════════════════════════════════════════════════════════
    // ★ 事件监听
    // ════════════════════════════════════════════════════════

    /** 初始化：加载配置 */
    public static void init() {
        KillFXConfig.getInstance();
        Fku.LOGGER.info("[KillFX] 功能已初始化");
    }

    /**
     * ★ TpAura 联动接口
     *
     * 供 TpAuraFeature 在发包攻击后调用，手动记录目标被攻击，
     * 解决 TpAura 绕过 AttackEntityEvent 导致 onlyTargeted 模式不触发击杀特效的问题。
     *
     * @param entityId 被攻击实体的 ID（Entity.getId()）
     */
    public static void markAttackedByTpAura(int entityId) {
        if (!KillFXConfig.getInstance().enabled) return;
        attackedTargets.put(entityId, System.currentTimeMillis());
    }

    /**
     * 攻击事件：记录玩家攻击过的实体ID及其时间戳
     * 用于 onlyTargeted 模式判断
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() == null) return;
        KillFXConfig cfg = KillFXConfig.getInstance();
        if (!cfg.enabled || !cfg.onlyTargeted) return;

        Entity target = event.getTarget();
        attackedTargets.put(target.getId(), System.currentTimeMillis());
    }

    /**
     * Tick 事件：
     *   1. 清理过期攻击记录和已处理记录
     *   2. ★ 健康度追踪：替代 LivingDeathEvent，纯客户端检测死亡
     *
     * ★ 矛盾定性（服务端失效问题）：
     *   LivingDeathEvent 在 Forge 专用服务器上不会向客户端侧触发，
     *   导致 KillFX 在服务器上完全失效。
     *
     * ★ 实践路线：
     *   放弃事件驱动，改用每 Tick 轮询实体健康度（health）的变化。
     *   通过比对上一帧血量与当前血量，当血量从 >0 跌到 ≤0 时视为死亡。
     *   此方法纯客户端实现，无需服务端支持。
     *
     * ★ 参考来源：
     *   Meteor Client KillAura 的实体健康度追踪模式
     *   net.minecraft.world.entity.LivingEntity#getHealth()
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        KillFXConfig cfg = KillFXConfig.getInstance();
        if (!cfg.enabled) {
            if (!processedEntities.isEmpty()) processedEntities.clear();
            if (!attackedTargets.isEmpty()) attackedTargets.clear();
            if (!lastHealthMap.isEmpty()) lastHealthMap.clear();
            return;
        }

        // 清理过期攻击记录
        long now = System.currentTimeMillis();
        attackedTargets.entrySet().removeIf(entry ->
            now - entry.getValue() > cfg.targetTimeout * 1000
        );

        // ★ 健康度追踪：检测生物死亡
        if (mc.level == null) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player) continue;
            int id = living.getId();
            float health = living.getHealth();
            Float prevHealth = lastHealthMap.get(id);

            // 血量从 >0 跌到 ≤0 → 死亡
            if (prevHealth != null && health <= 0.0F && prevHealth > 0.0F) {
                if (processedEntities.contains(id)) continue;

                // ★ onlyTargeted 模式检查
                if (cfg.onlyTargeted) {
                    Long attackTime = attackedTargets.get(id);
                    if (attackTime == null) continue;
                    if (now - attackTime > cfg.targetTimeout * 1000) {
                        attackedTargets.remove(id);
                        continue;
                    }
                }

                processedEntities.add(id);
                renderEffects(living, cfg);
            }

            // 更新血量追踪：只跟踪活着的实体，死亡的从映射中移除
            if (health > 0.0F) {
                lastHealthMap.put(id, health);
            } else {
                lastHealthMap.remove(id);
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 特效渲染入口
    // ════════════════════════════════════════════════════════

    /**
     * 渲染击杀特效（在 Tick 中执行以确保 ClientLevel 可用）
     * 此方法在 Minecraft.execute() 中运行，保证主线程安全
     */
    private static void renderEffects(LivingEntity entity, KillFXConfig cfg) {
        ClientLevel level = mc.level;
        if (level == null) return;

        Vec3 pos = entity.position();
        double x = pos.x, y = pos.y, z = pos.z;

        // ★ 闪电
        //   ★ 修复：在 Forge 1.20.1 中，LightningBolt 默认 life=2 但 visualOnly=false，
        //     通过反射扩展 bolt.life 为 4（标准闪电持续时间），使其有充足时间渲染。
        //     同时追加 setGlowing(true) 增强可见性。
        //     参考来源：net.minecraft.world.entity.LightningBolt 源码
        if (cfg.useLightning && cfg.lightningAmount > 0) {
            for (int i = 0; i < cfg.lightningAmount; i++) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.setPos(x, y, z);
                    // ★ 使用反射延长闪电生命周期和闪烁次数，确保客户端有充足时间渲染
                //   矛盾定性（三次分析）：
                //     第一轮：MCP 映射 "life" → IDE 有效，reobf JAR 需要 SRG 名
                //     第二轮：错误推测 SRG 名为 "field_70070_d" → 实际是 "field_7185"
                //     第三轮（本轮）：根据 mappings.dev 确认正确字段映射：
                //       - life: MCP="life", SRG="field_7185", Intermediary="f_20860_"
                //       - flashes: MCP="flashes", SRG="field_7183", Intermediary="f_20861_"
                //     实践路线：依次尝试 MCP→SRG→Intermediary 三级回退，
                //     同时设置 life（生命周期）和 flashes（闪烁次数），
                //     使用 addWeatherEffect 替代 addFreshEntity（天气效果专用方法）。
                try {
                    // 设置 life（生命周期）
                    java.lang.reflect.Field lifeField = tryGetField(LightningBolt.class,
                            "life", "field_7185", "f_20860_");
                    if (lifeField != null) {
                        lifeField.setAccessible(true);
                        lifeField.setInt(lightning, 6);
                    }
                    // 设置 flashes（闪烁次数）
                    java.lang.reflect.Field flashesField = tryGetField(LightningBolt.class,
                            "flashes", "field_7183", "f_20861_");
                    if (flashesField != null) {
                        flashesField.setAccessible(true);
                        flashesField.setInt(lightning, 6);
                    }
                    // 设置 visualOnly（仅视觉效果，不产生伤害/火灾）
                    // 参考来源：net.minecraft.world.entity.LightningBolt 源码
                    java.lang.reflect.Field visualOnlyField = tryGetField(LightningBolt.class,
                            "visualOnly", "field_20862_", "f_20862_");
                    if (visualOnlyField != null) {
                        visualOnlyField.setAccessible(true);
                        visualOnlyField.setBoolean(lightning, true);
                    }
                } catch (Exception ignored) {
                    // 所有映射名均失败，保留默认值
                }
                // ★ 使用 ClientLevel.putNonPlayerEntity() 添加闪电到客户端层级
                //   MCP 映射确认方法名为 putNonPlayerEntity(int, Entity)。
                //   这与 Meteor Client 的 mc.world.addEntity(lightning) 效果一致，
                //   在 Forge 1.20.1 MCP 中方法名为 putNonPlayerEntity。
                //   参考来源：net.minecraft.client.multiplayer.ClientLevel 源码
                level.putNonPlayerEntity(lightning.getId(), lightning);

                // ★ 闪电音效开关（v2.1）
                //   LightningBolt.tick() 中当 life==2 时播放雷声。
                //   关闭音效后立即将 life 设为 1，使其跳过 life==2 的音效触发点。
                //   视觉闪光由 flashes 字段控制，不受 life 影响。
                if (!cfg.useLightningSound) {
                    try {
                        java.lang.reflect.Field lifeField = tryGetField(LightningBolt.class,
                                "life", "field_7185", "f_20860_");
                        if (lifeField != null) {
                            lifeField.setAccessible(true);
                            lifeField.setInt(lightning, 1);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

        // ★ 粒子
        if (cfg.useParticles) {
            ParticleOptions particle = resolveParticle(cfg);
            if (particle != null) {
                spawnParticles(level, particle, x, y + entity.getBbHeight() / 2, z, cfg);
            }
        }

        // ★ 音效
        if (cfg.useSound) {
            SoundEvent sound = resolveSound(cfg);
            if (sound != null) {
                // ★ 使用 SoundSource.WEATHER（比 AMBIENT 音量更显著），
                //   且不受玩家"环境音效"滑块影响。
                //   参考来源：ClientLevel.playSound 参数说明
                level.playSound(mc.player, x, y, z, sound, SoundSource.WEATHER,
                        (float) cfg.volume, (float) cfg.pitch);
            }
        }

        // ★ 烟花
        //   使用 putNonPlayerEntity 替代 addFreshEntity：
        //   addFreshEntity 在客户端侧无效（isClientSide 检查返回 false），
        //   putNonPlayerEntity 直接将实体加入客户端的 entityStorage，
        //   实体 tick 正常执行，流星轨迹和爆炸粒子均可渲染。
        //   参考来源：ClientLevel.putNonPlayerEntity() MCP 映射
        if (cfg.useFirework) {
            ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
            CompoundTag tag = new CompoundTag();
            tag.putInt("Flight", 1);
            fireworkStack.setTag(tag);
            var rocket = new net.minecraft.world.entity.projectile.FireworkRocketEntity(
                    level, x, y, z, fireworkStack);
            level.putNonPlayerEntity(rocket.getId(), rocket);
        }

        // ★ 爆炸烟雾
        if (cfg.useExplosion) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0, 0, 0);
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 粒子解析
    // ════════════════════════════════════════════════════════

    /**
     * 根据配置解析当前选中的粒子类型
     */
    private static ParticleOptions resolveParticle(KillFXConfig cfg) {
        return switch (cfg.particleCategory) {
            case "Combat" -> resolveCombatParticle(cfg.combatParticle);
            case "Magic" -> resolveMagicParticle(cfg.magicParticle);
            case "Fire" -> resolveFireParticle(cfg.fireParticle);
            case "Nature" -> resolveNatureParticle(cfg.natureParticle);
            case "Update121" -> resolveUpdateParticle(cfg.updateParticle);
            case "Misc" -> resolveMiscParticle(cfg.miscParticle);
            default -> ParticleTypes.END_ROD;
        };
    }

    // ---------- 战斗粒子 ----------
    private static ParticleOptions resolveCombatParticle(String name) {
        return switch (name) {
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

    // ---------- 魔法粒子 ----------
    private static ParticleOptions resolveMagicParticle(String name) {
        return switch (name) {
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

    // ---------- 火焰粒子 ----------
    private static ParticleOptions resolveFireParticle(String name) {
        return switch (name) {
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

    // ---------- 自然粒子 ----------
    private static ParticleOptions resolveNatureParticle(String name) {
        return switch (name) {
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

    // ---------- 特殊/稀有粒子（仅含 Forge 1.20.1 有效 SimpleParticleType）----------
    private static ParticleOptions resolveUpdateParticle(String name) {
        return switch (name) {
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

    // ---------- 其他粒子 ----------
    private static ParticleOptions resolveMiscParticle(String name) {
        return switch (name) {
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

    // ════════════════════════════════════════════════════════
    // ★ 音效解析
    // ════════════════════════════════════════════════════════

    /**
     * 根据配置解析当前选中的音效
     */
    private static SoundEvent resolveSound(KillFXConfig cfg) {
        return switch (cfg.soundGroup) {
            case "Combat" -> resolveCombatSound(cfg.combatSound);
            case "Magic" -> resolveMagicSound(cfg.magicSound);
            case "Creature" -> resolveCreatureSound(cfg.creatureSound);
            case "Fun" -> resolveFunSound(cfg.funSound);
            default -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.lightning_bolt.thunder"));
        };
    }

    /** 战斗音效 */
    private static SoundEvent resolveCombatSound(String name) {
        return switch (name) {
            case "THUNDER" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.lightning_bolt.thunder"));
            case "EXPLODE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.generic.explode"));
            case "ANVIL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.anvil.land"));
            case "TRIDENT_THUNDER" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("item.trident.thunder"));
            case "WITHER_SPAWN" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.wither.spawn"));
            case "WITHER_SHOOT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.wither.shoot"));
            case "ANCHOR" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.respawn_anchor.deplete"));
            case "CRYSTAL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.end_crystal.explode"));
            case "BREAK" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("item.shield.break"));
            case "CRIT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.attack.crit"));
            case "CROSSBOW_HIT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("item.crossbow.hit"));
            case "TRIDENT_HIT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("item.trident.hit"));
            case "FIREWORK_BLAST" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.firework_rocket.blast"));
            case "ATK_STRONG" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.attack.strong"));
            case "ATK_SWEEP" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.attack.sweep"));
            default -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.lightning_bolt.thunder"));
        };
    }

    /** 魔法音效 */
    private static SoundEvent resolveMagicSound(String name) {
        return switch (name) {
            case "ANCHOR_CHARGE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.respawn_anchor.charge"));
            case "ANCHOR_SET" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.respawn_anchor.set_spawn"));
            case "TOTEM" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("item.totem.use"));
            case "BEACON" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.beacon.activate"));
            case "CONDUIT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.conduit.activate"));
            case "PORTAL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.portal.trigger"));
            case "LEVEL_UP" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.levelup"));
            case "ENCHANT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.enchantment_table.use"));
            case "TELEPORT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.enderman.teleport"));
            case "BELL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.bell.use"));
            case "CHIME" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.amethyst_block.chime"));
            case "RESONATE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.amethyst_block.resonate"));
            case "ENDER_EYE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.ender_eye.death"));
            case "EXP_ORB" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.experience_orb.pickup"));
            case "EVOKER_CAST" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.evoker.cast_spell"));
            case "CONDUIT_ATK" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.conduit.attack_target"));
            case "DRAGON_FIREBALL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.dragon_fireball.explode"));
            default -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.respawn_anchor.charge"));
        };
    }

    /** 生物音效 */
    private static SoundEvent resolveCreatureSound(String name) {
        return switch (name) {
            case "WARDEN" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.warden.sonic_boom"));
            case "WARDEN_HEART" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.warden.heartbeat"));
            case "DRAGON" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.ender_dragon.death"));
            case "DRAGON_GROWL" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.ender_dragon.growl"));
            case "BLAZE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.blaze.death"));
            case "GHAST" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.ghast.scream"));
            case "ENDERMAN" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.enderman.stare"));
            case "PHANTOM" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.phantom.bite"));
            case "WOLF" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.wolf.howl"));
            case "CAT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.cat.hiss"));
            case "ALLAY_ITEM" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.allay.item_given"));
            case "BEE_STING" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.bee.sting"));
            case "RAVAGER_ROAR" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.ravager.roar"));
            default -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.warden.sonic_boom"));
        };
    }

    /** 趣味音效 */
    private static SoundEvent resolveFunSound(String name) {
        return switch (name) {
            case "BURP" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.player.burp"));
            case "PLING" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.note_block.pling"));
            case "GOAT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.goat.screaming.milk"));
            case "NO" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.villager.no"));
            case "YES" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.villager.yes"));
            case "EAT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.generic.eat"));
            case "TOAST" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ui.toast.challenge_complete"));
            case "GLASS" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.glass.break"));
            case "VILLAGER_CELEBRATE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.villager.celebrate"));
            case "VILLAGER_TRADE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("entity.villager.trade"));
            case "BELL_RESONATE" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.bell.resonate"));
            case "NOTE_BIT" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.note_block.bit"));
            case "NOTE_BANJO" -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.note_block.banjo"));
            default -> SoundEvent.createVariableRangeEvent(new ResourceLocation("block.note_block.pling"));
        };
    }

    // ════════════════════════════════════════════════════════
    // ★ 粒子形状生成
    // ★ 参考来源：KillFX.java (Meteor Client Addon)
    // ════════════════════════════════════════════════════════

    /**
     * 根据配置的形状在指定位置生成粒子
     */
    private static void spawnParticles(ClientLevel level, ParticleOptions particle,
                                        double x, double y, double z, KillFXConfig cfg) {
        int count = cfg.particleCount;
        double speed = cfg.particleSpeed;
        String shape = cfg.particleShape;

        switch (shape) {
            case "Burst" -> spawnBurst(level, particle, x, y, z, count, speed);
            case "Sphere" -> spawnSphere(level, particle, x, y, z, count, speed);
            case "Spiral" -> spawnSpiral(level, particle, x, y, z, count, speed);
            case "Column" -> spawnColumn(level, particle, x, y, z, count, speed);
            case "Halo" -> spawnHalo(level, particle, x, y, z, count, speed);
            case "Heart" -> spawnHeart(level, particle, x, y, z, count, speed);
            case "Helix" -> spawnHelix(level, particle, x, y, z, count, speed);
            case "Star" -> spawnStar(level, particle, x, y, z, count, speed);
            case "Ring" -> spawnRing(level, particle, x, y, z, count, speed);
            default -> spawnBurst(level, particle, x, y, z, count, speed);
        }
    }

    /** 爆炸散开：随机方向辐射 */
    private static void spawnBurst(ClientLevel level, ParticleOptions particle,
                                    double x, double y, double z, int count, double speed) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            double vx = (rand.nextDouble() - 0.5) * speed * 2;
            double vy = (rand.nextDouble() - 0.5) * speed * 2;
            double vz = (rand.nextDouble() - 0.5) * speed * 2;
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    /** 球体包裹：粒子均匀分布在球面上 */
    private static void spawnSphere(ClientLevel level, ParticleOptions particle,
                                     double x, double y, double z, int count, double speed) {
        double radius = 1.5 * speed;
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            double theta = 2 * Math.PI * rand.nextDouble();
            double phi = Math.acos(2 * rand.nextDouble() - 1);
            double px = x + radius * Math.sin(phi) * Math.cos(theta);
            double py = y + radius * Math.sin(phi) * Math.sin(theta);
            double pz = z + radius * Math.cos(phi);
            level.addParticle(particle, px, py, pz, 0, 0, 0);
        }
    }

    /** 螺旋上升 */
    private static void spawnSpiral(ClientLevel level, ParticleOptions particle,
                                     double x, double y, double z, int count, double speed) {
        double radius = 1.0;
        double height = 2.0 * speed;
        int loops = 3;
        double particlesPerLoop = (double) count / loops;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / particlesPerLoop;
            double py = y + (height * i / count);
            double px = x + radius * Math.cos(angle);
            double pz = z + radius * Math.sin(angle);
            level.addParticle(particle, px, py, pz, 0, 0.02, 0);
        }
    }

    /** 光柱升天：竖直向上喷发 */
    private static void spawnColumn(ClientLevel level, ParticleOptions particle,
                                     double x, double y, double z, int count, double speed) {
        Random rand = new Random();
        double radius = 0.3;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * rand.nextDouble();
            double px = x + radius * Math.cos(angle);
            double pz = z + radius * Math.sin(angle);
            double py = y + rand.nextDouble() * speed * 2;
            level.addParticle(particle, px, py, pz, 0, speed * 0.5, 0);
        }
    }

    /** 头顶光环：水平圆环 */
    private static void spawnHalo(ClientLevel level, ParticleOptions particle,
                                   double x, double y, double z, int count, double speed) {
        double radius = 1.2;
        double heightOffset = 0.5 * speed;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double px = x + radius * Math.cos(angle);
            double pz = z + radius * Math.sin(angle);
            double py = y + heightOffset;
            level.addParticle(particle, px, py, pz, 0, 0, 0);
        }
    }

    /** 爱心轮廓：参数方程 (x=16sin³t, y=13cost-5cos2t-2cos3t-cos4t) */
    private static void spawnHeart(ClientLevel level, ParticleOptions particle,
                                    double x, double y, double z, int count, double speed) {
        double scale = 0.08 * speed;
        for (int i = 0; i < count; i++) {
            double t = 2 * Math.PI * i / count;
            double hx = 16 * Math.pow(Math.sin(t), 3);
            double hy = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            double px = x + hx * scale;
            double py = y + hy * scale;
            double pz = z;
            level.addParticle(particle, px, py, pz, 0, 0, 0);
        }
    }

    /** 双螺旋DNA */
    private static void spawnHelix(ClientLevel level, ParticleOptions particle,
                                    double x, double y, double z, int count, double speed) {
        double radius = 1.0;
        double height = 2.5 * speed;
        for (int i = 0; i < count; i++) {
            double t = 2 * Math.PI * i / (count / 2.0);
            double py = y + (height * i / count);
            // 两条螺旋链
            double px1 = x + radius * Math.cos(t);
            double pz1 = z + radius * Math.sin(t);
            level.addParticle(particle, px1, py, pz1, 0, 0.01, 0);
            if (i + 1 < count) {
                double px2 = x + radius * Math.cos(t + Math.PI);
                double pz2 = z + radius * Math.sin(t + Math.PI);
                level.addParticle(particle, px2, py, pz2, 0, 0.01, 0);
            }
        }
    }

    /** 五角星阵 */
    private static void spawnStar(ClientLevel level, ParticleOptions particle,
                                   double x, double y, double z, int count, double speed) {
        double radius = 2.0 * speed;
        int points = 5;
        // 绘制星形轮廓
        for (int i = 0; i < count; i++) {
            double t = 2 * Math.PI * i / count;
            double r = (i % 2 == 0) ? radius : radius * 0.4;
            double px = x + r * Math.cos(t);
            double py = y;
            double pz = z + r * Math.sin(t);
            level.addParticle(particle, px, py, pz, 0, 0.02, 0);
        }
    }

    /** 冲击波圆环：向外扩散的环 */
    private static void spawnRing(ClientLevel level, ParticleOptions particle,
                                   double x, double y, double z, int count, double speed) {
        double maxRadius = 2.5 * speed;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double r = maxRadius * (0.5 + 0.5 * Math.random()); // 随机半径制造扩散效果
            double px = x + r * Math.cos(angle);
            double pz = z + r * Math.sin(angle);
            double py = y + (Math.random() - 0.5) * 0.2;
            level.addParticle(particle, px, py, pz, Math.cos(angle) * 0.05, 0, Math.sin(angle) * 0.05);
        }
    }

    // ════════════════════════════════════════════════════════
    // ★ 工具方法
    // ════════════════════════════════════════════════════════

    /**
     * 三级回退反射字段查找（MCP → SRG → Intermediary）
     * 解决 reobf JAR 中字段名被混淆的问题
     */
    private static java.lang.reflect.Field tryGetField(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    /** 获取已处理实体数（用于GUI调试） */
    public static int getProcessedCount() {
        return processedEntities.size();
    }

    /** 清空所有缓存 */
    public static void clearCache() {
        processedEntities.clear();
        attackedTargets.clear();
    }
}