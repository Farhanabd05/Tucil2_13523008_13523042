import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ImageToRGBMatrix {
    public static void  main(String[] args) {
        
        String currentDir = System.getProperty("user.dir");
        String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc"; 
        Scanner scanner = new Scanner(System.in);
        System.out.println("Masukkan nama file: ");
        String fileName = scanner.nextLine();
        String filePath = testPath + File.separator + fileName;
        if (args.length > 0) {
            filePath = args[0];
        }

        System.out.println("[DEBUG] Memulai proses pembacaan gambar");
        System.out.println("[DEBUG] Path gambar: " + filePath);

        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                System.err.println("[ERROR] GAGAL membaca file image, pastikan file tersebut benar dan didudkung");
                scanner.close();
                return;
            }
            System.out.println("[DEBUG Gambar berhasil dimuat");
            System.out.println("[DEBUG] Dimensi Gambar: "+ image.getWidth() + " x " + image.getHeight());

            RGBMatrix rgbMatrix = new RGBMatrix(image.getWidth(), image.getHeight());
            System.out.println("[DEBUG] Objek RGBMAtrix dibuat dengan dimensi: " 
                                + rgbMatrix.getWidth() + " x " + rgbMatrix.getHeight());
            //konversi gambar ke matrix rgb
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = (pixel) & 0xFF;
                    rgbMatrix.setPixel(x, y, red, green, blue);

                    if (x < 3 && y < 3) {
                        System.out.println("DEBUG: Pixel di (" + x + ", " + y + ") = (" 
                        + red + ", " + green + ", " + blue + ")");
                    }
                }
            }

            System.out.println("DEBUG: Konversi gambar ke matriks RGB selesai.");
            System.out.println("DEBUG: Nilai RGB sample di (0,0): " + rgbMatrix.getPixel(0, 0));
            
            // Opsional: jika gambar berukuran kecil, bisa cetak seluruh matriks
            // rgbMatrix.printMatrix();
        } catch (IOException e) {
            System.err.println("ERROR: Terjadi kesalahan saat membaca file gambar: " + e.getMessage());
            e.printStackTrace();
        }
        scanner.close();
    }
}
/*


cd ..
javac -d ./bin/ ./src/*.java
cd bin
java ImageToRGBMatrix
*/