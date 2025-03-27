public class RGBMatrix {
    private int width, height;
    private Pixel[][] matrix;

    public RGBMatrix(int width, int height) {
        this.width = width;
        this.height = height;
        matrix = new Pixel[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                matrix[i][j] = new Pixel(0, 0, 0);
            }
        }
        System.out.println("RGBMatrix created with width=" + width + " and height=" + height);
    }

    public void setPixel(int x, int y, int r, int g, int b) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            System.err.println("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
            return;
        }
        matrix[y][x].setR(r);
        matrix[y][x].setG(g);
        matrix[y][x].setB(b);
    }

    public Pixel getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            System.err.println("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
            return null;
        }
        return matrix[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Fungsi ini dapat digunakan untuk mencetak seluruh matriks RGB (untuk gambar kecil)
    public void printMatrix() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(getPixel(x, y) + " ");
            }
            System.out.println();
        }
    }


}
