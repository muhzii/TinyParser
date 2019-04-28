package muhzi.parser;

import muhzi.parser.errors.ParserError;
import muhzi.parser.errors.SyntaxError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class Parser {
    /*
    * The Parser component generates a syntax tree for the program,
    * based on the following grammar:
    *
    * program -> stmt-sequence
    * stmt-sequence -> stmt-sequence ; statement | statement
    * statement -> if-stmt | repeat-stmt | assign-stmt | read-stmt | write-stmt
    * if-stmt -> if exp then stmt-sequence end
        | if exp then stmt-sequence else stmt-sequence end
    * repeat-stmt -> repeat stmt-sequence until exp
    * assign-stmt -> identifier := exp
    * read-stmt -> read identifier
    * write-stmt -> write exp
    * exp -> simple-exp comparison-op simple-exp | simple-exp
    * comparison-op -> = | <
    * simple-exp -> simple-exp addop term | term
    * addop -> + | -
    * term -> term mulop factor | factor
    * mulop -> * | /
    * factor -> (exp) | number | identifier
    */

    private static Logger logger = Logger.getLogger(Parser.class.getName());
    static {
        try {
            FileHandler fh =
                    new FileHandler(System.getProperty("user.dir") +
                                    File.separator + "parser_output.txt");
            fh.setFormatter(new Formatter() {
                private static final String format = "[%1$tF %1$tT] [%2$s] %3$s %n";

                @Override
                public String format(LogRecord record) {
                    return String.format(format,
                            new Date(record.getMillis()),
                            record.getLevel().getLocalizedName(),
                            record.getMessage());
                }
            });
            logger.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ParserError();
        }
    }

    private Token currentToken;
    private Scanner scanner;
    private SyntaxTreeNode tree;

    public SyntaxTreeNode parse(BufferedReader br) {
        SyntaxTreeNode root = tree = new SyntaxTreeNode("NIL");
        scanner = new Scanner(br);
        try {
            currentToken = scanner.getNextToken();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ParserError();
        }

        matchProgram();
        SyntaxTreeNode.removeNilNodes(root);
        return root;
    }

    private void LOG(String message, Level level) {
        if (level == Level.SEVERE) {
            logger.severe(message);
            throw new SyntaxError(message);
        } else {
            logger.info(message);
        }
    }

    private void matchProgram() {
        LOG("program is found", Level.INFO);
        matchStmtSequence(true);
    }

    private void matchStmtSequence(boolean ensureAllMatched) {
        LOG("stmt-sequence is found", Level.INFO);

        matchStatement();
        while (currentToken.getValue().equals(";")) {
            match(";");
            matchStatement();
        }

        String tk_val = currentToken.getValue();
        if (ensureAllMatched && !tk_val.equals("")) {
            LOG("Expected ; before new statement ["+tk_val+"...]", Level.SEVERE);
        }
    }

    private void matchStatement() {
        LOG("statement is found", Level.INFO);

        String tk_val = currentToken.getValue();
        String tk_type = currentToken.getType();
        if (tk_val.equals("if")) {
            matchIfStmt();
        } else if (tk_val.equals("repeat")) {
            matchRepeatStmt();
        } else if (tk_type.equals("identifier")) {
            matchAssignStmt();
        } else if (tk_val.equals("read")) {
            matchReadStmt();
        } else if (tk_val.equals("write")) {
            matchWriteStmt();
        } else {
            LOG("Undefined statement starting with ["+tk_val+"]", Level.SEVERE);
        }
    }

    private void matchWriteStmt() {
        LOG("write-stmt is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.setLabel("write");

        match("write");
        advanceToAChild(currentNode);
        matchExp();

        advanceToNextSameLevelNode(currentNode);
    }

    private void matchReadStmt() {
        LOG("read-stmt is found", Level.INFO);

        tree.setLabel("read");
        match("read");
        tree.setValue(currentToken.getValue());
        match("identifier");

        advanceToNextSameLevelNode(tree);
    }

    private void matchAssignStmt() {
        LOG("assign-stmt is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.setLabel("assign");
        currentNode.setValue(currentToken.getValue());

        match("identifier");
        match(":=");
        advanceToAChild(currentNode);
        matchExp();

        advanceToNextSameLevelNode(currentNode);
    }

    private void matchRepeatStmt() {
        LOG("repeat-stmt is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.setLabel("repeat");

        match("repeat");
        advanceToAChild(currentNode);
        matchStmtSequence(false);
        match("until");
        advanceToAChild(currentNode);
        matchExp();

        advanceToNextSameLevelNode(currentNode);
    }

    private void matchIfStmt() {
        LOG("if-stmt is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.setLabel("if");

        match("if");
        advanceToAChild(currentNode);
        matchExp();
        match("then");
        advanceToAChild(currentNode);
        matchStmtSequence(false);

        if (currentToken.getValue().equals("else")) {
            match("else");
            advanceToAChild(currentNode);
            matchStmtSequence(false);
        }
        match("end");

        advanceToNextSameLevelNode(currentNode);
    }

    private void matchExp() {
        LOG("exp is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.markAsExpression();

        matchSimpleExp();
        String tk_val = currentToken.getValue();
        if (tk_val.equals("<") || tk_val.equals("=")){
            matchOp(currentNode);
            advanceToAChild(tree);
            matchSimpleExp();
        }
    }

    private void matchSimpleExp() {
        LOG("simple-exp is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.markAsExpression();

        matchTerm();
        while (currentToken.getValue().equals("+") ||
                currentToken.getValue().equals("-")) {
            matchOp(currentNode);
            advanceToAChild(tree);
            matchTerm();
        }
    }

    private void matchOp(SyntaxTreeNode sourceNode) {
        String tk_val = currentToken.getValue();
        switch (tk_val) {
            case "<":
            case "=":
                LOG("comparison-op is found", Level.INFO);
                break;
            case "+":
            case "-":
                LOG("addop is found", Level.INFO);
                break;
            case "*":
            case "/":
                LOG("mulop is found", Level.INFO);
                break;
        }

        SyntaxTreeNode newNode = new SyntaxTreeNode("op");
        SyntaxTreeNode sourceParent = sourceNode.getParent();

        sourceParent.setChild(sourceParent.getChildNum(sourceNode), newNode);
        newNode.setParent(sourceParent);
        newNode.addChild(sourceNode);
        sourceNode.setParent(newNode);

        tree = newNode;
        newNode.setValue(tk_val);
        newNode.markAsExpression();

        match(tk_val);
    }

    private void matchTerm() {
        LOG("term is found", Level.INFO);

        SyntaxTreeNode currentNode = tree;
        currentNode.markAsExpression();

        matchFactor();
        while(currentToken.getValue().equals("*") ||
                currentToken.getValue().equals("/")) {
            matchOp(currentNode);
            advanceToAChild(tree);
            matchFactor();
        }
    }

    private void matchFactor() {
        LOG("factor is found", Level.INFO);

        tree.markAsExpression();

        if (currentToken.getValue().equals("(")) {
            match("(");
            matchExp();
            match(")");
        } else if (currentToken.getType().equals("identifier")) {
            tree.setLabel("id");
            tree.setValue(currentToken.getValue());
            match("identifier");
        } else if (currentToken.getType().equals("number")) {
            tree.setLabel("const");
            tree.setValue(currentToken.getValue());
            match("number");
        } else {
            LOG("Invalid token for factor: ["+currentToken.getValue()+"]", Level.SEVERE);
        }
    }

    private void match(String s) {
        if (currentToken.getValue().equals(s) ||
                currentToken.getType().equals(s)) {
            try {
                currentToken = scanner.getNextToken();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ParserError();
            }
        } else {
            LOG("Unexpected token: [" +currentToken.getValue()+"], expected: ["+s+"]", Level.SEVERE);
        }
    }

    private void advanceToNextSameLevelNode(SyntaxTreeNode sourceNode) {
        sourceNode.setNextSameLevelNode(new SyntaxTreeNode("NIL"));
        tree = sourceNode.getNextSameLevelNode();
    }

    private void advanceToAChild(SyntaxTreeNode sourceNode) {
        sourceNode.addChild(new SyntaxTreeNode("NIL"));
        tree = sourceNode.getLastChild();
    }
}
