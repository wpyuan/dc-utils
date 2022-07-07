package com.github.dc.utils.pojo;

import com.github.dc.utils.ReflectUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     针对没有children属性对象的包装类，作为树节点结构，有id、parentId、children对应字段可用TreeUtils.to方法
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 8:37
 */
@Data
@NoArgsConstructor
public class Node<E> {
    private Object id;
    private Object parentId;
    private E data;
    private List<Node<E>> children;

    public Node(E data) {
        if (data == null) {
            return;
        }
        this.id = ReflectUtils.getFieldValue(data, "id");
        this.parentId = ReflectUtils.getFieldValue(data, "parentId");
        this.data = data;
    }

    public Node(E data, String idFieldName, String parentIdFieldName) {
        if (data == null) {
            return;
        }
        this.id = ReflectUtils.getFieldValue(data, idFieldName);
        this.parentId = ReflectUtils.getFieldValue(data, parentIdFieldName);
        this.data = data;
    }

    /**
     * 根据默认属性id,parentId填充新建节点
     * @param data 实体数据
     * @param <E>
     * @return 节点
     */
    public static <E> Node<E> of(E data) {
        return Node.of(data, "id", "parentId");
    }

    /**
     * 根据对应属性填充新建节点
     * @param data 实体数据
     * @param idFieldName 实体数据唯一标识
     * @param parentIdFieldName 实体数据父级唯一标识
     * @param <E>
     * @return 节点
     */
    public static <E> Node<E> of(E data, String idFieldName, String parentIdFieldName) {
        if (data == null) {
            return null;
        }
        Node<E> node = new Node<E>();
        node.id = ReflectUtils.getFieldValue(data, idFieldName);
        node.parentId = ReflectUtils.getFieldValue(data, parentIdFieldName);
        node.data = data;
        return node;
    }

    /**
     * 解包，节点转实体对象
     * @param treeNodeList 包装类树结构数据
     * @param treeList 转换后的实体对象树结构数据
     * @param <E>
     */
    private static <E> void nodeToE(List<Node<E>> treeNodeList, List<E> treeList) {
        nodeToE(treeNodeList, treeList, "children");
    }

    /**
     * 解包，节点转实体对象
     * @param treeNodeList 包装类树结构数据
     * @param treeList 转换后的实体对象树结构数据
     * @param childrenFieldName children字段名称
     * @param <E>
     */
    private static <E> void nodeToE(List<Node<E>> treeNodeList, List<E> treeList, String childrenFieldName) {
        for (Node<E> currentNode : treeNodeList) {
            E entity = currentNode.getData();
            List<E> children = new ArrayList<>();
            nodeToE(currentNode.getChildren(), children);
            ReflectUtils.setFieldValue(entity, childrenFieldName, children);
            treeList.add(entity);
        }
    }
}
