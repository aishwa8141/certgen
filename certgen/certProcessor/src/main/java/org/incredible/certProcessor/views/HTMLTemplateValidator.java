package org.incredible.certProcessor.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class HTMLTemplateValidator {

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateValidator.class);

    private HashSet<String> htmlTemplateVariable;

    public HTMLTemplateValidator(HashSet<String> htmlTemplateVariable) {
        this.htmlTemplateVariable = htmlTemplateVariable;
    }

    public Boolean validate() {
        HashSet<String> invalidVariables = new HashSet<>();
        Iterator<String> iterator = htmlTemplateVariable.iterator();
        List<String> allVars = HTMLVars.get();
        try {
            while (iterator.hasNext()) {
                String htmlVar = iterator.next();
                if (!allVars.contains(htmlVar)) {
                    invalidVariables.add(htmlVar);
                }
            }
            if (invalidVariables.size() == 0) {
                logger.info("HTML template is valid");
                htmlTemplateVariable.clear();
                return true;
            } else throw new Exception("HTML template is not valid");

        } catch (Exception e) {
            logger.error("Exception while validating html template due to following invariables {} {}", invalidVariables, e.getMessage());
            htmlTemplateVariable.clear();
            return false;
        }

    }

}
