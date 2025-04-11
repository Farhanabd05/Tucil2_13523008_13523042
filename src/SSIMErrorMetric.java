public class SSIMErrorMetric implements ErrorMetric {
    private static final double C1 = 6.5025;  // (0.01 * 255)^2
    private static final double C2 = 58.5225; // (0.03 * 255)^2
    
    private static final double W_R = 0.299;
    private static final double W_G = 0.587;
    private static final double W_B = 0.114;
    
    public double calculateSSIMWithMonotoneBlock(RGBMatrix originalMatrix, 
    int x, int y, int width, int height, Pixel avgColor) {
        double ssimR = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 0);
        double ssimG = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 1);
        double ssimB = calculateSSIMForChannel(originalMatrix, x, y, width, height, avgColor, 2);
        return W_R * ssimR + W_G * ssimG + W_B * ssimB;
    }

    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        Pixel avgColor = calculateAverageColor(rgbMatrix, x, y, width, height);

        double ssim = calculateSSIMWithMonotoneBlock(rgbMatrix, x, y, width, height, avgColor);

        return 1.0 - ssim;
    }

    private Pixel calculateAverageColor(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        int totalR = 0, totalG = 0, totalB = 0;
        int count = 0;

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

        return new Pixel(totalR / count, totalG / count, totalB / count);
    }

    private double calculateSSIMForChannel(RGBMatrix originalMatrix, int x, int y, 
                                        int width, int height, Pixel avgColor, int channel) {
        double sumX = 0;
        double sumXSquare = 0;
        int count = 0;
        
        double meanY;
        if (channel == 0) {
            meanY = avgColor.getR();
        } else if (channel == 1) {
            meanY = avgColor.getG();
        } else { 
            meanY = avgColor.getB();
        }
        
        for (int cy = y; cy < y + height && cy < originalMatrix.getHeight(); cy++) {
            for (int cx = x; cx < x + width && cx < originalMatrix.getWidth(); cx++) {
                Pixel pixel = originalMatrix.getPixel(cx, cy);
                double pixelX;
                
                if (channel == 0) {
                    pixelX = pixel.getR();
                } else if (channel == 1) {
                    pixelX = pixel.getG();
                } else {
                    pixelX = pixel.getB();
                }
                
                sumX += pixelX;
                sumXSquare += pixelX * pixelX;
                count++;
            }
        }
        
        if (count == 0) return 1.0;
        
        double meanX = sumX / count;
        double varianceX = (sumXSquare / count) - (meanX * meanX);
        
        double stdDevX = Math.sqrt(Math.max(0, varianceX));
        
        double numerator = (2 * meanX * meanY + C1) * C2;
        double denominator = (meanX * meanX + meanY * meanY + C1) * (stdDevX * stdDevX + C2);
        
        if (denominator == 0) return 1.0;
        
        return numerator / denominator;
    }
    
    private double getChannelValue(Pixel pixel, int channel) {
        switch (channel) {
            case 0: return pixel.getR();
            case 1: return pixel.getG();
            case 2: return pixel.getB();
            default: throw new IllegalArgumentException("Invalid channel index: " + channel);
        }
    }
    
    @Override
    public String getName() {
        return "SSIM (Structural Similarity Index)";
    }
}