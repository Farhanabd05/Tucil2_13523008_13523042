/**
 * Dc = max(Pi,c) - min(Pi,c)
 * DRGB = (DR + DG + DB) / 3
 */
public class MaxPixelDifferenceErrorMetric implements ErrorMetric {
    
    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        double maxR = 0, maxG = 0, maxB = 0, minR = 255, minG = 255, minB = 255;
        
        for (int y1 = y; y1 < y + height; y1++) {
            for (int x1 = x; x1 < x + width; x1++) {
                if (x1 >= rgbMatrix.getWidth() || y1 >= rgbMatrix.getHeight()) continue;
                Pixel col = rgbMatrix.getPixel(x1, y1);
                
                maxR = Math.max(maxR, col.getR());
                maxG = Math.max(maxG, col.getG());
                maxB = Math.max(maxB, col.getB());

                minR = Math.min(minR, col.getR());
                minG = Math.min(minG, col.getG());
                minB = Math.min(minB, col.getB());
            }
        }

        double maxDiffR = maxR - minR;
        double maxDiffG = maxG - minG;
        double maxDiffB = maxB - minB;

        return (maxDiffR + maxDiffG + maxDiffB) / 3.0;
    }
    
    @Override
    public String getName() {
        return "Max Pixel Difference";
    }
}