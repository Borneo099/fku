package lexis.Client.Commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ClientJumpCommand {
   private static final Minecraft mc = Minecraft.m_91087_();
   private static final String PREFIX = "§d[§6Lexis§d] §f";

   @SubscribeEvent
   public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("jump").executes(ClientJumpCommand::jump))));
   }

   private static int jump(CommandContext context) {
      if (mc.f_91074_ == null) {
         ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f§f只有游戏内玩家才能使用此指令"));
         return 0;
      } else {
         boolean isOnGround = mc.f_91074_.m_20096_();
         boolean isInWater = mc.f_91074_.m_20069_();
         boolean isCrawling = mc.f_91074_.m_20089_() == Pose.SWIMMING && mc.f_91074_.m_6047_();
         if (isOnGround && !isInWater && !isCrawling) {
            mc.f_91074_.m_6135_();
            return 1;
         } else {
            ((CommandSourceStack)context.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f§f你现在不能跳跃(需要站在地面上+不在液体中)"));
            return 0;
         }
      }
   }
}
