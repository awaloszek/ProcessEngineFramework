package com.bpmnengine.script;

import com.bpmnengine.data.DataContextAccess;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author André Waloszek
 */
public class ScriptRunner {

    private static final Logger L = Logger.getLogger(ScriptRunner.class.getName());

    public static final String BINDING_CONTEXT = "dataContext";

    private static List<Preprocessor> preprocessors = new ArrayList<>();

    public static void addPreprocessor(Preprocessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    private static ClassLoader classLoader = null;

    public static void setClassLoader(ClassLoader cl) {
        classLoader = cl;
    }

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    public ScriptRunner() {
        preprocessors.add(new JavaScriptPreprocessor());
    }

    private Object eval(String language, String script, Object dataContext) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader != null)
                Thread.currentThread().setContextClassLoader(classLoader);

            ScriptEngineManager scriptEngineManager = classLoader != null ? new ScriptEngineManager(classLoader) : this.scriptEngineManager;
            ScriptEngine scriptEngine = scriptEngineManager.getEngineByMimeType(language);
            if (scriptEngine == null)
                throw new IllegalArgumentException("Unsupported language; Supported languages are: " + getAvailableScriptEngines());

            Bindings bindings = scriptEngine.createBindings();
            bindings.put(BINDING_CONTEXT, dataContext);
            scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            return scriptEngine.eval(script);

        } catch (ScriptException e) {
            L.log(Level.SEVERE, "An error occurred running the script", e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return null;
    }

    public Object eval(ScriptAccess scriptAccess, Object dataContext) {
        if (dataContext instanceof DataContextAccess)
            return eval(scriptAccess.getLanguage(), getExpression(scriptAccess), dataContext);
        else
            return eval(scriptAccess.getLanguage(), getPropertyExpression(scriptAccess), dataContext);
    }

    private Preprocessor getPreprocessor(String language) {
        for (Preprocessor preprocessor : preprocessors) {
            if (preprocessor.forLanguage(language))
                return preprocessor;
        }

        return null;
    }
    
    private String getExpression(ScriptAccess scriptAccess) {
        Preprocessor preprocessor = getPreprocessor(scriptAccess.getLanguage());
        return preprocessor != null ? preprocessor.preprocessExpression(scriptAccess.getCode()) : scriptAccess.getCode();
    }

    private String getPropertyExpression(ScriptAccess scriptAccess) {
        Preprocessor preprocessor = getPreprocessor(scriptAccess.getLanguage());
        return preprocessor != null ? preprocessor.preprocessPropertyExpression(scriptAccess.getCode()) : scriptAccess.getCode();
    }

    private String getAvailableScriptEngines() {
        List<String> languages = new LinkedList<>();
        for (ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
            for (String mimeType : scriptEngineFactory.getMimeTypes()) {
                languages.add(mimeType);
            }
        }
        return Arrays.toString(languages.toArray());
    }
}