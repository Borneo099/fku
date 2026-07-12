package lexis.Client.OOCCommand.CommandBlockOOC;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber({Dist.CLIENT})
public class StealBlockHandler {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final Map selectedBlocks = new ConcurrentHashMap();
   private static long rainbowStartTime = System.currentTimeMillis();
   private static boolean isActive = false;

   @SubscribeEvent
   public static void onMouseButton(InputEvent.MouseButton.Pre event) {
      if (isActive && mc.f_91080_ == null) {
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            ItemStack mainHand = mc.f_91074_.m_21205_();
            if (mainHand.m_41720_() == Items.f_42500_) {
               CompoundTag tag = mainHand.m_41783_();
               if (tag != null && tag.m_128441_("LexisXCherryStealBlocks")) {
                  if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.BLOCK) {
                     BlockHitResult blockHit = (BlockHitResult)mc.f_91077_;
                     BlockPos pos = blockHit.m_82425_();
                     if (event.getButton() == 1 && event.getAction() == 1) {
                        LocalPlayer var10000;
                        int var10001;
                        if (!selectedBlocks.containsKey(pos)) {
                           selectedBlocks.put(pos, "selected");
                           var10000 = mc.f_91074_;
                           var10001 = pos.m_123341_();
                           var10000.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已添加方块: " + var10001 + ", " + pos.m_123342_() + ", " + pos.m_123343_() + " (总数: " + selectedBlocks.size() + ")"), false);
                        } else {
                           selectedBlocks.remove(pos);
                           var10000 = mc.f_91074_;
                           var10001 = pos.m_123341_();
                           var10000.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已移除方块: " + var10001 + ", " + pos.m_123342_() + ", " + pos.m_123343_() + " (总数: " + selectedBlocks.size() + ")"), false);
                        }

                        event.setCanceled(true);
                     }

                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onKeyInput(InputEvent.Key event) {
      if (event.getAction() == 1) {
         if (isActive && mc.f_91080_ == null) {
            if (event.getKey() == 86) {
               isActive = false;
               selectedBlocks.clear();
               mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已退出方块选择模式"), false);
            } else {
               if (event.getKey() == 72) {
                  if (!selectedBlocks.isEmpty()) {
                     List commands = new ArrayList();
                     Iterator var2 = selectedBlocks.entrySet().iterator();

                     while(var2.hasNext()) {
                        Map.Entry entry = (Map.Entry)var2.next();
                        BlockPos pos = (BlockPos)entry.getKey();
                        String command = generateSetblockCommand(pos);
                        if (command != null) {
                           commands.add(command);
                        }
                     }

                     CommandBlockOOCScreen screen = new CommandBlockOOCScreen((Screen)null);
                     mc.m_91152_(screen);
                     Iterator var7 = commands.iterator();

                     while(var7.hasNext()) {
                        String cmd = (String)var7.next();
                        screen.addCommand(cmd);
                     }

                     mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已导入 " + commands.size() + " 个方块命令到OOC生成器"), false);
                     clearTaggedBones();
                     selectedBlocks.clear();
                     isActive = false;
                  } else {
                     mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f先用右键选择方块"), false);
                  }
               }

               if (event.getKey() == 67) {
                  selectedBlocks.clear();
                  mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已清空所有选择的方块"), false);
               }

            }
         }
      }
   }

   private static void clearTaggedBones() {
      if (mc.f_91074_ != null) {
         int clearedCount = 0;

         for(int i = 0; i < mc.f_91074_.m_150109_().f_35974_.size(); ++i) {
            ItemStack stack = (ItemStack)mc.f_91074_.m_150109_().f_35974_.get(i);
            if (stack.m_41720_() == Items.f_42500_) {
               CompoundTag tag = stack.m_41783_();
               if (tag != null && tag.m_128441_("LexisXCherryStealBlocks")) {
                  int slot = i < 9 ? i + 36 : i;
                  Utils.safeRemoveItem(slot, stack);
                  ++clearedCount;
               }
            }
         }

         ItemStack offhand = mc.f_91074_.m_21206_();
         if (offhand.m_41720_() == Items.f_42500_) {
            CompoundTag tag = offhand.m_41783_();
            if (tag != null && tag.m_128441_("LexisXCherryStealBlocks")) {
               Utils.safeRemoveItem(45, offhand);
               ++clearedCount;
            }
         }

      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
         if (isActive && !selectedBlocks.isEmpty()) {
            PoseStack poseStack = event.getPoseStack();
            Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.lineWidth(3.0F);
            RenderSystem.setShader(GameRenderer::m_172811_);
            poseStack.m_85836_();
            poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            Tesselator tesselator = Tesselator.m_85913_();
            BufferBuilder buffer = tesselator.m_85915_();
            long time = System.currentTimeMillis() - rainbowStartTime;
            float hue = (float)(time % 5000L) / 5000.0F;
            Color rainbowColor = Color.getHSBColor(hue, 0.9F, 1.0F);
            float r = (float)rainbowColor.getRed() / 255.0F;
            float g = (float)rainbowColor.getGreen() / 255.0F;
            float b = (float)rainbowColor.getBlue() / 255.0F;
            Iterator var13 = selectedBlocks.entrySet().iterator();

            while(var13.hasNext()) {
               Map.Entry entry = (Map.Entry)var13.next();
               BlockPos pos = (BlockPos)entry.getKey();
               float x = (float)pos.m_123341_();
               float y = (float)pos.m_123342_();
               float z = (float)pos.m_123343_();
               buffer.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
               buffer.m_252986_(matrix, x, y, z).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x, y, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x, y, z).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 0.3F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z).m_85950_(r, g, b, 0.3F).m_5752_();
               tesselator.m_85914_();
               RenderSystem.lineWidth(3.0F);
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               buffer.m_252986_(matrix, x, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x + 1.0F, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               buffer.m_252986_(matrix, x, y + 1.0F, z + 1.0F).m_85950_(r, g, b, 1.0F).m_5752_();
               tesselator.m_85914_();
            }

            poseStack.m_85849_();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.lineWidth(1.0F);
         }
      }
   }

   private static String generateSetblockCommand(BlockPos pos) {
      BlockState state = mc.f_91073_.m_8055_(pos);
      if (state != null && !state.m_60795_()) {
         Block block = state.m_60734_();
         String blockName = block.m_7705_().replace("block.minecraft.", "minecraft:");
         if (blockName.startsWith("block.")) {
            String[] parts = blockName.split("\\.");
            if (parts.length >= 3) {
               blockName = parts[1] + ":" + parts[2];
            }
         }

         int var10002 = pos.m_123341_();
         StringBuilder command = new StringBuilder("setblock " + var10002 + " " + pos.m_123342_() + " " + pos.m_123343_() + " ");
         command.append(blockName);
         StringBuilder stateBuilder = new StringBuilder();

         Property prop;
         String valueStr;
         for(Iterator var6 = state.m_61147_().iterator(); var6.hasNext(); stateBuilder.append(prop.m_61708_()).append("=").append(valueStr)) {
            prop = (Property)var6.next();
            if (stateBuilder.length() > 0) {
               stateBuilder.append(",");
            }

            Comparable value = state.m_61143_(prop);
            if (value instanceof Boolean) {
               valueStr = value.toString().toLowerCase();
            } else if (value instanceof Direction) {
               valueStr = ((Direction)value).m_122433_();
            } else {
               valueStr = value.toString().toLowerCase();
            }
         }

         if (stateBuilder.length() > 0) {
            command.append("[").append(stateBuilder).append("]");
         }

         BlockEntity entity = mc.f_91073_.m_7702_(pos);
         if (entity != null) {
            CompoundTag nbt = entity.m_187481_();
            if (nbt != null && !nbt.m_128456_()) {
               nbt.m_128473_("x");
               nbt.m_128473_("y");
               nbt.m_128473_("z");
               nbt.m_128473_("id");
               if (entity instanceof CommandBlockEntity) {
                  CommandBlockEntity cmdBlock = (CommandBlockEntity)entity;
                  boolean isAuto = cmdBlock.m_59143_();
                  if (!nbt.m_128441_("auto")) {
                     nbt.m_128344_("auto", (byte)(isAuto ? 1 : 0));
                  }
               }

               String nbtString = nbt.toString();
               command.append(nbtString);
            }
         }

         return command.toString();
      } else {
         return null;
      }
   }

   public static void activate() {
      isActive = true;
      selectedBlocks.clear();
   }
}
