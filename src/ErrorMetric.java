public interface ErrorMetric {
    double calculateError(RGBMatrix rgbMatrix, int x, int y, int width, int height);

    String getName();
}