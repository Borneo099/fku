package lexis.Hack.Hacks.Items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.config.HackConfig;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FumiGeneratorLoopHack extends Hack {
   private int speed = 1;
   private boolean autoDrop = false;
   private boolean loopMode = true;
   private final Random random = new Random();
   private List allItems;
   private HackConfig config;
   private int currentSlot = 9;
   private int dropDelay = 0;
   private static final int MAX_SLOT = 35;
   private static final int MIN_SLOT = 9;

   public FumiGeneratorLoopHack() {
      super("刷物品X扔物品", "快速生成物品+扔物品", Hack.Category.ITEMS, true);
      this.addSetting(new Hack.Setting("速度", "生成数量,警告：32~1024速度可能卡死服务器，掉落物品超多+发包个十几万", 1, 1, 1024, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("自动丢弃", "生成后就自动扔物品", false));
      this.addSetting(new Hack.Setting("循环模式", "循环生成+扔", true));
      this.config = HackConfig.getInstance();
      this.loadConfig();
      this.allItems = ForgeRegistries.ITEMS.getValues().stream().toList();
   }

   private void loadConfig() {
      this.speed = this.config.getIntSetting("刷物品X扔物品", "速度", 1);
      this.autoDrop = this.config.getBooleanSetting("刷物品X扔物品", "自动丢弃", false);
      this.loopMode = this.config.getBooleanSetting("刷物品X扔物品", "循环模式", true);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "速度":
               setting.setValue(this.speed);
               break;
            case "自动丢弃":
               setting.setValue(this.autoDrop);
               break;
            case "循环模式":
               setting.setValue(this.loopMode);
         }
      }

   }

   public void onEnable() {
      if (mc.f_91074_ != null) {
         if (!mc.f_91074_.m_150110_().f_35937_) {
            NotificationManager.error("刷物品X扔物品", "你需要是创造模式！");
            this.setEnabled(false);
         } else {
            this.currentSlot = 9;
            this.dropDelay = 0;
         }
      }
   }

   public void onDisable() {
      this.currentSlot = 9;
      this.dropDelay = 0;
   }

   public void onUpdate() {
      if (mc.f_91074_ != null && mc.m_91403_() != null) {
         Iterator var1 = this.getSettings().iterator();

         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            switch (setting.getName()) {
               case "速度":
                  this.speed = setting.getInt();
                  break;
               case "自动丢弃":
                  this.autoDrop = setting.getBoolean();
                  break;
               case "循环模式":
                  this.loopMode = setting.getBoolean();
            }
         }

         if (this.loopMode) {
            for(int i = 0; i < this.speed; ++i) {
               Item randomItem = (Item)this.allItems.get(this.random.nextInt(this.allItems.size()));
               ItemStack stack = new ItemStack(randomItem, this.random.nextInt(64) + 1);
               mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(this.currentSlot, stack));
               if (this.autoDrop) {
                  mc.m_91403_().m_104955_(new ServerboundContainerClickPacket(mc.f_91074_.f_36096_.f_38840_, mc.f_91074_.f_36096_.m_182424_(), this.currentSlot, 64, ClickType.THROW, ItemStack.f_41583_, Int2ObjectMaps.singleton(this.currentSlot, ItemStack.f_41583_)));
               }

               ++this.currentSlot;
               if (this.currentSlot > 35) {
                  this.currentSlot = 9;
               }
            }

         }
      }
   }

   public void onClick() {
      this.toggle();
   }
}
