public class RGBMatrix {
    private int width, height;
    private Pixel[] pixels;

    public RGBMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = new Pixel(0, 0, 0);
        }
        System.out.println("RGBMatrix created with width=" + width + " and height=" + height);
    }

    public void setPixel(int x, int y, int pixel) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        pixels[y * width + x].setRGB(pixel);
    }

    public Pixel getPixel(int x, int y) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        return pixels[y * width + x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Pixel[] getPixels() {
        return pixels;
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public void printRGB() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                System.out.print((getPixel(x, y)) + " ");
                System.out.println();
            }
        }
    }
}

