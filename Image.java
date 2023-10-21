/* Names: Emma Lee and Rachel Nguyen
* File: Image.java
* Desc:
*
* A class to define an Image.
*
* An Image contains a 2D array of color as the
* underlying storage. It supports image processing
* (read and write) and filters (negative, grayscale,
* tint, and random neighbor).
*
*/

import java.io.*;
import java.util.*;
import java.awt.Color;

public class Image {
    public static final String FILE_INFO_DELIMITER = " "; // the String used to delimit file info
    public static final int WIDTH = 0; // index of the width info in img size info line
    public static final int HEIGHT = 1; // index of height info in img size info line
    public static final int NUM_HEADER_LINES = 3; // number of lines containing header info
    public static final int IMG_SIZE_LINE = 1; // which line contains image size (w and h) info
    
    private Color[][] img; // the array of RGB colors as the underlying storage
    private int width; // the width of the image
    private int height; // the height of the image
    
    /** Creates an Image with the given filename
    * @param filename
    */
    public Image(String filename) {
        try {
            img = readImg(filename);
            System.out.println("successfully read");
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }
    
    /** Creates an Image with the given Color matrix
    * @param arr The given Color matrix
    */
    public Image(Color[][] arr) {
        img = arr;
        width = arr[0].length;
        height = arr.length;
    }
    
    // getters
    public Color[][] getImg() {
        return img;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setImg(Color[][] matrix) {
        img = matrix;
    }
    
    /** Get one pixel of color of the image
    * @param x the x coordinate
    * @param y the y coordinate
    * @return Color The color of the pixel
    */
    public Color getPixel(int x, int y) {
        return img[x][y];
    }
    
    /** Read a PPM file into an array of colors
    * @param filename the filename to be read
    * @return Color[][] the matrix of colors
    */
    public Color[][] readImg(String filename) throws IOException{
        Scanner in = new Scanner(new File(filename));
        
        // get header info (width and height)
        for (int i = 0; i < NUM_HEADER_LINES; i++) {
            if (i == IMG_SIZE_LINE) {
                String[] imgSize = in.nextLine().split(FILE_INFO_DELIMITER);
                width = Integer.parseInt(imgSize[WIDTH]);
                height = Integer.parseInt(imgSize[HEIGHT]);
            } else {
                in.nextLine();
            }
        }
        Color[][] result = new Color[width][height];
        
        // store everything to the underlying storage
        for (int i = 0; i < height; i++) { // loop through rows
            for (int j = 0; j < width; j++) { // loop through cols
                int r = in.nextInt();
                int g = in.nextInt();
                int b = in.nextInt();
                Color color = new Color(r, g, b);
                result[i][j] = color;
            }
        }
        return result;
    }
    
    /** Write an image to a PPM file
    * @param filename the filename to be written to
    */
    public void writeImg(String filename) throws IOException{
        PrintWriter out = new PrintWriter(filename);
        out.print("P3 ");
        out.println(img[0].length + " " + img.length + " 255");
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                out.print(img[i][j].getRed() + " " + img[i][j].getGreen() + " " + img[i][j].getBlue() + " ");
            }
            out.println();
        }
        out.close();
        System.out.println("successfully written to " + filename);
    }
    
    /** Check whether an interger is a power of two
    * @param x The integer to be checked
    * @return boolean True if the integer is a power of two; false if otherwise
    */
    public boolean isPowerOfTwo(int x) {
        return (int)(Math.ceil((Math.log(x) / Math.log(2)))) == (int)(Math.floor(((Math.log(x) / Math.log(2)))));
    }
    
    /** Find the nearest power of two
    * @param x The integer to be looked at
    * @return int The nearest power of two
    */
    public static int nearestPowerOfTwo(int x) {
        int a = (int)(Math.log(x) / Math.log(2));
        if (Math.pow(2, a) == x) return x;
        return (int) Math.pow(2, a + 1);
    }
    
    /** Check whether an image is a square & the side is a power of two
    * @return boolean True if the image is a square and its side is a power of 2; false if otherwise
    */ 
    public boolean checkSize() {
        return (height == width) && isPowerOfTwo(height);
    }
    
    /** Resize the image to be a square & the side is a power of two */
    public Image resize() {
        if (!checkSize()) {
            int max = Math.max(height, width); // pick the greater side to be the side of the new square
            int side = nearestPowerOfTwo(max);
            Color[][] padded = new Color[side][side]; // the underlying storage for the new image
            // copy to the new array
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    padded[i][j] = img[i][j];
                }
            }
            // fill in the null pixels with white
            for (int i = 0; i < side; i++) {
                for (int j = 0; j < side; j++) {
                    if (padded[i][j] == null) {
                        padded[i][j] = Color.white;
                    }
                }
            }
            return new Image(padded);
        }
        return this;
    }
    
    /** Applies negative filter to the image */
    public void negative() {
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                // get the rgb integer
                Color color = img[i][j];
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                // convert to negative
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                color = new Color(r,g,b);
                img[i][j] = color;
            }
        }
    }
    
    /** Applies grayscale filter to the image */
    public void grayscale() {
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                // get the rgb integer
                Color color = img[i][j];
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                // convert to negative
                int c = (int) (r*0.3+g*0.59+b*0.11);
                color = new Color(c,c,c);
                img[i][j] = color;
            }
        }
    }
    
    /** Applies a tint filter to an image using the given tint color
    * @param tint The given tint color
    */
    public void tint(Color tint) {
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[i].length; j++) {
                Color color = img[i][j];
                
                // get rgb values
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                // pick a tint color
                int R = tint.getRed();
                int G = tint.getGreen();
                int B = tint.getBlue();
                
                // modify rgb values
                r = (r*R)/255; // r/255*R makes r/255 a double, which makes entire thing a double, then it gets truncated
                g = (g*G)/255;
                b = (b*B)/255;
                
                color = new Color(r, g, b); // reset the color
                
                // set new rgb
                img[i][j] = color;
            }
        }
    }
    
    /** Applies random neighbor convolution filter to an image
    * Takes one pixel, picks one of its neighbors randomly, and
    * sets itself to the color of its randomly chosen neighbor
    */
    public void randomNeighbor() {
        Color[][] matrix = new Color[width][height];
        for (int i = 0; i < height; i++) { // for each pixel in node
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) { // if edge
                    matrix[i][j] = img[i][j];
                } else { // not edge node
                    Color[] neighborColors = {img[i-1][j-1], img[i][j-1], img[i+1][j-1],
                        img[i-1][j], img[i][j], img[i+1][j],
                        img[i-1][j+1], img[i][j+1], img[i+1][j+1]};
                        
                        
                        int random = (int) (Math.random() * 8) + 0;     
                        matrix[i][j] = neighborColors[random];
                    }
                }
            }
            setImg(matrix);
        }
    }