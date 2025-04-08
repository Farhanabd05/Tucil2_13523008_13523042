import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
            
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            
            // Save the output image
            OutputHandler.writeImage(quadTree, parser.getOutputPath(), parser.getInputFile(), elapsedTime);
            System.out.println("[DEBUG] Konversi matriks RGB ke gambar selesai.");

            // Save GIF with proper frame delay
            if (parser.getGifPath() != null && !parser.getGifPath().trim().isEmpty()) {
                saveGifIncrementally(quadTree, parser.getGifPath(), 500); // delay 500ms per frame
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
     * Menyimpan GIF secara incremental dengan menulis frame langsung ke output.
     * Metode ini menghindari penampungan seluruh frame dalam memori.
     *
     * @param quadTree QuadTree yang sudah dibangun dan berisi gambar asli serta struktur kompresi
     * @param path     Path output GIF
     * @param frameDelay Delay antar frame dalam milidetik
     */
    private static void saveGifIncrementally(QuadTree quadTree, String path, int frameDelay) {
        try (ImageOutputStream output = new FileImageOutputStream(new File(path))) {
            // Buat writer dengan tipe image INT_RGB dan frame delay tertentu
            GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, frameDelay, true);
            
            // Tulis frame awal: gambar asli (tanpa kompresi)
            BufferedImage initialFrame = OutputHandler.convertToBufferedImage(quadTree.getRGBMatrix());
            writer.writeToSequence(initialFrame, frameDelay);
            System.out.println("[INFO] Written initial frame.");
            
            // Menulis frame intermediet secara incremental
            int maxDepth = quadTree.getMaxDepth();
            // Batasi jumlah frame agar tidak terlalu banyak (misalnya, maksimum 15 frame)
            int frameCount = Math.min(maxDepth + 1, 15);
            double depthStep = maxDepth / (double)(frameCount - 1);
            
            for (int i = 1; i < frameCount; i++) {
                int depth = (int)Math.round(i * depthStep);
                
                // Buat objek RGBMatrix baru untuk frame pada depth tertentu
                RGBMatrix depthMatrix = new RGBMatrix(quadTree.getRGBMatrix().getWidth(), quadTree.getRGBMatrix().getHeight());
                
                // Terapkan warna ke depthMatrix berdasarkan depth tertentu (gunakan metode rekursif yang sama)
                quadTree.applyColorsAtDepth(quadTree.getRoot(), depthMatrix, depth, 0);
                
                // Ubah RGBMatrix hasil ke BufferedImage untuk ditulis ke file GIF
                BufferedImage frame = OutputHandler.convertToBufferedImage(depthMatrix);
                writer.writeToSequence(frame, frameDelay);
                
                // Bebaskan referensi agar garbage collector dapat menghapus
                depthMatrix = null;
                frame = null;
                System.gc();
                
                System.out.println("[INFO] Written frame for depth " + depth);
            }
            
            writer.close();
            System.out.println("[INFO] Successfully saved GIF incrementally at: " + path);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save GIF incrementally: " + e.getMessage());
            e.printStackTrace();
        }
    }
}