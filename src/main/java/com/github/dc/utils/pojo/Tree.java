package com.github.dc.utils.pojo;

import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 8:29
 */
@Data
public class Tree<E> {
    private List<Node<E>> tree = new CopyOnWriteArrayList<>();
    private List<Node<E>> list = new CopyOnWriteArrayList<>();

    /**
     * list 转 Tree结构
     * @param list
     * @param <E>
     * @return
     */
    public static <E> Tree<E> of(List<E> list) {
        Tree<E> data = new Tree<>();
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
    public static <E> Tree<E> of(List<E> list, String idFieldName, String parentIdFieldName) {
        Tree<E> data = new Tree<>();
        if (list == null || list.size() == 0) {
            return data;
        }

        list.forEach(l -> data.add(l, idFieldName, parentIdFieldName));

        return data;
    }

    public void add(E node) {
        this.add(Node.<E>of(node));
    }

    public void add(E node, String idFieldName, String parentIdFieldName) {
        this.add(Node.<E>of(node, idFieldName, parentIdFieldName));
    }

    public void add(Node<E> node) {
        // 1、找子节点
        List<Node<E>> children = this.list.stream().filter(l -> node.getId() != null && node.getId().equals(l.getParentId())).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        // 1.1、更新原先没找到父节点的节点 到 该节点下
        node.setChildren(children);

        // 2、找父节点
        Node parent = this.list.stream().filter(l -> l.getId() != null && l.getId().equals(node.getParentId())).findFirst().orElse(null);

        //
        // 采用先插入后删除
        //---------------------------------------
        // 3、将 node 放入list 和 tree
        this.list.add(node);
        if (parent == null) {
            // 暂时没有父节点，置于顶层
            this.tree.add(node);
        } else {
            // 找到父节点，置于其下
            List<Node<E>> brothers = parent.getChildren() == null ? new CopyOnWriteArrayList<>() : parent.getChildren();
            brothers.add(node);
        }

        // 4、更新tree结构, 删除 原先没找到父节点现已找到的节点
        children.forEach(c -> this.tree.remove(c));
    }

    public void remove(E data) {
        Node<E> node = this.list.stream().filter(l -> l.getData().equals(data)).findFirst().orElse(null);
        if (node == null) {
            return;
        }

        this.remove(node);
    }

    public void remove(Node<E> node) {
        // 1、找父节点的children，删除自己
        if (node.getParentId() == null || this.list.stream().filter(l -> l.getId().equals(node.getParentId())).count() == 0) {
            // 根节点
            this.tree.remove(node);
        } else {
            this.findParentAndRemoveSelf(node);
        }
        // 2、同步更新list结构
        this.list.remove(node);

        // 3、找子节点并移除
        List<Node<E>> children = this.list.stream().filter(l -> node.getId().equals(l.getParentId())).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        this.deepRemove(children);
    }

    public void deepRemove(List<Node<E>> nodeList) {
        for (Node<E> currentNode : nodeList) {
            this.list.remove(currentNode);
            List<Node<E>> children = this.list.stream().filter(l -> currentNode.getId().equals(l.getParentId())).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            this.deepRemove(children);
        }
    }

    public void findParentAndRemoveSelf(Node<E> node) {
        for (Node<E> currentNode : this.tree) {
            if (currentNode.getId().equals(node.getParentId())) {
                List<Node<E>> children = currentNode.getChildren();
                children.remove(node);
                currentNode.setChildren(children);
                return;
            }
            currentNode.getChildren().forEach(this::findParentAndRemoveSelf);
        }
    }
}
