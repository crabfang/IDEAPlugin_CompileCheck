package dialog;

import javax.swing.*;
import java.awt.event.*;

public class CompileCheckDialog extends JDialog {
    private JPanel contentPane;
    private JTextPane labelContent;

    public CompileCheckDialog() {
        setContentPane(contentPane);
        setModal(true);
        setBounds(300, 300, 400, 300);

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

    public static void main(String[] args) {
        CompileCheckDialog dialog = new CompileCheckDialog();
        dialog.pack();
        dialog.setLabel("label info text www.u51.com");
        dialog.setVisible(true);
        System.exit(0);
    }
}
