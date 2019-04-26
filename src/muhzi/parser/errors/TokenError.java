package muhzi.parser.errors;

public class TokenError extends Error {
    public TokenError(String token) {
        super("Invalid Token: ["+token+"]");
    }
}
