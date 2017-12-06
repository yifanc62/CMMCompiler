package Syntactic;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private NodeType type;
    private List<TreeNode> children;
    private String value;
    private int line;

    public TreeNode(NodeType type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public TreeNode(NodeType type, int line) {
        this.type = type;
        this.children = new ArrayList<>();
        this.line = line;
    }

    public NodeType getType() {
        return type;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode addChild(TreeNode child) {
        children.add(child);
        return this;
    }

    public String getValue() {
        return value;
    }

    public TreeNode setValue(String value) {
        this.value = value;
        return this;
    }

    public int getLine() {
        return line;
    }

    public TreeNode setLine(int line) {
        this.line = line;
        return this;
    }
}
