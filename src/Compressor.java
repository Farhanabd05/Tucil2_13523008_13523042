import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Compressor {
    public static final int MAX_DEPTH = 8;
    private int totalNodes = 0;
    private int maxDepth = 0;
    private int width, height;
    private QuadTreeNode root;
    private int errorMethod;
    private double threshold;
    private int minBlockSize;

    public Compressor(BufferedImage image, int errorMethod, double threshold, int minBlockSize) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        start(image);
    }

    private void start(BufferedImage image) {
        Rectangle fullRect = new Rectangle(0, 0, width, height);
        root = new QuadTreeNode(image, fullRect, 0, errorMethod);
        totalNodes++;
        build(root, image);
    }

    private void build(QuadTreeNode node, BufferedImage image) {
        if (node.depth >= MAX_DEPTH ||
            node.error <= threshold ||
            node.bbox.width / 2 < minBlockSize ||
            node.bbox.height / 2 < minBlockSize) {
            if (node.depth > maxDepth) {
                maxDepth = node.depth;
            }
            node.leaf = true;
            return;
        }
        node.split(image, errorMethod);
        for (QuadTreeNode child : node.children) {
            totalNodes++;
            build(child, image);
        }
    }

    public BufferedImage createImage(int customDepth) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        List<QuadTreeNode> leaves = new ArrayList<>();
        getLeaves(root, customDepth, leaves);
        for (QuadTreeNode node : leaves) {
            g2d.setColor(node.color);
            g2d.fillRect(node.bbox.x, node.bbox.y, node.bbox.width, node.bbox.height);
        }
        g2d.dispose();
        return output;
    }

    private void getLeaves(QuadTreeNode node, int targetDepth, List<QuadTreeNode> leaves) {
        if (node.leaf || node.depth == targetDepth) {
            leaves.add(node);
        } else if (node.children != null) {
            for (QuadTreeNode child : node.children) {
                getLeaves(child, targetDepth, leaves);
            }
        }
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public static void main(String[] args) {
        int errorMethod = InputParser.parseErrorMethod(args);
        double threshold = InputParser.parseThreshold(args);
        int minBlockSize = InputParser.parseMinBlockSize(args);
        String inputPath = InputParser.parseInputImagePath(args);
        String outputPath = InputParser.parseOutputImagePath(args);
        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            Compressor compressor = new Compressor(image, errorMethod, threshold, minBlockSize);
            BufferedImage outputImage = compressor.createImage(compressor.getMaxDepth());
            OutputHandler.saveImage(outputImage, outputPath, "jpg");
            System.out.println("Compression complete.");
            System.out.println("Max Depth: " + compressor.getMaxDepth());
            System.out.println("Total Nodes: " + compressor.getTotalNodes());
            System.out.println("Output image saved at: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*

java Compressor 1 15.0 4 "C:\Users\HP 14s\Documents\1_FARHAN\STIMA\TUCIL2\Tucil2_13523008_13523042\test\tc\alone.jpg" "C:\Users\HP 14s\Documents\1_FARHAN\STIMA\TUCIL2\Tucil2_13523008_13523042\test\sol\aloneout.jpg"
*/