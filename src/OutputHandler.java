import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class OutputHandler {
    /**
     * Mengubah RGBMatrix menjadi BufferedImage dan menyimpannya ke file output.
     *
     * @param rgbMatrix  Objek RGBMatrix yang berisi data gambar
     * @param outputPath Path file output (contoh: "output.png")
     */
    public static void writeImage(RGBMatrix rgbMatrix, String fileName) {
        int width = rgbMatrix.getWidth();
        int height = rgbMatrix.getHeight();

        // buat buffer dengan tipe RGB
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // loop untuk mengisi BufferedImage dengan data RGB dari RGBMatrix
        Pixel[] pixels = rgbMatrix.getPixels();
        int[] rgbArray = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            rgbArray[i] = pixels[i].getRGB();
        }
        bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);
        
        String currentDir = System.getProperty("user.dir");
        String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test"
                + File.separator + "sol";
        String outputPath = testPath + File.separator + fileName;

        // tentukan format gambar berdasarkan ekstensi file
        String format = getFormatFromPath(outputPath);
        if (format == null) {
            System.err.println("ERROR: Format gambar tidak dikenali.");
            return;
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new File(outputPath))) {
            ImageIO.write(bufferedImage, format, ios);
        } catch (IOException e) {
            System.err.println("ERROR: Gagal menyimpan gambar ke " + outputPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mengambil format gambar dari path file, misal "png" dari "output.png".
     *
     * @param path Path file
     * @return String format file atau null jika tidak ditemukan.
     */
    private static String getFormatFromPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == path.length() - 1) {
            return null;
        }
        return path.substring(dotIndex + 1);
    }
}
