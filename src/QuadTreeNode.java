/*
 * QuadTreeNode.java
 *
 * kelas ini merepresntasikan node dalam struktur pohon quadtree kompresi gamber
 * setiap node menyimpan informasi tentang posisi , ukuran, dan warna rata rata dari 
 * area piksel yang diwakilinya, serta referensi ke node anak (jika ada). 
*/

public class QuadTreeNode {
    // posisi awal dari area yang direpresentasikan oleh node ini
    private int x, y;
    
    // dimensi dari area yang direpresentasikan oleh node ini
    private int width, height;

    // warna rata-rata dari area yang direpresentasikan oleh node ini
    private Pixel averageColor;

    // referensi ke node anak
    private QuadTreeNode topLeft, topRight, bottomLeft, bottomRight;

    // flag yang menunggu apakah node ini merupakan anak atau tidak
    private boolean isLeaf;

    // konstruktor
    public QuadTreeNode(int x, int y, int width, int height) {
        /*
         * konstruktor untuk membuat objek QuadTreeNode
         * @param x posisi x
         * @param y posisi y
         * @param width lebar
         * @param height tinggi
        */
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.averageColor = null;
        this.topLeft = null;
        this.topRight = null;
        this.bottomLeft = null;
        this.bottomRight = null;
        this.isLeaf = true;
    }

    /*
     * Menghitung nilai rata rata RGB untuk area yang direpresentasikan node.
     * 
     * @param rgbMatrix matriks RGB
    */
    public void calculateAverageColor(RGBMatrix rgbMatrix) {
        int totalR = 0, totalG = 0, totalB = 0, cnt = 0;
        for (int y = this.y; y < this.y + this.height; y++) {
            for (int x = this.x; x < this.x + this.width; x++) {
                Pixel col = rgbMatrix.getPixel(x, y);
                totalR += col.getR();
                totalG += col.getG();
                totalB += col.getB();
                cnt++;
            }
        }
        this.averageColor = new Pixel(totalR / cnt, totalG / cnt, totalB / cnt);
    }

    /*
     * Menentukan apakah node ini merupakan anak atau tidak.
     * 
     * @return true jika node ini merupakan anak, false jika tidak
    */
    public boolean isLeaf() {
        return isLeaf;
    }

    /*
     * mengubah status nide menjadi non leaf dan membuat 4 anak baru
    */
    public void split() {
        int halfWidth = this.width / 2;
        int halfHeight = this.height / 2;
        int residualWidth = this.width - halfWidth;
        int residualHeight = this.height - halfHeight;
        // kiri atas
        this.topLeft = new QuadTreeNode(this.x, this.y, halfWidth, halfHeight);
        // kanan atas
        this.topRight = new QuadTreeNode(this.x + halfWidth, this.y, residualWidth, halfHeight);
        // kiri bawah
        this.bottomLeft = new QuadTreeNode(this.x, this.y + halfHeight, halfWidth, residualHeight);
        // kanan bawah
        this.bottomRight = new QuadTreeNode(this.x + halfWidth, this.y + halfHeight, residualWidth, residualHeight);

        this.isLeaf = false;
    }

    // Getter dan setter
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Pixel getAverageColor() { return averageColor; }
    public QuadTreeNode getTopLeft() { return topLeft; }
    public QuadTreeNode getTopRight() { return topRight; }
    public QuadTreeNode getBottomLeft() { return bottomLeft; }
    public QuadTreeNode getBottomRight() { return bottomRight; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setAvgerageColor(Pixel averageColor) { this.averageColor = averageColor; }
    public void setTopLeft(QuadTreeNode topLeft) { this.topLeft = topLeft; }
    public void setTopRight(QuadTreeNode topRight) { this.topRight = topRight; }
    public void setBottomLeft(QuadTreeNode bottomLeft) { this.bottomLeft = bottomLeft; }
    public void setBottomRight(QuadTreeNode bottomRight) { this.bottomRight = bottomRight; }


    public int getAverageColorRGB() {
        return (averageColor.getRGB());
    }

    public void setAvgerageColorRGB(int rgb) {
        averageColor.setRGB(rgb);
    }
}