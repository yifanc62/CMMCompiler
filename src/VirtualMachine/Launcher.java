package VirtualMachine;

import Semantic.Command;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static VirtualMachine.VariableType.INT;

public class Launcher {
    private List<Command> cmdList;
    private VariableTable varTable;
    private Stack<Variable> varStack;
    private Variable eax;
    private Variable ebx;
    private int cmp;
    private int p;

    public Launcher(InputStream input) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        List<Command> cmdList = new ArrayList<>();
        try {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                cmdList.add(Command.decode(currentLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.cmdList = cmdList;
        this.varTable = new VariableTable();
        this.varStack = new Stack<>();
        this.eax = new Variable("0", INT);
        this.ebx = new Variable("0", INT);
        this.cmp = 0;
        this.p = 0;
    }

    public Launcher(List<Command> cmdList) {
        this.cmdList = cmdList;
        this.varTable = new VariableTable();
        this.varStack = new Stack<>();
        this.eax = new Variable("0", INT);
        this.ebx = new Variable("0", INT);
        this.cmp = 0;
        this.p = 0;
    }

    public void launch(OutputStream outputStream) {
        PrintStream out = new PrintStream(outputStream);
        //添加运行时代码
    }
}
