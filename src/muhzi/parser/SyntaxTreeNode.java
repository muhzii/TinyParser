package muhzi.parser;

public class SyntaxTreeNode {

    private SyntaxTreeNode next;
    private int numOfChildren;
    private SyntaxTreeNode[] children;

    private SyntaxTreeNode parent;

    private String label;
    private boolean expression;
    private String value;

    SyntaxTreeNode(String label) {
        this.label = label;
        next = null;
        children = new SyntaxTreeNode[3];
        parent = null;
        expression = false;
        value = null;
        numOfChildren = 0;
    }

    public boolean isExpression() {
        return expression;
    }

    SyntaxTreeNode getParent() {
        return parent;
    }

    public SyntaxTreeNode[] getChildren() {
        return children;
    }

    public SyntaxTreeNode getNextSameLevelNode() {
        return next;
    }

    public int getNumOfChildren() {
        return numOfChildren;
    }

    SyntaxTreeNode getLastChild() {
        if (numOfChildren > 0)
            return children[numOfChildren-1];
        return null;
    }

    int getChildNum(SyntaxTreeNode child) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] == child)
                return i;
        }
        return -1;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    void addChild(SyntaxTreeNode child) {
        if (!(numOfChildren == children.length)) {
            children[numOfChildren] = child;
            children[numOfChildren].parent = this;
            numOfChildren++;
        }
    }

    void setLabel(String label) {
        this.label = label;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setParent(SyntaxTreeNode parent) {
        this.parent = parent;
    }

    void markAsExpression() {
        expression = true;
    }

    void setNextSameLevelNode(SyntaxTreeNode next) {
        this.next = next;
        this.next.parent = this;
    }

    void setChild(int childNum, SyntaxTreeNode child){
        children[childNum] = child;
    }

    static void removeNilNodes(SyntaxTreeNode root) {
        if (root == null)
            return;

        if (root.getLabel().equals("NIL")) {
            int childNum = root.parent.getChildNum(root);

            if (childNum == -1) {
                root.parent.next = null;
            } else {
                root.parent.setChild(root.parent.getChildNum(root), null);
            }
        }

        for (SyntaxTreeNode child: root.children) {
            removeNilNodes(child);
        }
        removeNilNodes(root.next);
    }

    public static int getLevelWidth(SyntaxTreeNode root) {
        if (root == null)
            return 0;

        int width = 0;
        int numOfChildren = root.numOfChildren;

        if (numOfChildren == 0) {
            if (root.next == null) {
                return 1;
            } else {
                width = 1;
            }
        }

        for (int i = 0; i < numOfChildren; i++) {
            width += getLevelWidth(root.children[i]);
        }
        return width + getLevelWidth(root.next);
    }
}
