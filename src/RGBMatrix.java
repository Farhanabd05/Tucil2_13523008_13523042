
import java.awt.Color;

public class RGBMatrix {
    private Color[][] matrix;

    public RGBMatrix(Color[][] matrix) {
        this.matrix = matrix;
    }

    public int getWidth() {
        return matrix[0].length;
    }

    public int getHeight() {
        return matrix.length;
    }

    public Color getColor(int x, int y) {
        return matrix[y][x];
    }
    
    public Color[][] getMatrix() {
        return matrix;
    }
}
