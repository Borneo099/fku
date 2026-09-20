package fku.org.example.fku.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GUI 动态背景系统 — 纯视觉装饰，不拦截任何交互
 * 提供多种灵动背景：雪花落下 / 彩带飘落 / 樱花飞舞 / 气泡上升 / 星河闪烁 / 流光溢彩
 */
public class GuiBackground {

    public static final int STYLE_SNOW = 0;
    public static final int STYLE_CONFETTI = 1;
    public static final int STYLE_SAKURA = 2;
    public static final int STYLE_BUBBLE = 3;
    public static final int STYLE_STARS = 4;
    public static final int STYLE_AURORA = 5;

    public static final String[] STYLE_NAMES = {
            "雪花落下", "彩带飘落", "樱花飞舞", "气泡上升", "星河闪烁", "流光溢彩"
    };

    private static final int[] CONFETTI = {0xFF6B6B, 0xFFD93D, 0x6BCB77, 0x4D96FF, 0xFF6FB5, 0xB983FF};
    private static final int[] SAKURA = {0xFFC0CB, 0xFFB7C5, 0xFF9EB5, 0xFFD1DC};
    private static final int[] BUBBLE = {0x9AD0EC, 0xBFE9FF, 0xCDEFFF};
    private static final int[] AURORA_COLS = {0x66FFCC, 0xB983FF, 0x4D96FF};

    private int style = STYLE_SNOW;
    private boolean enabled = false;
    private int screenW = 0, screenH = 0;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rand = new Random();
    private long lastTime = 0;
    private double auroraPhase = 0;
    private int builtStyle = -1;

    public void setStyle(int s) { this.style = Math.max(0, Math.min(5, s)); }
    public void setEnabled(boolean e) { this.enabled = e; }
    public void resize(int w, int h) { this.screenW = w; this.screenH = h; }
    public int getStyle() { return style; }

    private int targetCount() {
        if (style == STYLE_AURORA) return 0;
        if (style == STYLE_STARS) return 130;
        return 90;
    }

    private void ensureParticles() {
        if (builtStyle != style) {
            particles.clear();
            builtStyle = style;
        }
        int n = targetCount();
        while (particles.size() < n) particles.add(newParticle());
        while (particles.size() > n) particles.remove(particles.size() - 1);
    }

    private Particle newParticle() {
        Particle p = new Particle();
        p.type = style;
        p.x = rand.nextInt(Math.max(1, screenW));
        p.y = rand.nextInt(Math.max(1, screenH));
        p.phase = rand.nextDouble() * Math.PI * 2;
        p.vx = (rand.nextDouble() - 0.5) * 10;
        switch (style) {
            case STYLE_SNOW -> { p.vy = 18 + rand.nextDouble() * 22; p.color = 0xFFFFFF; p.size = 2 + rand.nextInt(3); }
            case STYLE_CONFETTI -> { p.vy = 40 + rand.nextDouble() * 50; p.color = CONFETTI[rand.nextInt(CONFETTI.length)]; p.size = 3 + rand.nextInt(3); }
            case STYLE_SAKURA -> { p.vy = 20 + rand.nextDouble() * 25; p.color = SAKURA[rand.nextInt(SAKURA.length)]; p.size = 3 + rand.nextInt(3); }
            case STYLE_BUBBLE -> { p.vy = -(30 + rand.nextDouble() * 40); p.color = BUBBLE[rand.nextInt(BUBBLE.length)]; p.size = 3 + rand.nextInt(5); }
            case STYLE_STARS -> { p.vy = 2 + rand.nextDouble() * 4; p.color = 0xFFFFFF; p.size = 1 + rand.nextInt(2); }
            default -> { p.vy = 20; p.color = 0xFFFFFF; p.size = 2; }
        }
        return p;
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (lastTime == 0) lastTime = now;
        long dt = now - lastTime;
        lastTime = now;
        if (dt > 100) dt = 16;
        float dts = dt / 1000f;
        auroraPhase += dts * 0.3;
        if (style == STYLE_AURORA) return;
        ensureParticles();
        int w = Math.max(1, screenW), h = Math.max(1, screenH);
        for (Particle p : particles) {
            p.phase += dts * 2f;
            switch (p.type) {
                case STYLE_SNOW -> { p.y += p.vy * dts; p.x += Math.sin(p.phase) * 8 * dts; }
                case STYLE_CONFETTI -> { p.y += p.vy * dts; p.x += Math.sin(p.phase) * 20 * dts; }
                case STYLE_SAKURA -> { p.y += p.vy * dts; p.x += Math.sin(p.phase) * 25 * dts; }
                case STYLE_BUBBLE -> { p.y += p.vy * dts; p.x += Math.sin(p.phase) * 15 * dts; }
                case STYLE_STARS -> { p.y += p.vy * dts; p.x += Math.cos(p.phase) * 3 * dts; }
                default -> { p.y += p.vy * dts; }
            }
            if (p.y > h + 5) { p.y = -5; p.x = rand.nextInt(w); }
            if (p.y < -10) { p.y = h + 5; p.x = rand.nextInt(w); }
            if (p.x < -10) p.x = w + 5;
            if (p.x > w + 10) p.x = -5;
        }
    }

