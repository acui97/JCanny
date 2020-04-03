/**
 * Copyright 2016 Robert Streetman
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package jcanny;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * This class demonstrates the usage of the JCanny Canny edge detector library.
 * 
 * @author robert, andy
 */

public class Tester {
    //Canny parameters
    private static final double CANNY_THRESHOLD_RATIO = .2; //Suggested range .2 - .4
    private static final int CANNY_STD_DEV = 1;             //Range 1-3
    
    //I/O parameters
    private static String imgFolderName;
    private static String imgOutFile;
    private static String imgExt;

    public static void main(String[] args) {
        //Read input file name and create output file name
        imgFolderName = args[0];
        imgExt = args[1];
        
        int fileCount = new File(imgFolderName).listFiles().length;
        int counter = 1;
        
        long start;
        long elapsed;
        
        for (final File fileEntry : new File(imgFolderName).listFiles()) {
        	start = System.currentTimeMillis();
        	
        	imgOutFile = args[2];
        	String[] arr = fileEntry.getName().split("\\.");
            
            for (int i = 0; i < arr.length - 1; i++) {
                imgOutFile += arr[i];
            }
            
            imgOutFile += "_canny.";
            imgOutFile += imgExt;
            
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
            	//Sample JCanny usage
                try {
                    BufferedImage input = ImageIO.read(fileEntry);
                    BufferedImage output = JCanny.CannyEdges(input, CANNY_STD_DEV, CANNY_THRESHOLD_RATIO);
                    ImageIO.write(output, imgExt, new File(imgOutFile));
                } catch (Exception ex) {
                    System.out.println("ERROR ACCESING IMAGE FILE:\n" + ex.getMessage());
                }
            }
            
            elapsed = System.currentTimeMillis() - start;
            
            System.out.println("Completed " + counter + " out of " + fileCount + " images. That image took " + elapsed + "ms. File created: " + imgOutFile);
            
            counter++;
        }
        
    }
    
    public static void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
            }
        }
    }
}
