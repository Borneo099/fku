package fku.org.example.fku.features.attackindicator;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.attackindicator.AttackIndicatorConfig;
import fku.org.example.fku.features.attackindicator.AttackIndicatorRenderer;
import fku.org.example.fku.features.tpaura.TpAuraFeature;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT})
public class AttackIndicatorFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean initialized = false;
    private static Entity currentTarget = null;
    private static Entity previousTarget = null;
    private static int despawnCounter = 0;
    private static int ticksSinceLastAttack = 0;
    private static final int ATTACK_TIMEOUT_TICKS = 40;
    private static boolean triggeredByAttack = false;
    private static boolean triggeredByTpAura = false;
    private static final List<SwordWave> activeSwordWaves = new ArrayList<SwordWave>();

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        AttackIndicatorConfig.load();
        MinecraftForge.EVENT_BUS.register(AttackIndicatorFeature.class);
        Fku.LOGGER.info("[AttackIndicator] \u5df2\u521d\u59cb\u5316");
    }

    public static Entity getCurrentTarget() {
        return currentTarget;
    }

    public static boolean hasActiveTarget() {
        return currentTarget != null;
    }

    public static List<SwordWave> getActiveSwordWaves() {
        return activeSwordWaves;
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!AttackIndicatorConfig.getInstance().enabled) {
            return;
        }
        if (event.getEntity() != AttackIndicatorFeature.mc.player) {
            return;
        }
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        String mode = cfg.triggerMode;
        if (mode.equals("ON_TPAURA_LOCK")) {
            return;
        }
        triggeredByAttack = true;
        ticksSinceLastAttack = 0;
        AttackIndicatorFeature.setTarget(event.getTarget());
        if (cfg.enableSwordWave && event.getTarget() != null && AttackIndicatorFeature.mc.player != null) {
            Vec3 playerPos = AttackIndicatorFeature.mc.player.position().add(0.0, AttackIndicatorFeature.mc.player.getBbHeight() * 0.5, 0.0);
            Vec3 targetPos = event.getTarget().position().add(0.0, event.getTarget().getBbHeight() * 0.5, 0.0);
            activeSwordWaves.add(new SwordWave(playerPos, targetPos));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        TpAuraFeature tpAura;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (AttackIndicatorFeature.mc.player == null) {
            return;
        }
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        if (!cfg.enabled) {
            return;
        }
        if (triggeredByAttack) {
            ++ticksSinceLastAttack;
        }
        if ((tpAura = TpAuraFeature.getInstance()) != null && tpAura.currentTarget != null) {
            String mode = cfg.triggerMode;
            if (mode.equals("BOTH") || mode.equals("ON_TPAURA_LOCK")) {
                triggeredByTpAura = true;
                AttackIndicatorFeature.setTarget(tpAura.currentTarget);
            }
        } else {
            triggeredByTpAura = false;
        }
        if (triggeredByAttack && !triggeredByTpAura && ticksSinceLastAttack > 40) {
            AttackIndicatorFeature.clearTarget();
            triggeredByAttack = false;
            ticksSinceLastAttack = 0;
        }
        if (!triggeredByAttack && !triggeredByTpAura && currentTarget != null) {
            AttackIndicatorFeature.clearTarget();
        }
        Iterator<SwordWave> it = activeSwordWaves.iterator();
        while (it.hasNext()) {
            SwordWave sw = it.next();
            if (!sw.isExpired()) continue;
            it.remove();
        }
    }

    private static void setTarget(Entity target) {
        if (target == currentTarget) {
            despawnCounter = 0;
            return;
        }
        if (currentTarget != null && currentTarget != target) {
            previousTarget = currentTarget;
            despawnCounter = AttackIndicatorConfig.getInstance().despawnDelay;
        }
        currentTarget = target;
        despawnCounter = 0;
    }

    private static void clearTarget() {
        if (currentTarget != null) {
            previousTarget = currentTarget;
            despawnCounter = AttackIndicatorConfig.getInstance().despawnDelay;
        }
        currentTarget = null;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        if (!AttackIndicatorConfig.getInstance().enabled) {
            return;
        }
        if (AttackIndicatorFeature.mc.player == null) {
            return;
        }
        AttackIndicatorConfig cfg = AttackIndicatorConfig.getInstance();
        Entity entity = currentTarget;
        if (entity instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) entity;
            AttackIndicatorRenderer.renderTargetEffects(event.getPoseStack(), livingTarget, cfg);
        }
        if (currentTarget != null) {
            AttackIndicatorRenderer.renderConnectionEffects(event.getPoseStack(), AttackIndicatorFeature.mc.player, currentTarget, cfg);
        }
        if (previousTarget instanceof LivingEntity && despawnCounter > 0) {
            AttackIndicatorRenderer.renderTargetEffects(event.getPoseStack(), (LivingEntity) previousTarget, cfg);
            if (--despawnCounter <= 0) {
                previousTarget = null;
            }
        }
        Vec3 cameraPos = AttackIndicatorFeature.mc.getEntityRenderDispatcher().camera.getPosition();
        for (SwordWave sw : activeSwordWaves) {
            Vec3 camStart = sw.startPos.subtract(cameraPos);
            Vec3 camEnd = sw.targetPos.subtract(cameraPos);
            AttackIndicatorRenderer.renderSwordWave(event.getPoseStack(), camStart, camEnd, sw.getProgress(), cfg);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!AttackIndicatorConfig.getInstance().enabled) {
            return;
        }
        if (AttackIndicatorFeature.mc.player == null || currentTarget == null) {
            return;
        }
        AttackIndicatorRenderer.renderScreenOverlay(event.getGuiGraphics(), currentTarget, AttackIndicatorConfig.getInstance());
    }

    public static class SwordWave {
        public Vec3 startPos;
        public Vec3 targetPos;
        public long spawnTime;
        public static final long DURATION_MS = 600L;
        public boolean alive = true;

        public SwordWave(Vec3 start, Vec3 target) {
            this.startPos = start;
            this.targetPos = target;
            this.spawnTime = System.currentTimeMillis();
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - this.spawnTime;
            return Math.min(1.0f, elapsed / 600.0f);
        }

        public Vec3 getCurrentPos() {
            float p = this.getProgress();
            return this.startPos.lerp(this.targetPos, p);
        }

        public boolean isExpired() {
            return this.getProgress() >= 1.0f;
        }
    }
}

