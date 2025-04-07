import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        InputParser parser = new InputParser();
        try {
            parser.parseInput();
            RGBMatrix rgbMatrix = parser.getRGBMatrix();
            System.out.println("[DEBUG] Konversi gambar ke matriks RGB selesai.");
            System.out.println("[DEBUG] Nilai RGB sample di (0,0): " + rgbMatrix.getPixel(0, 0));

            long startTime = System.currentTimeMillis();
            QuadTree quadTree = new QuadTree(rgbMatrix, parser.getErrorMetric(), parser.getThreshold(), parser.getMinBlockSize());
            // boolean createGif = !parser.getGifPath().isEmpty();
            // long originalSize = parser.getInputFile().length();
            
            List<BufferedImage> gifFrames = new ArrayList<>();
            quadTree.createGifFramesRecursive(
                quadTree.getRoot(), 
                gifFrames, 
                0
            );
            // // List untuk menyimpan frame GIF jika diaktifkan
            // List<BufferedImage> gifFrames = new ArrayList<>();
            // if (createGif) {
            //     BufferedImage image = ImageIO.read(parser.getInputFile());
            //     gifFrames.add(deepCopy(image));
            // }
            quadTree.buildTree();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            OutputHandler.writeImage(quadTree, parser.getOutputPath(), parser.getInputFile(), elapsedTime);

            System.out.println("[DEBUG] Konversi matriks RGB ke gambar selesai.");

            // // Buat dan simpan GIF jika diaktifkan
            // if (createGif && !gifFrames.isEmpty()) {
            //     saveGif(gifFrames, parser.getGifPath());
            //     System.out.println("8. GIF proses kompresi tersimpan di: " + parser.getGifPath());
            // }
            saveGif(gifFrames, parser.getGifPath());

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
     * Menyimpan rangkaian frame sebagai GIF
     */
    // private static void saveGif(List<BufferedImage> frames, String outputPath) {
    //     // Implementasi penyimpanan GIF
    //     // Note: Implementasi GIF memerlukan library tambahan seperti GifSequenceWriter
    //     // Untuk keperluan tugas ini, bisa menggunakan library seperti gifsicle atau ImageMagick
    //     System.out.println("GIF generation requires external library. Please implement using suitable GIF library.");
        
    //     // Contoh pseudocode untuk GifSequenceWriter
    //     try {
    //         ImageOutputStream output = new FileImageOutputStream(new File(outputPath));
    //         GifSequenceWriter writer = new GifSequenceWriter(output, frames.get(0).getType(), 500, true);
            
    //         for (BufferedImage frame : frames) {
    //             writer.writeToSequence(frame);
    //         }
            
    //         writer.close();
    //         output.close();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    private static void saveGif(List<BufferedImage> frames, String path) {
        try {
            ImageOutputStream output = 
                new FileImageOutputStream(new File(path));
            
            GifSequenceWriter writer = 
                new GifSequenceWriter(
                    output, 
                    BufferedImage.TYPE_INT_RGB, 
                    true
                );
            
            for (BufferedImage frame : frames) {
                writer.writeToSequence(frame, 500);
            }
            
            writer.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
