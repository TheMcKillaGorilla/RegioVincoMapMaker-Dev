package rvmm;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import rvmm.data.RegioVincoMapMakerData;

/**
 *
 * @author rtmck
 */
public class ImageScalingTester {
    public static void main(String[] args) {
/*        String dir = "orig_images/The World/Europe/";
        File europe = new File(dir);
        for (File file : europe.listFiles()) {
            try {
                if ((!file.isDirectory()) && (file.getName().endsWith(".png"))) {
                    BufferedImage sourceImage = ImageIO.read(file);
                    System.out.print(file.getName() + " image type: " + sourceImage.getType() + ": ");
                    switch(sourceImage.getType()) {
                        case BufferedImage.TYPE_3BYTE_BGR:
                            System.out.print("TYPE_3BYTE_BGR\n");
                            break;
                        case BufferedImage.TYPE_4BYTE_ABGR:
                            System.out.print("TYPE_4BYTE_ABGR\n");
                            break;
                        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                            System.out.print("TYPE_4BYTE_ABGR_PRE\n");
                            break;
                        case BufferedImage.TYPE_BYTE_BINARY:
                            System.out.print("TYPE_BYTE_BINARY\n");
                            break;
                        case BufferedImage.TYPE_BYTE_GRAY:
                            System.out.print("TYPE_BYTE_GRAY\n");
                            break;
                        case BufferedImage.TYPE_BYTE_INDEXED:
                            System.out.print("TYPE_BYTE_INDEXED\n");
                            break;
                        case BufferedImage.TYPE_CUSTOM:
                            System.out.print("TYPE_CUSTOM\n");
                            break;
                        case BufferedImage.TYPE_INT_ARGB:
                            System.out.print("TYPE_INT_ARGB\n");
                            break;
                        case BufferedImage.TYPE_INT_ARGB_PRE:
                            System.out.print("TYPE_INT_ARGB_PRE\n");
                            break;
                        case BufferedImage.TYPE_INT_BGR:
                            System.out.print("TYPE_INT_BGR\n");
                            break;
                        case BufferedImage.TYPE_INT_RGB:
                            System.out.print("TYPE_INT_RGB\n");
                            break;
                        case BufferedImage.TYPE_USHORT_555_RGB:
                            System.out.print("TYPE_USHORT_555_RGB\n");
                            break;
                        case BufferedImage.TYPE_USHORT_565_RGB:
                            System.out.print("TYPE_USHORT_565_RGB\n");
                            break;
                        case BufferedImage.TYPE_USHORT_GRAY:
                            System.out.print("TYPE_USHORT_GRAY\n");
                            break;
                    }
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }*/
        String country = "Austria Flag.png";
        String origPath = "orig_images/The World/Europe/" + country;
        String outputPath = "test/" + country;
        exportFlagImage(origPath, outputPath);
    }
       
    private static void exportFlagImage(
                                    String sourcePath,
                                    String destPath) {
        try {
            // LOAD THE SOURCE IMAGE FROM THE SOURCE
            File sourceFile = new File(sourcePath);
            BufferedImage sourceImage = ImageIO.read(sourceFile);
            BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            newImage.getGraphics().drawImage(sourceImage, 0, 0, null);
            int desiredFlagWidth = 200;
            int sourceImageWidth = newImage.getWidth();
            double percentage = (double) desiredFlagWidth / (double) sourceImageWidth;
            
            // GET THE SCALED IMAGE
            BufferedImage destImage = getScaledImage(newImage, percentage);
            
            // MAKE SURE THE EXPORT PATH EXISTS
            File flagFile = new File(destPath);
            File parentDir = flagFile.getParentFile();
            if (!parentDir.exists()) {
                Path dir = Paths.get(parentDir.toURI());
                Files.createDirectories(dir);
            }
            
            // SAVE THE IMAGE TO THE DESTINATION
            ImageIO.write(destImage, "png", flagFile);
        } catch (Exception e) {
            System.out.println("Unable to export " + sourcePath);
        }
    }
    public static BufferedImage getScaledImage(BufferedImage sourceImage, double percentage) {
        int sourceImageWidth = sourceImage.getWidth();
        int sourceImageHeight = sourceImage.getHeight();
        int scaledImageWidth = (int) (Math.round(sourceImageWidth * percentage));
        int scaledImageHeight = (int) (Math.round(sourceImageHeight * percentage));
        if ((scaledImageWidth <= 0) || (scaledImageHeight <= 0)) {
            System.out.println("scaled image width or height < 0");
        }
        // RESIZE THE IMAGE
        BufferedImage scaledImage = new BufferedImage(
                scaledImageWidth, scaledImageHeight, sourceImage.getType());

        // COPY THE OLD IMAGE DATA OVER        
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(sourceImage, 0, 0, scaledImageWidth, scaledImageHeight, null);
        g2.dispose();

        return scaledImage;
    }
}
