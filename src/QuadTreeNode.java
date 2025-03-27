public class QuadTreeNode {
    private int x, y, width, height;
    private Pixel avgColor;
    private double error;
    private QuadTreeNode[] children;
    private boolean isLeaf;
    private int errorMethod;

    public QuadTreeNode(int x, int y, int width, int height, Pixel avgColor, double error, int errorMethod) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.avgColor = avgColor;
        this.errorMethod = errorMethod;
        this.children = new QuadTreeNode[4];
        this.isLeaf = true;
    }

    public void split(RGBMatrix mat, double threshold, int minBlockSize) {
        if (width <= minBlockSize || height <= minBlockSize || error < threshold) {
            System.out.println("block tidak dapat dibagia lagi: (" + x + ", " + y + ")"+ " dimensi: " + width + " x " + height + " error: " + error);
            return;
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        children[0] = createChild(mat, x,               y,              halfWidth,   halfHeight, errorMethod);
        children[1] = createChild(mat, x + halfWidth,   y,              halfWidth,   halfHeight, errorMethod);
        children[2] = createChild(mat, x,               y + halfHeight, halfWidth,   halfHeight, errorMethod);
        children[3] = createChild(mat, x + halfWidth,   y + halfHeight, halfWidth,   halfHeight, errorMethod);
        isLeaf = false;

        for (QuadTreeNode child : children) {
            child.split(mat, threshold, minBlockSize);  
        }
    }

    private QuadTreeNode createChild(RGBMatrix mat, int x, int y, int w, int h, int errorMethod) {
        Pixel avgColor = mat.getAverageColor(x, y, w, h);
        double error = mat.getError(x, y, w, h, avgColor, errorMethod);
        return new QuadTreeNode(x, y, w, h, avgColor, error, errorMethod);
    }
}
