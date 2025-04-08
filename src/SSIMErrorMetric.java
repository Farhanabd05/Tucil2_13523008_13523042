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
   * Menghitung error berdasarkan metode SSIM
   * SSIM mengembalikan nilai antara 0 dan 1, di mana 1 berarti identik.
   * Untuk penggunaan sebagai error metric, kita menggunakan 1-SSIM sehingga
   * nilai rendah berarti error kecil (gambar lebih mirip).
   * 
   * @param rgbMatrix matriks RGB dari gambar asli
   * @param x posisi x dari region yang akan dievaluasi
   * @param y posisi y dari region yang akan dievaluasi
   * @param width lebar region
   * @param height tinggi region
   * @return nilai error berdasarkan SSIM (1-SSIM)
   */
  @Override
  public double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height) {
      // Hitung rata-rata warna untuk region
      Pixel avgColor = calculateAverageColor(rgbMatrix, x, y, width, height);
      
      // Hitung SSIM untuk masing-masing kanal RGB
      double ssimR = calculateSSIMForChannel(rgbMatrix, x, y, width, height, avgColor, 0);
      double ssimG = calculateSSIMForChannel(rgbMatrix, x, y, width, height, avgColor, 1);
      double ssimB = calculateSSIMForChannel(rgbMatrix, x, y, width, height, avgColor, 2);
      
      // Gabungkan SSIM dari ketiga kanal dengan bobot
      double ssim = W_R * ssimR + W_G * ssimG + W_B * ssimB;
      
      // Kembalikan 1-SSIM sebagai nilai error (0 berarti identik, 1 berarti berbeda total)
      return 1.0 - ssim;
  }
  
  /**
   * Menghitung rata-rata warna untuk region tertentu
   * 
   * @param rgbMatrix matriks RGB dari gambar asli
   * @param x posisi x dari region
   * @param y posisi y dari region
   * @param width lebar region
   * @param height tinggi region
   * @return Pixel yang mewakili rata-rata warna
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
  
  /**
   * Menghitung SSIM untuk satu kanal warna (R, G, atau B)
   * 
   * @param rgbMatrix matriks RGB dari gambar asli
   * @param x posisi x dari region
   * @param y posisi y dari region
   * @param width lebar region
   * @param height tinggi region
   * @param avgColor rata-rata warna untuk region
   * @param channel kanal yang akan dihitung (0=R, 1=G, 2=B)
   * @return nilai SSIM untuk kanal tersebut
   */
  private double calculateSSIMForChannel(RGBMatrix rgbMatrix, int x, int y, 
                                        int width, int height, Pixel avgColor, int channel) {
      // Dapatkan nilai rata-rata untuk kanal
      double meanX;
      if (channel == 0) {
          meanX = avgColor.getR();
      } else if (channel == 1) {
          meanX = avgColor.getG();
      } else {
          meanX = avgColor.getB();
      }
      
      // Hitung variansi untuk kanal
      double varianceX = 0;
      int count = 0;
      
      // Iterasi semua piksel dalam region
      for (int cy = y; cy < y + height && cy < rgbMatrix.getHeight(); cy++) {
          for (int cx = x; cx < x + width && cx < rgbMatrix.getWidth(); cx++) {
              Pixel pixel = rgbMatrix.getPixel(cx, cy);
              double pixelValue;
              
              // Ambil nilai piksel untuk kanal yang sesuai
              if (channel == 0) {
                  pixelValue = pixel.getR();
              } else if (channel == 1) {
                  pixelValue = pixel.getG();
              } else {
                  pixelValue = pixel.getB();
              }
              
              // Hitung selisih kuadrat dengan rata-rata untuk variansi
              varianceX += Math.pow(pixelValue - meanX, 2);
              count++;
          }
      }
      
      // Hindari pembagian dengan nol
      if (count == 0) return 1.0; // Kembalikan nilai SSIM maksimum
      
      // Selesaikan perhitungan variansi
      varianceX /= count;
      
      // Hitung standar deviasi
      double stdDevX = Math.sqrt(varianceX);
      
      // Untuk setiap region (blok), meanY adalah nilai tunggal dari warna rata-rata
      // dan stdDevY adalah 0 karena tidak ada variasi dalam blok tersebut
      double meanY = meanX;
      double stdDevY = 0;
      double covarianceXY = 0; // Kovarians adalah 0 dalam kasus ini
      
      // Hitung komponen SSIM menggunakan formula
      // SSIM = ((2*μx*μy + C1)(2*σxy + C2))/((μx² + μy² + C1)(σx² + σy² + C2))
      double numerator = (2 * meanX * meanY + C1) * (2 * covarianceXY + C2);
      double denominator = (meanX * meanX + meanY * meanY + C1) * 
                          (stdDevX * stdDevX + stdDevY * stdDevY + C2);
      
      // Hindari pembagian dengan nol
      if (denominator == 0) return 1.0;
      
      return numerator / denominator;
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