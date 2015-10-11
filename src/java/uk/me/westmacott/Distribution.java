package uk.me.westmacott;

import java.util.LinkedList;

public class Distribution {

    LinkedList<Double> data = new LinkedList<>();

    public void sample(double value) {
        data.add(value);
    }

    public void view() {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Double aDouble : data) {
            min = Math.min(min, aDouble);
            max = Math.max(max, aDouble);
        }
        double range = max - min;
//        System.out.println("Distribution: min, max, range = " + min + ", " + max + ", " + range);

        int count = data.size();
        int steps = Math.min(count / 10, 100);

//        System.out.println("Distribution: count = " + count);
//        System.out.println("Distribution: steps = " + steps);

        int[] hits = new int[steps];
        double[] mins = new double[steps];
        double[] maxs = new double[steps];
        for (int i = 0; i < steps; i++) {
            mins[i] = max;
            maxs[i] = min;
        }

        for (Double aDouble : data) {
            int index = (int)(99.0 * (aDouble - min) / range);
            hits[index]++;
            mins[index] = Math.min(aDouble, mins[index]);
            maxs[index] = Math.max(aDouble, maxs[index]);
        }

        for (int i = 0; i < steps; i++) {
            System.out.println(String.format("%10f   <->   %10f : %d", mins[i], maxs[i], hits[i]));
        }
    }


}
