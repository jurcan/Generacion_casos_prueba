
public class Tree {
    private Node root;

    public Tree(Node n) {this.root = n;}
}


class Node {
    private int info;
    private Node left,right;

    private node(int i) {this.info = i;}
    public void addLeft(Node n) {this.left = n;}
    public void addRight(Node n) {this.right = n;}
}   