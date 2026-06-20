package fku.org.example.fku.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * ClientLevelAccessor —— 暴露 ClientLevel 的 blockStatePredictionHandler 私有字段
 *
 * ★ 用途：
 *   参考 CheatUtils 的实现，通过此 Mixin 获取 BlockStatePredictionHandler，
 *   用于获取正确的预测序列号（currentSequence），确保发送的 ServerboundUseItemOnPacket
 *   和 ServerboundPlayerActionPacket 的序列号与服务端预测系统一致。
 *
 *   不使用正确的序列号将导致服务端拒绝所有数据包，功能完全失效。
 */
@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {

    @Accessor("blockStatePredictionHandler")
    BlockStatePredictionHandler getBlockStatePredictionHandler_CU();
}