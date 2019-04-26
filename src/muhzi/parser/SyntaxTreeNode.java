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
        next = null;
        children = new SyntaxTreeNode[3];
        parent = null;
        this.label = label;
        this.expression = false;
        this.value = null;
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

    private static int getNumOfLeafs(SyntaxTreeNode root) {
        if (root == null)
            return 0;
        if (root.numOfChildren == 0)
            return 1;

        int numOfLeafs = 0;
        for (SyntaxTreeNode child: root.children) {
            numOfLeafs += getNumOfLeafs(child);
        }

        return numOfLeafs;
    }

    public static int getNodeWidth(SyntaxTreeNode root) {
        if (root == null)
            return 0;

        int numOfChildren = root.numOfChildren;
        if (numOfChildren == 0)
            return 1;

        int width = 0;
        for (int i = 0; i < numOfChildren; i++) {
            SyntaxTreeNode child = root.children[i];
            width += getNumOfLeafs(child);
            width += getNodeWidth(child.next);
        }
        width += getNodeWidth(root.next);

        return width;
    }
}
