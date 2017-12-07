package Lexic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TokenStream {
    private List<Token> tokens;
    private int position = 0;

    public TokenStream() {
        this.tokens = new ArrayList<>();
    }

    public TokenStream(TokenStream tokenStream) {
        this.tokens = new ArrayList<>(tokenStream.getAllTokens());
    }

    public TokenStream(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
    }

    public boolean containErrors() {
        return getErrors().isEmpty();
    }

    private List<Token> getAllTokens() {
        return tokens;
    }

    public boolean endOfStream() {
        return position >= tokens.size();
    }

    public TokenStream add(Token token) {
        tokens.add(token);
        return this;
    }

    public Token pop() {
        if (endOfStream())
            return null;
        return tokens.get(position++);
    }

    public Token peek() {
        if (endOfStream())
            return null;
        return tokens.get(position);
    }

    public void reset() {
        position = 0;
    }

    public boolean isReset() {
        return position == 0;
    }

    public List<Token> getErrors() {
        return tokens.stream().filter(t -> t.getType().isError()).collect(Collectors.toList());
    }

    public TokenStream reduceErrors() {
        return new TokenStream(tokens.stream().filter(t -> !t.getType().isError()).collect(Collectors.toList()));
    }

    public TokenStream reduceComments() {
        return new TokenStream(tokens.stream().filter(t -> !t.getType().isComment()).collect(Collectors.toList()));
    }
}
