package Util;

import Lexic.Token;
import Lexic.TokenStream;
import Syntactic.TreeNode;

public class Util {
    public static void printToken(TokenStream tokenStream) {
        tokenStream = new TokenStream(tokenStream);
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            System.out.println(String.format("[line:%d, position:%d]type:%s, value:%s", token.getLine(), token.getPosition(), token.getType(), token.getValue()));
        }
    }

    public static void printLexerError(TokenStream tokenStream) {
        for (Token token : tokenStream.getErrors()) {
            if (token.getType().isUnrecognized()) {
                System.out.println(String.format("[line:%d, position:%d]Unrecognized char:'%s'", token.getLine(), token.getPosition(), token.getValue()));
            } else {
                System.out.println(String.format("[line:%d, position:%d]Comment block not closed", token.getLine(), token.getPosition()));
            }
        }
    }

    public static void printTreeNode(TreeNode root) {

    }

    private static void printTreeNode(TreeNode node, int indent) {

    }
}
