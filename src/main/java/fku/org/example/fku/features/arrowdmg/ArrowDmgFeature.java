package fku.org.example.fku.features.arrowdmg; /* water */

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ArrowDmgFeature {

    private static final Minecraft mc = Minecraft.getInstance();
    private static boolean forcedPress = false;
    private static Entity target = null;
    /** 存储目标原始碰撞箱（渲染用） */
    private static AABB targetOriginalBox = null;
    /** 自动下蹲释放计时器 */
    private static int crouchReleaseTimer = 0;

    /** ★ 从配置文件静默恢复开关状态 */
    public static void init() {
        ArrowDmgConfig.load();
    }

    public static void toggleEnabled() { setEnabled(!isEnabled()); }
    public static void setEnabled(boolean v) {
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
        if (!v) { if (forcedPress) { mc.options.keyUse.setDown(false); forcedPress = false; } target = null; }
    }
    public static boolean isEnabled() { return ArrowDmgConfig.getInstance().enabled; }
    /** 获取当前自瞄目标（供 HealthTag 联动） */
    public static Entity getTarget() { return target; }

    /**
     * ★ Mixin 调用：手动释放弓时（连射关闭）执行 VClip + 瞄准 + RELEASE
     *   返回 true = 取消原包由本方法发送，false = 走原版逻辑
     */
    public static boolean handleManualRelease() {
        if (!isEnabled() || mc.player == null || mc.player.connection == null) return false;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (cfg.autoShoot) return false;
        if (mc.player.getMainHandItem().getItem() != Items.BOW) return false;

        if (target != null && cfg.vClip) {
            // VClip 模式：拦截原包，发 doDMG + 瞬移 + 瞄准 + RELEASE
            doVClipShoot(mc.player, cfg);
            return true;
        } else {
            // 非 VClip 模式：发 doDMG，不拦截让原版 RELEASE 走
            doDMG(cfg);
            return false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.player == null || mc.level == null) return;
        LocalPlayer p = mc.player;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!isEnabled()) return;

        findTarget(cfg);

        // ★ 准星选中目标时每 Tick 持续放大碰撞箱（倍数缩放）
        if (target != null && cfg.expandHitbox > 1.0) {
            if (targetOriginalBox == null) {
                targetOriginalBox = target.getBoundingBox(); // 仅保存原始尺寸
            }
            // 每 Tick 从原始尺寸缩放，保证碰撞箱跟随目标移动
            AABB cur = target.getBoundingBox();
            double cx = cur.getCenter().x, cy = cur.getCenter().y, cz = cur.getCenter().z;
            double hw = (targetOriginalBox.maxX - targetOriginalBox.minX) / 2 * cfg.expandHitbox;
            double hh = (targetOriginalBox.maxY - targetOriginalBox.minY) / 2 * cfg.expandHitbox;
            double hd = (targetOriginalBox.maxZ - targetOriginalBox.minZ) / 2 * cfg.expandHitbox;
            target.setBoundingBox(new AABB(cx - hw, cy - hh, cz - hd, cx + hw, cy + hh, cz + hd));
        } else if (targetOriginalBox != null) {
            if (target != null) target.setBoundingBox(targetOriginalBox);
            targetOriginalBox = null;
        }

        // ★ Y校准：蓄力时持续传送玩家Y至目标Y + 瞄准修正 + 防卡方块
        if (cfg.yCalibrate && target != null && p.isUsingItem() && p.getUseItem().getItem() == Items.BOW) {
            double targetY = target.getY();
            if (Math.abs(p.getY() - targetY) > 0.1) {
                // 检查目标Y处是否有空间，如有方块阻挡则就近寻找空位
                double safeY = findSafeY(p, p.getX(), targetY, p.getZ());
                if (safeY != targetY) {
                    // 调整视角俯仰角以适应新的Y高度
                }
                p.connection.send(new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), p.onGround()));
                p.setPos(p.getX(), safeY, p.getZ());
            }
            // 校准视角：计算目标方向并发送 Rot 包（防止下传后枪口指向地面）
            Vec3 tc = target.getBoundingBox().getCenter();
            double dx = tc.x - p.getX(), dy = tc.y - p.getEyeY(), dz = tc.z - p.getZ();
            double hd = Math.sqrt(dx*dx + dz*dz);
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, hd));
            p.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround()));
        }

        // ★ 自动下蹲：选中目标且蓄力时下蹲，释放后2-3Tick自动起身
        boolean wantCrouch = cfg.autoCrouch && target != null && p.isUsingItem() && p.getUseItem().getItem() == Items.BOW;
        if (wantCrouch) {
            double targetH = targetOriginalBox != null ? targetOriginalBox.getYsize() : target.getBoundingBox().getYsize();
            if (targetH < 2.0) {
                mc.options.keyShift.setDown(true);
                crouchReleaseTimer = 3; // 设释放计时器
                if (p.getAbilities().flying) {
                    p.connection.send(new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), p.onGround()));
                }
            }
        } else if (crouchReleaseTimer > 0) {
            crouchReleaseTimer--;
            if (crouchReleaseTimer == 0) {
                mc.options.keyShift.setDown(false);
            }
        } else if (!mc.options.keyShift.isDown()) {
            mc.options.keyShift.setDown(false);
        }

        // 箭伤飞行
        if (cfg.arrowDmgFly) {
            boolean ch = p.isUsingItem() && p.getUseItem().getItem() == Items.BOW;
            if (ch) { if (!p.getAbilities().mayfly||!p.getAbilities().flying) { p.getAbilities().mayfly=true; p.getAbilities().flying=true; p.onUpdateAbilities(); } }
            else { if ((p.getAbilities().mayfly||p.getAbilities().flying)&&!p.isCreative()&&!p.isSpectator()) { p.getAbilities().mayfly=false; p.getAbilities().flying=false; p.onUpdateAbilities(); } }
        }

        boolean hasBow = p.getMainHandItem().getItem() == Items.BOW || p.getOffhandItem().getItem() == Items.BOW;
        if (!hasBow) { if (forcedPress) { mc.options.keyUse.setDown(false); forcedPress = false; } return; }

        // ★ 自动释放（VClip 时用 VClip 流程，否则至少发包+RELEASE）
        if (cfg.autoShoot && p.isUsingItem() && p.getUseItem().getItem() == Items.BOW && p.getTicksUsingItem() >= cfg.charge) {
            if (target != null && cfg.vClip) {
                doVClipShoot(p, cfg);
            } else {
                // 无目标或 vClip 关闭：至少发包 + RELEASE
                doDMG(cfg);
                p.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            }
        }
        if (!cfg.onlyWhenHoldingRightClick && !p.isUsingItem()) { mc.options.keyUse.setDown(true); forcedPress = true; }
    }

    /** ★ VClip 瞬移射击：发包 + PosRot 原子包 + 服务端/客户端双重瞄准 */
    private static void doVClipShoot(LocalPlayer p, ArrowDmgConfig cfg) {
        // ★ 先发包（只在释放前发一次，不卡蓄力）
        doDMG(cfg);
        Vec3 orig = p.position();
        float origYaw = p.getYRot();
        float origPitch = p.getXRot();

        // 找射击位：目标Y高度，玩家XZ
        Vec3 shootPos = new Vec3(orig.x, target.getBoundingBox().getCenter().y, orig.z);

        // 检查射击位
        AABB testBox = new AABB(shootPos.x-0.3, shootPos.y, shootPos.z-0.3, shootPos.x+0.3, shootPos.y+1.8, shootPos.z+0.3);
        if (!mc.level.noCollision(p, testBox)) {
            for (double yOff = 1; yOff <= 10; yOff++) {
                shootPos = new Vec3(orig.x, target.getBoundingBox().getCenter().y + yOff, orig.z);
                testBox = new AABB(shootPos.x-0.3, shootPos.y, shootPos.z-0.3, shootPos.x+0.3, shootPos.y+1.8, shootPos.z+0.3);
                if (mc.level.noCollision(p, testBox)) break;
            }
            for (double yOff = -1; yOff >= -5; yOff--) {
                shootPos = new Vec3(orig.x, target.getBoundingBox().getCenter().y + yOff, orig.z);
                if (mc.level.noCollision(p, testBox)) break;
            }
        }

        // ★ 计算瞄准角度（放大碰撞箱后取最近命中点）
        AABB hitBox = target.getBoundingBox().inflate(cfg.expandHitbox);
        Vec3 tc = getNearestPointOnBox(shootPos, hitBox);
        if (tc == null) tc = target.getBoundingBox().getCenter();
        double dx = tc.x - shootPos.x;
        double dy = tc.y - (shootPos.y + 1.62);
        double dz = tc.z - shootPos.z;
        double hd = Math.sqrt(dx*dx + dz*dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hd));

        // ★ 1) PosRot 原子包：位置+角度一次性发（服务端同时更新位置和朝向）
        p.connection.send(new ServerboundMovePlayerPacket.PosRot(
            shootPos.x, shootPos.y, shootPos.z, yaw, pitch, false));

        // ★ 2) 客户端临时旋转（让本 Tick 的后续包也用此角度）
        p.setYRot(yaw);
        p.setXRot(pitch);

        // 3) RELEASE 射箭
        p.connection.send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));

        // 4) 恢复客户端旋转 + PosRot 回原位
        p.setYRot(origYaw);
        p.setXRot(origPitch);
        p.connection.send(new ServerboundMovePlayerPacket.PosRot(
            orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, false));
        p.connection.send(new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true));

        p.fallDistance = 0;
    }

    private static void doDMG(ArrowDmgConfig cfg) {
        if (mc.player==null||mc.player.connection==null) return;
        // ★ 无硬上限，但建议不超过10000（防踢）
        int n = Math.max(1, (int)cfg.packets);
        if (n > 10000) n = 10000;
        double x=mc.player.getX(), y=mc.player.getY(), z=mc.player.getZ();
        mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        for(int i=0;i<n/2;i++) { mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x,y-1.0E-10,z,true)); mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x,y+1.0E-10,z,false)); }
        if(cfg.useOffset) mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x,y-0.01,z,true));
    }

    private static void sendPos(double x, double y, double z, boolean onGround) {
        if (mc.player != null && mc.player.connection != null)
            mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
    }

    /** ★ Y校准：找安全的Y坐标，防止卡入方块 */
    private static double findSafeY(LocalPlayer p, double x, double targetY, double z) {
        AABB playerBox = p.getBoundingBox();
        double eyeH = playerBox.maxY - playerBox.minY; // 约1.8格
        // 先检查目标Y是否可站立
        AABB testBox = new AABB(x - 0.3, targetY, z - 0.3, x + 0.3, targetY + eyeH, z + 0.3);
        if (mc.level.noCollision(p, testBox)) return targetY;

        // 向上搜索（优先）
        for (double yOff = 1; yOff <= 5; yOff++) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (mc.level.noCollision(p, testBox)) return targetY + yOff;
        }
        // 向下搜索
        for (double yOff = -1; yOff >= -3; yOff--) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (mc.level.noCollision(p, testBox)) return targetY + yOff;
        }
        return targetY; // 实在找不到就返回原值
    }

    /** ★ 从射击点向放大碰撞箱做射线，返回最近命中点 */
    private static Vec3 getNearestPointOnBox(Vec3 from, AABB box) {
        // 将射线起点限制在盒子外部
        Vec3 center = box.getCenter();
        Vec3 dir = center.subtract(from).normalize();

        // 沿射线从 from 向 center 步进，遇到 box 表面即停
        double dist = from.distanceTo(center);
        double step = 0.1;
        for (double d = 0; d <= dist; d += step) {
            Vec3 point = from.add(dir.scale(d));
            if (box.contains(point)) {
                // 回退半步到表面
                return from.add(dir.scale(Math.max(0, d - step)));
            }
        }
        return box.getCenter();
    }

    // ════════ 目标 ════════
    private static void findTarget(ArrowDmgConfig cfg) {
        target = null;
        if(mc.player==null||mc.level==null) return;
        boolean hasBow = mc.player.getMainHandItem().getItem()==Items.BOW||mc.player.getOffhandItem().getItem()==Items.BOW;
        if(!hasBow) return;
        double maxDist = cfg.aimRange;
        Entity best=null; double bestS=Double.MAX_VALUE;
        Vec3 eye=mc.player.getEyePosition(), look=mc.player.getLookAngle().normalize();
        for(Entity e:mc.level.entitiesForRendering()) {
            if(!(e instanceof LivingEntity)||e==mc.player||!e.isAlive()) continue;
            if(e instanceof net.minecraft.world.entity.player.Player pl&&(pl.isCreative()||pl.isSpectator())) continue;
            double d=eye.distanceTo(e.position()); if(d>maxDist) continue;
            Vec3 cen=e.getBoundingBox().getCenter();
            double ang=Math.toDegrees(Math.acos(Math.min(1,Math.max(-1,look.dot(cen.subtract(eye).normalize())))));
            double maxAng = 6 + cfg.expandHitbox * 2;
            if(ang > maxAng) continue;
            if(!cfg.ignoreWalls&&mc.level.clip(new ClipContext(eye,cen, ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player)).getType()!=HitResult.Type.MISS) continue;
            double sc=switch(cfg.priority){case"Distance"->d;case"Health"->((LivingEntity)e).getHealth();default->ang;};
            if(sc<bestS){bestS=sc;best=e;}
        }
        target=best;
    }

    // ════════ ESP 渲染（仅方框） ════════
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!isEnabled() || target == null || !target.isAlive() || mc.player == null) return;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) return;

        PoseStack ps = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);

        VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        Matrix4f mat = ps.last().pose();

        // 仅方框（TpAura 方式）— 使用实体当前碰撞箱
        if (cfg.showBox) renderBox(buf, mat, target.getBoundingBox().inflate(0.1), cfg.boxColor);

        ps.popPose();
    }

    private static void renderBox(VertexConsumer buf, Matrix4f mat, AABB box, int color) {
        float r=((color>>16)&0xFF)/255f,g=((color>>8)&0xFF)/255f,b=(color&0xFF)/255f,a=(((color>>24)&0xFF))/255f;
        if(a==0)a=1f;
        float mx=(float)box.minX,my=(float)box.minY,mz=(float)box.minZ,Mx=(float)box.maxX,My=(float)box.maxY,Mz=(float)box.maxZ;
        buf.vertex(mat,mx,my,mz).color(r,g,b,a).normal(0,-1,0).endVertex(); buf.vertex(mat,Mx,my,mz).color(r,g,b,a).normal(0,-1,0).endVertex();
        buf.vertex(mat,Mx,my,mz).color(r,g,b,a).normal(0,-1,0).endVertex(); buf.vertex(mat,Mx,my,Mz).color(r,g,b,a).normal(0,-1,0).endVertex();
        buf.vertex(mat,Mx,my,Mz).color(r,g,b,a).normal(0,-1,0).endVertex(); buf.vertex(mat,mx,my,Mz).color(r,g,b,a).normal(0,-1,0).endVertex();
        buf.vertex(mat,mx,my,Mz).color(r,g,b,a).normal(0,-1,0).endVertex(); buf.vertex(mat,mx,my,mz).color(r,g,b,a).normal(0,-1,0).endVertex();
        buf.vertex(mat,mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,Mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,Mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,Mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,Mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,mx,my,mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,Mx,my,mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,Mx,My,mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,Mx,my,Mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,Mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex();
        buf.vertex(mat,mx,my,Mz).color(r,g,b,a).normal(0,1,0).endVertex(); buf.vertex(mat,mx,My,Mz).color(r,g,b,a).normal(0,1,0).endVertex();
    }

    // ════════ Y坐标显示（移植自YPosOverlay，整合到渲染） ════════
    @SubscribeEvent
    public static void onRenderOverlay(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.type()) return;
        if (!isEnabled() || target == null || mc.player == null) return;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) return;

        double targetY = target.getY();
        double playerY = mc.player.getY();
        String text;
        if (Math.abs(targetY - playerY) < 0.5) {
            text = "Y: §a" + String.format("%.1f", targetY);
        } else {
            text = "Y: " + String.format("%.1f", targetY);
        }

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        event.getGuiGraphics().drawString(mc.font, text, cx - mc.font.width(text) / 2, cy + 15, 0xFFFFFF);
    }
}