    public void render(GuiGraphics g) {
        if (!enabled || screenW <= 0) return;
        if (style == STYLE_AURORA) { renderAurora(g); return; }
        for (Particle p : particles) {
            int alpha;
            int col;
            switch (p.type) {
                case STYLE_SNOW -> {
                    alpha = 200; col = (alpha << 24) | 0xFFFFFF;
                    GuiRenderHelper.drawCircle(g, (int) p.x, (int) p.y, p.size, col);
                }
                case STYLE_CONFETTI -> {
                    alpha = 220; col = (alpha << 24) | (p.color & 0xFFFFFF);
                    int sway = (int) (Math.sin(p.phase) * 4);
                    g.fill((int) p.x, (int) p.y, (int) p.x + p.size + Math.abs(sway), (int) p.y + p.size, col);
                }
                case STYLE_SAKURA -> {
                    alpha = 200; col = (alpha << 24) | (p.color & 0xFFFFFF);
                    int s = Math.max(1, (int) (p.size + Math.sin(p.phase) * 1.5));
                    GuiRenderHelper.drawCircle(g, (int) p.x, (int) p.y, s, col);
                }
                case STYLE_BUBBLE -> {
                    alpha = 120; col = (alpha << 24) | (p.color & 0xFFFFFF);
                    GuiRenderHelper.drawCircle(g, (int) p.x, (int) p.y, p.size, col);
                }
                case STYLE_STARS -> {
                    double tw = (Math.sin(p.phase) + 1) / 2;
                    alpha = (int) (60 + 160 * tw); col = (alpha << 24) | 0xFFFFFF;
                    GuiRenderHelper.drawCircle(g, (int) p.x, (int) p.y, p.size, col);
                }
                default -> {
                    alpha = 200; col = (alpha << 24) | 0xFFFFFF;
                    GuiRenderHelper.drawCircle(g, (int) p.x, (int) p.y, p.size, col);
                }
            }
        }
    }

    private void renderAurora(GuiGraphics g) {
        int h = screenH;
        for (int i = 0; i < AURORA_COLS.length; i++) {
            double off = Math.sin(auroraPhase + i * 2.0) * 0.18;
            int x = (int) (off * screenW);
            int y = (int) (h * (0.18 + i * 0.24));
            int bandH = (int) (h * 0.18);
            int a = (int) (38 + 26 * Math.sin(auroraPhase * 1.5 + i));
            int col = (Math.max(0, Math.min(255, a)) << 24) | AURORA_COLS[i];
            GuiRenderHelper.drawRoundedRect(g, x, y, screenW, bandH, col, 30);
        }
    }

    private static class Particle {
        int type;
        double x, y;
        double vx, vy;
        double phase;
        int size;
        int color;
    }
}
