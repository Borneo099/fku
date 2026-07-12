package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.io.Serializable;
import java.util.Arrays;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathIllegalStateException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathInternalError;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NotStrictlyPositiveException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NullArgumentException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.NumberIsTooSmallException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.util.LocalizedFormats;

public class ResizableDoubleArray implements DoubleArray, Serializable {
   /** @deprecated */
   @Deprecated
   public static final int ADDITIVE_MODE = 1;
   /** @deprecated */
   @Deprecated
   public static final int MULTIPLICATIVE_MODE = 0;
   private static final long serialVersionUID = -3485529955529426875L;
   private static final int DEFAULT_INITIAL_CAPACITY = 16;
   private static final double DEFAULT_EXPANSION_FACTOR = 2.0;
   private static final double DEFAULT_CONTRACTION_DELTA = 0.5;
   private double contractionCriterion;
   private double expansionFactor;
   private ExpansionMode expansionMode;
   private double[] internalArray;
   private int numElements;
   private int startIndex;

   public ResizableDoubleArray() {
      this(16);
   }

   public ResizableDoubleArray(int initialCapacity) throws MathIllegalArgumentException {
      this(initialCapacity, 2.0);
   }

   public ResizableDoubleArray(double[] initialArray) {
      this(16, 2.0, 2.5, ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE, initialArray);
   }

   /** @deprecated */
   @Deprecated
   public ResizableDoubleArray(int initialCapacity, float expansionFactor) throws MathIllegalArgumentException {
      this(initialCapacity, (double)expansionFactor);
   }

   public ResizableDoubleArray(int initialCapacity, double expansionFactor) throws MathIllegalArgumentException {
      this(initialCapacity, expansionFactor, 0.5 + expansionFactor);
   }

