/*
 * QuadTree.java
 * 
 * kelas ini merepresentasikan struktur pohon quadtree
 * kelas ini menglola proses pembagian area gambar menjadi blok blok berdasarkan
 * threshold error dan ukuran minimum blok.
*/

import java.io.IOException;
import java.util.List;
import java.awt.image.BufferedImage;


public class QuadTree {
    // root node dari quadtree
    private QuadTreeNode root;

    // Matriks RGB dari gambar asli
    private RGBMatrix rgbMatrix;

    // metode pengukuran error
    private ErrorMetric errorMetric;

    // threshold error untuk misahin blok
    private double threshold;

    // ukuran minimum blok dalam piksel
    private int minBlockSize;

    // statistic quadtree nodecount dan maxdepth untuk debugging
    private int nodeCount, maxDepth;

    // Tambahkan di bagian atribut kelas
    // private GifRecorder gifRecorder;

    /*
     * Konstruktor untuk membuat QuadTree Baru.
     * 
     * @param rgbMatrix Matriks RGB dari gambar asli
     * @param errorMetric metode pengukuran error
     * @param threshold threshold error untuk misahin blok
     * @param minBlockSize ukuran minimum blok dalam piksel
    */

    public QuadTree(RGBMatrix rgbMatrix, ErrorMetric errorMetric, double threshold, int minBlockSize) {
        this.rgbMatrix = rgbMatrix;
        this.errorMetric = errorMetric;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.nodeCount = 0;
        this.maxDepth = 0;

        // buat root node untuk seluruh gambar
        this.root = new QuadTreeNode(0, 0, rgbMatrix.getWidth(), rgbMatrix.getHeight());
        this.nodeCount++;
    }

    public void buildTree() {
        buildTreeRecursive(root, 0);
        applyColorToMatrix();
    }

    /*
     * membangun quadtree secara rekursif
     * 
     * @param node Node yang sedang diproses
     * @param depth kedalaman node saat ini
    */

    private void buildTreeRecursive(QuadTreeNode node, int depth) {
        // update kedalam maksimum
        maxDepth = Math.max(maxDepth, depth);

        // hitung nilai rata-rata warna node ini
        node.calculateAverageColor(rgbMatrix);

        // cek apakah blok ini perlu dibagi lagi
        if (shouldSplit(node)) {
            // bagi node menjadi 4 child
            node.split();

            // proses masing-masinf child secara rekursif
            buildTreeRecursive(node.getTopLeft(), depth + 1);
            buildTreeRecursive(node.getTopRight(), depth + 1);
            buildTreeRecursive(node.getBottomLeft(), depth + 1);
            buildTreeRecursive(node.getBottomRight(), depth + 1);

            nodeCount += 4;
        }
        // if (gifRecorder != null && node.isLeaf()) {
        //     // Rekam frame untuk GIF setiap kali sebuah node daun dibuat
        //     gifRecorder.addFrame(rgbMatrix);
        // }
    }

    /*
     * Mengecek apakah node ini perlu dibagi lagi berdasarkan error dan ukuran minimum.
     * 
     * @param node Node yang sedang diproses
     * @return true jika node ini perlu dibagi lagi, false jika tidak
    */

    private boolean shouldSplit(QuadTreeNode node) {
        // cek apakah ukuran blok setelah dibagi lebih kecil dari ukuran minimum blok
        if (node.getWidth() * node.getHeight() < minBlockSize) return false;

        // hitung error untuk node ini
        double error = errorMetric.calculateError(rgbMatrix, node.getX(), node.getY(), node.getWidth(), node.getHeight());

        // bagi jika error lebih besar dari threshold
        return error > threshold;
    }

    // menerapkan nilai warna rata-rata dari setiap leaf node ke matriks RGB

    private void applyColorToMatrix() {
        applyColorRecursive(root);
    }

    /*
     * menerapkan nilai warna rata-rata dari setiap leaf node ke matriks RGB secara rekursif
     * 
     * @param node Node yang sedang diproses
    */

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

    /*
     * Get the total number of nodes in the quadtree.
     * 
     * @return The total number of nodes in the quadtree.
    */
    public int getNodeCount() {
        return nodeCount;
    }

    /*
     * Get the maximum depth of the quadtree.
     * 
     * @return The maximum depth of the quadtree.
    */
    public int getMaxDepth() {
        return maxDepth;
    }

    public RGBMatrix getRGBMatrix() {
        return this.rgbMatrix;
    }

    // Tambahkan metode setter
    // public void setGifRecorder(GifRecorder gifRecorder) {
    //     this.gifRecorder = gifRecorder;
    // }

    // Modified QuadTree.java method for better GIF frame creation
public void createGifFramesRecursive(QuadTreeNode root, List<BufferedImage> frames, int maxDepth) throws IOException {
    // Create a frame showing the original image first
    RGBMatrix originalMatrix = this.rgbMatrix.copy();
    frames.add(OutputHandler.convertToBufferedImage(originalMatrix));
    
    // For each depth level, create a visualization frame
    for (int depth = 0; depth <= maxDepth; depth++) {
        // Create a fresh copy of the matrix for this depth level
        RGBMatrix depthMatrix = this.rgbMatrix.copy();
        
        // Apply colors based on current depth level
        applyColorsAtDepth(root, depthMatrix, depth, 0);
        
        // Add the rendered matrix as a frame
        BufferedImage frame = OutputHandler.convertToBufferedImage(depthMatrix);
        frames.add(frame);
    }
}

// Modified method to apply colors at specific depth
private void applyColorsAtDepth(QuadTreeNode node, RGBMatrix matrix, int targetDepth, int currentDepth) {
    // Base case: we've reached a leaf node
    if (node == null) {
        return;
    }
    
    // Case 1: Current node is at target depth, or is a leaf at a lesser depth
    if (currentDepth == targetDepth || (node.isLeaf() && currentDepth < targetDepth)) {
        // Calculate average color if not already calculated
        if (node.getAverageColor() == null) {
            node.calculateAverageColor(rgbMatrix);
        }
        
        // Apply average color to all pixels in this node's region
        for (int y = node.getY(); y < node.getY() + node.getHeight(); y++) {
            for (int x = node.getX(); x < node.getX() + node.getWidth(); x++) {
                if (x < matrix.getWidth() && y < matrix.getHeight()) {
                    matrix.setPixel(x, y, node.getAverageColor().getRGB());
                }
            }
        }
    } 
    // Case 2: Not yet at target depth and has children, process children
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
