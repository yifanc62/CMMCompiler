package Util;

import Lexic.Token;
import Lexic.TokenStream;
import Lexic.TokenType;
import Semantic.Command;
import Semantic.Compiler;
import Syntactic.Parser;
import Syntactic.TreeNode;

import java.util.List;

public class Util {
    public static void printToken(TokenStream tokenStream) {
        tokenStream = new TokenStream(tokenStream);
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            System.out.println(String.format("[line: %d, position: %d]type: %s, value: %s", token.getLine(), token.getPosition(), token.getType(), token.getValue()));
        }
    }

    public static void printLexerError(TokenStream tokenStream) {
        for (Token token : tokenStream.getErrors()) {
            if (token.getType().isUnrecognized()) {
                System.out.println(String.format("[line: %d, position: %d]Unrecognized char: '%s'", token.getLine(), token.getPosition(), token.getValue()));
            } else {
                System.out.println(String.format("[line: %d, position: %d]Comment block not closed", token.getLine(), token.getPosition()));
            }
        }
    }

    public static void printParserError(Parser parser) {
        for (Exception e : parser.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }

    public static void printCompilerError(Compiler compiler) {
        for (Exception e : compiler.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }

    public static void printTreeNode(TreeNode root) {
        if (root == null) {
            System.out.println("NULL");
        } else if (!root.getChildren().isEmpty()) {
            System.out.println(root.getType().toString());
            for (TreeNode child : root.getChildren()) {
                printTreeNode(child, 1);
            }
        } else {
            System.out.println(root.getType().toString() + (root.getValue() == null ? "" : ": " + root.getValue()));
        }
    }

    private static void printTreeNode(TreeNode node, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
        if (node == null) {
            System.out.println("NULL");
        } else if (!node.getChildren().isEmpty()) {
            System.out.println(node.getType().toString());
            for (TreeNode child : node.getChildren()) {
                printTreeNode(child, indent + 1);
            }
        } else {
            System.out.println(node.getType().toString() + (node.getValue() == null ? "" : ": " + node.getValue()));
        }
    }

    public static String tokenTypesToString(TokenType... types) {
        if (types.length == 0) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (TokenType type : types) {
                builder.append(type.toString());
                builder.append(", ");
            }
            String result = builder.toString();
            return result.substring(0, result.length() - 2);
        }
    }

    public static String commandListToString(List<Command> commands) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Command command : commands) {
            builder.append(command.encode());
            builder.append("\r\n");
        }
        return builder.toString();
    }
}
