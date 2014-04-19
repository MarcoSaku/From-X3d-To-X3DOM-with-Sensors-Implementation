package X3dToX3dom;

import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author MarcoSaku
 */
public class GUI {
    private ParserX3D parser;
    private JTextField text2, text22, text1, text11;
    private String log = "";
    private Object obj;
    private JFrame f;
    private String input;
    private String output;
    private final String helpString = "This software converts a X3D scene into X3DOM scene.\n"
            + "Furthermore it is an extension of X3DOM, in fact it implements automatically the following sensors that aren't present in X3DOM using JavaScript\n"
            + "\nTouchSensor (fields: isActive, isOver, touchTime).\n"
            + "CylinderSensor (fields: set_rotation)\n"
            + "PlaneSensor (fields: set_translation)\n"
            + "StringSensor (fields: string)";

    private final String aboutString = "X3D             http://www.web3d.org/realtime-3d/x3d/what-x3d\n"
            + "X3DOM       http://www.x3dom.org/\n"
            + "X3D-Edit     http://www.web3d.org/x3d/content/README.X3D-Edit.html  ";

    private final String authorString = "Marco Saviano\nItaly\nEmail: marco.saviano.89@gmail.com";

    public void startGUI() throws CloneNotSupportedException {
        parser = new ParserX3D();
        f = new JFrame("X3D to X3DOM");
        f.setIconImage(Toolkit.getDefaultToolkit().getImage("img.gif"));
        //f.setLayout(new GridLayout(2,2,10,30));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        // File Menu, F - Mnemonic
        JMenu fileMenu = new JMenu("Info");
        //fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem helpItem = new JMenuItem("Help", KeyEvent.VK_N);
        helpItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea tf = new JTextArea(helpString);
                tf.setEditable(false);
                tf.setBackground(null);
                JOptionPane.showMessageDialog(f, tf, "Help", PLAIN_MESSAGE);

            }
        });
        fileMenu.add(helpItem);
        JMenuItem aboutItem = new JMenuItem("About X3DOM", KeyEvent.VK_N);
        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea tf = new JTextArea(aboutString);
                tf.setEditable(false);

                tf.setBackground(null);

                JOptionPane.showMessageDialog(f, tf, "About X3DOM", PLAIN_MESSAGE);

            }
        });
        fileMenu.add(aboutItem);

        JMenuItem authorItem = new JMenuItem("Author", KeyEvent.VK_N);
        authorItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea tf = new JTextArea(authorString);
                tf.setEditable(false);

                tf.setBackground(null);
                JOptionPane.showMessageDialog(f, tf, "Author Info", PLAIN_MESSAGE);

            }
        });
        fileMenu.add(authorItem);
        menuBar.add(fileMenu);
        f.setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        JPanel panelInput = new JPanel();
        panelInput.setLayout(new FlowLayout());
        text1 = new JTextField("X3D File Name");
        text1.setEditable(false);
        text1.setBorder(null);
        text2 = new JTextField("test.x3d", 10);

        panelInput.add(text1);
        panelInput.add(text2);

        
        JPanel panelOutput = new JPanel();
        panelOutput.setLayout(new FlowLayout());
        text11 = new JTextField("XHTML File Name");
        text11.setEditable(false);
        text11.setBorder(null);
        text22 = new JTextField("out.xhtml", 10);

        panelOutput.add(text11);
        panelOutput.add(text22);

        panel.add(panelInput);
        panel.add(panelOutput);

        JButton but = new JButton("OK");
        obj = this;

        but.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    input = text2.getText();
                    output = text22.getText();
                    log += "INPUT FILE " + input + "\nOUTPUT FILE " + output + "\n\n";
                    if ((output.substring(output.length() - 6, output.length()).equals(".xhtml")) == false) {
                        log += "Attention: rename output file in .xhtml\n";
                    }
                    parser.parse(text2.getText(), text22.getText(), (GUI) obj);
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        panel.add(but);
        panel.validate();
        f.setContentPane(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.validate();
        f.setVisible(true);
    }

    public void addLog(String str) {
        log += str;
    }

    public void showLog() {
        JOptionPane.showMessageDialog(f, log, "Parsing Complete!", INFORMATION_MESSAGE);
        System.exit(0);
    }

    void notFound(String string) {
        JOptionPane.showMessageDialog(f, string, "File Not Found!", INFORMATION_MESSAGE);
        log = "";
    }

}
