package com.cirnoteam.cmm.graphic;

import com.cirnoteam.cmm.lexic.Lexer;
import com.cirnoteam.cmm.lexic.Token;
import com.cirnoteam.cmm.lexic.TokenStream;
import com.cirnoteam.cmm.machine.Launcher;
import com.cirnoteam.cmm.semantic.Command;
import com.cirnoteam.cmm.semantic.Compiler;
import com.cirnoteam.cmm.syntactic.Parser;
import com.cirnoteam.cmm.syntactic.TreeNode;
import com.cirnoteam.cmm.syntactic.UnexpectedTokenException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import static com.cirnoteam.cmm.util.Util.*;

import java.util.List;

public class Window {
    public final RGB WHITE = new RGB(0xFF, 0xFF, 0xFF);
    public final RGB BLACK = new RGB(0, 0, 0);
    public final RGB ERROR = new RGB(0xCC, 0x00, 0x00);
    public final RGB KEYWORD = new RGB(0, 0x33, 0xFF);
    public final RGB VALUE = new RGB(0x33, 0x99, 0xCC);
    public final RGB OP = new RGB(0xFF, 66, 0x00);
    public final RGB COMMENT = new RGB(0x66, 0xFF, 0x44);
    private Shell shell;
    private Display display;
    private String text;
    private StyledText input;
    private StyledText output;
    private boolean saved;
    private String filePath;
    private Window current;
    private Stack<Character> inputStack;

    public Window() {
        text = "";
        saved = false;
        filePath = null;
        inputStack = new Stack<>();
    }

