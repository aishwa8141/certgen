package org.incredible.certProcessor.views;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HTMLTempalteZip extends HTMLTemplateProvider {


    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLTempalteZip.class);

    /**
     * html zip file url
     */
    private  URL zipUrl;

    public HTMLTempalteZip(URL zipUrl) {
        this.zipUrl = zipUrl;
    }

    private static final int bufferSize = 4096;

    /**
     * This  method is to download a zip file from the URL in the specified target directory.
     *
     * @param targetDirectory
     * @throws IOException
     */
    private void getZipFileFromURl(File targetDirectory) throws Exception {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
            HttpURLConnection connection = (HttpURLConnection) zipUrl.openConnection();
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            String zipPath = targetDirectory.getAbsolutePath().concat("/") + getZipFileName().concat(".zip");
            FileOutputStream out = new FileOutputStream(zipPath);
            copy(in, out, 1024);
            out.close();
            in.close();
            logger.info("Downloading Zip file from given url : success");
            unzip(zipPath, targetDirectory.getAbsolutePath());
            readIndexHtmlFile(targetDirectory.getAbsolutePath());
    }

    private void readIndexHtmlFile(String absolutePath) throws IOException {
        FileInputStream fis = new FileInputStream(absolutePath + "/index.html");
        content = IOUtils.toString(fis, "UTF-8");
        fis.close();
    }

    /**
     * unzips zip file
     *
     * @param zipFilePath
     * @param destDir
     */
    private  void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zipIn = new ZipInputStream(fis);
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File subDir = new File(filePath);
                    subDir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
            fis.close();
            logger.info("Unzipping zip file is finished");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * extracts each files in zip file (zip entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private  void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private  void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }


    /**
     * used to get file name from the url
     *
     * @return zip file name
     */
    public  String getZipFileName() {
        String fileName = null;
        try {
            URI uri = new URI(zipUrl.toString());
            String path = uri.getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.debug("Exception while getting key id from the sign-creator url : {}", e.getMessage());
        }
        return StringUtils.substringBefore(fileName, ".");
    }

    /**
     * checks if zip file with this name is exists or not
     *
     * @param zipFile
     * @return
     */
    public static boolean checkZipFileExists(File zipFile) {
        boolean isExits = false;
        if (zipFile.exists()) {
            isExits = true;
            logger.info("zip file  exists : " + isExits);
        } else isExits = false;
        return isExits;
    }

    /**
     * This method is used to get Html file content in string format
     *
     * @return html string
     */
    @Override
    public String getTemplateContent(String filePath) throws Exception {
        if (content == null) {
                File targetDirectory =  new File(filePath);
                if (checkZipFileExists(new File(targetDirectory.getAbsolutePath().concat("/") + getZipFileName() + ".zip"))) {
                    readIndexHtmlFile(targetDirectory.getAbsolutePath());
                } else {
                    getZipFileFromURl(targetDirectory);
                }
            }
        return content;
    }

}