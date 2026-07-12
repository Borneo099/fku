package moze_intel.projecte.api.nss;

import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NSSCreator {
   @NotNull NormalizedSimpleStack create(String var1) throws JsonParseException;
}
