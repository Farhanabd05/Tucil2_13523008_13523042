public class RGBMatrix {
    private int width, height;
    private Pixel[] pixels;

    public RGBMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Pixel(0, 0, 0);
        }
        System.out.println("RGBMatrix created with width=" + width + " and height=" + height);
    }

    public void setPixel(int x, int y, int pixel) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        pixels[y * width + x].setRGB(pixel);
    }

    public Pixel getPixel(int x, int y) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        return pixels[y * width + x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Pixel[] getPixels() {
        return pixels;
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public void printRGB() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                System.out.print((getPixel(x, y)) + " ");
                System.out.println();
            }
        }
    }

    public Pixel getAverageColor (int x, int y, int w, int h) {
        int totalR = 0, totalG = 0, totalB = 0;
        // offset adalah posisi awal dari sub-matrix yang akan dihitung
        // dalam array 1D, maka offset ini digunakan untuk mengakses
        // elemen-elemen dalam array 1D dengan menggunakan perhitungan
        // offset + i, dimana i adalah indeks dari sub-matrix yang
        // sedang dihitung
        int offset = y * width + x;
        // loop ini hanya sekali karena kita ingin menghitung rata-rata
        // dari seluruh elemen dalam sub-matrix, maka kita hanya perlu
        // loop sebanyak jumlah elemen dalam sub-matrix
        for (int i = 0; i < w * h; i++) {
            // totalR, totalG, dan totalB digunakan untuk menghitung
            // total nilai dari setiap komponen warna (red, green, dan blue)
            // dalam sub-matrix
            totalR += pixels[offset + i].getR();
            totalG += pixels[offset + i].getG();
            totalB += pixels[offset + i].getB();
        }
        int avgR = totalR / (w * h);
        int avgG = totalG / (w * h);
        int avgB = totalB / (w * h);
        return new Pixel(avgR, avgG, avgB);
    }

    public double getMADError (int x, int y, int w, int h, Pixel avgColor) {
        int totalError = 0;
        int offset = y * width + x;
        for (int i = 0; i < w * h; i++) {
            Pixel color = pixels[offset + i];
            totalError += Math.abs(color.getR() - avgColor.getR()) +
                        + Math.abs(color.getG() - avgColor.getG()) +
                        + Math.abs(color.getB() - avgColor.getB());
        }
        return totalError / (w * h);
    }

    public double getVarianceError (int x, int y, int w, int h, Pixel avgColor) {
        double totalError = 0;
        int offset = y * width + x;
        for (int i = 0; i < w * h; i++) {
            Pixel color = pixels[offset + i];
            totalError += Math.pow(color.getR() - avgColor.getR(), 2) +
                        + Math.pow(color.getG() - avgColor.getG(), 2) +
                        + Math.pow(color.getB() - avgColor.getB(), 2);
        }
        return totalError / (w * h);
    }

    public double getErrorMaxPixelDifference (int x, int y, int w, int h, Pixel avgColor) {
        int maxError = 0;
        int offset = y * width + x;
        for (int i = 0; i < w * h; i++) {
            Pixel color = pixels[offset + i];
            maxError = Math.max(maxError, Math.abs(color.getR() - avgColor.getR()));
            maxError = Math.max(maxError, Math.abs(color.getG() - avgColor.getG()));
            maxError = Math.max(maxError, Math.abs(color.getB() - avgColor.getB()));
        }
        return maxError;
    }

    public double getMaxPixelDiffError(int x, int y, int w, int h) {
        int maxR = 0, maxG = 0, maxB = 0, minR = 255, minG = 255, minB = 255;
        int offset = y * width + x;
        for (int i = 0; i < w * h; i++) {
            Pixel col = pixels[offset + i];
            int r = col.getR(), g = col.getG(), b = col.getB();
            if (r > maxR) maxR = r;
            if (g > maxG) maxG = g;
            if (b > maxB) maxB = b;
            if (r < minR) minR = r;
            if (g < minG) minG = g;
            if (b < minB) minB = b;
        }
        return ((maxR - minR) + (maxG - minG) + (maxB - minB)) / 3.0;
    }

    public double getEntropyError(int x, int y, int w, int h) {
        int total = w * h;
        int[] histR = new int[256], histG = new int[256], histB = new int[256];
        int offset = y * width + x;
        for (int i = 0; i < w * h; i++) {
            Pixel col = pixels[offset + i];
            histR[col.getR()]++;
            histG[col.getG()]++;
            histB[col.getB()]++;
        }
        double entropyR = 0, entropyG = 0, entropyB = 0;
        for (int i = 0; i < 256; i++){
            double pR = histR[i] / (double) total;
            double pG = histG[i] / (double) total;
            double pB = histB[i] / (double) total;
            if (pR > 0) entropyR -= pR * (Math.log(pR) / Math.log(2));
            if (pG > 0) entropyG -= pG * (Math.log(pG) / Math.log(2));
            if (pB > 0) entropyB -= pB * (Math.log(pB) / Math.log(2));
        }
        return (entropyR + entropyG + entropyB) / 3;
    }

    public double getError(int x, int y, int w, int h, Pixel avgColor, String errorMethod) {
        switch (errorMethod) {
            case "variance": return getVarianceError(x, y, w, h, avgColor);
            case "max pixel difference": return getErrorMaxPixelDifference(x, y, w, h, avgColor);
            case "entropy": return getEntropyError(x, y, w, h);
            case "max pixel diff": return getMaxPixelDiffError(x, y, w, h);
            default: return 0;
        }
    }
}

