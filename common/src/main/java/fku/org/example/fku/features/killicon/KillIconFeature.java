package fku.org.example.fku.features.killicon; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class KillIconFeature {

    private static Minecraft getMc() { return Minecraft.getInstance(); }

    // ——— 击杀检测（参考 KillFX 的血量轮询 + 攻击标记方案） ———
    /** 每 tick 血量缓存，用于检测血量从 >0 变 <=0 */
    private static final Map<Integer, Float> lastHealthMap = new ConcurrentHashMap<>();
    /** 我攻击过的实体（<实体ID, 攻击时间戳>），死亡时检查此列表确认是否我击杀 */
    private static final Map<Integer, Long> attackedTargets = new ConcurrentHashMap<>();
    /** 已处理过的死亡实体ID（防重复） */
    private static final Set<Integer> processedDeaths = new HashSet<>();
    /** 攻击记录（含爆头/位置信息），用于生成更精确的击杀图标 */
    private static final Map<Integer, AttackRecord> recentAttacks = new ConcurrentHashMap<>();
    private static final long ATTACK_TIMEOUT_MS = 5000L;

    private static final List<KillEntry> killHistory = new ArrayList<>();
    private static boolean lastHeadshot = false;
    private static boolean dragging = false;
    private static int dragOffsetX, dragOffsetY;

    private static class AttackRecord {
        final long time; final boolean headshot; final Vec3 pos; final float health;
        AttackRecord(long t, boolean h, Vec3 p, float hl) { time=t; headshot=h; pos=p; health=hl; }
    }

    private static class KillEntry {
        final String name; final long time; final boolean headshot;
        final boolean isPlayer; final double dist; final int combo;
        String displayText;
        KillEntry(String n, long t, boolean hs, boolean ip, double d, int c) {
            name=n; time=t; headshot=hs; isPlayer=ip; dist=d; combo=c; buildText();
        }
        void buildText() {
            StringBuilder sb = new StringBuilder();
            if (combo > 1) sb.append("§6[").append(combo).append("连杀] ");
            if (headshot) sb.append("§c☠ ");
            else sb.append(isPlayer ? "§c✦ " : "§e✧ ");
            sb.append("§f").append(name);
            if (dist > 30) sb.append(" §7(").append((int)dist).append("m)");
            displayText = sb.toString();
        }
    }

    public static void markAttackedByTpAura(int entityId) {
        if (!KillIconConfig.getInstance().enabled) return;
        Minecraft mc = getMc();
        if (mc == null) return;
        attackedTargets.put(entityId, System.currentTimeMillis());
        Vec3 pos = mc.player != null ? mc.player.position() : Vec3.ZERO;
        recentAttacks.put(entityId, new AttackRecord(System.currentTimeMillis(), lastHeadshot, pos, mc.player != null ? mc.player.getHealth() : 20));
        lastHeadshot = false;
    }

    public static void markHeadshot(boolean hs) { lastHeadshot = hs; }

    // ═══════════ 血量轮询检测死亡（参考 KillFX） ═══════════

    /**
     * ★ 每 tick 遍历实体，检测血量从 >0 变 <=0 → 确认死亡
     *    然后检查 attackedTargets 确认是否是我击杀的
     *    （只处理我攻击过的实体死亡，避免范围任意生物死亡都触发）
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();

        // 清理过期攻击标记
        attackedTargets.entrySet().removeIf(e -> now - e.getValue() > ATTACK_TIMEOUT_MS);

        // 遍历所有实体检测死亡
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player) continue;
            int id = living.getId();
            float health = living.getHealth();
            Float prevHealth = lastHealthMap.get(id);

            // 血量从 >0 变 <=0 → 刚死亡
            if (prevHealth != null && health <= 0.0F && prevHealth > 0.0F) {
                if (processedDeaths.add(id)) {
                    // 检查是否是我攻击过的
                    Long at = attackedTargets.get(id);
                    if (at != null && now - at <= ATTACK_TIMEOUT_MS) {
                        addKillEntry(living);
                    }
                }
            }

            // 更新血量记录（只有活着才记录，死亡后移除）
            if (health > 0.0F) {
                lastHealthMap.put(id, health);
            } else {
                lastHealthMap.remove(id);
            }
        }

        // 定期清理去重集合
        if (processedDeaths.size() > 1000) processedDeaths.clear();
    }

    /** 添加击杀记录 */
    private static void addKillEntry(LivingEntity victim) {
        Minecraft mc = getMc();
        if (mc == null) return;
        AttackRecord rec = recentAttacks.remove(victim.getId());
        boolean hs;
        double d;
        if (rec != null && System.currentTimeMillis() - rec.time <= ATTACK_TIMEOUT_MS) {
            hs = rec.headshot || lastHeadshot;
            d = rec.pos.distanceTo(victim.position());
        } else {
            hs = lastHeadshot;
            d = mc.player.distanceTo(victim);
        }
        lastHeadshot = false;

        long now = System.currentTimeMillis();
        int combo = (!killHistory.isEmpty() && (now - killHistory.get(0).time) < 5000) ? killHistory.get(0).combo + 1 : 1;
        killHistory.add(0, new KillEntry(victim.getName().getString(), now, hs, victim instanceof Player, d, combo));
        int max = KillIconConfig.getInstance().maxEntries;
        while (killHistory.size() > max) killHistory.remove(killHistory.size() - 1);
    }

    /** Mixin 回调：实体被移除时的兜底处理（同样检查 attackedTargets） */
    public static void onEntityRemoved(Entity entity) {
        Minecraft mc = getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) return;
        if (!(entity instanceof LivingEntity victim)) return;
        int id = victim.getId();
        if (!processedDeaths.add(id)) return;
        Long at = attackedTargets.get(id);
        if (at == null || System.currentTimeMillis() - at > ATTACK_TIMEOUT_MS) return;
        addKillEntry(victim);
    }

    /** ★ 攻击事件：记录我攻击过的实体 */
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Minecraft mc = getMc();
        if (mc == null || event.getEntity() != mc.player || event.getTarget() == null) return;
        int id = event.getTarget().getId();
        attackedTargets.put(id, System.currentTimeMillis());
        recentAttacks.put(id, new AttackRecord(System.currentTimeMillis(), lastHeadshot, mc.player.position(), mc.player.getHealth()));
        lastHeadshot = false;
    }

    // ═══════════ 鼠标拖动 ═══════════
    public static boolean onMouseClicked(double mx, double my, int btn) {
        Minecraft mc = getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) return false;
        var cfg = KillIconConfig.getInstance();

        // 计算拖拽区域：有击杀记录时按实际内容大小，无记录时取固定最小区域（80x20）
        int areaW = 80, areaH = 20;
        if (!killHistory.isEmpty()) {
            areaW = 0;
            for (KillEntry e : killHistory) { int w = mc.font.width(e.displayText); if (w > areaW) areaW = w; }
            areaH = killHistory.size() * (cfg.entryHeight + cfg.entrySpacing);
        }
        if (mx >= cfg.x - 4 && mx <= cfg.x + areaW + 4 && my >= cfg.y - 4 && my <= cfg.y + areaH + 4) {
            if (btn == 0) { dragging = true; dragOffsetX = (int)mx - cfg.x; dragOffsetY = (int)my - cfg.y; return true; }
        }
        return false;
    }
    public static void onMouseDragged(double mx, double my, int btn) {
        if (dragging && btn == 0) { var cfg = KillIconConfig.getInstance(); cfg.x = Math.max(0, (int)mx - dragOffsetX); cfg.y = Math.max(0, (int)my - dragOffsetY); }
    }
    public static void onMouseReleased(double mx, double my, int btn) {
        if (dragging && btn == 0) { dragging = false; KillIconConfig.save(); }
    }

    // ═══════════ 事件 ═══════════
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        renderKillIcons(event.getGuiGraphics(), true);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (onMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        onMouseDragged(event.getMouseX(), event.getMouseY(), event.getMouseButton());
    }
    @SubscribeEvent
    public static void onMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        onMouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
    }

    // ═══════════ 渲染 ═══════════
    /**
     * @param screenOpen 当前是否打开了 GUI（打开时显示拖拽指示）
     */
    private static void renderKillIcons(GuiGraphics g, boolean screenOpen) {
        Minecraft mc = getMc();
        if (mc == null || !KillIconConfig.getInstance().enabled || mc.player == null) return;
        var cfg = KillIconConfig.getInstance();
        long now = System.currentTimeMillis();
        killHistory.removeIf(e -> (now - e.time) > cfg.displayDuration * 50L);
        // 清理 processedDeaths 防止内存泄漏（保留最近 1 分钟）
        if (processedDeaths.size() > 1000) processedDeaths.clear();

        var font = mc.font;

        // 计算区域宽高（用于拖拽指示）
        int totalW = 0, totalH = 0;
        if (!killHistory.isEmpty()) {
            totalH = killHistory.size() * (cfg.entryHeight + cfg.entrySpacing);
            for (KillEntry e : killHistory) {
                int w = font.width(e.displayText);
                if (w > totalW) totalW = w;
            }
        }
        if (totalW == 0) totalW = 80; // 无记录时占位

        if (screenOpen) {
            // ★ GUI打开时：显示拖拽指示边框 + 提示文字
            int pad = 4;
            g.fill(cfg.x - pad, cfg.y - pad, cfg.x + totalW + pad, cfg.y + totalH + pad + 12, 0x44000000);
            g.renderOutline(cfg.x - pad, cfg.y - pad, totalW + pad * 2, totalH + pad * 2 + 12, 0xAA4488FF);
            g.drawString(font, "§7⠿ 拖拽击杀图标", cfg.x, cfg.y + totalH + 2, 0x88FFFFFF);
            // 无历史记录时也要显示占位
            if (killHistory.isEmpty()) {
                cfg.x = Math.max(cfg.x, 10);
                cfg.y = Math.max(cfg.y, 10);
            }
        }

        // 渲染击杀记录
        for (int i = killHistory.size() - 1; i >= 0; i--) {
            KillEntry e = killHistory.get(i);
            float age = (now - e.time) / (float)(cfg.displayDuration * 50);
            float alpha = 1.0f - Math.max(0, age - 0.5f) * 2;
            if (alpha <= 0) continue;
            int yPos = cfg.y + i * (cfg.entryHeight + cfg.entrySpacing);
            int color = (int)(alpha * 255) << 24 | 0xFFFFFF;
            if (cfg.showBackground) {
                int bw = font.width(e.displayText) + 8;
                g.fill(cfg.x - 2, yPos - 1, cfg.x + bw, yPos + 10, (int)(alpha * cfg.bgOpacity * 255) << 24 | 0x000000);
            }
            g.drawString(font, e.displayText, cfg.x, yPos, color);
        }
    }

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiOverlayEvent.Post event) {
        renderKillIcons(event.getGuiGraphics(), false);
    }
}
