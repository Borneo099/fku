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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Fku.MOD_ID, value = Dist.CLIENT)
public class ArrowDmgFeature {

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
        if (!v) { if (forcedPress) { Minecraft.getInstance().options.keyUse.setDown(false); forcedPress = false; } target = null; }
    }
    public static boolean isEnabled() { return ArrowDmgConfig.getInstance().enabled; }
    /** 获取当前自瞄目标（供 HealthTag 联动） */
    public static Entity getTarget() { return target; }

    /** 兼容原版弓与模组弓（BowItem / ProjectileWeaponItem 子类） */
    public static boolean isBowItem(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        net.minecraft.world.item.Item item = stack.getItem();
        return item instanceof net.minecraft.world.item.ProjectileWeaponItem;
    }

    /**
     * ★ Mixin 调用：手动释放弓时（连射关闭）执行 VClip + 瞄准 + RELEASE
     *   返回 true = 取消原包由本方法发送，false = 走原版逻辑
     */
    public static boolean handleManualRelease() {
        if (!isEnabled() || Minecraft.getInstance().player == null || Minecraft.getInstance().player.connection == null) return false;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (cfg.autoShoot) return false;
        if (!isBowItem(Minecraft.getInstance().player.getMainHandItem())) return false;

        if (target != null && cfg.vClip) {
            // VClip 模式：拦截原包，发 doDMG + 瞬移 + 瞄准 + RELEASE
            doVClipShoot(Minecraft.getInstance().player, cfg);
            return true;
        } else {
            // 非 VClip 模式：发 doDMG，不拦截让原版 RELEASE 走
            doDMG(cfg);
            return false;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        LocalPlayer p = Minecraft.getInstance().player;
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
        if (cfg.yCalibrate && target != null && p.isUsingItem() && isBowItem(p.getUseItem())) {
            double targetY = target.getY();
            if (Math.abs(p.getY() - targetY) > 0.1) {
                // 检查目标Y处是否有空间，如有方块阻挡则就近寻找空位
                double safeY = findSafeY(p, p.getX(), targetY, p.getZ());
                if (safeY != targetY) {
                    // 调整视角俯仰角以适应新的Y高度
                }
                p.connection.send(new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), true, p.onGround()));
                p.setPos(p.getX(), safeY, p.getZ());
            }
            // 校准视角：计算目标方向并发送 Rot 包（防止下传后枪口指向地面）
            Vec3 tc = target.getBoundingBox().getCenter();
            double dx = tc.x - p.getX(), dy = tc.y - p.getEyeY(), dz = tc.z - p.getZ();
            double hd = Math.sqrt(dx*dx + dz*dz);
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, hd));
            p.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, true, p.onGround()));
        }
        boolean wantCrouch = cfg.autoCrouch && target != null && p.isUsingItem() && isBowItem(p.getUseItem());
        if (wantCrouch) {
            double targetH = targetOriginalBox != null ? targetOriginalBox.getYsize() : target.getBoundingBox().getYsize();
            if (targetH < 2.0) {
                Minecraft.getInstance().options.keyShift.setDown(true);
                crouchReleaseTimer = 3; // 设释放计时器
                if (p.getAbilities().flying) {
                    p.connection.send(new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), true, p.onGround()));
                }
            }
        } else if (crouchReleaseTimer > 0) {
            crouchReleaseTimer--;
            if (crouchReleaseTimer == 0) {
                Minecraft.getInstance().options.keyShift.setDown(false);
            }
        } else if (!Minecraft.getInstance().options.keyShift.isDown()) {
            Minecraft.getInstance().options.keyShift.setDown(false);
        }

        // 箭伤飞行
        if (cfg.arrowDmgFly) {
            boolean ch = p.isUsingItem() && isBowItem(p.getUseItem());
            if (ch) { if (!p.getAbilities().mayfly||!p.getAbilities().flying) { p.getAbilities().mayfly=true; p.getAbilities().flying=true; p.onUpdateAbilities(); } }
            else { if ((p.getAbilities().mayfly||p.getAbilities().flying)&&!p.isCreative()&&!p.isSpectator()) { p.getAbilities().mayfly=false; p.getAbilities().flying=false; p.onUpdateAbilities(); } }
        }

        boolean hasBow = isBowItem(p.getMainHandItem()) || isBowItem(p.getOffhandItem());
        if (!hasBow) { if (forcedPress) { Minecraft.getInstance().options.keyUse.setDown(false); forcedPress = false; } return; }

        // ★ 自动释放（VClip 时用 VClip 流程，否则至少发包+RELEASE）
        if (cfg.autoShoot && p.isUsingItem() && isBowItem(p.getUseItem()) && p.getTicksUsingItem() >= cfg.charge) {
            if (target != null && cfg.vClip) {
                doVClipShoot(p, cfg);
            } else {
                // 无目标或 vClip 关闭：至少发包 + RELEASE
                doDMG(cfg);
                p.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            }
        }
        if (!cfg.onlyWhenHoldingRightClick && !p.isUsingItem()) { Minecraft.getInstance().options.keyUse.setDown(true); forcedPress = true; }
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
        if (!Minecraft.getInstance().level.noCollision(p, testBox)) {
            for (double yOff = 1; yOff <= 10; yOff++) {
                shootPos = new Vec3(orig.x, target.getBoundingBox().getCenter().y + yOff, orig.z);
                testBox = new AABB(shootPos.x-0.3, shootPos.y, shootPos.z-0.3, shootPos.x+0.3, shootPos.y+1.8, shootPos.z+0.3);
                if (Minecraft.getInstance().level.noCollision(p, testBox)) break;
            }
            for (double yOff = -1; yOff >= -5; yOff--) {
                shootPos = new Vec3(orig.x, target.getBoundingBox().getCenter().y + yOff, orig.z);
                if (Minecraft.getInstance().level.noCollision(p, testBox)) break;
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
            shootPos.x, shootPos.y, shootPos.z, yaw, pitch, true, false));

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
            orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, true, false));
        p.connection.send(new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true, true));

        p.fallDistance = 0;
    }

    private static void doDMG(ArrowDmgConfig cfg) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null || p.connection == null) return;

        // ★ 参考 InvincibleMachineGun/ArrowDmg.java：先发疾跑包（START_SPRINTING），
        //   再做基于朝向的位置欺骗。1.21.8 下旧的「微型 ±1e-10 抖动」已失效，必须用此方式触发高伤害。
        p.connection.send(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_SPRINTING));

        double x = p.getX(), y = p.getY(), z = p.getZ();
        // 强度：复用 packets 作为强度，按 ArrowDmg 公式换算偏移量（strength/10 * √500）
        double currentStrength = Math.max(1, cfg.packets);
        if (currentStrength > 10000) currentStrength = 10000;
        double adjustedStrength = (currentStrength / 10.0) * Math.sqrt(500.0);
        Vec3 lookVec = p.getLookAngle().scale(adjustedStrength);
        double spoofX = -lookVec.x;
        double spoofY = cfg.vertical ? -lookVec.y : 0;   // ★ 垂直修正：开启含 Y 方向偏移，关闭则仅水平位移
        double spoofZ = -lookVec.z;
        double targetX = x + spoofX, targetY = y + spoofY, targetZ = z + spoofZ;

        // 参考 ArrowDmg.java processShoot：先回弹 4 次原位，再瞬移到欺骗位，再回原位
        for (int i = 0; i < 4; i++) sendPos(x, y, z, true);
        sendPos(targetX, targetY, targetZ, false);
        sendPos(x, y, z, false);
        // 垂直修正 + 防摔：Y 方向为正时 +0.01 防止摔伤（参考 ArrowDmg.java）
        if (cfg.useOffset && cfg.vertical && spoofY > 0) sendPos(x, y + 0.01, z, false);
    }

    private static void sendPos(double x, double y, double z, boolean onGround) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null)
            Minecraft.getInstance().player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, true, onGround));
    }

    /** ★ Y校准：找安全的Y坐标，防止卡入方块 */
    private static double findSafeY(LocalPlayer p, double x, double targetY, double z) {
        AABB playerBox = p.getBoundingBox();
        double eyeH = playerBox.maxY - playerBox.minY; // 约1.8格
        // 先检查目标Y是否可站立
        AABB testBox = new AABB(x - 0.3, targetY, z - 0.3, x + 0.3, targetY + eyeH, z + 0.3);
        if (Minecraft.getInstance().level.noCollision(p, testBox)) return targetY;

        // 向上搜索（优先）
        for (double yOff = 1; yOff <= 5; yOff++) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (Minecraft.getInstance().level.noCollision(p, testBox)) return targetY + yOff;
        }
        // 向下搜索
        for (double yOff = -1; yOff >= -3; yOff--) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (Minecraft.getInstance().level.noCollision(p, testBox)) return targetY + yOff;
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
        if(Minecraft.getInstance().player==null||Minecraft.getInstance().level==null) return;
        boolean hasBow = isBowItem(Minecraft.getInstance().player.getMainHandItem()) || isBowItem(Minecraft.getInstance().player.getOffhandItem());
        if(!hasBow) return;
        double maxDist = cfg.aimRange;
        Entity best=null; double bestS=Double.MAX_VALUE;
        Vec3 eye=Minecraft.getInstance().player.getEyePosition(), look=Minecraft.getInstance().player.getLookAngle().normalize();
        for(Entity e:Minecraft.getInstance().level.entitiesForRendering()) {
            if(!(e instanceof LivingEntity)||e==Minecraft.getInstance().player||!e.isAlive()) continue;
            if(e instanceof net.minecraft.world.entity.player.Player pl&&(pl.isCreative()||pl.isSpectator())) continue;
            double d=eye.distanceTo(e.position()); if(d>maxDist) continue;
            Vec3 cen=e.getBoundingBox().getCenter();
            double ang=Math.toDegrees(Math.acos(Math.min(1,Math.max(-1,look.dot(cen.subtract(eye).normalize())))));
            double maxAng = 6 + cfg.expandHitbox * 2;
            if(ang > maxAng) continue;
            if(!cfg.ignoreWalls&&Minecraft.getInstance().level.clip(new ClipContext(eye,cen, ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,Minecraft.getInstance().player)).getType()!=HitResult.Type.MISS) continue;
            double sc=switch(cfg.priority){case"Distance"->d;case"Health"->((LivingEntity)e).getHealth();default->ang;};
            if(sc<bestS){bestS=sc;best=e;}
        }
        target=best;
    }

    // ════════ ESP 渲染（仅方框） ════════
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterEntities event) {
        // RenderLevelStageEvent 在 NeoForge 21.8.53 起变为 abstract，必须监听其具体子类
        if (!isEnabled() || target == null || !target.isAlive() || Minecraft.getInstance().player == null) return;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) return;

        PoseStack ps = event.getPoseStack();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);

        VertexConsumer buf = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
        Matrix4f mat = ps.last().pose();

        // 仅方框（TpAura 方式）— 使用实体当前碰撞箱
        if (cfg.showBox) renderBox(buf, mat, target.getBoundingBox().inflate(0.1), cfg.boxColor);

        ps.popPose();
    }

    private static void renderBox(VertexConsumer buf, Matrix4f mat, AABB box, int color) {
        float r=((color>>16)&0xFF)/255f,g=((color>>8)&0xFF)/255f,b=(color&0xFF)/255f,a=(((color>>24)&0xFF))/255f;
        if(a==0)a=1f;
        float mx=(float)box.minX,my=(float)box.minY,mz=(float)box.minZ,Mx=(float)box.maxX,My=(float)box.maxY,Mz=(float)box.maxZ;
        buf.addVertex(mat,mx,my,mz).setColor(r,g,b,a).setNormal(0,-1,0); buf.addVertex(mat,Mx,my,mz).setColor(r,g,b,a).setNormal(0,-1,0);
        buf.addVertex(mat,Mx,my,mz).setColor(r,g,b,a).setNormal(0,-1,0); buf.addVertex(mat,Mx,my,Mz).setColor(r,g,b,a).setNormal(0,-1,0);
        buf.addVertex(mat,Mx,my,Mz).setColor(r,g,b,a).setNormal(0,-1,0); buf.addVertex(mat,mx,my,Mz).setColor(r,g,b,a).setNormal(0,-1,0);
        buf.addVertex(mat,mx,my,Mz).setColor(r,g,b,a).setNormal(0,-1,0); buf.addVertex(mat,mx,my,mz).setColor(r,g,b,a).setNormal(0,-1,0);
        buf.addVertex(mat,mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,Mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,Mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,Mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,Mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,mx,my,mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,Mx,my,mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,Mx,My,mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,Mx,my,Mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,Mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0);
        buf.addVertex(mat,mx,my,Mz).setColor(r,g,b,a).setNormal(0,1,0); buf.addVertex(mat,mx,My,Mz).setColor(r,g,b,a).setNormal(0,1,0);
    }

    // ════════ Y坐标显示（移植自YPosOverlay，整合到渲染） ════════
    @SubscribeEvent
    public static void onRenderOverlay(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
        if (event.getName() != net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR) return;
        if (!isEnabled() || target == null || Minecraft.getInstance().player == null) return;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) return;

        double targetY = target.getY();
        double playerY = Minecraft.getInstance().player.getY();
        String text;
        if (Math.abs(targetY - playerY) < 0.5) {
            text = "Y: §a" + String.format("%.1f", targetY);
        } else {
            text = "Y: " + String.format("%.1f", targetY);
        }

        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        event.getGuiGraphics().drawString(Minecraft.getInstance().font, text, cx - Minecraft.getInstance().font.width(text) / 2, cy + 15, 0xFFFFFFFF);
    }
}
