/* Names: Emma Lee and Rachel Nguyen
* File: QuadTree.java
* Desc: 
* 
* A class to define a QuadTree.
* 
* Takes an image and subdivides it based on an arbitrary detail threshold.
* Supports convultion filter edge detection and image compression.
* 
*/

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayDeque;

public class QuadTree {
    public static final double THRESHOLD = 5; // arbitrarily chosen threshold to determine level of detail
    // arbitrarily chosen number of pixels to determine if a node is of a sufficiently small size in order to apply edge detection
    public static final double SUFFICIENTLY_SMALL_NODE_SIZE = 20;
    public static final int[] EDGE_DETECT_KERNEL = {-1, -1, -1, -1, 8, -1, -1, -1, -1};
    public static final int EDGE_THRESHOLD = 300; // value to determine if a pixel is above a certain contrast as compared to neighbors

    private Image img; // the main image to be subdivided into QuadTree nodes
    private Node root; // the root of the QuadTree
    private int size; // the size of the QuadTree
    
    //---------------- nested Node class ----------------
    private static class Node {
        private int x; // the x coordinate of the upper leftmost pixel of the image
        private int y; // the y coordiante of the upper leftmost pixel of the image
        private int height; // the height of the Node
        private int width; // the width of the Node
        private Node nw; // a reference to the Node that contains the northwest portion
        private Node ne; // a reference to the Node that contains the northeast portion
        private Node sw; // a reference to the Node that contains the southwest portion
        private Node se; // a reference to the Node that contains the southeast portion
        
        /** Creates a Node with the given x, y, height, and width
        * @param x The x coordinate
        * @param y The y coordinate
        * @param h The height
        * @param w The width
        */
        public Node(int x, int y, int h, int w) {
            this.x = x;
            this.y = y;
            height = h;
            width = w;
        }
        
        // public accessor methods
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getHeight() {
            return height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public Node getNE() {
            return ne;
        }
        
        public Node getNW() {
            return nw;
        }
        
        public Node getSE() {
            return se;
        }
        
        public Node getSW() {
            return sw;
        }
        
        // update methods
        public void setNW(Node nw) {
            this.nw = nw;
        }
        
        public void setNE(Node ne) {
            this.ne = ne;
        }
        
        public void setSW(Node sw) {
            this.sw = sw;
        }
        
        public void setSE(Node se) {
            this.se = se;
        }
        
        /* Returns whether or not a Node is a leaf
        * @return boolean True if the Node is a leaf; false if otherwise
        */
        public boolean isLeaf() {
            return nw == null && ne == null && sw == null && se == null;
        }
        
        /** Returns a String representation of the Node
        *  @return String the String representation of the Node
        */
        public String toString() {
            return "node at (" + getX() +"," + getY() + ") with a height of " + getHeight() + " and a width of " + getWidth();
        }
    } //----------- end of nested Node class -----------
    
    /** Creates a QuadTree with the given image
    * @param img The image to be represented
    */
    public QuadTree(Image img) {
        this.img = img;
        this.root = new Node(0, 0, img.getHeight(), img.getWidth());
        this.size = 1;
    }
    
    // getter methods
    public Node getRoot() {
        return root;
    }
    
    public int size() {
        return size;
    }
    
    // setter
    public void setImg(Image img) {
        this.img = img;
    }

    /** Returns whether or not the QuadTree is empty
    * @return boolean True if the QuadTree is empty; false if otherwise
    */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /** Returns the current compression level, which is the number 
    * of leaves over the total amount of pixels in the image
    * @return double The current compression level
    */
    public double getCompressionLevel() {
        return ((double) getNumLeaves(root))/(img.getHeight()*img.getWidth());
    }
    
    /** Returns the mean color of the pixels in a given Node
    * @param node The given Node
    * @return Color The mean color
    */
    public Color meanColor(Node node) {
        // the array to be converted into a Color to be returned
        int[] rgb = new int[3];
        
        double totalRed = 0; // accumulator variable for the total red value of all pixels
        double totalGreen = 0; // accumulator variable for the total green value of all pixels
        double totalBlue = 0; // accumulator variable for the total blue value of all pixels
        
        double count = node.getHeight()*node.getWidth(); // total number of pixels in the Node
        
        // iterate through each pixel in the Node
        for (int i = node.getX(); i < node.getX() + node.getWidth(); i++) {
            for (int j = node.getY(); j < node.getY() + node.getHeight(); j++) {
                Color color = img.getImg()[j][i];
                
                // get rgb values and add to accumulator variables
                totalRed += color.getRed();
                totalGreen += color.getGreen();
                totalBlue += color.getBlue();
            }
        }
        
        // find the mean rgb values
        rgb[0] = (int)(totalRed/count);
        rgb[1] = (int)(totalGreen/count);
        rgb[2] = (int)(totalBlue/count);
        
        // create and return the mean color
        return new Color(rgb[0], rgb[1], rgb[2]);
    }
    
