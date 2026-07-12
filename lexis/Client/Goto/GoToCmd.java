package lexis.Client.Goto;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public class GoToCmd implements UpdateListener, RenderListener {
   private static final Minecraft MC = Minecraft.m_91087_();
   private static PathFinder pathFinder;
   private static PathProcessor processor;
   private static boolean enabled;
   private static BlockPos lastGoal;
   private static GoToCmd instance;
   private static boolean renderPath = true;
   private static boolean isTrackingPlayer = false;
   private static String trackedPlayerName = "";
   private static BlockPos lastPlayerPos = null;
   private static int playerMoveTicks = 0;
   private static final double REACH_PLAYER_DISTANCE = 2.0;
   private static final SuggestionProvider PLAYER_SUGGESTIONS = (context, builder) -> {
      List suggests = new ArrayList();
      if (MC.m_91403_() != null) {
         Iterator var3 = MC.m_91403_().m_246170_().iterator();

         while(var3.hasNext()) {
            PlayerInfo player = (PlayerInfo)var3.next();
            suggests.add(player.m_105312_().getName());
         }
      }

      return SharedSuggestionProvider.m_82970_(suggests, builder);
   };

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("goto").then(Commands.m_82127_("Pos").then(Commands.m_82129_("x", StringArgumentType.word()).then(Commands.m_82129_("y", StringArgumentType.word()).then(Commands.m_82129_("z", StringArgumentType.word()).executes(GoToCmd::executeCoord)))))).then(Commands.m_82127_("Player").then(Commands.m_82129_("name", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS).executes(GoToCmd::executePlayer)))).then(Commands.m_82127_("stop").executes((context) -> {
         stop();
         return 1;
      })))));
   }

   private static int executeCoord(CommandContext context) {
      String xStr = StringArgumentType.getString(context, "x");
      String yStr = StringArgumentType.getString(context, "y");
      String zStr = StringArgumentType.getString(context, "z");

      try {
         if (enabled) {
            stop();
         }

         BlockPos goal = parseXYZPos(xStr, yStr, zStr);
         if (goal == null) {
            ChatUtils.error("坐标格式错误");
            return 0;
         }

         lastGoal = goal;
         isTrackingPlayer = false;
         pathFinder = new PathFinder(goal);
         startPathFinding();
         ChatUtils.message("§a开始找路到坐标: " + formatPos(goal));
      } catch (Exception var5) {
         ChatUtils.error("坐标格式错误");
      }

      return 1;
   }

   private static int executePlayer(CommandContext context) {
      String playerName = StringArgumentType.getString(context, "name");

      try {
         if (enabled) {
            stop();
         }

         if (MC.f_91073_ == null || MC.f_91074_ == null) {
            ChatUtils.error("无法获取世界");
            return 0;
         }

         LivingEntity entity = (LivingEntity)StreamSupport.stream(MC.f_91073_.m_104735_().spliterator(), false).filter((e) -> {
            return e instanceof LivingEntity;
         }).map((e) -> {
            return (LivingEntity)e;
         }).filter((e) -> {
            return !e.m_21224_() && e.m_21223_() > 0.0F;
         }).filter((e) -> {
            return e != MC.f_91074_;
         }).filter((e) -> {
            return !(e instanceof FakePlayerEntity);
         }).filter((e) -> {
            return playerName.equalsIgnoreCase(e.m_7755_().getString());
         }).findFirst().orElse((Object)null);
         if (entity == null) {
            ChatUtils.error("找不到玩家: " + playerName);
            return 0;
         }

         isTrackingPlayer = true;
         trackedPlayerName = playerName;
         lastPlayerPos = BlockPos.m_274446_(entity.m_20182_());
         playerMoveTicks = 0;
         pathFinder = new PathFinder(lastPlayerPos);
         startPathFinding();
         ChatUtils.message("§a开始跟玩家: " + playerName);
      } catch (Exception var3) {
         ChatUtils.error("跟玩家失败");
      }

      return 1;
   }

   private static void startPathFinding() {
      enabled = true;
      renderPath = true;
      if (instance == null) {
         instance = new GoToCmd();
      }

      EventBus.add(UpdateListener.class, instance);
      EventBus.add(RenderListener.class, instance);
   }

   private static void stop() {
      if (enabled) {
         disable();
         ChatUtils.message("§c已停止");
      }

   }

   private static BlockPos parseXYZPos(String xStr, String yStr, String zStr) {
      if (MC.f_91074_ == null) {
         return null;
      } else {
         BlockPos playerPos = BlockPos.m_274446_(MC.f_91074_.m_20182_());

         int x;
         int y;
         int z;
         try {
            x = parseCoordinate(xStr, playerPos.m_123341_());
            y = parseCoordinate(yStr, playerPos.m_123342_());
            z = parseCoordinate(zStr, playerPos.m_123343_());
         } catch (Exception var8) {
            return null;
         }

         return new BlockPos(x, y, z);
      }
   }

   private static int parseCoordinate(String str, int playerCoord) {
      if (str.equals("~")) {
         return playerCoord;
      } else {
         return str.startsWith("~") ? playerCoord + Integer.parseInt(str.substring(1)) : Integer.parseInt(str);
      }
   }

   private static String formatPos(BlockPos pos) {
      int var10000 = pos.m_123341_();
      return "" + var10000 + " " + pos.m_123342_() + " " + pos.m_123343_();
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         EventBus.fire(UpdateEvent.INSTANCE);
      }

   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         EventBus.fire(new RenderEvent(event.getPoseStack(), event.getPartialTick()));
      }

   }

   public void onUpdate() {
      if (enabled && pathFinder != null && MC.f_91074_ != null) {
         if (isTrackingPlayer) {
            this.updateTrackingMode();
         } else {
            this.updateNormalMode();
         }
      }
   }

   private void updateTrackingMode() {
      ++playerMoveTicks;
      if (playerMoveTicks >= 5) {
         playerMoveTicks = 0;

         try {
            LivingEntity entity = (LivingEntity)StreamSupport.stream(MC.f_91073_.m_104735_().spliterator(), false).filter((e) -> {
               return e instanceof LivingEntity;
            }).map((e) -> {
               return (LivingEntity)e;
            }).filter((e) -> {
               return !e.m_21224_() && e.m_21223_() > 0.0F;
            }).filter((e) -> {
               return e != MC.f_91074_;
            }).filter((e) -> {
               return !(e instanceof FakePlayerEntity);
            }).filter((e) -> {
               return trackedPlayerName.equalsIgnoreCase(e.m_7755_().getString());
            }).findFirst().orElse((Object)null);
            if (entity != null) {
               double distanceToPlayer = MC.f_91074_.m_20280_(entity);
               if (distanceToPlayer < 4.0) {
                  ChatUtils.message("§a已到达玩家 " + trackedPlayerName);
                  disable();
                  return;
               }

               BlockPos newPos = BlockPos.m_274446_(entity.m_20182_());
               if (!newPos.equals(lastPlayerPos)) {
                  lastPlayerPos = newPos;
                  pathFinder = new PathFinder(newPos);
                  processor = null;
               }
            } else {
               ChatUtils.error("玩家 " + trackedPlayerName + " 消失");
               disable();
            }
         } catch (Exception var5) {
            disable();
         }
      }

      if (!pathFinder.isDone()) {
         PathProcessor.lockControls();
         pathFinder.think();
         if (!pathFinder.isDone()) {
            if (pathFinder.isFailed()) {
               ChatUtils.error("无法找到路径");
               disable();
            }

            return;
         }

         pathFinder.formatPath();
         processor = pathFinder.getProcessor();
      }

      if (processor != null) {
         processor.process();
      }

   }

   private void updateNormalMode() {
      if (!pathFinder.isDone()) {
         PathProcessor.lockControls();
         pathFinder.think();
         if (!pathFinder.isDone()) {
            if (pathFinder.isFailed()) {
               ChatUtils.error("无法找到路径");
               disable();
            }

            return;
         }

         pathFinder.formatPath();
         processor = pathFinder.getProcessor();
      }

      if (processor != null) {
         processor.process();
         if (processor.isDone()) {
            ChatUtils.message("§a已到达目标位置");
            disable();
         }
      }

   }

   public void onRender(PoseStack poseStack, float partialTick) {
      if (enabled && pathFinder != null && renderPath) {
         List path = pathFinder.getPath();
         if (path != null && !path.isEmpty()) {
            PathRenderer.renderPath(poseStack, path, false, isTrackingPlayer);
         }
      }

   }

   private static void disable() {
      if (instance != null) {
         EventBus.remove(UpdateListener.class, instance);
         EventBus.remove(RenderListener.class, instance);
      }

      if (MC.f_91066_ != null) {
         if (MC.f_91066_.f_92089_ != null) {
            MC.f_91066_.f_92089_.m_7249_(false);
         }

         if (MC.f_91066_.f_92085_ != null) {
            MC.f_91066_.f_92085_.m_7249_(false);
         }
      }

      PathProcessor.releaseControls();
      pathFinder = null;
      processor = null;
      enabled = false;
      isTrackingPlayer = false;
      trackedPlayerName = "";
      lastPlayerPos = null;
   }
}
