/*
 * Combined ErrorMetric Implementation
 * 
 * This file contains all the components related to error metrics used in quadtree image compression.
 * It includes the ErrorMetric interface and all its implementations (Variance, MAD, MaxPixelDifference, Entropy),
 * as well as the factory for creating appropriate error metric objects.
 */

/**
 * ErrorMetric.java
 * 
 * Interface yang mendefinisikan metode untuk menghitung error (variansi)
 * dari piksel dalam proses kompresi quadtree. Implementasi interface ini akan menyediakan berbagai metode pengukuran error
 * seperti variance, MAD, Max Pixel Diff, entropy dan SSIM.
 */
public interface ErrorMetric {
    /**
     * Menghitung error dari area piksel tertentu pada RGB matrix
     * 
     * @param rgbMatrix matriks RGB
     * @param x posisi x awal area
     * @param y posisi y awal area
     * @param width lebar area
     * @param height tinggi area
     * @return nilai error
     */
    double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height);

    /**
     * Mendapatkan nama metode pengukuran error
     * 
     * @return nama metode
     */
    String getName();
}