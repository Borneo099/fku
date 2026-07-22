package fku.org.example.fku.features.killaura;

import fku.org.example.fku.features.killaura.KillAuraConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT})
public class KillAuraFeature {
    private static int tickCounter = 0;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        KillAuraConfig.getInstance();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        float cooldown;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = KillAuraFeature.getMc();
        if (mc == null) {
            return;
        }
        KillAuraConfig cfg = KillAuraConfig.getInstance();
        if (!cfg.enabled || mc.player == null || mc.level == null) {
            return;
        }
        if (cfg.attackCooldown && (cooldown = mc.player.getAttackStrengthScale(0.5f)) < 0.9f) {
            return;
        }
        if (++tickCounter % (cfg.delay + 1) != 0) {
            return;
        }
        double r = cfg.range;
        AABB box = mc.player.getBoundingBox().inflate(r);
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        double bestScore = Double.MAX_VALUE;
        LivingEntity singleTarget = null;
        List all = mc.level.getEntitiesOfClass(LivingEntity.class, box, e -> {
            Player p;
            if (e == mc.player || !e.isAlive()) {
                return false;
            }
            if (e instanceof Player && (p = (Player)e).isSpectator()) {
                return false;
            }
            if (cfg.playersOnly && !(e instanceof Player)) {
                return false;
            }
            if (e.distanceToSqr((Entity)mc.player) > r * r) {
                return false;
            }
            if (!cfg.whitelist.isEmpty()) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
                return cfg.whitelist.contains(id);
            }
            return true;
        });
        if (cfg.multiTarget) {
            targets.addAll(all);
        } else {
            for (Object entityObj : all) {
                LivingEntity entity = (LivingEntity)entityObj;
                double score = cfg.targetMode == 1 ? (entity.getHealth() / Math.max(entity.getMaxHealth(), 1.0f)) : mc.player.distanceToSqr((Entity)entity);
                if (!(score < bestScore)) continue;
                bestScore = score;
                singleTarget = entity;
            }
            if (singleTarget != null) {
                targets.add(singleTarget);
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        for (LivingEntity target : targets) {
            if (cfg.autoRotate && target == targets.get(0)) {
                Vec3 diff = target.getBoundingBox().getCenter().subtract(mc.player.getEyePosition(1.0f));
                float yaw = (float)Math.toDegrees(Math.atan2(-diff.x, diff.z));
                float pitch = (float)Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
                mc.player.setYRot(yaw);
                mc.player.setXRot(pitch);
            }
            if (cfg.autoSwitch) {
                int cur = mc.player.getInventory().selected;
                if (!(mc.player.getInventory().getItem(cur).getItem() instanceof SwordItem)) {
                    for (int i = 0; i < 9; ++i) {
                        if (!(mc.player.getInventory().getItem(i).getItem() instanceof SwordItem)) continue;
                        mc.player.getInventory().selected = i;
                        break;
                    }
                }
            }
            mc.player.connection.send((Packet)ServerboundInteractPacket.createAttackPacket(target, (boolean)mc.player.isShiftKeyDown()));
        }
        mc.player.swing(mc.player.getUsedItemHand());
    }
}

