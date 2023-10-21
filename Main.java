/* Names: Emma Lee and Rachel Nguyen
* File: Main.java
* Desc:
*
* The main driver for Image Processing project
*
* This program takes a .ppm image file as input
* and performs image compression, edge detection
* or filter. User can decide the name of the ouput
* file this program writes to.
*
*/

import java.io.*;
import java.util.*;
import java.awt.Color;

public class Main {
    public static final String INPUT_FLAG = "-i"; // the flag for the input image filename (.ppm)
    public static final String OUTLINE_FLAG = "-t"; // the flag that indicates that output images have the quadtree outlined
    public static final String OUTPUT_FILENAME_FLAG = "-o"; // the flag that indicates the root name of the output file this program writes to
    public static final String OUTPUT_FILENAME_SEPARATOR = "-"; // the separator to format the output filename
    public static final String COMPRESSION_FLAG = "-c"; // the flag that indicates image compression
    public static final String EDGE_DETECTION_FLAG = "-e"; // the flag that indicates the output image has edge detection
    public static final String RANDOM_NEIGHBOR_FLAG = "-x"; // the flag that indicates the output image has random neighbor filter
    public static final double[] COMPRESSION_LVLS = {0.002, 0.004, 0.01, 0.033, 0.077, 0.2, 0.5, 0.75}; // an array of compression level
    public static final String FILETYPE = ".ppm"; // the file type to write to

    public static String filename = ""; // the filename user enters after flag -i
    public static String outputFilename = "out"; // the output filename user enters after flag -o; or "out" by default if no given (explained in README)
    public static boolean toOutline; // check whether user enters -t
    public static boolean toCompress; // check whether user enters -c
    public static boolean toEdgeDetect; // check whether user enters -e
    public static boolean toFilter; // check whether user enters -x

    /** Reads in flag information
        * @param args The flag information to be read
        * @return String[] An array of the output filenames
        */
    public static String[] parseFlags(String[] args) {
        String[] outputFilenames = new String[COMPRESSION_LVLS.length]; // an array of 8 output filenames based on 8 compression levels
        
        for (int i = 0; i < args.length; i++) {
            String str = args[i];
            if (str.compareTo(OUTLINE_FLAG) == 0) {
                toOutline = true;
            }
            if (str.compareTo(COMPRESSION_FLAG) == 0) {
                toCompress = true;
            }
            if (str.compareTo(EDGE_DETECTION_FLAG) == 0) {
                toEdgeDetect = true;
            }
            if (str.compareTo(RANDOM_NEIGHBOR_FLAG) == 0) {
                toFilter = true;
            }
            if (str.compareTo(INPUT_FLAG) == 0) {
                filename = args[i + 1];
            }
            if (str.compareTo(OUTPUT_FILENAME_FLAG) == 0) {
                outputFilename = args[i + 1];
                for (int j = 0; j < COMPRESSION_LVLS.length; j++) {
                    outputFilenames[j] = outputFilename + OUTPUT_FILENAME_SEPARATOR + (j + 1) + FILETYPE;
                }
            }
        }
        return outputFilenames;
    }

    public static void main(String[] args) {
        String[] outputFilenames = parseFlags(args);
        
        Image img = new Image(filename);
        img = img.resize();
        QuadTree qt = new QuadTree(img);

        if (toCompress) {
            int outputFileTracker = 0;
            for (double compressionLvl : COMPRESSION_LVLS) {
                qt.divide(compressionLvl);
                Image compressed = qt.compress();
                qt.setImg(compressed);
            
                if (toOutline) {
                    qt.outline(qt.getRoot());
                }
    
                try {
                    compressed.writeImg(outputFilenames[outputFileTracker]);
                } catch (IOException e) {
                    System.out.println(e);
                    System.exit(0);
                }

                qt.setImg(img);
                outputFileTracker++;
            }
        }
        
        if (toEdgeDetect) {
            qt.divide();
            qt.edgeDetection();

            if (toOutline) {
                qt.outline(qt.getRoot());
            }
            
            try {
                outputFilename = outputFilename + FILETYPE;
                img.writeImg(outputFilename);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            }
        }

        if (toFilter) {
            qt.divide();
            img.randomNeighbor();


            if (toOutline) {
                qt.outline(qt.getRoot());
            }
            
            try {
                outputFilename = outputFilename + FILETYPE;
                img.writeImg(outputFilename);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}