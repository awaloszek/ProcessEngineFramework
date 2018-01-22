package com.bpmnengine.script;

/**
 * @author André Waloszek
 */
public interface Preprocessor {

    boolean forLanguage(String language);

    String preprocessExpression(String expression);

    String preprocessPropertyExpression(String propertyExpression);
}
