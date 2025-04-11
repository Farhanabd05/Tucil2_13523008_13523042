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
            // CLIUtils.clearScreen();
            CLIUtils.printLogo();
            
            // STEP 1: Get input image path
            CLIUtils.printSectionHeader("STEP 1: SELECT INPUT IMAGE");
            System.out.println(CLIUtils.BOLD + "Enter path to image file:" + CLIUtils.RESET);
            System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            String inputFilePath = scanner.nextLine();
            
            CLIUtils.simulateLoading("Loading image...", 1500);
            
            // Load and validate the image
            this.inputFile = new File(inputFilePath);

            // Periksa format file
            String fileExtension = getFileExtension(inputFile);
            if (!fileExtension.equalsIgnoreCase("jpg") && !fileExtension.equalsIgnoreCase("jpeg") && !fileExtension.equalsIgnoreCase("png")) {
                CLIUtils.printError("Format file tidak didukung. Hanya JPG, JPEG, dan PNG yang diperbolehkan.");
                throw new IOException("Unsupported file format");
            }
            
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                CLIUtils.printError("Failed to load image. Please ensure the file exists and is a valid image format.");
                throw new IOException("Cannot read image file");
            }
            
            this.width = image.getWidth();
            this.height = image.getHeight();
            CLIUtils.printSuccess("Image loaded successfully: " + width + "×" + height + " pixels");
            
            // STEP 2: Set output location
            CLIUtils.printSectionHeader("STEP 2: SET OUTPUT LOCATION");
            System.out.println(CLIUtils.BOLD + "Enter path for compressed image:" + CLIUtils.RESET);
            System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            this.outputPath = scanner.nextLine();

            // cek jika output format tidak sama dengan input format throw error
            this.outputFormat = outputPath.substring(outputPath.lastIndexOf(".") + 1);
            if (!outputFormat.equalsIgnoreCase(getFileExtension(inputFile))) {
                CLIUtils.printError("Format output tidak sama dengan format input. Silakan gunakan format yang sama.");
                throw new IOException("Output format mismatch");
            }
            // Convert image to RGB matrix with progress bar
            CLIUtils.printInfo("Converting image to RGB matrix...");
            this.rgbMatrix = new RGBMatrix(width, height);
            
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
            
            // STEP 3: Target compression ratio
            CLIUtils.printSectionHeader("STEP 3: TARGET COMPRESSION");
            System.out.println(CLIUtils.BOLD + "Enter target compression ratio (0 - 1) or enter 0 to use threshold-based compression:" + CLIUtils.RESET);
            System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            
            while (true) {
                try {
                    this.targetCompression = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    
                    if (this.targetCompression < 0 || this.targetCompression > 1) {
                        CLIUtils.printError("Invalid target compression ratio. Please enter a value between 0 and 1.");
                        System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    scanner.nextLine(); // Clear the scanner
                    CLIUtils.printError("Input must be a number. Please try again.");
                    System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                }
            }

            if (this.targetCompression > 0) {
                this.isTargetCompressionSet = true;
                CLIUtils.printSuccess("Target compression ratio set to " + this.targetCompression);
            } else {
                CLIUtils.printInfo("Using threshold-based compression instead of target ratio");
            }
            
            // STEP 4: Compression parameters
            CLIUtils.printSectionHeader("STEP 4: COMPRESSION PARAMETERS");
            displayErrorMetrics();
            
            // Get and validate error metric selection
            int errorMetricChoice;
            System.out.println(CLIUtils.BOLD + "Select error metric (1-5):" + CLIUtils.RESET);
            System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            
            while (true) {
                try {
                    errorMetricChoice = scanner.nextInt();
                    if (errorMetricChoice < 1 || errorMetricChoice > 5) {
                        CLIUtils.printError("Please enter a number between 1 and 5.");
                        System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    scanner.nextLine(); // Clear the scanner
                    CLIUtils.printError("Input must be a number. Please try again.");
                    System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                }
            }
            
            errorMetric = ErrorMetricFactory.createErrorMetric(errorMetricChoice);
            CLIUtils.printSuccess("Selected error metric: " + errorMetric.getName());

            // If not using target compression, get threshold and min block size
            if (!this.isTargetCompressionSet) {
                // Get and validate threshold based on selected error metric
                this.threshold = getValidThreshold(scanner, errorMetricChoice);
                
                // Get and validate minimum block size
                this.minBlockSize = getValidMinBlockSize(scanner);
            } else {
                scanner.nextLine(); // Consume newline
            }

            // STEP 5: GIF visualization path
            CLIUtils.printSectionHeader("STEP 5: GIF VISUALIZATION");
            System.out.println(CLIUtils.BOLD + "Enter path for GIF animation (leave empty to skip):" + CLIUtils.RESET);
            System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            this.gifPath = scanner.nextLine();
            
            // Display summary and prepare for compression
            CLIUtils.printSectionHeader("READY TO COMPRESS");
            CLIUtils.printInfo("Image: " + inputFile.getName() + " (" + width + "×" + height + ")");
            CLIUtils.printInfo("Error metric: " + errorMetric.getName());
            
            if (!isTargetCompressionSet) {
                CLIUtils.printInfo("Error threshold: " + threshold);
                CLIUtils.printInfo("Minimum block size: " + minBlockSize);
            } else {
                CLIUtils.printInfo("Target compression ratio: " + targetCompression);
            }
            
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
        }
    }

    private double getValidThreshold(Scanner scanner, int errorMetricChoice) {
        double thresholdValue;
        scanner.nextLine(); // Consume newline
        
        System.out.println(CLIUtils.BOLD + "Enter error threshold value:" + CLIUtils.RESET);
        System.out.println(CLIUtils.CYAN + "Note - Valid ranges for each metric:" + CLIUtils.RESET);
        System.out.println("1. Variance: (0 - 16256.25)");
        System.out.println("2. Mean Absolute Deviation: (0 - 127.5)");
        System.out.println("3. Max Pixel Difference: (0 - 255)");
        System.out.println("4. Entropy: (0 - 8)");
        System.out.println("5. SSIM: (0 - 1)");
        System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
        
        while (true) {
            try {
                thresholdValue = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                
                boolean isValid = true;
                
                // Validate threshold based on selected error metric
                switch (errorMetricChoice) {
                    case 1: // Variance
                        if (thresholdValue < 0 || thresholdValue > 16256.25) {
                            CLIUtils.printError("Invalid range! Threshold for Variance must be between 0 and 16256.25");
                            isValid = false;
                        }
                        break;
                    case 2: // MAD
                        if (thresholdValue < 0 || thresholdValue > 127.5) {
                            CLIUtils.printError("Invalid range! Threshold for MAD must be between 0 and 127.5");
                            isValid = false;
                        }
                        break;
                    case 3: // Max Pixel Difference
                        if (thresholdValue < 0 || thresholdValue > 255) {
                            CLIUtils.printError("Invalid range! Threshold for Max Pixel Difference must be between 0 and 255");
                            isValid = false;
                        }
                        break;
                    case 4: // Entropy
                        if (thresholdValue < 0 || thresholdValue > 8) {
                            CLIUtils.printError("Invalid range! Threshold for Entropy must be between 0 and 8");
                            isValid = false;
                        }
                        break;
                    case 5: // SSIM
                        if (thresholdValue < 0 || thresholdValue > 1) {
                            CLIUtils.printError("Invalid range! Threshold for SSIM must be between 0 and 1");
                            isValid = false;
                        }
                        break;
                }
                
                if (isValid) {
                    CLIUtils.printSuccess("Threshold value set to: " + thresholdValue);
                    return thresholdValue;
                } else {
                    System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                }
            } catch (Exception e) {
                scanner.nextLine(); // Clear the scanner
                CLIUtils.printError("Input must be a number. Please try again.");
                System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            }
        }
    }
    
    private int getValidMinBlockSize(Scanner scanner) {
        int minSize;
        
        System.out.println(CLIUtils.BOLD + "Enter minimum block size:" + CLIUtils.RESET);
        System.out.println(CLIUtils.CYAN + "Note: Size must be positive and not larger than the original image dimensions" + CLIUtils.RESET);
        System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
        
        while (true) {
            try {
                minSize = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                if (minSize <= 0) {
                    CLIUtils.printError("Minimum size must be positive. Please try again.");
                    System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                } else if (minSize > height * width) {
                    CLIUtils.printError("Minimum size cannot be larger than the image size (" + (height * width) + " pixels). Please try again.");
                    System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
                } else {
                    CLIUtils.printSuccess("Minimum block size set to: " + minSize);
                    return minSize;
                }
            } catch (Exception e) {
                scanner.nextLine(); // Clear the scanner
                CLIUtils.printError("Input must be a number. Please try again.");
                System.out.print(CLIUtils.GREEN + ">> " + CLIUtils.RESET);
            }
        }
    }

    public static void displayErrorMetrics() {
        System.out.println(CLIUtils.BOLD + "Available Error Metrics:" + CLIUtils.RESET);
        System.out.println(CLIUtils.CYAN + "1. " + CLIUtils.RESET + "Variance (Standard deviation of pixel values)");
        System.out.println(CLIUtils.CYAN + "2. " + CLIUtils.RESET + "Mean Absolute Deviation (Average difference from mean)");
        System.out.println(CLIUtils.CYAN + "3. " + CLIUtils.RESET + "Max Pixel Difference (Maximum difference between pixels)");
        System.out.println(CLIUtils.CYAN + "4. " + CLIUtils.RESET + "Entropy (Information content measure)");
        System.out.println(CLIUtils.CYAN + "5. " + CLIUtils.RESET + "SSIM (Structural Similarity Index)");
    }
    
    // Getters
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
    // Fungsi untuk mendapatkan ekstensi file
    private String getFileExtension(File file) {
        String fileName = file.getName();
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > 0) {
            return fileName.substring(extensionIndex + 1).toLowerCase();
        } else {
            return "";
        }
    }
}