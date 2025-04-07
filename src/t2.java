/**
 * EntropyErrorMetric.java
 * 
 * Implementasi ErrorMetric untuk menghitung error menggunakan metode Entropy.
 * Metode ini menghitung entropi informasi untuk setiap kanal warna (R,G,B) 
 * dan menggabungkannya menjadi satu nilai.
 * 
 * Formula:
 * Hc = -Σ(Pc(i) * log2(Pc(i)))
 * HRGB = (HR + HG + HB) / 3
 */
class EntropyErrorMetric implements ErrorMetric {
    @Override
    public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
        // Array untuk menyimpan frekuensi nilai piksel (0-255) untuk setiap kanal warna
        int[] freqR = new int[256];
        int[] freqG = new int[256];
        int[] freqB = new int[256];
        int cnt = 0;
        // Menghitung frekuensi nilai piksel untuk setiap kanal warna
        for (int y1 = y; y1 < y + height; y1++) {
            for (int x1 = x; x1 < x + width; x1++) {
                if (x1 >= rgbMatrix.getWidth() || y1 >= rgbMatrix.getHeight()) continue;
                Pixel col = rgbMatrix.getPixel(x1, y1);
                freqR[col.getR()]++;
                freqG[col.getG()]++;
                freqB[col.getB()]++;
                cnt++;
            }
        }

        // Menghitung entropi informasi untuk setiap kanal warna
        double entropyR = 0, entropyG = 0, entropyB = 0;
        for (int i = 0; i < 256; i++) {
            double piR = (double) freqR[i] / cnt;
            double piG = (double) freqG[i] / cnt;
            double piB = (double) freqB[i] / cnt;
            if (piR > 0) entropyR -= piR * Math.log(piR) / Math.log(2);
            if (piG > 0) entropyG -= piG * Math.log(piG) / Math.log(2);
            if (piB > 0) entropyB -= piB * Math.log(piB) / Math.log(2);
        }

        // Menggabungkan entropi informasi untuk setiap kanal warna
        return (entropyR + entropyG + entropyB) / 3.0;
    }    

    @Override
    public String getName() {
        return "Entropy";
    }
}