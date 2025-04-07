import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class Main {
    public static void main(String[] args) {
        InputParser parser = new InputParser();
        
        try {
            parser.parseInput();
            RGBMatrix rgbMatrix = parser.getRGBMatrix();
            System.out.println("[DEBUG] Konversi gambar ke matriks RGB selesai.");
            System.out.println("[DEBUG] Nilai RGB sample di (0,0): " + rgbMatrix.getPixel(0, 0));

            long startTime = System.currentTimeMillis();
            
            // Create QuadTree and build it first (before generating GIF frames)
            QuadTree quadTree = new QuadTree(rgbMatrix, parser.getErrorMetric(), parser.getThreshold(), parser.getMinBlockSize());
            quadTree.buildTree();
            
            // Now create GIF frames showing the progressive decomposition
            List<BufferedImage> gifFrames = new ArrayList<>();
            quadTree.createGifFramesRecursive(quadTree.getRoot(), gifFrames, quadTree.getMaxDepth());
            
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            
            // Save the output image
            OutputHandler.writeImage(quadTree, parser.getOutputPath(), parser.getInputFile(), elapsedTime);
            System.out.println("[DEBUG] Konversi matriks RGB ke gambar selesai.");

            // Save GIF with proper frame delay
            if (!gifFrames.isEmpty()) {
                saveGif(gifFrames, parser.getGifPath(), 500); // 500ms delay between frames
                System.out.println("[INFO] GIF visualisasi proses kompresi tersimpan di: " + parser.getGifPath());
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Terjadi kesalahan saat membaca file gambar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Membuat salinan mendalam dari BufferedImage
     */
    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(bi, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    /**
     * Menyimpan rangkaian frame sebagai GIF dengan delay tertentu
     */
/**
 * Menyimpan rangkaian frame sebagai GIF dengan delay tertentu
 */
private static void saveGif(List<BufferedImage> frames, String path, int frameDelay) {
    try {
        ImageOutputStream output = new FileImageOutputStream(new File(path));
        
        GifSequenceWriter writer = new GifSequenceWriter(
            output, 
            BufferedImage.TYPE_INT_RGB, 
            true
        );
        
        System.out.println("[INFO] Writing " + frames.size() + " frames to GIF...");
        
        // Track memory usage
        Runtime runtime = Runtime.getRuntime();
        long startMem = runtime.totalMemory() - runtime.freeMemory();
        
        // Process each frame
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            writer.writeToSequence(frame, frameDelay);
            
            // Free memory after writing - important for large images
            if (i % 5 == 0) { // Every 5 frames
                frames.set(i, null); // Help garbage collector
                System.gc(); // Request garbage collection
            }
            
            // Print progress for large GIFs
            if (i % 10 == 0 || i == frames.size() - 1) {
                System.out.println("[INFO] Processed " + (i + 1) + "/" + frames.size() + " frames");
            }
        }
        
        writer.close();
        output.close();
        
        // Report memory use
        long endMem = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("[INFO] Successfully saved GIF with " + frames.size() + 
                          " frames (Memory used: " + ((endMem - startMem) / 1024 / 1024) + "MB)");
    } catch (IOException e) {
        System.err.println("[ERROR] Failed to save GIF: " + e.getMessage());
        e.printStackTrace();
    }
}
}