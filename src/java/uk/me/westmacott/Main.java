package uk.me.westmacott;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Main {

    static int FULL_SIZE = 10000;
    static int FULL_INSET = 4;
    static int FULL_SIZE_2 = FULL_SIZE / 2;
    static int FULL_INSET_SIZE_4 = (FULL_SIZE - FULL_INSET - FULL_INSET) / 4;
    static int CROPPED_INSET = 4;
    static int BORDERED_INSET = 20;
    static int SCALED_MAX = 2000;

    static Color BACKGROUND = Color.WHITE;

//    static double a = 1.641, b = 1.902, c = 0.316, d = 1.525;

    static int iterations = 1_000_000_000;
    static String as = "-2.7", bs = "-0.09", cs = "-0.86", ds = "-2.2";
    static double
            a = Double.parseDouble(as),
            b = Double.parseDouble(bs),
            c = Double.parseDouble(cs),
            d = Double.parseDouble(ds);

    static String filename = String.format("deJong_%s_%s_%s_%s_%d", as, bs, cs, ds, iterations);


    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Timer t = new Timer();

        Path path = Paths.get("number.text");
        int number = Integer.parseInt(Files.readAllLines(path).get(0));
        Files.write(path, Arrays.asList(Integer.toString(number + 1)));
        t.mark("Loaded number.");

        int[][] hits;
        try {
            FileInputStream fis = new FileInputStream(filename + ".data");
            ObjectInputStream iis = new ObjectInputStream(fis);
            hits = (int[][]) iis.readObject();
            t.mark("Loaded data.");
        }
        catch (Exception e) {
            hits = new int[FULL_SIZE][FULL_SIZE];
            iterate(hits, 0, 0, iterations);
            t.mark("Iterated for hits.");

            FileOutputStream fos = new FileOutputStream(filename + ".data");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hits);
            t.mark("Saved data.");
        }

        int maxHits = 0;
        int maxX = 0;
        int maxY = 0;
        int minX = FULL_SIZE;
        int minY = FULL_SIZE;
        for (int x = 0; x < FULL_SIZE; x++) {
            for (int y = 0; y < FULL_SIZE; y++) {
                maxHits = Math.max(hits[x][y], maxHits);
                if (hits[x][y] > 0) {
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                }
            }
        }
        t.mark("Scanned hits.");

        int croppedWidth = maxX - minX + CROPPED_INSET + CROPPED_INSET;
        int croppedHeight = maxY - minY + CROPPED_INSET + CROPPED_INSET;

        BufferedImage image = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, croppedWidth, croppedHeight);
        g.dispose();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                int count = hits[x][y];
                if (count != 0) {
                    int dx = CROPPED_INSET - minX + x;
                    int dy = CROPPED_INSET - minY + y;
                    image.setRGB(dx, dy, lookupColour(count).getRGB());
                }
            }
            if (x%10 == 0) {
                System.out.print("\r" + minX + "..." + x + "..." + maxX);
            }
        }
        System.out.println();
        t.mark("Rendered full image.");

        double scaleFactor = Math.min(
                (double)SCALED_MAX / (double)croppedWidth,
                (double)SCALED_MAX / (double)croppedHeight);
        int scaledWidth = (int) (croppedWidth * scaleFactor);
        int scaledHeight = (int) (croppedHeight * scaleFactor);
        BufferedImage scaledImage = getScaledInstance(image, scaledWidth, scaledHeight,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC, true, 1.1);
        t.mark("Scaled image.");

        {
            printMemory();
            image = null;
            System.gc();
            printMemory();
        }

        int borderedWidth = scaledWidth + BORDERED_INSET + BORDERED_INSET;
        int borderedHeight = scaledHeight + BORDERED_INSET + BORDERED_INSET;
        BufferedImage borderedImage = new BufferedImage(borderedWidth, borderedHeight, BufferedImage.TYPE_INT_RGB);
        g = borderedImage.createGraphics();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, borderedWidth, borderedHeight);
        g.drawImage(scaledImage, BORDERED_INSET, BORDERED_INSET, null);
        g.dispose();

        ImageIO.write(borderedImage, "png", new File("./" + filename + ".png"));

        t.mark("Wrote image.");
        t.finished();
    }

    private static void printMemory() {
        int mb = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Memory: " +
                        (runtime.totalMemory() - runtime.freeMemory()) / mb +
                        " / " +
                        runtime.totalMemory() / mb);
    }

    private static Color lookupColour(int count) {

        double r = 700.0;
        double g = 225.0;
        double b = 400.0;

        for (int i = 0; i < count; i++) {
            r *= 0.999;
            g *= 0.99;
            b *= 0.98;
        }
        
        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;

        return new Color((int) r, (int) g, (int) b);
    }

    private static Color lookupColour(int count, int max) {
//        if (count == 0) {
//            return BACKGROUND;
//        }

        double scaled = Math.log(100.0 * count) / Math.log(100.0 * max); // 0.0 -> 1.0

        float hue = 0.35f + (float) (-1.0 * scaled);
        float sat = 1.0f;
        float lit = 1.0f - (float) (scaled * scaled * scaled);
//        Color hsb =
        return Color.getHSBColor(hue, sat, lit);
//        int alpha = (int) (255.0 * scaled * scaled);// * scaled * scaled);
//        return new Color(hsb.getRed(), hsb.getGreen(), hsb.getBlue(), alpha);
    }

    private static void iterate(int[][] hits, double x1, double y1, int iterations) {
        double x0;
        double y0;
        int xg;
        int yg;

        String track = " -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  - /";
        String marker = "`<o&=o>";

        int chunk = iterations / 100;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < chunk; j++) {
                x0 = x1;
                y0 = y1;
                x1 = Math.sin(a * y0) - Math.cos(b * x0);
                y1 = Math.sin(c * x0) - Math.cos(d * y0);
                xg = FULL_SIZE_2 + (int)(FULL_INSET_SIZE_4 * x0);
                yg = FULL_SIZE_2 + (int)(FULL_INSET_SIZE_4 * y0);
                if (xg < FULL_SIZE && yg < FULL_SIZE) {
                    hits[xg][yg]++;
                }
            }
            System.out.print("\r" +
                    track.substring(0, i) +
                    marker +
                    track.substring(i + marker.length()));
        }
        System.out.println("");

