import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class InputParser {
    private RGBMatrix rgbMatrix;
    private int height, width;

    public InputParser() {}

    public void parseInput() throws IOException {
        Scanner scanner = new Scanner(System.in);
        try {
            String currentDir = System.getProperty("user.dir");
            String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc";
            System.out.println("Masukkan nama file: ");
            String fileName = scanner.nextLine();
            String filePath = testPath + File.separator + fileName;

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
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rgbMatrix.setPixel(x, y, image.getRGB(x, y));
                }
            }
        } finally {
            scanner.close();
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
}

