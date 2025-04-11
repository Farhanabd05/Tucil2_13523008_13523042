public class CLIUtils {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Background colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Text styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    public static void printLogo() {
        System.out.println(CYAN + BOLD);
        System.out.println(" █████╗ ██╗   ██╗ █████╗ ██████╗ ████████╗██████╗ ███████╗███████╗");
        System.out.println("██╔══██╗██║   ██║██╔══██╗██╔══██╗╚══██╔══╝██╔══██╗██╔════╝██╔════╝");
        System.out.println("██║  ██║██║   ██║███████║██║  ██║   ██║   ██████╔╝█████╗  █████╗  ");
        System.out.println("██║  ██║██║   ██║██╔══██║██║  ██║   ██║   ██╔══██╗██╔══╝  ██╔══╝  ");
        System.out.println("╚█████╔╝╚██████╔╝██║  ██║██████╔╝   ██║   ██║  ██║███████╗███████╗");
        System.out.println(" ╚════╝  ╚═════╝ ╚═╝  ╚═╝╚═════╝    ╚═╝   ╚═╝  ╚═╝╚══════╝╚══════╝");
        System.out.println("                                                                   ");
        System.out.println(RESET);
        
        System.out.println(YELLOW + BOLD + "✨ Advanced Image Compression using Quadtree Algorithm ✨" + RESET);
        System.out.println(PURPLE + "Created by: ITB Students (13523008 & 13523042)" + RESET);
        System.out.println(CYAN + "═══════════════════════════════════════════════════════════════" + RESET);
        System.out.println();
    }
    
    public static void printSectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + BLUE + "┌─────────────────────────────────────────────┐" + RESET);
        System.out.println(BOLD + BLUE + "│ " + YELLOW + title + 
                " ".repeat(Math.max(0, 41 - title.length())) + BLUE + "│" + RESET);
        System.out.println(BOLD + BLUE + "└─────────────────────────────────────────────┘" + RESET);
    }

    public static void printProgressBar(int percent) {
        final int width = 50; // Progress bar width
        int completed = (int)((double)percent / 100 * width);
        
        System.out.print("\r[");
        for (int i = 0; i < width; i++) {
            if (i < completed) {
                System.out.print(GREEN + "█" + RESET);
            } else {
                System.out.print(" ");
            }
        }
        System.out.print("] " + percent + "%");
    }
    
    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }
    
    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }
    
    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }
    
    public static void simulateLoading(String message, int durationMs) {
        String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs;
        int frameIndex = 0;
        
        while (System.currentTimeMillis() < endTime) {
            System.out.print("\r" + CYAN + frames[frameIndex] + " " + message + RESET);
            frameIndex = (frameIndex + 1) % frames.length;
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println();
    }
}