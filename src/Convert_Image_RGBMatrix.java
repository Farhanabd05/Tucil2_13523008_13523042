import java.awt.Color;
import java.awt.image.BufferedImage;

public class Convert_Image_RGBMatrix {
    public static RGBMatrix convert(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Color[][] Colors = new Color[height][width];
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                Colors[y][x] = new Color(image.getRGB(x, y));
            }
        }
        return new RGBMatrix(Colors);
    }
}

/*


cd ..
javac -d ./bin/ ./src/*.java
cd bin
java ImageToRGBMatrix
*/