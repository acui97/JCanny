# JCanny
A pure Java implementation of John Canny's 1986 edge detector, including a Gaussian filter. The algorithm accepts an image, converts it to grayscale, blurs it with a Gaussian filter, and then detects the edges within it. It does so by finding the 'edge' magnitude of each pixel with a bi-directional Sobel operator, then disregarding all 'weak' edge pixels unless they are directly adjacent to a 'strong' edge pixel (**hysteresis**). The Canny is elegant - despite it's age and simplicity, it is still **the** standard for edge detection, and readily lends itself to optimization. This program is intended to provide quick method of detecting edges and study the Canny method, as well as getting quick feedback while tweaking parameters for specific projects.

## Tester Class Usage
**Command-line arguments:** *-inputFolderName* *-outputFileExtension* *-outputFolderName*

## Code Usage
```java
//Sample JCanny usage
try {
    BufferedImage input = ImageIO.read(new File(imgFileName));
    BufferedImage output = JCanny.CannyEdges(input, CANNY_STD_DEV, CANNY_THRESHOLD_RATIO);
    ImageIO.write(output, imgExt, new File(imgOutFile));
} catch (Exception ex) {
    System.out.println("ERROR ACCESING IMAGE FILE:\n" + ex.getMessage());
}
```

## Example:
```
test/test1.png png
```
![Original Image](https://github.com/rstreet85/JCanny/blob/master/test/test1.png)
![Output Image](https://github.com/rstreet85/JCanny/blob/master/test/test1_canny.png)

