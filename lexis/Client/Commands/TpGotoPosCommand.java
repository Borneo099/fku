package lexis.Client.Commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lexis.Hack.Utils.Color;
import lexis.Hack.Utils.Render.RenderUtils;
import lexis.Hack.Utils.pathfinding.AStarPathFinder;
import lexis.Hack.events.EventManager;
import lexis.Hack.events.RenderListener;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "lexis"
)
public class TpGotoPosCommand {
   private static final int[] GRADIENT_COLORS = new int[]{-2461482, -2252579, -1146130, -18751, -38476};
   private static boolean active = false;
   private static boolean moving = false;
   private static List path = null;
   private static int pathIndex = 0;
   private static Vec3 targetPos = null;
   private static long lastPacketTime = 0L;
   private static final long PACKET_INTERVAL_MS = 30L;
   private static final double STOP_DISTANCE = 1.5;
   private static boolean waitingForChunk = false;
   private static List renderPathPoints = null;
   private static boolean rendererRegistered = false;
   private static final RenderListener renderListener = (poseStack, partialTick) -> {
      if (renderPathPoints != null && renderPathPoints.size() >= 2 && active) {
         int totalSegments = renderPathPoints.size() - 1;

         for(int i = 0; i < totalSegments; ++i) {
            Vec3 a = (Vec3)renderPathPoints.get(i);
            Vec3 b = (Vec3)renderPathPoints.get(i + 1);
            float progress = (float)i / (float)totalSegments;
            int color = interpolateColor(progress);
            RenderUtils.drawLine(poseStack, a.f_82479_, a.f_82480_, a.f_82481_, b.f_82479_, b.f_82480_, b.f_82481_, new Color(color));
         }

      }
   };

   private static int interpolateColor(float progress) {
      int index = (int)(progress * (float)(GRADIENT_COLORS.length - 1));
      float blend = progress * (float)(GRADIENT_COLORS.length - 1) - (float)index;
      if (index >= GRADIENT_COLORS.length - 1) {
         return GRADIENT_COLORS[GRADIENT_COLORS.length - 1];
      } else {
         int c1 = GRADIENT_COLORS[index];
         int c2 = GRADIENT_COLORS[index + 1];
         int r = (int)((float)(c1 >> 16 & 255) * (1.0F - blend) + (float)(c2 >> 16 & 255) * blend);
         int g = (int)((float)(c1 >> 8 & 255) * (1.0F - blend) + (float)(c2 >> 8 & 255) * blend);
         int b = (int)((float)(c1 & 255) * (1.0F - blend) + (float)(c2 & 255) * blend);
         return -16777216 | r << 16 | g << 8 | b;
      }
   }

   private static void startPathRendering(List blockPath) {
      if (rendererRegistered) {
         EventManager.remove(RenderListener.class, renderListener);
      }

      renderPathPoints = new ArrayList();
      Iterator var1 = blockPath.iterator();

      while(var1.hasNext()) {
         BlockPos bp = (BlockPos)var1.next();
         renderPathPoints.add(new Vec3((double)bp.m_123341_() + 0.5, (double)bp.m_123342_() + 0.2, (double)bp.m_123343_() + 0.5));
      }

      EventManager.add(RenderListener.class, renderListener);
      rendererRegistered = true;
   }

