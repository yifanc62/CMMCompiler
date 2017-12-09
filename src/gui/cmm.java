package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wb.swt.SWTResourceManager;

import Lexic.Lexer;
import Lexic.TokenStream;
import Semantic.Command;
import Semantic.Compiler;
import Syntactic.Parser;
import Syntactic.TreeNode;
import Util.Util;
import VirtualMachine.Launcher;


public class cmm {

	protected Shell shlCmmInterpreter;
	private LineStyler lineStyler = new LineStyler();
	private Text outText; 
	private String about = "";
	private boolean saved = false;
	private String filePath = null; 

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			cmm window = new cmm();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents(display);
		shlCmmInterpreter.open();
		shlCmmInterpreter.layout();
		while (!shlCmmInterpreter.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents(Display display) {
		shlCmmInterpreter = new Shell();
		shlCmmInterpreter.setImage(null);
		shlCmmInterpreter.setSize(845, 679);
		shlCmmInterpreter.setText("cmm interpreter");
		shlCmmInterpreter.setLayout(null);

		Menu menu = new Menu(shlCmmInterpreter, SWT.BAR);
		shlCmmInterpreter.setMenuBar(menu);

		StyledText inputText = new StyledText(shlCmmInterpreter, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		inputText.setBounds(10, 10, 807, 400);

		outText = new Text(shlCmmInterpreter, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		outText.setEditable(false);
		outText.setBounds(10, 441, 807, 156);
		outText.setText(about);

		MenuItem mntmOpen = new MenuItem(menu, SWT.NONE);
		String[] filterExt = { "*.cmm", "*.txt", "*.*" };
		mntmOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shlCmmInterpreter, SWT.OPEN);
				fd.setText("打开");
				fd.setFilterExtensions(filterExt);
				filePath = fd.open(); // 文件绝对路径
				if (filePath != null) {
					try {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
						final StringBuilder sb = new StringBuilder();
						String content;
						while ((content = br.readLine()) != null) {
							sb.append(content);
							sb.append(System.getProperty("line.separator"));
						}
						br.close();
						Display display = inputText.getDisplay();
						display.asyncExec(new Runnable() {
							public void run() {
								inputText.setText(sb.toString()); // 设置输入区文本
								saved = true;
							}
						});
						
						lineStyler.parseBlockComments(sb.toString());
					} catch (FileNotFoundException e1) {
						// e1.printStackTrace();
					} catch (IOException e1) {
						// e1.printStackTrace();
					}
				}

			}
		});
		mntmOpen.setText("\u6253\u5F00");

		MenuItem mntmSave = new MenuItem(menu, SWT.NONE);
		mntmSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog fd = new FileDialog(shlCmmInterpreter, SWT.SAVE);
				fd.setText("保存");
				fd.setFilterExtensions(filterExt);
				filePath = fd.open();

