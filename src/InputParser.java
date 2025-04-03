import java.io.File;

public class InputParser {
    public static int parseErrorMethod(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // gunakan nilai default
                return 1;
            }
        }
        return 1;
    }

    public static double parseThreshold(String[] args) {
        if (args.length > 1) {
            try {
                return Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {}
        }
        return 15.0;
    }

    public static int parseMinBlockSize(String[] args) {
        if (args.length > 2) {
            try {
                return Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {}
        }
        return 4;
    }

    public static String parseInputImagePath(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "tc";
        String fileName = args.length > 3 ? args[3] : "alone.jpg";
        return testPath + File.separator + fileName;
    }

    public static String parseOutputImagePath(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String testPath = currentDir.substring(0, currentDir.lastIndexOf(File.separator)) + File.separator + "test" + File.separator + "sol";
        String fileName = args.length > 3 ? args[4] : "alone.jpg";
        return testPath + File.separator + fileName;
    }
}