    /** Calculates the mean squared error of the pixels in a given Node as compared to its mean color
    * @param node The given node
    * @return double The mean squared error
    */
    public double meanSquaredError(Node node) {
        Color meanColor = meanColor(node);
        
        // get the mean rgb values
        int meanRed = meanColor.getRed();
        int meanGreen = meanColor.getGreen();
        int meanBlue = meanColor.getBlue();
        
        int[] errors = new int[node.getWidth() * node.getHeight()]; // stores the mean squaree error of each pixel
        int count = 0;
        
        for (int i = node.getX(); i < node.getX() + node.getWidth(); i++) { // iterate through each pixel
            for (int j = node.getY(); j < node.getY() + node.getHeight(); j++) {
                Color color = img.getImg()[j][i];
                
                // get rgb values
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                // calculate the squared error of each pixel
                int error = (int)(Math.pow(r - meanRed,2) + Math.pow(g - meanGreen,2) + Math.pow(b - meanBlue,2));
                errors[count] = error; // add it to the array
                count++;
            }
        }
        
        // calculate the total mean squared error 
        double sumError = 0;
        for (int i = 0; i < errors.length; i++) {
            sumError += errors[i];
        }
        
        double meanSquaredError = sumError/count;
        return meanSquaredError;
    }
    
    /** Divides the QuadTree into Nodes until it can no longer divide or when the compression level is reached
    * @param compressionLvl An indicator of when to stop dividing
    */
    public void divide(double compressionLvl) {
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.add(root);
        
        // stop as soon as you get over the compression level
        while (!queue.isEmpty() && getCompressionLevel() < compressionLvl) {
            subdivide(queue.pop(), queue);
        }
    }
    
    /** Divides the QuadTree into Nodes until it can no longer divide */
    public void divide() {
        ArrayDeque<Node> queue = new ArrayDeque<Node>();
        queue.add(root);
        
        while (!queue.isEmpty()) {
            subdivide(queue.pop(), queue);
        }
    }
    
    /** Subdivides a Node into 4 children and adds them to an ArrayDeque
    * @param node The Node to be divided
    * @param queue An ArrayDequeue to keep track of the divided Node's children
    */
    public void subdivide(Node node, ArrayDeque<Node> queue) {
        if (node == null) {
            return; 
        }
        
        int x = node.getX();
        int y = node.getY();
        int h = node.getHeight();
        int w = node.getWidth();
        
        if (h == 1 && w == 1) { // down to a single pixel; a leaf
            return;
        }
        
        if (meanSquaredError(node) > THRESHOLD) {
            node.setNW(new Node(x, y, h / 2, w / 2));
            node.setNE(new Node(x + h / 2, y, h - h / 2, w / 2));
            node.setSW(new Node(x, y + w / 2, h / 2, w - w / 2));
            node.setSE(new Node(x + h / 2, y + w / 2, h - h / 2, w - w / 2));
            
            queue.add(node.getNW());
            queue.add(node.getNE());
            queue.add(node.getSW());
            queue.add(node.getSE());
            
            size += 4;
        }
    }
    
    /** Returns the number of leaves of a given Node
    * @param node The given Node
    * @return int The number of leaves
    */
    public int getNumLeaves(Node node){
        if (node.isLeaf()) {
            return 1;
        }
        int numLeaves = 0;
        numLeaves += getNumLeaves(node.getNW());
        numLeaves += getNumLeaves(node.getNE());
        numLeaves += getNumLeaves(node.getSW());
        numLeaves += getNumLeaves(node.getSE());
        return numLeaves;
    }
    
    /** Outlines the QuadTree Nodes recursively by taking
    * the given Node and outlining its four children
    * @param node The given Node
    */
    public void outline(Node node) {
        if (node == null) {
            return;
        }
        
        int endY = node.getHeight() + node.getY(); // ending y-axis bound
        int endX = node.getWidth() + node.getX(); // ending x-axis bound
        
        for (int i = node.getY(); i < endY; i++) {
            for (int j = node.getX(); j < endX; j++) {
                if (i == node.getY() || i == endY - 1
                || j == node.getX() || j == endX - 1) { // border of node
                    img.getImg()[i][j] = Color.red;
                }
            }
        }
        
        outline(node.getNW());
        outline(node.getNE());
        outline(node.getSW());
        outline(node.getSE());
    }

