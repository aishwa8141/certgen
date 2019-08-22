package org.incredible.certProcessor.views;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ParserVisitor;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;


import java.util.HashSet;

public abstract class HTMLTemplateProvider {

    abstract public String getTemplateContent(String filePath) throws Exception;

    /**
     * variables present in html template
     */
    private static HashSet<String> htmlReferenceVariable = new HashSet<>();


    public static Boolean checkHtmlTemplateIsValid(String htmlString) {
        if (htmlString == null) return false;
        else {
            HTMLTemplateValidator htmlTemplateValidator = new HTMLTemplateValidator(storeAllHTMLTemplateVariables(htmlString));
            return htmlTemplateValidator.validate();
        }
    }


    /**
     * to get all the reference variables present in htmlString
     *
     * @param htmlString html file read in the form of string
     * @return set of reference variables
     */
    public static HashSet<String> storeAllHTMLTemplateVariables(String htmlString) {
        RuntimeInstance runtimeInstance = new RuntimeInstance();
        SimpleNode node = null;
        try {
            node = runtimeInstance.parse(htmlString, null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        visitor.visit(node, null);
        return htmlReferenceVariable;
    }

    private static ParserVisitor visitor = new BaseVisitor() {
        @Override
        public Object visit(final ASTReference node, final Object data) {
            htmlReferenceVariable.add(node.literal());
            return null;
        }
    };

}

