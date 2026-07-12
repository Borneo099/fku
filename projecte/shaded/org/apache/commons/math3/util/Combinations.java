package moze_intel.projecte.shaded.org.apache.commons.math3.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.DimensionMismatchException;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.MathInternalError;
import moze_intel.projecte.shaded.org.apache.commons.math3.exception.OutOfRangeException;

public class Combinations implements Iterable {
   private final int n;
   private final int k;
   private final IterationOrder iterationOrder;

   public Combinations(int n, int k) {
      this(n, k, Combinations.IterationOrder.LEXICOGRAPHIC);
   }

   private Combinations(int n, int k, IterationOrder iterationOrder) {
      CombinatoricsUtils.checkBinomial(n, k);
      this.n = n;
      this.k = k;
      this.iterationOrder = iterationOrder;
   }

   public int getN() {
      return this.n;
   }

   public int getK() {
      return this.k;
   }

   public Iterator iterator() {
      if (this.k != 0 && this.k != this.n) {
         switch (this.iterationOrder) {
            case LEXICOGRAPHIC:
               return new LexicographicIterator(this.n, this.k);
            default:
               throw new MathInternalError();
         }
      } else {
         return new SingletonIterator(MathArrays.natural(this.k));
      }
   }

   public Comparator comparator() {
      return new LexicographicComparator(this.n, this.k);
   }

   private static class LexicographicComparator implements Comparator, Serializable {
      private static final long serialVersionUID = 20130906L;
      private final int n;
      private final int k;

      LexicographicComparator(int n, int k) {
         this.n = n;
         this.k = k;
      }

      public int compare(int[] c1, int[] c2) {
         if (c1.length != this.k) {
            throw new DimensionMismatchException(c1.length, this.k);
         } else if (c2.length != this.k) {
            throw new DimensionMismatchException(c2.length, this.k);
         } else {
            int[] c1s = MathArrays.copyOf(c1);
            Arrays.sort(c1s);
            int[] c2s = MathArrays.copyOf(c2);
            Arrays.sort(c2s);
            long v1 = this.lexNorm(c1s);
            long v2 = this.lexNorm(c2s);
            if (v1 < v2) {
               return -1;
            } else {
               return v1 > v2 ? 1 : 0;
            }
         }
      }

      private long lexNorm(int[] c) {
         long ret = 0L;

         for(int i = 0; i < c.length; ++i) {
            int digit = c[i];
            if (digit < 0 || digit >= this.n) {
               throw new OutOfRangeException(digit, 0, this.n - 1);
            }

            ret += (long)(c[i] * ArithmeticUtils.pow(this.n, i));
         }

         return ret;
      }
   }

   private static class SingletonIterator implements Iterator {
      private final int[] singleton;
      private boolean more = true;

      SingletonIterator(int[] singleton) {
         this.singleton = singleton;
      }

      public boolean hasNext() {
         return this.more;
      }

      public int[] next() {
         if (this.more) {
            this.more = false;
            return this.singleton;
         } else {
            throw new NoSuchElementException();
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static class LexicographicIterator implements Iterator {
      private final int k;
      private final int[] c;
      private boolean more = true;
      private int j;

      LexicographicIterator(int n, int k) {
         this.k = k;
         this.c = new int[k + 3];
         if (k != 0 && k < n) {
            for(int i = 1; i <= k; ++i) {
               this.c[i] = i - 1;
            }

            this.c[k + 1] = n;
            this.c[k + 2] = 0;
            this.j = k;
         } else {
            this.more = false;
         }
      }

      public boolean hasNext() {
         return this.more;
      }

      public int[] next() {
         if (!this.more) {
            throw new NoSuchElementException();
         } else {
            int[] ret = new int[this.k];
            System.arraycopy(this.c, 1, ret, 0, this.k);
            int x = 0;
            if (this.j > 0) {
               x = this.j;
               this.c[this.j] = x;
               --this.j;
               return ret;
            } else if (this.c[1] + 1 < this.c[2]) {
               int var10002 = this.c[1]++;
               return ret;
            } else {
               this.j = 2;
               boolean stepDone = false;

               while(!stepDone) {
                  this.c[this.j - 1] = this.j - 2;
                  x = this.c[this.j] + 1;
                  if (x == this.c[this.j + 1]) {
                     ++this.j;
                  } else {
                     stepDone = true;
                  }
               }

               if (this.j > this.k) {
                  this.more = false;
                  return ret;
               } else {
                  this.c[this.j] = x;
                  --this.j;
                  return ret;
               }
            }
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   private static enum IterationOrder {
      LEXICOGRAPHIC;
   }
}
