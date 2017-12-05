package Syntactic;

import Lexic.Token;
import Lexic.TokenStream;
import Lexic.TokenType;

import java.util.ArrayList;
import java.util.List;

import static Lexic.TokenType.*;

public class Parser {
    private TokenStream input;
    private List<Exception> exceptions;

    public Parser(TokenStream input) {
        this.input = input;
        exceptions = new ArrayList<>();
    }

    public boolean isParseSuccess() {
        return !input.isReset() && exceptions.isEmpty();
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public TreeNode parse() {
        input.reset();
        exceptions.clear();
        try {
            return parseProgram();
        } catch (UnexpectedEOFException e) {
            exceptions.add(e);
        }
        return null;
    }

    private TreeNode parseProgram() throws UnexpectedEOFException {
        TreeNode programNode = new TreeNode(NodeType.PROGRAM,1);
        while (!input.endOfStream()) {
            programNode.addChild(parseStatement());
        }
        return programNode;
    }

    private TreeNode parseStatement() throws UnexpectedEOFException {
        TreeNode statementNode = new TreeNode(NodeType.STMT);
        try {
            Token next = input.peek();
            TokenType type = next.getType();
            if (type == K_STMT_IF) {
                return parseIf();
            } else if (type == K_STMT_WHILE) {
                return parseWhile();
            } else if (type == K_STMT_FOR) {
                return parseFor();
            } else if (type == V_VARIABLE) {
                return parseAssign();
            } else if (type.isType()) {
                return parseDeclare();
            } else if (type == K_IO_READ) {
                return parseRead();
            } else if (type == K_IO_WRITE) {
                return parseWrite();
            } else if (type == S_BRACE_L) {
                return parseBlock();
            } else if (type == S_SEMICOLON) {
                input.pop();
            } else {
                throw new UnexpectedTokenException(next);
            }
        } catch (UnexpectedTokenException e) {
            //短语层错误恢复
            exceptions.add(e);
            while (!(input.peek().getType().isOneOf(K_STMT_IF, K_STMT_WHILE, K_STMT_FOR, V_VARIABLE, K_TYPE_INT, K_TYPE_DOUBLE, K_IO_READ, K_IO_WRITE, S_BRACE_L) || input.peek().getType().isOneOf(S_BRACE_R, S_SEMICOLON))) {
                input.pop();
                check();
            }
            if (input.peek().getType().isOneOf(S_BRACE_R, S_SEMICOLON)) {
                input.pop();
                return parseStatement();
            } else {
                return parseStatement();
            }
        }
        return statementNode;
    }

    private TreeNode parseBlock() throws UnexpectedEOFException {
        TreeNode blockNode = new TreeNode(NodeType.STMT_BLOCK);
        Token next = input.peek();
        if (next.getType() == S_BRACE_L) {
            input.pop(); //大括号左部
            if (input.endOfStream())
                throw new UnexpectedEOFException();
            while (input.peek().getType() != S_BRACE_R) {
                blockNode.addChild(parseStatement());
                if (input.endOfStream())
                    throw new UnexpectedEOFException();
            }
            input.pop(); //大括号右部
        } else {
            blockNode.addChild(parseStatement()); //无大括号，作用域为单个statement
        }
        return blockNode;
    }

    private TreeNode parseIf() throws UnexpectedEOFException {
        TreeNode ifNode = new TreeNode(NodeType.STMT_IF);
        try {
            Token next = input.pop(); //if关键字
            if (input.endOfStream())
                throw new UnexpectedEOFException();
            if ((next = input.pop()).getType() != S_PARENTHESIS_L) //小括号左部
                throw new UnexpectedTokenException(next, "'('");
            if (input.endOfStream())
                throw new UnexpectedEOFException();
            ifNode.addChild(parseExpression());
            if (input.endOfStream())
                throw new UnexpectedEOFException();
            if ((next = input.pop()).getType() != S_PARENTHESIS_R) //小括号左部
                throw new UnexpectedTokenException(next, "')'");
            if (input.endOfStream())
                throw new UnexpectedEOFException();

        } catch (UnexpectedTokenException e) {
            if (e.getToken() == null)
                throw e;
            exceptions.add(e);
        }
        return ifNode;
    }

    private TreeNode parseWhile() throws UnexpectedEOFException {
    }

    private TreeNode parseFor() throws UnexpectedEOFException {
    }

    private TreeNode parseAssign() throws UnexpectedEOFException {
    }

    private TreeNode parseDeclare() throws UnexpectedEOFException {
    }

    private TreeNode parseRead() throws UnexpectedEOFException {
    }

    private TreeNode parseWrite() throws UnexpectedEOFException {
    }

    private TreeNode parseVariable() throws UnexpectedEOFException {
    }

    private TreeNode parseExpression() throws UnexpectedEOFException {
    }

    private TreeNode parseExpressionLogical() throws UnexpectedEOFException {
    }

    private TreeNode parseExpressionArithmetical() throws UnexpectedEOFException {
    }

    private TreeNode parseTerm() throws UnexpectedEOFException {
    }

    private TreeNode parseFactor() throws UnexpectedEOFException {
    }

    private TreeNode parseOperatorLogical() throws UnexpectedEOFException {
    }

    private TreeNode parseOperatorComparative() throws UnexpectedEOFException {
    }

    private TreeNode parseOperatorAdditive() throws UnexpectedEOFException {
    }

    private TreeNode parseOperatorMultiplicative() throws UnexpectedEOFException {
    }

    private void check() throws UnexpectedEOFException {
        if (input.endOfStream())
            throw new UnexpectedEOFException();
    }
}
