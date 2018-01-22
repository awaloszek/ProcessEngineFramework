package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TFormalExpression;
import com.bpmnengine.script.ScriptAccess;
import com.bpmnengine.script.ScriptRunner;

/**
 * @author André Waloszek
 */
public class FormalExpression extends TFormalExpression {

    private final ScriptRunner scriptRunner = new ScriptRunner();

    public Object eval(Object dataContextAccess) {
        return scriptRunner.eval(new FormalExpressionScriptAccess(this), dataContextAccess);
    }

    public Boolean evalCondition(Object dataContextAccess) {
        return (Boolean) eval(dataContextAccess);
    }

    private static final class FormalExpressionScriptAccess implements ScriptAccess {

        private final TFormalExpression formalExpression;

        private FormalExpressionScriptAccess(TFormalExpression formalExpression) {
            this.formalExpression = formalExpression;
        }

        @Override
        public String getLanguage() {
            return formalExpression.getLanguage();
        }

        @Override
        public String getCode() {
            return (String) formalExpression.getContent().get(0);
        }
    }

}
