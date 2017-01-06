package com.cabe.idea.plugin.dialog;

import javax.swing.*;
import java.awt.event.*;

public class CompileCheckDialog extends JDialog {
    private JPanel contentPane;
    private JTextPane labelContent;

    private int positionX = 300;
    private int positionY = 200;

    public CompileCheckDialog() {
        setContentPane(contentPane);
        setModal(true);
        setBounds(positionX, positionY, 400, 300);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void setLabel(String info) {
        labelContent.setText(info);
    }

    public void updateDialogWidth(int maxLength) {
        if(maxLength > 60) {
            int width = (int) (1.0f * 400 / 60 * maxLength + 20);
            if(width > 1000) {
                width = 1000;
            }
            setBounds(positionX, positionY, width, 300);
        }
    }

    public static void main(String[] args) {
        CompileCheckDialog dialog = new CompileCheckDialog();
        dialog.pack();
        dialog.setLabel("label info text www.u51.com");
        dialog.setVisible(true);
        System.exit(0);
    }
}
