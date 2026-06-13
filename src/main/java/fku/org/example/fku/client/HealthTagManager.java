package fku.org.example.fku.client;

import fku.org.example.fku.config.HealthTagConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.entity.PartEntity;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class HealthTagManager {
    private static LivingEntity targetEntity;
    private static long lastAttackTime;
    private static final long DISPLAY_DURATION = 2000; // 2 seconds

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // --- 核心逻辑：持弓瞄准时寻找并锁定目标 ---
        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        boolean holdingBow = mainHand.getItem() instanceof BowItem || offHand.getItem() instanceof BowItem || 
                             mainHand.getItem() instanceof CrossbowItem || offHand.getItem() instanceof CrossbowItem;

        if (holdingBow) {
            findAndLockBestTarget(mc);
        }
    }

    private static void findAndLockBestTarget(Minecraft mc) {
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        
        List<Entity> entities = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(128.0));
        LivingEntity bestCandidate = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || living == mc.player) continue;
            if (entity instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity parent) {
                living = parent;
            }

            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
            Vec3 entityVec = entityPos.subtract(eyePos);
            double distance = entityVec.length();
            if (distance > 128.0) continue;

            // 计算夹角
            double angle = Math.toDegrees(Math.acos(lookVec.dot(entityVec.normalize())));
            
            // --- 动态锥形判定逻辑 ---
            // 近处实体允许的角度更大（容易锁定），远处实体允许的角度更小（需要精准瞄准）
            // 基础角度 15度，每增加 10格距离，允许的角度缩小
            double maxAllowedAngle = 15.0 / (1.0 + distance * 0.1); 
            
            if (angle < maxAllowedAngle) {
                // 计算评分：角度越小、距离越近，评分越低（越优先）
                // 引入距离权重，防止远处的小角度实体抢走近处大角度实体的焦点
                double score = angle * (1.0 + distance * 0.05); 
                if (score < bestScore) {
                    bestScore = score;
                    bestCandidate = living;
                }
            }
        }

        if (bestCandidate != null) {
            targetEntity = bestCandidate;
            lastAttackTime = System.currentTimeMillis(); // 蓄力时持续重置计时，保持 UI 显示
        } else if (targetEntity != null) {
            // 如果当前持弓但准星没对准任何实体，缩短淡出时间（使其更快消失）
            long timeSinceLastUpdate = System.currentTimeMillis() - lastAttackTime;
            if (timeSinceLastUpdate < 1500) {
                lastAttackTime = System.currentTimeMillis() - 1500; // 强制进入最后 0.5s 淡出阶段
            }
        }
    }

    public static void onAttack(Entity entity) {
        if (!HealthTagConfig.getInstance().enabled) return;
        updateTarget(entity);
    }

    public static void onEntityHurt(LivingEntity entity) {
        if (!HealthTagConfig.getInstance().enabled) return;
        
        // 远程伤害命中时，如果是当前目标则更新时间
        if (entity == targetEntity) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    private static void updateTarget(Entity entity) {
        Entity actualTarget = entity;
        
        // 处理多部位实体（如末影龙）
        if (entity instanceof PartEntity<?> part) {
            Entity parent = part.getParent();
            if (parent != null) {
                actualTarget = parent;
            }
        }

        if (actualTarget instanceof LivingEntity living) {
            targetEntity = living;
            lastAttackTime = System.currentTimeMillis();
        }
    }

    public static LivingEntity getTargetEntity() {
        return targetEntity;
    }

    public static float getAlpha() {
        if (isEditing()) return 0.8f;
        if (targetEntity == null) return 0f;
        
        long timeSinceLastAttack = System.currentTimeMillis() - lastAttackTime;
        if (timeSinceLastAttack > DISPLAY_DURATION) return 0f;
        
        if (timeSinceLastAttack < 1000) {
            return 0.5f;
        } else {
            float fadeProgress = (timeSinceLastAttack - 1000) / 1000f;
            return 0.5f * (1f - fadeProgress);
        }
    }

    public static boolean shouldDisplay() {
        if (!HealthTagConfig.getInstance().enabled) return false;
        if (isEditing()) return true;
        return targetEntity != null && getAlpha() > 0;
    }

    public static boolean isEditing() {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen instanceof fku.org.example.fku.client.gui.ClickGuiScreen;
    }
}
