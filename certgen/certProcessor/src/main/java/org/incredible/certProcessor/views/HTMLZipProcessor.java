package org.incredible.certProcessor.views;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HTMLZipProcessor extends HTMLTemplateProvider {


    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLZipProcessor.class);

    private URL zipUrl;

    public HTMLZipProcessor(URL zipUrl) {
        this.zipUrl = zipUrl;
    }

    private static final int bufferSize = 4096;

    private void getZipFileFromURl(File targetDirectory) throws IOException {
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

    private void unzip(File zip, File targetDirectory) throws IOException {
        if (!zip.exists())
            throw new IOException(zip.getAbsolutePath() + " does not exist");
        if (!isDirectoryExists(targetDirectory))
            throw new IOException("Could not create directory: " + targetDirectory);
        ZipFile zipFile = new ZipFile(zip);
        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File(targetDirectory, File.separator + entry.getName());
            if (!isDirectoryExists(file.getParentFile()))
                throw new IOException("Could not create directory: " + file.getParentFile());
            if (!entry.isDirectory()) {
                extractFile(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
                if (entry.getName().endsWith("certificate_pdf/index.html")) {
                    convertToString(zipFile.getInputStream(entry));
                }
            } else {
                if (!isDirectoryExists(file)) {
                    throw new IOException("Could not create directory: " + file);
                }
            }
        }
        zipFile.close();
    }

    public static boolean isDirectoryExists(File file) {
        return file.exists() || file.mkdirs();
    }

    @Override
    public String getTemplateContent() {
        if (content == null) {
            try {
                getZipFileFromURl(new File("src/main/resources/certificate"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    private void convertToString(InputStream inputStream) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
            content = writer.toString();
            System.out.println(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
