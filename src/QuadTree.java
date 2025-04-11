import java.awt.image.BufferedImage;
public class QuadTree {

    private QuadTreeNode root;
    private RGBMatrix rgbMatrix;
    private ErrorMetric errorMetric;
    private double threshold;
    private int minBlockSize;
    private int nodeCount, maxDepth;

    public QuadTree(RGBMatrix rgbMatrix, ErrorMetric errorMetric, double threshold, int minBlockSize) {
        this.rgbMatrix = rgbMatrix;
        this.errorMetric = errorMetric;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.nodeCount = 0;
        this.maxDepth = 0;

        this.root = new QuadTreeNode(0, 0, rgbMatrix.getWidth(), rgbMatrix.getHeight());
        this.nodeCount++;
    }

    public void buildTree() {
        buildTreeRecursive(root, 0);
        applyColorToMatrix();
    }

    private void buildTreeRecursive(QuadTreeNode node, int depth) {
        maxDepth = Math.max(maxDepth, depth);

        node.calculateAverageColor(rgbMatrix);
        
        if (shouldSplit(node)) {
            node.split();

            buildTreeRecursive(node.getTopLeft(), depth + 1);
            buildTreeRecursive(node.getTopRight(), depth + 1);
            buildTreeRecursive(node.getBottomLeft(), depth + 1);
            buildTreeRecursive(node.getBottomRight(), depth + 1);

            nodeCount += 4;
        }
    }

    private boolean shouldSplit(QuadTreeNode node) {
        if (node.getWidth() * node.getHeight() < minBlockSize) return false;

        double error = errorMetric.calculateError(rgbMatrix, node.getX(), node.getY(), node.getWidth(), node.getHeight());

        return error > threshold;
    }

    public void buildTree(double currentThreshold, BufferedImage image) {
        buildTreeRecursive(root, 0, currentThreshold, image);
        applyColorToMatrix();
    }
    
    private void buildTreeRecursive(QuadTreeNode node, int depth, double currentThreshold, BufferedImage image) {
        maxDepth = Math.max(maxDepth, depth);
    
        node.calculateAverageColor(rgbMatrix);
    
        if (shouldSplit(node, currentThreshold, image)) {
            node.split();
    
            buildTreeRecursive(node.getTopLeft(), depth + 1, currentThreshold, image);
            buildTreeRecursive(node.getTopRight(), depth + 1, currentThreshold, image);
            buildTreeRecursive(node.getBottomLeft(), depth + 1, currentThreshold, image);
            buildTreeRecursive(node.getBottomRight(), depth + 1, currentThreshold, image);
    
            nodeCount += 4;
        } else {
            applyColorRecursive(node);
        }
    }
    
    private boolean shouldSplit(QuadTreeNode node, double currentThreshold, BufferedImage image) {
        if (node.getWidth() * node.getHeight() < minBlockSize) return false;
    
        double error = errorMetric.calculateError(rgbMatrix, node.getX(), node.getY(), node.getWidth(), node.getHeight());
    
        return error > currentThreshold;
    }
    

    private void applyColorToMatrix() {
        applyColorRecursive(root);
    }

    private void applyColorRecursive(QuadTreeNode node) {
        if (node.isLeaf()) {
            for (int y = node.getY(); y < node.getY() + node.getHeight(); y++) {
                for (int x = node.getX(); x < node.getX() + node.getWidth(); x++) {
                    if (x < rgbMatrix.getWidth() && y < rgbMatrix.getHeight()) {
                        rgbMatrix.setPixel(x, y, node.getAverageColor().getRGB());
                    }
                }
            }
        } else {
            applyColorRecursive(node.getTopLeft());
            applyColorRecursive(node.getTopRight());
            applyColorRecursive(node.getBottomLeft());
            applyColorRecursive(node.getBottomRight());
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public RGBMatrix getRGBMatrix() {
        return this.rgbMatrix;
    }

    public void applyColorsAtDepth(QuadTreeNode node, RGBMatrix matrix, int targetDepth, int currentDepth) {
        if (node == null) return;
        
        if (currentDepth >= targetDepth || node.isLeaf()) {
            for (int y = node.getY(); y < node.getY() + node.getHeight() && y < matrix.getHeight(); y++) {
                for (int x = node.getX(); x < node.getX() + node.getWidth() && x < matrix.getWidth(); x++) {
                    if (node.getAverageColor() == null) matrix.setPixel(x, y, 0);
                    matrix.setPixel(x, y, node.getAverageColor().getRGB());
                }
            }
        } 
        else if (currentDepth < targetDepth && !node.isLeaf()) {
            applyColorsAtDepth(node.getTopLeft(), matrix, targetDepth, currentDepth + 1);
            applyColorsAtDepth(node.getTopRight(), matrix, targetDepth, currentDepth + 1);
            applyColorsAtDepth(node.getBottomLeft(), matrix, targetDepth, currentDepth + 1);
            applyColorsAtDepth(node.getBottomRight(), matrix, targetDepth, currentDepth + 1);
        }
}

    public QuadTreeNode getRoot() {
        return this.root;
    }
}
