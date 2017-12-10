package com.cirnoteam.cmm.graphic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

public class InputDialog extends Dialog {
    private String text;

    public InputDialog(Shell parent) {
        super(parent, SWT.NONE);
        text = "";
    }

    public String open() {
        Shell parent = getParent();
        Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setSize(300, 105);
        shell.setText(getText());

        FormLayout layout = new FormLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        shell.setLayout(layout);

        Label label = new Label(shell, SWT.NONE);
        FormData formLabel = new FormData();
        formLabel.width = 300;
        formLabel.height = 15;
        formLabel.top = new FormAttachment(0);
        formLabel.left = new FormAttachment(0);
        formLabel.right = new FormAttachment(100);
        label.setLayoutData(formLabel);
        label.setText("输入：");

        Text input = new Text(shell, SWT.BORDER | SWT.SINGLE);
        FormData formInput = new FormData();
        formLabel.width = 300;
        formInput.height = 15;
        formInput.top = new FormAttachment(label, 5, SWT.BOTTOM);
        formInput.left = new FormAttachment(0);
        formInput.right = new FormAttachment(100);
        input.setLayoutData(formInput);

        Button submit = new Button(shell, SWT.PUSH | SWT.CENTER);
        FormData formSubmit = new FormData();
        formSubmit.width = 60;
        formSubmit.height = 25;
        formSubmit.top = new FormAttachment(input, 5, SWT.BOTTOM);
        formSubmit.right = new FormAttachment(100, 5);
        submit.setLayoutData(formSubmit);
        submit.setText("确定");

        SelectionListener submitListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                text = input.getText();
                if (!shell.isDisposed())
                    shell.dispose();
            }
        };
        submit.addSelectionListener(submitListener);

        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return text;
    }
}
