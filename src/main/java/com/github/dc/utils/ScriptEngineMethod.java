package com.github.dc.utils;

import lombok.Getter;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * <p>
 *     js脚本引擎方法
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/9/27 8:45
 */
@Getter
public abstract class ScriptEngineMethod {

    protected ScriptEngine engine;
    protected Bindings bindings;

    /**
     * 执行js脚本
     * @param script
     * @return
     */
    public Object execute(String script) {
        try {
            return this.engine.eval(script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行js脚本里面指定的方法
     * @param script
     * @param functionName
     * @param param
     * @return
     */
    public Object executeFunction(String script, String functionName, Object... param) {
        try {
            this.engine.eval(script);
            Invocable invocable = (Invocable) this.engine;
            return invocable.invokeFunction(functionName, param);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行js脚本文件里面指定的方法
     * @param scriptFile
     * @param functionName
     * @param param
     * @return
     */
    public Object executeFunction(File scriptFile, String functionName, Object... param) {
        try {
            this.engine.eval(new FileReader(scriptFile));
            Invocable invocable = (Invocable) this.engine;
            return invocable.invokeFunction(functionName, param);
        } catch (ScriptException | NoSuchMethodException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行js脚本里面指定对象的成员属性指向的方法。
     * 例如：const obj.isNum = () => { ... return true }， 调用obj对象的isNum属性方法，此时的property为isNum
     * @param script
     * @param objName
     * @param property
     * @param param
     * @return
     */
    public Object executeObjPropertyFunction(String script, String objName, String property, Object... param) {
        try {
            this.engine.eval(script);
            Invocable invocable = (Invocable) engine;
            // 获取我们想调用那个方法所属的js对象
            Object obj = engine.get(objName);
            // 执行obj对象的property的方法
            return invocable.invokeMethod(obj, property, param);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行js脚本文件里面指定对象的成员属性指向的方法
     * @param scriptFile
     * @param objName
     * @param property
     * @param param
     * @return
     */
    public Object executeObjPropertyFunctionByFile(File scriptFile, String objName, String property, Object... param) {
        try {
            this.engine.eval(new FileReader(scriptFile));
            Invocable invocable = (Invocable) this.engine;
            Object obj = this.engine.get(objName);
            return invocable.invokeMethod(obj, property, param);
        } catch (ScriptException | NoSuchMethodException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * js脚本实现java接口，返回已实例化的接口
     * @param script
     * @param clazz
     * @return 已实例化的接口
     * @param <T>
     */
    public <T> T executeImplByJava(String script, Class<T> clazz) {
        try {
            this.engine.eval(script);
            Invocable invocable = (Invocable) this.engine;
            return invocable.getInterface(clazz);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