				if(filePath != null) {
					File file = new File(filePath);
					try {
						if (!file.exists()) {
							file.createNewFile();
						}
						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()), "UTF-8"));
						bw.write(inputText.getText());
						bw.close();
						saved = true;
					} catch (IOException e1) {
						// e1.printStackTrace();
					}
				}
			}
		});
		mntmSave.setText("\u4FDD\u5B58");
		
		MenuItem mntmRun_1 = new MenuItem(menu, SWT.CASCADE);
		mntmRun_1.setText("Run");
		
		Menu menu_1 = new Menu(mntmRun_1);
		mntmRun_1.setMenu(menu_1);
		
		MenuItem mntmLexer = new MenuItem(menu_1, SWT.NONE);
		mntmLexer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(saved) {
					if(filePath != null) {
						Lexer lexer;
						try {
							outText.setText(""); //每次输出词法分析结果前清空输出区
							lexer = new Lexer(new FileInputStream(filePath));
		                    TokenStream stream = lexer.getTokens();
		                    Util.outputToken(stream, outText);
		                    if (!stream.containErrors()) {
		                        Util.outputLexerError(stream, outText);		        
		                    }else {
		                    	
		                    }
		                    
						} catch (FileNotFoundException e2) {
							// TODO Auto-generated catch block
							//e2.printStackTrace();
							outText.setText("没有找到该文件");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	                    
					}else {
						outText.setText("没有打开或保存文件");
					}
				}else {
					outText.setText("没有打开或保存文件");
				}
			
			}
		});
		mntmLexer.setText("词法分析结果(Token)");
		
		MenuItem mntmTreenode = new MenuItem(menu_1, SWT.NONE);
		mntmTreenode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(saved) {
					if(filePath != null) {
						Lexer lexer;
						try {
							outText.setText(""); //每次输出词法分析结果前清空输出区
							lexer = new Lexer(new FileInputStream(filePath));
		                    TokenStream stream = lexer.getTokens();
		                    //Util.outputToken(stream, outText);
		                    if (!stream.containErrors()) {
		                        Util.outputLexerError(stream, outText);		        
		                    }else {
		                    	Parser parser = new Parser(stream);
			                    TreeNode root = parser.parse();
			                    if (!parser.isSuccess()) {
			                        Util.outputParserError(parser, outText);
			                        return;
			                    }else {
			                    	Util.outputTreeNode(root, outText);
			                    }
		                    }		                    		                    
						} catch (FileNotFoundException e2) {
							// TODO Auto-generated catch block
							//e2.printStackTrace();
							outText.setText("没有找到该文件");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	                    
					}else {
						outText.setText("没有打开或保存文件");
					}
				}else {
					outText.setText("没有打开或保存文件");
				}
			}
		});
		mntmTreenode.setText("语法树");

		MenuItem mntmIR = new MenuItem(menu_1, SWT.CASCADE);
		mntmIR.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(saved) {
					if(filePath != null) {
						Lexer lexer;
						try {
							outText.setText(""); //每次输出词法分析结果前清空输出区
							lexer = new Lexer(new FileInputStream(filePath));
		                    TokenStream stream = lexer.getTokens();
		                    //Util.outputToken(stream, outText);
		                    if (!stream.containErrors()) {
		                        Util.outputLexerError(stream, outText);		        
		                    }else {
		                    	Parser parser = new Parser(stream);
			                    TreeNode root = parser.parse();
			                    if (!parser.isSuccess()) {
			                        Util.outputParserError(parser, outText);
			                        return;
			                    }else {
			                    	//Util.outputTreeNode(root, outText);
			                    	Compiler compiler = new Compiler(root);
			                        List<Command> commands = compiler.compile();
			                        if (!compiler.isSuccess()) {
			                            Util.outputCompilerError(compiler, outText);
			                            return;
			                        }else {
			                        	Util.outputCommandList(commands, outText);
			                        }
			                    }
		                    }		                    		                    
						} catch (FileNotFoundException e2) {
							// TODO Auto-generated catch block
							//e2.printStackTrace();
							outText.setText("没有找到该文件");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	                    
					}else {
						outText.setText("没有打开或保存文件");
					}
				}else {
					outText.setText("没有打开或保存文件");
				}
			}
		});
		mntmIR.setText("四元式");
		
		MenuItem mntmRun = new MenuItem(menu_1, SWT.NONE);
		mntmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(saved) {
					if(filePath != null) {
						Lexer lexer;
						try {
							outText.setText(""); //每次输出词法分析结果前清空输出区
							lexer = new Lexer(new FileInputStream(filePath));
		                    TokenStream stream = lexer.getTokens();
		                    //Util.outputToken(stream, outText);
		                    if (!stream.containErrors()) {
		                        Util.outputLexerError(stream, outText);		        
		                    }else {
		                    	Parser parser = new Parser(stream);
			                    TreeNode root = parser.parse();
			                    if (!parser.isSuccess()) {
			                        Util.outputParserError(parser, outText);
			                        return;
			                    }else {
			                    	//Util.outputTreeNode(root, outText);
			                    	Compiler compiler = new Compiler(root);
			                        List<Command> commands = compiler.compile();
			                        if (!compiler.isSuccess()) {
			                            Util.outputCompilerError(compiler, outText);
			                            return;
			                        }else {
			                        	//Util.outputCommandList(commands, outText);
			                        	Launcher launcher = new Launcher(commands);
			                            launcher.launch(System.in, System.out, System.err, outText);
			                        }
			                    }
		                    }		                    		                    
						} catch (FileNotFoundException e2) {
							// TODO Auto-generated catch block
							//e2.printStackTrace();
							outText.setText("没有找到该文件");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	                    
					}else {
						outText.setText("没有打开或保存文件");
					}
				}else {
					outText.setText("没有打开或保存文件");
				}
			}
		});
		mntmRun.setText("解释执行");

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//outText.setText(about);
			}
		});
		mntmAbout.setText("\u8BF4\u660E");

		MenuItem mntmExit = new MenuItem(menu, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!shlCmmInterpreter.isDisposed()) {
					shlCmmInterpreter.dispose();
				}
				return;
			}
		});
		mntmExit.setText("\u9000\u51FA");

		Label label = new Label(shlCmmInterpreter, SWT.NONE);
		label.setBounds(20, 415, 76, 20);
		label.setText("\u8F93\u51FA\uFF1A");
		
		inputText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				saved = false;
				lineStyler.parseBlockComments(inputText.getText());
			}
		});
		inputText.addLineStyleListener(lineStyler);
	}
}
