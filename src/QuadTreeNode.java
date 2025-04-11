public class QuadTreeNode {
    private int x, y;
    private int width, height; 
    private Pixel averageColor; 
    private QuadTreeNode topLeft, topRight, bottomLeft, bottomRight;
    private boolean isLeaf;

    public QuadTreeNode(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.averageColor = new Pixel(0, 0, 0);
        this.topLeft = null;
        this.topRight = null;
        this.bottomLeft = null;
        this.bottomRight = null;
        this.isLeaf = true;
    }

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
        if (cnt > 0) {
            this.averageColor = new Pixel(totalR / cnt, totalG / cnt, totalB / cnt);
        } else {
            this.averageColor = new Pixel(0, 0, 0);
        }
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void split() {
        int halfWidth = this.width / 2;
        int halfHeight = this.height / 2;
        int residualWidth = this.width - halfWidth;
        int residualHeight = this.height - halfHeight;
        this.topLeft = new QuadTreeNode(this.x, this.y, halfWidth, halfHeight);
        this.topRight = new QuadTreeNode(this.x + halfWidth, this.y, residualWidth, halfHeight);
        this.bottomLeft = new QuadTreeNode(this.x, this.y + halfHeight, halfWidth, residualHeight);
        this.bottomRight = new QuadTreeNode(this.x + halfWidth, this.y + halfHeight, residualWidth, residualHeight);

        this.isLeaf = false;
    }

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