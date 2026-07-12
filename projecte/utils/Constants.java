package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.text.NumberFormat;

public final class Constants {
   public static final NumberFormat EMC_FORMATTER = getFormatter();
   public static final BigInteger MAX_EXACT_TRANSMUTATION_DISPLAY = BigInteger.valueOf(1000000000000L);
   public static final BigInteger MAX_INTEGER = BigInteger.valueOf(2147483647L);
   public static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
   public static final long[] MAX_KLEIN_EMC = new long[]{50000L, 200000L, 800000L, 3200000L, 12800000L, 51200000L};
   public static final float[] EXPLOSIVE_LENS_RADIUS = new float[]{4.0F, 8.0F, 12.0F, 16.0F, 16.0F, 16.0F, 16.0F, 16.0F};
   public static final long[] EXPLOSIVE_LENS_COST = new long[]{384L, 768L, 1536L, 2304L, 2304L, 2304L, 2304L, 2304L};
   public static final long FREE_ARITHMETIC_VALUE = Long.MIN_VALUE;
   public static final long BLOCK_ENTITY_MAX_EMC = Long.MAX_VALUE;
   public static final int MAX_CONDENSER_PROGRESS = 102;
   public static final int MAX_VEIN_SIZE = 250;
   public static final String NBT_KEY_STORED_EMC = "StoredEMC";
   public static final String NBT_KEY_GEM_WHITELIST = "Whitelist";
   public static final String NBT_KEY_COOLDOWN = "Cooldown";
   public static final String NBT_KEY_ACTIVE = "Active";
   public static final String NBT_KEY_MODE = "Mode";
   public static final String NBT_KEY_STEP_ASSIST = "StepAssist";
   public static final String NBT_KEY_NIGHT_VISION = "NightVision";
   public static final String NBT_KEY_UNPROCESSED_EMC = "UnprocessedEMC";
   public static final String NBT_KEY_GEM_CONSUMED = "Consumed";
   public static final String NBT_KEY_GEM_ITEMS = "Items";
   public static final String NBT_KEY_TIME_MODE = "TimeMode";
   public static final String NBT_KEY_STORED_XP = "StoredXP";

   private static NumberFormat getFormatter() {
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(1);
      return format;
   }
}
