package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.function.Predicate;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class ChestRenderer implements BlockEntityRenderer {
   private final ModelPart lid;
   private final ModelPart bottom;
   private final ModelPart lock;
   private final Predicate blockChecker;
   private final ResourceLocation texture;

   public ChestRenderer(BlockEntityRendererProvider.Context context, ResourceLocation texture, Supplier type) {
      this.texture = texture;
      this.blockChecker = (block) -> {
         return block == ((BlockRegistryObject)type.get()).getBlock();
      };
      ModelPart modelpart = context.m_173582_(ModelLayers.f_171275_);
      this.bottom = modelpart.m_171324_("bottom");
      this.lid = modelpart.m_171324_("lid");
      this.lock = modelpart.m_171324_("lock");
   }

   public void render(@NotNull EmcChestBlockEntity chest, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
      matrix.m_85836_();
      if (chest.m_58904_() != null && !chest.m_58901_()) {
         BlockState state = chest.m_58904_().m_8055_(chest.m_58899_());
         if (this.blockChecker.test(state.m_60734_())) {
            matrix.m_85837_(0.5, 0.5, 0.5);
            matrix.m_252781_(Axis.f_252436_.m_252977_(-((Direction)state.m_61143_(BlockStateProperties.f_61374_)).m_122435_()));
            matrix.m_85837_(-0.5, -0.5, -0.5);
         }
      }

      float lidAngle = 1.0F - chest.m_6683_(partialTick);
      lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
      VertexConsumer builder = renderer.m_6299_(RenderType.m_110452_(this.texture));
      this.lid.f_104203_ = -(lidAngle * 1.5707964F);
      this.lock.f_104203_ = this.lid.f_104203_;
      this.lid.m_104301_(matrix, builder, light, overlayLight);
      this.lock.m_104301_(matrix, builder, light, overlayLight);
      this.bottom.m_104301_(matrix, builder, light, overlayLight);
      matrix.m_85849_();
   }
}
