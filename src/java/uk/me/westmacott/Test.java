package uk.me.westmacott;

import java.awt.*;

public class Test {

    public static void main(String[] args) throws Exception {

        DeJongAttractor one = new DeJongAttractor(1_000_000_000, "-2.7", "-0.09", "-0.86", "-2.2") {

            @Override
            public Color lookupColour(int hits, int maxHits) {

                if (hits == 0) {
                    return Color.WHITE;
                }

                double r = 700.0;
                double g = 225.0;
                double b = 400.0;

                for (int i = 0; i < hits; i++) {
                    r *= 0.999;
                    g *= 0.99;
                    b *= 0.98;
                }

                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                return new Color((int) r, (int) g, (int) b);
            }
        };

//        Pattern p = Pattern.compile("[^-\\.0-9]*([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]*");
//        Matcher matcher = p.matcher("a = -0.827, b = -1.637, c = 1.659, d = -0.943");
//        System.out.println(matcher.find());
//        System.out.println(matcher.groupCount());
//        System.out.println(matcher.group(0));
//        System.out.println(matcher.group(1));
//        System.out.println(matcher.group(2));
//        System.out.println(matcher.group(3));
//        System.out.println(matcher.group(4));

//        String[] bits = "a = -0.827, b = -1.637, c = 1.659, d = -0.943".split("[^-\\.0-9]*");
//        Stream.of(bits).forEach(System.out::println);
//        System.out.println(bits);
//
        DeJongAttractor two = new DeJongAttractor(1_000_000_000, "a = -0.827, b = -1.637, c = 1.659, d = -0.943") {

            @Override
            public Color lookupColour(int hits, int maxHits) {

                if (hits == 0) {
                    return Color.WHITE;
                }

                double r = 400.0;
                double g = 225.0;
                double b = 40000.0;

                for (int i = 0; i < hits; i++) {
                    r *= 0.995;
                    g *= 0.98;
                    b *= 0.92;
                }

                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                return new Color((int) b, (int) g, (int) r);
            }
        };

        DeJongAttractor three = new DeJongAttractor(1_000_000_000, "a = 0.970, b = -1.899, c = 1.381,d = -1.506") {
            @Override
            public Color lookupColour(int hits, int maxHits) {

                if (hits == 0) {
                    return Color.WHITE;
                }

                double r = 225.0;
                double g = 40000.0;
                double b = 400.0;

                for (int i = 0; i < hits; i++) {
                    r *= 0.995;
                    g *= 0.98;
                    b *= 0.92;
                }

                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                return new Color((int) b, (int) g, (int) r);
            }
        };

//        one.run();
//        two.run();
        three.run();

    }

}
