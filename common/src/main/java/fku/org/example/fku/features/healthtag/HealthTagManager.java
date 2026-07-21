package fku.org.example.fku.features.healthtag;

import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.features.arrowdmg.ArrowDmgFeature;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

public class HealthTagManager {
    private static LivingEntity targetEntity;
    private static long lastAttackTime;
    private static final long DISPLAY_DURATION = 2000L;

    public static void tick() {
        boolean holdingBow;
        Minecraft mc = Minecraft.getInstance();
        if (mc.f_91073_ == null || mc.player == null) {
            return;
        }
        ItemStack mainHand = mc.player.m_21205_();
        ItemStack offHand = mc.player.m_21206_();
        boolean bl = holdingBow = ArrowDmgFeature.isBowItem(mainHand) || ArrowDmgFeature.isBowItem(offHand);
        if (holdingBow) {
            HealthTagManager.findAndLockBestTarget(mc);
        }
    }

    private static void findAndLockBestTarget(Minecraft mc) {
        long timeSinceLastUpdate;
        Entity t;
        if (ArrowDmgFeature.isEnabled() && (t = ArrowDmgFeature.getTarget()) instanceof LivingEntity) {
            LivingEntity lt;
            targetEntity = lt = (LivingEntity)t;
            lastAttackTime = System.currentTimeMillis();
            return;
        }
        Vec3 eyePos = mc.player.m_146892_();
        Vec3 lookVec = mc.player.getLookAngle();
        List entities = mc.f_91073_.m_45933_((Entity)mc.player, mc.player.m_20191_().m_82400_(128.0));
        LivingEntity bestCandidate = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : entities) {
            double score;
            double maxAllowedAngle;
            double angle;
            Vec3 entityPos;
            Vec3 entityVec;
            double distance;
            PartEntity part;
            Entity entity2;
            LivingEntity living;
            if (!(entity instanceof LivingEntity) || !(living = (LivingEntity) entity).m_6084_() || living == mc.player) continue;
            if (entity instanceof PartEntity && (entity2 = (part = (PartEntity)entity).getParent()) instanceof LivingEntity) {
                LivingEntity parent;
                living = parent = (LivingEntity) entity2;
            }
            if ((distance = (entityVec = (entityPos = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0)).subtract(eyePos)).length()) > 128.0 || !((angle = Math.toDegrees(Math.acos(lookVec.dot(entityVec.normalize())))) < (maxAllowedAngle = 15.0 / (1.0 + distance * 0.1))) || !((score = angle * (1.0 + distance * 0.05)) < bestScore)) continue;
            bestScore = score;
            bestCandidate = living;
        }
        if (bestCandidate != null) {
            targetEntity = bestCandidate;
            lastAttackTime = System.currentTimeMillis();
        } else if (targetEntity != null && (timeSinceLastUpdate = System.currentTimeMillis() - lastAttackTime) < 1500L) {
            lastAttackTime = System.currentTimeMillis() - 1500L;
        }
    }

    public static void onAttack(Entity entity) {
        if (!HealthTagConfig.getInstance().enabled) {
            return;
        }
        HealthTagManager.updateTarget(entity);
    }

    public static void onEntityHurt(LivingEntity entity) {
        if (!HealthTagConfig.getInstance().enabled) {
            return;
        }
        if (entity == targetEntity) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    private static void updateTarget(Entity entity) {
        PartEntity part;
        Entity parent;
        Entity actualTarget = entity;
        if (entity instanceof PartEntity && (parent = (part = (PartEntity)entity).getParent()) != null) {
            actualTarget = parent;
        }
        if (actualTarget instanceof LivingEntity) {
            LivingEntity living;
            targetEntity = living = (LivingEntity)actualTarget;
            lastAttackTime = System.currentTimeMillis();
        }
    }

    public static LivingEntity getTargetEntity() {
        return targetEntity;
    }

    public static float getAlpha() {
        if (HealthTagManager.isEditing()) {
            return 0.8f;
        }
        if (targetEntity == null) {
            return 0.0f;
        }
        long timeSinceLastAttack = System.currentTimeMillis() - lastAttackTime;
        if (timeSinceLastAttack > 2000L) {
            return 0.0f;
        }
        if (timeSinceLastAttack < 1000L) {
            return 0.5f;
        }
        float fadeProgress = (timeSinceLastAttack - 1000L) / 1000.0f;
        return 0.5f * (1.0f - fadeProgress);
    }

    public static boolean shouldDisplay() {
        if (!HealthTagConfig.getInstance().enabled) {
            return false;
        }
        if (HealthTagManager.isEditing()) {
            return true;
        }
        return targetEntity != null && HealthTagManager.getAlpha() > 0.0f;
    }

    public static boolean isEditing() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen instanceof ClickGuiScreen;
    }
}

