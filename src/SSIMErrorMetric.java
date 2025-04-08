/**
 * SSIMErrorMetric.java
 * 
 * Kelas ini mengimplementasikan metode Structural Similarity Index (SSIM)
 * untuk mengukur error dalam kompresi gambar. SSIM lebih baik dari metode
 * tradisional seperti MSE karena mempertimbangkan persepsi visual manusia.
 */
public class SSIMErrorMetric implements ErrorMetric {
    // Konstanta untuk stabilitas SSIM
    private static final double C1 = 6.5025;  // (0.01 * 255)^2
    private static final double C2 = 58.5225; // (0.03 * 255)^2
    
    // Bobot untuk masing-masing kanal RGB
    private static final double W_R = 0.299;
    private static final double W_G = 0.587;
    private static final double W_B = 0.114;
    
    /**
     * Menghitung SSIM antara blok gambar asli dan blok monoton dengan warna rata-rata
     * 
     * @param originalMatrix matriks RGB dari gambar asli
     * @param x posisi x dari region yang akan dievaluasi
     * @param y posisi y dari region yang akan dievaluasi
     * @param width lebar region
     * @param height tinggi region
     * @param avgColor warna rata-rata untuk blok monoton
     * @return nilai SSIM (1 berarti identik, 0 berarti tidak mirip)
     */
    public double calculateSSIMWithMonotoneBlock(RGBMatrix originalMatrix, 
    int x, int y, int width, int height, Pixel avgColor) {
    // Hitung SSIM untuk masing-masing kanal RGB
        double ssimR = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 0);
        double ssimG = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 1);
        double ssimB = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 2);

    // Gabungkan SSIM dari ketiga kanal dengan bobot
        return W_R * ssimR + W_G * ssimG + W_B * ssimB;
    }

    /**
    * Implementasi interface ErrorMetric yang asli, memanggil metode SSIM dengan blok monoton
    */
    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        // Hitung warna rata-rata untuk blok monoton
        Pixel avgColor = calculateAverageColor(rgbMatrix, x, y, width, height);

        // Hitung SSIM antara blok original dan blok rata-rata
        double ssim = calculateSSIMWithMonotoneBlock(rgbMatrix, x, y, width, height, avgColor);

        // Kembalikan 1-SSIM sebagai nilai error (0 berarti identik, 1 berarti berbeda total)
        return 1.0 - ssim;
    }

    /**
    * Menghitung rata-rata warna untuk region tertentu
    */
    private Pixel calculateAverageColor(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        int totalR = 0, totalG = 0, totalB = 0;
        int count = 0;

        // Iterasi semua piksel dalam region
        for (int cy = y; cy < y + height && cy < rgbMatrix.getHeight(); cy++) {
            for (int cx = x; cx < x + width && cx < rgbMatrix.getWidth(); cx++) {
                Pixel pixel = rgbMatrix.getPixel(cx, cy);
                totalR += pixel.getR();
                totalG += pixel.getG();
                totalB += pixel.getB();
                count++;
        }
    }

    // Hindari pembagian dengan nol
        if (count == 0) return new Pixel(0, 0, 0);

        // Kembalikan rata-rata warna
        return new Pixel(totalR / count, totalG / count, totalB / count);
    }
    
    // Removed duplicate method definition to resolve the compile error.
    
    /**
     * Menghitung SSIM untuk satu kanal warna (R, G, atau B)
     * 
     * @param originalMatrix matriks RGB dari gambar asli
     * @param compressedMatrix matriks RGB dari gambar terkompresi
     * @param x posisi x dari region
     * @param y posisi y dari region
     * @param width lebar region
     * @param height tinggi region
     * @param channel kanal yang akan dihitung (0=R, 1=G, 2=B)
     * @return nilai SSIM untuk kanal tersebut
     */
/**
 * Menghitung SSIM untuk satu kanal warna (R, G, atau B) 
 * dengan asumsi compressed image adalah blok monoton
 */
/**
 * Menghitung SSIM untuk satu kanal warna (R, G, atau B) 
 * dengan asumsi compressed image adalah blok monoton
 */
    private double calculateSSIMForChannel(RGBMatrix originalMatrix, int x, int y, 
                                        int width, int height, Pixel avgColor, int channel) {
        // Inisialisasi variabel statistik
        double sumX = 0;
        double sumXSquare = 0;
        int count = 0;
        
        // Nilai y adalah konstan (warna rata-rata dari avgColor)
        double meanY;
        if (channel == 0) {
            meanY = avgColor.getR();
        } else if (channel == 1) {
            meanY = avgColor.getG();
        } else { // channel == 2
            meanY = avgColor.getB();
        }
        
        // Iterasi semua piksel dalam region
        for (int cy = y; cy < y + height && cy < originalMatrix.getHeight(); cy++) {
            for (int cx = x; cx < x + width && cx < originalMatrix.getWidth(); cx++) {
                // Ambil nilai piksel dari gambar original
                Pixel pixel = originalMatrix.getPixel(cx, cy);
                double pixelX;
                
                // Pilih kanal yang sesuai
                if (channel == 0) {
                    pixelX = pixel.getR();
                } else if (channel == 1) {
                    pixelX = pixel.getG();
                } else { // channel == 2
                    pixelX = pixel.getB();
                }
                
                // Akumulasi untuk statistik
                sumX += pixelX;
                sumXSquare += pixelX * pixelX;
                count++;
            }
        }
        
        // Hindari pembagian dengan nol
        if (count == 0) return 1.0;
        
        // Hitung statistik
        double meanX = sumX / count;
        double varianceX = (sumXSquare / count) - (meanX * meanX);
        
        // Hitung standar deviasi
        double stdDevX = Math.sqrt(Math.max(0, varianceX));
        
        // Hitung SSIM yang disederhanakan karena variansi Y = 0 dan covariance = 0
        double numerator = (2 * meanX * meanY + C1) * C2;
        double denominator = (meanX * meanX + meanY * meanY + C1) * (stdDevX * stdDevX + C2);
        
        // Hindari pembagian dengan nol
        if (denominator == 0) return 1.0;
        
        return numerator / denominator;
    }
    
    /**
     * Helper method untuk mengambil nilai dari kanal RGB tertentu
     */
    private double getChannelValue(Pixel pixel, int channel) {
        switch (channel) {
            case 0: return pixel.getR();
            case 1: return pixel.getG();
            case 2: return pixel.getB();
            default: throw new IllegalArgumentException("Invalid channel index: " + channel);
        }
    }
    
    /**
     * Mendapatkan nama dari metode error ini untuk ditampilkan
     * 
     * @return nama metode error
     */
    @Override
    public String getName() {
        return "SSIM (Structural Similarity Index)";
    }
  }