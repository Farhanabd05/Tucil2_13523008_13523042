import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class MemoryEfficientGifWriter {
    private GifSequenceWriter gifWriter;
    private ImageOutputStream outputStream;

    /**
     * Konstruktor untuk inisialisasi writer dengan output file.
     * 
     * @param outputPath Path file output GIF.
     * @param imageType Tipe image (misal, BufferedImage.TYPE_INT_RGB).
     * @param loopContinuously Jika true, GIF akan loop terus-menerus.
     * @throws IOException Jika terjadi kesalahan pada I/O.
     */
    public MemoryEfficientGifWriter(String outputPath, int imageType, boolean loopContinuously) throws IOException {
        outputStream = new FileImageOutputStream(new File(outputPath));
        gifWriter = new GifSequenceWriter(outputStream, imageType, loopContinuously);
    }

    /**
     * Menulis sebuah frame ke dalam GIF dan segera melepaskan referensinya.
     * 
     * @param frame Frame yang akan ditulis.
     * @param frameDelay Delay untuk frame tersebut dalam milidetik.
     * @throws IOException Jika terjadi kesalahan saat menulis frame.
     */
    public void writeFrame(BufferedImage frame, int frameDelay) throws IOException {
        gifWriter.writeToSequence(frame, frameDelay);
        // Bersihkan resource frame secepatnya
        frame.flush();
    }

    /**
     * Menutup writer dan output stream.
     * 
     * @throws IOException Jika terjadi kesalahan saat penutupan stream.
     */
    public void close() throws IOException {
        gifWriter.close();
        outputStream.close();
    }
}
