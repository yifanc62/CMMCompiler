package Syntactic;

import Lexic.Token;

public class UnexpectedTokenException extends Exception {
    private Token token;

    public UnexpectedTokenException(Token token) {
        super(String.format("[line:%d, position:%d]Unexpected Token: %s", token.getLine(), token.getPosition(), token.getValue()));
        this.token = token;
    }

    public UnexpectedTokenException(Token token, String expectedToken) {
        super(String.format("[line:%d, position:%d]Unexpected Token: %s, expected: %s", token.getLine(), token.getPosition(), token.getValue(), expectedToken));
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
