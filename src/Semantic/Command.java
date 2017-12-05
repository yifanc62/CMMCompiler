package Semantic;

public class Command {
    private CommandType type;
    private String arg0;
    private String arg1;
    private String arg2;

    public Command(CommandType type, String arg0, String arg1, String arg2) {
        this.type = type;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    private String encode() {
        return String.format("(%s,%s,%s,%s)", type.toString(), arg0 == null ? " " : arg0, arg1 == null ? " " : arg1, arg2 == null ? " " : arg2);
    }

    private static Command decode(String encodedStr) {
        try {
            encodedStr = encodedStr.trim();
            if (!(encodedStr.startsWith("(") && encodedStr.endsWith(")")))
                throw new Exception();
            encodedStr = encodedStr.substring(1, encodedStr.length() - 1);
            String[] results = encodedStr.split(",");
            CommandType type = CommandType.valueOf(results[0].trim());
            String arg0 = results[1].trim();
            String arg1 = results[2].trim();
            String arg2 = results[3].trim();
            return new Command(type, arg0.equals("") ? null : arg0, arg1.equals("") ? null : arg1, arg2.equals("") ? null : arg2);
        } catch (Exception e) {
            return null;
        }
    }
}
