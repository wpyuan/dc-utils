package com.github.dc.utils.pojo;

import com.github.dc.utils.ReflectUtils;
import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * <p>
 *     标准树，泛型实体结构应包含id、parentId、children属性或对应属性
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 8:29
 */
@Data
public class StandardTree<E> {
    private List<E> tree = new CopyOnWriteArrayList<>();
    private List<E> list = new CopyOnWriteArrayList<>();

    /**
     * list 转 Tree结构
     * @param list
     * @param <E>
     * @return
     */
    public static <E> StandardTree<E> of(List<E> list) {
        StandardTree<E> data = new StandardTree<>();
        if (list == null || list.size() == 0) {
            return data;
        }

        list.forEach(data::add);

        return data;
    }

    /**
     * list 转 Tree结构
     * @param list 数据
     * @param idFieldName id字段名称
     * @param parentIdFieldName parentId字段名称
     * @param <E>
     * @return
     */
    public static <E> StandardTree<E> of(List<E> list, String idFieldName, String parentIdFieldName, String childrenFieldName) {
        StandardTree<E> data = new StandardTree<>();
        if (list == null || list.size() == 0) {
            return data;
        }

        list.forEach(l -> data.add(l, idFieldName, parentIdFieldName, childrenFieldName));

        return data;
    }

    public void add(E entity) {
        this.add(entity, "id", "parentId", "children");
    }

    public void add(E entity, String idFieldName, String parentIdFieldName, String childrenFieldName) {
        Object id = ReflectUtils.getFieldValue(entity, idFieldName);
        Object parentId = ReflectUtils.getFieldValue(entity, parentIdFieldName);
        // 1、找子节点
        List<E> children = this.list.stream().filter(l -> id != null && id.equals(ReflectUtils.getFieldValue(l, parentIdFieldName))).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        // 1.1、更新原先没找到父节点的节点 到 该节点下
        ReflectUtils.setFieldValue(entity, childrenFieldName, children);

        // 2、找父节点
        E parent = this.list.stream().filter(l -> ReflectUtils.getFieldValue(l, idFieldName) != null && ReflectUtils.getFieldValue(l, idFieldName).equals(parentId)).findFirst().orElse(null);

        //
        // 采用先插入后删除
        //---------------------------------------
        // 3、将 entity 放入list 和 tree
        this.list.add(entity);
        if (parent == null) {
            // 暂时没有父节点，置于顶层
            this.tree.add(entity);
        } else {
            // 找到父节点，置于其下
            List<E> brothers = ReflectUtils.getFieldValue(parent, childrenFieldName) == null ? new CopyOnWriteArrayList<>() : (List<E>) ReflectUtils.getFieldValue(parent, childrenFieldName);
            brothers.add(entity);
        }

        // 4、更新tree结构, 删除 原先没找到父节点现已找到的节点
        children.forEach(c -> this.tree.remove(c));
    }

    public void remove(E entity) {
        this.remove(entity, "id", "parentId", "children");
    }

    public void remove(E entity, String idFieldName, String parentIdFieldName, String childrenFieldName) {
        Object id = ReflectUtils.getFieldValue(entity, idFieldName);
        Object parentId = ReflectUtils.getFieldValue(entity, parentIdFieldName);
        // 1、找父节点的children，删除自己
        if (parentId == null || this.list.stream().filter(l -> ReflectUtils.getFieldValue(l, idFieldName).equals(parentId)).count() == 0) {
            // 根节点
            this.tree.remove(entity);
        } else {
            this.findParentAndRemoveSelf(entity, idFieldName, parentIdFieldName, childrenFieldName);
        }
        // 2、同步更新list结构
        this.list.remove(entity);

        // 3、找子节点并移除
        List<E> children = this.list.stream().filter(l -> id.equals(ReflectUtils.getFieldValue(l, parentIdFieldName))).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        this.deepRemove(children, idFieldName, parentIdFieldName);
    }

    public void deepRemove(List<E> nodeList, String idFieldName, String parentIdFieldName) {
        for (E currentNode : nodeList) {
            this.list.remove(currentNode);
            List<E> children = this.list.stream().filter(l -> ReflectUtils.getFieldValue(currentNode, idFieldName).equals(ReflectUtils.getFieldValue(l, parentIdFieldName))).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            this.deepRemove(children, idFieldName, parentIdFieldName);
        }
    }

    public void findParentAndRemoveSelf(E entity, String idFieldName, String parentIdFieldName, String childrenFieldName) {
        for (E currentNode : this.tree) {
            // 找父节点
            if (ReflectUtils.getFieldValue(currentNode, idFieldName).equals(ReflectUtils.getFieldValue(entity, parentIdFieldName))) {
                List<E> children = (List<E>) ReflectUtils.getFieldValue(currentNode, childrenFieldName);
                // 删除自己
                children.remove(entity);
                // 更新子节点数据
                ReflectUtils.setFieldValue(currentNode, childrenFieldName, children);
                return;
            }
            for (E node : ((List<E>) ReflectUtils.getFieldValue(currentNode, childrenFieldName))) {
                this.findParentAndRemoveSelf(entity, idFieldName, parentIdFieldName, childrenFieldName);
            }
        }
    }
}
