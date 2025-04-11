import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
public class CompressionController {

    public static class OptimalParameters {
        public double blockSize;
        public double threshold;

        public OptimalParameters(double blockSize, double threshold) {
            this.blockSize = blockSize;
            this.threshold = threshold;
        }

        @Override
        public String toString() {
            return "OptimalParameters { blockSize = " + blockSize + ", threshold = " + threshold + " }";
        }
    }

    public static double getDefaultThreshold(ErrorMetric errorMetric) {
        String metricName = errorMetric.getName().toLowerCase();
        if (metricName.contains("variance")) {
            return 2000;
        } else if (metricName.contains("max pixel difference")) {
            return 15.0;
        } else if (metricName.contains("mad")) {
            return 10.0;
        } else if (metricName.contains("entropy")) {
            return 5.0;
        } else if (metricName.contains("ssim")) {
            return 0.1;
        } else {
            return 10.0;
        }
    }

    public static BufferedImage compressImage(RGBMatrix rgbMatrix, ErrorMetric errorMetric,
                                                double threshold, int blockSize) {
        QuadTree qt = new QuadTree(rgbMatrix.copy(), errorMetric, threshold, blockSize);
        qt.buildTree();
        return OutputHandler.convertToBufferedImage(qt.getRGBMatrix());
    }

