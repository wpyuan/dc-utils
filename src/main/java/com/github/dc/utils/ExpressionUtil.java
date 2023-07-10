package com.github.dc.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * <p>
 *     表达式工具类
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/4/18 16:44
 */
@UtilityClass
public class ExpressionUtil {

    /**
     * 执行布尔表达式得到布尔值结果
     * @param booleanExpression
     * @return
     */
    public static Boolean execute(String booleanExpression) {
        return (Boolean) ScriptEngineHelper.init().execute(booleanExpression);
    }

    /**
     * 执行布尔表达式得到布尔值结果
     * @param booleanExpression
     * @param data
     * @return
     */
    public static Boolean execute(String booleanExpression, Map<String, Object> data) {
        return (Boolean) ScriptEngineHelper.init().variable(data).execute(booleanExpression);
    }
}
