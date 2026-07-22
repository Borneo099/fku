package fku.org.example.fku.features.arrowdmg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fku.org.example.fku.features.arrowdmg.ArrowDmgConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class ArrowDmgFeature {
    private static boolean forcedPress = false;
    private static Entity target = null;
    private static AABB targetOriginalBox = null;
    private static int crouchReleaseTimer = 0;

    private static Minecraft getMc() {
        return Minecraft.getInstance();
    }

    public static void init() {
        ArrowDmgConfig.load();
    }

    public static void toggleEnabled() {
        ArrowDmgFeature.setEnabled(!ArrowDmgFeature.isEnabled());
    }

    public static void setEnabled(boolean v) {
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
        if (!v) {
            Minecraft mc = ArrowDmgFeature.getMc();
            if (forcedPress && mc != null) {
                mc.options.keyUse.setDown(false);
                forcedPress = false;
            }
            target = null;
        }
    }

    public static boolean isEnabled() {
        return ArrowDmgConfig.getInstance().enabled;
    }

    public static Entity getTarget() {
        return target;
    }

    public static boolean handleManualRelease() {
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || !ArrowDmgFeature.isEnabled() || mc.player == null || mc.player.connection == null) {
            return false;
        }
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (cfg.autoShoot) {
            return false;
        }
        if (!ArrowDmgFeature.isBowItem(mc.player.getMainHandItem())) {
            return false;
        }
        if (target != null && cfg.vClip) {
            ArrowDmgFeature.doVClipShoot(mc.player, cfg);
            return true;
        }
        ArrowDmgFeature.doDMG(cfg);
        return false;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        boolean wantCrouch;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || mc.player == null || mc.level == null) {
            return;
        }
        LocalPlayer p = mc.player;
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!ArrowDmgFeature.isEnabled()) {
            return;
        }
        ArrowDmgFeature.findTarget(cfg);
        if (target != null && cfg.expandHitbox > 1.0) {
            if (targetOriginalBox == null) {
                targetOriginalBox = target.getBoundingBox();
            }
            AABB cur = target.getBoundingBox();
            double cx = cur.getCenter().x;
            double cy = cur.getCenter().y;
            double cz = cur.getCenter().z;
            double hw = (ArrowDmgFeature.targetOriginalBox.maxX - ArrowDmgFeature.targetOriginalBox.minX) / 2.0 * cfg.expandHitbox;
            double hh = (ArrowDmgFeature.targetOriginalBox.maxY - ArrowDmgFeature.targetOriginalBox.minY) / 2.0 * cfg.expandHitbox;
            double hd = (ArrowDmgFeature.targetOriginalBox.maxZ - ArrowDmgFeature.targetOriginalBox.minZ) / 2.0 * cfg.expandHitbox;
            target.setBoundingBox(new AABB(cx - hw, cy - hh, cz - hd, cx + hw, cy + hh, cz + hd));
        } else if (targetOriginalBox != null) {
            if (target != null) {
                target.setBoundingBox(targetOriginalBox);
            }
            targetOriginalBox = null;
        }
        if (cfg.yCalibrate && target != null && p.isUsingItem() && ArrowDmgFeature.isBowItem(p.getUseItem())) {
            double targetY = target.getY();
            if (Math.abs(p.getY() - targetY) > 0.1) {
                double safeY = ArrowDmgFeature.findSafeY(p, p.getX(), targetY, p.getZ());
                if (safeY != targetY) {
                    // empty if block
                }
                p.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), p.onGround()));
                p.setPos(p.getX(), safeY, p.getZ());
            }
            Vec3 tc = target.getBoundingBox().getCenter();
            double dx = tc.x - p.getX();
            double dy = tc.y - p.getEyeY();
            double dz = tc.z - p.getZ();
            double hd = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float)(-Math.toDegrees(Math.atan2(dy, hd)));
            p.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround()));
        }
        boolean bl = wantCrouch = cfg.autoCrouch && target != null && p.isUsingItem() && ArrowDmgFeature.isBowItem(p.getUseItem());
        if (wantCrouch) {
            double targetH;
            double d = targetH = targetOriginalBox != null ? targetOriginalBox.getYsize() : target.getBoundingBox().getYsize();
            if (targetH < 2.0) {
                mc.options.keyShift.setDown(true);
                crouchReleaseTimer = 3;
                if (p.getAbilities().flying) {
                    p.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), p.onGround()));
                }
            }
        } else if (crouchReleaseTimer > 0) {
            if (--crouchReleaseTimer == 0) {
                mc.options.keyShift.setDown(false);
            }
        } else if (!mc.options.keyShift.isDown()) {
            mc.options.keyShift.setDown(false);
        }
        if (cfg.arrowDmgFly) {
            boolean ch;
            boolean bl2 = ch = p.isUsingItem() && ArrowDmgFeature.isBowItem(p.getUseItem());
            if (ch) {
                if (!p.getAbilities().mayfly || !p.getAbilities().flying) {
                    p.getAbilities().mayfly = true;
                    p.getAbilities().flying = true;
                    p.onUpdateAbilities();
                }
            } else if ((p.getAbilities().mayfly || p.getAbilities().flying) && !p.isCreative() && !p.isSpectator()) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.onUpdateAbilities();
            }
        }
        boolean hasBow = ArrowDmgFeature.isBowItem(p.getMainHandItem()) || ArrowDmgFeature.isBowItem(p.getOffhandItem());
        Minecraft mc2 = ArrowDmgFeature.getMc();
        if (mc2 == null) {
            return;
        }
        if (!hasBow) {
            if (forcedPress) {
                mc2.options.keyUse.setDown(false);
                forcedPress = false;
            }
            return;
        }
        if (cfg.autoShoot && p.isUsingItem() && ArrowDmgFeature.isBowItem(p.getUseItem()) && p.getTicksUsingItem() >= cfg.charge) {
            if (target != null && cfg.vClip) {
                ArrowDmgFeature.doVClipShoot(p, cfg);
            } else {
                ArrowDmgFeature.doDMG(cfg);
                p.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
            }
        }
        if (!cfg.onlyWhenHoldingRightClick && !p.isUsingItem()) {
            mc.options.keyUse.setDown(true);
            forcedPress = true;
        }
    }

    private static void doVClipShoot(LocalPlayer p, ArrowDmgConfig cfg) {
        AABB hitBox;
        Vec3 tc;
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null) {
            return;
        }
        ArrowDmgFeature.doDMG(cfg);
        Vec3 orig = p.position();
        float origYaw = p.getYRot();
        float origPitch = p.getXRot();
        Vec3 shootPos = new Vec3(orig.x, ArrowDmgFeature.target.getBoundingBox().getCenter().y, orig.z);
        AABB testBox = new AABB(shootPos.x - 0.3, shootPos.y, shootPos.z - 0.3, shootPos.x + 0.3, shootPos.y + 1.8, shootPos.z + 0.3);
        if (!mc.level.noCollision((Entity)p, testBox)) {
            double yOff;
            for (yOff = 1.0; yOff <= 10.0; yOff += 1.0) {
                shootPos = new Vec3(orig.x, ArrowDmgFeature.target.getBoundingBox().getCenter().y + yOff, orig.z);
                testBox = new AABB(shootPos.x - 0.3, shootPos.y, shootPos.z - 0.3, shootPos.x + 0.3, shootPos.y + 1.8, shootPos.z + 0.3);
                if (mc.level.noCollision((Entity)p, testBox)) break;
            }
            for (yOff = -1.0; yOff >= -5.0; yOff -= 1.0) {
                shootPos = new Vec3(orig.x, ArrowDmgFeature.target.getBoundingBox().getCenter().y + yOff, orig.z);
                if (mc.level.noCollision((Entity)p, testBox)) break;
            }
        }
        if ((tc = ArrowDmgFeature.getNearestPointOnBox(shootPos, hitBox = target.getBoundingBox().inflate(cfg.expandHitbox))) == null) {
            tc = target.getBoundingBox().getCenter();
        }
        double dx = tc.x - shootPos.x;
        double dy = tc.y - (shootPos.y + 1.62);
        double dz = tc.z - shootPos.z;
        double hd = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, hd)));
        p.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(shootPos.x, shootPos.y, shootPos.z, yaw, pitch, false));
        p.setYRot(yaw);
        p.setXRot(pitch);
        p.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        p.setYRot(origYaw);
        p.setXRot(origPitch);
        p.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, false));
        p.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true));
        p.fallDistance = 0.0f;
    }

    private static void doDMG(ArrowDmgConfig cfg) {
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || mc.player == null || mc.player.connection == null) {
            return;
        }
        int n = (int)Math.max(1, cfg.packets);
        if (n > 10000) {
            n = 10000;
        }
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        for (int i = 0; i < n / 2; ++i) {
            mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(x, y - 1.0E-10, z, true));
            mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-10, z, false));
        }
        if (cfg.useOffset) {
            mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(x, y - 0.01, z, true));
        }
    }

    public static boolean isBowItem(ItemStack stack) {
        ResourceLocation id;
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ProjectileWeaponItem) {
            return true;
        }
        String customIds = ArrowDmgConfig.getInstance().customBowIds;
        if (customIds != null && !customIds.isEmpty() && (id = ForgeRegistries.ITEMS.getKey(item)) != null) {
            String itemId = id.toString();
            for (String s : customIds.split(",")) {
                if (!s.trim().equalsIgnoreCase(itemId)) continue;
                return true;
            }
        }
        return false;
    }

    private static void sendPos(double x, double y, double z, boolean onGround) {
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc != null && mc.player != null && mc.player.connection != null) {
            mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
        }
    }

    private static double findSafeY(LocalPlayer p, double x, double targetY, double z) {
        double yOff;
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null) {
            return targetY;
        }
        AABB playerBox = p.getBoundingBox();
        double eyeH = playerBox.maxY - playerBox.minY;
        AABB testBox = new AABB(x - 0.3, targetY, z - 0.3, x + 0.3, targetY + eyeH, z + 0.3);
        if (mc.level.noCollision((Entity)p, testBox)) {
            return targetY;
        }
        for (yOff = 1.0; yOff <= 5.0; yOff += 1.0) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (!mc.level.noCollision((Entity)p, testBox)) continue;
            return targetY + yOff;
        }
        for (yOff = -1.0; yOff >= -3.0; yOff -= 1.0) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (!mc.level.noCollision((Entity)p, testBox)) continue;
            return targetY + yOff;
        }
        return targetY;
    }

    private static Vec3 getNearestPointOnBox(Vec3 from, AABB box) {
        Vec3 center = box.getCenter();
        Vec3 dir = center.subtract(from).normalize();
        double dist = from.distanceTo(center);
        double step = 0.1;
        for (double d = 0.0; d <= dist; d += step) {
            Vec3 point = from.add(dir.scale(d));
            if (!box.contains(point)) continue;
            return from.add(dir.scale(Math.max(0.0, d - step)));
        }
        return box.getCenter();
    }

    private static void findTarget(ArrowDmgConfig cfg) {
        boolean hasBow;
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null) {
            return;
        }
        target = null;
        if (mc.player == null || mc.level == null) {
            return;
        }
        boolean bl = hasBow = ArrowDmgFeature.isBowItem(mc.player.getMainHandItem()) || ArrowDmgFeature.isBowItem(mc.player.getOffhandItem());
        if (!hasBow) {
            return;
        }
        double maxDist = cfg.aimRange;
        Entity best = null;
        double bestS = Double.MAX_VALUE;
        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getLookAngle().normalize();
        for (Entity e : mc.level.entitiesForRendering()) {
            double sc;
            double maxAng;
            Vec3 cen;
            double ang;
            double d;
            Player pl;
            if (!(e instanceof LivingEntity) || e == mc.player || !e.isAlive() || e instanceof Player && ((pl = (Player)e).isCreative() || pl.isSpectator()) || (d = eye.distanceTo(e.position())) > maxDist || (ang = Math.toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, look.dot((cen = e.getBoundingBox().getCenter()).subtract(eye).normalize())))))) > (maxAng = 6.0 + cfg.expandHitbox * 2.0) || !cfg.ignoreWalls && mc.level.clip(new ClipContext(eye, cen, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player)).getType() != HitResult.Type.MISS) continue;
            if (!((sc = (switch (cfg.priority) {
                case "Distance" -> d;
                case "Health" -> ((LivingEntity)e).getHealth();
                default -> ang;
            })) < bestS)) continue;
            bestS = sc;
            best = e;
        }
        target = best;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || !ArrowDmgFeature.isEnabled() || target == null || !target.isAlive() || mc.player == null) {
            return;
        }
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) {
            return;
        }
        PoseStack ps = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer((RenderType)RenderType.LINES);
        Matrix4f mat = ps.last().pose();
        if (cfg.showBox) {
            ArrowDmgFeature.renderBox(buf, mat, target.getBoundingBox().inflate(0.1), cfg.boxColor);
        }
        ps.popPose();
    }

    private static void renderBox(VertexConsumer buf, Matrix4f mat, AABB box, int color) {
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = (color >> 24 & 0xFF) / 255.0f;
        if (a == 0.0f) {
            a = 1.0f;
        }
        float mx = (float)box.minX;
        float my = (float)box.minY;
        float mz = (float)box.minZ;
        float Mx = (float)box.maxX;
        float My = (float)box.maxY;
        float Mz = (float)box.maxZ;
        buf.vertex(mat, mx, my, mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, mz).color(r, g, b, a).normal(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        double playerY;
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || !ArrowDmgFeature.isEnabled() || target == null || mc.player == null) {
            return;
        }
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) {
            return;
        }
        double targetY = target.getY();
        String text = Math.abs(targetY - (playerY = mc.player.getY())) < 0.5 ? "Y: \u00a7a" + String.format("%.1f", targetY) : "Y: " + String.format("%.1f", targetY);
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        event.getGuiGraphics().drawString(mc.font, text, cx - mc.font.width(text) / 2, cy + 15, 0xFFFFFF);
    }
}

