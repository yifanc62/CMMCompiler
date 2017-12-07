package Semantic;

import Syntactic.TreeNode;

import java.util.ArrayList;
import java.util.List;

import static Semantic.CommandType.IN;
import static Semantic.CommandType.OUT;

public class Compiler {
    private TreeNode programNode;
    private List<Command> result;

    public Compiler(TreeNode programNode) {
        this.programNode = programNode;
        result = new ArrayList<>();
    }

    public List<Command> compile() {
        result.clear();
        for (TreeNode node : programNode.getChildren()) {
            switch (node.getType()) {
                case STMT_BLOCK:
                    addCommand(new Command(IN, null, null, null, node.getLine()));
                    addCommands(compileBlock(node));
                    addCommand(new Command(OUT, null, null, null, -1));
                    break;
                case STMT_IF:
                    //TODO
            }
        }
        return result;
    }

    private List<Command> compileBlock(TreeNode blockNode) {

    }

    private List<Command> compileIf(TreeNode ifNode) {

    }

    private List<Command> compileWhile(TreeNode whileNode) {

    }

    private List<Command> compileFor(TreeNode forNode) {

    }

    private List<Command> compileAssign(TreeNode assignNode) {

    }

    private List<Command> compileDeclare(TreeNode declareNode) {

    }

    private List<Command> compileRead(TreeNode readNode) {

    }

    private List<Command> compileWrite(TreeNode writeNode) {

    }

    private List<Command> compileVariable(TreeNode varNode) {

    }

    private List<Command> compileExpressionLogical(TreeNode exprNode) {

    }

    private List<Command> compileExpressionArithmetical(TreeNode exprNode) {

    }

    private void addCommand(List<Command> oldCommands, Command newCommand) {
        if (newCommand.getType().isJump())
            newCommand.setArg0(Integer.toString(Integer.valueOf(newCommand.getArg0()) + oldCommands.size()));
        oldCommands.add(newCommand);
    }

    private void addCommand(Command newCommand) {
        addCommand(result, newCommand);
    }

    private void addCommands(List<Command> oldCommands, List<Command> newCommands) {
        int length = oldCommands.size();
        for (Command command : newCommands) {
            if (command.getType().isJump())
                command.setArg0(Integer.toString(Integer.valueOf(command.getArg0()) + length));
        }
        oldCommands.addAll(newCommands);
    }

    private void addCommands(List<Command> newCommands) {
        addCommands(result, newCommands);
    }
}
