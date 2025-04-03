import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class QuadTreeCompression {
    // Constants for error measurement methods
    private static final int VARIANCE = 1;
    private static final int MAD = 2;
    private static final int MAX_PIXEL_DIFF = 3;
    private static final int ENTROPY = 4;
    private static final int SSIM = 5; // Bonus method
    
    private static long startTime;
    private static int treeDepth = 0;
    private static int nodeCount = 0;
    
    // Main method
    public static void main(String[] args) {
        startTime = System.currentTimeMillis();
        Scanner scanner = new Scanner(System.in);
        
        String currentDir = System.getProperty("user.dir");
        String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc";
        String solPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "sol";

        System.out.println("Masukkan nama file: ");
        String fileName = scanner.nextLine();
        String inputImagePath = testPath + File.separator + fileName;
        System.out.println("Masukkan nama file output: ");
        String outputFileName = scanner.nextLine();
        String outputImagePath = solPath + File.separator + outputFileName;
        
        
        System.out.println("Choose error measurement method:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        System.out.println("5. Structural Similarity Index (SSIM) [BONUS]");
        int errorMethod = scanner.nextInt();
        
        System.out.println("Enter threshold value:");
        double threshold = scanner.nextDouble();
        
        System.out.println("Enter minimum block size:");
        int minBlockSize = scanner.nextInt();
        
        System.out.println("Enter target compression percentage (0.0-1.0, 0 to disable):");
        double targetCompression = scanner.nextDouble();
                
        System.out.println("Enter the absolute path to save GIF (leave empty to skip):");
        String gifPath = scanner.nextLine();
        
        // Process the image
        try {
            // Load the original image
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
            
            // Preprocess the image to handle non-power-of-2 and non-square images
            BufferedImage processedImage = preprocessImage(originalImage);
            
            // Build the quad tree
            QuadNode root = buildQuadTree(processedImage, errorMethod, threshold, minBlockSize, targetCompression);
            
            // Create the compressed image
            BufferedImage compressedImage = decompressQuadTree(root, processedImage.getWidth(), processedImage.getHeight());
            
            // Crop back to original dimensions if needed
            if (originalImage.getWidth() != processedImage.getWidth() || 
                originalImage.getHeight() != processedImage.getHeight()) {
                compressedImage = compressedImage.getSubimage(0, 0, 
                                                             originalImage.getWidth(), 
                                                             originalImage.getHeight());
            }
            
            // Save the compressed image
            ImageIO.write(compressedImage, "png", new File(outputImagePath));
            
            // If GIF path is provided, create the GIF (bonus feature)
            if (!gifPath.trim().isEmpty()) {
                System.out.println("GIF creation not implemented yet");
                // createGif(gifPath, root, originalImage.getWidth(), originalImage.getHeight());
            }
            
            // Calculate compression statistics
            long originalSize = new File(inputImagePath).length();
            long compressedSize = new File(outputImagePath).length();
            double compressionRatio = 1.0 - ((double) compressedSize / originalSize);
            
            // Print results
            System.out.println("Execution time: " + (System.currentTimeMillis() - startTime) + " ms");
            System.out.println("Original image size: " + originalSize + " bytes");
            System.out.println("Compressed image size: " + compressedSize + " bytes");
            System.out.println("Compression percentage: " + (compressionRatio * 100) + "%");
            System.out.println("Tree depth: " + treeDepth);
            System.out.println("Number of nodes: " + nodeCount);
            
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
        
        scanner.close();
    }
    
    /**
     * Preprocess the image to ensure dimensions are power of 2 and it's square.
     * Pads the image if necessary.
     */
    private static BufferedImage preprocessImage(BufferedImage original) {
        // Find the next power of 2 that is greater than both width and height
        int maxDimension = Math.max(original.getWidth(), original.getHeight());
        int paddedSize = nextPowerOfTwo(maxDimension);
        
        // Create a new square image with the power-of-2 dimension
        BufferedImage paddedImage = new BufferedImage(paddedSize, paddedSize, BufferedImage.TYPE_INT_RGB);
        
        // Fill with white background
        Graphics2D g2d = paddedImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, paddedSize, paddedSize);
        
        // Draw the original image onto the padded image
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        
        return paddedImage;
    }
    
    /**
     * Calculate the next power of 2 greater than or equal to n
     */
    private static int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
    
    /**
     * Build the quad tree using the specified error measurement method and threshold
     */
    private static QuadNode buildQuadTree(BufferedImage image, int errorMethod, double threshold, 
                                         int minBlockSize, double targetCompression) {
        // If target compression is enabled, adjust threshold to meet the target
        if (targetCompression > 0) {
            threshold = findOptimalThreshold(image, errorMethod, minBlockSize, targetCompression);
        }
        
        // Reset node counter
        nodeCount = 0;
        
        // Start building the tree from the root
        return buildQuadTreeNode(image, 0, 0, image.getWidth(), 0, errorMethod, threshold, minBlockSize);
    }
    
    /**
     * Recursive function to build a quad tree node
     */
    private static QuadNode buildQuadTreeNode(BufferedImage image, int x, int y, int size, 
                                             int depth, int errorMethod, double threshold, int minBlockSize) {
        nodeCount++;
        treeDepth = Math.max(treeDepth, depth);
        
        // Check if the block size is smaller than the minimum allowed
        if (size <= minBlockSize) {
            // Calculate average color of the block
            int avgColor = calculateAverageColor(image, x, y, size);
            return new QuadNode(x, y, size, avgColor, true);
        }
        
        // Calculate error using the selected method
        double error = calculateError(image, x, y, size, errorMethod);
        
        // If error is below threshold, this is a leaf node
        if (error <= threshold) {
            int avgColor = calculateAverageColor(image, x, y, size);
            return new QuadNode(x, y, size, avgColor, true);
        }
        
        // Otherwise, split into four quadrants
        int halfSize = size / 2;
        
        QuadNode topLeft = buildQuadTreeNode(image, x, y, halfSize, depth + 1, 
                                            errorMethod, threshold, minBlockSize);
        
        QuadNode topRight = buildQuadTreeNode(image, x + halfSize, y, halfSize, depth + 1, 
                                             errorMethod, threshold, minBlockSize);
        
        QuadNode bottomLeft = buildQuadTreeNode(image, x, y + halfSize, halfSize, depth + 1, 
                                               errorMethod, threshold, minBlockSize);
        
        QuadNode bottomRight = buildQuadTreeNode(image, x + halfSize, y + halfSize, halfSize, depth + 1, 
                                                errorMethod, threshold, minBlockSize);
        
        return new QuadNode(x, y, size, 0, false, topLeft, topRight, bottomLeft, bottomRight);
    }
    
    /**
     * Find optimal threshold to achieve target compression ratio (Bonus)
     */
    private static double findOptimalThreshold(BufferedImage image, int errorMethod, 
                                              int minBlockSize, double targetCompression) {
        // Simple binary search to find threshold that achieves target compression
        double low = 0;
        double high = 255; // Max possible error for RGB
        double bestThreshold = 0;
        double bestDiff = Double.MAX_VALUE;
        
        for (int i = 0; i < 10; i++) { // 10 iterations should be enough for approximation
            double mid = (low + high) / 2;
            
            // Reset counters
            nodeCount = 0;
            treeDepth = 0;
            
            // Build tree with this threshold
            QuadNode root = buildQuadTreeNode(image, 0, 0, image.getWidth(), 0, 
                                             errorMethod, mid, minBlockSize);
            
            // Calculate theoretical compression ratio
            int pixelCount = image.getWidth() * image.getHeight();
            double compressionRatio = 1.0 - ((double) nodeCount / pixelCount);
            
            double diff = Math.abs(compressionRatio - targetCompression);
            
            if (diff < bestDiff) {
                bestDiff = diff;
                bestThreshold = mid;
            }
            
            if (compressionRatio < targetCompression) {
                high = mid;
            } else {
                low = mid;
            }
        }
        
        System.out.println("Adjusted threshold to " + bestThreshold + " for target compression of " + targetCompression);
        return bestThreshold;
    }
    
    /**
     * Calculate error based on the selected method
     */
    private static double calculateError(BufferedImage image, int x, int y, int size, int method) {
        switch (method) {
            case VARIANCE:
                return calculateVariance(image, x, y, size);
            case MAD:
                return calculateMAD(image, x, y, size);
            case MAX_PIXEL_DIFF:
                return calculateMaxPixelDiff(image, x, y, size);
            case ENTROPY:
                return calculateEntropy(image, x, y, size);
            case SSIM:
                return calculateSSIM(image, x, y, size);
            default:
                return calculateVariance(image, x, y, size); // Default to variance
        }
    }
    
    /**
     * Calculate variance across RGB channels
     */
    private static double calculateVariance(BufferedImage image, int x, int y, int size) {
        int[] sums = {0, 0, 0}; // R, G, B sums
        int[] sumSquares = {0, 0, 0}; // R, G, B sum of squares
        
        // Calculate sums for mean and sum of squares for variance
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                Color color = new Color(image.getRGB(i, j));
                sums[0] += color.getRed();
                sums[1] += color.getGreen();
                sums[2] += color.getBlue();
                
                sumSquares[0] += color.getRed() * color.getRed();
                sumSquares[1] += color.getGreen() * color.getGreen();
                sumSquares[2] += color.getBlue() * color.getBlue();
            }
        }
        
        int pixelCount = size * size;
        
        // Calculate variances for each channel
        double[] variances = new double[3];
        for (int i = 0; i < 3; i++) {
            double mean = (double) sums[i] / pixelCount;
            variances[i] = (double) sumSquares[i] / pixelCount - mean * mean;
        }
        
        // Average variance across channels
        return (variances[0] + variances[1] + variances[2]) / 3.0;
    }
    
    /**
     * Calculate Mean Absolute Deviation (MAD) across RGB channels
     */
    private static double calculateMAD(BufferedImage image, int x, int y, int size) {
        int[] sums = {0, 0, 0}; // R, G, B sums
        
        // Calculate sums for mean
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                Color color = new Color(image.getRGB(i, j));
                sums[0] += color.getRed();
                sums[1] += color.getGreen();
                sums[2] += color.getBlue();
            }
        }
        
        int pixelCount = size * size;
        double[] means = {
            (double) sums[0] / pixelCount,
            (double) sums[1] / pixelCount,
            (double) sums[2] / pixelCount
        };
        
        // Calculate Mean Absolute Deviation
        double[] mads = {0, 0, 0};
        
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                Color color = new Color(image.getRGB(i, j));
                mads[0] += Math.abs(color.getRed() - means[0]);
                mads[1] += Math.abs(color.getGreen() - means[1]);
                mads[2] += Math.abs(color.getBlue() - means[2]);
            }
        }
        
        for (int i = 0; i < 3; i++) {
            mads[i] /= pixelCount;
        }
        
        // Average MAD across channels
        return (mads[0] + mads[1] + mads[2]) / 3.0;
    }
    
    /**
     * Calculate Max Pixel Difference across RGB channels
     */
    private static double calculateMaxPixelDiff(BufferedImage image, int x, int y, int size) {
        int[] mins = {255, 255, 255}; // R, G, B minimums
        int[] maxs = {0, 0, 0}; // R, G, B maximums
        
        // Find min and max values for each channel
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                Color color = new Color(image.getRGB(i, j));
                mins[0] = Math.min(mins[0], color.getRed());
                mins[1] = Math.min(mins[1], color.getGreen());
                mins[2] = Math.min(mins[2], color.getBlue());
                
                maxs[0] = Math.max(maxs[0], color.getRed());
                maxs[1] = Math.max(maxs[1], color.getGreen());
                maxs[2] = Math.max(maxs[2], color.getBlue());
            }
        }
        
        // Calculate differences
        double[] diffs = {
            maxs[0] - mins[0],
            maxs[1] - mins[1],
            maxs[2] - mins[2]
        };
        
        // Average difference across channels
        return (diffs[0] + diffs[1] + diffs[2]) / 3.0;
    }
    
    /**
     * Calculate Entropy across RGB channels
     */
    private static double calculateEntropy(BufferedImage image, int x, int y, int size) {
        // Count occurrences of each color intensity (0-255) for each channel
        int[][] counts = new int[3][256];
        
        int pixelCount = 0;
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                pixelCount++;
                Color color = new Color(image.getRGB(i, j));
                counts[0][color.getRed()]++;
                counts[1][color.getGreen()]++;
                counts[2][color.getBlue()]++;
            }
        }
        
        // Calculate entropy for each channel
        double[] entropies = {0, 0, 0};
        
        for (int channel = 0; channel < 3; channel++) {
            for (int i = 0; i < 256; i++) {
                if (counts[channel][i] > 0) {
                    double probability = (double) counts[channel][i] / pixelCount;
                    entropies[channel] -= probability * (Math.log(probability) / Math.log(2));
                }
            }
        }
        
        // Average entropy across channels
        return (entropies[0] + entropies[1] + entropies[2]) / 3.0;
    }
    
    /**
     * Calculate SSIM (Structural Similarity Index) across RGB channels (Bonus)
     */
    private static double calculateSSIM(BufferedImage image, int x, int y, int size) {
        // For simplicity, we'll approximate SSIM by comparing block to its average color
        // SSIM requires two images to compare, so we'll create a reference image with the average color
        
        int avgColor = calculateAverageColor(image, x, y, size);
        Color avgRGB = new Color(avgColor);
        
        // Constants for SSIM calculation
        double C1 = Math.pow(0.01 * 255, 2);
        double C2 = Math.pow(0.03 * 255, 2);
        
        // Calculate statistics for original block
        double[] means = {0, 0, 0}; // R, G, B means
        double[] variances = {0, 0, 0}; // R, G, B variances
        
        // First pass - calculate means
        int pixelCount = 0;
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                pixelCount++;
                Color color = new Color(image.getRGB(i, j));
                means[0] += color.getRed();
                means[1] += color.getGreen();
                means[2] += color.getBlue();
            }
        }
        
        for (int i = 0; i < 3; i++) {
            means[i] /= pixelCount;
        }
        
        // Second pass - calculate variances
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                Color color = new Color(image.getRGB(i, j));
                variances[0] += Math.pow(color.getRed() - means[0], 2);
                variances[1] += Math.pow(color.getGreen() - means[1], 2);
                variances[2] += Math.pow(color.getBlue() - means[2], 2);
            }
        }
        
        for (int i = 0; i < 3; i++) {
            variances[i] /= pixelCount;
        }
        
        // Reference image stats (constant color = avg color)
        double[] refMeans = {avgRGB.getRed(), avgRGB.getGreen(), avgRGB.getBlue()};
        // Variance is 0 for constant image
        
        // Calculate SSIM for each channel
        double[] ssims = new double[3];
        
        for (int i = 0; i < 3; i++) {
            double covariance = means[i] * refMeans[i] - means[i] * means[i]; // Approximation
            
            // SSIM formula
            double numerator = (2 * means[i] * refMeans[i] + C1) * (2 * covariance + C2);
            double denominator = (means[i] * means[i] + refMeans[i] * refMeans[i] + C1) * 
                               (variances[i] + 0 + C2); // 0 is reference variance
            
            ssims[i] = numerator / denominator;
        }
        
        // For SSIM, higher is better (1 is perfect). For error metric, we want lower to be better.
        // Transform to make it consistent with other error metrics (0 is perfect)
        return (3 - (ssims[0] + ssims[1] + ssims[2])) / 3.0;
    }
    
    /**
     * Calculate the average color of a block
     */
    private static int calculateAverageColor(BufferedImage image, int x, int y, int size) {
        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;
        
        for (int j = y; j < y + size; j++) {
            for (int i = x; i < x + size; i++) {
                // Ensure we don't go beyond image bounds
                if (i >= image.getWidth() || j >= image.getHeight()) continue;
                
                count++;
                Color color = new Color(image.getRGB(i, j));
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
            }
        }
        
        if (count == 0) return Color.WHITE.getRGB(); // Default to white if no pixels
        
        int avgR = (int) (sumR / count);
        int avgG = (int) (sumG / count);
        int avgB = (int) (sumB / count);
        
        return new Color(avgR, avgG, avgB).getRGB();
    }
    
    /**
     * Create a decompressed image from the quad tree
     */
    private static BufferedImage decompressQuadTree(QuadNode root, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        drawNode(image, root);
        return image;
    }
    
    /**
     * Recursively draw a node onto the image
     */
    private static void drawNode(BufferedImage image, QuadNode node) {
        if (node.isLeaf) {
            // For leaf nodes, fill the area with the node's color
            for (int j = node.y; j < node.y + node.size; j++) {
                for (int i = node.x; i < node.x + node.size; i++) {
                    // Ensure we don't go beyond image bounds
                    if (i < image.getWidth() && j < image.getHeight()) {
                        image.setRGB(i, j, node.color);
                    }
                }
            }
        } else {
            // For internal nodes, recursively draw the children
            if (node.topLeft != null) drawNode(image, node.topLeft);
            if (node.topRight != null) drawNode(image, node.topRight);
            if (node.bottomLeft != null) drawNode(image, node.bottomLeft);
            if (node.bottomRight != null) drawNode(image, node.bottomRight);
        }
    }
    
    /**
     * Quad tree node class
     */
    private static class QuadNode {
        int x, y, size, color;
        boolean isLeaf;
        QuadNode topLeft, topRight, bottomLeft, bottomRight;
        
        // Constructor for leaf nodes
        public QuadNode(int x, int y, int size, int color, boolean isLeaf) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.isLeaf = isLeaf;
        }
        
        // Constructor for internal nodes
        public QuadNode(int x, int y, int size, int color, boolean isLeaf,
                        QuadNode topLeft, QuadNode topRight, 
                        QuadNode bottomLeft, QuadNode bottomRight) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.isLeaf = isLeaf;
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
        }
    }
}