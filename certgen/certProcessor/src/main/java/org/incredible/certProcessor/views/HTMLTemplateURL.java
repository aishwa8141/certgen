package org.incredible.certProcessor.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class HTMLTemplateURL extends HTMLTemplateProvider {
    private URL url;
    private String content = null;
    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateURL.class);

    public HTMLTemplateURL(URL httpUrl) {
        url = httpUrl;
    }

    private void fetchUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            while ((content = in.readLine()) != null) {
                stringBuilder.append(content + "\n");
            }
            content = stringBuilder.toString();
        } catch (IOException e) {
            logger.error("Exception while reading file from given url {}", e.getMessage());
        }

    }

    @Override
    public String getTemplateContent(String filePath) {
        if (content == null) {
            fetchUrl();
        }
        return content;
    }
}
