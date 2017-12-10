package Syntactic;

public class UnexpectedEOFException extends Exception {
    public UnexpectedEOFException() {
        super("Unexpected end of file.");
    }
}
