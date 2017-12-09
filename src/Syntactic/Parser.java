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
        this.input = input.reduceComments();
        exceptions = new ArrayList<>();
    }

    public boolean isSuccess() {
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
            } else if (type == K_STMT_BREAK) {
                return parseBreak();
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
                throw new UnexpectedTokenException(next).startsWith(K_STMT_IF, K_STMT_WHILE, K_STMT_FOR, V_VARIABLE, K_TYPE_INT, K_TYPE_DOUBLE, K_STMT_BREAK, K_IO_READ, K_IO_WRITE, S_BRACE_L).endsWith(S_BRACE_R, S_SEMICOLON);
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
        blockNode.setLine(next.getLine());
        if (next.getType() == S_BRACE_L) {
            input.pop(); //大括号左部
            check();
            while (input.peek().getType() != S_BRACE_R) {
                blockNode.addChild(parseStatement());
                check();
            }
            input.pop(); //大括号右部
        } else {
            blockNode.addChild(parseStatement()); //无大括号，作用域为单个statement
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
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
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
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                    check();
                }
            }
        }
        check();
        ifNode.addChild(parseBlock());
        if (!input.endOfStream() && (next = input.peek()).getType() == K_STMT_ELSE) {
            input.pop(); //else关键字
            check();
            ifNode.addChild(parseBlock().setLine(next.getLine()));
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
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
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
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
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
                while (!input.pop().getType().isOneOf(e.getStartTypes())) {
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
            if ((next = input.peek()).getType() == S_SEMICOLON) {
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
            }
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
        check();
        forNode.addChild(parseBlock());
        return forNode;
    }

    private TreeNode parseAssign() throws UnexpectedEOFException {
        TreeNode assignNode = new TreeNode(NodeType.STMT_ASSIGN);
        Token next = input.peek();
        assignNode.setLine(next.getLine());
        assignNode.addChild(parseVariable());
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
        Token next = input.peek();
        assignNode.setLine(next.getLine());
        assignNode.addChild(parseVariable());
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
        Token next = input.pop(); //类型
        declareNode.setLine(next.getLine());
        TreeNode typeNode;
        if (next.getType() == K_TYPE_INT) {
            typeNode = new TreeNode(NodeType.TYPE_INT).setLine(next.getLine());
        } else if (next.getType() == K_TYPE_DOUBLE) {
            typeNode = new TreeNode(NodeType.TYPE_DOUBLE).setLine(next.getLine());
        } else {
            try {
                throw new UnexpectedTokenException(next, K_TYPE_INT, K_TYPE_DOUBLE);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
            return parseDeclare();
        }
        check();
        if (input.peek().getType() == S_BRACKET_L) {
            input.pop(); //中括号左部
            check();
            if ((next = input.peek()).getType() == V_INT) {
                input.pop(); //索引
                typeNode.setValue(next.getValue());
            } else {
                try {
                    throw new UnexpectedTokenException(next, V_INT).endsWith(S_BRACKET_R);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.peek().getType().isOneOf(e.getEndTypes())) {
                        input.pop();
                        check();
                    }
                }
            }
            check();
            if ((next = input.peek()).getType() == S_BRACKET_R) {
                input.pop(); //中括号右部
            } else {
                try {
                    throw new UnexpectedTokenException(next, S_BRACKET_R);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                        check();
                    }
                }
            }
        }
        declareNode.addChild(typeNode);
        check();
        TreeNode idNode = new TreeNode(NodeType.IDENTIFIER);
        idNode.setLine((next = input.peek()).getLine());
        if (next.getType().isVariable()) {
            input.pop();
            idNode.addChild(new TreeNode(NodeType.NAME).setValue(next.getValue()).setLine(next.getLine()));
            if (input.peek().getType() == S_ASSIGN) {
                input.pop();
                idNode.addChild(parseExpressionArithmetical());
            } else {
                idNode.addChild(null);
            }
        } else {
            try {
                throw new UnexpectedTokenException(next, V_VARIABLE);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
        }
        declareNode.addChild(idNode);
        check();
        while ((next = input.peek()).getType() != S_SEMICOLON) {
            if (next.getType() == S_COMMA) {
                input.pop();
            } else {
                try {
                    throw new UnexpectedTokenException(next, S_COMMA).endsWith(S_COMMA, S_SEMICOLON);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.peek().getType().isOneOf(e.getEndTypes())) {
                        input.pop();
                        check();
                    }
                }
                continue;
            }
            idNode = new TreeNode(NodeType.IDENTIFIER);
            idNode.setLine((next = input.peek()).getLine());
            if (next.getType().isVariable()) {
                input.pop();
                idNode.addChild(new TreeNode(NodeType.NAME).setValue(next.getValue()).setLine(next.getLine()));
                if (input.peek().getType() == S_ASSIGN) {
                    input.pop();
                    idNode.addChild(parseExpressionArithmetical());
                } else {
                    idNode.addChild(null);
                }
            } else {
                try {
                    throw new UnexpectedTokenException(next, V_VARIABLE);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                        input.pop();
                        check();
                    }
                }
            }
            declareNode.addChild(idNode);
            check();
        }
        input.pop(); //分号
        return declareNode;
    }

    private TreeNode parseBreak() throws UnexpectedEOFException {
        TreeNode breakNode = new TreeNode(NodeType.STMT_BREAK);
        breakNode.setLine(input.pop().getLine()); //break关键字
        check();
        Token next;
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
        return breakNode;
    }

    private TreeNode parseRead() throws UnexpectedEOFException {
        TreeNode readNode = new TreeNode(NodeType.STMT_READ);
        Token next;
        readNode.setLine(input.pop().getLine()); //read关键字
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
        readNode.addChild(parseVariable());
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
        return readNode;
    }

    private TreeNode parseWrite() throws UnexpectedEOFException {
        TreeNode writeNode = new TreeNode(NodeType.STMT_WRITE);
        Token next;
        writeNode.setLine(input.pop().getLine()); //write关键字
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
        writeNode.addChild(parseExpressionArithmetical());
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
        return writeNode;
    }

    private TreeNode parseVariable() throws UnexpectedEOFException {
        TreeNode varNode = new TreeNode(NodeType.VARIABLE);
        Token next = input.peek();
        varNode.setLine(next.getLine());
        if (next.getType().isVariable()) {
            input.pop(); //变量名
            varNode.addChild(new TreeNode(NodeType.NAME).setValue(next.getValue()).setLine(next.getLine()));
        } else {
            try {
                throw new UnexpectedTokenException(next, V_VARIABLE);
            } catch (UnexpectedTokenException e) {
                exceptions.add(e);
                //短语层错误恢复
                while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                    input.pop();
                    check();
                }
            }
            return parseVariable();
        }
        if (!input.endOfStream() && input.peek().getType() == S_BRACKET_L) {
            input.pop(); //中括号左部
            check();
            varNode.addChild(parseExpressionArithmetical());
            check();
            if ((next = input.peek()).getType() == S_BRACKET_R) {
                input.pop(); //中括号右部
            } else {
                try {
                    throw new UnexpectedTokenException(next, S_BRACKET_R);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.pop().getType().isOneOf(e.getStartTypes())) {
                        check();
                    }
                }
            }
        } else {
            varNode.addChild(null);
        }
        return varNode;
    }

    private TreeNode parseExpressionLogical() throws UnexpectedEOFException {
        TreeNode exprNode = new TreeNode(NodeType.EXP_LOGICAL);
        Token next = input.peek();
        exprNode.setLine(next.getLine());
        if (next.getType().isOneOf(V_BOOL_TRUE, V_BOOL_FALSE)) {
            input.pop();
            exprNode.addChild(new TreeNode(NodeType.V_BOOL).setValue(next.getValue()).setLine(next.getLine()));
            exprNode.addChild(null);
            exprNode.addChild(null);
        } else {
            exprNode.addChild(parseExpressionArithmetical());
            if (!input.endOfStream() && input.peek().getType().isComparativeOperator()) {
                exprNode.addChild(parseOperatorComparative());
                check();
                exprNode.addChild(parseExpressionArithmetical());
            } else {
                exprNode.addChild(null);
                exprNode.addChild(null);
            }
        }
        return exprNode;
    }

    private TreeNode parseExpressionArithmetical() throws UnexpectedEOFException {
        TreeNode exprNode = new TreeNode(NodeType.EXP_ARITHMETICAL);
        TreeNode termNode = parseTerm();
        exprNode.setLine(termNode.getLine());
        check();
        Token next = input.peek();
        if (next.getType() == S_PLUS) {
            input.pop();
            exprNode.addChild(termNode);
            exprNode.addChild(new TreeNode(NodeType.OP_PLUS).setLine(next.getLine()));
            check();
            exprNode.addChild(parseExpressionArithmetical());
        } else if (next.getType() == S_MINUS) {
            input.pop();
            exprNode.addChild(termNode);
            exprNode.addChild(new TreeNode(NodeType.OP_MINUS).setLine(next.getLine()));
            check();
            exprNode.addChild(parseExpressionArithmetical());
        } else {
            return termNode;
        }
        return exprNode;
    }

    private TreeNode parseTerm() throws UnexpectedEOFException {
        TreeNode termNode = new TreeNode(NodeType.TERM);
        TreeNode factorNode = parseFactor();
        termNode.setLine(factorNode.getLine());
        check();
        Token next = input.peek();
        if (next.getType() == S_MULTIPLY) {
            input.pop();
            termNode.addChild(factorNode);
            termNode.addChild(new TreeNode(NodeType.OP_MULTIPLY).setLine(next.getLine()));
            check();
            termNode.addChild(parseTerm());
        } else if (next.getType() == S_DIVIDE) {
            input.pop();
            termNode.addChild(factorNode);
            termNode.addChild(new TreeNode(NodeType.OP_DIVIDE).setLine(next.getLine()));
            check();
            termNode.addChild(parseTerm());
        } else if (next.getType() == S_MOD) {
            input.pop();
            termNode.addChild(factorNode);
            termNode.addChild(new TreeNode(NodeType.OP_MOD).setLine(next.getLine()));
            check();
            termNode.addChild(parseTerm());
        } else {
            return factorNode;
        }
        return termNode;
    }

    private TreeNode parseFactor() throws UnexpectedEOFException {
        TreeNode factorNode = new TreeNode(NodeType.FACTOR);
        Token next = input.peek();
        factorNode.setLine(next.getLine());
        if (next.getType() == S_PARENTHESIS_L) {
            input.pop();
            check();
            TreeNode exprNode = parseExpressionArithmetical();
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
            return exprNode;
        } else if (next.getType() == V_INT) {
            input.pop();
            return new TreeNode(NodeType.V_INT).setValue(next.getValue()).setLine(next.getLine());
        } else if (next.getType() == V_DOUBLE) {
            input.pop();
            return new TreeNode(NodeType.V_DOUBLE).setValue(next.getValue()).setLine(next.getLine());
        } else if (next.getType() == S_PLUS) {
            input.pop();
            check();
            return parseExpressionArithmetical();
        } else if (next.getType() == S_MINUS) {
            input.pop();
            factorNode.addChild(new TreeNode(NodeType.OP_MINUS).setLine(next.getLine()));
            check();
            factorNode.addChild(parseExpressionArithmetical());
            return factorNode;
        } else {
            return parseVariable();
        }
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
                try {
                    throw new UnexpectedTokenException(next, S_EQUAL, S_UNEQUAL, S_GT, S_GE, S_LT, S_LE);
                } catch (UnexpectedTokenException e) {
                    exceptions.add(e);
                    //短语层错误恢复
                    while (!input.peek().getType().isOneOf(e.getStartTypes())) {
                        input.pop();
                        check();
                    }
                }
                return parseOperatorComparative();
        }
    }

    private void check() throws UnexpectedEOFException {
        if (input.endOfStream())
            throw new UnexpectedEOFException();
    }
}
