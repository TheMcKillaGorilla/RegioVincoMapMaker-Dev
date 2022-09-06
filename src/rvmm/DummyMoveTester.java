package rvmm;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;


public class DummyMoveTester {
    public static void main(String[] args) {
        // MOVE ALL THE CONTENTS OF dummy to test
        String sourcePath = "test/dummy/dummy2";
        String destPath = "test/";
        
        File sourceDir = new File(sourcePath);
        File destDir = new File(destPath);
        
        try {
            FileUtils.moveDirectoryToDirectory(sourceDir, destDir, true);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
