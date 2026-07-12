package moze_intel.projecte.api.imc;

import moze_intel.projecte.api.nss.NSSCreator;

public record NSSCreatorInfo(String key, NSSCreator creator) {
   public NSSCreatorInfo(String key, NSSCreator creator) {
      this.key = key;
      this.creator = creator;
   }

   public String key() {
      return this.key;
   }

   public NSSCreator creator() {
      return this.creator;
   }
}
