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

    public void setPixel(int x, int y, int pixel) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        matrix[y][x].setRGB(pixel);
    }

    // ini untuk print sebagian saja buat debugging tipis2 ngeck warnanya bener atau kgk
    public void printRGB() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                System.out.println("DEBUG: Pixel di (" + x + ", " + y + ") = (" 
                + matrix[x][y].getR() + ", " + matrix[x][y].getG() + ", " + matrix[x][y].getB() + ")");
            }
        }
    }

    public Pixel getPixel(int x, int y) {
        if (isOutOfBounds(x, y)) {
            throw new IllegalArgumentException("ERROR: Posisi pixel (" + x + ", " + y + ") di luar batas.");
        }
        return matrix[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }
}


