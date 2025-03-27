import java.io.IOException;

public class ImageToRGBMatrix {
    public static void  main(String[] args) {
        
        InputParser parser = new InputParser();
        try {
            parser.parseInput();
            RGBMatrix rgbMatrix = parser.getRGBMatrix();
            rgbMatrix.printRGB();
            System.out.println("DEBUG: Konversi gambar ke matriks RGB selesai.");
            System.out.println("DEBUG: Nilai RGB sample di (0,0): " + rgbMatrix.getPixel(0, 0));
            OutputHandler.writeImage(rgbMatrix, parser.getOutputFileName());
        } catch (IOException e) {
            System.err.println("ERROR: Terjadi kesalahan saat membaca file gambar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
/*


cd ..
javac -d ./bin/ ./src/*.java
cd bin
java ImageToRGBMatrix
*/