package com.cirnoteam.cmm;

import com.cirnoteam.cmm.lexic.Lexer;
import com.cirnoteam.cmm.lexic.TokenStream;
import com.cirnoteam.cmm.machine.Launcher;
import com.cirnoteam.cmm.semantic.Command;
import com.cirnoteam.cmm.semantic.Compiler;
import com.cirnoteam.cmm.syntactic.Parser;
import com.cirnoteam.cmm.syntactic.TreeNode;

import java.io.FileInputStream;
import java.util.List;

import static com.cirnoteam.cmm.util.Util.*;

public class Temp {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: CMMParser [file1 file2 file3...]");
        } else {
            for (String file : args) {
                try {
                    System.out.println(String.format("File: %s", file));
                    Lexer lexer = new Lexer(new FileInputStream(file));
                    TokenStream stream = lexer.getTokens();
                    if (!stream.containErrors()) {
                        printLexerError(stream);
                        return;
                    }
                    Parser parser = new Parser(stream);
                    TreeNode root = parser.parse();
                    if (!parser.isSuccess()) {
                        printParserError(parser);
                        return;
                    }
                    Compiler compiler = new Compiler(root);
                    List<Command> commands = compiler.compile();
                    if (!compiler.isSuccess()) {
                        printCompilerError(compiler);
                        return;
                    }
                    Launcher launcher = new Launcher(commands);
                    System.out.println(commandListToString(commands));
                    launcher.launch(System.in, System.out, System.err);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
