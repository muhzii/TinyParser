package muhzi.parser;

import muhzi.parser.errors.TokenError;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

class Scanner {

    private enum STATE {
        IS_NUM,
        IS_IDENTIFIER,
        IS_ASSIGN,
        IS_COMMENT,
        DONE
    }

    private static HashMap<String, String> reservedSymbols = new HashMap<>();
    static {
        reservedSymbols.put("+", "Addition operator");
        reservedSymbols.put("-", "Subtraction operator");
        reservedSymbols.put("*", "Multiplication operator");
        reservedSymbols.put("/", "Division operator");
        reservedSymbols.put("=", "Equality operator");
        reservedSymbols.put("<", "Comparison operator");
        reservedSymbols.put("(", "Left bracket");
        reservedSymbols.put(")", "Right bracket");
        reservedSymbols.put(";", "Semicolon");
    }

    private static String[] reservedKeywords = {
            "if",
            "then",
            "else",
            "end",
            "repeat",
            "until",
            "read",
            "write"
    };

    private BufferedReader br;
    private STATE state;

    Scanner(BufferedReader br) {
        this.br = br;
    }

    private boolean determineDigit(int x) {
        return Character.isDigit(x);
    }

    private boolean determineWhiteSpace(int x) {
        return Character.isWhitespace(x);
    }

    private boolean determineAlpha(int x) {
        return Character.isAlphabetic(x);
    }

    private boolean determineGeneric(int x) {
        if (determineDigit(x))
            state = STATE.IS_NUM;
        else if (determineAlpha(x))
            state = STATE.IS_IDENTIFIER;
        else if (x == ':')
            state = STATE.IS_ASSIGN;
        else if (x == '{')
            state = STATE.IS_COMMENT;
        else if (reservedSymbols.get(Character.toString((char)x)) != null)
            state = STATE.DONE;
        else
            return false;
        return true;
    }

    private boolean checkReservedKeyword(Token s){
        for (String i : reservedKeywords){
            if (s.getValue().equals(i))
                return true;
        }
        return false;
    }

    private void checkTokenType(Token s){
        if (s.getType().equals("")) {
            String symbolType = reservedSymbols.get(s.getValue());

            if (symbolType != null)
                s.setType(symbolType);
            else if (checkReservedKeyword(s))
                s.setType("Reserved keyword");
            else
                s.setType("identifier");
        }
    }

    private void appendToToken(Token tk, int val, boolean doAppend) throws IOException {
        if (doAppend){
            String s = tk.getValue();
            tk.setValue(s + (char) val);
        } else {
            br.reset();
        }
    }

    Token getNextToken() throws IOException {
        int x;
        state = null;
        boolean append;
        Token currentToken = new Token();

        do {
            append = true;
            br.mark(0);
            x = br.read();

            if (state == null) {
                if (x == -1)
                    return new Token();

                boolean recognized = determineGeneric(x);
                if (!recognized) {
                    if (determineWhiteSpace(x)) {
                        return getNextToken();
                    } else {
                        throw new TokenError(""+(char)x);
                    }
                }
            } else if (state == STATE.IS_NUM) {
                currentToken.setType("number");
                if (!determineDigit(x)) {
                    append = false;
                    state = STATE.DONE;
                }
            } else if (state == STATE.IS_ASSIGN) {
                currentToken.setType("Assignment operator");
                if (x == '=') {
                    state = STATE.DONE;
                } else {
                    throw new TokenError(":"+(char)x);
                }
            } else if (state == STATE.IS_IDENTIFIER) {
                if (!determineAlpha(x) && !determineDigit(x)) {
                    append = false;
                    state = STATE.DONE;
                }
            } else if (state == STATE.IS_COMMENT) {
                if (x == '}')
                    return getNextToken();
            }

            appendToToken(currentToken, x, append);
        } while (state != STATE.DONE);

        checkTokenType(currentToken);
        return currentToken;
    }
}
