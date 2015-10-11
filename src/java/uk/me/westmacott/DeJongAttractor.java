package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DeJongAttractor {

    static int FULL_SIZE = 10000;
    static int FULL_INSET = 4;
    static int FULL_SIZE_2 = FULL_SIZE / 2;
    static int FULL_INSET_SIZE_4 = (FULL_SIZE - FULL_INSET - FULL_INSET) / 4;
    static int CROPPED_INSET = 4;
    static int BORDERED_INSET = 20;
    static int SCALED_MAX = 2000;
    static Pattern p = Pattern.compile("[^-\\.0-9]*([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]+([-\\.0-9]+)[^-\\.0-9]*");

    private int iterations;
    private double a, b, c, d;
    private String filename;
    private Timer t = new Timer();

    public DeJongAttractor(int iterations, String constants) {
        Matcher m = p.matcher(constants);
        m.find();
        String a = m.group(1);
        String b = m.group(2);
        String c = m.group(3);
        String d = m.group(4);
        System.out.println(a + ", " +  b +  ", " + c + ", " + d);
        this.filename = String.format("deJong_%s_%s_%s_%s_%d", a, b, c, d, iterations);
        this.iterations = iterations;
        this.a = Double.parseDouble(a);
        this.b = Double.parseDouble(b);
        this.c = Double.parseDouble(c);
        this.d = Double.parseDouble(d);
    }

    public DeJongAttractor(int iterations, String a, String b, String c, String d) {
        this.filename = String.format("deJong_%s_%s_%s_%s_%d", a, b, c, d, iterations);
        this.iterations = iterations;
        this.a = Double.parseDouble(a);
        this.b = Double.parseDouble(b);
        this.c = Double.parseDouble(c);
        this.d = Double.parseDouble(d);
    }

    public void run() throws Exception {

        int[][] hits;
        try {
            hits = loadHits();
        }
        catch (Exception e) {
            hits = computeHits();
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
        Color background = lookupColour(0, maxHits);
        t.mark("Scanned hits.");

        int croppedWidth = maxX - minX + CROPPED_INSET + CROPPED_INSET;
        int croppedHeight = maxY - minY + CROPPED_INSET + CROPPED_INSET;
        BufferedImage image = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(background);
        g.fillRect(0, 0, croppedWidth, croppedHeight);
        g.dispose();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                int count = hits[x][y];
                if (count != 0) {
                    int dx = CROPPED_INSET - minX + x;
                    int dy = CROPPED_INSET - minY + y;
                    image.setRGB(dx, dy, lookupColour(count, maxHits).getRGB());
                }
            }
            if (x%10 == 0) {
                System.out.print("\r" + minX + "..." + x + "..." + maxX);
            }
        }
        System.out.println();
        t.mark("Rendered cropped image.");

        double scaleFactor = Math.min(
                (double)SCALED_MAX / (double)croppedWidth,
                (double)SCALED_MAX / (double)croppedHeight);
        int scaledWidth = (int) (croppedWidth * scaleFactor);
        int scaledHeight = (int) (croppedHeight * scaleFactor);
        BufferedImage scaledImage = getScaledInstance(image, scaledWidth, scaledHeight, 1.1);
        { image = null; System.gc(); }
        t.mark("Rendered scaled image.");

        int borderedWidth = scaledWidth + BORDERED_INSET + BORDERED_INSET;
        int borderedHeight = scaledHeight + BORDERED_INSET + BORDERED_INSET;
        BufferedImage borderedImage = new BufferedImage(borderedWidth, borderedHeight, BufferedImage.TYPE_INT_RGB);
        g = borderedImage.createGraphics();
        g.setColor(background);
        g.fillRect(0, 0, borderedWidth, borderedHeight);
        g.drawImage(scaledImage, BORDERED_INSET, BORDERED_INSET, null);
        g.dispose();
        t.mark("Rendered bordered image.");

        ImageIO.write(borderedImage, "png", new File("./" + filename + ".png"));
        t.mark("Wrote image to file.");

        t.finished();
    }

    private int[][] computeHits() throws IOException {
        int[][] hits;
        hits = new int[FULL_SIZE][FULL_SIZE];
        iterate(hits, 0, 0, iterations);
        t.mark("Iterated for hits.");

        FileOutputStream fos = new FileOutputStream(filename + ".data");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(hits);
        t.mark("Saved data.");
        return hits;
    }

    private int[][] loadHits() throws IOException, ClassNotFoundException {
        int[][] hits;FileInputStream fis = new FileInputStream(filename + ".data");
        ObjectInputStream iis = new ObjectInputStream(fis);
        hits = (int[][]) iis.readObject();
        t.mark("Loaded data.");
        return hits;
    }

    private void iterate(int[][] hits, double x1, double y1, int iterations) {
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
        System.out.println();
    }

    public static BufferedImage getScaledInstance(BufferedImage img,
                                                  int targetWidth,
                                                  int targetHeight,
                                                  double stagefactor) {

        final int type = (img.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage current = img;
        int w = current.getWidth();
        int h = current.getHeight();
        do {
            if (w > targetWidth) {
                w /= stagefactor;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (h > targetHeight) {
                h /= stagefactor;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage next = new BufferedImage(w, h, type);
            Graphics2D g2 = next.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(current, 0, 0, w, h, null);
            g2.dispose();
            current = next;
        } while (w != targetWidth || h != targetHeight);

        return current;
    }

    public abstract Color lookupColour(int hits, int maxHits);

}