    public static void main(String[] args) {
        try {
            Window window = new Window();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void open() {
        current = this;
        display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private void createContents() {
        //窗体
        shell = new Shell(display);
        shell.setImage(null);
        shell.setSize(800, 600);
        shell.setText("CMM Compiler");
        //菜单栏
        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);
        //视图
        FormLayout layout = new FormLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        shell.setLayout(layout);
        //代码框
        input = new StyledText(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        FormData formInput = new FormData();
        formInput.top = new FormAttachment(0);
        formInput.bottom = new FormAttachment(60);
        formInput.left = new FormAttachment(0);
        formInput.right = new FormAttachment(100);
        input.setLayoutData(formInput);
        //输出标签
        Label label = new Label(shell, SWT.NONE);
        FormData formLabel = new FormData();
        formLabel.height = 15;
        formLabel.top = new FormAttachment(input, 5);
        formLabel.left = new FormAttachment(0);
        formLabel.right = new FormAttachment(100);
        label.setLayoutData(formLabel);
        label.setText("输出结果：");
        //输出框
        output = new StyledText(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        output.setEditable(false);
        FormData formOutput = new FormData();
        formOutput.top = new FormAttachment(label, 5);
        formOutput.bottom = new FormAttachment(100);
        formOutput.left = new FormAttachment(0);
        formOutput.right = new FormAttachment(100);
        output.setLayoutData(formOutput);
        outputResult("Welcome to CMM Compiler!");
        //菜单项-文件
        MenuItem menuItemFile = new MenuItem(menu, SWT.CASCADE);
        menuItemFile.setText("文件");
        Menu menuFile = new Menu(menuItemFile);
        menuItemFile.setMenu(menuFile);
        //菜单项-新建
        MenuItem menuItemNew = new MenuItem(menuFile, SWT.NONE);
        menuItemNew.setText("新建...");
        menuItemNew.setAccelerator(SWT.CTRL + 'N');
        //菜单项-打开
        MenuItem menuItemOpen = new MenuItem(menuFile, SWT.NONE);
        menuItemOpen.setText("打开...");
        menuItemOpen.setAccelerator(SWT.CTRL + 'O');
        //菜单项-保存
        MenuItem menuItemSave = new MenuItem(menuFile, SWT.NONE);
        menuItemSave.setText("保存...");
        menuItemSave.setAccelerator(SWT.CTRL + 'S');
        //菜单项-另存为
        MenuItem menuItemSaveAs = new MenuItem(menuFile, SWT.NONE);
        menuItemSaveAs.setText("另存为...");
        //菜单分隔符
        new MenuItem(menuFile, SWT.SEPARATOR);
        //菜单项-退出
        MenuItem menuItemQuit = new MenuItem(menuFile, SWT.NONE);
        menuItemQuit.setText("退出");
        menuItemQuit.setAccelerator(SWT.CTRL + 'Q');
        //菜单项-生成
        MenuItem menuItemCompile = new MenuItem(menu, SWT.CASCADE);
        menuItemCompile.setText("生成");
        Menu menuCompile = new Menu(menuItemCompile);
        menuItemCompile.setMenu(menuCompile);
        //菜单项-生成可执行文件
        MenuItem menuItemBuild = new MenuItem(menuCompile, SWT.NONE);
        menuItemBuild.setText("生成可执行文件");
        menuItemBuild.setAccelerator(SWT.CTRL + 'B');
        //菜单项-输出词法分析结果
        MenuItem menuItemLexer = new MenuItem(menuCompile, SWT.NONE);
        menuItemLexer.setText("输出词法分析结果");
        //菜单项-输出语法分析结果
        MenuItem menuItemParser = new MenuItem(menuCompile, SWT.NONE);
        menuItemParser.setText("输出语法分析结果");
        //菜单项-输出未优化的中间代码
        MenuItem menuItemCompilerFalse = new MenuItem(menuCompile, SWT.NONE);
        menuItemCompilerFalse.setText("输出未优化的中间代码");
        //菜单项-输出中间代码
        MenuItem menuItemCompiler = new MenuItem(menuCompile, SWT.NONE);
        menuItemCompiler.setText("输出中间代码");
        //菜单项-运行
        MenuItem menuItemRun = new MenuItem(menu, SWT.CASCADE);
        menuItemRun.setText("运行");
        Menu menuRun = new Menu(menuItemRun);
        menuItemRun.setMenu(menuRun);
        //菜单项-运行当前文件
        MenuItem menuItemRunNow = new MenuItem(menuRun, SWT.NONE);
        menuItemRunNow.setText("运行当前文件");
        menuItemRunNow.setAccelerator(SWT.CTRL + 'R');
        //菜单项-运行可执行文件
        MenuItem menuItemRunFile = new MenuItem(menuRun, SWT.NONE);
        menuItemRunFile.setText("运行可执行文件...");
        //菜单项-帮助
        MenuItem menuItemHelp = new MenuItem(menu, SWT.CASCADE);
        menuItemHelp.setText("帮助");
        Menu menuHelp = new Menu(menuItemHelp);
        menuItemHelp.setMenu(menuHelp);
        //菜单项-GitHub项目主页
        MenuItem menuItemHome = new MenuItem(menuHelp, SWT.NONE);
        menuItemHome.setText("GitHub项目主页");
        //菜单分隔符
        new MenuItem(menuHelp, SWT.SEPARATOR);
        //菜单项-关于
        MenuItem menuItemAbout = new MenuItem(menuHelp, SWT.NONE);
        menuItemAbout.setText("关于...");

        SelectionListener newListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saved || text.trim().equals("")) {
                    filePath = null;
                    saved = false;
                    refresh("");
                } else {
                    MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
                    dialog.setMessage("当前文件还没有保存，是否保存？");
                    int action = dialog.open();
                    if (action == SWT.YES) {
                        saveFile(false);
                    } else if (action == SWT.NO) {
                        filePath = null;
                        saved = false;
                        refresh("");
                    }
                }
            }
        };
        SelectionListener openListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saved || text.trim().equals("")) {
                    openFile();
                } else {
                    MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
                    dialog.setMessage("当前文件还没有保存，是否保存？");
                    int action = dialog.open();
                    if (action == SWT.YES) {
                        saveFile(false);
                    } else if (action == SWT.NO) {
                        openFile();
                    }
                }
            }
        };
        SelectionListener saveListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveFile(false);
            }
        };
        SelectionListener saveAsListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveFile(true);
            }
        };
        SelectionListener quitListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!shell.isDisposed())
                    shell.dispose();
            }
        };
        SelectionListener buildListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream tokenStream = lexer.getTokens();
                    if (tokenStream.containErrors()) {
                        outputError(outputLexerError(tokenStream));
                        return;
                    }
                    Parser parser = new Parser(tokenStream.reduceErrors());
                    TreeNode root = parser.parse();
                    if (!parser.isSuccess()) {
                        outputError(outputParserError(parser));
                        return;
                    }
                    Compiler compiler = new Compiler(root);
                    List<Command> commandList = compiler.compile();
                    if (!compiler.isSuccess()) {
                        outputError(outputCompilerError(compiler));
                        return;
                    }
                    if (filePath == null) {
                        MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR);
                        dialog.setMessage("请在生成前保存新文件！");
                        dialog.open();
                        return;
                    }
                    File file = new File(filePath);
                    String outFilePath = file.getAbsolutePath().substring(0, file.getName().contains(".") ? file.getAbsolutePath().lastIndexOf('.') : file.getAbsolutePath().length()) + ".out";
                    File outFile = new File(outFilePath);
                    if (!outFile.exists())
                        if (!outFile.createNewFile())
                            throw new IOException("write");
                    FileOutputStream stream = new FileOutputStream(outFile);
                    stream.write(commandListToString(commandList).getBytes("UTF-8"));
                    stream.close();
                    outputResult(String.format("文件'%s'生成成功！", outFilePath));
                } catch (IOException ex) {
                    if (ex.getMessage().equals("write"))
                        outputError("文件创建失败！");
                    else
                        outputError("文件读取失败！");
                }
            }
        };
        SelectionListener lexerListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream stream = lexer.getTokens();
                    if (stream.containErrors())
                        outputError(outputLexerError(stream));
                    else
                        outputResult(outputToken(stream));
                } catch (IOException ex) {
                    outputError("文件读取失败！");
                }
            }
        };
        SelectionListener parserListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream stream = lexer.getTokens();
                    if (stream.containErrors()) {
                        outputError(outputLexerError(stream));
                    }
                    Parser parser = new Parser(stream.reduceErrors());
                    TreeNode root = parser.parse();
                    if (parser.isSuccess())
                        outputResult(outputTreeNode(root));
                    else
                        outputError(outputParserError(parser));
                } catch (IOException ex) {
                    outputError("文件读取失败！");
                }
            }
        };
        SelectionListener compilerFalseListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream stream = lexer.getTokens();
                    if (stream.containErrors()) {
                        outputError(outputLexerError(stream));
                    }
                    Parser parser = new Parser(stream.reduceErrors());
                    TreeNode root = parser.parse();
                    if (parser.isSuccess()) {
                        Compiler compiler = new Compiler(root);
                        List<Command> commandList = compiler.compile(false);
                        if (compiler.isSuccess())
                            outputResult(outputCommandList(commandList));
                        else
                            outputError(outputCompilerError(compiler));
                    } else {
                        outputError(outputParserError(parser));
                    }
                } catch (IOException ex) {
                    outputError("文件读取失败！");
                }
            }
        };
        SelectionListener compilerListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream stream = lexer.getTokens();
                    if (stream.containErrors()) {
                        outputError(outputLexerError(stream));
                    }
                    Parser parser = new Parser(stream.reduceErrors());
                    TreeNode root = parser.parse();
                    if (parser.isSuccess()) {
                        Compiler compiler = new Compiler(root);
                        List<Command> commandList = compiler.compile();
                        if (compiler.isSuccess())
                            outputResult(outputCommandList(commandList));
                        else
                            outputError(outputCompilerError(compiler));
                    } else {
                        outputError(outputParserError(parser));
                    }
                } catch (IOException ex) {
                    outputError("文件读取失败！");
                }
            }
        };
        SelectionListener runNowListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    TokenStream tokenStream = lexer.getTokens();
                    if (tokenStream.containErrors()) {
                        outputError(outputLexerError(tokenStream));
                        return;
                    }
                    Parser parser = new Parser(tokenStream.reduceErrors());
                    TreeNode root = parser.parse();
                    if (!parser.isSuccess()) {
                        outputError(outputParserError(parser));
                        return;
                    }
                    Compiler compiler = new Compiler(root);
                    List<Command> commandList = compiler.compile();
                    if (!compiler.isSuccess()) {
                        outputError(outputCompilerError(compiler));
                        return;
                    }
                    Launcher launcher = new Launcher(commandList);
                    output.setText("");
                    launcher.launch(new CustomInputStream(current), new CustomOutputStream(current), new CustomErrorStream(current));
                } catch (IOException ex) {
                    if (ex.getMessage().equals("write"))
                        outputError("文件创建失败！");
                    else
                        outputError("文件读取失败！");
                }
            }
        };
        SelectionListener runFileListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setText("打开");
                dialog.setFilterNames(new String[]{"out文件", "所有文件"});
                dialog.setFilterExtensions(new String[]{"*.out", "*.*"});
                String newFilePath = dialog.open();
                if (newFilePath == null)
                    return;
                try {
                    FileInputStream stream = new FileInputStream(newFilePath);
                    Launcher launcher = new Launcher(stream);
                    output.setText("");
                    launcher.launch(new CustomInputStream(current), new CustomOutputStream(current), new CustomErrorStream(current));
                } catch (FileNotFoundException fileEx) {
                    outputError(String.format("未找到文件'%s'！", filePath));
                }
            }
        };
        SelectionListener homeListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    try {
                        URI uri = URI.create("https://github.com/yifanc62/CMMCompiler");
                        Desktop dp = Desktop.getDesktop();
                        if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                            dp.browse(uri);
                        }
                    } catch (Exception ex) {
                        //ignored
                    }
                }
            }
        };
        SelectionListener aboutListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION);
                dialog.setMessage("Cirno Team Project 2017\r\nMembers: \r\nXiyu Huang,\r\nYifan Cui,\r\nZhongtao Miao\r\n");
                dialog.open();
            }
        };

        input.addModifyListener(e -> {
            saved = false;
            display.asyncExec(this::refreshFromInput);
        });
        menuItemNew.addSelectionListener(newListener);
        menuItemOpen.addSelectionListener(openListener);
        menuItemSave.addSelectionListener(saveListener);
        menuItemSaveAs.addSelectionListener(saveAsListener);
        menuItemQuit.addSelectionListener(quitListener);
        menuItemBuild.addSelectionListener(buildListener);
        menuItemLexer.addSelectionListener(lexerListener);
        menuItemParser.addSelectionListener(parserListener);
        menuItemCompilerFalse.addSelectionListener(compilerFalseListener);
        menuItemCompiler.addSelectionListener(compilerListener);
        menuItemRunNow.addSelectionListener(runNowListener);
        menuItemRunFile.addSelectionListener(runFileListener);
        menuItemHome.addSelectionListener(homeListener);
        menuItemAbout.addSelectionListener(aboutListener);
    }

    private void saveFile(boolean asNew) {
        if (asNew || filePath == null) {
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
            dialog.setText("保存");
            dialog.setFilterNames(new String[]{"cmm文件", "文本文档", "所有文件"});
            dialog.setFilterExtensions(new String[]{"*.cmm", "*.txt", "*.*"});
            String newFilePath = dialog.open();
            if (newFilePath == null)
                return;
            filePath = newFilePath;
        } else if (saved) {
            return;
        }
        try {
            File newFile = new File(filePath);
            if (!newFile.exists())
                if (!newFile.createNewFile())
                    throw new IOException();
            FileOutputStream stream = new FileOutputStream(newFile);
            stream.write(text.getBytes("UTF-8"));
            stream.close();
            saved = true;
        } catch (FileNotFoundException fileEx) {
            outputError(String.format("未找到文件'%s'！", filePath));
        } catch (IOException ioEx) {
            outputError("文件保存失败！");
        }
    }

    private void openFile() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText("打开");
        dialog.setFilterNames(new String[]{"cmm文件", "文本文档", "所有文件"});
        dialog.setFilterExtensions(new String[]{"*.cmm", "*.txt", "*.*"});
        String newFilePath = dialog.open();
        if (newFilePath == null)
            return;
        try {
            filePath = newFilePath;
            int length = (int) new File(filePath).length();
            byte[] buffer = new byte[length];
            FileInputStream stream = new FileInputStream(filePath);
            if (stream.read(buffer, 0, length) != length)
                throw new IOException();
            stream.close();
            refresh(new String(buffer, "UTF-8"));
            saved = true;
        } catch (FileNotFoundException fileEx) {
            outputError(String.format("未找到文件'%s'！", filePath));
        } catch (IOException ioEx) {
            outputError("文件读取失败！");
        }
    }

    private void refresh(String newText) {
        input.setText(newText);
        refreshFromInput();
    }

    private void refreshFromInput() {
        text = input.getText();
        try {
            Lexer lexer = new Lexer(new ByteArrayInputStream(text.getBytes("UTF-8")));
            TokenStream stream = lexer.getTokens();
            ArrayList<StyleRange> lexerStyles = new ArrayList<>();
            for (Token token : stream.getAllTokens()) {
                switch (token.getType()) {
                    case K_STMT_IF:
                    case K_STMT_ELSE:
                    case K_STMT_WHILE:
                    case K_STMT_BREAK:
                    case K_STMT_FOR:
                    case K_IO_READ:
                    case K_IO_WRITE:
                    case K_TYPE_INT:
                    case K_TYPE_DOUBLE:
                        StyleRange keywordStyle = new StyleRange(token.getIndex(), token.getLength(), new Color(display, KEYWORD), null);
                        keywordStyle.fontStyle = SWT.BOLD;
                        lexerStyles.add(keywordStyle);
                        break;
                    case V_INT:
                    case V_DOUBLE:
                    case V_BOOL_TRUE:
                    case V_BOOL_FALSE:
                        StyleRange valueStyle = new StyleRange(token.getIndex(), token.getLength(), new Color(display, VALUE), null);
                        lexerStyles.add(valueStyle);
                        break;
                    case S_PLUS:
                    case S_MINUS:
                    case S_MULTIPLY:
                    case S_DIVIDE:
                    case S_MOD:
                    case S_EQUAL:
                    case S_UNEQUAL:
                    case S_GT:
                    case S_GE:
                    case S_LT:
                    case S_LE:
                        StyleRange operatorStyle = new StyleRange(token.getIndex(), token.getLength(), new Color(display, OP), null);
                        operatorStyle.fontStyle = SWT.BOLD;
                        lexerStyles.add(operatorStyle);
                        break;
                    case V_VARIABLE:
                    case S_ASSIGN:
                    case S_PARENTHESIS_L:
                    case S_PARENTHESIS_R:
                    case S_BRACKET_L:
                    case S_BRACKET_R:
                    case S_BRACE_L:
                    case S_BRACE_R:
                    case S_COMMA:
                    case S_SEMICOLON:
                        break;
                    case C:
                        StyleRange commentStyle = new StyleRange(token.getIndex(), token.getLength(), new Color(display, COMMENT), null);
                        commentStyle.fontStyle = SWT.ITALIC;
                        lexerStyles.add(commentStyle);
                        break;
                    case E_UNRECOGNIZED:
                    case E_VALUE:
                        StyleRange errorStyle = new StyleRange(token.getIndex(), token.getLength(), new Color(display, ERROR), null);
                        errorStyle.fontStyle = SWT.ITALIC;
                        errorStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
                        errorStyle.underlineColor = new Color(display, ERROR);
                        errorStyle.underline = true;
                        lexerStyles.add(errorStyle);
                        break;
                }
            }
            Parser parser = new Parser(stream.reduceErrors());
            parser.parse();
            ArrayList<StyleRange> parserStyles = new ArrayList<>();
            for (Exception e : parser.getExceptions()) {
                if (e instanceof UnexpectedTokenException) {
                    Token token = ((UnexpectedTokenException) e).getToken();
                    StyleRange errorStyle = new StyleRange(token.getIndex(), token.getLength(), null, null);
                    errorStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
                    errorStyle.underlineColor = new Color(display, ERROR);
                    errorStyle.underline = true;
                    parserStyles.add(errorStyle);
                }
            }
            for (StyleRange parserStyle : parserStyles) {
                boolean exist = false;
                for (StyleRange lexerStyle : lexerStyles) {
                    if (lexerStyle.start == parserStyle.start && lexerStyle.length == parserStyle.length) {
                        lexerStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
                        lexerStyle.underlineColor = new Color(display, ERROR);
                        lexerStyle.underline = true;
                        exist = true;
                    }
                }
                if (!exist) {
                    lexerStyles.add(parserStyle);
                }
            }
            lexerStyles.sort(Comparator.comparingInt(o -> o.start));
            input.setStyleRanges(lexerStyles.toArray(new StyleRange[lexerStyles.size()]));
        } catch (IOException e) {
            //ignored
        }
    }

    private void outputResult(String result) {
        output.setText(result);
        output.setStyleRange(new StyleRange(0, result.length(), new Color(display, BLACK), null));
    }

    private void outputError(String message) {
        output.setText(message);
        output.setStyleRange(new StyleRange(0, message.length(), new Color(display, ERROR), null));
    }

    public void outputToResult(int b) {
        output.setText(output.getText() + (char) b);
        output.setStyleRange(new StyleRange(0, output.getText().length(), new Color(display, BLACK), null));
    }

    public void outputToError(int b) {
        output.setText(output.getText() + (char) b);
        output.setStyleRange(new StyleRange(0, output.getText().length(), new Color(display, ERROR), null));
    }

    public int getChar() {
        if (inputStack.isEmpty()) {
            String s = new InputDialog(shell).open();
            while (s == null || s.trim().equals(""))
                s = new InputDialog(shell).open();
            s += "\n";
            for (int i = s.length(); i > 0; i--) {
                inputStack.push(s.charAt(i - 1));
            }
        }
        return (int) inputStack.pop();
    }
}
