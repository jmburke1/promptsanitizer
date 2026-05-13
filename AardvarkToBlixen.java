import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AardvarkToBlixen {

    private static final String AARDVARK = "aardvark";
    private static final String BLIXEN = "blixen";

    private final JTextArea leftArea  = new JTextArea();
    private final JTextArea rightArea = new JTextArea();

    public static void main(String[] args) {
        new AardvarkToBlixen().createUI();
    }

    private void createUI() {
        JFrame frame = new JFrame("Aardvark ↔ Blixen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Left text area (source for >, destination for <)
        leftArea.setText("Hello aardvark!");
        leftArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        leftArea.setLineWrap(true);
        leftArea.setWrapStyleWord(true);
        leftArea.setPreferredSize(new Dimension(300, 200));

        // Right text area (destination for >, source for <)
        rightArea.setText("");
        rightArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        rightArea.setLineWrap(true);
        rightArea.setWrapStyleWord(true);
        rightArea.setPreferredSize(new Dimension(300, 200));

        // Center panel with two text areas side by side
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel leftPanel  = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(leftArea), BorderLayout.CENTER);
        leftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Left"));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(rightArea), BorderLayout.CENTER);
        rightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Right"));

        // Button panel with > and < buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton moveRightButton = new JButton(">");
        JButton moveLeftButton  = new JButton("<");

        moveRightButton.addActionListener(e -> moveTextLeftToRight());
        moveLeftButton.addActionListener(e -> moveTextRightToLeft());

        buttonPanel.add(moveLeftButton);
        buttonPanel.add(moveRightButton);

        // Assemble the center panel — GridBagLayout for proportional widths (46% / 8% / 46%)
        JPanel topRow = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.weightx = 0.46; gbc.weighty = 1.0;
        topRow.add(leftPanel, gbc);

        gbc.weightx = 0.08;
        topRow.add(buttonPanel, gbc);

        gbc.weightx = 0.46;
        topRow.add(rightPanel, gbc);

        centerPanel.add(topRow, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Size the window and center on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width / 2, screenSize.height / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Move text from left to right, replacing "aardvark" → "blixen". */
    private void moveTextLeftToRight() {
        String source = leftArea.getText();
        String transformed = source.replace(AARDVARK, BLIXEN);
        rightArea.setText(transformed);
        leftArea.setText("");
    }

    /** Move text from right to left, replacing "blixen" → "aardvark". */
    private void moveTextRightToLeft() {
        String source = rightArea.getText();
        String transformed = source.replace(BLIXEN, AARDVARK);
        leftArea.setText(transformed);
        rightArea.setText("");
    }
}
