package lexis.Client.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Iterator;
import lexis.item.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class TpPlayerEggCommand {
   private static final SuggestionProvider ONLINE_PLAYER_SUGGESTIONS = (ctx, builder) -> {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
         Iterator var3 = mc.f_91074_.f_108617_.m_246170_().iterator();

         while(var3.hasNext()) {
            PlayerInfo playerInfo = (PlayerInfo)var3.next();
            builder.suggest(playerInfo.m_105312_().getName());
         }
      }

      return builder.buildFuture();
   };

   @SubscribeEvent
   public static void onRegisterCommands(RegisterClientCommandsEvent event) {
      CommandDispatcher dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_("lexis").then(Commands.m_82127_("client").then(Commands.m_82127_("tpPlayerEgg").then(Commands.m_82129_("player", StringArgumentType.word()).suggests(ONLINE_PLAYER_SUGGESTIONS).executes(TpPlayerEggCommand::giveTpEgg)))));
   }

   private static int giveTpEgg(CommandContext ctx) {
      String playerName = StringArgumentType.getString(ctx, "player");
      Minecraft mc = Minecraft.m_91087_();
      if (!mc.f_91074_.m_7500_()) {
         ((CommandSourceStack)ctx.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f仅创造模式可用"));
         return 0;
      } else {
         PlayerInfo targetInfo = null;
         Iterator var4 = mc.f_91074_.f_108617_.m_246170_().iterator();

         while(var4.hasNext()) {
            PlayerInfo info = (PlayerInfo)var4.next();
            if (info.m_105312_().getName().equalsIgnoreCase(playerName)) {
               targetInfo = info;
               break;
            }
         }

         if (targetInfo == null) {
            ((CommandSourceStack)ctx.getSource()).m_81352_(Component.m_237113_("§d[§6Lexis§d] §f玩家不存在: " + playerName));
            return 0;
         } else {
            ItemStack egg = createTpEgg(targetInfo);
            boolean added = Utils.addItem(egg);
            if (added) {
               ((CommandSourceStack)ctx.getSource()).m_288197_(() -> {
                  return Component.m_237113_("§d[§6Lexis§d] §f已取得 " + playerName + " 的传送蛋");
               }, false);
            }

            return 1;
         }
      }
   }

   private static ItemStack createTpEgg(PlayerInfo target) {
      ItemStack egg = new ItemStack(Items.f_42601_);
      CompoundTag tag = egg.m_41784_();
      ListTag enchantments = new ListTag();
      tag.m_128365_("Enchantments", enchantments);
      CompoundTag entityTag = new CompoundTag();
      ListTag motion = new ListTag();
      motion.add(DoubleTag.m_128500_(0.0));
      motion.add(DoubleTag.m_128500_(-10.0));
      motion.add(DoubleTag.m_128500_(0.0));
      entityTag.m_128365_("Motion", motion);
      entityTag.m_128362_("Owner", target.m_105312_().getId());
      entityTag.m_128359_("id", "minecraft:ender_pearl");
      tag.m_128365_("EntityTag", entityTag);
      CompoundTag display = new CompoundTag();
      String nameJson = "{\"text\":\"§d§l" + target.m_105312_().getName() + " §6§lof §4§lTpEgg\"}";
      display.m_128359_("Name", nameJson);
      tag.m_128365_("display", display);
      egg.m_41751_(tag);
      egg.m_41764_(1);
      return egg;
   }
}
