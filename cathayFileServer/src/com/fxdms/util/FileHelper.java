package com.fxdms.util;

import java.io.*;

public class FileHelper {

	
    public static void copy(String sourceName, String targetName) throws IOException {
        File sourceFile = new File(sourceName);  // Get File objects from Strings
        File targetFile = new File(targetName);
        if (sourceFile.isDirectory())
            copyDirectory(sourceFile, targetFile);
        else
            copyFile(sourceFile, targetFile);
    }

    public static void copyFile(String sourceName, String targetName) throws IOException {
        File sourceFile = new File(sourceName);  // Get File objects from Strings
        File targetFile = new File(targetName);
        copyFile(sourceFile, targetFile);
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        // First make sure the source file exists, is a file, and is readable.
        validate(sourceFile, targetFile);
        if (!sourceFile.isFile())
            throw new IOException("CopyFile: can't copy directory: " + sourceFile);

//System.out.println("Copying file "+sourceFile);
        // If the destination is a directory, use the source file name
        // as the destination file name
        if (targetFile.isDirectory())
          targetFile = new File(targetFile, sourceFile.getName());

        FileInputStream in = new FileInputStream(sourceFile);  // Stream to read in source
        FileOutputStream out = new FileOutputStream(targetFile);   // Stream to write to destination
        //copy the actual content of the file
        copyData(in, out);
        //close the input stream
        out.close();
        in.close();
    }

    public static void copyDirectory(String sourceName, String targetName) throws IOException {
        File sourceFile = new File(sourceName);  // Get File objects from Strings
        File targetFile = new File(targetName);
        copyDirectory(sourceFile, targetFile);
    }

    public static void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (sourceDir.isFile()) {    // copying file instead of directory
            copyFile(sourceDir, targetDir);
            return;
        }

        // First make sure the source file exists, is a file, and is readable.
        validate(sourceDir, targetDir);
        if (targetDir.isFile())
            throw new IOException("CopyFile: can't copy directory to a file: " + targetDir);
        else if (!targetDir.exists())
            targetDir.mkdirs();

        File[] dirList = sourceDir.listFiles();
        for (int i = 0; i < dirList.length; i++) {
            File myTarget = targetDir;
            if (dirList[i].isDirectory()) {   // a directory
                String filename = dirList[i].getName();
                myTarget = new File(targetDir, filename);
                if (! myTarget.exists())
                    myTarget.mkdir();
                copyDirectory(dirList[i], myTarget);
            } else {
                copyFile(dirList[i], targetDir);
            }
        }
    }

    public static void copyData(InputStream sourceStream, OutputStream targetStream) throws IOException {
        byte[] buffer = new byte[4096];         // A buffer to hold file contents
        int bytes_read;                         // How many bytes in buffer
        // Read a chunk of bytes into the buffer, then write them out,
        // looping until we reach the end of the file (when read() returns -1).
        // Note the combination of assignment and comparison in this while
        // loop.  This is a common I/O programming idiom.
        while((bytes_read = sourceStream.read(buffer)) != -1) // Read bytes until EOF
            targetStream.write(buffer, 0, bytes_read);            //   write bytes
    }

    protected static void validate(final File sourceFile, final File targetFile) throws IOException {
        if (!sourceFile.exists())
            throw new FileNotFoundException("CopyFile: no such source file: " + sourceFile);
        if (!sourceFile.canRead())
            throw new IOException("CopyFile: source file is unreadable: " + sourceFile);
        if (targetFile.exists() && !targetFile.canWrite())
            throw new IOException("CopyFile: target file is not writeable: " + targetFile);
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        if (args.length != 2) {    // Check arguments
            System.err.println("Usage: java FileHelper <source> <destination>");
        } else {
            // Call copy() to do the copy, and display any error messages it throws.
            try {
                FileHelper.copy(args[0], args[1]);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
