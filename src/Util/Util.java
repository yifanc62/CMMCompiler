package Util;

import Lexic.Token;
import Lexic.TokenStream;
import Lexic.TokenType;
import Semantic.Command;
import Semantic.Compiler;
import Syntactic.Parser;
import Syntactic.TreeNode;

import java.util.List;

import org.eclipse.swt.widgets.Text;

public class Util {
    public static void printToken(TokenStream tokenStream) {
        tokenStream = new TokenStream(tokenStream);
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            System.out.println(String.format("[line: %d, position: %d]type: %s, value: %s", token.getLine(), token.getPosition(), token.getType(), token.getValue()));
        }
    }
    
    public static void outputToken(TokenStream tokenStream, Text outText) {
    	tokenStream = new TokenStream(tokenStream);
		outText.append("分析结果：\n");
        while (!tokenStream.endOfStream()) {
            Token token = tokenStream.pop();
            outText.append(String.format("[line: %d, position: %d]type: %s, value: %s", token.getLine(), token.getPosition(), token.getType(), token.getValue()));
            outText.append(System.getProperty("line.separator"));
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

    public static void outputLexerError(TokenStream tokenStream, Text outText) {
    	outText.append("错误信息：\n");
        for (Token token : tokenStream.getErrors()) {
            if (token.getType().isUnrecognized()) {
            	outText.append(String.format("[line: %d, position: %d]Unrecognized char: '%s'", token.getLine(), token.getPosition(), token.getValue()));
            	outText.append(System.getProperty("line.separator"));
            } else {
                outText.append(String.format("[line: %d, position: %d]Comment block not closed", token.getLine(), token.getPosition()));
                outText.append(System.getProperty("line.separator"));
            }
        }
	}
    
    public static void printParserError(Parser parser) {
        for (Exception e : parser.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void outputParserError(Parser parser, Text outText) {
    	outText.append("错误信息：\n");
    	for (Exception e : parser.getExceptions()) {
    		outText.append(e.getMessage());
    		outText.append(System.getProperty("line.separator"));
        }
	}

    public static void printCompilerError(Compiler compiler) {
        for (Exception e : compiler.getExceptions()) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void outputCompilerError(Compiler compiler, Text outText) {
    	outText.append("错误信息：\n");
    	for (Exception e : compiler.getExceptions()) {
    		outText.append(e.getMessage());
    		outText.append(System.getProperty("line.separator"));
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
    
    public static void outputTreeNode(TreeNode root, Text outText) {
    	outText.append("分析结果：\n");
    	 if (root == null) {
    		 outText.append("NULL");
    		 outText.append(System.getProperty("line.separator"));
         } else if (!root.getChildren().isEmpty()) {
        	 outText.append(root.getType().toString());
        	 outText.append(System.getProperty("line.separator"));
             for (TreeNode child : root.getChildren()) {
                 outputTreeNode(child, 1, outText);
             }
         } else {
        	 outText.append(root.getType().toString() + (root.getValue() == null ? "" : ": " + root.getValue()));
        	 outText.append(System.getProperty("line.separator"));
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
    
    private static void outputTreeNode(TreeNode node, int indent, Text outText) {
        for (int i = 0; i < indent; i++) {
        	outText.append("\t");
        }
        if (node == null) {
        	outText.append("NULL");
        	outText.append(System.getProperty("line.separator"));
        } else if (!node.getChildren().isEmpty()) {
        	outText.append(node.getType().toString());
        	outText.append(System.getProperty("line.separator"));
            for (TreeNode child : node.getChildren()) {
                outputTreeNode(child, indent + 1, outText);
            }
        } else {
        	outText.append(node.getType().toString() + (node.getValue() == null ? "" : ": " + node.getValue()));
            outText.append(System.getProperty("line.separator"));
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
    
    public static void outputCommandList(List<Command> commands, Text outText) {
    	for (Command command : commands) {
    		outText.append(command.encode());
    		outText.append(System.getProperty("line.separator"));
        }
    }
}
