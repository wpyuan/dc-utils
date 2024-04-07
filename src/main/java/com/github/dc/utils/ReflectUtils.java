package com.github.dc.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 15:53
 */
@UtilityClass
public class ReflectUtils {

    /**
     * 获取对象字段值(只往上找一级父类)
     * @param target 对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object target, String fieldName) {
        Class clazz = target.getClass();
        List<Field> field = new ArrayList<>();
        field.addAll(Arrays.asList(clazz.getDeclaredFields()));
        field.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));

        for (Field f : field) {
            if (!fieldName.equals(f.getName())) {
                continue;
            }
            f.setAccessible(true);
            try {
                return f.get(target);
            } catch (IllegalAccessException e) {
                ;
            }
        }

        return null;
    }

    /**
     * 设置对象字段值
     * @param target 对象
     * @param fieldName 字段名
     * @param fieldValue 字段值
     */
    public static void setFieldValue(Object target, String fieldName, Object fieldValue) {
        Class clazz = target.getClass();
        Field[] field = clazz.getDeclaredFields();
        for (Field f : field) {
            if (!fieldName.equals(f.getName())) {
                continue;
            }
            f.setAccessible(true);
            try {
                f.set(target, fieldValue);
            } catch (IllegalAccessException e) {
                ;
            }
        }
    }
}
