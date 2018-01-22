package com.bpmnengine.script;

import static com.bpmnengine.script.ScriptRunner.BINDING_CONTEXT;

/**
 * @author André Waloszek
 */
public class JavaScriptPreprocessor implements Preprocessor {

    @Override
    public boolean forLanguage(String language) {
        return "application/javascript".equals(language);
    }

    @Override
    public String preprocessExpression(String expression) {
        return "function getDataInput(id) { return " + BINDING_CONTEXT + ".getDataInput(id); } \n" +
                "function setDataOutput(id, value) { " + BINDING_CONTEXT + ".setDataOutput(id, value); } \n" +
                "function getDataObject(id) { return " + BINDING_CONTEXT + ".getDataObject(id); } \n" +
                expression;
    }

    @Override
    public String preprocessPropertyExpression(String propertyExpression) {
        return BINDING_CONTEXT + "." + propertyExpression.trim();
    }

}
