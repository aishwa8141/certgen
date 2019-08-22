package org.incredible.certProcessor.views;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import static org.junit.Assert.*;

public class HTMLTemplateProviderTest {

    String filePath;
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void TestValidHTMlTemplate() {

        HTMLTemplateFile htmlTemplateFile = new HTMLTemplateFile("ValidTemplate.html");
        Boolean valid = HTMLTemplateProvider.checkHtmlTemplateIsValid(htmlTemplateFile.getTemplateContent(filePath));
        assertEquals(true, valid);

    }

    @Test
    public void TestInValidHTMLTemplate() {

        HTMLTemplateFile htmlTemplateFile = new HTMLTemplateFile("InvalidTemplate.html");
        Boolean valid = HTMLTemplateProvider.checkHtmlTemplateIsValid(htmlTemplateFile.getTemplateContent(filePath));
        assertEquals(false, valid);

    }
}