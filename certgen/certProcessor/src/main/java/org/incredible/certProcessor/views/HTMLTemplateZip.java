package org.incredible.certProcessor.views;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HTMLTemplateZip extends HTMLTemplateProvider {


    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateZip.class);

    private URL zipUrl;

    public HTMLTemplateZip(URL zipUrl) {
        this.zipUrl = zipUrl;
    }

    private static final int bufferSize = 4096;

    /**
     * This  method is to download a zip file from the URL in the specified  target directory
     *
     * @param targetDirectory
     * @throws IOException
     */
    private void getZipFileFromURl(File targetDirectory) throws Exception {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        InputStream inputStream = new BufferedInputStream(zipUrl.openStream());
        // make sure we get the actual file
        File zipFile = File.createTempFile("arc", ".zip", targetDirectory);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(zipFile));
        extractFile(inputStream, outputStream);
        outputStream.close();
        unzip(zipFile, targetDirectory);
    }

    /**
     * This method is used to Extract each file in the given zipEntry (zipFile)
     *
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    private void extractFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int length = inputStream.read(buffer);
        while (length >= 0) {
            outputStream.write(buffer, 0, length);
            length = inputStream.read(buffer);
        }
        inputStream.close();
        outputStream.close();
    }

    /**
     * This method is to unzip the zip file
     *
     * @param zip             zip file to extract
     * @param targetDirectory directory to store Unzip files
     * @throws IOException
     */
    private void unzip(File zip, File targetDirectory) throws Exception {
        if (!zip.exists())
            throw new IOException(zip.getAbsolutePath() + " does not exist");
        if (!isDirectoryExists(targetDirectory))
            throw new IOException("Could not create directory: " + targetDirectory);
        ZipFile zipFile = new ZipFile(zip);
        if (isZipFileIsValid(zipFile.entries())) {
            for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File file = new File(targetDirectory, File.separator + entry.getName());
                if (!isDirectoryExists(file.getParentFile()))
                    throw new IOException("Could not create directory: " + file.getParentFile());
                if (!entry.isDirectory()) {
                    extractFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
                    if (entry.getName().endsWith(".html")) {
                        convertToString(zipFile.getInputStream(entry));
                    }
                } else {
                    if (!isDirectoryExists(file)) {
                        throw new IOException("Could not create directory: " + file);
                    }
                }
            }
        } else throw new Exception("Zip file is not valid");
        zipFile.close();
    }

    /**
     * This method is used to check  whether the directory exists or not, if not it creates the directory
     *
     * @param file
     * @return
     */
    public static boolean isDirectoryExists(File file) {
        return file.exists() || file.mkdirs();
    }

    @Override
    public String getTemplateContent() {
        if (content == null) {
            try {
                getZipFileFromURl(new File("src/main/resources/certificate"));
            } catch (Exception e) {
                logger.info("Exception while unzip the zip file {}", e.getMessage());
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * This method is used to convert file input stream to string
     *
     * @param inputStream
     */
    private void convertToString(InputStream inputStream) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
            content = writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is to check the number of files present is a zip with .html extension
     *
     * @param ZipEntries
     * @return
     */
    private boolean isZipFileIsValid(Enumeration<? extends ZipEntry> ZipEntries) {
        int noOfHTMLFiles = 0;
        for (Enumeration entries = ZipEntries; entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().endsWith(".html")) noOfHTMLFiles++;
        }
        System.out.println("no of Html files " + noOfHTMLFiles);
        if (noOfHTMLFiles == 1) return true;
        else return false;

    }
}
