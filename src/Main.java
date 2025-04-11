import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        InputParser parser = new InputParser();
        
        try {
            parser.parseInput();
            RGBMatrix rgbMatrix = parser.getRGBMatrix();

            long startTime = System.currentTimeMillis();
            if (!parser.isTargetCompressionSet()) {
                QuadTree quadTree = new QuadTree(rgbMatrix, parser.getErrorMetric(), parser.getThreshold(), parser.getMinBlockSize());
                quadTree.buildTree();
                
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                
                OutputHandler.writeImage(quadTree, parser.getOutputPath(), parser.getInputFile(), elapsedTime);
    
                if (!parser.getGifPath().isEmpty()) {
                    saveGifEfficiently(quadTree, parser.getGifPath(), 500);
                }
            }
            else {
                CompressionController controller = new CompressionController();
                CompressedImage compressedImage = controller.compressWithTarget(
                    rgbMatrix,
                    parser.getErrorMetric(),
                    parser.getTargetCompression(),
                    parser.getOutputImageFormat() 
                );
                
                long endTime2 = System.currentTimeMillis();
                long elapsedTime2 = endTime2 - startTime;
                
                BufferedImage compressedImageBuffer = compressedImage.getImage();
                OutputHandler.writeImage2(compressedImageBuffer, parser.getOutputPath(), parser.getInputFile(), elapsedTime2, compressedImage.getCompressionRate(), compressedImage.getCompressedSizeInBytes(), compressedImage.getOriginalSizeInBytes());
                System.out.println("Gambar terkompresi telah disimpan.");
                if (!parser.getGifPath().isEmpty()) {
                    saveGifEfficiently(compressedImage.getQuadTree(), parser.getGifPath(), 500);
                }
            }


        } catch (IOException e) {
            System.err.println("[ERROR] Terjadi kesalahan saat membaca file gambar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(bi, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static void saveGifEfficiently(QuadTree quadTree, String gifPath, int frameDelay) {
        try {
            CLIUtils.printSectionHeader("CREATING GIF VISUALIZATION");
            System.out.println(CLIUtils.BOLD + "Generating frames and saving GIF..." + CLIUtils.RESET);
            
            MemoryEfficientGifWriter efficientWriter = new MemoryEfficientGifWriter(gifPath, BufferedImage.TYPE_INT_RGB, true);
    
            int maxDepth = quadTree.getMaxDepth();
            int frameCount = Math.min(maxDepth + 1, 15);
            double depthStep = maxDepth / (double)(frameCount - 1);
    
            for (int i = 0; i < frameCount; i++) {
                int depth = (int)Math.round(i * depthStep);
                RGBMatrix depthMatrix = new RGBMatrix(quadTree.getRGBMatrix().getWidth(), quadTree.getRGBMatrix().getHeight());
                
                // Show current progress
                int percent = (i * 100) / frameCount;
                CLIUtils.printProgressBar(percent);
                
                quadTree.applyColorsAtDepth(quadTree.getRoot(), depthMatrix, depth, 0);
                
                BufferedImage frame = OutputHandler.convertToBufferedImage(depthMatrix);
                efficientWriter.writeFrame(frame, frameDelay);
    
                depthMatrix = null;
                System.gc();
            }
            
            // Complete the progress bar
            CLIUtils.printProgressBar(100);
            System.out.println();
    
            efficientWriter.close();
            CLIUtils.printSuccess("GIF animation saved successfully to " + gifPath);
        } catch (IOException e) {
            CLIUtils.printError("Failed to save GIF: " + e.getMessage());
            e.printStackTrace();
        }
    }


}