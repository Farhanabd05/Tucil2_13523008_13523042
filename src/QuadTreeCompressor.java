import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class QuadTreeCompressor {
    // Enum untuk memilih metode pengukuran error
    public enum ErrorMethod {
        VARIANCE,
        MAD,
        MAX_DIFF,
        ENTROPY
    }

    // Kelas node Quadtree
    public static class QuadtreeNode {
        int x, y, width, height;
        Color avgColor;
        double error; // nilai error untuk blok ini
        boolean isLeaf;
        QuadtreeNode[] children; // urutan: top-left, top-right, bottom-left, bottom-right

        // Konstruktor: menghitung rata-rata warna dan error pada blok
        public QuadtreeNode(int x, int y, int width, int height, BufferedImage image, ErrorMethod method) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.avgColor = computeAverageColor(image, x, y, width, height);
            this.error = computeError(image, x, y, width, height, this.avgColor, method);
            this.isLeaf = true;
            this.children = null;
        }

        /**
         * subdivide: jika error > threshold, bagi blok menjadi empat bagian.
         * Jika unlimitedDepth aktif, berhenti hanya bila ukuran blok mencapai 1 piksel.
         * Jika tidak, berhenti jika ukuran blok <= minSize.
         */
        public void subdivide(BufferedImage image, ErrorMethod method, double threshold, int minSize, boolean unlimitedDepth) {
            if (unlimitedDepth) {
                if (width <= 1 || height <= 1) return;
            } else {
                if (width <= minSize || height <= minSize) return;
            }
            if (this.error > threshold) {
                int halfWidth = (width + 1) / 2;
                int halfHeight = (height + 1) / 2;
                if (!unlimitedDepth && (halfWidth < minSize || halfHeight < minSize)) return;
                
                children = new QuadtreeNode[4];
                children[0] = new QuadtreeNode(x, y, halfWidth, halfHeight, image, method); // Top-left
                children[1] = new QuadtreeNode(x + halfWidth, y, width - halfWidth, halfHeight, image, method); // Top-right
                children[2] = new QuadtreeNode(x, y + halfHeight, halfWidth, height - halfHeight, image, method); // Bottom-left
                children[3] = new QuadtreeNode(x + halfWidth, y + halfHeight, width - halfWidth, height - halfHeight, image, method); // Bottom-right

                this.isLeaf = false;
                for (QuadtreeNode child : children) {
                    child.subdivide(image, method, threshold, minSize, unlimitedDepth);
                }
            }
        }

        // Menggambar node (jika leaf) ke Graphics2D
        public void draw(Graphics2D g) {
            if (isLeaf) {
                g.setColor(avgColor);
                g.fillRect(x, y, width, height);
            } else {
                for (QuadtreeNode child : children) {
                    child.draw(g);
                }
            }
        }
    }

    // Menghitung rata-rata warna pada region
    public static Color computeAverageColor(BufferedImage image, int x, int y, int w, int h) {
        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                Color color = new Color(image.getRGB(i, j));
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
                count++;
            }
        }
        if (count == 0) return new Color(0, 0, 0);
        return new Color((int)(sumR / count), (int)(sumG / count), (int)(sumB / count));
    }

    // Menghitung error pada blok menggunakan metode yang dipilih
    public static double computeError(BufferedImage image, int x, int y, int w, int h, Color avg, ErrorMethod method) {
        switch(method) {
            case VARIANCE:
                return computeVariance(image, x, y, w, h, avg);
            case MAD:
                return computeMAD(image, x, y, w, h, avg);
            case MAX_DIFF:
                return computeMaxDiff(image, x, y, w, h);
            case ENTROPY:
                return computeEntropy(image, x, y, w, h);
            default:
                return 0;
        }
    }

    // Variance
    public static double computeVariance(BufferedImage image, int x, int y, int w, int h, Color avg) {
        double sumVar = 0.0;
        int count = 0;
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                Color color = new Color(image.getRGB(i, j));
                double varR = Math.pow(color.getRed() - avg.getRed(), 2);
                double varG = Math.pow(color.getGreen() - avg.getGreen(), 2);
                double varB = Math.pow(color.getBlue() - avg.getBlue(), 2);
                sumVar += (varR + varG + varB) / 3.0;
                count++;
            }
        }
        return count > 0 ? sumVar / count : 0;
    }

    // Mean Absolute Deviation (MAD)
    public static double computeMAD(BufferedImage image, int x, int y, int w, int h, Color avg) {
        double sumMad = 0.0;
        int count = 0;
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                Color color = new Color(image.getRGB(i, j));
                double madR = Math.abs(color.getRed() - avg.getRed());
                double madG = Math.abs(color.getGreen() - avg.getGreen());
                double madB = Math.abs(color.getBlue() - avg.getBlue());
                sumMad += (madR + madG + madB) / 3.0;
                count++;
            }
        }
        return count > 0 ? sumMad / count : 0;
    }

    // Maximum Pixel Difference
    public static double computeMaxDiff(BufferedImage image, int x, int y, int w, int h) {
        int minR = 255, minG = 255, minB = 255;
        int maxR = 0, maxG = 0, maxB = 0;
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                Color color = new Color(image.getRGB(i, j));
                int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
                if(r < minR) minR = r;
                if(g < minG) minG = g;
                if(b < minB) minB = b;
                if(r > maxR) maxR = r;
                if(g > maxG) maxG = g;
                if(b > maxB) maxB = b;
            }
        }
        return (maxR - minR + maxG - minG + maxB - minB) / 3.0;
    }

    // Entropy
    public static double computeEntropy(BufferedImage image, int x, int y, int w, int h) {
        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];
        int total = w * h;
        for (int j = y; j < y + h; j++) {
            for (int i = x; i < x + w; i++) {
                Color color = new Color(image.getRGB(i, j));
                histR[color.getRed()]++;
                histG[color.getGreen()]++;
                histB[color.getBlue()]++;
            }
        }
        double entropyR = 0.0, entropyG = 0.0, entropyB = 0.0;
        for (int i = 0; i < 256; i++) {
            if (histR[i] > 0) {
                double p = (double) histR[i] / total;
                entropyR -= p * (Math.log(p) / Math.log(2));
            }
            if (histG[i] > 0) {
                double p = (double) histG[i] / total;
                entropyG -= p * (Math.log(p) / Math.log(2));
            }
            if (histB[i] > 0) {
                double p = (double) histB[i] / total;
                entropyB -= p * (Math.log(p) / Math.log(2));
            }
        }
        return (entropyR + entropyG + entropyB) / 3.0;
    }

    // Traversal untuk menghitung statistik (max depth, total nodes)
    public static void traverseTree(QuadtreeNode node, int depth, List<Integer> stats) {
        stats.set(0, Math.max(stats.get(0), depth));
        stats.set(1, stats.get(1) + 1);
        if (!node.isLeaf && node.children != null) {
            for (QuadtreeNode child : node.children) {
                traverseTree(child, depth + 1, stats);
            }
        }
    }

    // Proses utama kompresi gambar menggunakan QuadTree
    public static BufferedImage compressImage(BufferedImage image, ErrorMethod method,
            double threshold, int minSize, boolean unlimitedDepth) {
        int width = image.getWidth();
        int height = image.getHeight();
        QuadtreeNode root = new QuadtreeNode(0, 0, width, height, image, method);
        root.subdivide(image, method, threshold, minSize, unlimitedDepth);

        BufferedImage compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = compressed.createGraphics();
        root.draw(g);
        g.dispose();
        return compressed;
    }

    // Main QuadTreeCompressor untuk eksekusi
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Input image absolute path: ");
        String inputPath = sc.nextLine();

        System.out.println("Select error measurement method:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Maximum Pixel Difference");
        System.out.println("4. Entropy");
        int methodChoice = sc.nextInt();
        ErrorMethod method = ErrorMethod.VARIANCE;
        switch(methodChoice) {
            case 1: method = ErrorMethod.VARIANCE; break;
            case 2: method = ErrorMethod.MAD; break;
            case 3: method = ErrorMethod.MAX_DIFF; break;
            case 4: method = ErrorMethod.ENTROPY; break;
            default: System.out.println("Invalid choice, defaulting to Variance.");
        }

        System.out.print("Input error threshold: ");
        double threshold = sc.nextDouble();

        System.out.print("Input minimum block size (pixels): ");
        int minSize = sc.nextInt();

        System.out.print("Input target compression percentage (1.0 = 100%, 0 to disable): ");
        double targetCompression = sc.nextDouble();

        System.out.print("Aktifkan unlimited depth? (true/false): ");
        boolean unlimitedDepth = sc.nextBoolean();
        sc.nextLine(); // consume newline

        System.out.print("Input output compressed image absolute path: ");
        String outputImagePath = sc.nextLine();

        System.out.print("Input output GIF absolute path (press Enter to skip): ");
        String outputGifPath = sc.nextLine();

        try {
            BufferedImage image = ImageIO.read(new File(inputPath));
            long startTime = System.currentTimeMillis();
            BufferedImage compressedImage = compressImage(image, method, threshold, minSize, unlimitedDepth);
            long endTime = System.currentTimeMillis();

            ImageIO.write(compressedImage, "jpg", new File(outputImagePath));

            // Hitung statistik QuadTree
            QuadtreeNode root = new QuadtreeNode(0, 0, image.getWidth(), image.getHeight(), image, method);
            root.subdivide(image, method, threshold, minSize, unlimitedDepth);
            List<Integer> stats = new ArrayList<>();
            stats.add(0); // max depth
            stats.add(0); // node count
            traverseTree(root, 0, stats);
            int maxDepth = stats.get(0);
            int nodeCount = stats.get(1);

            File origFile = new File(inputPath);
            File compFile = new File(outputImagePath);
            long origSize = origFile.length();
            long compSize = compFile.length();
            double compPercentage = (1.0 - ((double) compSize / origSize)) * 100;

            System.out.println("Execution Time: " + (endTime - startTime) + " ms");
            System.out.println("Original Image Size: " + origSize + " bytes");
            System.out.println("Compressed Image Size: " + compSize + " bytes");
            System.out.printf("Compression Percentage: %.2f%%\n", compPercentage);
            System.out.println("QuadTree Max Depth: " + maxDepth);
            System.out.println("Total Number of Nodes: " + nodeCount);
            System.out.println("Output image saved at: " + outputImagePath);

            // Jika outputGifPath tidak kosong, hasilkan GIF visualisasi
            if (!outputGifPath.trim().isEmpty()) {
                // Buat frame dari level 0 hingga maxDepth, tambahkan beberapa frame terakhir
                List<BufferedImage> frames = new ArrayList<>();
                for (int d = 0; d <= maxDepth; d++) {
                    frames.add(createImageAtDepth(image.getWidth(), image.getHeight(), root, d));
                }
                for (int i = 0; i < 5; i++) {
                    frames.add(frames.get(frames.size() - 1));
                }
                generateGif(frames, outputGifPath, 500);
            }
        } catch (IOException e) {
            System.out.println("Error reading or writing image files: " + e.getMessage());
        }
        sc.close();
    }

    // Method untuk membuat gambar pada kedalaman tertentu (untuk GIF)
    public static BufferedImage createImageAtDepth(int width, int height, QuadtreeNode root, int depthLimit) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        drawAtDepth(root, g2d, depthLimit);
        g2d.dispose();
        return output;
    }

    // Fungsi rekursif untuk menggambar QuadTree hingga kedalaman tertentu
    public static void drawAtDepth(QuadtreeNode node, Graphics2D g2d, int depthLimit) {
        if (node == null) return;
        if (node.isLeaf || node.width <= 1 || node.height <= 1 || depthLimit <= 0) {
            g2d.setColor(node.avgColor);
            g2d.fillRect(node.x, node.y, node.width, node.height);
        } else {
            if (depthLimit > 0 && node.children != null) {
                for (QuadtreeNode child : node.children) {
                    drawAtDepth(child, g2d, depthLimit - 1);
                }
            } else {
                g2d.setColor(node.avgColor);
                g2d.fillRect(node.x, node.y, node.width, node.height);
            }
        }
    }

    // Fungsi untuk menghasilkan GIF dari frame-frame yang telah dibuat
    public static void generateGif(List<BufferedImage> frames, String gifFileName, int delayTimeMs) {
        try {
            ImageOutputStream output = ImageIO.createImageOutputStream(new File(gifFileName));
            GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, delayTimeMs, true);
            for (BufferedImage frame : frames) {
                writer.writeToSequence(frame);
            }
            writer.close();
            output.close();
            System.out.println("GIF visualisasi disimpan sebagai: " + gifFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Kelas Inner untuk GifSequenceWriter ===
    public static class GifSequenceWriter {
        protected ImageWriter gifWriter;
        protected ImageWriteParam imageWriteParam;
        protected IIOMetadata imageMetaData;

        public GifSequenceWriter(
                ImageOutputStream outputStream,
                int imageType,
                int timeBetweenFramesMS,
                boolean loopContinuously) throws IOException {
            gifWriter = getWriter();
            imageWriteParam = gifWriter.getDefaultWriteParam();
            IIOMetadata metadata = gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(imageType), imageWriteParam);
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");

            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            int loop = loopContinuously ? 0 : 1;
            child.setUserObject(new byte[]{0x1, (byte)(loop & 0xFF), (byte)((loop >> 8) & 0xFF)});
            appExtensionsNode.appendChild(child);
            metadata.setFromTree(metaFormatName, root);
            imageMetaData = metadata;

            gifWriter.setOutput(outputStream);
            gifWriter.prepareWriteSequence(null);
        }

        public void writeToSequence(RenderedImage img) throws IOException {
            gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        public void close() throws IOException {
            gifWriter.endWriteSequence();
        }

        private static ImageWriter getWriter() throws IOException {
            Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
            if (!iter.hasNext()) {
                throw new IOException("No GIF Image Writers Exist");
            }
            return iter.next();
        }

        private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) rootNode.item(i);
                }
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }
    }
}
