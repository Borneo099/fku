package moze_intel.projecte.shaded.org.apache.commons.math3.util;

public interface DoubleArray {
   int getNumElements();

   double getElement(int var1);

   void setElement(int var1, double var2);

   void addElement(double var1);

   void addElements(double[] var1);

   double addElementRolling(double var1);

   double[] getElements();

   void clear();
}
