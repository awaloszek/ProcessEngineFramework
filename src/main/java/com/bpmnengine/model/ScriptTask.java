package com.bpmnengine.model;

import com.bpmnengine.ActivityInstance;
import com.bpmnengine.LifecycleState;
import com.bpmnengine.data.DataContextAccess;
import com.bpmnengine.gen.omg.spec.bpmn.model.TScriptTask;
import com.bpmnengine.script.ScriptAccess;
import com.bpmnengine.script.ScriptRunner;

/**
 * @author André Waloszek
 */
public class ScriptTask extends TScriptTask {

    private final ScriptRunner scriptRunner = new ScriptRunner();

    @Override
    public boolean isScriptTask() {
        return true;
    }

    public Object eval(DataContextAccess dataContextAccess) {
        return scriptRunner.eval(new ScriptTaskScriptAccess(this), dataContextAccess);
    }

    @Override
    protected LifecycleState run(ActivityInstance activityInstance) {
        eval(activityInstance.getDataContext().createDataContextAccess());
        return LifecycleState.COMPLETING;
    }

    @Override
    public ScriptTask asScriptTask() {
        return this;
    }

    private static final class ScriptTaskScriptAccess implements ScriptAccess {

        private final TScriptTask scriptTask;

        private ScriptTaskScriptAccess(TScriptTask scriptTask) {
            this.scriptTask = scriptTask;
        }

        @Override
        public String getLanguage() {
            return scriptTask.getScriptFormat();
        }

        @Override
        public String getCode() {
            return scriptTask.getScript().getContent().isEmpty() ? "" : (String) scriptTask.getScript().getContent().get(0);
        }
    }

}
