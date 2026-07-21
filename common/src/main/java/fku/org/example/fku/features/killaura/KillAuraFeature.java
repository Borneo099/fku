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
        if (!cfg.enabled || mc.player == null || mc.f_91073_ == null) {
            return;
        }
        if (cfg.attackCooldown && (cooldown = mc.player.m_36403_(0.5f)) < 0.9f) {
            return;
        }
        if (++tickCounter % (cfg.delay + 1) != 0) {
            return;
        }
        double r = cfg.range;
        AABB box = mc.player.m_20191_().m_82400_(r);
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        double bestScore = Double.MAX_VALUE;
        LivingEntity singleTarget = null;
        List all = mc.f_91073_.m_6443_(LivingEntity.class, box, e -> {
            Player p;
            if (e == mc.player || !e.m_6084_()) {
                return false;
            }
            if (e instanceof Player && (p = (Player)e).m_5833_()) {
                return false;
            }
            if (cfg.playersOnly && !(e instanceof Player)) {
                return false;
            }
            if (e.m_20280_((Entity)mc.player) > r * r) {
                return false;
            }
            if (!cfg.whitelist.isEmpty()) {
                String id = BuiltInRegistries.f_256780_.m_7981_(e.m_6095_()).toString();
                return cfg.whitelist.contains(id);
            }
            return true;
        });
        if (cfg.multiTarget) {
            targets.addAll(all);
        } else {
            for (LivingEntity entity : all) {
                double score = cfg.targetMode == 1 ? (entity.m_21223_() / Math.max(entity.m_21233_(), 1.0f)) : mc.player.m_20280_((Entity)entity);
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
                Vec3 diff = target.m_20191_().m_82399_().subtract(mc.player.m_20299_(1.0f));
                float yaw = Math.toDegrees(Math.atan2(-diff.x, diff.z));
                float pitch = Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
                mc.player.m_146922_(yaw);
                mc.player.m_146926_(pitch);
            }
            if (cfg.autoSwitch) {
                int cur = mc.player.m_150109_().f_35977_;
                if (!(mc.player.m_150109_().m_8020_(cur).m_41720_() instanceof SwordItem)) {
                    for (int i = 0; i < 9; ++i) {
                        if (!(mc.player.m_150109_().m_8020_(i).m_41720_() instanceof SwordItem)) continue;
                        mc.player.m_150109_().f_35977_ = i;
                        break;
                    }
                }
            }
            mc.player.f_108617_.m_104955_((Packet)ServerboundInteractPacket.m_179605_(target, (boolean)mc.player.m_6144_()));
        }
        mc.player.m_6674_(mc.player.m_7655_());
    }
}

