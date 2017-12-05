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

    public Command(String encodedStr) {

    }

    private String encode() {

    }

    private static Command decode(String encodedStr) {

    }
}
