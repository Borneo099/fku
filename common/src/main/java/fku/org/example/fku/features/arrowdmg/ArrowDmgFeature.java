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
                mc.f_91066_.f_92095_.m_7249_(false);
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
        if (mc == null || !ArrowDmgFeature.isEnabled() || mc.player == null || mc.player.f_108617_ == null) {
            return false;
        }
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (cfg.autoShoot) {
            return false;
        }
        if (!ArrowDmgFeature.isBowItem(mc.player.m_21205_())) {
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
        if (mc == null || mc.player == null || mc.f_91073_ == null) {
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
                targetOriginalBox = target.m_20191_();
            }
            AABB cur = target.m_20191_();
            double cx = cur.m_82399_().x;
            double cy = cur.m_82399_().y;
            double cz = cur.m_82399_().z;
            double hw = (ArrowDmgFeature.targetOriginalBox.f_82291_ - ArrowDmgFeature.targetOriginalBox.f_82288_) / 2.0 * cfg.expandHitbox;
            double hh = (ArrowDmgFeature.targetOriginalBox.f_82292_ - ArrowDmgFeature.targetOriginalBox.f_82289_) / 2.0 * cfg.expandHitbox;
            double hd = (ArrowDmgFeature.targetOriginalBox.f_82293_ - ArrowDmgFeature.targetOriginalBox.f_82290_) / 2.0 * cfg.expandHitbox;
            target.m_20011_(new AABB(cx - hw, cy - hh, cz - hd, cx + hw, cy + hh, cz + hd));
        } else if (targetOriginalBox != null) {
            if (target != null) {
                target.m_20011_(targetOriginalBox);
            }
            targetOriginalBox = null;
        }
        if (cfg.yCalibrate && target != null && p.m_6117_() && ArrowDmgFeature.isBowItem(p.m_21211_())) {
            double targetY = target.getY();
            if (Math.abs(p.getY() - targetY) > 0.1) {
                double safeY = ArrowDmgFeature.findSafeY(p, p.getX(), targetY, p.getZ());
                if (safeY != targetY) {
                    // empty if block
                }
                p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(p.getX(), safeY, p.getZ(), p.m_20096_()));
                p.m_6034_(p.getX(), safeY, p.getZ());
            }
            Vec3 tc = target.m_20191_().m_82399_();
            double dx = tc.x - p.getX();
            double dy = tc.y - p.m_20188_();
            double dz = tc.z - p.getZ();
            double hd = Math.sqrt(dx * dx + dz * dz);
            float yaw = Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (-Math.toDegrees(Math.atan2(dy, hd)));
            p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.m_20096_()));
        }
        boolean bl = wantCrouch = cfg.autoCrouch && target != null && p.m_6117_() && ArrowDmgFeature.isBowItem(p.m_21211_());
        if (wantCrouch) {
            double targetH;
            double d = targetH = targetOriginalBox != null ? targetOriginalBox.m_82376_() : target.m_20191_().m_82376_();
            if (targetH < 2.0) {
                mc.f_91066_.f_92090_.m_7249_(true);
                crouchReleaseTimer = 3;
                if (p.m_150110_().f_35935_) {
                    p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY(), p.getZ(), p.m_20096_()));
                }
            }
        } else if (crouchReleaseTimer > 0) {
            if (--crouchReleaseTimer == 0) {
                mc.f_91066_.f_92090_.m_7249_(false);
            }
        } else if (!mc.f_91066_.f_92090_.m_90857_()) {
            mc.f_91066_.f_92090_.m_7249_(false);
        }
        if (cfg.arrowDmgFly) {
            boolean ch;
            boolean bl2 = ch = p.m_6117_() && ArrowDmgFeature.isBowItem(p.m_21211_());
            if (ch) {
                if (!p.m_150110_().f_35936_ || !p.m_150110_().f_35935_) {
                    p.m_150110_().f_35936_ = true;
                    p.m_150110_().f_35935_ = true;
                    p.m_6885_();
                }
            } else if ((p.m_150110_().f_35936_ || p.m_150110_().f_35935_) && !p.m_7500_() && !p.m_5833_()) {
                p.m_150110_().f_35936_ = false;
                p.m_150110_().f_35935_ = false;
                p.m_6885_();
            }
        }
        boolean hasBow = ArrowDmgFeature.isBowItem(p.m_21205_()) || ArrowDmgFeature.isBowItem(p.m_21206_());
        Minecraft mc2 = ArrowDmgFeature.getMc();
        if (mc2 == null) {
            return;
        }
        if (!hasBow) {
            if (forcedPress) {
                mc2.f_91066_.f_92095_.m_7249_(false);
                forcedPress = false;
            }
            return;
        }
        if (cfg.autoShoot && p.m_6117_() && ArrowDmgFeature.isBowItem(p.m_21211_()) && p.m_21252_() >= cfg.charge) {
            if (target != null && cfg.vClip) {
                ArrowDmgFeature.doVClipShoot(p, cfg);
            } else {
                ArrowDmgFeature.doDMG(cfg);
                p.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.f_121853_, Direction.DOWN));
            }
        }
        if (!cfg.onlyWhenHoldingRightClick && !p.m_6117_()) {
            mc.f_91066_.f_92095_.m_7249_(true);
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
        float origYaw = p.m_146908_();
        float origPitch = p.m_146909_();
        Vec3 shootPos = new Vec3(orig.x, ArrowDmgFeature.target.m_20191_().m_82399_().y, orig.z);
        AABB testBox = new AABB(shootPos.x - 0.3, shootPos.y, shootPos.z - 0.3, shootPos.x + 0.3, shootPos.y + 1.8, shootPos.z + 0.3);
        if (!mc.f_91073_.m_45756_((Entity)p, testBox)) {
            double yOff;
            for (yOff = 1.0; yOff <= 10.0; yOff += 1.0) {
                shootPos = new Vec3(orig.x, ArrowDmgFeature.target.m_20191_().m_82399_().y + yOff, orig.z);
                testBox = new AABB(shootPos.x - 0.3, shootPos.y, shootPos.z - 0.3, shootPos.x + 0.3, shootPos.y + 1.8, shootPos.z + 0.3);
                if (mc.f_91073_.m_45756_((Entity)p, testBox)) break;
            }
            for (yOff = -1.0; yOff >= -5.0; yOff -= 1.0) {
                shootPos = new Vec3(orig.x, ArrowDmgFeature.target.m_20191_().m_82399_().y + yOff, orig.z);
                if (mc.f_91073_.m_45756_((Entity)p, testBox)) break;
            }
        }
        if ((tc = ArrowDmgFeature.getNearestPointOnBox(shootPos, hitBox = target.m_20191_().m_82400_(cfg.expandHitbox))) == null) {
            tc = target.m_20191_().m_82399_();
        }
        double dx = tc.x - shootPos.x;
        double dy = tc.y - (shootPos.y + 1.62);
        double dz = tc.z - shootPos.z;
        double hd = Math.sqrt(dx * dx + dz * dz);
        float yaw = Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (-Math.toDegrees(Math.atan2(dy, hd)));
        p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(shootPos.x, shootPos.y, shootPos.z, yaw, pitch, false));
        p.m_146922_(yaw);
        p.m_146926_(pitch);
        p.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.f_121853_, Direction.DOWN));
        p.m_146922_(origYaw);
        p.m_146926_(origPitch);
        p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(orig.x, orig.y + 0.01, orig.z, origYaw, origPitch, false));
        p.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(orig.x, orig.y, orig.z, true));
        p.f_19789_ = 0.0f;
    }

    private static void doDMG(ArrowDmgConfig cfg) {
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null || mc.player == null || mc.player.f_108617_ == null) {
            return;
        }
        int n = Math.max(1, cfg.packets);
        if (n > 10000) {
            n = 10000;
        }
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        for (int i = 0; i < n / 2; ++i) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(x, y - 1.0E-10, z, true));
            mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(x, y + 1.0E-10, z, false));
        }
        if (cfg.useOffset) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(x, y - 0.01, z, true));
        }
    }

    public static boolean isBowItem(ItemStack stack) {
        ResourceLocation id;
        if (stack == null || stack.m_41619_()) {
            return false;
        }
        Item item = stack.m_41720_();
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
        if (mc != null && mc.player != null && mc.player.f_108617_ != null) {
            mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
        }
    }

    private static double findSafeY(LocalPlayer p, double x, double targetY, double z) {
        double yOff;
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null) {
            return targetY;
        }
        AABB playerBox = p.m_20191_();
        double eyeH = playerBox.f_82292_ - playerBox.f_82289_;
        AABB testBox = new AABB(x - 0.3, targetY, z - 0.3, x + 0.3, targetY + eyeH, z + 0.3);
        if (mc.f_91073_.m_45756_((Entity)p, testBox)) {
            return targetY;
        }
        for (yOff = 1.0; yOff <= 5.0; yOff += 1.0) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (!mc.f_91073_.m_45756_((Entity)p, testBox)) continue;
            return targetY + yOff;
        }
        for (yOff = -1.0; yOff >= -3.0; yOff -= 1.0) {
            testBox = new AABB(x - 0.3, targetY + yOff, z - 0.3, x + 0.3, targetY + yOff + eyeH, z + 0.3);
            if (!mc.f_91073_.m_45756_((Entity)p, testBox)) continue;
            return targetY + yOff;
        }
        return targetY;
    }

    private static Vec3 getNearestPointOnBox(Vec3 from, AABB box) {
        Vec3 center = box.m_82399_();
        Vec3 dir = center.subtract(from).normalize();
        double dist = from.m_82554_(center);
        double step = 0.1;
        for (double d = 0.0; d <= dist; d += step) {
            Vec3 point = from.add(dir.scale(d));
            if (!box.m_82390_(point)) continue;
            return from.add(dir.scale(Math.max(0.0, d - step)));
        }
        return box.m_82399_();
    }

    private static void findTarget(ArrowDmgConfig cfg) {
        boolean hasBow;
        Minecraft mc = ArrowDmgFeature.getMc();
        if (mc == null) {
            return;
        }
        target = null;
        if (mc.player == null || mc.f_91073_ == null) {
            return;
        }
        boolean bl = hasBow = ArrowDmgFeature.isBowItem(mc.player.m_21205_()) || ArrowDmgFeature.isBowItem(mc.player.m_21206_());
        if (!hasBow) {
            return;
        }
        double maxDist = cfg.aimRange;
        Entity best = null;
        double bestS = Double.MAX_VALUE;
        Vec3 eye = mc.player.m_146892_();
        Vec3 look = mc.player.getLookAngle().normalize();
        for (Entity e : mc.f_91073_.m_104735_()) {
            double sc;
            double maxAng;
            Vec3 cen;
            double ang;
            double d;
            Player pl;
            if (!(e instanceof LivingEntity) || e == mc.player || !e.m_6084_() || e instanceof Player && ((pl = (Player)e).m_7500_() || pl.m_5833_()) || (d = eye.m_82554_(e.position())) > maxDist || (ang = Math.toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, look.dot((cen = e.m_20191_().m_82399_()).subtract(eye).normalize())))))) > (maxAng = 6.0 + cfg.expandHitbox * 2.0) || !cfg.ignoreWalls && mc.f_91073_.m_45547_(new ClipContext(eye, cen, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player)).m_6662_() != HitResult.Type.MISS) continue;
            if (!((sc = (switch (cfg.priority) {
                case "Distance" -> d;
                case "Health" -> ((LivingEntity)e).m_21223_();
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
        if (mc == null || !ArrowDmgFeature.isEnabled() || target == null || !target.m_6084_() || mc.player == null) {
            return;
        }
        ArrowDmgConfig cfg = ArrowDmgConfig.getInstance();
        if (!cfg.renderEnabled) {
            return;
        }
        PoseStack ps = event.getPoseStack();
        Vec3 cam = mc.f_91063_.m_109153_().getPosition();
        ps.pushPose();
        ps.translate(-cam.x, -cam.y, -cam.z);
        VertexConsumer buf = mc.m_91269_().m_110104_().m_6299_((RenderType)RenderType.f_110371_);
        Matrix4f mat = ps.last().pose();
        if (cfg.showBox) {
            ArrowDmgFeature.renderBox(buf, mat, target.m_20191_().m_82400_(0.1), cfg.boxColor);
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
        float mx = box.f_82288_;
        float my = box.f_82289_;
        float mz = box.f_82290_;
        float Mx = box.f_82291_;
        float My = box.f_82292_;
        float Mz = box.f_82293_;
        buf.vertex(mat, mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, Mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, my, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        buf.vertex(mat, mx, My, Mz).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
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
        int sw = mc.getWindow().m_85445_();
        int sh = mc.getWindow().m_85446_();
        int cx = sw / 2;
        int cy = sh / 2;
        event.getGuiGraphics().drawString(mc.font, text, cx - mc.font.m_92895_(text) / 2, cy + 15, 0xFFFFFF);
    }
}

