package fku.org.example.fku.features.tpaura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fku.org.example.fku.features.fakeplayer.FakePlayerFeature;
import fku.org.example.fku.features.healthtag.HealthTagManager;
import fku.org.example.fku.features.killfx.KillFXFeature;
import fku.org.example.fku.features.killicon.KillIconFeature;
import fku.org.example.fku.features.tpaura.TpAuraConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class TpAuraFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static TpAuraFeature instance;
    public Entity currentTarget;
    public final List<Vec3> renderPathNodes = new ArrayList<Vec3>();
    private final List<Entity> targets = new ArrayList<Entity>();
    private int originalSlot = -1;
    private int silentSwapSlot = -1;
    private int silentSwapPrevSlot = -1;
    private int delayTimer = 0;
    private static long overlayShowUntil;
    private static boolean wasHotkeyDown;
    private static boolean waitingKeyBind;
    private static Runnable onKeyBoundCallback;

    public static boolean isEnabled() {
        return TpAuraConfig.getInstance().enabled;
    }

    public static void setEnabled(boolean v) {
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        cfg.enabled = v;
        cfg.save();
        overlayShowUntil = System.currentTimeMillis() + 3000L;
        if (v) {
            if (TpAuraFeature.mc.player != null) {
                TpAuraFeature.mc.player.m_5661_(Component.literal((String)("\u00a76[TpAura] \u00a7a\u5df2\u542f\u7528 \u00a77(\u8303\u56f4=" + cfg.maxRange + ", \u6a21\u5f0f=" + cfg.mode + ")")), false);
            }
        } else {
            TpAuraFeature feature = instance;
            if (feature != null) {
                feature.cleanup();
            }
            if (TpAuraFeature.mc.player != null) {
                TpAuraFeature.mc.player.m_5661_(Component.literal((String)"\u00a76[TpAura] \u00a7c\u5df2\u7981\u7528"), false);
            }
        }
    }

    public TpAuraFeature() {
        instance = this;
    }

    public static TpAuraFeature getInstance() {
        if (instance == null) {
            instance = new TpAuraFeature();
        }
        return instance;
    }

    public static void init() {
        TpAuraConfig.load();
        TpAuraFeature.getInstance();
    }

    public static void startHotkeyBind(Runnable onBound) {
        waitingKeyBind = true;
        onKeyBoundCallback = onBound;
        if (TpAuraFeature.mc.player != null) {
            TpAuraFeature.mc.player.m_5661_(Component.literal((String)"\u00a76[TpAura] \u00a7e\u6309\u4e0b\u952e\u76d8\u4e0a\u7684\u6309\u952e\u7ed1\u5b9a\u70ed\u952e. (Esc\u53d6\u6d88)"), false);
        }
    }

    public static void cancelHotkeyBind() {
        waitingKeyBind = false;
        onKeyBoundCallback = null;
        if (TpAuraFeature.mc.player != null) {
            TpAuraFeature.mc.player.m_5661_(Component.literal((String)"\u00a76[TpAura] \u00a77\u70ed\u952e\u7ed1\u5b9a\u5df2\u53d6\u6d88"), false);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (TpAuraFeature.mc.player == null) {
            return;
        }
        if (waitingKeyBind) {
            if (event.getAction() != 1) {
                return;
            }
            if (event.getKey() == 256) {
                TpAuraFeature.cancelHotkeyBind();
                return;
            }
            TpAuraConfig cfg = TpAuraConfig.getInstance();
            cfg.setHotkeyKey(event.getKey());
            String keyName = GLFW.glfwGetKeyName(event.getKey(), event.getScanCode());
            if (keyName == null || keyName.isEmpty()) {
                keyName = switch (event.getKey()) {
                    case 340 -> "LSHIFT";
                    case 344 -> "RSHIFT";
                    case 341 -> "LCTRL";
                    case 345 -> "RCTRL";
                    case 342 -> "LALT";
                    case 346 -> "RALT";
                    case 32 -> "SPACE";
                    case 258 -> "TAB";
                    case 256 -> "ESC";
                    case 257 -> "ENTER";
                    case 280 -> "CAPS";
                    default -> "KEY_" + event.getKey();
                };
            } else {
                keyName = keyName.toUpperCase();
            }
            cfg.setHotkeyName(keyName);
            waitingKeyBind = false;
            if (TpAuraFeature.mc.player != null) {
                TpAuraFeature.mc.player.m_5661_(Component.literal((String)("\u00a76[TpAura] \u00a7a\u70ed\u952e\u5df2\u7ed1\u5b9a: \u00a7e" + keyName)), false);
            }
            if (onKeyBoundCallback != null) {
                onKeyBoundCallback.run();
                onKeyBoundCallback = null;
            }
            return;
        }
    }

    private static void updateAutoFlight() {
        LocalPlayer p = TpAuraFeature.mc.player;
        if (p == null) {
            return;
        }
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        if (cfg.autoFlight && cfg.enabled) {
            p.m_150110_().f_35935_ = true;
            p.f_108617_.m_104955_((Packet)new ServerboundPlayerAbilitiesPacket(p.m_150110_()));
            float fwd = p.f_108618_.f_108567_;
            float str = -p.f_108618_.f_108566_;
            float camYaw = TpAuraFeature.mc.f_91063_.m_109153_().m_90590_();
            Vec3 h = Vec3.m_82498_(0.0f, camYaw).m_82542_(fwd, 0.0, fwd).add(Vec3.m_82498_(0.0f, (camYaw + 90.0f)).m_82542_(str, 0.0, str));
            double hSpeed = cfg.autoFlightHorizontalSpeed;
            h = h.m_82556_() > 1.0E-4 ? h.normalize().scale(hSpeed) : Vec3.f_82478_;
            double vy = p.f_108618_.f_108572_ ? cfg.autoFlightSpeed : (p.f_108618_.f_108573_ ? -cfg.autoFlightSpeed : 0.0);
            p.m_20334_(h.x, vy, h.z);
            p.f_19864_ = true;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.f_91073_ == null) {
            return;
        }
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        TpAuraFeature.updateAutoFlight();
        if (!TpAuraFeature.isEnabled()) {
            return;
        }
        TpAuraFeature self = TpAuraFeature.getInstance();
        if (cfg.autoSwitch) {
            self.checkAndSwapWeapon(cfg);
        }
        if (("Smart".equals(cfg.attackMode) || "Universal".equals(cfg.attackMode)) && TpAuraFeature.mc.player.m_36403_(0.5f) < cfg.cooldownThreshold) {
            return;
        }
        if (self.delayTimer > 0) {
            --self.delayTimer;
            self.swapBackWeapon();
            return;
        }
        self.targets.clear();
        Entity target = self.findTarget(cfg);
        if (target == null) {
            self.currentTarget = null;
            self.swapBackWeapon();
            return;
        }
        self.currentTarget = target;
        self.executeTrouserAttack(target, cfg);
        self.swapBackWeapon();
        self.delayTimer = cfg.attackDelay;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        if (!TpAuraFeature.isEnabled()) {
            return;
        }
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.f_91073_ == null) {
            return;
        }
        TpAuraFeature self = TpAuraFeature.getInstance();
        TpAuraConfig cfg = TpAuraConfig.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = TpAuraFeature.mc.f_91063_.m_109153_().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        VertexConsumer consumer = mc.m_91269_().m_110104_().m_6299_((RenderType)RenderType.f_110371_);
        if (self.currentTarget != null) {
            TpAuraFeature.renderBox(poseStack, consumer, self.currentTarget.m_20191_(), cfg.getTargetColor());
        }
        if (cfg.renderPath && !self.renderPathNodes.isEmpty()) {
            int pathColor = cfg.getPathColor();
            float pr = (pathColor >> 16 & 0xFF) / 255.0f;
            float pg = (pathColor >> 8 & 0xFF) / 255.0f;
            float pb = (pathColor & 0xFF) / 255.0f;
            float pa = (pathColor >> 24 & 0xFF) / 255.0f;
            for (int i = 0; i < self.renderPathNodes.size(); ++i) {
                Vec3 n = self.renderPathNodes.get(i);
                TpAuraFeature.renderBox(poseStack, consumer, new AABB(n.x - 0.2, n.y, n.z - 0.2, n.x + 0.2, n.y + 2.0, n.z + 0.2), pathColor);
                if (i >= self.renderPathNodes.size() - 1) continue;
                Vec3 next = self.renderPathNodes.get(i + 1);
                Matrix4f mat = poseStack.last().pose();
                consumer.vertex(mat, n.x, (n.y + 1.0), n.z).m_85950_(pr, pg, pb, pa).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
                consumer.vertex(mat, next.x, (next.y + 1.0), next.z).m_85950_(pr, pg, pb, pa).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
            }
        }
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }
        if (System.currentTimeMillis() > overlayShowUntil) {
            return;
        }
        String text = "\u00a76[TpAura " + (TpAuraFeature.isEnabled() ? "\u00a7aON" : "\u00a7cOFF") + "\u00a76]";
        int w = mc.getWindow().m_85445_();
        int h = mc.getWindow().m_85446_();
        int textX = w / 2 - TpAuraFeature.mc.font.m_92895_(text) / 2;
        int textY = h - 62;
        event.getGuiGraphics().drawString(TpAuraFeature.mc.font, text, textX, textY, 0xFFFFFF);
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = (color >> 24 & 0xFF) / 255.0f;
        Matrix4f mat = poseStack.last().pose();
        double minX = box.f_82288_;
        double minY = box.f_82289_;
        double minZ = box.f_82290_;
        double maxX = box.f_82291_;
        double maxY = box.f_82292_;
        double maxZ = box.f_82293_;
        consumer.vertex(mat, minX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, -1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, minZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, maxX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, minY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
        consumer.vertex(mat, minX, maxY, maxZ).m_85950_(r, g, b, a).m_5601_(0.0f, 1.0f, 0.0f).endVertex();
    }

    private void executeTrouserAttack(Entity target, TpAuraConfig cfg) {
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.f_91073_ == null) {
            return;
        }
        if (TpAuraFeature.mc.player.f_108617_ == null) {
            return;
        }
        if (target == null || !target.m_6084_()) {
            return;
        }
        try {
            this.executeTrouserAttackInternal(target, cfg);
        }
        catch (Exception e) {
            this.cleanup();
        }
    }

    private void executeTrouserAttackInternal(Entity target, TpAuraConfig cfg) {
        boolean totemMode;
        int spam;
        Vec3 finalPos;
        Vec3 basePos = TpAuraFeature.mc.player.position();
        Vec3 targetPos = target.position();
        if (Double.isNaN(basePos.x) || Double.isNaN(basePos.y) || Double.isNaN(basePos.z)) {
            return;
        }
        if (Double.isNaN(targetPos.x) || Double.isNaN(targetPos.y) || Double.isNaN(targetPos.z)) {
            return;
        }
        double reach = cfg.maxRange;
        int worldMinY = TpAuraFeature.mc.f_91073_.m_141937_();
        int worldMaxY = TpAuraFeature.mc.f_91073_.m_151558_() - 1;
        if (basePos.y < worldMinY || basePos.y > worldMaxY) {
            return;
        }
        if ("Paper".equals(cfg.mode) && cfg.goUp && cfg.limitCeiling) {
            double safeHeight = this.getSafeCeilingHeight(basePos, reach, cfg.ceilingScanStep);
            reach = safeHeight <= basePos.y + 1.0 ? 0.0 : Math.min(reach, safeHeight - basePos.y);
        }
        if (cfg.tpOffset > 0) {
            finalPos = this.findRandomLandingPoint(targetPos, cfg.tpOffset, cfg.maxRange);
            if (finalPos == null) {
                finalPos = !this.invalid(targetPos) ? targetPos : this.findNearestPos(targetPos);
            }
        } else {
            Vec3 vec3 = finalPos = !this.invalid(targetPos) ? targetPos : this.findNearestPos(targetPos);
        }
        if (finalPos == null) {
            return;
        }
        Vec3 highStart = basePos.add(0.0, reach, 0.0);
        Vec3 highTarget = finalPos.add(0.0, reach, 0.0);
        this.renderPathNodes.clear();
        this.renderPathNodes.add(basePos);
        if ("Paper".equals(cfg.mode) && cfg.goUp) {
            this.renderPathNodes.add(highStart);
            this.renderPathNodes.add(highTarget);
        }
        this.renderPathNodes.add(finalPos);
        int n = spam = "Paper".equals(cfg.mode) ? cfg.paperPackets : 4;
        if (spam > 100) {
            spam = 100;
        }
        for (int i = 0; i < spam && TpAuraFeature.mc.player != null && TpAuraFeature.mc.player.f_108617_ != null; ++i) {
            TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(TpAuraFeature.mc.player.getX(), TpAuraFeature.mc.player.getY(), TpAuraFeature.mc.player.getZ(), false));
        }
        boolean bl = totemMode = cfg.totemBypass && "Paper".equals(cfg.mode);
        if (totemMode) {
            int attackCount = cfg.totemAttacks;
            for (int i = 0; i < attackCount; ++i) {
                int worldTop;
                int blocks = reach + i * cfg.totemHeightIncrease;
                if ("Paper".equals(cfg.mode) && cfg.goUp && cfg.limitCeiling && blocks > 0) {
                    double safeH = this.getSafeCeilingHeight(basePos, blocks, cfg.ceilingScanStep);
                    if (safeH <= basePos.y + 1.0) break;
                    blocks = Math.min(blocks, (safeH - basePos.y));
                }
                if (TpAuraFeature.mc.f_91073_ == null || !(basePos.y + blocks > (worldTop = TpAuraFeature.mc.f_91073_.m_151558_() - 1)) || (blocks = (worldTop - basePos.y)) >= 1) {
                    Vec3 progressiveAbove = new Vec3(basePos.x, basePos.y + blocks, basePos.z);
                    if (cfg.goUp) {
                        this.sendMove(progressiveAbove);
                    }
                    this.sendMove(finalPos);
                    this.performAttack(target, cfg);
                    continue;
                }
                break;
            }
        } else {
            if ("Paper".equals(cfg.mode) && cfg.goUp) {
                this.sendMove(highStart);
                this.sendMove(highTarget);
            }
            this.sendMove(finalPos);
            this.performAttack(target, cfg);
        }
        this.doReturn(basePos, finalPos, cfg);
        if (cfg.returnPos && TpAuraFeature.mc.player != null) {
            TpAuraFeature.mc.player.m_6034_(basePos.x, basePos.y, basePos.z);
            if (TpAuraFeature.mc.player.f_108617_ != null) {
                TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(basePos.x, basePos.y, basePos.z, false));
                TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(basePos.x, basePos.y, basePos.z, false));
            }
        }
    }

    private void performAttack(Entity target, TpAuraConfig cfg) {
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.player.f_108617_ == null) {
            return;
        }
        if (target == null) {
            return;
        }
        if (FakePlayerFeature.handleTpAuraAttack(target)) {
            if (cfg.swingHand) {
                TpAuraFeature.mc.player.m_6674_(InteractionHand.MAIN_HAND);
            }
            TpAuraFeature.mc.player.m_36334_();
            return;
        }
        TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)ServerboundInteractPacket.m_179605_(target, (boolean)TpAuraFeature.mc.player.m_6144_()));
        KillFXFeature.markAttackedByTpAura(target.m_19879_());
        KillIconFeature.markAttackedByTpAura(target.m_19879_());
        HealthTagManager.onAttack(target);
        if (cfg.swingHand) {
            TpAuraFeature.mc.player.m_6674_(InteractionHand.MAIN_HAND);
        }
        TpAuraFeature.mc.player.m_36334_();
    }

    private void sendMove(Vec3 pos) {
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.player.f_108617_ == null) {
            return;
        }
        TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, false));
    }

    private void doReturn(Vec3 startPos, Vec3 finalPos, TpAuraConfig cfg) {
        if (TpAuraFeature.mc.player == null) {
            return;
        }
        if (cfg.returnPos) {
            if ("Paper".equals(cfg.mode) && cfg.goUp) {
                double returnReach = cfg.maxRange;
                if (cfg.limitCeiling) {
                    double safeH = this.getSafeCeilingHeight(startPos, returnReach, cfg.ceilingScanStep);
                    returnReach = Math.max(0.0, safeH - startPos.y);
                }
                Vec3 highStart = startPos.add(0.0, Math.min(cfg.maxRange, returnReach), 0.0);
                Vec3 highTarget = finalPos.add(0.0, Math.min(cfg.maxRange, returnReach), 0.0);
                this.sendMove(highTarget);
                this.sendMove(highStart);
            }
            this.sendMove(startPos);
            if (cfg.offsetFix) {
                Vec3 offset = this.getOffset(startPos);
                this.sendMove(offset);
                TpAuraFeature.mc.player.m_6034_(offset.x, offset.y, offset.z);
            } else {
                TpAuraFeature.mc.player.m_6034_(startPos.x, startPos.y, startPos.z);
            }
        } else if (cfg.offsetFix) {
            Vec3 offset = this.getOffset(finalPos);
            this.sendMove(offset);
            TpAuraFeature.mc.player.m_6034_(offset.x, offset.y, offset.z);
        } else {
            TpAuraFeature.mc.player.m_6034_(finalPos.x, finalPos.y, finalPos.z);
        }
    }

    private Vec3 getOffset(Vec3 base) {
        double dx = 0.05;
        double dy = 0.01;
        List<Vec3> offsets = Arrays.asList(base.add(dx, dy, 0.0), base.add(-dx, dy, 0.0), base.add(0.0, dy, dx), base.add(0.0, dy, -dx));
        Collections.shuffle(offsets);
        for (Vec3 pos : offsets) {
            if (this.invalid(pos)) continue;
            return pos;
        }
        return base.add(0.0, dy, 0.0);
    }

    private boolean invalid(Vec3 pos) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return true;
        }
        BlockPos bp = BlockPos.m_274561_(pos.x, pos.y, pos.z);
        if (bp.m_123342_() < TpAuraFeature.mc.f_91073_.m_141937_() || bp.m_123342_() >= TpAuraFeature.mc.f_91073_.m_151558_()) {
            return true;
        }
        if (TpAuraFeature.mc.f_91073_.m_6325_(bp.m_123341_() >> 4, bp.m_123343_() >> 4) == null) {
            return true;
        }
        AABB box = TpAuraFeature.mc.player.m_20191_().m_82383_(pos.subtract(TpAuraFeature.mc.player.position()));
        for (BlockPos bPos : BlockPos.m_121940_((BlockPos)BlockPos.m_274561_(box.f_82288_, box.f_82289_, box.f_82290_), (BlockPos)BlockPos.m_274561_(box.f_82291_, box.f_82292_, box.f_82293_))) {
            BlockState state = TpAuraFeature.mc.f_91073_.m_8055_(bPos);
            if (state.m_60812_((BlockGetter)TpAuraFeature.mc.f_91073_, bPos).m_83281_() && state.m_60734_() != Blocks.f_49991_) continue;
            return true;
        }
        return false;
    }

    private Vec3 findNearestPos(Vec3 desired) {
        for (int dy = 0; dy <= 2; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    Vec3 test = desired.add(dx, dy, dz);
                    if (this.invalid(test)) continue;
                    return test;
                }
            }
        }
        return null;
    }

    private double getSafeCeilingHeight(Vec3 startPos, double maxHeight, int step) {
        if (TpAuraFeature.mc.f_91073_ == null) {
            return startPos.y + maxHeight;
        }
        for (int y = step; y <= maxHeight; y += step) {
            BlockPos checkPos = BlockPos.m_274561_(startPos.x, (startPos.y + y), startPos.z);
            BlockState state = TpAuraFeature.mc.f_91073_.m_8055_(checkPos);
            if (state.m_60795_() || state.m_60812_((BlockGetter)TpAuraFeature.mc.f_91073_, checkPos).m_83281_()) continue;
            return Math.max(startPos.y, startPos.y + y - 2.0);
        }
        return startPos.y + maxHeight;
    }

    private Vec3 findRandomLandingPoint(Vec3 center, int offset, double maxRange) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return null;
        }
        ArrayList<LandingCandidate> candidates = new ArrayList<LandingCandidate>();
        int radius = Math.max(0, offset);
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                for (int dy = -1; dy <= 1; ++dy) {
                    LandingCandidate candidate;
                    Vec3 testPos = center.add(dx, dy, 0.0);
                    if (TpAuraFeature.mc.player.m_20238_(testPos) > maxRange * maxRange || (candidate = this.evaluateLandingPoint(testPos)) == null) continue;
                    candidates.add(candidate);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return this.weightedRandomSelect(candidates);
    }

    private LandingCandidate evaluateLandingPoint(Vec3 pos) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return null;
        }
        BlockPos bp = BlockPos.m_274561_(pos.x, pos.y, pos.z);
        if (bp.m_123342_() < TpAuraFeature.mc.f_91073_.m_141937_() || bp.m_123342_() >= TpAuraFeature.mc.f_91073_.m_151558_()) {
            return null;
        }
        if (TpAuraFeature.mc.f_91073_.m_6325_(bp.m_123341_() >> 4, bp.m_123343_() >> 4) == null) {
            return null;
        }
        boolean hasBlockCollision = this.checkBlockCollision(pos);
        if (hasBlockCollision) {
            return new LandingCandidate(pos, 1);
        }
        boolean hasEntity = this.checkEntityAtPosition(pos);
        int weight = !hasEntity ? 100 : 50;
        return new LandingCandidate(pos, weight);
    }

    private boolean checkBlockCollision(Vec3 pos) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return true;
        }
        AABB box = TpAuraFeature.mc.player.m_20191_().m_82383_(pos.subtract(TpAuraFeature.mc.player.position()));
        for (BlockPos bPos : BlockPos.m_121940_((BlockPos)BlockPos.m_274561_(box.f_82288_, box.f_82289_, box.f_82290_), (BlockPos)BlockPos.m_274561_(box.f_82291_, box.f_82292_, box.f_82293_))) {
            BlockState state = TpAuraFeature.mc.f_91073_.m_8055_(bPos);
            if (state.m_60812_((BlockGetter)TpAuraFeature.mc.f_91073_, bPos).m_83281_() && state.m_60734_() != Blocks.f_49991_) continue;
            return true;
        }
        return false;
    }

    private boolean checkEntityAtPosition(Vec3 pos) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return false;
        }
        AABB checkArea = new AABB(pos.x - 0.5, pos.y, pos.z - 0.5, pos.x + 0.5, pos.y + 2.0, pos.z + 0.5);
        for (Entity e : TpAuraFeature.mc.f_91073_.m_45933_(null, checkArea)) {
            if (e == TpAuraFeature.mc.player || !e.m_6084_()) continue;
            return true;
        }
        return false;
    }

    private Vec3 weightedRandomSelect(List<LandingCandidate> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0).pos;
        }
        long totalWeight = 0L;
        for (LandingCandidate c : candidates) {
            totalWeight += c.weight;
        }
        long rand = ThreadLocalRandom.current().nextLong(totalWeight);
        long cumulative = 0L;
        for (LandingCandidate c : candidates) {
            if (rand >= (cumulative += c.weight)) continue;
            return c.pos;
        }
        return candidates.get((candidates.size() - 1)).pos;
    }

    private Entity findTarget(TpAuraConfig cfg) {
        if (TpAuraFeature.mc.f_91073_ == null || TpAuraFeature.mc.player == null) {
            return null;
        }
        Set<String> allowedTypes = cfg.getEntityTypeSet();
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : TpAuraFeature.mc.f_91073_.m_104735_()) {
            if (!this.entityFilter(entity, cfg, allowedTypes)) continue;
            double dist = TpAuraFeature.mc.player.distanceTo(entity);
            double effectiveRange = cfg.maxRange;
            if (!(dist < bestDist) || !(dist <= effectiveRange)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private boolean entityFilter(Entity entity, TpAuraConfig cfg, Set<String> allowedTypes) {
        Player p;
        TamableAnimal ta;
        String entityTypeKey;
        if (!(entity instanceof LivingEntity) || !entity.m_6084_() || entity == TpAuraFeature.mc.player) {
            return false;
        }
        if (!cfg.attackAllEntities && !allowedTypes.contains(entityTypeKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.m_6095_()).m_135815_().toLowerCase())) {
            return false;
        }
        if (TpAuraFeature.mc.player.distanceTo(entity) > cfg.maxRange) {
            return false;
        }
        if (cfg.ignoreNamed && entity.m_8077_()) {
            return false;
        }
        if (cfg.ignoreTamed && entity instanceof TamableAnimal && (ta = (TamableAnimal)entity).m_21824_()) {
            return false;
        }
        if (cfg.whitelistEnabled) {
            String entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.m_6095_()).m_135815_().toLowerCase();
            List wl = Arrays.stream(cfg.whitelist.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (wl.contains(entityType)) {
                return false;
            }
        }
        return !(entity instanceof Player) || !(p = (Player)entity).m_7500_() && !p.m_5833_();
    }

    private int findWeaponInventorySlot() {
        if (TpAuraFeature.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 45; ++i) {
            String name = TpAuraFeature.mc.player.m_150109_().m_8020_(i).m_41720_().toString().toLowerCase();
            if (!name.contains("sword") && !name.contains("mace") && !name.contains("axe")) continue;
            return i;
        }
        return -1;
    }

    private boolean checkAndSwapWeapon(TpAuraConfig cfg) {
        boolean isWeapon;
        if (TpAuraFeature.mc.player == null || TpAuraFeature.mc.player.f_108617_ == null) {
            return false;
        }
        ItemStack mainHand = TpAuraFeature.mc.player.m_21205_();
        String itemName = mainHand.m_41720_().toString().toLowerCase();
        boolean bl = isWeapon = itemName.contains("sword") || itemName.contains("mace") || itemName.contains("axe");
        if (isWeapon && (!cfg.requireMace || itemName.contains("mace"))) {
            return true;
        }
        if (cfg.silentSwap) {
            int slot = this.findWeaponInventorySlot();
            if (slot != -1) {
                this.silentSwapSlot = slot;
                this.silentSwapPrevSlot = TpAuraFeature.mc.player.m_150109_().f_35977_;
                if (slot >= 36) {
                    TpAuraFeature.mc.player.m_150109_().f_35977_ = slot - 36;
                } else {
                    TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(0, TpAuraFeature.mc.player.f_36096_.m_182424_(), slot, 0, ClickType.SWAP, TpAuraFeature.mc.player.f_36096_.m_142621_(), (Int2ObjectMap)new Int2ObjectOpenHashMap()));
                    TpAuraFeature.mc.player.m_150109_().f_35977_ = 0;
                }
                return true;
            }
        } else {
            for (int i = 0; i < 9; ++i) {
                String name = TpAuraFeature.mc.player.m_150109_().m_8020_(i).m_41720_().toString().toLowerCase();
                if (!name.contains("sword") && !name.contains("mace") && !name.contains("axe")) continue;
                if (this.originalSlot == -1) {
                    this.originalSlot = TpAuraFeature.mc.player.m_150109_().f_35977_;
                }
                TpAuraFeature.mc.player.m_150109_().f_35977_ = i;
                return true;
            }
        }
        return false;
    }

    private void swapBackWeapon() {
        if (this.silentSwapSlot == -1 && this.originalSlot == -1) {
            return;
        }
        if (this.silentSwapSlot != -1 && TpAuraFeature.mc.player != null && TpAuraFeature.mc.player.f_108617_ != null) {
            if (this.silentSwapSlot >= 36) {
                TpAuraFeature.mc.player.m_150109_().f_35977_ = this.silentSwapPrevSlot;
            } else {
                TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(0, TpAuraFeature.mc.player.f_36096_.m_182424_(), this.silentSwapSlot, 0, ClickType.SWAP, TpAuraFeature.mc.player.f_36096_.m_142621_(), (Int2ObjectMap)new Int2ObjectOpenHashMap()));
                TpAuraFeature.mc.player.m_150109_().f_35977_ = this.silentSwapPrevSlot;
                TpAuraFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClosePacket(TpAuraFeature.mc.player.f_36096_.f_38840_));
            }
            this.silentSwapSlot = -1;
            this.silentSwapPrevSlot = -1;
        }
        if (this.originalSlot != -1 && TpAuraFeature.mc.player != null) {
            TpAuraFeature.mc.player.m_150109_().f_35977_ = this.originalSlot;
            this.originalSlot = -1;
        }
    }

    private void cleanup() {
        this.swapBackWeapon();
        this.currentTarget = null;
        this.targets.clear();
        this.renderPathNodes.clear();
        this.delayTimer = 0;
    }

    static {
        overlayShowUntil = 0L;
        wasHotkeyDown = false;
        waitingKeyBind = false;
    }

    private static class LandingCandidate {
        final Vec3 pos;
        final int weight;

        LandingCandidate(Vec3 pos, int weight) {
            this.pos = pos;
            this.weight = weight;
        }
    }
}

