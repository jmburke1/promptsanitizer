import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzip {

    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        String zipFile = "downloaded.zip";
        unzip(zipFile, ".");
    }

    public static void unzip(String zipFilePath, String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String filePath = destDirectory + File.separator + entry.getName();

                // Security check: prevent zip-slip (entries outside destination)
                if (!new File(filePath).getCanonicalPath().startsWith(destDir.getCanonicalPath())) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    new File(filePath).mkdirs();
                } else {
                    // Ensure parent directories exist for nested entries
                    File parent = new File(filePath).getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    try (OutputStream out = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        } catch (IOException e) {
            System.err.println("Error unzipping: " + e.getMessage());
            System.exit(1);
        }

        System.out.println("Unzipped successfully.");
    }
}
