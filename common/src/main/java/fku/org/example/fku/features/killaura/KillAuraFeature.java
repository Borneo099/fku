package fku.org.example.fku.features.killaura; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 杀戮光环 — 参考 Wurst 实现
 *
 * 关键差异：
 * - 用 ServerboundInteractPacket 确保攻击包到达服务端
 * - 自动旋转视角瞄准目标
 * - 实体类型白名单过滤
 * - 攻击硬直（Cooldown）检测
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class KillAuraFeature {
    private static Minecraft getMc() { return Minecraft.getInstance(); }
    private static int tickCounter = 0;

    public static void init() { KillAuraConfig.getInstance(); }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = getMc();
        if (mc == null) return;
        var cfg = KillAuraConfig.getInstance();
        if (!cfg.enabled || mc.player == null || mc.level == null) return;

        // Cooldown 检测（攻击冷却开关）
        if (cfg.attackCooldown) {
            float cooldown = mc.player.getAttackStrengthScale(0.5f);
            if (cooldown < 0.9f) return;
        }

        tickCounter++;
        if (tickCounter % (cfg.delay + 1) != 0) return;

        // 查找目标
        double r = cfg.range;
        AABB box = mc.player.getBoundingBox().inflate(r);
        List<LivingEntity> targets = new ArrayList<>();
        double bestScore = Double.MAX_VALUE;
        LivingEntity singleTarget = null;

        java.util.List<LivingEntity> all = mc.level.getEntitiesOfClass(LivingEntity.class, box, (LivingEntity e) -> {
            if (e == mc.player || !e.isAlive()) return false;
            if (e instanceof Player p && p.isSpectator()) return false;
            if (cfg.playersOnly && !(e instanceof Player)) return false;
            if (e.distanceToSqr(mc.player) > r * r) return false;
            if (!cfg.whitelist.isEmpty()) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                return cfg.whitelist.contains(id);
            }
            return true;
        });

        if (cfg.multiTarget) {
            // ★ 多目标模式：攻击所有符合条件的实体
            targets.addAll(all);
        } else {
            // 单目标模式：选择最近/最低血的
            for (var entity : all) {
                double score;
                if (cfg.targetMode == 1) {
                    score = entity.getHealth() / Math.max(entity.getMaxHealth(), 1);
                } else {
                    score = mc.player.distanceToSqr(entity);
                }
                if (score < bestScore) { bestScore = score; singleTarget = entity; }
            }
            if (singleTarget != null) targets.add(singleTarget);
        }

        if (targets.isEmpty()) return;

        // 对每个目标执行攻击
        for (LivingEntity target : targets) {
            // 自动旋转瞄准第一个目标
            if (cfg.autoRotate && target == targets.get(0)) {
                Vec3 diff = target.getBoundingBox().getCenter().subtract(mc.player.getEyePosition(1));
                float yaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
                float pitch = (float) Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
                mc.player.setYRot(yaw);
                mc.player.setXRot(pitch);
            }

            // 自动切剑
            if (cfg.autoSwitch) {
                int cur = mc.player.getInventory().selected;
                if (!(mc.player.getInventory().getItem(cur).getItem() instanceof SwordItem)) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.player.getInventory().getItem(i).getItem() instanceof SwordItem) {
                            mc.player.getInventory().selected = i;
                            break;
                        }
                    }
                }
            }

            mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(target, mc.player.isShiftKeyDown()));
        }
        mc.player.swing(mc.player.getUsedItemHand());
    }
}
