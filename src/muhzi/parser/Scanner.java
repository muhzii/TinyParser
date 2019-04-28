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

    private static HashMap<String, String> specialSymbols = new HashMap<>();
    static {
        specialSymbols.put("+", "Addition operator");
        specialSymbols.put("-", "Subtraction operator");
        specialSymbols.put("*", "Multiplication operator");
        specialSymbols.put("/", "Division operator");
        specialSymbols.put("=", "Equality operator");
        specialSymbols.put("<", "Comparison operator");
        specialSymbols.put("(", "Left bracket");
        specialSymbols.put(")", "Right bracket");
        specialSymbols.put(";", "Semicolon");
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
        if (determineDigit(x)) {
            state = STATE.IS_NUM;
        } else if (determineAlpha(x)) {
            state = STATE.IS_IDENTIFIER;
        } else if (x == ':') {
            state = STATE.IS_ASSIGN;
        } else if (x == '{') {
            state = STATE.IS_COMMENT;
        } else if (specialSymbols.get(Character.toString((char)x)) != null) {
            state = STATE.DONE;
        } else {
            return false;
        }
        return true;
    }

    private boolean determineReservedKeyword(Token s){
        for (String i : reservedKeywords){
            if (s.getValue().equals(i))
                return true;
        }
        return false;
    }

    private void evaluateTokenType(Token s){
        if (s.getType().equals("")) {
            String symbolType = specialSymbols.get(s.getValue());

            if (symbolType != null) {
                s.setType(symbolType);
            } else if (determineReservedKeyword(s)) {
                s.setType("Reserved keyword");
            } else {
                s.setType("identifier");
            }
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

            if (append){
                String s = currentToken.getValue();
                currentToken.setValue(s + (char) x);
            } else {
                br.reset();
            }
        } while (state != STATE.DONE);

        evaluateTokenType(currentToken);
        return currentToken;
    }
}
