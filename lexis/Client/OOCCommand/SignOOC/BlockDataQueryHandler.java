package lexis.Client.OOCCommand.SignOOC;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;

public class BlockDataQueryHandler {
   private static final int ID_BASE = 1000000;
   private static final AtomicInteger transactionCounter = new AtomicInteger(1000000);
   private static final ConcurrentHashMap queryPositions = new ConcurrentHashMap();
   private static final ConcurrentHashMap blockData = new ConcurrentHashMap();
   private static final Set responded = ConcurrentHashMap.newKeySet();

   public static void queryBlockEntity(BlockPos pos) {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91074_ != null && mc.f_91074_.f_108617_ != null) {
         int transactionId = transactionCounter.getAndIncrement();
         queryPositions.put(transactionId, pos.m_7949_());
         mc.f_91074_.f_108617_.m_104955_(new ServerboundBlockEntityTagQuery(transactionId, pos.m_7949_()));
      }
   }

   public static void handleResponse(int transactionId, CompoundTag tag) {
      BlockPos pos = (BlockPos)queryPositions.remove(transactionId);
      if (pos != null) {
         responded.add(pos);
         if (tag != null && !tag.m_128456_()) {
            blockData.put(pos, tag.m_6426_());
         }

      }
   }

   public static CompoundTag getStoredData(BlockPos pos) {
      return (CompoundTag)blockData.get(pos);
   }

   public static boolean hasData(BlockPos pos) {
      return blockData.containsKey(pos);
   }

   public static boolean hasResponded(BlockPos pos) {
      return responded.contains(pos);
   }

   public static void clear() {
      queryPositions.clear();
      blockData.clear();
      responded.clear();
      transactionCounter.set(1000000);
   }
}
