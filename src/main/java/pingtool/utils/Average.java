package pingtool.utils;

public class Average {

    private double[] doubles = new double[100];
    private int index = 0;
    private boolean first = true;

    public Average() {

    }

    public Average(int length) {
        if (length <= 0) {
            return;
        }
        doubles = new double[length];
    }

    public void add(double i) {
        doubles[index++] = i;
        if (index == doubles.length) {
            first = false;
            index = 0;
        }
    }

    public double average() {
        if (first && index == 0) {
            return 0;
        }
        if (first) {
            double d = 0;
            for (int i = 0; i < index; i++) {
                d += doubles[i];
            }
            return d / index;
        } else {
            double d = 0;
            for (int i = 0; i < doubles.length; i++) {
                d += doubles[i];
            }
            return d / doubles.length;
        }
    }

}
