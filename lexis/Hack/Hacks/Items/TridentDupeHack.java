package lexis.Hack.Hacks.Items;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TridentDupeHack extends Hack {
   private Phase phase;
   private int tickCounter;
   private int consecutiveFails;
   private boolean stopping;
   private float lastHealth;
   private int holdDuration;
   private int dupeDelay;
   private boolean dropTridents;
   private boolean bypassGrim;
   private boolean autoCloseOnDamage;
   private boolean autoCleanInventory;
   private int dropScanIndex;

   public TridentDupeHack() {
      super("三叉戟复制", new String[]{"三叉戟复制漏洞", "§c§l注意：Grim V3 反作弊会检测你", "你可以去在设置功能 开启绕过Grim V3检测"}, Hack.Category.ITEMS, true);
      this.phase = TridentDupeHack.Phase.IDLE;
      this.tickCounter = 0;
      this.consecutiveFails = 0;
      this.stopping = false;
      this.lastHealth = -1.0F;
      this.holdDuration = 10;
      this.dupeDelay = 5;
      this.dropTridents = true;
      this.bypassGrim = false;
      this.autoCloseOnDamage = true;
      this.autoCleanInventory = true;
      this.dropScanIndex = 0;
      this.addSetting(new Hack.Setting("蓄力时长", "蓄力持续tick数", 10, 1, 200));
      this.addSetting(new Hack.Setting("冷却时长", "每次复制后冷却tick数", 5, 1, 200));
      this.addSetting(new Hack.Setting("自动丢弃", "复制后自动丢出副本", true));
      this.addSetting(new Hack.Setting("绕过GrimV3", "欺骗Grim反作弊检测不到你", false));
      this.addSetting(new Hack.Setting("受伤自动关闭", "检测到受伤自动关闭功能，防止被怪物打死", true));
      this.addSetting(new Hack.Setting("自动清理背包", "背包三叉戟太多时自动扔掉，第一格保留", true));
      HackConfig config = HackConfig.getInstance();
      Iterator var2 = this.getSettings().iterator();

      while(var2.hasNext()) {
         Hack.Setting s = (Hack.Setting)var2.next();
         switch (s.getName()) {
            case "蓄力时长":
               s.setValue(config.getIntSetting(this.getName(), "蓄力时长", 10));
               break;
            case "冷却时长":
               s.setValue(config.getIntSetting(this.getName(), "冷却时长", 5));
               break;
            case "自动丢弃":
               s.setValue(config.getBooleanSetting(this.getName(), "自动丢弃", true));
               break;
            case "绕过GrimV3":
               s.setValue(config.getBooleanSetting(this.getName(), "绕过GrimV3", false));
               break;
            case "受伤自动关闭":
               s.setValue(config.getBooleanSetting(this.getName(), "受伤自动关闭", true));
               break;
            case "自动清理背包":
               s.setValue(config.getBooleanSetting(this.getName(), "自动清理背包", true));
         }
      }

   }

   public void onEnable() {
      if (!this.stopping) {
         this.reset();
         MinecraftForge.EVENT_BUS.register(this);
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null) {
            this.lastHealth = mc.f_91074_.m_21223_();
            DupeScreen screen = new DupeScreen();
            mc.f_91080_ = screen;
            screen.m_6575_(mc, mc.m_91268_().m_85445_(), mc.m_91268_().m_85446_());
         }

      }
   }

   public void onDisable() {
      if (!this.stopping) {
         this.stopping = true;
         MinecraftForge.EVENT_BUS.unregister(this);
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91080_ instanceof DupeScreen) {
            mc.f_91080_ = null;
         }

         this.reset();
         this.stopping = false;
      }
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting s = (Hack.Setting)var1.next();
         switch (s.getName()) {
            case "蓄力时长":
               this.holdDuration = s.getInt();
               break;
            case "冷却时长":
               this.dupeDelay = s.getInt();
               break;
            case "自动丢弃":
               this.dropTridents = s.getBoolean();
               break;
            case "绕过GrimV3":
               this.bypassGrim = s.getBoolean();
               break;
            case "受伤自动关闭":
               this.autoCloseOnDamage = s.getBoolean();
               break;
            case "自动清理背包":
               this.autoCleanInventory = s.getBoolean();
         }
      }

   }

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == net.minecraftforge.event.TickEvent.Phase.START) {
         Minecraft mc = Minecraft.m_91087_();
         if (!(mc.f_91080_ instanceof DupeScreen)) {
            if (!this.stopping) {
               this.setEnabled(false);
            }

         } else {
            this.tick();
         }
      }
   }

   private void tick() {
      Minecraft mc = Minecraft.m_91087_();
      LocalPlayer player = mc.f_91074_;
      if (player != null && mc.f_91073_ != null && mc.m_91403_() != null) {
         if (this.autoCloseOnDamage) {
            float currentHealth = player.m_21223_();
            if (this.lastHealth > 0.0F && currentHealth < this.lastHealth) {
               player.m_5661_(Component.m_237113_("[Lexis] §e[§6三叉戟复制§e] §f你有受伤！功能已被自动关闭"), false);
               this.setEnabled(false);
               return;
            }

            this.lastHealth = currentHealth;
         }

         int i;
         switch (this.phase) {
            case IDLE:
               this.tickCounter = 0;
               this.phase = TridentDupeHack.Phase.ARMING;
               break;
            case ARMING:
               i = this.findBestWeaponSlot(player);
               if (i == -1) {
                  ++this.tickCounter;
                  if (this.tickCounter >= 20) {
                     this.phase = TridentDupeHack.Phase.IDLE;
                     this.tickCounter = 0;
                  }

                  return;
               }

               if (i != 0) {
                  mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 36 + i, 0, ClickType.SWAP, player);
               }

               KeyMapping.m_90835_(mc.f_91066_.f_92095_.getKey());
               KeyMapping.m_90837_(mc.f_91066_.f_92095_.getKey(), true);
               mc.m_91403_().m_104955_(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0));
               if (this.bypassGrim) {
                  mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Pos(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.m_20096_()));
                  mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Rot(player.m_146908_() + 0.1F, player.m_146909_(), player.m_20096_()));
               }

               this.phase = TridentDupeHack.Phase.HOLDING;
               this.tickCounter = 0;
               break;
            case HOLDING:
               KeyMapping.m_90837_(mc.f_91066_.f_92095_.getKey(), true);
               ++this.tickCounter;
               if (this.tickCounter >= this.holdDuration) {
                  KeyMapping.m_90837_(mc.f_91066_.f_92095_.getKey(), false);
                  this.phase = TridentDupeHack.Phase.DUPING;
                  this.tickCounter = 0;
               }
               break;
            case DUPING:
               try {
                  if (this.bypassGrim) {
                     mc.m_91403_().m_104955_(new ServerboundMovePlayerPacket.Pos(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.m_20096_()));
                  }

                  mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 3, 0, ClickType.SWAP, player);
                  if (this.dropTridents) {
                     mc.f_91072_.m_171799_(player.f_36096_.f_38840_, 44, 0, ClickType.THROW, player);
                  }

                  mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.f_121853_, Direction.DOWN, 0));
                  this.consecutiveFails = 0;
               } catch (Exception var6) {
                  ++this.consecutiveFails;
                  if (this.consecutiveFails >= 3) {
                     player.m_5661_(Component.m_237113_("[Lexis] §6[§e三叉戟复制§6] §f失败过多次了 服务器可能是有插件修了？还是via版本吗？"), false);
                     this.setEnabled(false);
                     return;
                  }
               }

               this.phase = TridentDupeHack.Phase.COOLDOWN;
               this.tickCounter = 0;
               break;
            case COOLDOWN:
               ++this.tickCounter;
               if (this.tickCounter >= this.dupeDelay) {
                  if (this.autoCleanInventory && this.countExtraTridents(player) >= 3) {
                     this.phase = TridentDupeHack.Phase.DROPPING;
                     this.tickCounter = 0;
                     this.dropScanIndex = -1;
                     HeadOnlyLook.startRotation(player.m_146908_() + 180.0F, -15.0F, 0L);
                  } else {
                     this.phase = TridentDupeHack.Phase.IDLE;
                     this.tickCounter = 0;
                  }
               }
               break;
            case DROPPING:
               if (this.dropScanIndex == -1) {
                  if (HeadOnlyLook.hasReachedTarget()) {
                     this.dropScanIndex = 0;
                  }
               } else if (this.dropScanIndex == -2) {
                  if (!HeadOnlyLook.isLooking()) {
                     this.phase = TridentDupeHack.Phase.IDLE;
                     this.tickCounter = 0;
                  }
               } else if (this.bypassGrim) {
                  i = this.findNextTridentSlot(player, this.dropScanIndex);
                  if (i == -1) {
                     HeadOnlyLook.stopLooking();
                     this.dropScanIndex = -2;
                  } else {
                     int containerSlot = i < 9 ? 36 + i : i;
                     mc.f_91072_.m_171799_(player.f_36096_.f_38840_, containerSlot, 1, ClickType.THROW, player);
                     this.dropScanIndex = i + 1;
                  }
               } else {
                  for(i = 1; i < 36; ++i) {
                     ItemStack stack = player.m_150109_().m_8020_(i);
                     if (!stack.m_41619_() && stack.m_41720_() instanceof TridentItem) {
                        int containerSlot = i < 9 ? 36 + i : i;
                        mc.f_91072_.m_171799_(player.f_36096_.f_38840_, containerSlot, 1, ClickType.THROW, player);
                     }
                  }

                  HeadOnlyLook.stopLooking();
                  this.dropScanIndex = -2;
               }
         }

      }
   }

   private int findBestWeaponSlot(LocalPlayer player) {
      int bestSlot = -1;
      int bestDurability = Integer.MAX_VALUE;
      boolean hasRiptideWarned = false;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (!stack.m_41619_() && stack.m_41720_() instanceof TridentItem) {
            int riptide = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.f_44957_, stack);
            if (riptide > 0) {
               if (!hasRiptideWarned) {
                  hasRiptideWarned = true;
                  player.m_5661_(Component.m_237113_("[Lexis] §6[§e三叉戟复制§6] §f这看起来有激流附魔！无法！"), false);
               }
            } else {
               int dur = stack.m_41776_() - stack.m_41773_();
               if (dur < bestDurability) {
                  bestDurability = dur;
                  bestSlot = i;
               }
            }
         }
      }

      return bestSlot;
   }

   private int countExtraTridents(LocalPlayer player) {
      int count = 0;

      for(int i = 1; i < 36; ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (!stack.m_41619_() && stack.m_41720_() instanceof TridentItem) {
            ++count;
         }
      }

      return count;
   }

   private int findNextTridentSlot(LocalPlayer player, int startIndex) {
      for(int i = Math.max(1, startIndex); i < 36; ++i) {
         ItemStack stack = player.m_150109_().m_8020_(i);
         if (!stack.m_41619_() && stack.m_41720_() instanceof TridentItem) {
            return i;
         }
      }

      return -1;
   }

   private void reset() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91066_ != null) {
         KeyMapping.m_90837_(mc.f_91066_.f_92095_.getKey(), false);
      }

      if (HeadOnlyLook.isLooking()) {
         HeadOnlyLook.forceStop();
      }

      this.phase = TridentDupeHack.Phase.IDLE;
      this.tickCounter = 0;
      this.consecutiveFails = 0;
      this.lastHealth = -1.0F;
      this.dropScanIndex = 0;
   }

   public void onClick() {
      this.toggle();
   }

   private static enum Phase {
      IDLE,
      ARMING,
      HOLDING,
      DUPING,
      COOLDOWN,
      DROPPING;

      // $FF: synthetic method
      private static Phase[] $values() {
         return new Phase[]{IDLE, ARMING, HOLDING, DUPING, COOLDOWN, DROPPING};
      }
   }

   private class DupeScreen extends Screen {
      protected DupeScreen() {
         super(Component.m_237113_("三叉戟复制"));
      }

      public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
         if (this.f_96541_ != null && this.f_96547_ != null) {
            this.m_280273_(graphics);
            graphics.m_280137_(this.f_96547_, "§6正在自动快速复制三叉戟中...", this.f_96543_ / 2, this.f_96544_ / 2 - 20, 16777215);
            graphics.m_280137_(this.f_96547_, "§7按 Esc 关闭功能并退出", this.f_96543_ / 2, this.f_96544_ / 2 + 5, 11184810);
            super.m_88315_(graphics, mouseX, mouseY, partialTick);
         }
      }

      public void m_7379_() {
         if (!TridentDupeHack.this.stopping) {
            TridentDupeHack.this.setEnabled(false);
         }

      }

      public boolean m_7043_() {
         return false;
      }
   }
}
