package com.github.dc.utils;

import javax.script.*;
import java.util.Map;

/**
 * <p>
 *     js脚本引擎
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/9/27 8:45
 */
public class ScriptEngineHelper extends ScriptEngineMethod {

    public static ScriptEngineHelper init() {
        System.setProperty("engine.WarnInterpreterOnly","false");
        System.setProperty("nashorn.args","--no-deprecation-warning --language=es6");
        ScriptEngineHelper scriptEngineHelper = new ScriptEngineHelper();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        // 设置 Nashorn 选项以启用 ES6 特性
        try {
            engine.eval("load('nashorn:mozilla_compat.js');");
        } catch (ScriptException e) {
            System.out.println("设置 Nashorn 选项以启用 ES6 特性失败。" + e.getMessage());
        }
        scriptEngineHelper.engine(engine);
        return scriptEngineHelper;
    }

    /**
     * 构建ScriptEngine
     * @param data
     * @return
     */
    public ScriptEngineHelper variable(Map<String, Object> data) {
        data.forEach(this.engine::put);
        return this;
    }

    public ScriptEngineHelper bindings(int scope, Map<String, Object> data) {
        ScriptContext context = new SimpleScriptContext();
        this.bindings = context.getBindings(scope);
        data.forEach(this.bindings::put);
        return this;
    }

    public ScriptEngineHelper engine(ScriptEngine engine) {
        this.engine = engine;
        return this;
    }
}
