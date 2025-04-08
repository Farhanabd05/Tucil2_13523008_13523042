import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
            if (!parser.getGifPath().isEmpty()) {
                saveGifEfficiently(quadTree, parser.getGifPath(), 500); // 500ms delay between frames
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
     * Menyimpan GIF secara incremental dengan menulis frame langsung ke output.
     * Metode ini menghindari penampungan seluruh frame dalam memori.
     *
     * @param quadTree QuadTree yang sudah dibangun dan berisi gambar asli serta struktur kompresi
     * @param path     Path output GIF
     * @param frameDelay Delay antar frame dalam milidetik
     */
/**
 * Menyimpan rangkaian frame sebagai GIF dengan delay tertentu
 */
public static void saveGifEfficiently(QuadTree quadTree, String gifPath, int frameDelay) {
    try {
        // Inisialisasi writer baru
        MemoryEfficientGifWriter efficientWriter = new MemoryEfficientGifWriter(gifPath, BufferedImage.TYPE_INT_RGB, true);

        int maxDepth = quadTree.getMaxDepth();
        // Batasi jumlah frame, misalnya hingga 15 frame
        int frameCount = Math.min(maxDepth + 1, 15);
        double depthStep = maxDepth / (double)(frameCount - 1);

        // Tuliskan frame awal (gambar asli)
        BufferedImage originalFrame = OutputHandler.convertToBufferedImage(quadTree.getRGBMatrix());
        efficientWriter.writeFrame(originalFrame, frameDelay);

        // Generate dan tulis frame secara iteratif tanpa menyimpan semua frame sekaligus
        for (int i = 1; i < frameCount; i++) {
            int depth = (int)Math.round(i * depthStep);
            // Misalnya, Anda memiliki metode untuk membuat RGBMatrix untuk frame pada depth tertentu.
            // Pastikan metode ini menghasilkan objek baru sehingga memori sebelumnya bisa dilepaskan.
            RGBMatrix depthMatrix = new RGBMatrix(quadTree.getRGBMatrix().getWidth(), quadTree.getRGBMatrix().getHeight());
            // Gunakan metode rekursif untuk mengisi depthMatrix sesuai depth yang diinginkan
            quadTree.applyColorsAtDepth(quadTree.getRoot(), depthMatrix, depth, 0);
            
            BufferedImage frame = OutputHandler.convertToBufferedImage(depthMatrix);
            efficientWriter.writeFrame(frame, frameDelay);

            // Bersihkan referensi dan minta garbage collection secara berkala
            depthMatrix = null;
            System.gc();
        }

        efficientWriter.close();
        System.out.println("[INFO] GIF berhasil disimpan di " + gifPath);
    } catch (IOException e) {
        System.err.println("[ERROR] Gagal menyimpan GIF: " + e.getMessage());
        e.printStackTrace();
    }
}
}