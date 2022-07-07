package com.github.dc.utils;

import com.github.dc.utils.pojo.StandardTree;
import com.github.dc.utils.pojo.TestNode;
import com.github.dc.utils.pojo.TestNode2;
import com.github.dc.utils.pojo.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/7/7 11:20
 */
public class TreeTest {
    public static void main(String[] args) {
        // 插入
        add();
        // 删除
        remove();
        // list to Tree
        listToTree();
        // list<E> to treeList<E>
        listToTreeList();
        listToTreeList2();
    }

    public static void add() {
        TestNode a = TestNode.builder().id(1).name("A").build();
        TestNode b = TestNode.builder().id(2).name("B").parentId(1).build();
        TestNode c = TestNode.builder().id(3).name("C").parentId(1).build();
        TestNode d = TestNode.builder().id(4).name("D").parentId(2).build();
        TestNode e = TestNode.builder().id(5).name("E").parentId(2).build();
        TestNode f = TestNode.builder().id(6).name("F").build();

        List<TestNode> xzqs = new ArrayList<>();
        xzqs.add(a);
        xzqs.add(b);
        xzqs.add(c);
        xzqs.add(d);
        xzqs.add(e);
        xzqs.add(f);

        Tree<TestNode> tree = new Tree<>();
        tree.add(f);
        tree.add(c);
        tree.add(b);
        tree.add(a);
        tree.add(d);
        tree.add(e);
        System.out.println(tree);
    }

    public static void remove() {
        TestNode a = TestNode.builder().id(1).name("A").build();
        TestNode b = TestNode.builder().id(2).name("B").parentId(1).build();
        TestNode c = TestNode.builder().id(3).name("C").parentId(1).build();
        TestNode d = TestNode.builder().id(4).name("D").parentId(2).build();
        TestNode e = TestNode.builder().id(5).name("E").parentId(2).build();
        TestNode f = TestNode.builder().id(6).name("F").build();

        List<TestNode> xzqs = new ArrayList<>();
        xzqs.add(a);
        xzqs.add(b);
        xzqs.add(c);
        xzqs.add(d);
        xzqs.add(e);
        xzqs.add(f);

        Tree<TestNode> tree = new Tree<>();
        tree.add(f);
        tree.add(c);
        tree.add(b);
        tree.add(a);
        tree.add(d);
        tree.add(e);
        System.out.println(tree);

        tree.remove(a);
        System.out.println(tree);
    }

    public static void listToTree() {
        TestNode a = TestNode.builder().id(1).name("A").build();
        TestNode b = TestNode.builder().id(2).name("B").parentId(1).build();
        TestNode c = TestNode.builder().id(3).name("C").parentId(1).build();
        TestNode d = TestNode.builder().id(4).name("D").parentId(2).build();
        TestNode e = TestNode.builder().id(5).name("E").parentId(2).build();
        TestNode f = TestNode.builder().id(6).name("F").build();

        List<TestNode> xzqs = new ArrayList<>();
        xzqs.add(a);
        xzqs.add(b);
        xzqs.add(c);
        xzqs.add(d);
        xzqs.add(e);
        xzqs.add(f);

        Tree<TestNode> tree = Tree.<TestNode>of(xzqs);
        System.out.println(tree);
    }

    public static void listToTreeList() {
        TestNode a = TestNode.builder().id(1).name("A").build();
        TestNode b = TestNode.builder().id(2).name("B").parentId(1).build();
        TestNode c = TestNode.builder().id(3).name("C").parentId(1).build();
        TestNode d = TestNode.builder().id(4).name("D").parentId(2).build();
        TestNode e = TestNode.builder().id(5).name("E").parentId(2).build();
        TestNode f = TestNode.builder().id(6).name("F").build();

        List<TestNode> xzqs = new ArrayList<>();
        xzqs.add(a);
        xzqs.add(b);
        xzqs.add(c);
        xzqs.add(d);
        xzqs.add(e);
        xzqs.add(f);

        List<TestNode> treeList = TreeUtils.to(xzqs);
        System.out.println(treeList);

        StandardTree<TestNode> treeList2 = StandardTree.of(xzqs);
        System.out.println(treeList2);
    }

    public static void listToTreeList2() {
        TestNode2 a = TestNode2.builder().xzqId(1).name("A").build();
        TestNode2 b = TestNode2.builder().xzqId(2).name("B").xzqParentId(1).build();
        TestNode2 c = TestNode2.builder().xzqId(3).name("C").xzqParentId(1).build();
        TestNode2 d = TestNode2.builder().xzqId(4).name("D").xzqParentId(2).build();
        TestNode2 e = TestNode2.builder().xzqId(5).name("E").xzqParentId(2).build();
        TestNode2 f = TestNode2.builder().xzqId(6).name("F").build();

        List<TestNode2> xzqs = new ArrayList<>();
        xzqs.add(a);
        xzqs.add(b);
        xzqs.add(c);
        xzqs.add(d);
        xzqs.add(e);
        xzqs.add(f);

        List<TestNode2> treeList = TreeUtils.to(xzqs, "xzqId", "xzqParentId", "xzqChildren");

        System.out.println(treeList);
    }
}
