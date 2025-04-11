import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CompressionController {
    /**
     * Mengembalikan nilai default threshold berdasarkan jenis ErrorMetric.
     */
    public static double getDefaultThreshold(ErrorMetric errorMetric) {
        String metricName = errorMetric.getName().toLowerCase();
        if (metricName.contains("Variance")) {
            return 2000;
        }
        else if (metricName.contains("Max Pixel Difference")) {
            return 15.0;
        } else if (metricName.contains("mad")) {
            return 10.0;
        }
        else if (metricName.contains("Entropy")) {
            return 5.0;
        } else if (metricName.contains("SSIM")) {
            return 0.1;
        } else {
            // fallback: gunakan 10 sebagai default
            return 10.0;
        }
    }
    
    /**
     * Membuat kompresi gambar dengan threshold tertentu.
     * Metode ini membuat QuadTree baru, membangun tree, lalu mengembalikan
     * BufferedImage hasil kompresi.
     */
    public static BufferedImage compressImage(RGBMatrix rgbMatrix, ErrorMetric errorMetric, 
                                                double threshold, int minBlockSize) {
        // Gunakan copy agar rgbMatrix asli tidak termodifikasi
        QuadTree qt = new QuadTree(rgbMatrix.copy(), errorMetric, threshold, minBlockSize);
        qt.buildTree();
        return OutputHandler.convertToBufferedImage(qt.getRGBMatrix());
    }
    
    /**
     * Menghitung ukuran (dalam byte) dari BufferedImage menggunakan format tertentu.
     */
    private static long getImageSizeInBytes(BufferedImage image, String formatName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            baos.flush();
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Melakukan kompresi gambar dengan target persentase kompresi.
     * Jika targetCompression == 0, langsung mengembalikan hasil kompresi dengan default threshold.
     * 
     * @param rgbMatrix         Matriks RGB gambar asli
     * @param errorMetric       Metode pengukuran error yang digunakan
     * @param minBlockSize      Ukuran blok minimum
     * @param targetCompression Target persentase kompresi (misalnya 50.0 untuk 50%)
     * @param imageFormat       Format gambar (misalnya "png")
     * @return                  BufferedImage hasil kompresi dengan threshold yang disesuaikan
     */
    public CompressedImage compressWithTarget(RGBMatrix rgbMatrix, ErrorMetric errorMetric, 
                                     int minBlockSize, double targetCompression,
                                     String imageFormat) {

    double defaultThreshold = getDefaultThreshold(errorMetric);
        
        // Inisialisasi batas untuk binary search
        double lowThreshold = 0.0;
        double highThreshold;
        String metricName = errorMetric.toString().toLowerCase();
        if (metricName.contains("variance")) {
            highThreshold = 16256.25;
        } else if (metricName.contains("mad")) {
            highThreshold = 127.5;
        } else if (metricName.contains("mpd")) {
            highThreshold = 255;
        }
        else if (metricName.contains("entropy")) {
            highThreshold = 8;
        } else if (metricName.contains("ssim")) {
            highThreshold = 1.0;
        } else {
            highThreshold = defaultThreshold * 2;
        }
        
        double bestThreshold = defaultThreshold;
        double tolerance = 0.0001;  // margin error 1%
        BufferedImage compressedImage = null;
        
        // Ambil ukuran gambar asli (sebagai acuan)
        BufferedImage originalImage = OutputHandler.convertToBufferedImage(rgbMatrix);
        long originalSizeInBytes = getImageSizeInBytes(originalImage, imageFormat);
        System.out.println("[DEBUG] Original image size: " + originalSizeInBytes + " bytes");
        double currentCompression = 0;
        boolean isEarlyStop = false;
        int counterForSame = 0;
        double prevCompression = 0;
        // Binary search untuk menemukan threshold terbaik
        while (true) {
            

            double midThreshold = (lowThreshold + highThreshold) / 2.0;
            compressedImage = compressImage(rgbMatrix, errorMetric, midThreshold, minBlockSize);
            long compressedSize = getImageSizeInBytes(compressedImage, imageFormat);
            currentCompression = (1 - ((double) compressedSize / originalSizeInBytes)) * 100.0;

            if (prevCompression == currentCompression) {
                counterForSame++;
            } else {
                counterForSame = 0; 
            }
            prevCompression = currentCompression;
            // (1 - (3830340/15933389))*100
            // Jika kompresi kurang tinggi dari target, artinya perlu mengurangi pembagian (meningkatkan threshold)
            if (currentCompression < targetCompression) {
                lowThreshold = midThreshold;
            } else {
                // Jika kompresi terlalu tinggi, threshold harus diturunkan
                highThreshold = midThreshold;
            }
            
            // if (currentCompression < 0) {
            //     currentCompression = 0;
            // } else if (currentCompression > 100) {
            //     currentCompression = 100;
            // }

            if (counterForSame >= 3 && Math.abs(currentCompression - targetCompression) <= tolerance) {
                isEarlyStop = true;
            }
            bestThreshold = midThreshold;
            System.out.println("[DEBUG] Current compression: " + currentCompression + "%");
            System.out.println("[DEBUG] Current threshold: " + bestThreshold);
            System.out.println("[DEBUG] Current image size: " + compressedSize + " bytes");
            if ((highThreshold - lowThreshold) <= tolerance || isEarlyStop) {
                return new CompressedImage(compressedImage, currentCompression, compressedSize, originalSizeInBytes);
            }
        }
        
    }
// }
}

