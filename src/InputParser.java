import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class InputParser {
    private RGBMatrix rgbMatrix;
    private int height, width;
    private String outputFileName;
    private int errorMethod;
    private double threshold;
    private int minBlockSize;
    private boolean showAvgColors;

    public InputParser() {}

    public void parseInput() throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            String currentDir = System.getProperty("user.dir");
            String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc";

            System.out.println("Masukkan nama file: ");
            String fileName = scanner.nextLine();
            String filePath = testPath + File.separator + fileName;
            System.out.println("Masukkan nama file output: ");
            this.outputFileName = scanner.nextLine();

            // Create quadtree root node
            System.out.println("Masukkan metode error (1: Variance, 2: MaxPixelDifference, 3: Entropy, 4: MaxPixelDiff): ");
            this.errorMethod = Integer.parseInt(scanner.nextLine());
            
            System.out.println("Masukkan threshold error: ");
            this.threshold = Double.parseDouble(scanner.nextLine());
            
            System.out.println("Masukkan ukuran minimal blok: ");
            this.minBlockSize = Integer.parseInt(scanner.nextLine());

            System.out.println("Tampilkan warna rata-rata di setiap node? (y/n): ");
            this.showAvgColors = scanner.nextLine().toLowerCase().equals("y");

            System.out.println("[DEBUG] Memulai proses pembacaan gambar");
            System.out.println("[DEBUG] Path gambar: " + filePath);

            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                throw new IOException("[ERROR] GAGAL membaca file image, pastikan file tersebut benar dan didukung");
            }

            System.out.println("[DEBUG] Gambar berhasil dimuat");
            this.width = image.getWidth();
            this.height = image.getHeight();
            System.out.println("[DEBUG] Dimensi Gambar: " + width + " x " + height);

            this.rgbMatrix = new RGBMatrix(width, height);
            System.out.println("[DEBUG] Objek RGBMatrix dibuat dengan dimensi: " + width + " x " + height);

            // Konversi gambar ke matriks RGB
            int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);
            for (int i = 0; i < rgbArray.length; i++) {
                rgbMatrix.setPixel(i % width, i / width, rgbArray[i]);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public RGBMatrix getRGBMatrix() {
        return rgbMatrix;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public int getErrorMethod() {
        return errorMethod;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getMinBlockSize() {
        return minBlockSize;
    }

    public boolean getShowAvgColors() {
        return showAvgColors;
    }
}