//        int centile = iterations / 100;
//        System.out.println("._________._________|_________|_________|_________|_________|_________|_________|_________|_________|");
//        System.out.println("]]]]]]]]]]]]]]]]]_________|_________|_________|_________|_________|_________|_________|_________|_________|");
//        for (int i = 0; i < iterations; i++) {
//            x0 = x1;
//            y0 = y1;
//            x1 = Math.sin(a * y0) - Math.cos(b * x0);
//            y1 = Math.sin(c * x0) - Math.cos(d * y0);
//            xg = FULL_SIZE_2 + (int)(FULL_INSET_SIZE_4 * x0);
//            yg = FULL_SIZE_2 + (int)(FULL_INSET_SIZE_4 * y0);
//            if (xg < FULL_SIZE && yg < FULL_SIZE) {
//                hits[xg][yg]++;
//            }
//            if (i % centile == 0) {
//                System.out.print(".");
//            }
//        }
//        System.out.println("]");
    }

    static class ImageFrame extends JFrame {

        private Image image;

        public ImageFrame(Image image, long refreshPeriodMillis) {
            this.image = image;
            setTitle("ImageTest");
            setSize(image.getWidth(null), image.getHeight(null));
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setVisible(true);

            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                    this::repaint,
                    0,
                    refreshPeriodMillis,
                    TimeUnit.MILLISECONDS);
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, this);
        }
    }

    public static <X> Consumer<X> wrap(ThrowingConsumer<X> consumer) {
        return x -> {
            try {
                consumer.accept(x);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public interface ThrowingConsumer<X> {
        void accept(X input) throws Exception;
    }

    public static BufferedImage getScaledInstance(BufferedImage img,
                                                  int targetWidth,
                                                  int targetHeight,
                                                  Object hint,
                                                  boolean higherQuality,
                                                  double stagefactor)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= stagefactor;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= stagefactor;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
}
