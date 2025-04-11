/**
 * MADc = (1/N) * Σ|Pi,c - μc|
 * MADRGB = (MADR + MADG + MADB) / 3
 */
public class MADErrorMetric implements ErrorMetric {
    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        int cnt = 0;
        double avgR = 0, avgG = 0, avgB = 0;
        double[] avgRGB = rgbMatrix.calculateAverageRGB(x, y, width, height);
        avgR = avgRGB[0]; avgG = avgRGB[1]; avgB = avgRGB[2];
        double madR = 0, madG = 0, madB = 0;
        for (int y1 = y; y1 < y + height; y1++) {
            for (int x1 = x; x1 < x + width; x1++) {
                Pixel col = rgbMatrix.getPixel(x1, y1);
                madR += Math.abs(col.getR() - avgR);
                madG += Math.abs(col.getG() - avgG);
                madB += Math.abs(col.getB() - avgB);
                cnt++;
            }
        }
        madR /= cnt;
        madG /= cnt;
        madB /= cnt;
        
        return (madR + madG + madB) / 3.0;
    }

    @Override
    public String getName() {
        return "MADErrorMetric";
    }
}