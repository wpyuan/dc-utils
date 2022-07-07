package com.github.dc.utils;

import com.github.dc.utils.pojo.StandardTree;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * <p>
 *     树结构数据工具类
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 11:27
 */
@UtilityClass
public class TreeUtils {

    /**
     * list 转 treeList，默认每个实体对象含有id、parentId、children
     * @param list
     * @param <E>
     * @return
     */
    public static <E> List<E> to(List<E> list) {
        List<E> treeList = to(list, "id", "parentId", "children");
        return treeList;
    }

    /**
     * list 转 treeList，根据每个实体对象对应id、parentId、children字段关系转换
     * @param list 数据
     * @param idFieldName id字段名称
     * @param parentIdFieldName parentId字段名称
     * @param childrenFieldName children字段名称
     * @param <E>
     * @return
     */
    public static <E> List<E> to(List<E> list, String idFieldName, String parentIdFieldName, String childrenFieldName) {
        List<E> treeList = StandardTree.<E>of(list, idFieldName, parentIdFieldName, childrenFieldName).getTree();
        return treeList;
    }


}
