import java.awt.image.BufferedImage;
public class CompressedImage {
    private BufferedImage image;
    private double compressionRate;
    private long compressedSizeInBytes;
    private long originalSizeInBytes;
    private QuadTree quadTree;

    public CompressedImage(BufferedImage image, double compressionRate, long compressedSizeInBytes, long originalSizeInBytes, QuadTree qt) {
        this.image = image;
        this.compressionRate = compressionRate;
        this.compressedSizeInBytes = compressedSizeInBytes;
        this.originalSizeInBytes = originalSizeInBytes;
        this.quadTree = qt;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getCompressionRate() {
        return compressionRate;
    }

    public long getCompressedSizeInBytes() {
        return compressedSizeInBytes;
    }

    public long getOriginalSizeInBytes() {
        return originalSizeInBytes;
    }

    public QuadTree getQuadTree() {
        return quadTree;
    }
}