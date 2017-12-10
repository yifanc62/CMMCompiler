package com.cirnoteam.cmm.semantic;

import java.util.ArrayList;
import java.util.List;

import static com.cirnoteam.cmm.semantic.CommandType.*;

public class Optimizer {
    public static List<Command> optimize(List<Command> source) {
        List<Command> result = new ArrayList<>(source);
        int p = 0;
        while (p < result.size() - 1) {
            Command current = result.get(p);
            Command next = result.get(p + 1);
            CommandType currentType = current.getType();
            CommandType nextType = next.getType();
            if (currentType == PUSH && nextType == POP) {
                if (current.getArg0() == null) {
                    String pushArg1 = current.getArg1();
                    String pushArg2 = current.getArg2();
                    String popArg0 = next.getArg0();
                    result.remove(p);
                    result.remove(p);
                    result.add(p, new Command(MOV, popArg0, pushArg1, pushArg2, current.getLine()));
                    setOffset(result, p, -1);
                } else if (current.getArg0().equals(next.getArg0())) {
                    result.remove(p);
                    result.remove(p);
                    setOffset(result, p, -2);
                } else {
                    p++;
                }
            } else if (currentType == IN && nextType == OUT) {
                result.remove(p);
                result.remove(p);
                setOffset(result, p, -2);
            } else if (currentType == JMP && nextType == OUT) {
                boolean isToggled = false;
                for (Command c : result)
                    if (c.getType().isJump() && Integer.valueOf(c.getArg0()) == p + 1) {
                        isToggled = true;
                        break;
                    }
                if (isToggled) {
                    p++;
                } else {
                    result.remove(p + 1);
                    setOffset(result, p + 1, -1);
                }
            } else if (currentType == PUSH && nextType == PUSH && p < result.size() - 2 && current.getArg0() == null && next.getArg0() == null && result.get(p + 2).getType().isCalc()) {
                if (current.getArg2().equals("double") || next.getArg2().equals("double")) {
                    double left = Double.valueOf(current.getArg1());
                    double right = Double.valueOf(next.getArg1());
                    Command c = null;
                    switch (result.get(p + 2).getType()) {
                        case ADD:
                            c = new Command(PUSH, null, Double.toString(left + right), "double", current.getLine());
                            break;
                        case SUB:
                            c = new Command(PUSH, null, Double.toString(left - right), "double", current.getLine());
                            break;
                        case MUL:
                            c = new Command(PUSH, null, Double.toString(left * right), "double", current.getLine());
                            break;
                        case DIV:
                            c = new Command(PUSH, null, Double.toString(left / right), "double", current.getLine());
                            break;
                        case MOD:
                            c = new Command(PUSH, null, Double.toString(left % right), "double", current.getLine());
                            break;
                        default:
                            //Unreachable area
                            break;
                    }
                    result.remove(p);
                    result.remove(p);
                    result.remove(p);
                    result.add(p, c);
                    setOffset(result, p, -2);
                } else {
                    int left = Integer.valueOf(current.getArg1());
                    int right = Integer.valueOf(next.getArg1());
                    Command c = null;
                    switch (result.get(p + 2).getType()) {
                        case ADD:
                            c = new Command(PUSH, null, Integer.toString(left + right), "int", current.getLine());
                            break;
                        case SUB:
                            c = new Command(PUSH, null, Integer.toString(left - right), "int", current.getLine());
                            break;
                        case MUL:
                            c = new Command(PUSH, null, Integer.toString(left * right), "int", current.getLine());
                            break;
                        case DIV:
                            c = new Command(PUSH, null, Integer.toString(left / right), "int", current.getLine());
                            break;
                        case MOD:
                            c = new Command(PUSH, null, Integer.toString(left % right), "int", current.getLine());
                            break;
                        default:
                            //Unreachable area
                            break;
                    }
                    result.remove(p);
                    result.remove(p);
                    result.remove(p);
                    result.add(p, c);
                    setOffset(result, p, -2);
                }
            } else {
                //may have more optimizer
                p++;
            }
        }
        return result;
    }

    private static void setOffset(List<Command> source, int start, int offset) {
        for (Command c : source) {
            if (c.getType().isJump()) {
                int address = Integer.valueOf(c.getArg0());
                if (address - start > (-offset))
                    c.addressPlus(offset);
                else if (address - start >= 0)
                    c.setAddress(start);
            }
        }
    }
}
