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
        if (args.length > 3) {
            return args[3];
        }
        return "alone.jpg";
    }

    public static String parseOutputImagePath(String[] args) {
        if (args.length > 4) {
            return args[4];
        }
        return "aloneout.jpg";
    }
}
