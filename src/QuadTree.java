import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class QuadTree {
    public static final int MAX_DEPTH = 8;
    private int totalNodes;
    private int maxDepth;
    private int width, height;
    private Quadrant root;
    private int errorMethod;
    private double threshold;
    private int minBlockSize;

    public QuadTree(BufferedImage image, int errorMethod, double threshold, int minBlockSize) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.maxDepth = 0;
        this.totalNodes = 0;
        start(image);
    }

    private void start(BufferedImage image) {
        Rectangle fullRect = new Rectangle(0, 0, width, height);
        root = new Quadrant(image, fullRect, 0, errorMethod);
        totalNodes++;
        build(root, image);
    }

    private void build(Quadrant quadrant, BufferedImage image) {
        if (quadrant.depth >= MAX_DEPTH ||
            quadrant.error <= threshold ||
            quadrant.bbox.width / 2 < minBlockSize ||
            quadrant.bbox.height / 2 < minBlockSize) {
            if (quadrant.depth > maxDepth) {
                maxDepth = quadrant.depth;
            }
            quadrant.leaf = true;
            return;
        }

        quadrant.splitQuadrant(image, errorMethod);
        for (Quadrant child : quadrant.children) {
            totalNodes++;
            build(child, image);
        }
    }

    public BufferedImage createImage(int customDepth) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        List<Quadrant> leaves = getLeafQuadrants(customDepth);
        for (Quadrant q : leaves) {
            g2d.setColor(q.color);
            g2d.fillRect(q.bbox.x, q.bbox.y, q.bbox.width, q.bbox.height);
        }
        g2d.dispose();
        return output;
    }

    public List<Quadrant> getLeafQuadrants(int depth) {
        if (depth > maxDepth) {
            throw new IllegalArgumentException("A depth larger than the tree's depth was given");
        }
        List<Quadrant> leaves = new ArrayList<>();
        recursiveSearch(root, depth, leaves);
        return leaves;
    }

    private void recursiveSearch(Quadrant quadrant, int maxSearchDepth, List<Quadrant> list) {
        if (quadrant.leaf || quadrant.depth == maxSearchDepth) {
            list.add(quadrant);
        } else if (quadrant.children != null) {
            for (Quadrant child : quadrant.children) {
                recursiveSearch(child, maxSearchDepth, list);
            }
        }
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public static class Quadrant {
        public Rectangle bbox;
        public int depth;
        public Quadrant[] children;
        public boolean leaf;
        public double error;
        public Color color;

        public Quadrant(BufferedImage image, Rectangle bbox, int depth, int errorMethod) {
            this.bbox = bbox;
            this.depth = depth;
            this.children = null;
            this.leaf = false;
            BufferedImage subImage = image.getSubimage(bbox.x, bbox.y, bbox.width, bbox.height);
            this.error = computeError(subImage, errorMethod);
            this.color = averageColor(subImage);
        }

        public void splitQuadrant(BufferedImage image, int errorMethod) {
            int x = bbox.x;
            int y = bbox.y;
            int w = bbox.width;
            int h = bbox.height;
            int midX = x + w / 2;
            int midY = y + h / 2;

            Rectangle upperLeftRect = new Rectangle(x, y, w / 2, h / 2);
            Rectangle upperRightRect = new Rectangle(midX, y, w - w / 2, h / 2);
            Rectangle bottomLeftRect = new Rectangle(x, midY, w / 2, h - h / 2);
            Rectangle bottomRightRect = new Rectangle(midX, midY, w - w / 2, h - h / 2);

            children = new Quadrant[4];
            children[0] = new Quadrant(image, upperLeftRect, depth + 1, errorMethod);
            children[1] = new Quadrant(image, upperRightRect, depth + 1, errorMethod);
            children[2] = new Quadrant(image, bottomLeftRect, depth + 1, errorMethod);
            children[3] = new Quadrant(image, bottomRightRect, depth + 1, errorMethod);
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
            int w = img.getWidth(), h = img.getHeight(), total = w * h;
            long sumR = 0, sumG = 0, sumB = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    sumR += col.getRed();
                    sumG += col.getGreen();
                    sumB += col.getBlue();
                }
            }
            double avgR = sumR / (double) total;
            double avgG = sumG / (double) total;
            double avgB = sumB / (double) total;
            double varR = 0, varG = 0, varB = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    varR += Math.pow(col.getRed() - avgR, 2);
                    varG += Math.pow(col.getGreen() - avgG, 2);
                    varB += Math.pow(col.getBlue() - avgB, 2);
                }
            }
            varR /= total;
            varG /= total;
            varB /= total;
            return (varR + varG + varB) / 3;
        }

        private static double computeMADError(BufferedImage img) {
            int w = img.getWidth(), h = img.getHeight(), total = w * h;
            long sumR = 0, sumG = 0, sumB = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    sumR += col.getRed();
                    sumG += col.getGreen();
                    sumB += col.getBlue();
                }
            }
            double avgR = sumR / (double) total;
            double avgG = sumG / (double) total;
            double avgB = sumB / (double) total;
            double madR = 0, madG = 0, madB = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    madR += Math.abs(col.getRed() - avgR);
                    madG += Math.abs(col.getGreen() - avgG);
                    madB += Math.abs(col.getBlue() - avgB);
                }
            }
            madR /= total;
            madG /= total;
            madB /= total;
            return (madR + madG + madB) / 3;
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
            int w = img.getWidth(), h = img.getHeight(), total = w * h;
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
            int w = img.getWidth(), h = img.getHeight(), total = w * h;
            long sumR = 0, sumG = 0, sumB = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    sumR += col.getRed();
                    sumG += col.getGreen();
                    sumB += col.getBlue();
                }
            }
            int avgR = (int) (sumR / total);
            int avgG = (int) (sumG / total);
            int avgB = (int) (sumB / total);
            return new Color(avgR, avgG, avgB);
        }
    }

    public static void main(String[] args) {
        int errorMethod = 1;
        double threshold = 15.0;
        int minBlockSize = 4;
        String inputImagePath = "alone.jpg";

        try {
            BufferedImage image = ImageIO.read(new File(inputImagePath));
            QuadTree quadTree = new QuadTree(image, errorMethod, threshold, minBlockSize);
            BufferedImage outputImage = quadTree.createImage(quadTree.getMaxDepth());
            String outputImagePath = "aloneout.jpg";
            ImageIO.write(outputImage, "jpg", new File(outputImagePath));
            System.out.println("QuadTree compression complete.");
            System.out.println("Max Depth: " + quadTree.getMaxDepth());
            System.out.println("Total Nodes: " + quadTree.getTotalNodes());
            System.out.println("Output image saved at: " + outputImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
