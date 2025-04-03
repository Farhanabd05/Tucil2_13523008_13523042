import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OutputHandler {
    public static void saveImage(BufferedImage image, String path, String format) throws IOException {
        File file = new File(path);
        ImageIO.write(image, format, file);
    }
}
