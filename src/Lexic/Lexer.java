package Lexic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Lexer {
    private InputStream input;
    private TokenStream result;
    private int currentLine;
    private int currentPosition;
    private int currentTokenPosition;
    private int currentIndex;
    private int currentTokenIndex;
    private LexerStatus currentStatus;
    private String currentValue;
    private boolean rollback;

    public Lexer(InputStream input) {
        this.input = input;
    }

    private static boolean isNum(char input) {
        return input >= '0' && input <= '9';
    }

    private static boolean isAlphabet(char input) {
        return (input >= 'A' && input <= 'Z') || (input >= 'a' && input <= 'z');
    }

    private static boolean isHexChar(char input) {
        return isNum(input) || (input >= 'A' && input <= 'F') || (input >= 'a' && input <= 'f');
    }

    private static boolean isIdentifier(char input) {
        return isNum(input) || isAlphabet(input) || input == '_';
    }

    private static boolean isSkippable(char input) {
        return input == ' ' || input == '\t' || input == '\r' || input == '\n';
    }

    private void init() {
        result = new TokenStream();
        currentLine = 1;
        currentPosition = 1;
        currentTokenPosition = 1;
        currentIndex = 0;
        currentTokenIndex = 0;
        currentStatus = LexerStatus.START;
        currentValue = "";
        rollback = false;
    }

    public TokenStream getTokens() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        init();
        int currentInteger;
        char currentChar;
        while ((currentInteger = reader.read()) != -1) {
            currentChar = (char) currentInteger;
            rollback = true;
            while (rollback) {
                rollback = false;
                next(currentChar);
            }
            if (currentChar == '\n') {
                currentLine++;
                currentPosition = 1;
            } else {
                currentPosition++;
            }
            currentIndex++;
        }
        last();
        return result;
    }

    private void next(char input) {
        switch (currentStatus) {
            case START:
                switch (input) {
                    case 'i':
                        setStatusAppendingValue(LexerStatus.K_I, input, currentPosition, currentIndex);
                        break;
                    case 'e':
                        setStatusAppendingValue(LexerStatus.K_E, input, currentPosition, currentIndex);
                        break;
                    case 'w':
                        setStatusAppendingValue(LexerStatus.K_W, input, currentPosition, currentIndex);
                        break;
                    case 'b':
                        setStatusAppendingValue(LexerStatus.K_B, input, currentPosition, currentIndex);
                        break;
                    case 'r':
                        setStatusAppendingValue(LexerStatus.K_R, input, currentPosition, currentIndex);
                        break;
                    case 'd':
                        setStatusAppendingValue(LexerStatus.K_D, input, currentPosition, currentIndex);
                        break;
                    case 't':
                        setStatusAppendingValue(LexerStatus.K_T, input, currentPosition, currentIndex);
                        break;
                    case 'f':
                        setStatusAppendingValue(LexerStatus.K_F, input, currentPosition, currentIndex);
                        break;
                    case '=':
                        setStatusAppendingValue(LexerStatus.S_ASSIGN, input, currentPosition, currentIndex);
                        break;
                    case '/':
                        setStatusAppendingValue(LexerStatus.S_SLASH, input, currentPosition, currentIndex);
                        break;
                    case '>':
                        setStatusAppendingValue(LexerStatus.S_G, input, currentPosition, currentIndex);
                        break;
                    case '<':
                        setStatusAppendingValue(LexerStatus.S_L, input, currentPosition, currentIndex);
                        break;
                    case '!':
                        setStatusAppendingValue(LexerStatus.S_EXCLAMATION, input, currentPosition, currentIndex);
                        break;
                    case '0':
                        setStatusAppendingValue(LexerStatus.V_ZERO, input, currentPosition, currentIndex);
                        break;
                    case '+':
                        addTokenAppendingValue(TokenType.S_PLUS, input, currentPosition, currentIndex);
                        break;
                    case '-':
                        addTokenAppendingValue(TokenType.S_MINUS, input, currentPosition, currentIndex);
                        break;
                    case '*':
                        addTokenAppendingValue(TokenType.S_MULTIPLY, input, currentPosition, currentIndex);
                        break;
                    case '%':
                        addTokenAppendingValue(TokenType.S_MOD, input, currentPosition, currentIndex);
                        break;
                    case '(':
                        addTokenAppendingValue(TokenType.S_PARENTHESIS_L, input, currentPosition, currentIndex);
                        break;
                    case ')':
                        addTokenAppendingValue(TokenType.S_PARENTHESIS_R, input, currentPosition, currentIndex);
                        break;
                    case '[':
                        addTokenAppendingValue(TokenType.S_BRACKET_L, input, currentPosition, currentIndex);
                        break;
                    case ']':
                        addTokenAppendingValue(TokenType.S_BRACKET_R, input, currentPosition, currentIndex);
                        break;
                    case '{':
                        addTokenAppendingValue(TokenType.S_BRACE_L, input, currentPosition, currentIndex);
                        break;
                    case '}':
                        addTokenAppendingValue(TokenType.S_BRACE_R, input, currentPosition, currentIndex);
                        break;
                    case ',':
                        addTokenAppendingValue(TokenType.S_COMMA, input, currentPosition, currentIndex);
                        break;
                    case ';':
                        addTokenAppendingValue(TokenType.S_SEMICOLON, input, currentPosition, currentIndex);
                        break;
                    default:
                        if (isSkippable(input)) {
                            break;
                        } else if (isNum(input)) {
                            setStatusAppendingValue(LexerStatus.V_INT, input, currentPosition, currentIndex);
                        } else if (isIdentifier(input)) {
                            setStatusAppendingValue(LexerStatus.V_VAR, input, currentPosition, currentIndex);
                        } else {
                            addTokenAppendingValue(TokenType.E_UNRECOGNIZED, input, currentPosition, currentIndex);
                        }
                        break;
                }
                break;
            case K_I:
                if (input == 'n') {
                    setStatusAppendingValue(LexerStatus.K_IN, input);
                } else if (input == 'f') {
                    setStatusAppendingValue(LexerStatus.K_IF, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_IN:
                if (input == 't') {
                    setStatusAppendingValue(LexerStatus.K_INT, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_INT:
                if (isSkippable(input)) {
                    addToken(TokenType.K_TYPE_INT);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_TYPE_INT);
                }
                break;
            case K_IF:
                if (isSkippable(input)) {
                    addToken(TokenType.K_STMT_IF);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_STMT_IF);
                }
                break;
            case K_E:
                if (input == 'l') {
                    setStatusAppendingValue(LexerStatus.K_EL, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_EL:
                if (input == 's') {
                    setStatusAppendingValue(LexerStatus.K_ELS, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_ELS:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_ELSE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_ELSE:
                if (isSkippable(input)) {
                    addToken(TokenType.K_STMT_ELSE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_STMT_ELSE);
                }
                break;
            case K_W:
                if (input == 'h') {
                    setStatusAppendingValue(LexerStatus.K_WH, input);
                } else if (input == 'r') {
                    setStatusAppendingValue(LexerStatus.K_WR, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WH:
                if (input == 'i') {
                    setStatusAppendingValue(LexerStatus.K_WHI, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WHI:
                if (input == 'l') {
                    setStatusAppendingValue(LexerStatus.K_WHIL, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WHIL:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_WHILE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WHILE:
                if (isSkippable(input)) {
                    addToken(TokenType.K_STMT_WHILE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_STMT_WHILE);
                }
                break;
            case K_WR:
                if (input == 'i') {
                    setStatusAppendingValue(LexerStatus.K_WRI, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WRI:
                if (input == 't') {
                    setStatusAppendingValue(LexerStatus.K_WRIT, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WRIT:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_WRITE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_WRITE:
                if (isSkippable(input)) {
                    addToken(TokenType.K_IO_WRITE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_IO_WRITE);
                }
                break;
            case K_B:
                if (input == 'r') {
                    setStatusAppendingValue(LexerStatus.K_BR, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_BR:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_BRE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_BRE:
                if (input == 'a') {
                    setStatusAppendingValue(LexerStatus.K_BREA, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_BREA:
                if (input == 'k') {
                    setStatusAppendingValue(LexerStatus.K_BREAK, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_BREAK:
                if (isSkippable(input)) {
                    addToken(TokenType.K_STMT_BREAK);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_STMT_BREAK);
                }
                break;
            case K_R:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_RE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_RE:
                if (input == 'a') {
                    setStatusAppendingValue(LexerStatus.K_REA, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_REA:
                if (input == 'd') {
                    setStatusAppendingValue(LexerStatus.K_READ, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_READ:
                if (isSkippable(input)) {
                    addToken(TokenType.K_IO_READ);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_IO_READ);
                }
                break;
            case K_D:
                if (input == 'o') {
                    setStatusAppendingValue(LexerStatus.K_DO, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_DO:
                if (input == 'u') {
                    setStatusAppendingValue(LexerStatus.K_DOU, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_DOU:
                if (input == 'b') {
                    setStatusAppendingValue(LexerStatus.K_DOUB, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_DOUB:
                if (input == 'l') {
                    setStatusAppendingValue(LexerStatus.K_DOUBL, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_DOUBL:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_DOUBLE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_DOUBLE:
                if (isSkippable(input)) {
                    addToken(TokenType.K_TYPE_DOUBLE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_TYPE_DOUBLE);
                }
                break;
            case K_T:
                if (input == 'r') {
                    setStatusAppendingValue(LexerStatus.K_TR, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_TR:
                if (input == 'u') {
                    setStatusAppendingValue(LexerStatus.K_TRU, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_TRU:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_TRUE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_TRUE:
                if (isSkippable(input)) {
                    addToken(TokenType.V_BOOL_TRUE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.V_BOOL_TRUE);
                }
                break;
            case K_F:
                if (input == 'a') {
                    setStatusAppendingValue(LexerStatus.K_FA, input);
                } else if (input == 'o') {
                    setStatusAppendingValue(LexerStatus.K_FO, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_FA:
                if (input == 'l') {
                    setStatusAppendingValue(LexerStatus.K_FAL, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_FAL:
                if (input == 's') {
                    setStatusAppendingValue(LexerStatus.K_FALS, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_FALS:
                if (input == 'e') {
                    setStatusAppendingValue(LexerStatus.K_FALSE, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_FALSE:
                if (isSkippable(input)) {
                    addToken(TokenType.V_BOOL_FALSE);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.V_BOOL_FALSE);
                }
                break;
            case K_FO:
                if (input == 'r') {
                    setStatusAppendingValue(LexerStatus.K_FOR, input);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case K_FOR:
                if (isSkippable(input)) {
                    addToken(TokenType.K_STMT_FOR);
                } else if (isIdentifier(input)) {
                    setStatusAppendingValue(LexerStatus.V_VAR, input);
                } else {
                    addTokenRollback(TokenType.K_STMT_FOR);
                }
                break;
            case S_SLASH:
                if (input == '/') {
                    setStatusAppendingValue(LexerStatus.C_L, input);
                } else if (input == '*') {
                    setStatusAppendingValue(LexerStatus.C_B, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.S_DIVIDE);
                } else {
                    addTokenRollback(TokenType.S_DIVIDE);
                }
                break;
            case S_G:
                if (input == '=') {
                    addTokenAppendingValue(TokenType.S_GE, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.S_GT);
                } else {
                    addTokenRollback(TokenType.S_GT);
                }
                break;
            case S_L:
                if (input == '=') {
                    addTokenAppendingValue(TokenType.S_LE, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.S_LT);
                } else {
                    addTokenRollback(TokenType.S_LT);
                }
                break;
            case S_EXCLAMATION:
                if (input == '=') {
                    addTokenAppendingValue(TokenType.S_UNEQUAL, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.E_UNRECOGNIZED);
                } else {
                    addTokenRollback(TokenType.E_UNRECOGNIZED);
                }
                break;
            case S_ASSIGN:
                if (input == '=') {
                    addTokenAppendingValue(TokenType.S_EQUAL, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.S_ASSIGN);
                } else {
                    addTokenRollback(TokenType.S_ASSIGN);
                }
                break;
            case V_INT:
                if (isNum(input)) {
                    appendingValue(input);
                } else if (input == '.') {
                    setStatusAppendingValue(LexerStatus.V_DOUBLE, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_INT);
                } else {
                    addTokenRollback(TokenType.V_INT);
                }
                break;
            case V_HEX:
                if (isHexChar(input)) {
                    appendingValue(input);
                } else if (isSkippable(input)) {
                    addHexToken(TokenType.V_INT);
                } else {
                    addHexTokenRollback(TokenType.V_INT);
                }
                break;
            case V_DOUBLE:
                if (isNum(input)) {
                    appendingValue(input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_DOUBLE);
                } else {
                    addTokenRollback(TokenType.V_DOUBLE);
                }
                break;
            case V_ZERO:
                if (input == 'x' || input == 'X') {
                    setStatusResetValue(LexerStatus.V_HEX);
                } else if (input == '.') {
                    setStatusAppendingValue(LexerStatus.V_DOUBLE, input);
                } else if (isNum(input)) {
                    setStatusAppendingValue(LexerStatus.V_INT, input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_INT);
                } else {
                    addTokenRollback(TokenType.V_INT);
                }
                break;
            case V_VAR:
                if (isIdentifier(input)) {
                    appendingValue(input);
                } else if (isSkippable(input)) {
                    addToken(TokenType.V_VARIABLE);
                } else {
                    addTokenRollback(TokenType.V_VARIABLE);
                }
                break;
            case C_L:
                if (input == '\n') {
                    addTokenAppendingValue(TokenType.C, input);
                } else {
                    appendingValue(input);
                }
                break;
            case C_B:
                if (input == '*') {
                    setStatusAppendingValue(LexerStatus.C_END, input);
                } else {
                    appendingValue(input);
                }
                break;
            case C_END:
                if (input == '/') {
                    addTokenAppendingValue(TokenType.C, input);
                } else {
                    appendingValue(input);
                }
                break;
        }
    }

    private void last() {
        switch (currentStatus) {
            case START:
                break;
            case K_I:
            case K_IN:
            case K_E:
            case K_EL:
            case K_ELS:
            case K_W:
            case K_WH:
            case K_WHI:
            case K_WHIL:
            case K_WR:
            case K_WRI:
            case K_WRIT:
            case K_B:
            case K_BR:
            case K_BRE:
            case K_BREA:
            case K_R:
            case K_RE:
            case K_REA:
            case K_D:
            case K_DO:
            case K_DOU:
            case K_DOUB:
            case K_DOUBL:
            case K_T:
            case K_TR:
            case K_TRU:
            case K_F:
            case K_FA:
            case K_FAL:
            case K_FALS:
            case K_FO:
                addToken(TokenType.V_VARIABLE);
                break;
            case K_INT:
                addToken(TokenType.K_TYPE_INT);
                break;
            case K_IF:
                addToken(TokenType.K_STMT_IF);
                break;
            case K_ELSE:
                addToken(TokenType.K_STMT_ELSE);
                break;
            case K_WHILE:
                addToken(TokenType.K_STMT_WHILE);
                break;
            case K_WRITE:
                addToken(TokenType.K_IO_WRITE);
                break;
            case K_BREAK:
                addToken(TokenType.K_STMT_BREAK);
                break;
            case K_READ:
                addToken(TokenType.K_IO_READ);
                break;
            case K_DOUBLE:
                addToken(TokenType.K_TYPE_DOUBLE);
                break;
            case K_TRUE:
                addToken(TokenType.V_BOOL_TRUE);
                break;
            case K_FALSE:
                addToken(TokenType.V_BOOL_FALSE);
                break;
            case K_FOR:
                addToken(TokenType.K_STMT_FOR);
                break;
            case S_SLASH:
                addToken(TokenType.S_DIVIDE);
                break;
            case S_L:
                addToken(TokenType.S_LT);
                break;
            case S_G:
                addToken(TokenType.S_GT);
                break;
            case S_EXCLAMATION:
                addToken(TokenType.E_UNRECOGNIZED);
                break;
            case S_ASSIGN:
                addToken(TokenType.S_ASSIGN);
                break;
            case V_INT:
                addToken(TokenType.V_INT);
                break;
            case V_HEX:
                addHexToken(TokenType.V_INT);
                break;
            case V_DOUBLE:
                addToken(TokenType.V_DOUBLE);
                break;
            case V_ZERO:
                addToken(TokenType.V_INT);
                break;
            case V_VAR:
                addToken(TokenType.V_VARIABLE);
                break;
            case C_L:
                addToken(TokenType.C);
                break;
            case C_B:
                addToken(TokenType.C);
                addToken(TokenType.E_COMMENT);
                break;
            case C_END:
                addToken(TokenType.C);
                addToken(TokenType.E_COMMENT);
                break;
        }
    }

    private void resetValue() {
        currentValue = "";
    }

    private void setStatus(LexerStatus newStatus) {
        currentStatus = newStatus;
    }

    private void appendingValue(char c) {
        currentValue += c;
    }

    private void setStatusAppendingValue(LexerStatus newStatus, char c) {
        currentStatus = newStatus;
        currentValue += c;
    }

    private void setStatusAppendingValue(LexerStatus newStatus, char c, int newPosition, int newIndex) {
        currentStatus = newStatus;
        currentValue += c;
        currentTokenPosition = newPosition;
        currentTokenIndex = newIndex;
    }

    private void setStatusResetValue(LexerStatus newStatus) {
        currentStatus = newStatus;
        resetValue();
    }

    private void addToken(TokenType type) {
        setStatus(LexerStatus.START);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue));
        resetValue();
    }

    private void addTokenAppendingValue(TokenType type, char input) {
        setStatusAppendingValue(LexerStatus.START, input);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue));
        resetValue();
    }

    private void addTokenAppendingValue(TokenType type, char input, int newPosition, int newIndex) {
        setStatusAppendingValue(LexerStatus.START, input, newPosition, newIndex);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue));
        resetValue();
    }

    private void addTokenRollback(TokenType type) {
        setStatus(LexerStatus.START);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue));
        resetValue();
        rollback = true;
    }

    private void addHexToken(TokenType type) {
        setStatus(LexerStatus.START);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue).setValueWithoutLength(Integer.valueOf(currentValue, 16).toString()));
        resetValue();
    }

    private void addHexTokenRollback(TokenType type) {
        setStatus(LexerStatus.START);
        result.add(new Token(currentLine, currentTokenPosition, currentTokenIndex, type, currentValue).setValueWithoutLength(Integer.valueOf(currentValue, 16).toString()));
        resetValue();
        rollback = true;
    }
}