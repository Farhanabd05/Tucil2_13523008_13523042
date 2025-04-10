import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class InputParser {
    private RGBMatrix rgbMatrix;
    private int height, width;
    private String outputPath;
    private ErrorMetric errorMetric;
    private double threshold;
    private int minBlockSize;
    private File inputFile;
    private String gifPath;
    private double targetCompression;
    private boolean isTargetCompressionSet = false;
    private String outputFormat;
    public InputParser() {}

    public void parseInput() throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            // Clear screen and show program logo
            CLIUtils.clearScreen();
            CLIUtils.printLogo();
            
            // Input file selection
            CLIUtils.printSectionHeader("STEP 1: SELECT INPUT IMAGE");
            System.out.print(CLIUtils.BOLD + "Enter path to image file: " + CLIUtils.RESET);
            String inputFilePath = scanner.nextLine();
            
            CLIUtils.simulateLoading("Loading image...", 1500);
            
            // Load image
            this.inputFile = new File(inputFilePath);
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                CLIUtils.printError("Failed to load image. Please ensure the file exists and is a valid image format.");
                throw new IOException("Cannot read image file");
            }
            
            this.width = image.getWidth();
            this.height = image.getHeight();
            CLIUtils.printSuccess("Image loaded successfully: " + width + "×" + height + " pixels");
            
            // Output file selection
            CLIUtils.printSectionHeader("STEP 2: SET OUTPUT LOCATION");
            System.out.print(CLIUtils.BOLD + "Enter path for compressed image: " + CLIUtils.RESET);
            this.outputPath = scanner.nextLine();
            this.outputFormat = outputPath.substring(outputPath.lastIndexOf(".") + 1);
            // Convert image to RGB matrix
            CLIUtils.printInfo("Converting image to RGB matrix...");
            this.rgbMatrix = new RGBMatrix(width, height);
            
            // Show progress bar for conversion
            int[] rgbArray = image.getRGB(0, 0, width, height, null, 0, width);
            int totalPixels = rgbArray.length;
            for (int i = 0; i < rgbArray.length; i++) {
                rgbMatrix.setPixel(i % width, i / width, rgbArray[i]);
                
                if (i % (totalPixels / 100) == 0) {
                    CLIUtils.printProgressBar((i * 100) / totalPixels);
                }
            }
            CLIUtils.printProgressBar(100);
            System.out.println();
            CLIUtils.printSuccess("Image converted to RGB matrix");
            
            // Error metric selection
            CLIUtils.printSectionHeader("STEP 3: COMPRESSION PARAMETERS");
            displayErrorMetrics();
            int errorMetricChoice = scanner.nextInt();
            errorMetric = ErrorMetricFactory.createErrorMetric(errorMetricChoice);
            CLIUtils.printSuccess("Selected error metric: " + errorMetric.getName());

            // Error threshold
            System.out.print(CLIUtils.BOLD + "\nEnter error threshold value: " + CLIUtils.RESET);
            threshold = scanner.nextDouble();

            // Minimum block size
            System.out.print(CLIUtils.BOLD + "\nEnter minimum block size (e.g., 4): " + CLIUtils.RESET);
            minBlockSize = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            // GIF path
            CLIUtils.printSectionHeader("STEP 4: GIF VISUALIZATION");
            System.out.print(CLIUtils.BOLD + "Enter path for GIF animation (leave empty to skip): " + CLIUtils.RESET);
            this.gifPath = scanner.nextLine();
            
            // Target compression
            CLIUtils.printSectionHeader("STEP 5: TARGET COMPRESSION");
            System.out.print(CLIUtils.BOLD + "Enter target compression ratio (e.g., 0.8): " + CLIUtils.RESET);
            this.targetCompression = scanner.nextDouble();
            if (this.targetCompression < 0 || this.targetCompression > 1) {
                CLIUtils.printError("Invalid target compression ratio. Please enter a value between 0 and 1.");
                throw new IOException("Invalid target compression ratio");
            }

            if (this.targetCompression > 0) {
                this.isTargetCompressionSet = true;
            }
            
            // Display summary
            CLIUtils.printSectionHeader("READY TO COMPRESS");
            CLIUtils.printInfo("Image: " + inputFile.getName() + " (" + width + "×" + height + ")");
            CLIUtils.printInfo("Error metric: " + errorMetric.getName());
            CLIUtils.printInfo("Error threshold: " + threshold);
            CLIUtils.printInfo("Minimum block size: " + minBlockSize);
            
            if (!gifPath.isEmpty()) {
                CLIUtils.printInfo("GIF animation will be created at: " + gifPath);
            } else {
                CLIUtils.printWarning("GIF animation skipped");
            }
            
            System.out.println(CLIUtils.BOLD + CLIUtils.GREEN + "\nStarting compression in 3 seconds..." + CLIUtils.RESET);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            CLIUtils.clearScreen();
        }
    }

    public static void displayErrorMetrics() {
        System.out.println(CLIUtils.BOLD + "Available Error Metrics:" + CLIUtils.RESET);
        System.out.println(CLIUtils.CYAN + "1. " + CLIUtils.RESET + "Variance (Standard deviation of pixel values)");
        System.out.println(CLIUtils.CYAN + "2. " + CLIUtils.RESET + "Mean Absolute Deviation (Average difference from mean)");
        System.out.println(CLIUtils.CYAN + "3. " + CLIUtils.RESET + "Max Pixel Difference (Maximum difference between pixels)");
        System.out.println(CLIUtils.CYAN + "4. " + CLIUtils.RESET + "Entropy (Information content measure)");
        System.out.print(CLIUtils.BOLD + "\nSelect error metric (1-4): " + CLIUtils.RESET);
    }
    
    // Getters remain unchanged
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public RGBMatrix getRGBMatrix() { return rgbMatrix; }
    public String getOutputPath() { return outputPath; }
    public ErrorMetric getErrorMetric() { return errorMetric; }
    public double getThreshold() { return threshold; }
    public int getMinBlockSize() { return minBlockSize; }
    public File getInputFile() { return inputFile; }
    public String getGifPath() { return gifPath; }
    public double getTargetCompression() { return targetCompression; }
    public boolean isTargetCompressionSet() { return isTargetCompressionSet; }
    public String getOutputImageFormat() { return outputFormat; }
}