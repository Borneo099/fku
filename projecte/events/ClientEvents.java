package moze_intel.projecte.events;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.sound.MovingSoundSWRG;
import moze_intel.projecte.network.commands.client.DumpMissingEmc;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "projecte",
   value = {Dist.CLIENT}
)
public class ClientEvents {
   @SubscribeEvent
   public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
      Minecraft mc = Minecraft.m_91087_();
      Entity var3 = event.getEntity();
      if (var3 instanceof EntitySWRGProjectile projectile) {
         if (mc.f_91067_.m_91600_()) {
            mc.m_91106_().m_120367_(new MovingSoundSWRG(projectile, event.getLevel().m_213780_()));
         }
      }

   }

   @SubscribeEvent
   public static void registerClientCommands(RegisterClientCommandsEvent event) {
      CommandBuildContext context = event.getBuildContext();
      event.getDispatcher().register((LiteralArgumentBuilder)Commands.m_82127_("projecte").then(DumpMissingEmc.register(context)));
   }
}
