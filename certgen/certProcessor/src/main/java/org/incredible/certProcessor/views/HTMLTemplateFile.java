package org.incredible.certProcessor.views;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class HTMLTemplateFile extends HTMLTemplateProvider {

    private String HTML_TEMPLATE_NAME;

    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateFile.class);


    public HTMLTemplateFile(String fileName) {
        HTML_TEMPLATE_NAME = fileName;
    }

    private void fetchFile() {
        File htmlTemplateFile = new File(getPath(HTML_TEMPLATE_NAME));
        try {
            content = FileUtils.readFileToString(htmlTemplateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTemplateContent(String filePath) {
        if (content == null) {
            fetchFile();
        }
        return content;
    }

    private static String getPath(String file) {
        ClassLoader loader = HTMLGenerator.class.getClassLoader();

        String result = null;
        try {
            result = loader.getResource(file).getFile();
        } catch (Exception e) {
            logger.error("Exception while retrieving the path of the file {}", e.getMessage());
        } finally {
            return result;
        }
    }

}