   private static void stopPathRendering() {
      if (rendererRegistered) {
         EventManager.remove(RenderListener.class, renderListener);
         rendererRegistered = false;
      }

      renderPathPoints = null;
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)Commands.m_82127_("tpgotoPos").then(Commands.m_82129_("x", DoubleArgumentType.doubleArg()).then(Commands.m_82129_("y", DoubleArgumentType.doubleArg()).then(Commands.m_82129_("z", DoubleArgumentType.doubleArg()).executes((ctx) -> {
         double x = DoubleArgumentType.getDouble(ctx, "x");
         double y = DoubleArgumentType.getDouble(ctx, "y");
         double z = DoubleArgumentType.getDouble(ctx, "z");
         startTeleport(x, y, z);
         return 1;
      }))))).then(Commands.m_82127_("stop").executes((ctx) -> {
         stopWalking("已停止");
         return 1;
      })))));
   }

   private static void startTeleport(double x, double y, double z) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91073_ != null) {
         if (active) {
            sendMessage("已经有正在传送运行中 可以先使用 /lexis client tpgotoPos stop 就停止！");
         } else {
            BlockPos targetBlock = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
            targetPos = new Vec3((double)targetBlock.m_123341_() + 0.5, (double)targetBlock.m_123342_() + 0.01, (double)targetBlock.m_123343_() + 0.5);
            active = true;
            int var10000 = targetBlock.m_123341_();
            sendMessage("正在计算前往坐标 " + var10000 + ", " + targetBlock.m_123342_() + ", " + targetBlock.m_123343_() + " 的路径");
            CompletableFuture.supplyAsync(() -> {
               return computePath(mc, targetBlock);
            }).thenAccept((p) -> {
               handlePath(mc, p, "目标坐标");
            });
         }
      }
   }

   private static List computePath(Minecraft mc, BlockPos targetBlock) {
      if (mc.f_91074_ == null) {
         return null;
      } else {
         Vec3 start = mc.f_91074_.m_20182_();
         Vec3 end = new Vec3((double)targetBlock.m_123341_() + 0.5, (double)targetBlock.m_123342_() + 0.01, (double)targetBlock.m_123343_() + 0.5);

         try {
            AStarPathFinder finder = new AStarPathFinder(mc.f_91073_);
            finder.setAirPath(true);
            finder.setHClip(true);
            finder.setAttackRange(4.0);
            List raw = finder.findPath(start, end, 3.0);
            if (raw != null && raw.size() >= 2) {
               List blockPath = new ArrayList();
               Iterator var7 = raw.iterator();

               while(var7.hasNext()) {
                  Vec3 v = (Vec3)var7.next();
                  blockPath.add(BlockPos.m_274446_(v));
               }

               List simplified = new ArrayList();
               simplified.add((BlockPos)blockPath.get(0));

               for(int i = 1; i < blockPath.size(); ++i) {
                  if (!((BlockPos)simplified.get(simplified.size() - 1)).equals(blockPath.get(i))) {
                     simplified.add((BlockPos)blockPath.get(i));
                  }
               }

               return simplified;
            } else {
               return null;
            }
         } catch (Exception var9) {
            var9.printStackTrace();
            return null;
         }
      }
   }

   private static void handlePath(Minecraft mc, List p, String targetDesc) {
      if (p != null && p.size() >= 2) {
         mc.execute(() -> {
            path = p;
            pathIndex = 1;
            startPathRendering(p);
            mc.execute(() -> {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var1) {
               }

               moving = true;
               waitingForChunk = false;
               sendMessage("路径计算完成");
            });
         });
      } else {
         mc.execute(() -> {
            sendMessage("无法找到路径");
            active = false;
            stopPathRendering();
         });
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         if (active && moving) {
            if (targetPos == null) {
               stopWalking("目标无效");
            } else if (path != null && pathIndex < path.size()) {
               if (mc.f_91074_.m_20182_().m_82554_(targetPos) <= 1.5) {
                  stopWalking("已经到位置了");
               } else {
                  BlockPos destBlock = (BlockPos)path.get(pathIndex);
                  if (!mc.f_91073_.m_46749_(destBlock)) {
                     if (!waitingForChunk) {
                        waitingForChunk = true;
                     }

                  } else {
                     if (waitingForChunk) {
                        waitingForChunk = false;
                     }

                     long now = System.currentTimeMillis();
                     if (now - lastPacketTime >= 30L) {
                        Vec3 dest = new Vec3((double)destBlock.m_123341_() + 0.5, (double)destBlock.m_123342_() + 0.01, (double)destBlock.m_123343_() + 0.5);
                        sendTeleport(mc, dest);
                        lastPacketTime = now;
                        ++pathIndex;
                     }

                  }
               }
            } else {
               stopWalking("已经到位置了");
            }
         }
      }
   }

   private static void sendTeleport(Minecraft mc, Vec3 pos) {
      if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
         boolean onGround = mc.f_91074_.m_20096_();
         ServerboundMovePlayerPacket.Pos packet = new ServerboundMovePlayerPacket.Pos(pos.f_82479_, pos.f_82480_, pos.f_82481_, onGround);
         mc.f_91074_.f_108617_.m_104955_(packet);
         mc.f_91074_.m_6034_(pos.f_82479_, pos.f_82480_, pos.f_82481_);
      }
   }

   private static void stopWalking(String reason) {
      active = false;
      moving = false;
      path = null;
      targetPos = null;
      pathIndex = 0;
      waitingForChunk = false;
      stopPathRendering();
      if (reason != null) {
         sendMessage(reason);
      }

   }

   private static void sendMessage(String msg) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null) {
         mc.f_91074_.m_5661_(Component.m_237113_("§c[§6Lexis§c] §f" + msg), false);
      }

   }
}
