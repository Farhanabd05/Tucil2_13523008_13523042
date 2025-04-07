/**
 * VarianceErrorMetric.java
 * 
 * VarianceErrorMetric adalah kelas yang mendefinisikan metode untuk menghitung error (variansi)
 * dari piksel dalam proses kompresi quadtree
 * metode ini menghitung variansi untuk setiap kanal warna (R, G, B) dan
 * menggabungkannya menjadi 1 nilai
 * 
 * Formula:
 * σc² = (1/N) * Σ(Pi,c - μc)²
 * σRGB² = (σR² + σG² + σB²) / 3
 */
public class VarianceErrorMetric implements ErrorMetric {
    
    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        // menghitung rara rata RGB untuk area yang direpresentasikan node
        double avgR = 0, avgG = 0, avgB = 0, cnt = 0;
        double[] avgRGB = rgbMatrix.calculateAverageRGB(x, y, width, height);
        avgR = avgRGB[0]; avgG = avgRGB[1]; avgB = avgRGB[2];
        // menghitung variansi untuk setiap kanal warna
        double varR = 0, varG = 0, varB = 0;
        for (int y1 = y; y1 < y + height; y1++) {
            for (int x1 = x; x1 < x + width; x1++) {
                Pixel col = rgbMatrix.getPixel(x1, y1);
                double diffR = col.getR() - avgR;
                double diffG = col.getG() - avgG;
                double diffB = col.getB() - avgB;
                varR += diffR * diffR;
                varG += diffG * diffG;
                varB += diffB * diffB;
                cnt++;
            }
        }

        varR /= cnt;
        varG /= cnt;
        varB /= cnt;
        
        // menggabungkan variansi untuk setiap kanal warna menjadi 1 nilai
        return (varR + varG + varB) / 3.0;
    }

    @Override
    public String getName() {
        return "Variance";
    }
}

/*
*/