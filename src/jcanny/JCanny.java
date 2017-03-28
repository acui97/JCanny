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
 * @author Robert Streetman
 */
package jcanny;

import java.awt.image.BufferedImage;

public class JCanny {
    private static final int GAUSSIAN_RADIUS = 4;
    private static final double GAUSSIAN_INTENSITY = 1.75;
    
    private static int stDev;       //Standard deviation in magnitude of image's pixels
    private static int mean;        //Mean of magnitude in image's pixels
    private static int numDev;      //Number of standard deviations above mean for high threshold
    private static double tHi;      //Hysteresis high threshold; Definitely edge pixels, do not examine
    private static double tLo;      //Hysteresis low threshold; possible edge pixel, examine further.
    private static double tFract;   //Low threshold is this fraction of high threshold
    private static int[][] dir;     //Gradient direction mask. Equals Math.atan2(gy/gx)
    private static int[][] gx;      //Mask resulting from horizontal 3x3 Sobel mask
    private static int[][] gy;      //Mask resulting from vertical 3x3 Sobel mask
    private static double[][] mag;  //Direction mask. Equals Math.sqrt(gx^2 * gy^2)
    
    /**
     * This function accepts a single-channel (grayscale, red, blue, Y, etc) image and returns an image with detected edges.
     * Currently computes hysteresis thresholds based on an a given ratio, but in the future all parameters will be passed 
     * in from an external source to allow another program to optimize them.
     * 
     * @param img               A BufferedImage that is to undergo Canny edge detector. 
     * @param numberDeviations  Set high threshold as a function of number of standard deviations above the mean.
     *                          mean + std. dev: 68% of pixel magnitudes fall below this value
     *                          mean + 2 * std. dev: 95% of pixel magnitudes fall below this value
     *                          mean + 3 * std. dev: 99.7% of pixel magnitudes fall below this value
     * @param fract             Set low threshold as a fraction of the high threshold
     * @return                  A binary image of the edges in the input image.
     * @throws Exception
     */
    public static BufferedImage CannyEdges(BufferedImage img, int numberDeviations, double fract) throws Exception {
        if (fract < 0 || fract > 1) {  
            throw new IllegalArgumentException("ERROR: Hysteresis threshold ratio in range 0 - 1.0!");
        }
        
        int[][] raw = ImgIO.GSArray(img);
        int[][] blurred = Gaussian.BlurGS(raw, GAUSSIAN_RADIUS, GAUSSIAN_INTENSITY);
        numDev = numberDeviations;
        tFract = fract;
        
        gx = Sobel.Horizontal(blurred);  //Convolved with 3x3 horizontal Sobel mask
        gy = Sobel.Vertical(blurred);    //Convolved with 3x3 vertical Sobel mask
        
        Magnitude(gx, gy);    //Find the gradient magnitude at each pixel
        Direction(gx, gy);    //Find the gradient direction at each pixel
        Suppression();        //Using the direction and magnitude images, identify candidate points
        
        BufferedImage edges = ImgIO.GSImg(Hysteresis());
        
        return edges;
    }
    
