
public class SSIMErrorMetric implements ErrorMetric {
    // Konstanta untuk stabilitas SSIM
    private static final double C1 = 6.5025;  // (0.01 * 255)^2
    private static final double C2 = 58.5225; // (0.03 * 255)^2
    
    // Bobot untuk masing-masing kanal RGB
    private static final double W_R = 0.299;
    private static final double W_G = 0.587;
    private static final double W_B = 0.114;

    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        // Hitung warna rata-rata untuk membuat blok monoton
        Pixel avgColor = calculateAverageColor(rgbMatrix, x, y, width, height);
        
        RGBMatrix monotoneMatrix = createMonotoneMatrix(avgColor, width, height);
        
        double ssim = calculateSSIM(rgbMatrix, monotoneMatrix, x, y, x, y, width, height);
        
        return 1.0 - ssim;
    }
    
    private RGBMatrix createMonotoneMatrix(Pixel color, int width, int height) {
        RGBMatrix monotoneMatrix = new RGBMatrix(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                monotoneMatrix.setPixel(x, y, color.getRGB());
            }
        }
        return monotoneMatrix;
    }
    

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
        
        if (count == 0) return new Pixel(0, 0, 0);
        
        // Kembalikan rata-rata warna
        return new Pixel(totalR / count, totalG / count, totalB / count);
    }
    
    private double calculateMean(RGBMatrix matrix, int startX, int startY, int width, int height, int channel) {
        double sum = 0.0;
        int count = 0;
        
        for (int y = startY; y < startY + height && y < matrix.getHeight(); y++) {
            for (int x = startX; x < startX + width && x < matrix.getWidth(); x++) {
                Pixel pixel = matrix.getPixel(x, y);
                sum += getChannelValue(pixel, channel);
                count++;
            }
        }
        
        return (count == 0) ? 0.0 : sum / count;
    }
    
    private double calculateVariance(RGBMatrix matrix, int startX, int startY, int width, int height, int channel, double mean) {
        double sumSquared = 0.0;
        int count = 0;
        
        for (int y = startY; y < startY + height && y < matrix.getHeight(); y++) {
            for (int x = startX; x < startX + width && x < matrix.getWidth(); x++) {
                Pixel pixel = matrix.getPixel(x, y);
                double value = getChannelValue(pixel, channel);
                double diff = value - mean;
                sumSquared += diff * diff;
                count++;
            }
        }
        
        return (count == 0) ? 0.0 : sumSquared / count;
    }
    
    private double calculateCovariance(RGBMatrix matrix1, RGBMatrix matrix2, 
                                     int startX1, int startY1, int startX2, int startY2, 
                                     int width, int height, int channel, 
                                     double mean1, double mean2) {
        double sumCovariance = 0.0;
        int count = 0;
        
        for (int dy = 0; dy < height; dy++) {
            int y1 = startY1 + dy;
            int y2 = startY2 + dy;
            
            if (y1 >= matrix1.getHeight() || y2 >= matrix2.getHeight()) {
                continue;
            }
            
            for (int dx = 0; dx < width; dx++) {
                int x1 = startX1 + dx;
                int x2 = startX2 + dx;
                
                if (x1 >= matrix1.getWidth() || x2 >= matrix2.getWidth()) {
                    continue;
                }
                
                Pixel pixel1 = matrix1.getPixel(x1, y1);
                Pixel pixel2 = matrix2.getPixel(x2, y2);
                
                double value1 = getChannelValue(pixel1, channel);
                double value2 = getChannelValue(pixel2, channel);
                
                sumCovariance += (value1 - mean1) * (value2 - mean2);
                count++;
            }
        }
        
        return (count == 0) ? 0.0 : sumCovariance / count;
    }
    
    private double getChannelValue(Pixel pixel, int channel) {
        switch (channel) {
            case 0: return pixel.getR();
            case 1: return pixel.getG();
            case 2: return pixel.getB();
            default: throw new IllegalArgumentException("Invalid channel index: " + channel);
        }
    }
    
    public double calculateSSIM(RGBMatrix matrix1, RGBMatrix matrix2,
                              int startX1, int startY1, int startX2, int startY2,
                              int width, int height) {
        // Hitung SSIM untuk setiap kanal
        double ssimR = calculateSSIMForChannel(matrix1, matrix2, startX1, startY1, startX2, startY2, width, height, 0);
        double ssimG = calculateSSIMForChannel(matrix1, matrix2, startX1, startY1, startX2, startY2, width, height, 1);
        double ssimB = calculateSSIMForChannel(matrix1, matrix2, startX1, startY1, startX2, startY2, width, height, 2);
        
        // Gabungkan SSIM dari ketiga kanal dengan bobot
        return W_R * ssimR + W_G * ssimG + W_B * ssimB;
    }
    

    private double calculateSSIMForChannel(RGBMatrix matrix1, RGBMatrix matrix2,
                                         int startX1, int startY1, int startX2, int startY2,
                                         int width, int height, int channel) {
        // Hitung statistik dasar
        double mean1 = calculateMean(matrix1, startX1, startY1, width, height, channel);
        double mean2 = calculateMean(matrix2, startX2, startY2, width, height, channel);
        double variance1 = calculateVariance(matrix1, startX1, startY1, width, height, channel, mean1);
        double variance2 = calculateVariance(matrix2, startX2, startY2, width, height, channel, mean2);
        double covariance = calculateCovariance(matrix1, matrix2, startX1, startY1, startX2, startY2, 
                                              width, height, channel, mean1, mean2);
        
        // Hitung SSIM menggunakan rumus yang diberikan
        double numerator = (2 * mean1 * mean2 + C1) * (2 * covariance + C2);
        double denominator = (mean1 * mean1 + mean2 * mean2 + C1) * (variance1 + variance2 + C2);
        
        // Hindari pembagian dengan nol
        if (denominator == 0.0) return 1.0;
        
        return numerator / denominator;
    }
    

    @Override
    public String getName() {
        return "SSIM (Structural Similarity Index)";
    }
}