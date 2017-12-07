package Syntactic;

import Lexic.Token;
import Lexic.TokenType;

import static Util.Util.tokenTypesToString;

public class UnexpectedTokenException extends Exception {
    private Token token;
    private TokenType[] startTypes;
    private TokenType[] endTypes;

    public UnexpectedTokenException(Token token) {
        super(String.format("[line: %d, position: %d]Unexpected Token: '%s'", token.getLine(), token.getPosition(), token.getValue()));
        this.token = token;
    }

    public UnexpectedTokenException(Token token, TokenType... startTypes) {
        super(String.format("[line: %d, position: %d]Unexpected Token: '%s', expected: {%s}", token.getLine(), token.getPosition(), token.getValue(), tokenTypesToString(startTypes)));
        this.startTypes = startTypes;
        this.token = token;
    }

    public UnexpectedTokenException startsWith(TokenType... types) {
        startTypes = types;
        return this;
    }

    public UnexpectedTokenException endsWith(TokenType... types) {
        endTypes = types;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public TokenType[] getStartTypes() {
        return startTypes;
    }

    public TokenType[] getEndTypes() {
        return endTypes;
    }
}
