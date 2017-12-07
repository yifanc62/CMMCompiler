import Lexic.Lexer;
import Lexic.TokenStream;
import Syntactic.Parser;
import Syntactic.TreeNode;

import java.io.FileInputStream;

import static Util.Util.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: CMMParser [file1 file2 file3...]");
            return;
        }
        for (String file : args) {
            try {
                Lexer lexer = new Lexer(new FileInputStream(file));
                TokenStream stream = lexer.getTokens();
                if (!stream.isSuccess()) {
                    printLexerError(stream);
                    return;
                }
                Parser parser = new Parser(stream);
                TreeNode root = parser.parse();
                if (!parser.isSuccess()) {
                    printParserError(parser);
                    return;
                }
                printTreeNode(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}