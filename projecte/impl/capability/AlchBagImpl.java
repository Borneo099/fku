package moze_intel.projecte.impl.capability;

import java.util.EnumMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.capability.managing.SerializableCapabilityResolver;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.SyncBagDataPKT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AlchBagImpl {
   public static IAlchBagProvider getDefault() {
      return new DefaultImpl();
   }

   private AlchBagImpl() {
   }

   private static class DefaultImpl implements IAlchBagProvider {
      private final Map inventories = new EnumMap(DyeColor.class);

      public @NotNull IItemHandler getBag(@NotNull DyeColor color) {
         if (!this.inventories.containsKey(color)) {
            this.inventories.put(color, new ItemStackHandler(104));
         }

         return (IItemHandler)this.inventories.get(color);
      }

      public void sync(@Nullable DyeColor color, @NotNull ServerPlayer player) {
         PacketHandler.sendTo(new SyncBagDataPKT(this.writeNBT(color)), player);
      }

      private CompoundTag writeNBT(DyeColor color) {
         CompoundTag ret = new CompoundTag();
         DyeColor[] colors = color == null ? DyeColor.values() : new DyeColor[]{color};
         DyeColor[] var4 = colors;
         int var5 = colors.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            DyeColor c = var4[var6];
            if (this.inventories.containsKey(c)) {
               ret.m_128365_(c.m_7912_(), ((ItemStackHandler)this.inventories.get(c)).serializeNBT());
            }
         }

         return ret;
      }

      public CompoundTag serializeNBT() {
         return this.writeNBT((DyeColor)null);
      }

      public void deserializeNBT(CompoundTag nbt) {
         DyeColor[] var2 = DyeColor.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            DyeColor e = var2[var4];
            if (nbt.m_128441_(e.m_7912_())) {
               ItemStackHandler inv = new ItemStackHandler(104);
               inv.deserializeNBT(nbt.m_128469_(e.m_7912_()));
               this.inventories.put(e, inv);
            }
         }

      }
   }

   public static class Provider extends SerializableCapabilityResolver {
      public static final ResourceLocation NAME = PECore.rl("alch_bags");

      public Provider() {
         super(AlchBagImpl.getDefault());
      }

      public @NotNull Capability getMatchingCapability() {
         return PECapabilities.ALCH_BAG_CAPABILITY;
      }
   }
}
