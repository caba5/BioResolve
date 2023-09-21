package bioresolve;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {
    private JTextArea reactionsTextArea;
    private JPanel mainPanel;
    private JTextArea environmentTextArea;
    private JTextArea contextTextArea;
    private JButton computeFinalResultButton;
    private JLabel outLabel;

    private final List<JTextArea> textAreas;

    public MainFrame() {
        setContentPane(mainPanel);
        setTitle("BioResolve");
        setMinimumSize(new Dimension(900, 500));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        ButtonListener bl = new ButtonListener();
        reactionsTextArea.getDocument().addDocumentListener(bl);
        environmentTextArea.getDocument().addDocumentListener(bl);
        contextTextArea.getDocument().addDocumentListener(bl);

        textAreas = List.of(reactionsTextArea, environmentTextArea, contextTextArea);

        computeFinalResultButton.addActionListener(e -> {
            try {
                String reactionsString = reactionsTextArea.getText();

                Reaction.checkReactionStringConformity(reactionsString);

                Set<Entity> S = Entity.extrapolateEntitiesFromReactionsString(reactionsString);

                Set<Reaction> A = Reaction.parseReactions(reactionsString);

                ReactionSystem RS = new ReactionSystem(S, A);

                String environment = environmentTextArea.getText();
                String context = contextTextArea.getText();

                List<Context> parGamma = Context.parseParallel(context);
                Environment env = new Environment(environment);

                List<InteractiveProcess> pi = InteractiveProcess.createParallelProcesses(env, parGamma);

                ManagersCoordinator.setRS(RS);
                ManagersCoordinator coordinator = ManagersCoordinator.getInstance();

                coordinator.spawnManager(pi);
                coordinator.getLastManager().bindManagerToProcesses();

                Duration totalTime = coordinator.compute();

                outLabel.setVisible(true);
                outLabel.setForeground(new Color(0, 153, 51));
                outLabel.setText("Computed in " + (float) totalTime.toNanos() / 1000000000 + "s");

                coordinator.resetCoordinator(); // Resets the coordinator allowing subsequent executions
            } catch (IllegalArgumentException ex) {
                outLabel.setVisible(true);
                outLabel.setForeground(Color.RED);
                outLabel.setText(String.valueOf(ex));
            }
        });
    }

    private void checkTextAreasFilled() {
        for (final JTextArea t : textAreas) {
            if (t.getText().isBlank()) {
                computeFinalResultButton.setEnabled(false);
                return;
            }
        }

        computeFinalResultButton.setEnabled(true);
    }

    private class ButtonListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            checkTextAreasFilled();
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            checkTextAreasFilled();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            checkTextAreasFilled();
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
