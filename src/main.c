#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define min(a, b) ((a) < (b) ? (a) : (b))
// rgb struct
// untuk menyimpan warna

typedef struct {
    unsigned char r;
	unsigned char g;
	unsigned char b;
} rgb;

// quadTreeNode struct

typedef struct quadTreeNode {
    rgb color;
    long long index;
    uint32_t area;

    struct quadTreeNode *topLeft, *topRight, *bottomLeft, *bottomRight;
} quadTreeNode;

// vector struct

typedef struct vquadTreeNode {
    rgb color;
    uint32_t area;
    int32_t topLeft, topRight, bottomLeft, bottomRight;
} vquadTreeNode;

/*
 *	Function used for reading from a file
 *	returns a rgb matrix
 */
rgb ** readFile(int * rows, int * cols, char * filename) {
    FILE *fp;
    char magic_number[3];
    int max_color;
    int i, j;

    fp = fopen(filename, "rb");

    fscanf(fp, "%s", magic_number);
    fscanf(fp, "%d %d\n", cols, rows);
    fscanf(fp, "%d\n", &max_color);

    char garbage;
    fread(&garbage, sizeof(char), 1, fp);

    rgb **mat = (rgb **)malloc(sizeof(rgb*) * (*rows));
    for (i = 0; i < *rows; i++) {
        mat[i] = malloc(sizeof(rgb) * (*cols));
        fread(mat[i], sizeof(rgb), *cols, fp);
    }
    fclose(fp);
    return mat;
}

/*
 *	Compression function
 */

void compress(rgb ** mat, quadTreeNode ** node, int row, int col, int size, int threshold) {
    uint8_t r = 0, g = 0, b = 0, mean = 0;

    (*node) = malloc(sizeof(quadTreeNode));
    (*node)->area = size * size;

    // compute medium rgb on the matrix
    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            r += mat[i][j].r;
            g += mat[i][j].g;
            b += mat[i][j].b;
        }
    }
    (*node)->color.r = r / (size * size);
    (*node)->color.g = g / (size * size);
    (*node)->color.b = b / (size * size);

    if (row == 1 || col == 1) {
        return;
    }

    // compute score
    int i, j;
    for (i = col; i < col + size; i++) {
        for (j = row; j < row + size; j++) {
            mean += (r - mat[i][j].r) * (r - mat[i][j].r) 
                  + (g - mat[i][j].g) * (g - mat[i][j].g) 
                  + (b - mat[i][j].b) * (b - mat[i][j].b);
        }
    }
    mean = mean / (3 * size * size);

    if (mean > threshold) {
        compress(mat, &((*node)->topLeft),      row,            col,            size / 2, threshold);
        compress(mat, &((*node)->topRight),     row + size / 2, col,            size / 2, threshold);
        compress(mat, &((*node)->bottomLeft),   row,            col + size / 2, size / 2, threshold);
        compress(mat, &((*node)->bottomRight),  row + size / 2, col + size / 2, size / 2, threshold);
    }
    else {
        (*node)->topLeft        = NULL;
        (*node)->topRight       = NULL;
        (*node)->bottomLeft     = NULL;
        (*node)->bottomRight    = NULL;
    }
}

/*
 * Function used to copy nodes addresses 
 * from the tree into a pointer vector
 */

void traverse(quadTreeNode * node, quadTreeNode ** nodes[], unsigned int * index) {
    if (node == NULL) {
        return;
    }
    if (*index > 0) {
        *nodes = realloc(*nodes, sizeof(quadTreeNode *) * (*index + 1));
    }
    (*nodes)[*index] = node;
    node->index = *index;
    (*index)++;
    traverse(node->topLeft,     nodes, index);
    traverse(node->topRight,    nodes, index);
    traverse(node->bottomLeft,  nodes, index);
    traverse(node->bottomRight, nodes, index);
}

/*
 *	Using the vector from above 
 *	computes the vector that needs to be written
 */

void computeVector(quadTreeNode ** nodes, vquadTreeNode ** vector, int index) {
    unsigned int i = 0;
    for (i = 0; i < index; i++) {
        (*vector)[i].color = nodes[i]->color;
        (*vector)[i].area = nodes[i]->area;
        if (nodes[i]->topLeft != NULL) {
            (*vector)[i].topLeft = nodes[i]->topLeft->index;
        } else {
            (*vector)[i].topLeft = -1;
        }

        if (nodes[i]->topRight != NULL) {
            (*vector)[i].topRight = nodes[i]->topRight->index;
        } else {
            (*vector)[i].topRight = -1;
        }

        if (nodes[i]->bottomLeft != NULL) {
            (*vector)[i].bottomLeft = nodes[i]->bottomLeft->index;
        } else {
            (*vector)[i].bottomLeft = -1;
        }

        if (nodes[i]->bottomRight != NULL) {
            (*vector)[i].bottomRight = nodes[i]->bottomRight->index;
        } else {
            (*vector)[i].bottomRight = -1;
        }
    }
}

/*
 *	Read a tree struct from a vector
 */

void readTree(vquadTreeNode * vector, quadTreeNode ** nodes, int index) {
    (*nodes) = malloc(sizeof(quadTreeNode));
    (*nodes)->color = vector[index].color;
    (*nodes)->area = vector[index].area;

    (*nodes)->index = index;
    if (vector[index].topLeft != -1) {
        readTree(vector, &((*nodes)->topLeft), vector[index].topLeft);
    } else {
        (*nodes)->topLeft = NULL;
    }

    if (vector[index].topRight != -1) {
        readTree(vector, &((*nodes)->topRight), vector[index].topRight);
    } else {
        (*nodes)->topRight = NULL;
    }

    if (vector[index].bottomLeft != -1) {
        readTree(vector, &((*nodes)->bottomLeft), vector[index].bottomLeft);
    } else {
        (*nodes)->bottomLeft = NULL;
    }

    if (vector[index].bottomRight != -1) {
        readTree(vector, &((*nodes)->bottomRight), vector[index].bottomRight);
    } else {
        (*nodes)->bottomRight = NULL;
    }


}

int main(int argc, char *argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s <input_file> <threshold> <output_file>\n", argv[0]);
        return 1;
    }


    unsigned int index = 0;
    unsigned int i;
    int threshold = atoi(argv[2]);
    int rows, cols;

    rgb **mat;
    quadTreeNode *root = NULL;
    
    mat = readFile(&rows, &cols, argv[1]);
    compress(mat, &root, 0, 0, min(rows, cols), threshold);

    // vector of pointers to the tree nodes
    quadTreeNode ** nodes = malloc(sizeof(quadTreeNode*));
    traverse(root, &nodes, &index);

    vquadTreeNode * vector = malloc(sizeof(vquadTreeNode) * index);
    computeVector(nodes, &vector, index);

    for (i = 0; i < index; i++) {
        free(nodes[i]);
    }
    free(nodes);

    // write vector to file
    FILE * output = fopen(argv[3], "wb");

    unsigned int count = 0;
    for (i = 0; i < index; i++) {
        if (vector[i].topLeft == -1) {
            count++;
        }
    }
    fwrite(&count, sizeof(int), 1, output);
    fwrite(&index, sizeof(int), 1, output);

    for (i = 0; i < index; i++) {
        fwrite(&vector[i], sizeof(vquadTreeNode), 1, output);
        if (vector[i].topLeft == -1) {
            count++;
        }
    }

    free(vector);
    for (i = 0; i < rows; i++) {
        free(mat[i]);
    }
    free(mat);
    fclose(output);

    return 0;
}