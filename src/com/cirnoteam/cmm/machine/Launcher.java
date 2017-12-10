package com.cirnoteam.cmm.machine;

import com.cirnoteam.cmm.semantic.Command;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.cirnoteam.cmm.machine.VariableType.*;

public class Launcher {
    private final double PRECISION = 1e-8;
    private List<Command> cmdList;
    private VariableTable varTable;
    private Stack<Variable> varStack;
    private Variable eax;
    private Variable ebx;
    private double cmp;
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
            System.out.println("Compiled code reading failed");
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

    public void launch(InputStream inputStream, OutputStream outputStream, OutputStream errorStream) {
        PrintStream out = new PrintStream(outputStream);
        PrintStream err = new PrintStream(errorStream);
        try {
            while (true) {
                Command current = cmdList.get(p);
                switch (current.getType()) {
                    case CMP:
                        cmp(current);
                        break;
                    case JMP:
                        jmp(current);
                        break;
                    case JE:
                        je(current);
                        break;
                    case JNE:
                        jne(current);
                        break;
                    case JG:
                        jg(current);
                        break;
                    case JGE:
                        jge(current);
                        break;
                    case JL:
                        jl(current);
                        break;
                    case JLE:
                        jle(current);
                        break;
                    case DEF:
                        def(current);
                        break;
                    case MOV:
                        mov(current);
                        break;
                    case IN:
                        in();
                        break;
                    case OUT:
                        out();
                        break;
                    case PUSH:
                        push(current);
                        break;
                    case POP:
                        pop(current);
                        break;
                    case ADD:
                        add();
                        break;
                    case SUB:
                        sub();
                        break;
                    case MUL:
                        mul();
                        break;
                    case DIV:
                        div();
                        break;
                    case MOD:
                        mod();
                        break;
                    case SC:
                        sc(current, inputStream, err);
                        break;
                    case PR:
                        pr(current, out);
                        break;
                    case EXIT:
                        return;
                }
            }
        } catch (LauncherException e) {
            err.println(e.getMessage());
        } catch (Exception e) {
            err.println(String.format("Unexpected runtime exception %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    private void cmp(Command current) {
        String left = current.getArg0();
        String right = current.getArg1();
        if (left.equals("<") && right.equals(">")) {
            if (eax.getType() == INT) {
                int eaxValue = eax.getIntValue();
                if (ebx.getType() == INT) {
                    int ebxValue = ebx.getIntValue();
                    cmp = eaxValue - ebxValue;
                } else {
                    double ebxValue = ebx.getDoubleValue();
                    cmp = eaxValue - ebxValue;
                }
            } else {
                double eaxValue = eax.getDoubleValue();
                if (ebx.getType() == INT) {
                    int ebxValue = ebx.getIntValue();
                    cmp = eaxValue - ebxValue;
                } else {
                    double ebxValue = ebx.getDoubleValue();
                    cmp = eaxValue - ebxValue;
                }
            }
        } else if (left.equals("<") && right.equals("<")) {
            cmp = 0;
        } else if (left.equals(">") && right.equals(">")) {
            cmp = 0;
        } else if (left.equals(">") && right.equals("<")) {
            if (eax.getType() == INT) {
                int eaxValue = eax.getIntValue();
                if (ebx.getType() == INT) {
                    int ebxValue = ebx.getIntValue();
                    cmp = ebxValue - eaxValue;
                } else {
                    double ebxValue = ebx.getDoubleValue();
                    cmp = ebxValue - eaxValue;
                }
            } else {
                double eaxValue = eax.getDoubleValue();
                if (ebx.getType() == INT) {
                    int ebxValue = ebx.getIntValue();
                    cmp = ebxValue - eaxValue;
                } else {
                    double ebxValue = ebx.getDoubleValue();
                    cmp = ebxValue - eaxValue;
                }
            }
        } else {
            throw new LauncherException("Internal error: CMP command invalid");
        }
        p++;
    }

    private void jmp(Command current) {
        p = Integer.valueOf(current.getArg0());
    }

    private void je(Command current) {
        if (Math.abs(cmp) <= PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void jne(Command current) {
        if (Math.abs(cmp) > PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void jg(Command current) {
        if (cmp > PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void jge(Command current) {
        if (cmp >= -PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void jl(Command current) {
        if (cmp < -PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void jle(Command current) {
        if (cmp <= PRECISION) {
            p = Integer.valueOf(current.getArg0());
        } else {
            p++;
        }
    }

    private void def(Command current) {
        //(def, name, register, type)/(def, name, length, type)/(def, name, null, type)
        String name = current.getArg0();
        if (current.getArg1() == null) {
            if (current.getArg2().equals("int"))
                varTable.put(name, "0", INT, current.getLine());
            else
                varTable.put(name, "0", DOUBLE, current.getLine());
        } else if (current.getArg1().equals("<")) {
            if (current.getArg2().equals("int")) {
                if (eax.getType() == INT)
                    varTable.put(name, eax.getValue(), INT, current.getLine());
                else
                    throw new LauncherException(String.format("Can't initialize int '%s' with double value", name), current.getLine());
            } else {
                varTable.put(name, eax.getValue(), DOUBLE, current.getLine());
            }
        } else if (current.getArg1().equals(">")) {
            if (current.getArg2().equals("int")) {
                if (ebx.getType() == INT)
                    varTable.put(name, ebx.getValue(), INT, current.getLine());
                else
                    throw new LauncherException(String.format("Can't initialize int '%s' with double value", name), current.getLine());
            } else {
                varTable.put(name, ebx.getValue(), DOUBLE, current.getLine());
            }
        } else {
            int length = Integer.valueOf(current.getArg1());
            String[] values = new String[length];
            for (int i = 0; i < length; i++) {
                values[i] = "0";
            }
            if (current.getArg2().equals("int"))
                varTable.put(name, values, ARRAY_INT, current.getLine());
            else
                varTable.put(name, values, ARRAY_DOUBLE, current.getLine());
        }
        p++;
    }

    private void mov(Command current) {
        //(mov, name, null, register)/(mov, name, register, register)/(mov, register, name, null)/(mov, register, name, register)/(mov, register, value, type)
        if (current.getArg0().equals("<")) {
            if (current.getArg2() == null) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == ARRAY_INT || var.getType() == ARRAY_DOUBLE)
                    throw new LauncherException(String.format("Array '%s' move without an index", name), current.getLine());
                eax = new Variable(var);
            } else if (current.getArg2().equals("<")) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == INT || var.getType() == DOUBLE)
                    throw new LauncherException(String.format("Variable '%s' move with a redundant index", name), current.getLine());
                if (eax.getType() != INT)
                    throw new LauncherException(String.format("Array '%s' move with an invalid index", name), current.getLine());
                int index = eax.getIntValue();
                if (index >= var.getLength())
                    throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                if (var.getType() == ARRAY_INT)
                    eax = new Variable(var.getValues()[index], INT);
                else
                    eax = new Variable(var.getValues()[index], DOUBLE);
            } else if (current.getArg2().equals(">")) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == INT || var.getType() == DOUBLE)
                    throw new LauncherException(String.format("Array '%s' move without an index", name), current.getLine());
                if (ebx.getType() != INT)
                    throw new LauncherException(String.format("Variable '%s' move with a redundant index", name), current.getLine());
                int index = ebx.getIntValue();
                if (index >= var.getLength())
                    throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                if (var.getType() == ARRAY_INT)
                    eax = new Variable(var.getValues()[index], INT);
                else
                    eax = new Variable(var.getValues()[index], DOUBLE);
            } else if (current.getArg2().equals("int")) {
                eax = new Variable(current.getArg1(), INT);
            } else {
                eax = new Variable(current.getArg1(), DOUBLE);
            }
        } else if (current.getArg0().equals(">")) {
            if (current.getArg2() == null) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == ARRAY_INT || var.getType() == ARRAY_DOUBLE)
                    throw new LauncherException(String.format("Array '%s' move without an index", name), current.getLine());
                ebx = new Variable(var);
            } else if (current.getArg2().equals("<")) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == INT || var.getType() == DOUBLE)
                    throw new LauncherException(String.format("Variable '%s' move with a redundant index", name), current.getLine());
                if (eax.getType() != INT)
                    throw new LauncherException(String.format("Array '%s' move with an invalid index", name), current.getLine());
                int index = eax.getIntValue();
                if (index >= var.getLength())
                    throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                if (var.getType() == ARRAY_INT)
                    ebx = new Variable(var.getValues()[index], INT);
                else
                    ebx = new Variable(var.getValues()[index], DOUBLE);
            } else if (current.getArg2().equals(">")) {
                String name = current.getArg1();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == INT || var.getType() == DOUBLE)
                    throw new LauncherException(String.format("Array '%s' move without an index", name), current.getLine());
                if (ebx.getType() != INT)
                    throw new LauncherException(String.format("Variable '%s' move with a redundant index", name), current.getLine());
                int index = ebx.getIntValue();
                if (index >= var.getLength())
                    throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                if (var.getType() == ARRAY_INT)
                    ebx = new Variable(var.getValues()[index], INT);
                else
                    ebx = new Variable(var.getValues()[index], DOUBLE);
            } else if (current.getArg2().equals("int")) {
                ebx = new Variable(current.getArg1(), INT);
            } else {
                ebx = new Variable(current.getArg1(), DOUBLE);
            }
        } else {
            if (current.getArg1() == null) {
                String name = current.getArg0();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == ARRAY_INT || var.getType() == ARRAY_DOUBLE)
                    throw new LauncherException(String.format("Array '%s' move without an index", name), current.getLine());
                switch (current.getArg2()) {
                    case "<":
                        if (var.getType() == INT && eax.getType() == DOUBLE)
                            throw new LauncherException(String.format("Variable '%s' can't move with double value", name), current.getLine());
                        var.assign(eax);
                        break;
                    case ">":
                        if (var.getType() == INT && ebx.getType() == DOUBLE)
                            throw new LauncherException(String.format("Variable '%s' can't move with double value", name), current.getLine());
                        var.assign(ebx);
                        break;
                    default:
                        throw new LauncherException("Internal error: MOV command invalid");
                }
            } else {
                String name = current.getArg0();
                Variable var = varTable.get(name, current.getLine());
                if (var.getType() == INT || var.getType() == DOUBLE)
                    throw new LauncherException(String.format("Variable '%s' move with a redundant index", name), current.getLine());
                switch (current.getArg1()) {
                    case "<": {
                        if (eax.getType() != INT)
                            throw new LauncherException(String.format("Array '%s' move with an invalid index", name), current.getLine());
                        int index = eax.getIntValue();
                        if (index >= var.getLength())
                            throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                        switch (current.getArg2()) {
                            case "<":
                                var.assignChild(eax.getValue(), index);
                                break;
                            case ">":
                                if (var.getType() == ARRAY_INT && ebx.getType() == DOUBLE)
                                    throw new LauncherException(String.format("Array '%s' can't move with double value", name), current.getLine());
                                var.assignChild(ebx.getValue(), index);
                                break;
                            default:
                                throw new LauncherException("Internal error: MOV command invalid");
                        }
                        break;
                    }
                    case ">": {
                        if (ebx.getType() != INT)
                            throw new LauncherException(String.format("Array '%s' move with an invalid index", name), current.getLine());
                        int index = ebx.getIntValue();
                        if (index >= var.getLength())
                            throw new LauncherException(String.format("Array '%s' out of bound with %d of %d", name, index, var.getLength()), current.getLine());
                        switch (current.getArg2()) {
                            case "<":
                                if (var.getType() == ARRAY_INT && eax.getType() == DOUBLE)
                                    throw new LauncherException(String.format("Array '%s' can't move with double value", name), current.getLine());
                                var.assignChild(eax.getValue(), index);
                                break;
                            case ">":
                                var.assignChild(ebx.getValue(), index);
                                break;
                            default:
                                throw new LauncherException("Internal error: MOV command invalid");
                        }
                        break;
                    }
                    default:
                        throw new LauncherException("Internal error: MOV command invalid");
                }
            }
        }
        p++;
    }

    private void in() {
        varTable = varTable.createChild();
        p++;
    }

    private void out() {
        varTable = varTable.returnParent();
        p++;
    }

    private void push(Command current) {
        //(push, null, value, type)/(push, register, null, null)
        if (current.getArg0() == null) {
            if (current.getArg2().equals("int"))
                varStack.push(new Variable(current.getArg1(), INT));
            else
                varStack.push(new Variable(current.getArg1(), DOUBLE));
        } else if (current.getArg0().equals("<")) {
            varStack.push(new Variable(eax));
        } else if (current.getArg0().equals(">")) {
            varStack.push(new Variable(ebx));
        } else {
            throw new LauncherException("Internal error: PUSH command invalid");
        }
        p++;
    }

    private void pop(Command current) {
        switch (current.getArg0()) {
            case "<":
                eax = new Variable(varStack.pop());
                break;
            case ">":
                ebx = new Variable(varStack.pop());
                break;
            default:
                throw new LauncherException("Internal error: POP command invalid");
        }
        p++;
    }

    private void add() {
        Variable right = varStack.pop();
        Variable left = varStack.pop();
        if (left.getType() == INT) {
            int leftValue = left.getIntValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Integer.toString(leftValue + rightValue), INT));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue + rightValue), DOUBLE));
            }
        } else {
            double leftValue = left.getDoubleValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Double.toString(leftValue + rightValue), DOUBLE));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue + rightValue), DOUBLE));
            }
        }
        p++;
    }

    private void sub() {
        Variable right = varStack.pop();
        Variable left = varStack.pop();
        if (left.getType() == INT) {
            int leftValue = left.getIntValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Integer.toString(leftValue - rightValue), INT));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue - rightValue), DOUBLE));
            }
        } else {
            double leftValue = left.getDoubleValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Double.toString(leftValue - rightValue), DOUBLE));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue - rightValue), DOUBLE));
            }
        }
        p++;
    }

    private void mul() {
        Variable right = varStack.pop();
        Variable left = varStack.pop();
        if (left.getType() == INT) {
            int leftValue = left.getIntValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Integer.toString(leftValue * rightValue), INT));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue * rightValue), DOUBLE));
            }
        } else {
            double leftValue = left.getDoubleValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Double.toString(leftValue * rightValue), DOUBLE));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue * rightValue), DOUBLE));
            }
        }
        p++;
    }

    private void div() {
        Variable right = varStack.pop();
        Variable left = varStack.pop();
        if (left.getType() == INT) {
            int leftValue = left.getIntValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Integer.toString(leftValue / rightValue), INT));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue / rightValue), DOUBLE));
            }
        } else {
            double leftValue = left.getDoubleValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Double.toString(leftValue / rightValue), DOUBLE));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue / rightValue), DOUBLE));
            }
        }
        p++;
    }

    private void mod() {
        Variable right = varStack.pop();
        Variable left = varStack.pop();
        if (left.getType() == INT) {
            int leftValue = left.getIntValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Integer.toString(leftValue % rightValue), INT));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue % rightValue), DOUBLE));
            }
        } else {
            double leftValue = left.getDoubleValue();
            if (right.getType() == INT) {
                int rightValue = right.getIntValue();
                varStack.push(new Variable(Double.toString(leftValue % rightValue), DOUBLE));
            } else {
                double rightValue = right.getDoubleValue();
                varStack.push(new Variable(Double.toString(leftValue % rightValue), DOUBLE));
            }
        }
        p++;
    }

    private void sc(Command current, InputStream in, PrintStream err) throws IOException {
        out:
        while (true) {
            StringBuilder builder = new StringBuilder();
            int i;
            while ((i = in.read()) != ((int) '\n')) {
                builder.append((char) i);
            }
            String input = builder.toString().trim();
            switch (current.getArg0()) {
                case "<":
                    try {
                        int value = Integer.valueOf(input);
                        eax = new Variable(Integer.toString(value), INT);
                        break out;
                    } catch (NumberFormatException exInt) {
                        try {
                            double value = Double.valueOf(input);
                            eax = new Variable(Double.toString(value), DOUBLE);
                            break out;
                        } catch (NumberFormatException exDouble) {
                            err.println(String.format("Invalid value '%s' and retry", input));
                        }
                    }
                    break;
                case ">":
                    try {
                        int value = Integer.valueOf(input);
                        ebx = new Variable(Integer.toString(value), INT);
                        break out;
                    } catch (NumberFormatException exInt) {
                        try {
                            double value = Double.valueOf(input);
                            ebx = new Variable(Double.toString(value), DOUBLE);
                            break out;
                        } catch (NumberFormatException exDouble) {
                            err.println(String.format("Invalid value '%s' and retry", input));
                        }
                    }
                    break;
                default:
                    throw new LauncherException("Internal error: SC command invalid");
            }
        }
        p++;
    }

    private void pr(Command current, PrintStream out) throws IOException {
        switch (current.getArg0()) {
            case "<":
                if (eax.getType() == DOUBLE && !eax.getValue().contains("."))
                    out.println(eax.getValue() + ".0");
                else
                    out.println(eax.getValue());
                break;
            case ">":
                if (ebx.getType() == DOUBLE && !ebx.getValue().contains("."))
                    out.println(ebx.getValue() + ".0");
                else
                    out.println(ebx.getValue());
                break;
            default:
                throw new LauncherException("Internal error: SC command invalid");
        }
        p++;
    }
}
