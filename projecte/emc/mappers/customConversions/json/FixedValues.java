package moze_intel.projecte.emc.mappers.customConversions.json;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixedValues {
   @SerializedName("before")
   public Map setValueBefore = new HashMap();
   @SerializedName("after")
   public Map setValueAfter = new HashMap();
   public List conversion = new ArrayList();

   public void merge(FixedValues other) {
      this.setValueBefore.putAll(other.setValueBefore);
      this.setValueAfter.putAll(other.setValueAfter);
      this.conversion.addAll(other.conversion);
   }
}
