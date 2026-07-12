package lexis.Client.OOCCommand.SignOOC;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Matrix4f;

@EventBusSubscriber({Dist.CLIENT})
public class SignOOCRegionHandler {
   private static boolean active = false;
   private static BlockPos pos1 = null;
   private static BlockPos pos2 = null;
   private static final int MAX_BLOCKS = 32768;
   private static final long QUERY_TIMEOUT_MS = 3000L;
   private static boolean importing = false;
   private static List queriedPositions = new ArrayList();
   private static long importDeadline = 0L;

   public static void activate() {
      active = true;
      pos1 = null;
      pos2 = null;
      importing = false;
      queriedPositions = new ArrayList();
   }

   @SubscribeEvent
   public static void onMouseButton(InputEvent.MouseButton.Pre event) {
      Minecraft mc = Minecraft.m_91087_();
      if (active && mc.f_91080_ == null) {
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            if (event.getAction() == 1) {
               if (mc.f_91077_ != null && mc.f_91077_.m_6662_() == Type.BLOCK) {
                  BlockHitResult blockHit = (BlockHitResult)mc.f_91077_;
                  BlockPos pos = blockHit.m_82425_().m_7949_();
                  LocalPlayer var10000;
                  int var10001;
                  if (event.getButton() == 0) {
                     pos1 = pos;
                     event.setCanceled(true);
                     var10000 = mc.f_91074_;
                     var10001 = pos.m_123341_();
                     var10000.m_5661_(Component.m_237113_("§d[§6Lexis§d] §fpos1 = " + var10001 + ", " + pos.m_123342_() + ", " + pos.m_123343_()), false);
                  } else if (event.getButton() == 1) {
                     pos2 = pos;
                     event.setCanceled(true);
                     var10000 = mc.f_91074_;
                     var10001 = pos.m_123341_();
                     var10000.m_5661_(Component.m_237113_("§d[§6Lexis§d] §fpos2 = " + var10001 + ", " + pos.m_123342_() + ", " + pos.m_123343_()), false);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onKeyInput(InputEvent.Key event) {
      Minecraft mc = Minecraft.m_91087_();
      if (event.getAction() == 1) {
         if (active && mc.f_91080_ == null) {
            if (mc.f_91074_ != null && mc.f_91073_ != null) {
               if (event.getKey() == 86) {
                  active = false;
                  importing = false;
                  pos1 = null;
                  pos2 = null;
                  BlockDataQueryHandler.clear();
                  mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已退出区域选择模式"), false);
               } else if (event.getKey() == 67) {
                  pos1 = null;
                  pos2 = null;
                  mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f已清空 pos1/pos2"), false);
               } else {
                  if (event.getKey() == 72) {
                     if (importing) {
                        mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f正在读取方块数据中, 等一下..."), false);
                        return;
                     }

                     if (pos1 == null || pos2 == null) {
                        mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f先用 左键设pos1 右键设pos2"), false);
                        return;
                     }

                     long volume = (long)(Math.abs(pos1.m_123341_() - pos2.m_123341_()) + 1) * (long)(Math.abs(pos1.m_123342_() - pos2.m_123342_()) + 1) * (long)(Math.abs(pos1.m_123343_() - pos2.m_123343_()) + 1);
                     if (volume > 32768L) {
                        mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §c区域太大(" + volume + " 格)！上限 32768，请缩小范围"), false);
                        return;
                     }

                     BlockDataQueryHandler.clear();
                     queriedPositions = new ArrayList();
                     Iterator var4 = BlockPos.m_121940_(pos1, pos2).iterator();

                     while(var4.hasNext()) {
                        BlockPos p = (BlockPos)var4.next();
                        BlockState state = mc.f_91073_.m_8055_(p);
                        if (state != null && !state.m_60795_() && mc.f_91073_.m_7702_(p) != null) {
                           BlockPos ip = p.m_7949_();
                           queriedPositions.add(ip);
                           BlockDataQueryHandler.queryBlockEntity(ip);
                        }
                     }

                     if (queriedPositions.isEmpty()) {
                        assembleAndOpen();
                        return;
                     }

                     importing = true;
                     importDeadline = System.currentTimeMillis() + 3000L;
                     mc.f_91074_.m_5661_(Component.m_237113_("§d[§6Lexis§d] §f正在读取 " + queriedPositions.size() + " 个方块的服务端完整数据..."), false);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         if (importing) {
            boolean allDone = true;
            Iterator var2 = queriedPositions.iterator();

            while(var2.hasNext()) {
               BlockPos p = (BlockPos)var2.next();
               if (!BlockDataQueryHandler.hasResponded(p)) {
                  allDone = false;
                  break;
               }
            }

            if (allDone || System.currentTimeMillis() > importDeadline) {
               assembleAndOpen();
            }

         }
      }
   }

   private static void assembleAndOpen() {
      Minecraft mc = Minecraft.m_91087_();
      importing = false;
      if (mc.f_91074_ != null && mc.f_91073_ != null && pos1 != null && pos2 != null) {
         List commands = new ArrayList();
         int failCount = 0;
         Iterator var3 = BlockPos.m_121940_(pos1, pos2).iterator();

         while(var3.hasNext()) {
            BlockPos pos = (BlockPos)var3.next();
            BlockState state = mc.f_91073_.m_8055_(pos);
            if (state != null && !state.m_60795_()) {
               String command = generateSetblockCommand(pos.m_7949_());
               if (command != null) {
                  commands.add(command);
               } else {
                  ++failCount;
               }
            }
         }

         SignOOCScreen screen = new SignOOCScreen((Screen)null);
         mc.m_91152_(screen);
         Iterator var8 = commands.iterator();

         while(var8.hasNext()) {
            String cmd = (String)var8.next();
            screen.addCommand(cmd);
         }

         String msg = "§d[§6Lexis§d] §f已导入 " + commands.size() + " 个方块命令到告示牌OOC生成器";
         if (failCount > 0) {
            msg = msg + " §c(" + failCount + " 个失败)";
         }

         mc.f_91074_.m_5661_(Component.m_237113_(msg), false);
         cleanup();
      } else {
         cleanup();
      }
   }

   private static void cleanup() {
      active = false;
      importing = false;
      pos1 = null;
      pos2 = null;
      queriedPositions = new ArrayList();
      BlockDataQueryHandler.clear();
   }

   private static String generateSetblockCommand(BlockPos pos) {
      Minecraft mc = Minecraft.m_91087_();
      BlockState state = mc.f_91073_.m_8055_(pos);
      if (state != null && !state.m_60795_()) {
         Block block = state.m_60734_();
         ResourceLocation blockId = BuiltInRegistries.f_256975_.m_7981_(block);
         String blockName = blockId.toString();
         int var10002 = pos.m_123341_();
         StringBuilder command = new StringBuilder("setblock " + var10002 + " " + pos.m_123342_() + " " + pos.m_123343_() + " ");
         command.append(blockName);
         StringBuilder stateBuilder = new StringBuilder();

         Property prop;
         String valueStr;
         for(Iterator var8 = state.m_61147_().iterator(); var8.hasNext(); stateBuilder.append(prop.m_61708_()).append("=").append(valueStr)) {
            prop = (Property)var8.next();
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

         CompoundTag nbt = BlockDataQueryHandler.getStoredData(pos);
         BlockEntity entity;
         if (nbt != null) {
            nbt = nbt.m_6426_();
         } else {
            entity = mc.f_91073_.m_7702_(pos);
            if (entity != null) {
               nbt = entity.m_187481_();
            }
         }

         if (nbt != null && !nbt.m_128456_()) {
            nbt.m_128473_("x");
            nbt.m_128473_("y");
            nbt.m_128473_("z");
            nbt.m_128473_("id");
            entity = mc.f_91073_.m_7702_(pos);
            if (entity instanceof CommandBlockEntity) {
               CommandBlockEntity cmdBlock = (CommandBlockEntity)entity;
               if (!nbt.m_128441_("auto")) {
                  nbt.m_128344_("auto", (byte)(cmdBlock.m_59143_() ? 1 : 0));
               }
            }

            command.append(nbt.toString());
         }

         return command.toString();
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS) {
         if (active) {
            if (pos1 != null || pos2 != null) {
               Minecraft mc = Minecraft.m_91087_();
               PoseStack poseStack = event.getPoseStack();
               Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
               float phase = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;
               float tri = phase < 0.5F ? phase * 2.0F : (1.0F - phase) * 2.0F;
               float hue = 0.66F + 0.3F * tri;
               Color c = Color.getHSBColor(hue, 0.9F, 1.0F);
               float r = (float)c.getRed() / 255.0F;
               float g = (float)c.getGreen() / 255.0F;
               float b = (float)c.getBlue() / 255.0F;
               float ax;
               float ay;
               float az;
               float bx;
               float by;
               float bz;
               if (pos1 != null && pos2 != null) {
                  ax = (float)Math.min(pos1.m_123341_(), pos2.m_123341_());
                  ay = (float)Math.min(pos1.m_123342_(), pos2.m_123342_());
                  az = (float)Math.min(pos1.m_123343_(), pos2.m_123343_());
                  bx = (float)(Math.max(pos1.m_123341_(), pos2.m_123341_()) + 1);
                  by = (float)(Math.max(pos1.m_123342_(), pos2.m_123342_()) + 1);
                  bz = (float)(Math.max(pos1.m_123343_(), pos2.m_123343_()) + 1);
               } else {
                  BlockPos p = pos1 != null ? pos1 : pos2;
                  ax = (float)p.m_123341_();
                  ay = (float)p.m_123342_();
                  az = (float)p.m_123343_();
                  bx = (float)(p.m_123341_() + 1);
                  by = (float)(p.m_123342_() + 1);
                  bz = (float)(p.m_123343_() + 1);
               }

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
               buffer.m_166779_(Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
               line(buffer, matrix, ax, ay, az, bx, ay, az, r, g, b);
               line(buffer, matrix, bx, ay, az, bx, ay, bz, r, g, b);
               line(buffer, matrix, bx, ay, bz, ax, ay, bz, r, g, b);
               line(buffer, matrix, ax, ay, bz, ax, ay, az, r, g, b);
               line(buffer, matrix, ax, by, az, bx, by, az, r, g, b);
               line(buffer, matrix, bx, by, az, bx, by, bz, r, g, b);
               line(buffer, matrix, bx, by, bz, ax, by, bz, r, g, b);
               line(buffer, matrix, ax, by, bz, ax, by, az, r, g, b);
               line(buffer, matrix, ax, ay, az, ax, by, az, r, g, b);
               line(buffer, matrix, bx, ay, az, bx, by, az, r, g, b);
               line(buffer, matrix, bx, ay, bz, bx, by, bz, r, g, b);
               line(buffer, matrix, ax, ay, bz, ax, by, bz, r, g, b);
               tesselator.m_85914_();
               poseStack.m_85849_();
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
               RenderSystem.lineWidth(1.0F);
            }
         }
      }
   }

   private static void line(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
      buf.m_252986_(m, x1, y1, z1).m_85950_(r, g, b, 1.0F).m_5752_();
      buf.m_252986_(m, x2, y2, z2).m_85950_(r, g, b, 1.0F).m_5752_();
   }
}
