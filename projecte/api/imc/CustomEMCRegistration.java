package moze_intel.projecte.api.imc;

import moze_intel.projecte.api.nss.NormalizedSimpleStack;

public record CustomEMCRegistration(NormalizedSimpleStack stack, long value) {
   public CustomEMCRegistration(NormalizedSimpleStack stack, long value) {
      this.stack = stack;
      this.value = value;
   }

   public NormalizedSimpleStack stack() {
      return this.stack;
   }

   public long value() {
      return this.value;
   }
}
