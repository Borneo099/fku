package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import fku.org.example.fku.mixin.ClientLevelAccessor;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class SuperDistanceInteraction {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final SuperDistanceInteraction INSTANCE = new SuperDistanceInteraction();
    private static final UUID RANGE_MODIFIER_UUID = UUID.fromString("a0b8e4f2-1c3d-5e6f-7a8b-9c0d1e2f3a4b");
    private static final String RANGE_MODIFIER_NAME = "WorldEdit super range";
    private ItemStack originalHelmet = ItemStack.EMPTY;
    private boolean helmetEquipped = false;
    private int originalSelectedSlot = -1;

    public static SuperDistanceInteraction getInstance() {
        return INSTANCE;
    }

    private SuperDistanceInteraction() {
    }

    public void enable() {
        AttributeInstance attr;
        if (SuperDistanceInteraction.mc.player == null) {
            return;
        }
        this.originalHelmet = SuperDistanceInteraction.mc.player.getItemBySlot(EquipmentSlot.HEAD).copy();
        ItemStack button = new ItemStack((ItemLike)Items.OAK_BUTTON, 1);
        AttributeModifier modifier = new AttributeModifier(RANGE_MODIFIER_UUID, RANGE_MODIFIER_NAME, 9999.0, AttributeModifier.Operation.ADDITION);
        button.addAttributeModifier((Attribute)ForgeMod.BLOCK_REACH.get(), modifier, EquipmentSlot.HEAD);
        SuperDistanceInteraction.mc.player.setItemSlot(EquipmentSlot.HEAD, button);
        if (SuperDistanceInteraction.mc.player.getAbilities().instabuild) {
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(5, button));
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(103, button));
        }
        if ((attr = SuperDistanceInteraction.mc.player.getAttribute((Attribute)ForgeMod.BLOCK_REACH.get())) != null && !attr.hasModifier(modifier)) {
            attr.addTransientModifier(modifier);
        }
        this.helmetEquipped = true;
        Fku.LOGGER.info("[WorldEdit] \u8d85\u8fdc\u8ddd\u79bb\u4ea4\u4e92\u5df2\u6fc0\u6d3b (BLOCK_REACH +9999)");
    }

    public void disable() {
        if (SuperDistanceInteraction.mc.player == null) {
            return;
        }
        AttributeInstance attr = SuperDistanceInteraction.mc.player.getAttribute((Attribute)ForgeMod.BLOCK_REACH.get());
        if (attr != null) {
            attr.removeModifier(RANGE_MODIFIER_UUID);
        }
        SuperDistanceInteraction.mc.player.setItemSlot(EquipmentSlot.HEAD, this.originalHelmet);
        if (SuperDistanceInteraction.mc.player.getAbilities().instabuild) {
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(5, this.originalHelmet));
        }
        this.originalHelmet = ItemStack.EMPTY;
        this.helmetEquipped = false;
        Fku.LOGGER.info("[WorldEdit] \u8d85\u8fdc\u8ddd\u79bb\u4ea4\u4e92\u5df2\u505c\u7528");
    }

    public boolean handleClick(int button, InteractionHand hand) {
        double range;
        Vec3 lookVec;
        Vec3 endPos;
        if (SuperDistanceInteraction.mc.player == null || SuperDistanceInteraction.mc.level == null) {
            return false;
        }
        if (!this.helmetEquipped) {
            return false;
        }
        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled) {
            return false;
        }
        this.originalSelectedSlot = SuperDistanceInteraction.mc.player.getInventory().selected;
        Vec3 eyePos = SuperDistanceInteraction.mc.player.getEyePosition(1.0f);
        BlockHitResult hitResult = this.customRayTrace(eyePos, endPos = eyePos.add((lookVec = SuperDistanceInteraction.mc.player.getLookAngle()).scale(range = cfg.rangeMultiplier)));
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            this.restoreSlot();
            return false;
        }
        BlockPos targetPos = hitResult.getBlockPos();
        if (button == 0) {
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            int seq = this.getSequence();
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, targetPos, hitResult.getDirection(), seq));
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, targetPos, hitResult.getDirection(), seq));
            SuperDistanceInteraction.mc.level.destroyBlock(targetPos, false);
        } else if (button == 1) {
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            Vec3 blockCenter = Vec3.atCenterOf((Vec3i)targetPos);
            Vec3 hitVec = blockCenter.add(Vec3.atLowerCornerOf((Vec3i)hitResult.getDirection().getNormal()).scale(0.5));
            BlockHitResult placeHit = new BlockHitResult(hitVec, hitResult.getDirection(), targetPos, false);
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(hand != null ? hand : InteractionHand.MAIN_HAND, placeHit, this.getSequence()));
        }
        if (cfg.autoRestoreSlot) {
            this.restoreSlot();
        }
        return true;
    }

    private BlockHitResult customRayTrace(Vec3 start, Vec3 end) {
        if (SuperDistanceInteraction.mc.level == null) {
            return null;
        }
        return SuperDistanceInteraction.mc.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)SuperDistanceInteraction.mc.player));
    }

    private int getSequence() {
        if (SuperDistanceInteraction.mc.level == null) {
            return 0;
        }
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)SuperDistanceInteraction.mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    private void restoreSlot() {
        if (this.originalSelectedSlot >= 0 && SuperDistanceInteraction.mc.player != null) {
            SuperDistanceInteraction.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.originalSelectedSlot));
            this.originalSelectedSlot = -1;
        }
    }

    public boolean isHelmetEquipped() {
        return this.helmetEquipped;
    }
}

