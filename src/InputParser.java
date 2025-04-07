import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class InputParser {
    private RGBMatrix rgbMatrix;
    private int height, width;
    private String outputPath;
    private ErrorMetric errorMetric;
    private double threshold;
    private int minBlockSize;
    private File inputFile;
    private String gifPath;

    public InputParser() {}

    public void parseInput() throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            String currentDir = System.getProperty("user.dir");
            String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc";
            String solPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "sol";

            System.out.println("Masukkan nama file: ");
            String inputfileName = scanner.nextLine();
            String inputFilePath = testPath + File.separator + inputfileName;
            System.out.println("Masukkan nama file output: ");
            String outputFileName = scanner.nextLine();
            this.outputPath = solPath + File.separator + outputFileName;

            BufferedImage image = ImageIO.read(new File(inputFilePath));
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

            // 2. Choose error metric
            displayErrorMetrics();
            int errorMetricChoice = scanner.nextInt();
            errorMetric = ErrorMetricFactory.createErrorMetric(errorMetricChoice);

            // 3. Error threshold
            System.out.print("\nEnter error threshold value: ");
            threshold = scanner.nextDouble();

            // 4. Minimum block size
            System.out.print("\nEnter minimum block size (e.g., 4): ");
            minBlockSize = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            this.inputFile = new File(inputFilePath);
            
            System.out.println("\n Enter gif path : ");
            String gifFileName= scanner.nextLine();
            this.gifPath = solPath + File.separator + gifFileName;
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

    public String getOutputPath() {
        return outputPath;
    }

    public ErrorMetric getErrorMetric() {
        return errorMetric;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getMinBlockSize() {
        return minBlockSize;
    }

    public File getInputFile() {
        return inputFile;
    }

    public String getGifPath() {
        return gifPath;
    }

        /*
     * Metode untuk menampilkan daftar metode error yang tersedia.
    */

    public static void displayErrorMetrics() {
        System.out.println("Daftar Metode Error:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        System.out.print("\nEnter error metric choice (1-4): ");
    }
}

