package VirtualMachine;

import Semantic.Command;
import Semantic.CommandType;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SimpleVM {

    private int p = 0;
    private Map<String, Double> datas = new HashMap<>();

    public static void main(String[] args) {
        Vector<Command> commands = new Vector<>();
        Command c1 = new Command(CommandType.ADD, "addsum", "2", "1", 0);
        Command c2 = new Command(CommandType.PR, "6", null, "Integer", 0);
        commands.add(c1);
        commands.add(c2);
        SimpleVM vm = new SimpleVM();
        vm.execute(commands);
    }

    private void execute(Vector<Command> commands) {
        for (int i = 0; i != commands.size(); ++i) {
            Command currentCMD = commands.get(p);
            ++p;
            switch (currentCMD.getType()) {
                case JMP:
                    p = Integer.valueOf(currentCMD.getArg0());
                    break;
                case ADD: {
                    double sum = Double.valueOf(currentCMD.getArg1()) + Double.valueOf(currentCMD.getArg2());
                    datas.put(currentCMD.getArg0(), sum);
                }
                break;
                case SUB: {
                    double sum = Double.valueOf(currentCMD.getArg1()) - Double.valueOf(currentCMD.getArg2());
                    datas.put(currentCMD.getArg0(), sum);
                }
                break;
                case MUL: {
                    double sum = Double.valueOf(currentCMD.getArg1()) * Double.valueOf(currentCMD.getArg2());
                    datas.put(currentCMD.getArg0(), sum);
                }
                break;
                case DIV: {
                    double sum = Double.valueOf(currentCMD.getArg1()) / Double.valueOf(currentCMD.getArg2());
                    datas.put(currentCMD.getArg0(), sum);
                }
                case PR:
                    if (currentCMD.getArg2().equals("Integer")) {
                        System.out.println(Integer.valueOf(currentCMD.getArg0()));
                    } else if (currentCMD.getArg2().equals("Double")) {
                        System.out.println(Double.valueOf(currentCMD.getArg0()));
                    } else {
                        System.out.println(currentCMD.getArg0());
                    }
                default:
                    break;
            }
        }
    }
}
