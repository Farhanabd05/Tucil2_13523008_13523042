import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class OutputHandler {
    public static void writeImage(QuadTree quadTree, String outputPath, File inputFile, long executionTime) throws IOException {
        RGBMatrix rgbMatrix = quadTree.getRGBMatrix();
        int width = rgbMatrix.getWidth();
        int height = rgbMatrix.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Pixel[] pixels = rgbMatrix.getPixels();
        int[] rgbArray = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            rgbArray[i] = pixels[i].getRGB();
        }
        bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);

        String format = getFormatFromPath(outputPath);
        if (format == null) {
            System.err.println("ERROR: Format gambar tidak dikenali.");
            return;
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new File(outputPath))) {
            ImageIO.write(bufferedImage, format, ios);
        } catch (IOException e) {
            System.err.println("ERROR: Gagal menyimpan gambar ke " + outputPath + ": " + e.getMessage());
            e.printStackTrace();
        }

        long originalSize = inputFile.length();
        long compressedSize = new File(outputPath).length();
        
        double compressionPercentage = (1.0 - (double) compressedSize / originalSize) * 100;        

        int nodeCount = quadTree.getNodeCount();
        int maxDepth = quadTree.getMaxDepth();
        DecimalFormat df = new DecimalFormat("#.##");

        printCompressionResults(
            df.format(executionTime / 1000.0) + " seconds", 
            originalSize + " bytes", 
            compressedSize + " bytes", 
            df.format(compressionPercentage) + "%", 
            String.valueOf(nodeCount), 
            String.valueOf(maxDepth)
        );
    }

    private static void printCompressionResults(String execTime, String origSize, String compSize, String compPerc, String nodeCount, String maxDepth) {
        String col1Title = "Parameter";
        String col2Title = "Nilai";
        int colWidth1 = 30;
        int colWidth2 = 30;

        String topBorder = "┌" + "─".repeat(colWidth1) + "┬" + "─".repeat(colWidth2) + "┐";
        String midBorder = "├" + "─".repeat(colWidth1) + "┼" + "─".repeat(colWidth2) + "┤";
        String bottomBorder = "└" + "─".repeat(colWidth1) + "┴" + "─".repeat(colWidth2) + "┘";

        System.out.println();
        System.out.println(topBorder);
        System.out.printf("│%" + ((colWidth1 + col1Title.length()) / 2) + "s%" + ((colWidth1 - col1Title.length() + 1) / 2) + "s", col1Title, "");
        System.out.printf("│%" + ((colWidth2 + col2Title.length()) / 2) + "s%" + ((colWidth2 - col2Title.length() + 1) / 2) + "s│\n", col2Title, "");
        System.out.println(midBorder);

        printTableRow("Execution time", execTime, colWidth1, colWidth2);
        printTableRow("Original image size", origSize, colWidth1, colWidth2);
        printTableRow("Compressed image size", compSize, colWidth1, colWidth2);
        printTableRow("Compression percentage", compPerc, colWidth1, colWidth2);
        printTableRow("Node count", nodeCount, colWidth1, colWidth2);
        printTableRow("Max depth", maxDepth, colWidth1, colWidth2);

        System.out.println(bottomBorder);
    }

    public static void printTable(String[] headers, String[][] data, int[] columnWidths) {
        int totalWidth = 1;
        for (int width : columnWidths) {
            totalWidth += width + 3;
        }
    
        System.out.print("┌");
        for (int i = 0; i < headers.length; i++) {
            System.out.print("─".repeat(columnWidths[i] + 2));
            System.out.print(i < headers.length - 1 ? "┬" : "┐");
        }
        System.out.println();
    
        System.out.print("│");
        for (int i = 0; i < headers.length; i++) {
            System.out.print(" " + headers[i]);
            System.out.print(" ".repeat(columnWidths[i] - headers[i].length()));
            System.out.print("│");
        }
        System.out.println();
    
        System.out.print("├");
        for (int i = 0; i < headers.length; i++) {
            System.out.print("─".repeat(columnWidths[i] + 2));
            System.out.print(i < headers.length - 1 ? "┼" : "┤");
        }
        System.out.println();
    
        for (String[] row : data) {
            System.out.print("│");
            for (int i = 0; i < row.length; i++) {
                System.out.print(" " + row[i]);
                System.out.print(" ".repeat(columnWidths[i] - row[i].length()));
                System.out.print("│");
            }
            System.out.println();
        }
    
        System.out.print("└");
        for (int i = 0; i < headers.length; i++) {
            System.out.print("─".repeat(columnWidths[i] + 2));
            System.out.print(i < headers.length - 1 ? "┴" : "┘");
        }
        System.out.println();
    }
    
    private static void printTableRow(String param, String value, int width1, int width2) {
        System.out.print("│ ");
        System.out.print(param);
        System.out.print(" ".repeat(Math.max(0, width1 - param.length() - 2)));
        System.out.print(" │ ");
        System.out.print(value);
        System.out.print(" ".repeat(Math.max(0, width2 - value.length() - 2)));
        System.out.println(" │");
    }

    private static String getFormatFromPath(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == path.length() - 1) {
            return null;
        }
        return path.substring(dotIndex + 1);
    }

    public static BufferedImage convertToBufferedImage(RGBMatrix rgbMatrix) {
        int width = rgbMatrix.getWidth();
        int height = rgbMatrix.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    
        int[] rgbArray = new int[width * height];
        Pixel[] pixels = rgbMatrix.getPixels();
    
        for (int i = 0; i < pixels.length; i++) {
            rgbArray[i] = pixels[i].getRGB();
        }
    
        bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);
    
        rgbArray = null;
    
        return bufferedImage;
    }
}
