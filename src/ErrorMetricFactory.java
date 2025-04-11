
public class ErrorMetricFactory {
    public static ErrorMetric createErrorMetric(int input) {
        return switch (input) {
            case 1 -> new VarianceErrorMetric();
            case 2 -> new MADErrorMetric();
            case 3 -> new MaxPixelDifferenceErrorMetric();
            case 4 -> new EntropyErrorMetric();
            case 5 -> new SSIMErrorMetric();
            default -> throw new IllegalArgumentException("Metode error tidak valid.");
        };
    }
}
