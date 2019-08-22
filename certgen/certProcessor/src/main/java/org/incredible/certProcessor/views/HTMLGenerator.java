package org.incredible.certProcessor.views;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;


public class HTMLGenerator {

    private static Logger logger = LoggerFactory.getLogger(HTMLGenerator.class);

    private String HtmlString;

    private HashSet<String> htmlReferenceVariable;


    public HTMLGenerator(String htmlString) {
        HtmlString = htmlString;
        htmlReferenceVariable = HTMLTemplateProvider.storeAllHTMLTemplateVariables(HtmlString);
    }


    public void initVelocity() {
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
    }

    /**
     * create velocity context  where Velocity.init() called once
     *
     * @param certificateExtension
     */

    public void generate(CertificateExtension certificateExtension, String directory) {
        initVelocity();
        VelocityContext context = new VelocityContext();
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        htmlReferenceVariable = HTMLTemplateProvider.storeAllHTMLTemplateVariables(HtmlString);
        Iterator<String> iterator = htmlReferenceVariable.iterator();
        while (iterator.hasNext()) {
            String macro = iterator.next().substring(1);
            try {
                Method method = htmlVarResolver.getClass().getMethod("get" + capitalize(macro));
                method.setAccessible(true);
                context.put(macro, method.invoke(htmlVarResolver));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                logger.info("exception while generating html for certificate {}", e.getMessage());
            }
        }
        createHTMLFile(context, getUUID(certificateExtension.getId()), directory);
    }

    private String getUUID(String id) {
        try {
            URI uri = new URI(id);
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            return StringUtils.substringBefore(idStr, ".");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createHTMLFile(VelocityContext context, String id, String directory) {
        try {
            File file = new File(directory, id + ".html");
            Writer writer = new FileWriter(file);
            Velocity.evaluate(context, writer, "velocity", HtmlString);
            writer.flush();
            writer.close();
            logger.info("html file is created {}", file.getName());
            PdfConverter.convertor(file, id, directory);
        } catch (IOException e) {
            logger.error("IO exception while creating html file :{}", e.getMessage());
        }
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


}