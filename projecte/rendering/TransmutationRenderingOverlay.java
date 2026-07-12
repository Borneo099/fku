package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.utils.WorldTransmutations;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class TransmutationRenderingOverlay implements IGuiOverlay {
   private final Minecraft mc = Minecraft.m_91087_();
   private @Nullable BlockState transmutationResult;
   private long lastGameTime;

   public TransmutationRenderingOverlay() {
      MinecraftForge.EVENT_BUS.addListener(this::onOverlay);
   }

   public void render(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
      if (!this.mc.f_91066_.f_92062_ && this.transmutationResult != null) {
         Block var7 = this.transmutationResult.m_60734_();
         if (var7 instanceof LiquidBlock) {
            LiquidBlock liquidBlock = (LiquidBlock)var7;
            IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(liquidBlock.getFluid());
            int color = properties.getTintColor();
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            float alpha = (float)(color >> 24 & 255) / 255.0F;
            TextureAtlasSprite sprite = (TextureAtlasSprite)this.mc.m_91258_(TextureAtlas.f_118259_).apply(properties.getStillTexture());
            graphics.m_280565_(1, 1, 0, 16, 16, sprite, red, green, blue, alpha);
         } else {
            graphics.m_280480_(new ItemStack(this.transmutationResult.m_60734_()), 1, 1);
         }

         long gameTime = this.mc.f_91073_ == null ? 0L : this.mc.f_91073_.m_46467_();
         if (this.lastGameTime != gameTime) {
            this.transmutationResult = null;
            this.lastGameTime = gameTime;
         }
      }

   }

   private void onOverlay(RenderHighlightEvent.Block event) {
      Camera activeRenderInfo = event.getCamera();
      Entity var4 = activeRenderInfo.m_90592_();
      if (var4 instanceof Player player) {
         this.lastGameTime = this.mc.f_91073_ == null ? 0L : this.mc.f_91073_.m_46467_();
         Level level = player.m_9236_();
         ItemStack stack = player.m_21205_();
         if (stack.m_41619_()) {
            stack = player.m_21206_();
         }

         if (!stack.m_41619_()) {
            Item var7 = stack.m_41720_();
            if (var7 instanceof PhilosophersStone) {
               PhilosophersStone philoStone = (PhilosophersStone)var7;
               BlockHitResult rtr = philoStone.getHitBlock(player);
               if (rtr.m_6662_() == Type.BLOCK) {
                  BlockState current = level.m_8055_(rtr.m_82425_());
                  this.transmutationResult = WorldTransmutations.getWorldTransmutation(current, player.m_36341_());
                  if (this.transmutationResult != null) {
                     Vec3 viewPosition = activeRenderInfo.m_90583_();
                     int charge = philoStone.getCharge(stack);
                     byte mode = philoStone.getMode(stack);
                     float alpha = ProjectEConfig.client.pulsatingOverlay.get() ? this.getPulseProportion() * 0.6F : 0.35F;
                     VertexConsumer builder = event.getMultiBufferSource().m_6299_(PERenderType.TRANSMUTATION_OVERLAY);
                     PoseStack matrix = event.getPoseStack();
                     matrix.m_85836_();
                     matrix.m_85837_(-viewPosition.f_82479_, -viewPosition.f_82480_, -viewPosition.f_82481_);
                     CollisionContext selectionContext = CollisionContext.m_82750_(player);
                     Iterator var16 = PhilosophersStone.getChanges(level, rtr.m_82425_(), player, rtr.m_82434_(), mode, charge).keySet().iterator();

                     while(var16.hasNext()) {
                        BlockPos pos = (BlockPos)var16.next();
                        BlockState state = level.m_8055_(pos);
                        if (!state.m_60795_()) {
                           VoxelShape shape = state.m_60651_(level, pos, selectionContext);
                           if (!shape.m_83281_()) {
                              matrix.m_85836_();
                              matrix.m_252880_((float)pos.m_123341_(), (float)pos.m_123342_(), (float)pos.m_123343_());
                              Matrix4f matrix4f = matrix.m_85850_().m_252922_();
                              shape.m_83286_((minX, minY, minZ, maxX, maxY, maxZ) -> {
                                 this.addBox(builder, matrix4f, alpha, (float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ);
                              });
                              matrix.m_85849_();
                           }
                        }
                     }

                     matrix.m_85849_();
                  }
               } else {
                  this.transmutationResult = null;
               }

               return;
            }
         }

         this.transmutationResult = null;
      }
   }

   private void addBox(VertexConsumer builder, Matrix4f matrix4f, float alpha, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      builder.m_252986_(matrix4f, minX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, minX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, maxY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, minZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
      builder.m_252986_(matrix4f, maxX, minY, maxZ).m_85950_(1.0F, 1.0F, 1.0F, alpha).m_5752_();
   }

   private float getPulseProportion() {
      return (float)(0.5 * Math.sin((double)System.currentTimeMillis() / 350.0) + 0.5);
   }
}