    public Image compress() {
        Color[][] matrix = new Color[img.getHeight()][img.getWidth()];
        fillWithMeanColor(root, matrix);
        /*for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.println(matrix[i][j]);
            }
        }*/
        return new Image(matrix);
    }
    
    /** Compresses the image by filling its Nodes with its mean color
    * recursively by filling the given Node's children
    * @param node The given Node
    */
    public void fillWithMeanColor(Node node, Color[][] matrix) {
        if (node == null) {
            return;
        }
        
        if (node.isLeaf()) { // if it has no more children, fill
            Color meanColor = meanColor(node);
            for (int i = node.getX(); i <  node.getX() + node.getWidth(); i++) {
                for (int j = node.getY(); j < node.getY() + node.getHeight(); j++) {
                    matrix[j][i] = meanColor;
                }  
            }
        } 
        
        fillWithMeanColor(node.getNW(), matrix);
        fillWithMeanColor(node.getNE(), matrix);
        fillWithMeanColor(node.getSW(), matrix);
        fillWithMeanColor(node.getSE(), matrix);
    }
    
    /** Applies the edge detection filter on the image, but 
    * only on Nodes of sufficiently small size to save time 
    */
    public void edgeDetection() {
        Color[][] matrix = new Color[img.getHeight()][img.getWidth()];
        edgeDetectionRec(root, SUFFICIENTLY_SMALL_NODE_SIZE, matrix);
        img.setImg(matrix);
    }
    
    /** Private helper method to apply the edge detection filter
    * @param node
    * @param nodeThreshold
    * @param matrix
    */
    private void edgeDetectionRec(Node node, double nodeThreshold, Color[][] matrix) {
        // img.grayscale();
        if (node.isLeaf()) {
            return;
        }
        
        if (node.getHeight() <= nodeThreshold) { // if small enough node
            for (int i = node.getY(); i < node.getY() + node.getHeight(); i++) { // for each pixel in node
                for (int j = node.getX(); j < node.getX() + node.getWidth(); j++) {
                    if (i == 0 || i == img.getHeight() - 1 || j == 0 || j == img.getWidth() - 1) { // if node is on the edge of the image
                        matrix[i][j] = Color.black;
                    } else { // node is not on the edge, so no index out of bounds
                        int weight = EDGE_DETECT_KERNEL[0] * getRGB(img.getImg()[i-1][j-1]) + EDGE_DETECT_KERNEL[1] * getRGB(img.getImg()[i][j-1])
                        + EDGE_DETECT_KERNEL[2] * getRGB(img.getImg()[i+1][j-1]) + EDGE_DETECT_KERNEL[3] * getRGB(img.getImg()[i-1][j]) 
                        + EDGE_DETECT_KERNEL[4] * getRGB(img.getImg()[i][j]) + EDGE_DETECT_KERNEL[5] * getRGB(img.getImg()[i+1][j])
                        + EDGE_DETECT_KERNEL[6] * getRGB(img.getImg()[i-1][j+1]) + EDGE_DETECT_KERNEL[7] * getRGB(img.getImg()[i][j+1])
                        + EDGE_DETECT_KERNEL[8] * getRGB(img.getImg()[i+1][j+1]);
                        
                        if (Math.abs(weight) > EDGE_THRESHOLD) { // if it is an edge
                            matrix[i][j] = Color.white;
                        } else {
                            matrix[i][j] = Color.black;
                        }
                    }
                }
            }
        } else { // not small enough node; prob has little detail; set black
            for (int i = node.getY(); i < node.getY() + node.getHeight(); i++) { // for each pixel in node
                for (int j = node.getX(); j < node.getX() + node.getWidth(); j++) {
                    matrix[i][j] = Color.black;
                }
            }
        }
        
        edgeDetectionRec(node.getNW(), nodeThreshold, matrix);
        edgeDetectionRec(node.getNE(), nodeThreshold, matrix);
        edgeDetectionRec(node.getSW(), nodeThreshold, matrix);
        edgeDetectionRec(node.getSE(), nodeThreshold, matrix);
    }
    
    /** Returns an integer representation of a Color's RGB values
    * @param color
    * @return int The sum of the Color's RGB values
    */
    public int getRGB(Color color) {
        return color.getRed() + color.getGreen() + color.getBlue();
    } 
}