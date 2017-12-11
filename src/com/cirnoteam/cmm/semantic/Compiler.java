package com.cirnoteam.cmm.semantic;

import com.cirnoteam.cmm.syntactic.TreeNode;

import java.util.ArrayList;
import java.util.List;

import static com.cirnoteam.cmm.semantic.CommandType.*;
import static com.cirnoteam.cmm.semantic.VariableType.*;
import static com.cirnoteam.cmm.syntactic.NodeType.*;

public class Compiler {
    private TreeNode programNode;
    private List<Command> result;
    private VariableTable varTable;
    private List<Exception> exceptions;

    public Compiler(TreeNode programNode) {
        this.programNode = programNode;
        result = new ArrayList<>();
        varTable = new VariableTable();
        exceptions = new ArrayList<>();
    }

    public boolean isSuccess() {
        return exceptions.isEmpty();
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public List<Command> compile() {
        return compile(true);
    }

    public List<Command> compile(boolean optimize) {
        result.clear();
        varTable = new VariableTable();
        exceptions.clear();
        addCommands(compileBlock(programNode, true, null, 0));
        addCommand(new Command(EXIT, null, null, null));
        if (optimize) {
            Optimizer optimizer = new Optimizer(result);
            List<Command> newResult = optimizer.optimize();
            while (result.size() != newResult.size()) {
                if (!optimizer.isSuccess()) {
                    exceptions.addAll(optimizer.getExceptions());
                    return newResult;
                }
                result = newResult;
                newResult = (optimizer = new Optimizer(result)).optimize();
            }
            if (!optimizer.isSuccess()) {
                exceptions.addAll(optimizer.getExceptions());
                return newResult;
            }
        }
        return result;
    }

    private List<Command> compileBlock(TreeNode blockNode, boolean into, List<Command> breakCommands, int level) {
        List<Command> result = new ArrayList<>();
        if (!into) {
            addCommand(result, new Command(IN, null, null, null));
            varTable = varTable.createChild();
        }
        for (TreeNode node : blockNode.getChildren()) {
            try {
                switch (node.getType()) {
                    case STMT_BLOCK:
                        addCommands(result, compileBlock(node, breakCommands, level + 1));
                        break;
                    case STMT_IF:
                        addCommands(result, compileIf(node, breakCommands, level + 1));
                        break;
                    case STMT_WHILE:
                        addCommands(result, compileWhile(node));
                        break;
                    case STMT_FOR:
                        addCommands(result, compileFor(node));
                        break;
                    case STMT_BREAK:
                        if (breakCommands != null && level > 0) {
                            for (int i = 0; i < level; i++) {
                                addCommand(result, new Command(OUT, null, null, null));
                            }
                            Command breakCommand = new Command(JMP, "0", null, null, node.getLine());
                            addCommand(result, breakCommand);
                            breakCommands.add(breakCommand);
                            break;
                        }
                        throw new CompilerException("Break statement not allowed here", node.getLine());
                    case STMT_ASSIGN:
                        addCommands(result, compileAssign(node));
                        break;
                    case STMT_DECLARE:
                        addCommands(result, compileDeclare(node));
                        break;
                    case STMT_READ:
                        addCommands(result, compileRead(node));
                        break;
                    case STMT_WRITE:
                        addCommands(result, compileWrite(node));
                        break;
                    default:
                        //Unreachable area
                        break;
                }
            } catch (CompilerException e) {
                exceptions.add(e);
            }
        }
        if (!into) {
            varTable = varTable.returnParent();
            addCommand(result, new Command(OUT, null, null, null));
        }
        return result;
    }

    private List<Command> compileBlock(TreeNode blockNode, List<Command> breakCommands, int level) {
        return compileBlock(blockNode, false, breakCommands, level);
    }

    private List<Command> compileIf(TreeNode ifNode, List<Command> breakCommands, int level) {
        List<Command> result = new ArrayList<>();
        TreeNode exprNode = ifNode.getChild(0);
        TreeNode ifBlockNode = ifNode.getChild(1);
        TreeNode elseBlockNode = ifNode.getChild(2);
        Command jmpToElse = null;
        try {
            addCommands(result, compileExpressionLogical(exprNode));
            jmpToElse = result.get(result.size() - 1);
        } catch (CompilerException e) {
            exceptions.add(e);
        }
        addCommands(result, compileBlock(ifBlockNode, breakCommands, level));
        if (jmpToElse != null)
            jmpToElse.setAddress(result.size());
        if (elseBlockNode != null) {
            Command jmpToEnd = new Command(JMP, null, null, null, elseBlockNode.getLine());
            addCommand(result, jmpToEnd);
            if (jmpToElse != null)
                jmpToElse.setAddress(result.size());
            addCommands(result, compileBlock(elseBlockNode, breakCommands, level));
            jmpToEnd.setAddress(result.size());
        }
        return result;
    }

    private List<Command> compileWhile(TreeNode whileNode) {
        List<Command> result = new ArrayList<>();
        TreeNode exprNode = whileNode.getChild(0);
        TreeNode blockNode = whileNode.getChild(1);
        Command jmpToEnd = null;
        try {
            addCommands(result, compileExpressionLogical(exprNode));
            jmpToEnd = result.get(result.size() - 1);
        } catch (CompilerException e) {
            exceptions.add(e);
        }
        List<Command> breakCommands = new ArrayList<>();
        addCommands(result, compileBlock(blockNode, breakCommands, 1));
        addCommand(result, new Command(JMP, "0", null, null));
        int pToEnd = result.size();
        if (jmpToEnd != null)
            jmpToEnd.setAddress(pToEnd);
        for (Command breakCommand : breakCommands) {
            breakCommand.setAddress(pToEnd);
        }
        return result;
    }

    private List<Command> compileFor(TreeNode forNode) {
        List<Command> result = new ArrayList<>();
        TreeNode initNode = forNode.getChild(0);
        TreeNode exprNode = forNode.getChild(1);
        TreeNode assignNode = forNode.getChild(2);
        TreeNode blockNode = forNode.getChild(3);
        addCommand(result, new Command(IN, null, null, null));
        varTable = varTable.createChild();
        if (initNode != null) {
            try {
                switch (initNode.getType()) {
                    case STMT_ASSIGN:
                        addCommands(result, compileAssign(initNode));
                        break;
                    case STMT_DECLARE:
                        addCommands(result, compileDeclare(initNode));
                        break;
                    default:
                        //Unreachable area
                        break;
                }
            } catch (CompilerException e) {
                exceptions.add(e);
            }
        }
        int pToStart = result.size();
        Command jmpToEnd = null;
        if (exprNode != null) {
            try {
                addCommands(result, compileExpressionLogical(exprNode));
                jmpToEnd = result.get(result.size() - 1);
            } catch (CompilerException e) {
                exceptions.add(e);
            }
        }
        List<Command> breakCommands = new ArrayList<>();
        addCommands(result, compileBlock(blockNode, true, breakCommands, 1));
        if (assignNode != null) {
            try {
                addCommands(result, compileAssign(assignNode));
            } catch (CompilerException e) {
                exceptions.add(e);
            }
        }
        addCommand(result, new Command(JMP, Integer.toString(pToStart), null, null));
        int pToEnd = result.size();
        if (jmpToEnd != null)
            jmpToEnd.setAddress(pToEnd);
        varTable = varTable.returnParent();
        addCommand(result, new Command(OUT, null, null, null));
        pToEnd = result.size();
        for (Command breakCommand : breakCommands) {
            breakCommand.setAddress(pToEnd);
        }
        return result;
    }

    private List<Command> compileAssign(TreeNode assignNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode varNode = assignNode.getChild(0);
        TreeNode exprNode = assignNode.getChild(1);
        String name = varNode.getChild(0).getValue();
        TreeNode indexExprNode = varNode.getChild(1);
        addCommands(result, compileExpressionArithmetical(exprNode));
        if (indexExprNode == null) {
            if (!varTable.existAll(name))
                throw new CompilerException(String.format("Unknown variable '%s'", name), varNode.getLine());
            if (varTable.get(name) == ARRAY_DOUBLE || varTable.get(name) == ARRAY_INT)
                throw new CompilerException(String.format("Array '%s' need an index", name), varNode.getLine());
            addCommand(result, new Command(POP, "<", null, null, exprNode.getLine()));
            addCommand(result, new Command(MOV, name, null, "<", assignNode.getLine()));
        } else {
            if (!varTable.existAll(name))
                throw new CompilerException(String.format("Unknown variable '%s'", name), varNode.getLine());
            if (varTable.get(name) == DOUBLE || varTable.get(name) == INT)
                throw new CompilerException(String.format("Variable '%s' do not need an index", name), varNode.getLine());
            addCommands(result, compileExpressionArithmetical(indexExprNode));
            addCommand(result, new Command(POP, ">", null, null, indexExprNode.getLine()));
            addCommand(result, new Command(POP, "<", null, null, exprNode.getLine()));
            addCommand(result, new Command(MOV, name, ">", "<", assignNode.getLine()));
        }
        return result;
    }

    private List<Command> compileDeclare(TreeNode declareNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode typeNode = declareNode.getChild(0);
        int length = typeNode.getValue() == null ? -1 : Integer.valueOf(typeNode.getValue());
        VariableType type = typeNode.getType() == TYPE_INT ? (length == -1 ? INT : ARRAY_INT) : (length == -1 ? DOUBLE : ARRAY_DOUBLE);
        for (int i = 1; i < declareNode.getChildren().size(); i++) {
            TreeNode idNode = declareNode.getChild(i);
            String name = idNode.getChild(0).getValue();
            TreeNode initExprNode = idNode.getChild(1);
            if (varTable.existCurrent(name))
                throw new CompilerException(String.format("Redefined variable '%s'", name), idNode.getLine());
            varTable.put(name, type);
            if (initExprNode == null) {
                switch (type) {
                    case INT:
                        addCommand(result, new Command(DEF, name, null, "int", idNode.getLine()));
                        break;
                    case DOUBLE:
                        addCommand(result, new Command(DEF, name, null, "double", idNode.getLine()));
                        break;
                    case ARRAY_INT:
                        addCommand(result, new Command(DEF, name, Integer.toString(length), "int", idNode.getLine()));
                        break;
                    case ARRAY_DOUBLE:
                        addCommand(result, new Command(DEF, name, Integer.toString(length), "double", idNode.getLine()));
                        break;
                }
            } else if (type == ARRAY_INT || type == ARRAY_DOUBLE) {
                throw new CompilerException(String.format("Array '%s' can't be initialized", name), initExprNode.getLine());
            } else {
                addCommands(result, compileExpressionArithmetical(initExprNode));
                addCommand(result, new Command(POP, "<", null, null, idNode.getLine()));
                switch (type) {
                    case INT:
                        addCommand(result, new Command(DEF, name, "<", "int", idNode.getLine()));
                        break;
                    case DOUBLE:
                        addCommand(result, new Command(DEF, name, "<", "double", idNode.getLine()));
                        break;
                    default:
                        //Unreachable area
                        break;
                }
            }
        }
        return result;
    }

    private List<Command> compileRead(TreeNode readNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode varNode = readNode.getChild(0);
        String name = varNode.getChild(0).getValue();
        TreeNode indexExprNode = varNode.getChild(1);
        if (indexExprNode == null) {
            if (!varTable.existAll(name))
                throw new CompilerException(String.format("Unknown variable '%s'", name), varNode.getLine());
            if (varTable.get(name) == ARRAY_DOUBLE || varTable.get(name) == ARRAY_INT)
                throw new CompilerException(String.format("Array '%s' need an index", name), varNode.getLine());
            addCommand(result, new Command(SC, "<", null, null, varNode.getLine()));
            addCommand(result, new Command(MOV, name, null, "<", readNode.getLine()));
        } else {
            if (!varTable.existAll(name))
                throw new CompilerException(String.format("Unknown variable '%s'", name), varNode.getLine());
            if (varTable.get(name) == DOUBLE || varTable.get(name) == INT)
                throw new CompilerException(String.format("Variable '%s' do not need an index", name), varNode.getLine());
            addCommands(result, compileExpressionArithmetical(indexExprNode));
            addCommand(result, new Command(POP, ">", null, null, indexExprNode.getLine()));
            addCommand(result, new Command(SC, "<", null, null, varNode.getLine()));
            addCommand(result, new Command(MOV, name, ">", "<", readNode.getLine()));
        }
        return result;
    }

    private List<Command> compileWrite(TreeNode writeNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode exprNode = writeNode.getChild(0);
        addCommands(result, compileExpressionArithmetical(exprNode));
        addCommand(result, new Command(POP, "<", null, null, exprNode.getLine()));
        addCommand(result, new Command(PR, "<", null, null, writeNode.getLine()));
        return result;
    }

    private List<Command> compileExpressionLogical(TreeNode exprNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode leftNode = exprNode.getChild(0);
        TreeNode opNode = exprNode.getChild(1);
        TreeNode rightNode = exprNode.getChild(2);
        if (opNode == null) {
            if (leftNode.getType() == V_BOOL) {
                if (leftNode.getValue().equals("true"))
                    addCommand(result, new Command(MOV, "<", "1", "int", leftNode.getLine()));
                else
                    addCommand(result, new Command(MOV, "<", "0", "int", leftNode.getLine()));
            } else {
                addCommands(result, compileExpressionArithmetical(leftNode));
                addCommand(result, new Command(POP, "<", null, null, leftNode.getLine()));
            }
            addCommand(result, new Command(MOV, ">", "0", "int", leftNode.getLine()));
            addCommand(result, new Command(CMP, "<", ">", null, leftNode.getLine()));
            Command jmpToEnd = new Command(JE, null, null, null, leftNode.getLine());
            addCommand(result, jmpToEnd);
            jmpToEnd.setAddress(result.size());
        } else {
            addCommands(result, compileExpressionArithmetical(leftNode));
            addCommands(result, compileExpressionArithmetical(rightNode));
            addCommand(result, new Command(POP, ">", null, null, rightNode.getLine()));
            addCommand(result, new Command(POP, "<", null, null, leftNode.getLine()));
            addCommand(result, new Command(CMP, "<", ">", null, exprNode.getLine()));
            Command jmpToEnd = null;
            switch (opNode.getType()) {
                case OP_EQUAL:
                    jmpToEnd = new Command(JNE, null, null, null, exprNode.getLine());
                    break;
                case OP_UNEQUAL:
                    jmpToEnd = new Command(JE, null, null, null, exprNode.getLine());
                    break;
                case OP_GT:
                    jmpToEnd = new Command(JLE, null, null, null, exprNode.getLine());
                    break;
                case OP_GE:
                    jmpToEnd = new Command(JL, null, null, null, exprNode.getLine());
                    break;
                case OP_LT:
                    jmpToEnd = new Command(JGE, null, null, null, exprNode.getLine());
                    break;
                case OP_LE:
                    jmpToEnd = new Command(JG, null, null, null, exprNode.getLine());
                    break;
                default:
                    //Unreachable area
                    break;
            }
            addCommand(result, jmpToEnd);
            jmpToEnd.setAddress(result.size());
        }
        return result;
    }

    private List<Command> compileExpressionArithmetical(TreeNode exprNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        if (exprNode.getType() == TERM)
            return compileTerm(exprNode);
        if (exprNode.getType() == FACTOR)
            return compileFactor(exprNode);
        if (exprNode.getType() == V_INT) {
            addCommand(result, new Command(PUSH, null, exprNode.getValue(), "int", exprNode.getLine()));
        } else if (exprNode.getType() == V_DOUBLE) {
            addCommand(result, new Command(PUSH, null, exprNode.getValue(), "double", exprNode.getLine()));
        } else if (exprNode.getType() == VARIABLE) {
            String name = exprNode.getChild(0).getValue();
            TreeNode indexExprNode = exprNode.getChild(1);
            if (indexExprNode == null) {
                if (!varTable.existAll(name))
                    throw new CompilerException(String.format("Unknown variable '%s'", name), exprNode.getLine());
                if (varTable.get(name) == ARRAY_DOUBLE || varTable.get(name) == ARRAY_INT)
                    throw new CompilerException(String.format("Array '%s' need an index", name), exprNode.getLine());
                addCommand(result, new Command(MOV, "<", name, null, exprNode.getLine()));
            } else {
                if (!varTable.existAll(name))
                    throw new CompilerException(String.format("Unknown variable '%s'", name), exprNode.getLine());
                if (varTable.get(name) == DOUBLE || varTable.get(name) == INT)
                    throw new CompilerException(String.format("Variable '%s' do not need an index", name), exprNode.getLine());
                addCommands(result, compileExpressionArithmetical(indexExprNode));
                addCommand(result, new Command(POP, ">", null, null, indexExprNode.getLine()));
                addCommand(result, new Command(MOV, "<", name, ">", exprNode.getLine()));
            }
            addCommand(result, new Command(PUSH, "<", null, null, exprNode.getLine()));
        } else {
            TreeNode leftNode = exprNode.getChild(0);
            TreeNode opNode = exprNode.getChild(1);
            TreeNode rightNode = exprNode.getChild(2);
            if (rightNode.getType() == EXP_ARITHMETICAL) {
                exprNode.replaceChild(2, rightNode.getChild(0));
                rightNode.replaceChild(0, exprNode);
                return compileExpressionArithmetical(rightNode);
            }
            addCommands(result, compileExpressionArithmetical(leftNode));
            addCommands(result, compileExpressionArithmetical(rightNode));
            switch (opNode.getType()) {
                case OP_PLUS:
                    addCommand(result, new Command(ADD, null, null, null, opNode.getLine()));
                    break;
                case OP_MINUS:
                    addCommand(result, new Command(SUB, null, null, null, opNode.getLine()));
                    break;
                default:
                    //Unreachable area
                    break;
            }
        }
        return result;
    }

    private List<Command> compileTerm(TreeNode termNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode leftNode = termNode.getChild(0);
        TreeNode opNode = termNode.getChild(1);
        TreeNode rightNode = termNode.getChild(2);
        if (rightNode.getType() == TERM) {
            termNode.replaceChild(2, rightNode.getChild(0));
            rightNode.replaceChild(0, termNode);
            return compileTerm(rightNode);
        }
        if (opNode.getType() == OP_DIVIDE) {
            if (rightNode.getType() == V_INT) {
                if (Integer.valueOf(rightNode.getValue()) == 0)
                    throw new CompilerException("Divide by 0", rightNode.getLine());
            } else if (rightNode.getType() == V_DOUBLE) {
                if (Double.valueOf(rightNode.getValue()) == 0)
                    throw new CompilerException("Divide by 0", rightNode.getLine());
            }
        }
        addCommands(result, compileExpressionArithmetical(leftNode));
        addCommands(result, compileExpressionArithmetical(rightNode));
        switch (opNode.getType()) {
            case OP_MULTIPLY:
                addCommand(result, new Command(MUL, null, null, null, opNode.getLine()));
                break;
            case OP_DIVIDE:
                addCommand(result, new Command(DIV, null, null, null, opNode.getLine()));
                break;
            case OP_MOD:
                addCommand(result, new Command(MOD, null, null, null, opNode.getLine()));
                break;
            default:
                //Unreachable area
                break;
        }
        return result;
    }

    private List<Command> compileFactor(TreeNode factorNode) throws CompilerException {
        List<Command> result = new ArrayList<>();
        TreeNode opNode = factorNode.getChild(0);
        TreeNode exprNode = factorNode.getChild(1);
        switch (opNode.getType()) {
            case OP_PLUS:
                addCommands(result, compileExpressionArithmetical(exprNode));
                break;
            case OP_MINUS:
                addCommands(result, compileExpressionArithmetical(exprNode));
                addCommand(result, new Command(PUSH, null, "-1", "int", factorNode.getLine()));
                addCommand(result, new Command(MUL, null, null, null, factorNode.getLine()));
                break;
            default:
                //Unreachable area
                break;
        }
        return result;
    }

    private void addCommand(List<Command> oldCommands, Command newCommand) {
        oldCommands.add(newCommand);
    }

    private void addCommand(Command newCommand) {
        addCommand(result, newCommand);
    }

    private void addCommands(List<Command> oldCommands, List<Command> newCommands) {
        int length = oldCommands.size();
        for (Command command : newCommands) {
            if (command.getType().isJump())
                command.addressPlus(length);
        }
        oldCommands.addAll(newCommands);
    }

    private void addCommands(List<Command> newCommands) {
        addCommands(result, newCommands);
    }
}
