package muhzi.app;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

import muhzi.parser.SyntaxTreeNode;

public class DrawingPane extends Pane {

    private static int NODE_WIDTH = 60;
    private static int NODE_HEIGHT = 40;
    private static int NODE_GAP = 15;

    void drawTree(SyntaxTreeNode root, double xPos, double yPos) {
        if (root == null)
            return;

        // add current node
        addNode(root, xPos, yPos);

        // determine first child position
        double childX = xPos;
        double childY = yPos + 2 * NODE_HEIGHT;
        int numOfChildren = root.getNumOfChildren();
        if (numOfChildren > 0) {
            double branchesGap = numOfChildren * (NODE_WIDTH + NODE_GAP) - NODE_GAP;
            childX -= 0.5 * (branchesGap - NODE_WIDTH);
            childX = ensureNotOverlapped(childX, childY);
        } else {
            childX += 2 * NODE_WIDTH;
        }

        // add children tress
        SyntaxTreeNode[] children = root.getChildren();
        for (int i = 0; i < root.getNumOfChildren(); i++) {
            double linkXStart = xPos + 0.5 * NODE_WIDTH;
            double linkYStart = yPos + NODE_HEIGHT;
            SyntaxTreeNode child = children[i];

            Line line = new Line(linkXStart, linkYStart, childX + 0.5 * NODE_WIDTH, childY);
            this.getChildren().add(line);

            drawTree(child, childX, childY);
            childX += SyntaxTreeNode.getNodeWidth(child) * (NODE_WIDTH + NODE_GAP);
        }

        // determine next tree coordinates
        double siblingX = childX + NODE_GAP;
        double siblingLinkY = yPos + 0.5 * NODE_HEIGHT;

        // add next tree
        SyntaxTreeNode siblingTree = root.getNextSameLevelNode();
        drawTree(siblingTree, siblingX, yPos);
        if (siblingTree != null) {
            Line line = new Line(xPos + NODE_WIDTH, siblingLinkY, siblingX, siblingLinkY);
            this.getChildren().add(line);
        }

        // adjust pane size
        double boundX = siblingX + SyntaxTreeNode.getNodeWidth(siblingTree) * (NODE_WIDTH + NODE_GAP);
        adjustBounds(boundX, childY);
    }

    private void addNode(SyntaxTreeNode node, double xPos, double yPos) {
        Color strokePaint;
        if (node.isExpression()) {
            strokePaint = Color.rgb(102, 21, 19);
            addEllipse(xPos, yPos, strokePaint);
        } else {
            strokePaint = Color.rgb(48, 89, 164);
            addRectangle(xPos, yPos, strokePaint);
        }

        addTextOnNode(xPos, yPos+15, node.getLabel(), strokePaint);
        if (node.getValue() != null)
            addTextOnNode(xPos, yPos+30, "("+node.getValue()+")", strokePaint);
    }

    private void addEllipse(double xPos, double yPos, Color strokePaint) {
        double centerX = xPos + 0.5*NODE_WIDTH;
        double centerY = yPos + 0.5*NODE_HEIGHT;

        Ellipse ellipse = new Ellipse(centerX, centerY, 0.5*NODE_WIDTH, 0.5*NODE_HEIGHT);
        ellipse.setStroke(strokePaint);
        ellipse.setFill(Color.TRANSPARENT);
        this.getChildren().add(ellipse);
    }

    private void addRectangle(double xPos, double yPos, Color strokePaint) {
        Rectangle rectangle = new Rectangle(xPos, yPos, NODE_WIDTH, NODE_HEIGHT);

        rectangle.setStroke(strokePaint);
        rectangle.setFill(Color.TRANSPARENT);
        this.getChildren().add(rectangle);
    }

    private void addTextOnNode(double xPos, double yPos, String text, Color strokePaint) {
        Text textObj = new Text(text);
        double labelXPos = xPos +
                0.5 * (NODE_WIDTH-textObj.getLayoutBounds().getWidth());

        textObj.setX(labelXPos);
        textObj.setY(yPos);
        textObj.setStroke(strokePaint);
        this.getChildren().add(textObj);
    }

    private void adjustBounds(double x, double y) {
        if (x > this.getWidth()) {
            double delta = x- this.getWidth();
            this.setMinWidth(this.getWidth() + delta);
        }

        if (y > this.getHeight()) {
            double delta = y- this.getHeight();
            this.setMinHeight(this.getHeight() + delta);
        }
    }

    private double ensureNotOverlapped(double x, double y) {
        for (Node node: this.getChildren()) {
            Shape shape;
            if (node instanceof Shape) {
                shape = (Shape) node;
            } else {
                continue;
            }

            if (x < 0)
                return NODE_GAP;
            if (shape.intersects(x, y, NODE_WIDTH, NODE_HEIGHT)) {
                if (shape instanceof Rectangle) {
                    Rectangle rect = (Rectangle) shape;
                    return rect.getX() + rect.getWidth() + NODE_GAP;
                } else if (shape instanceof Ellipse) {
                    Ellipse ellipse = (Ellipse) shape;
                    return ellipse.getCenterX() + ellipse.getRadiusX() + NODE_GAP;
                } else {
                    return x + NODE_GAP;
                }
            }
        }
        return x;
    }

    void clearPane(Label label) {
        this.getChildren().clear();
        this.getChildren().add(label);
    }
}
