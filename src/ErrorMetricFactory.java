/**
 * ErrorMetricFactory.java
 * 
 * Kelas factory untuk menciptakan objek ErrorMetric yang sesuai berdasarkan
 * input dari user. Factory ini menyediakan metode untuk membuat berbagai implementasi
 * ErrorMetric yang tersedia.
 */
public class ErrorMetricFactory {
    /**
     * Membuat objek ErrorMetric berdasarkan input dari user.
     * 
     * @param input metode yang dipilih oleh user (1 - 5)
     * @return objek ErrorMetric yang sesuai
     * @throws IllegalArgumentException jika input tidak valid
     */
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
