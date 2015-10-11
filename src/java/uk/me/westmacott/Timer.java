package uk.me.westmacott;

public class Timer {

    long start = System.currentTimeMillis();
    long last = start;

    private String display(long millis) {
        long duration = millis;
        String unit = " Millisecond";
        if (duration >= 1000) {
            duration /= 1000;
            unit = " Second";
            if (duration >= 60) {
                duration /= 60;
                unit = " Minute";
                if (duration >= 60) {
                    duration /= 60;
                    unit = " Hour";
                }
            }
        }
        if (duration > 1) {
            unit += "s";
        }
        return duration + unit;
    }

    public void mark(String message) {
        long now = System.currentTimeMillis();
        long since = now - last;
        System.out.println(display(since) + " : " + message);
        last = now;
    }

    public void finished() {
        long now = System.currentTimeMillis();
        long since = now - start;
        System.out.println("Finished in: " + display(since));
    }

}