    public static long getImageSizeInBytes(BufferedImage image, String formatName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            baos.flush();
            return baos.toByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double testCompression(RGBMatrix rgbMatrix, ErrorMetric errorMetric,
                                         double blockSize, double threshold, long inputFileSize,
                                         String imageFormat) {
        BufferedImage compressedImage = compressImage(rgbMatrix, errorMetric, threshold, (int) blockSize);
        long compressedSize = getImageSizeInBytes(compressedImage, imageFormat);
        double compressionRate = (1 - ((double) compressedSize / inputFileSize)) * 100.0;
        return compressionRate;
    }

    public static OptimalParameters findOptimalParameters(RGBMatrix rgbMatrix, ErrorMetric errorMetric,
                                                            double targetCompressionRate, String imageFormat,
                                                            boolean useSSIM) {
        BufferedImage originalImage = OutputHandler.convertToBufferedImage(rgbMatrix);
        long inputFileSize = getImageSizeInBytes(originalImage, imageFormat);

        double targetCompression = targetCompressionRate * 100.0;

        int width = rgbMatrix.getWidth();
        int height = rgbMatrix.getHeight();
        int minDimension = Math.min(width, height);
        System.out.println("Target compression: " + targetCompression + "%");

        double maxBlockSize = 1.0;
        while (maxBlockSize * 8 <= minDimension) {
            maxBlockSize *= 8;
        }

        double bestBlockSize = maxBlockSize;
        double bestThreshold = 0.0;
        double closestCompressionRate = 0.0;
        double minDifference = 5.0;

        double detailedStep = useSSIM ? 0.1 : 1.0;
        double detailedTolerance = 1.0;

        boolean foundOptimal = true;
        double blockSize = maxBlockSize; 
        
        while (blockSize >= 4.0 && foundOptimal) {
            System.out.println("Block size: " + blockSize);

            double startThreshold = useSSIM ? 0.1 : 5.0;
            double endThreshold = useSSIM ? 0.9 : 50.0;

            double compressionRateStart = testCompression(rgbMatrix, errorMetric, blockSize, startThreshold, inputFileSize, imageFormat);
            System.out.println("  Threshold: " + startThreshold + ", Compression: " + compressionRateStart + "%");

            double diffStart = Math.abs(compressionRateStart - targetCompression);
            System.out.println("  Diff (start): " + diffStart + "%");

            if (diffStart > 15.0) {
                System.out.println("  Skip. Hasil jauh (>15%) dari target.");
                blockSize /= 4;
                continue;
            }

            if (diffStart < minDifference) {
                minDifference = diffStart;
                closestCompressionRate = compressionRateStart;
                bestBlockSize = blockSize;
                bestThreshold = startThreshold;
            }

            double compressionRateEnd = testCompression(rgbMatrix, errorMetric, blockSize, endThreshold, inputFileSize, imageFormat);
            System.out.println("  End test - Threshold: " + endThreshold + ", Compression: " + compressionRateEnd + "%");


            double diffEnd = Math.abs(compressionRateEnd - targetCompression);
            if (diffEnd < minDifference) {
                minDifference = diffEnd;
                closestCompressionRate = compressionRateEnd;
                bestBlockSize = blockSize;
                bestThreshold = endThreshold;
            }

            if (diffStart <= 5.0 || diffEnd <= 5.0) {
                System.out.println("  Lakukan pencarian detail threshold (step: " + detailedStep + ", toleransi: " + detailedTolerance + "%)");
                double currentThreshold = (Math.abs(compressionRateEnd - targetCompression) < Math.abs(compressionRateStart - targetCompression))
                                          ? endThreshold - detailedStep 
                                          : startThreshold + detailedStep;
                while (currentThreshold >= startThreshold && currentThreshold <= endThreshold) {
                    double currentCompressionRate = testCompression(rgbMatrix, errorMetric, blockSize, currentThreshold, inputFileSize, imageFormat);
                    double diffCurrent = Math.abs(currentCompressionRate - targetCompression);
                    System.out.println("    Testing - Threshold: " + currentThreshold + ", Compression: " + currentCompressionRate + "%, Diff: " + diffCurrent + "%");
    
                    if (diffCurrent < minDifference) {
                        minDifference = diffCurrent;
                        closestCompressionRate = currentCompressionRate;
                        bestBlockSize = blockSize;
                        bestThreshold = currentThreshold;
                        if (diffCurrent <= detailedTolerance) {
                            System.out.println("    Presisi tercapai (diff <= " + detailedTolerance + "%).");
                            foundOptimal = true;
                            break;
                        }
                    }
                    if ((useSSIM && currentCompressionRate < targetCompression) || 
                        (!useSSIM && currentCompressionRate > targetCompression)) {
                        currentThreshold += detailedStep;
                    } else {
                        currentThreshold -= detailedStep;
                    }
                }
                if (foundOptimal) {
                    break;
                }
            }

            blockSize /= 4;
        }

        System.out.println("Aproksimasi terbaik: " + closestCompressionRate + "% (dengan error: " + minDifference + "%)");
        System.out.println("  bestThreshold: " + bestThreshold + "%");
        return new OptimalParameters(bestBlockSize, bestThreshold);
    }

    public CompressedImage compressWithTarget(RGBMatrix rgbMatrix, ErrorMetric errorMetric,
                                                double targetCompression, String imageFormat) {
        if (targetCompression == 0) {
            double defaultThreshold = getDefaultThreshold(errorMetric);
            QuadTree qt = new QuadTree(rgbMatrix.copy(), errorMetric, defaultThreshold, 8);
            qt.buildTree();
            BufferedImage compressedImage = OutputHandler.convertToBufferedImage(qt.getRGBMatrix());
            BufferedImage originalImage = OutputHandler.convertToBufferedImage(rgbMatrix);
            long originalSize = getImageSizeInBytes(originalImage, imageFormat);
            long compressedSize = getImageSizeInBytes(compressedImage, imageFormat);
            double compRate = (1 - ((double) compressedSize / originalSize)) * 100.0;
            return new CompressedImage(compressedImage, compRate, compressedSize, originalSize, qt);
        }

        boolean useSSIM = errorMetric.getName().toLowerCase().contains("ssim");
        OptimalParameters params = findOptimalParameters(rgbMatrix, errorMetric, targetCompression, imageFormat, useSSIM);
        System.out.println("Parameter optimal ditemukan: " + params.blockSize + ", " + params.threshold);
        QuadTree qt = new QuadTree(rgbMatrix.copy(), errorMetric, params.threshold, (int) params.blockSize);
        qt.buildTree();
        BufferedImage finalCompressedImage = OutputHandler.convertToBufferedImage(qt.getRGBMatrix());
        BufferedImage originalImage = OutputHandler.convertToBufferedImage(rgbMatrix);
        long originalSize = getImageSizeInBytes(originalImage, imageFormat);
        long finalSize = getImageSizeInBytes(finalCompressedImage, imageFormat);
        double finalCompression = (1 - ((double) finalSize / originalSize)) * 100.0;
        return new CompressedImage(finalCompressedImage, finalCompression, finalSize, originalSize, qt);
    }
}
