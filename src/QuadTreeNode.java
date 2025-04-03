import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class QuadTreeNode {
    public Rectangle bbox;
    public int depth;
    public QuadTreeNode[] children;
    public boolean leaf;
    public double error;
    public Color color;

    public QuadTreeNode(BufferedImage image, Rectangle bbox, int depth, int errorMethod) {
        this.bbox = bbox;
        this.depth = depth;
        this.leaf = false;
        this.children = null;
        BufferedImage subImage = image.getSubimage(bbox.x, bbox.y, bbox.width, bbox.height);
        this.error = computeError(subImage, errorMethod);
        this.color = averageColor(subImage);
    }

    public void split(BufferedImage image, int errorMethod) {
        int x = bbox.x;
        int y = bbox.y;
        int w = bbox.width;
        int h = bbox.height;
        int midX = x + w / 2;
        int midY = y + h / 2;

        Rectangle rect1 = new Rectangle(x, y, w / 2, h / 2);
        Rectangle rect2 = new Rectangle(midX, y, w - w / 2, h / 2);
        Rectangle rect3 = new Rectangle(x, midY, w / 2, h - h / 2);
        Rectangle rect4 = new Rectangle(midX, midY, w - w / 2, h - h / 2);

        children = new QuadTreeNode[4];
        children[0] = new QuadTreeNode(image, rect1, depth + 1, errorMethod);
        children[1] = new QuadTreeNode(image, rect2, depth + 1, errorMethod);
        children[2] = new QuadTreeNode(image, rect3, depth + 1, errorMethod);
        children[3] = new QuadTreeNode(image, rect4, depth + 1, errorMethod);
    }

    public static double computeError(BufferedImage img, int errorMethod) {
        switch (errorMethod) {
            case 1:
                return computeVarianceError(img);
            case 2:
                return computeMADError(img);
            case 3:
                return computeMaxPixelDiffError(img);
            case 4:
                return computeEntropyError(img);
            default:
                return computeVarianceError(img);
        }
    }

    private static double computeVarianceError(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight(), total = 0;
        Color avgColor = averageColor(img);
        double avgR = avgColor.getRed(), avgG = avgColor.getGreen(), avgB = avgColor.getBlue();
        double varR = 0, varG = 0, varB = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color col = new Color(img.getRGB(x, y));
                varR += Math.pow(col.getRed() - avgR, 2);
                varG += Math.pow(col.getGreen() - avgG, 2);
                varB += Math.pow(col.getBlue() - avgB, 2);
                total++;
            }
        }
        return (varR / total + varG / total + varB / total) / 3;
    }

    private static double computeMADError(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight(), total = 0;
        Color avgColor = averageColor(img);
        double avgR = avgColor.getRed(), avgG = avgColor.getGreen(), avgB = avgColor.getBlue();
        double madR = 0, madG = 0, madB = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color col = new Color(img.getRGB(x, y));
                madR += Math.abs(col.getRed() - avgR);
                madG += Math.abs(col.getGreen() - avgG);
                madB += Math.abs(col.getBlue() - avgB);
                total++;
            }
        }
        return (madR / total + madG / total + madB / total) / 3;
    }

    private static double computeMaxPixelDiffError(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int maxR = 0, maxG = 0, maxB = 0, minR = 255, minG = 255, minB = 255;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color col = new Color(img.getRGB(x, y));
                int r = col.getRed(), g = col.getGreen(), b = col.getBlue();
                if (r > maxR) maxR = r;
                if (g > maxG) maxG = g;
                if (b > maxB) maxB = b;
                if (r < minR) minR = r;
                if (g < minG) minG = g;
                if (b < minB) minB = b;
            }
        }
        return ((maxR - minR) + (maxG - minG) + (maxB - minB)) / 3.0;
    }

    private static double computeEntropyError(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight(), total = 0;
        int[] histR = new int[256], histG = new int[256], histB = new int[256];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color col = new Color(img.getRGB(x, y));
                histR[col.getRed()]++;
                histG[col.getGreen()]++;
                histB[col.getBlue()]++;
            }
        }
        double entropyR = 0, entropyG = 0, entropyB = 0;
        for (int i = 0; i < 256; i++) {
            double pR = histR[i] / (double) total;
            double pG = histG[i] / (double) total;
            double pB = histB[i] / (double) total;
            if (pR > 0) entropyR -= pR * (Math.log(pR) / Math.log(2));
            if (pG > 0) entropyG -= pG * (Math.log(pG) / Math.log(2));
            if (pB > 0) entropyB -= pB * (Math.log(pB) / Math.log(2));
        }
        return (entropyR + entropyG + entropyB) / 3;
    }

    private static Color averageColor(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight(), total = 0;
        long sumR = 0, sumG = 0, sumB = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color col = new Color(img.getRGB(x, y));
                sumR += col.getRed();
                sumG += col.getGreen();
                sumB += col.getBlue();
            }
        }
        return new Color((int)(sumR / total), (int)(sumG / total), (int)(sumB / total));
    }
}