   /** @deprecated */
   @Deprecated
   public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria) throws MathIllegalArgumentException {
      this(initialCapacity, (double)expansionFactor, (double)contractionCriteria);
   }

   public ResizableDoubleArray(int initialCapacity, double expansionFactor, double contractionCriterion) throws MathIllegalArgumentException {
      this(initialCapacity, expansionFactor, contractionCriterion, ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE, (double[])null);
   }

   /** @deprecated */
   @Deprecated
   public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria, int expansionMode) throws MathIllegalArgumentException {
      this(initialCapacity, (double)expansionFactor, (double)contractionCriteria, expansionMode == 1 ? ResizableDoubleArray.ExpansionMode.ADDITIVE : ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE, (double[])null);
      this.setExpansionMode(expansionMode);
   }

   public ResizableDoubleArray(int initialCapacity, double expansionFactor, double contractionCriterion, ExpansionMode expansionMode, double... data) throws MathIllegalArgumentException {
      this.contractionCriterion = 2.5;
      this.expansionFactor = 2.0;
      this.expansionMode = ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE;
      this.numElements = 0;
      this.startIndex = 0;
      if (initialCapacity <= 0) {
         throw new NotStrictlyPositiveException(LocalizedFormats.INITIAL_CAPACITY_NOT_POSITIVE, initialCapacity);
      } else {
         this.checkContractExpand(contractionCriterion, expansionFactor);
         this.expansionFactor = expansionFactor;
         this.contractionCriterion = contractionCriterion;
         this.expansionMode = expansionMode;
         this.internalArray = new double[initialCapacity];
         this.numElements = 0;
         this.startIndex = 0;
         if (data != null && data.length > 0) {
            this.addElements(data);
         }

      }
   }

   public ResizableDoubleArray(ResizableDoubleArray original) throws NullArgumentException {
      this.contractionCriterion = 2.5;
      this.expansionFactor = 2.0;
      this.expansionMode = ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE;
      this.numElements = 0;
      this.startIndex = 0;
      MathUtils.checkNotNull(original);
      copy(original, this);
   }

   public synchronized void addElement(double value) {
      if (this.internalArray.length <= this.startIndex + this.numElements) {
         this.expand();
      }

      this.internalArray[this.startIndex + this.numElements++] = value;
   }

   public synchronized void addElements(double[] values) {
      double[] tempArray = new double[this.numElements + values.length + 1];
      System.arraycopy(this.internalArray, this.startIndex, tempArray, 0, this.numElements);
      System.arraycopy(values, 0, tempArray, this.numElements, values.length);
      this.internalArray = tempArray;
      this.startIndex = 0;
      this.numElements += values.length;
   }

   public synchronized double addElementRolling(double value) {
      double discarded = this.internalArray[this.startIndex];
      if (this.startIndex + this.numElements + 1 > this.internalArray.length) {
         this.expand();
      }

      ++this.startIndex;
      this.internalArray[this.startIndex + (this.numElements - 1)] = value;
      if (this.shouldContract()) {
         this.contract();
      }

      return discarded;
   }

   public synchronized double substituteMostRecentElement(double value) throws MathIllegalStateException {
      if (this.numElements < 1) {
         throw new MathIllegalStateException(LocalizedFormats.CANNOT_SUBSTITUTE_ELEMENT_FROM_EMPTY_ARRAY, new Object[0]);
      } else {
         int substIndex = this.startIndex + (this.numElements - 1);
         double discarded = this.internalArray[substIndex];
         this.internalArray[substIndex] = value;
         return discarded;
      }
   }

   /** @deprecated */
   @Deprecated
   protected void checkContractExpand(float contraction, float expansion) throws MathIllegalArgumentException {
      this.checkContractExpand((double)contraction, (double)expansion);
   }

   protected void checkContractExpand(double contraction, double expansion) throws NumberIsTooSmallException {
      NumberIsTooSmallException e;
      if (contraction < expansion) {
         e = new NumberIsTooSmallException(contraction, 1, true);
         e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_EXPANSION_FACTOR, contraction, expansion);
         throw e;
      } else if (contraction <= 1.0) {
         e = new NumberIsTooSmallException(contraction, 1, false);
         e.getContext().addMessage(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_ONE, contraction);
         throw e;
      } else if (expansion <= 1.0) {
         e = new NumberIsTooSmallException(contraction, 1, false);
         e.getContext().addMessage(LocalizedFormats.EXPANSION_FACTOR_SMALLER_THAN_ONE, expansion);
         throw e;
      }
   }

   public synchronized void clear() {
      this.numElements = 0;
      this.startIndex = 0;
   }

   public synchronized void contract() {
      double[] tempArray = new double[this.numElements + 1];
      System.arraycopy(this.internalArray, this.startIndex, tempArray, 0, this.numElements);
      this.internalArray = tempArray;
      this.startIndex = 0;
   }

   public synchronized void discardFrontElements(int i) throws MathIllegalArgumentException {
      this.discardExtremeElements(i, true);
   }

   public synchronized void discardMostRecentElements(int i) throws MathIllegalArgumentException {
      this.discardExtremeElements(i, false);
   }

   private synchronized void discardExtremeElements(int i, boolean front) throws MathIllegalArgumentException {
      if (i > this.numElements) {
         throw new MathIllegalArgumentException(LocalizedFormats.TOO_MANY_ELEMENTS_TO_DISCARD_FROM_ARRAY, new Object[]{i, this.numElements});
      } else if (i < 0) {
         throw new MathIllegalArgumentException(LocalizedFormats.CANNOT_DISCARD_NEGATIVE_NUMBER_OF_ELEMENTS, new Object[]{i});
      } else {
         this.numElements -= i;
         if (front) {
            this.startIndex += i;
         }

         if (this.shouldContract()) {
            this.contract();
         }

      }
   }

   protected synchronized void expand() {
      int newSize = false;
      int newSize;
      if (this.expansionMode == ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE) {
         newSize = (int)FastMath.ceil((double)this.internalArray.length * this.expansionFactor);
      } else {
         newSize = (int)((long)this.internalArray.length + FastMath.round(this.expansionFactor));
      }

      double[] tempArray = new double[newSize];
      System.arraycopy(this.internalArray, 0, tempArray, 0, this.internalArray.length);
      this.internalArray = tempArray;
   }

   private synchronized void expandTo(int size) {
      double[] tempArray = new double[size];
      System.arraycopy(this.internalArray, 0, tempArray, 0, this.internalArray.length);
      this.internalArray = tempArray;
   }

   /** @deprecated */
   @Deprecated
   public float getContractionCriteria() {
      return (float)this.getContractionCriterion();
   }

   public double getContractionCriterion() {
      return this.contractionCriterion;
   }

   public synchronized double getElement(int index) {
      if (index >= this.numElements) {
         throw new ArrayIndexOutOfBoundsException(index);
      } else if (index >= 0) {
         return this.internalArray[this.startIndex + index];
      } else {
         throw new ArrayIndexOutOfBoundsException(index);
      }
   }

   public synchronized double[] getElements() {
      double[] elementArray = new double[this.numElements];
      System.arraycopy(this.internalArray, this.startIndex, elementArray, 0, this.numElements);
      return elementArray;
   }

   /** @deprecated */
   @Deprecated
   public float getExpansionFactor() {
      return (float)this.expansionFactor;
   }

   /** @deprecated */
   @Deprecated
   public int getExpansionMode() {
      synchronized(this) {
         switch (this.expansionMode) {
            case MULTIPLICATIVE:
               return 0;
            case ADDITIVE:
               return 1;
            default:
               throw new MathInternalError();
         }
      }
   }

   /** @deprecated */
   @Deprecated
   synchronized int getInternalLength() {
      return this.internalArray.length;
   }

   public int getCapacity() {
      return this.internalArray.length;
   }

   public synchronized int getNumElements() {
      return this.numElements;
   }

   /** @deprecated */
   @Deprecated
   public synchronized double[] getInternalValues() {
      return this.internalArray;
   }

   protected double[] getArrayRef() {
      return this.internalArray;
   }

   protected int getStartIndex() {
      return this.startIndex;
   }

   /** @deprecated */
   @Deprecated
   public void setContractionCriteria(float contractionCriteria) throws MathIllegalArgumentException {
      this.checkContractExpand(contractionCriteria, this.getExpansionFactor());
      synchronized(this) {
         this.contractionCriterion = (double)contractionCriteria;
      }
   }

   public double compute(MathArrays.Function f) {
      double[] array;
      int start;
      int num;
      synchronized(this) {
         array = this.internalArray;
         start = this.startIndex;
         num = this.numElements;
      }

      return f.evaluate(array, start, num);
   }

   public synchronized void setElement(int index, double value) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException(index);
      } else {
         if (index + 1 > this.numElements) {
            this.numElements = index + 1;
         }

         if (this.startIndex + index >= this.internalArray.length) {
            this.expandTo(this.startIndex + index + 1);
         }

         this.internalArray[this.startIndex + index] = value;
      }
   }

   /** @deprecated */
   @Deprecated
   public void setExpansionFactor(float expansionFactor) throws MathIllegalArgumentException {
      this.checkContractExpand(this.getContractionCriterion(), (double)expansionFactor);
      synchronized(this) {
         this.expansionFactor = (double)expansionFactor;
      }
   }

   /** @deprecated */
   @Deprecated
   public void setExpansionMode(int expansionMode) throws MathIllegalArgumentException {
      if (expansionMode != 0 && expansionMode != 1) {
         throw new MathIllegalArgumentException(LocalizedFormats.UNSUPPORTED_EXPANSION_MODE, new Object[]{expansionMode, 0, "MULTIPLICATIVE_MODE", 1, "ADDITIVE_MODE"});
      } else {
         synchronized(this) {
            if (expansionMode == 0) {
               this.setExpansionMode(ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE);
            } else if (expansionMode == 1) {
               this.setExpansionMode(ResizableDoubleArray.ExpansionMode.ADDITIVE);
            }

         }
      }
   }

   /** @deprecated */
   @Deprecated
   public void setExpansionMode(ExpansionMode expansionMode) {
      synchronized(this) {
         this.expansionMode = expansionMode;
      }
   }

   /** @deprecated */
   @Deprecated
   protected void setInitialCapacity(int initialCapacity) throws MathIllegalArgumentException {
   }

   public synchronized void setNumElements(int i) throws MathIllegalArgumentException {
      if (i < 0) {
         throw new MathIllegalArgumentException(LocalizedFormats.INDEX_NOT_POSITIVE, new Object[]{i});
      } else {
         int newSize = this.startIndex + i;
         if (newSize > this.internalArray.length) {
            this.expandTo(newSize);
         }

         this.numElements = i;
      }
   }

   private synchronized boolean shouldContract() {
      if (this.expansionMode == ResizableDoubleArray.ExpansionMode.MULTIPLICATIVE) {
         return (double)((float)this.internalArray.length / (float)this.numElements) > this.contractionCriterion;
      } else {
         return (double)(this.internalArray.length - this.numElements) > this.contractionCriterion;
      }
   }

   /** @deprecated */
   @Deprecated
   public synchronized int start() {
      return this.startIndex;
   }

   public static void copy(ResizableDoubleArray source, ResizableDoubleArray dest) throws NullArgumentException {
      MathUtils.checkNotNull(source);
      MathUtils.checkNotNull(dest);
      synchronized(source) {
         synchronized(dest) {
            dest.contractionCriterion = source.contractionCriterion;
            dest.expansionFactor = source.expansionFactor;
            dest.expansionMode = source.expansionMode;
            dest.internalArray = new double[source.internalArray.length];
            System.arraycopy(source.internalArray, 0, dest.internalArray, 0, dest.internalArray.length);
            dest.numElements = source.numElements;
            dest.startIndex = source.startIndex;
         }

      }
   }

   public synchronized ResizableDoubleArray copy() {
      ResizableDoubleArray result = new ResizableDoubleArray();
      copy(this, result);
      return result;
   }

   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof ResizableDoubleArray)) {
         return false;
      } else {
         synchronized(this) {
            boolean var10000;
            synchronized(object) {
               boolean result = true;
               ResizableDoubleArray other = (ResizableDoubleArray)object;
               result = result && other.contractionCriterion == this.contractionCriterion;
               result = result && other.expansionFactor == this.expansionFactor;
               result = result && other.expansionMode == this.expansionMode;
               result = result && other.numElements == this.numElements;
               result = result && other.startIndex == this.startIndex;
               if (result) {
                  var10000 = Arrays.equals(this.internalArray, other.internalArray);
                  return var10000;
               }

               var10000 = false;
            }

            return var10000;
         }
      }
   }

   public synchronized int hashCode() {
      int[] hashData = new int[]{Double.valueOf(this.expansionFactor).hashCode(), Double.valueOf(this.contractionCriterion).hashCode(), this.expansionMode.hashCode(), Arrays.hashCode(this.internalArray), this.numElements, this.startIndex};
      return Arrays.hashCode(hashData);
   }

   public static enum ExpansionMode {
      MULTIPLICATIVE,
      ADDITIVE;
   }
}
