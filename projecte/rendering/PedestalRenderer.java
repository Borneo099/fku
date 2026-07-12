package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import moze_intel.projecte.gameObjs.block_entities.DMPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class PedestalRenderer implements BlockEntityRenderer {
   public PedestalRenderer(BlockEntityRendererProvider.Context context) {
   }

   public void render(@NotNull DMPedestalBlockEntity pedestal, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
      if (!pedestal.m_58901_() && pedestal.m_58904_() != null) {
         if (Minecraft.m_91087_().m_91290_().m_114377_()) {
            matrix.m_85836_();
            BlockPos pos = pedestal.m_58899_();
            AABB aabb = pedestal.getEffectBounds().m_82386_((double)(-pos.m_123341_()), (double)(-pos.m_123342_()), (double)(-pos.m_123343_()));
            VertexConsumer vertexBuilder = renderer.m_6299_(RenderType.m_110504_());
            LevelRenderer.m_109621_(matrix, vertexBuilder, aabb.f_82288_, aabb.f_82289_, aabb.f_82290_, aabb.f_82291_ + 1.0, aabb.f_82292_ + 1.0, aabb.f_82293_ + 1.0, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F);
            matrix.m_85849_();
         }

         ItemStack stack = pedestal.getInventory().getStackInSlot(0);
         if (!stack.m_41619_()) {
            matrix.m_85836_();
            matrix.m_85837_(0.5, 0.7, 0.5);
            long gameTime = pedestal.m_58904_().m_46467_();
            matrix.m_85837_(0.0, (double)Mth.m_14031_(((float)gameTime + partialTick) / 10.0F) * 0.1 + 0.1, 0.0);
            matrix.m_85841_(0.75F, 0.75F, 0.75F);
            float angle = ((float)gameTime + partialTick) / 20.0F * 57.295776F;
            matrix.m_252781_(Axis.f_252436_.m_252977_(angle));
            Minecraft.m_91087_().m_91291_().m_269128_(stack, ItemDisplayContext.GROUND, light, overlayLight, matrix, renderer, pedestal.m_58904_(), (int)pedestal.m_58899_().m_121878_());
            matrix.m_85849_();
         }
      }

   }
}
