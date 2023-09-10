package reactionsystem;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTextArea reactionsTextArea;
    private JPanel mainPanel;
    private JTextArea environmentTextArea;
    private JTextArea contextTextArea;
    private JButton computeFinalResultButton;
    private JButton interactivelyComputeNextStepButton;
    private JButton writeResultToDOTButton;

    public MainFrame() {
        setContentPane(mainPanel);
        setTitle("BioResolve");
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
