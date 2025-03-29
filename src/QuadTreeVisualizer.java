import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import javax.imageio.ImageIO;
import java.io.File;

public class QuadTreeVisualizer {
    public static void main(String[] args) {
        InputParser parser = new InputParser();
        try {
            // Parse input and get RGB matrix
            parser.parseInput();
            RGBMatrix rgbMatrix = parser.getRGBMatrix();
            // time now
            long start = System.currentTimeMillis();
            // Calculate initial values for root node
            Pixel avgColor = rgbMatrix.getAverageColor(0, 0, rgbMatrix.getWidth(), rgbMatrix.getHeight());
            double error = rgbMatrix.getError(0, 0, rgbMatrix.getWidth(), rgbMatrix.getHeight(), avgColor, parser.getErrorMethod());
            
            // Create and build quadtree
            QuadTreeNode root = new QuadTreeNode(0, 0, rgbMatrix.getWidth(), rgbMatrix.getHeight(), avgColor, error, parser.getErrorMethod());
            System.out.println("Memulai splitting quadtree...");
            root.split(rgbMatrix, parser.getThreshold(), parser.getMinBlockSize());
            System.out.println("Splitting quadtree selesai");
            long end = System.currentTimeMillis();
            System.out.println("Waktu eksekusi: " + (end - start) + " ms");
            // Create and save visualization
            String originalOutput = parser.getOutputFileName();
            String quadtreeOutput = "quadtree_" + originalOutput;
            String avgColorOutput = "avgcolor_" + originalOutput;
            
            // Save original processed image
            OutputHandler.writeImage(rgbMatrix, originalOutput);
            
            // Save quadtree visualization
            createQuadTreeVisualization(rgbMatrix, root, quadtreeOutput);
            
            // If requested, save average color visualization
            if (parser.getShowAvgColors()) {
                createAverageColorVisualization(root, rgbMatrix.getWidth(), rgbMatrix.getHeight(), avgColorOutput);
            }
            
            System.out.println("Visualisasi QuadTree telah dibuat dan disimpan sebagai: " + quadtreeOutput);
            if (parser.getShowAvgColors()) {
                System.out.println("Visualisasi warna rata-rata telah disimpan sebagai: " + avgColorOutput);
            }
            
            // Print statistics
            int[] nodeCountByLevel = countNodesByLevel(root);
            int totalNodes = 0;
            System.out.println("\nStatistik QuadTree:");
            for (int i = 0; i < nodeCountByLevel.length; i++) {
                if (nodeCountByLevel[i] > 0) {
                    // System.out.println("Level " + i + ": " + nodeCountByLevel[i] + " nodes");
                    totalNodes += nodeCountByLevel[i];
                }
            }
            System.out.println("Total nodes: " + totalNodes);
            System.out.println("Leaf nodes: " + countLeafNodes(root));
            
        } catch (IOException e) {
            System.err.println("ERROR: Terjadi kesalahan saat memproses gambar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createQuadTreeVisualization(RGBMatrix rgbMatrix, QuadTreeNode root, String outputFilename) {
        int width = rgbMatrix.getWidth();
        int height = rgbMatrix.getHeight();
        
        // Create a new image
        BufferedImage quadTreeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Copy original image data
        Pixel[] pixels = rgbMatrix.getPixels();
        int[] rgbArray = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            rgbArray[i] = pixels[i].getRGB();
        }
        quadTreeImage.setRGB(0, 0, width, height, rgbArray, 0, width);
        
        // Create graphics for drawing
        Graphics2D g2d = quadTreeImage.createGraphics();
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1));
        
        // Draw quadtree structure
        drawQuadTreeNodes(g2d, root);
        
        g2d.dispose();
        
        // Save the image
        saveImage(quadTreeImage, outputFilename);
    }
    
    private static void createAverageColorVisualization(QuadTreeNode root, int width, int height, String outputFilename) {
        // Create a blank image
        BufferedImage avgColorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = avgColorImage.createGraphics();
        
        // Fill with average colors
        fillWithAverageColors(g2d, root);
        
        // Draw quadtree structure
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        drawQuadTreeNodes(g2d, root);
        
        g2d.dispose();
        
        // Save the image
        saveImage(avgColorImage, outputFilename);
    }
    
    private static void fillWithAverageColors(Graphics2D g2d, QuadTreeNode node) {
        if (node.isLeaf()) {
            // For leaf nodes, fill with average color
            Pixel avgColor = node.getAvgColor();
            g2d.setColor(new Color(avgColor.getR(), avgColor.getG(), avgColor.getB()));
            g2d.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        } else {
            // For non-leaf nodes, recursively process children
            for (QuadTreeNode child : node.getChildren()) {
                fillWithAverageColors(g2d, child);
            }
        }
    }
    
    private static void drawQuadTreeNodes(Graphics2D g2d, QuadTreeNode node) {
        // Draw this node's boundary
        g2d.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        
        // If not a leaf, draw children
        if (!node.isLeaf()) {
            for (QuadTreeNode child : node.getChildren()) {
                drawQuadTreeNodes(g2d, child);
            }
        }
    }
    
    private static void saveImage(BufferedImage image, String filename) {
        try {
            String currentDir = System.getProperty("user.dir");
            String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test"
                    + File.separator + "sol";
            String outputPath = testPath + File.separator + filename;
            
            String format = getFormatFromPath(outputPath);
            if (format == null) {
                format = "png"; // Default format
            }
            
            ImageIO.write(image, format, new File(outputPath));
        } catch (IOException e) {
            System.err.println("ERROR: Gagal menyimpan gambar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getFormatFromPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == path.length() - 1) {
            return null;
        }
        return path.substring(dotIndex + 1);
    }
    
    private static int[] countNodesByLevel(QuadTreeNode root) {
        int[] counts = new int[20]; // Assuming max 20 levels
        countNodesByLevel(root, 0, counts);
        return counts;
    }
    
    private static void countNodesByLevel(QuadTreeNode node, int level, int[] counts) {
        counts[level]++;
        if (!node.isLeaf()) {
            for (QuadTreeNode child : node.getChildren()) {
                countNodesByLevel(child, level + 1, counts);
            }
        }
    }
    
    private static int countLeafNodes(QuadTreeNode node) {
        if (node.isLeaf()) {
            return 1;
        }
        
        int count = 0;
        for (QuadTreeNode child : node.getChildren()) {
            count += countLeafNodes(child);
        }
        return count;
    }
}