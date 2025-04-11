import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class MemoryEfficientGifWriter {
    private GifSequenceWriter gifWriter;
    private ImageOutputStream outputStream;

    public MemoryEfficientGifWriter(String outputPath, int imageType, boolean loopContinuously) throws IOException {
        outputStream = new FileImageOutputStream(new File(outputPath));
        gifWriter = new GifSequenceWriter(outputStream, imageType, loopContinuously);
    }

    public void writeFrame(BufferedImage frame, int frameDelay) throws IOException {
        gifWriter.writeToSequence(frame, frameDelay);
        frame.flush();
    }

    public void close() throws IOException {
        gifWriter.close();
        outputStream.close();
    }
}
