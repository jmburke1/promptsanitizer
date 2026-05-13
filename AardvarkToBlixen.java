import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
        leftArea.setLineWrap(true);
        leftArea.setWrapStyleWord(true);

        // Right text area (destination for >, source for <)
        rightArea.setText("");
        rightArea.setLineWrap(true);
        rightArea.setWrapStyleWord(true);

        // Center panel with two text areas side by side
        JPanel centerPanel = new JPanel(new BorderLayout());

        JScrollPane leftScroll  = new JScrollPane(leftArea);
        JScrollPane rightScroll = new JScrollPane(rightArea);

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

        // Assemble the center panel
        JPanel sideBySide = new JPanel(new BorderLayout());
        sideBySide.add(leftPanel,  BorderLayout.WEST);
        sideBySide.add(buttonPanel, BorderLayout.CENTER);
        sideBySide.add(rightPanel, BorderLayout.EAST);

        centerPanel.add(sideBySide, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Size the window and center on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(Math.min(800, screenSize.width / 2), Math.min(600, screenSize.height / 2));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** Move text from left to right, replacing "aardvark" → "blixen". */
    private void moveTextLeftToRight() {
        String source = leftArea.getText();
        String transformed = source.replace(AARDVARK, BLIXEN);
        rightArea.setText(transformed);
    }

    /** Move text from right to left, replacing "blixen" → "aardvark". */
    private void moveTextRightToLeft() {
        String source = rightArea.getText();
        String transformed = source.replace(BLIXEN, AARDVARK);
        leftArea.setText(transformed);
    }
}
