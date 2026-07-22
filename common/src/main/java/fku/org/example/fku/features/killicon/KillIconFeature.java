package fku.org.example.fku.features.killicon;

import fku.org.example.fku.features.killicon.KillIconConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", value={Dist.CLIENT})
public class KillIconFeature {
    private static final Map<Integer, Float> lastHealthMap = new ConcurrentHashMap<Integer, Float>();
    private static final Map<Integer, Long> attackedTargets = new ConcurrentHashMap<Integer, Long>();
    private static final Set<Integer> processedDeaths = new HashSet<Integer>();
    private static final Map<Integer, AttackRecord> recentAttacks = new ConcurrentHashMap<Integer, AttackRecord>();
    private static final long ATTACK_TIMEOUT_MS = 5000L;
    private static final List<KillEntry> killHistory = new ArrayList<KillEntry>();
    private static boolean lastHeadshot = false;
    private static boolean dragging = false;
    private static int dragOffsetX;
    private static int dragOffsetY;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void markAttackedByTpAura(int entityId) {
        if (!KillIconConfig.getInstance().enabled) {
            return;
        }
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null) {
            return;
        }
        attackedTargets.put(entityId, System.currentTimeMillis());
        Vec3 pos = mc.player != null ? mc.player.position() : Vec3.ZERO;
        recentAttacks.put(entityId, new AttackRecord(System.currentTimeMillis(), lastHeadshot, pos, mc.player != null ? mc.player.getHealth() : 20.0f));
        lastHeadshot = false;
    }

    public static void markHeadshot(boolean hs) {
        lastHeadshot = hs;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null || mc.level == null) {
            return;
        }
        long now = System.currentTimeMillis();
        attackedTargets.entrySet().removeIf(e -> now - (Long)e.getValue() > 5000L);
        for (Entity entity : mc.level.entitiesForRendering()) {
            Long at;
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (entity == mc.player) continue;
            int id = living.getId();
            float health = living.getHealth();
            Float prevHealth = lastHealthMap.get(id);
            if (prevHealth != null && health <= 0.0f && prevHealth.floatValue() > 0.0f && processedDeaths.add(id) && (at = attackedTargets.get(id)) != null && now - at <= 5000L) {
                KillIconFeature.addKillEntry(living);
            }
            if (health > 0.0f) {
                lastHealthMap.put(id, health);
                continue;
            }
            lastHealthMap.remove(id);
        }
        if (processedDeaths.size() > 1000) {
            processedDeaths.clear();
        }
    }

    private static void addKillEntry(LivingEntity victim) {
        double d;
        boolean hs;
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null) {
            return;
        }
        AttackRecord rec = recentAttacks.remove(victim.getId());
        if (rec != null && System.currentTimeMillis() - rec.time <= 5000L) {
            hs = rec.headshot || lastHeadshot;
            d = rec.pos.distanceTo(victim.position());
        } else {
            hs = lastHeadshot;
            d = mc.player.distanceTo((Entity)victim);
        }
        lastHeadshot = false;
        long now = System.currentTimeMillis();
        int combo = !killHistory.isEmpty() && now - KillIconFeature.killHistory.get(0).time < 5000L ? KillIconFeature.killHistory.get(0).combo + 1 : 1;
        killHistory.add(0, new KillEntry(victim.getName().getString(), now, hs, victim instanceof Player, d, combo));
        int max = KillIconConfig.getInstance().maxEntries;
        while (killHistory.size() > max) {
            killHistory.remove(killHistory.size() - 1);
        }
    }

    public static void onEntityRemoved(Entity entity) {
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) {
            return;
        }
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity victim = (LivingEntity) entity;
        int id = victim.getId();
        if (!processedDeaths.add(id)) {
            return;
        }
        Long at = attackedTargets.get(id);
        if (at == null || System.currentTimeMillis() - at > 5000L) {
            return;
        }
        KillIconFeature.addKillEntry(victim);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null || event.getEntity() != mc.player || event.getTarget() == null) {
            return;
        }
        int id = event.getTarget().getId();
        attackedTargets.put(id, System.currentTimeMillis());
        recentAttacks.put(id, new AttackRecord(System.currentTimeMillis(), lastHeadshot, mc.player.position(), mc.player.getHealth()));
        lastHeadshot = false;
    }

    public static boolean onMouseClicked(double mx, double my, int btn) {
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) {
            return false;
        }
        KillIconConfig cfg = KillIconConfig.getInstance();
        int areaW = 80;
        int areaH = 20;
        if (!killHistory.isEmpty()) {
            areaW = 0;
            for (KillEntry e : killHistory) {
                int w = mc.font.width(e.displayText);
                if (w <= areaW) continue;
                areaW = w;
            }
            areaH = killHistory.size() * (cfg.entryHeight + cfg.entrySpacing);
        }
        if (mx >= (cfg.x - 4) && mx <= (cfg.x + areaW + 4) && my >= (cfg.y - 4) && my <= (cfg.y + areaH + 4) && btn == 0) {
            dragging = true;
            dragOffsetX = (int)(mx - cfg.x);
            dragOffsetY = (int)(my - cfg.y);
            return true;
        }
        return false;
    }

    public static void onMouseDragged(double mx, double my, int btn) {
        if (dragging && btn == 0) {
            KillIconConfig cfg = KillIconConfig.getInstance();
            cfg.x = (int)Math.max(0, mx - dragOffsetX);
            cfg.y = (int)Math.max(0, my - dragOffsetY);
        }
    }

    public static void onMouseReleased(double mx, double my, int btn) {
        if (dragging && btn == 0) {
            dragging = false;
            KillIconConfig.save();
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        KillIconFeature.renderKillIcons(event.getGuiGraphics(), true);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (KillIconFeature.onMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        KillIconFeature.onMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
    }

    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        KillIconFeature.onMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
    }

    private static void renderKillIcons(GuiGraphics g, boolean screenOpen) {
        Minecraft mc = KillIconFeature.getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) {
            return;
        }
        KillIconConfig cfg = KillIconConfig.getInstance();
        long now = System.currentTimeMillis();
        killHistory.removeIf(e -> now - e.time > cfg.displayDuration * 50L);
        if (processedDeaths.size() > 1000) {
            processedDeaths.clear();
        }
        Font font = mc.font;
        int totalW = 0;
        int totalH = 0;
        if (!killHistory.isEmpty()) {
            totalH = killHistory.size() * (cfg.entryHeight + cfg.entrySpacing);
            for (KillEntry e2 : killHistory) {
                int w = font.width(e2.displayText);
                if (w <= totalW) continue;
                totalW = w;
            }
        }
        if (totalW == 0) {
            totalW = 80;
        }
        if (screenOpen) {
            int pad = 4;
            g.fill(cfg.x - pad, cfg.y - pad, cfg.x + totalW + pad, cfg.y + totalH + pad + 12, 0x44000000);
            g.renderOutline(cfg.x - pad, cfg.y - pad, totalW + pad * 2, totalH + pad * 2 + 12, -1438349057);
            g.drawString(font, "\u00a77\u283f \u62d6\u62fd\u51fb\u6740\u56fe\u6807", cfg.x, cfg.y + totalH + 2, -1996488705);
            if (killHistory.isEmpty()) {
                cfg.x = Math.max(cfg.x, 10);
                cfg.y = Math.max(cfg.y, 10);
            }
        }
        for (int i = killHistory.size() - 1; i >= 0; --i) {
            KillEntry e2;
            e2 = killHistory.get(i);
            float age = (now - e2.time) / (cfg.displayDuration * 50);
            float alpha = 1.0f - Math.max(0.0f, age - 0.5f) * 2.0f;
            if (alpha <= 0.0f) continue;
            int yPos = cfg.y + i * (cfg.entryHeight + cfg.entrySpacing);
            int color = ((int)(alpha * 255.0f)) << 24 | 0xFFFFFF;
            if (cfg.showBackground) {
                int bw = font.width(e2.displayText) + 8;
                g.fill(cfg.x - 2, yPos - 1, cfg.x + bw, yPos + 10, ((int)(alpha * cfg.bgOpacity * 255.0f)) << 24 | 0);
            }
            g.drawString(font, e2.displayText, cfg.x, yPos, color);
        }
    }

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiOverlayEvent.Post event) {
        KillIconFeature.renderKillIcons(event.getGuiGraphics(), false);
    }

    private static class AttackRecord {
        final long time;
        final boolean headshot;
        final Vec3 pos;
        final float health;

        AttackRecord(long t, boolean h, Vec3 p, float hl) {
            this.time = t;
            this.headshot = h;
            this.pos = p;
            this.health = hl;
        }
    }

    private static class KillEntry {
        final String name;
        final long time;
        final boolean headshot;
        final boolean isPlayer;
        final double dist;
        final int combo;
        String displayText;

        KillEntry(String n, long t, boolean hs, boolean ip, double d, int c) {
            this.name = n;
            this.time = t;
            this.headshot = hs;
            this.isPlayer = ip;
            this.dist = d;
            this.combo = c;
            this.buildText();
        }

        void buildText() {
            StringBuilder sb = new StringBuilder();
            if (this.combo > 1) {
                sb.append("\u00a76[").append(this.combo).append("\u8fde\u6740] ");
            }
            if (this.headshot) {
                sb.append("\u00a7c\u2620 ");
            } else {
                sb.append(this.isPlayer ? "\u00a7c\u2726 " : "\u00a7e\u2727 ");
            }
            sb.append("\u00a7f").append(this.name);
            if (this.dist > 30.0) {
                sb.append(" \u00a77(").append(this.dist).append("m)");
            }
            this.displayText = sb.toString();
        }
    }
}