    /**
     * Using horizontal & vertical Sobel convolutions, calculate gradient magnitude at each pixel
     */
    private static void Magnitude(int[][] gx, int[][] gy) {
        double sum = 0;
        double var = 0;
        int height = gx.length;
        int width = gx[0].length;
        mag = new double[height][width];
        
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                mag[r][c] = Math.sqrt(gx[r][c] * gx[r][c] + gy[r][c] * gy[r][c]);
                
                sum += mag[r][c];
            }
        }
        
        mean = (int) Math.round(sum / ((double) height * width));
        
        //Get variance
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                double magnitude = mag[r][c];
                
                var += (magnitude - mean) * (magnitude - mean);
            }
        }
        
        stDev = (int) Math.sqrt(var / ((double) height * width));
    }
    
    /**
     * Using horizontal & vertical Sobel convolutions, calculate gradient direction at each pixel
     */
    private static void Direction(int[][] gx, int[][] gy) {
        double piRad = 180 / Math.PI;
        int height = gx.length;
        int width = gx[0].length;
        dir = new int[height][width];
        
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                double angle = Math.atan2(gy[r][c], gx[r][c]) * piRad;    //Convert radians to degrees
                
                //Check for negative angles
                if (angle < 0) {
                    angle += 360.;
                }
                
                //Each pixels ACTUAL angle is examined and placed in 1 of four groups (for the four searched 45-degree neighbors)
                if (angle <= 22.5 || (angle >= 157.5 && angle <= 202.5) || angle >= 337.5) {
                    dir[r][c] = 0;      //Check left and right neighbors
                } else if ((angle >= 22.5 && angle <= 67.5) || (angle >= 202.5 && angle <= 247.5)) {
                    dir[r][c] = 45;     //Check diagonal (upper right and lower left) neighbors
                } else if ((angle >= 67.5 && angle <= 112.5) || (angle >= 247.5 && angle <= 292.5)) {
                    dir[r][c] = 90;     //Check top and bottom neighbors
                } else {
                    dir[r][c] = 135;    //Check diagonal (upper left and lower right) neighbors
                }
            }
        }
    }
    
    /**
     * Using gradient magnitude & direction, suppress all pixels that are not greater than neighbors along direction 
     */
    private static void Suppression() throws Exception {
        int height = mag.length - 1;
        int width = mag[0].length - 1;
        
        for (int r = 1; r < height; r++) {
            for (int c = 1; c < width; c++) {
                int direction = dir[r][c];
                double magnitude = mag[r][c];
                
                switch (direction) {
                    case 0 :
                        if (magnitude < mag[r][c - 1] && magnitude < mag[r][c + 1]) {
                            mag [r - 1][c - 1] = 0;
                        }
                        break;
                    case 45 :
                        if (magnitude < mag[r - 1][c + 1] && magnitude < mag[r + 1][c - 1]) {
                            mag [r - 1][c - 1] = 0;
                        }
                        break;
                    case 90 :
                        if (magnitude < mag[r - 1][c] && magnitude < mag[r + 1][c]) {
                            mag [r - 1][c - 1] = 0;
                        }
                        break;
                    case 135 :
                        if (magnitude < mag[r - 1][c - 1] && magnitude < mag[r + 1][c + 1]) {
                            mag [r - 1][c - 1] = 0;
                        }
                        break;
                }
            }
        }
    }
    
    /**
     * Eliminate 'false' edge pixels. A pixel is definitely an edge if its magnitude is greater than
     * or equal to high threshold. If magnitude is less than low threshold, it is not an edge and is
     * discarded. If magnitude is greater than or equal to low threshold AND lower than high threshold,
     * pixel is edge if and only if it is connected to a pixel of magnitude greater than or equal to
     * high threshold.
     */
    private static int[][] Hysteresis() {
        int height = mag.length - 1;
        int width = mag[0].length - 1;
        int[][] bin = new int[height - 1][width - 1];
        
        tHi = mean + (numDev * stDev);    //Magnitude greater than or equal to high threshold is an edge pixel
        tLo = tHi * tFract;               //Magnitude less than low threshold not an edge, equal or greater possible edge
        
        for (int r = 1; r < height; r++) {
            for (int c = 1; c < width; c++) {
                double magnitude = mag[r][c];
                
                if (magnitude >= tHi) {
                    bin[r - 1][c - 1] = 255;
                } else if (magnitude < tLo) {
                    bin[r - 1][c - 1] = 0;
                } else {
                    boolean connected = false;
                    
                    for (int nr = -1; nr < 2; nr++) {
                        for (int nc = -1; nc < 2; nc++) {
                            if (mag[r + nr][c + nc] >= tHi) {
                                connected = true;
                            }
                        }
                    }
                    
                    bin[r - 1][c - 1] = (connected) ? 255 : 0;
                }
            }
        }
        
        return bin;
    }
}
