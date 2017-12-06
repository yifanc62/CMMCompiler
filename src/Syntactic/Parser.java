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
        TreeNode programNode = new TreeNode(NodeType.PROGRAM).setLine(input.peek().getLine());
        while (!input.endOfStream()) {
            programNode.addChild(parseStatement());
        }
        return programNode;
    }

    private TreeNode parseStatement() throws UnexpectedEOFException {
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
                return parseStatement();
            } else {
                throw new UnexpectedTokenException(next).startsWith(K_STMT_IF, K_STMT_WHILE, K_STMT_FOR, V_VARIABLE, K_TYPE_INT, K_TYPE_DOUBLE, K_IO_READ, K_IO_WRITE, S_BRACE_L).endsWith(S_BRACE_R, S_SEMICOLON);
            }
        } catch (UnexpectedTokenException e) {
            exceptions.add(e);
            //短语层错误恢复
            while (!(input.peek().getType().isOneOf(e.getStartTypes()) || input.peek().getType().isOneOf(e.getEndTypes()))) {
                input.pop();
                check();
            }
            if (input.peek().getType().isOneOf(e.getEndTypes())) {
                input.pop();
                return parseStatement();
            } else {
                return parseStatement();
            }
        }
    }

    private TreeNode parseBlock() throws UnexpectedEOFException {
        TreeNode blockNode = new TreeNode(NodeType.STMT_BLOCK);
        Token next = input.peek();
        if (next.getType() == S_BRACE_L) {
            blockNode.setLine(next.getLine());
            input.pop(); //大括号左部
            check();
            while (input.peek().getType() != S_BRACE_R) {
                blockNode.addChild(parseStatement());
                check();
            }
            input.pop(); //大括号右部
        } else {
            blockNode.addChild(parseStatement()).setLine(next.getLine()); //无大括号，作用域为单个statement
        }
        return blockNode;
    }

    private TreeNode parseIf() throws UnexpectedEOFException {
        TreeNode ifNode = new TreeNode(NodeType.STMT_IF);
        Token next;
        ifNode.setLine(input.pop().getLine()); //if关键字
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_L) {
            input.pop(); //小括号左部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_L);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        check();
        ifNode.addChild(parseExpressionLogical());
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_R) {
            input.pop(); //小括号右部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_R);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        check();
        ifNode.addChild(parseBlock());
        if (!input.endOfStream() && input.peek().getType() == K_STMT_ELSE) {
            input.pop(); //else关键字
            check();
            ifNode.addChild(parseBlock());
        } else {
            ifNode.addChild(null);
        }
        return ifNode;
    }

    private TreeNode parseWhile() throws UnexpectedEOFException {
        TreeNode whileNode = new TreeNode(NodeType.STMT_WHILE);
        Token next;
        whileNode.setLine(input.pop().getLine()); //if关键字
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_L) {
            input.pop(); //小括号左部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_L);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        check();
        whileNode.addChild(parseExpressionLogical());
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_R) {
            input.pop(); //小括号右部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_R);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        check();
        whileNode.addChild(parseBlock());
        return whileNode;
    }

    private TreeNode parseFor() throws UnexpectedEOFException {
        TreeNode forNode = new TreeNode(NodeType.STMT_FOR);
        Token next;
        forNode.setLine(input.pop().getLine()); //if关键字
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_L) {
            input.pop(); //小括号左部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_L);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        check();
        //循环变量初始化表达式
        next = input.peek();
        if (next.getType().isType()) {
            forNode.addChild(parseDeclare());
        } else if (next.getType().isVariable()) {
            forNode.addChild(parseAssign());
        } else if (next.getType() == S_SEMICOLON) {
            forNode.addChild(null);
            input.pop();
        } else {
            try {
                throw new UnexpectedTokenException(next).endsWith(S_SEMICOLON);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getEndTypes())) {
                    check();
                }
            }
            forNode.addChild(null);
        }
        check();
        //循环条件表达式
        next = input.peek();
        if (next.getType() == S_SEMICOLON) {
            forNode.addChild(null);
            input.pop();
        } else {
            forNode.addChild(parseExpressionLogical());
        }
        check();
        //循环变量再赋值表达式
        next = input.peek();
        if (next.getType().isVariable()) {
            forNode.addChild(parseAssignWithoutSemicolon());
        } else if (next.getType() == S_PARENTHESIS_R) {
            forNode.addChild(null);
            input.pop();
        } else {
            try {
                throw new UnexpectedTokenException(next).endsWith(S_PARENTHESIS_R);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getEndTypes())) {
                    check();
                }
            }
            forNode.addChild(null);
        }
        return forNode;
    }

    private TreeNode parseAssign() throws UnexpectedEOFException {
        TreeNode assignNode = new TreeNode(NodeType.STMT_ASSIGN);
        Token next = input.pop();
        assignNode.setLine(next.getLine());
        assignNode.addChild(new TreeNode(NodeType.VARIABLE).setValue(next.getValue()));
        check();
        if ((next = input.peek()).getType() == S_ASSIGN) {
            input.pop(); //赋值符
        } else {
            try {
                throw new UnexpectedTokenException(next, S_ASSIGN);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                    check();
                }
            }
        }
        check();
        assignNode.addChild(parseExpressionArithmetical());
        check();
        if ((next = input.peek()).getType() == S_SEMICOLON) {
            input.pop(); //分号
        } else {
            try {
                throw new UnexpectedTokenException(next, S_SEMICOLON);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                    check();
                }
            }
        }
        return assignNode;
    }

    private TreeNode parseAssignWithoutSemicolon() throws UnexpectedEOFException {
        TreeNode assignNode = new TreeNode(NodeType.STMT_ASSIGN);
        Token next = input.pop();
        assignNode.setLine(next.getLine());
        assignNode.addChild(new TreeNode(NodeType.VARIABLE).setValue(next.getValue()));
        check();
        if ((next = input.peek()).getType() == S_ASSIGN) {
            input.pop(); //赋值符
        } else {
            try {
                throw new UnexpectedTokenException(next, S_ASSIGN);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                    check();
                }
            }
        }
        check();
        assignNode.addChild(parseExpressionArithmetical());
        check();
        if ((next = input.peek()).getType() == S_PARENTHESIS_R) {
            input.pop(); //小括号右部
        } else {
            try {
                throw new UnexpectedTokenException(next, S_PARENTHESIS_R);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                    check();
                }
            }
        }
        return assignNode;
    }

    private TreeNode parseDeclare() throws UnexpectedEOFException {
        TreeNode declareNode = new TreeNode(NodeType.STMT_DECLARE);

    }

    private TreeNode parseRead() throws UnexpectedEOFException {
        TreeNode readNode = new TreeNode(NodeType.STMT_READ);
    }

    private TreeNode parseWrite() throws UnexpectedEOFException {
        TreeNode writeNode = new TreeNode(NodeType.STMT_WRITE);
    }

    private TreeNode parseExpressionLogical() throws UnexpectedEOFException {
        TreeNode exprNode = new TreeNode(NodeType.EXP_LOGICAL);
    }

    private TreeNode parseExpressionArithmetical() throws UnexpectedEOFException {
        TreeNode exprNode = new TreeNode(NodeType.EXP_ARITHMETICAL);
    }

    private TreeNode parseTerm() throws UnexpectedEOFException {
        TreeNode termNode = new TreeNode(NodeType.TERM);
    }

    private TreeNode parseFactor() throws UnexpectedEOFException {
        TreeNode factorNode = new TreeNode(NodeType.FACTOR);
    }

    private TreeNode parseOperatorComparative() throws UnexpectedEOFException {
        Token next = input.pop();
        switch (next.getType()) {
            case S_EQUAL:
                return new TreeNode(NodeType.OP_EQUAL).setLine(next.getLine());
            case S_UNEQUAL:
                return new TreeNode(NodeType.OP_UNEQUAL).setLine(next.getLine());
            case S_GT:
                return new TreeNode(NodeType.OP_GT).setLine(next.getLine());
            case S_GE:
                return new TreeNode(NodeType.OP_GE).setLine(next.getLine());
            case S_LT:
                return new TreeNode(NodeType.OP_LT).setLine(next.getLine());
            case S_LE:
                return new TreeNode(NodeType.OP_LE).setLine(next.getLine());
            default:
                throw new UnexpectedTokenException(next, S_EQUAL, S_UNEQUAL, S_GT, S_GE, S_LT, S_LE);
        }
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
