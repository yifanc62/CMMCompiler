package com.cirnoteam.cmm.util;

import com.cirnoteam.cmm.lexic.Token;
import com.cirnoteam.cmm.lexic.TokenStream;
import com.cirnoteam.cmm.lexic.TokenType;
import com.cirnoteam.cmm.semantic.Command;
import com.cirnoteam.cmm.semantic.Compiler;
import com.cirnoteam.cmm.syntactic.Parser;
import com.cirnoteam.cmm.syntactic.TreeNode;

import java.util.List;

public class Util {
    public static void printToken(TokenStream tokenStream) {
        tokenStream = new TokenStream(tokenStream);
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            System.out.println(String.format("[line: %d, position: %d]type: %s, value: %s", token.getLine(), token.getPosition(), token.getType().name(), token.getValue()));
        }
    }

    public static String outputToken(TokenStream tokenStream) {
        tokenStream = new TokenStream(tokenStream);
        StringBuilder outText = new StringBuilder();
        outText.append("词法分析结果：\r\n");
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            outText.append(String.format("[line: %d, position: %d]type: %s, value: %s\r\n", token.getLine(), token.getPosition(), token.getType().name(), token.getValue()));
        }
        return outText.toString();
    }

    public static void printLexerError(TokenStream tokenStream) {
        for (Token token : tokenStream.getErrors()) {
            if (token.getType().isUnrecognized()) {
                System.out.println(String.format("[line: %d, position: %d]Unrecognized char: '%s'.", token.getLine(), token.getPosition(), token.getValue()));
            } else if (token.getType().isValueInvalid()) {
                System.out.println(String.format("[line: %d, position: %d]Value not invalid: '%s'.", token.getLine(), token.getPosition(), token.getValue()));
            } else {
                System.out.println(String.format("[line: %d, position: %d]Comment block not closed.", token.getLine(), token.getPosition()));
            }
        }
    }

    public static String outputLexerError(TokenStream tokenStream) {
        StringBuilder outText = new StringBuilder();
        outText.append("词法分析错误信息：\r\n");
        for (Token token : tokenStream.getErrors()) {
            if (token.getType().isUnrecognized()) {
                outText.append(String.format("[line: %d, position: %d]Unrecognized char: '%s'.\r\n", token.getLine(), token.getPosition(), token.getValue()));
            } else if (token.getType().isValueInvalid()) {
                outText.append(String.format("[line: %d, position: %d]Value not invalid: '%s'.\r\n", token.getLine(), token.getPosition(), token.getValue()));
            } else {
                outText.append(String.format("[line: %d, position: %d]Comment block not closed.\r\n", token.getLine(), token.getPosition()));
            }
        }
        return outText.toString();
    }

    public static void printParserError(Parser parser) {
        for (Exception e : parser.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }

    public static String outputParserError(Parser parser) {
        StringBuilder outText = new StringBuilder();
        outText.append("语法分析错误信息：\r\n");
        for (Exception e : parser.getExceptions()) {
            outText.append(e.getMessage());
            outText.append("\r\n");
        }
        return outText.toString();
    }

    public static void printCompilerError(Compiler compiler) {
        for (Exception e : compiler.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }

    public static String outputCompilerError(Compiler compiler) {
        StringBuilder outText = new StringBuilder();
        outText.append("语义分析错误信息：\r\n");
        for (Exception e : compiler.getExceptions()) {
            outText.append(e.getMessage());
            outText.append("\r\n");
        }
        return outText.toString();
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

    public static String outputTreeNode(TreeNode root) {
        StringBuilder outText = new StringBuilder();
        outText.append("语法分析结果：\r\n");
        if (root == null) {
            outText.append("NULL\r\n");
        } else if (!root.getChildren().isEmpty()) {
            outText.append(root.getType().toString());
            outText.append("\r\n");
            for (TreeNode child : root.getChildren()) {
                outText.append(outputTreeNode(child, 1));
            }
        } else {
            outText.append(root.getType().toString()).append(root.getValue() == null ? "" : ": " + root.getValue());
            outText.append("\r\n");
        }
        return outText.toString();
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

    private static String outputTreeNode(TreeNode node, int indent) {
        StringBuilder outText = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            outText.append("\t");
        }
        if (node == null) {
            outText.append("NULL\r\n");
        } else if (!node.getChildren().isEmpty()) {
            outText.append(node.getType().toString());
            outText.append("\r\n");
            for (TreeNode child : node.getChildren()) {
                outText.append(outputTreeNode(child, indent + 1));
            }
        } else {
            outText.append(node.getType().toString()).append(node.getValue() == null ? "" : ": " + node.getValue());
            outText.append("\r\n");
        }
        return outText.toString();
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
        for (Command command : commands) {
            builder.append(command.encode());
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static String outputCommandList(List<Command> commands) {
        StringBuilder builder = new StringBuilder();
        builder.append("可执行中间代码：\r\n");
        for (int i = 0; i < commands.size(); i++) {
            builder.append(i);
            builder.append(" ");
            builder.append(commands.get(i).encode());
            builder.append("\r\n");
        }
        return builder.toString().replaceAll("<", "eax").replaceAll(">", "ebx");
    }
}
