package lexis.Server.AICHAT;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AIConfig {
   public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
   public static final ForgeConfigSpec SPEC;
   public static final ForgeConfigSpec.ConfigValue ENABLED;
   public static final ForgeConfigSpec.ConfigValue COOLDOWN_SECONDS;
   public static final ForgeConfigSpec.ConfigValue MAX_RESPONSE_LENGTH;
   public static final ForgeConfigSpec.ConfigValue REQUIRE_PREFIX;
   public static final ForgeConfigSpec.ConfigValue AI_NAME;

   public static void register() {
      ModLoadingContext.get().registerConfig(Type.SERVER, SPEC);
   }

   static {
      BUILDER.push("AI Chat Settings");
      ENABLED = BUILDER.comment("是否启用AI聊天功能").define("enabled", true);
      COOLDOWN_SECONDS = BUILDER.comment("冷却时间（秒）").defineInRange("cooldown", 3, 1, 60);
      MAX_RESPONSE_LENGTH = BUILDER.comment("AI响应最大长度").defineInRange("maxResponseLength", 200, 50, 500);
      REQUIRE_PREFIX = BUILDER.comment("是否必须使用@AI或ai:前缀").define("requirePrefix", true);
      AI_NAME = BUILDER.comment("AI显示名称").define("aiName", "Lexis");
      BUILDER.pop();
      SPEC = BUILDER.build();
   }
}
