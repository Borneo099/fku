package moze_intel.projecte.shaded.org.apache.commons.math3.fraction;

import java.io.Serializable;
import moze_intel.projecte.shaded.org.apache.commons.math3.Field;

public class BigFractionField implements Field, Serializable {
   private static final long serialVersionUID = -1699294557189741703L;

   private BigFractionField() {
   }

   public static BigFractionField getInstance() {
      return BigFractionField.LazyHolder.INSTANCE;
   }

   public BigFraction getOne() {
      return BigFraction.ONE;
   }

   public BigFraction getZero() {
      return BigFraction.ZERO;
   }

   public Class getRuntimeClass() {
      return BigFraction.class;
   }

   private Object readResolve() {
      return BigFractionField.LazyHolder.INSTANCE;
   }

   // $FF: synthetic method
   BigFractionField(Object x0) {
      this();
   }

   private static class LazyHolder {
      private static final BigFractionField INSTANCE = new BigFractionField();
   }
}
