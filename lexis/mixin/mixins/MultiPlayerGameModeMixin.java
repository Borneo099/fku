package lexis.mixin.mixins;

import java.util.Iterator;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Items.HeadlessPistonHack;
import lexis.Hack.Hackutil.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
   private final Minecraft mc = Minecraft.m_91087_();
   private ItemStack originalStack;
   private int originalSlot;
   private boolean isHeadlessActive;

   public MultiPlayerGameModeMixin() {
      this.originalStack = ItemStack.f_41583_;
      this.originalSlot = -1;
      this.isHeadlessActive = false;
   }

   @Inject(
      method = {"useItemOn"},
      at = {@At("HEAD")}
   )
   private void onUseItemOnHead(LocalPlayer p_233733_, InteractionHand p_233734_, BlockHitResult p_233735_, CallbackInfoReturnable cir) {
      Iterator var6 = HackManager.getInstance().getHacks().iterator();

      while(var6.hasNext()) {
         Hack h = (Hack)var6.next();
         if (h instanceof HeadlessPistonHack hack && h.isEnabled()) {
            break;
         }
      }

      if (hack != null && this.mc.f_91074_.m_7500_()) {
         this.originalSlot = this.mc.f_91074_.m_150109_().f_35977_;
         this.originalStack = this.mc.f_91074_.m_150109_().m_8020_(this.originalSlot).m_41777_();
         ItemStack headlessPiston = new ItemStack(Items.f_41869_);
         CompoundTag tag = headlessPiston.m_41784_();
         CompoundTag blockStateTag = new CompoundTag();
         blockStateTag.m_128359_("extended", "true");
         tag.m_128365_("BlockStateTag", blockStateTag);
         this.mc.f_91074_.m_150109_().m_6836_(this.originalSlot, headlessPiston);
         int packetSlot = this.originalSlot < 9 ? this.originalSlot + 36 : this.originalSlot;
         this.mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(packetSlot, headlessPiston));
         this.isHeadlessActive = true;
      }
   }

   @Inject(
      method = {"useItemOn"},
      at = {@At("RETURN")}
   )
   private void onUseItemOnReturn(CallbackInfoReturnable cir) {
      if (this.isHeadlessActive) {
         this.mc.f_91074_.m_150109_().m_6836_(this.originalSlot, this.originalStack);
         int packetSlot = this.originalSlot < 9 ? this.originalSlot + 36 : this.originalSlot;
         this.mc.m_91403_().m_104955_(new ServerboundSetCreativeModeSlotPacket(packetSlot, this.originalStack));
         this.originalStack = ItemStack.f_41583_;
         this.originalSlot = -1;
         this.isHeadlessActive = false;
      }
   }
}
