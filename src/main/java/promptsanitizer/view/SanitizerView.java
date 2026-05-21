package promptsanitizer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.Box;
import javax.swing.BoxLayout;
import promptsanitizer.controller.SanitizerController;
import promptsanitizer.model.SanitizerModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java.awt.Color;

public class SanitizerView {
    public SanitizerView(String fileName, SanitizerController controller, SanitizerModel model) {
        this.fileName = fileName;
        this.controller = controller;
        this.model = model;
        leftArea = new JTextArea();
        rightArea = new JTextArea();
    }

    private final SanitizerController controller;
    private final SanitizerModel model;
    private final String fileName;
    private final JTextArea leftArea;
    private final JTextArea rightArea;

    public void createUI() {
        model.init(fileName);
        controller.init(model, fileName);
        JFrame frame = new JFrame("Replace Sensitive Strings in Your Prompts to an LLM.  Back replace the answer from the LLM.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Left text area (source for >, destination for <)
        leftArea.setText("");
        leftArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        leftArea.setLineWrap(true);
        leftArea.setWrapStyleWord(true);
        leftArea.setPreferredSize(new Dimension(300, 200));

        // Right text area (destination for >, source for <)
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 24);
        rightArea.setText("");
        rightArea.setFont(font);
        rightArea.setLineWrap(true);
        rightArea.setWrapStyleWord(true);
        rightArea.setPreferredSize(new Dimension(300, 200));

        // Center panel with two text areas side by side
        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel leftPanel  = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(leftArea), BorderLayout.CENTER);
        leftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Unsanitized Prompt", TitledBorder.LEFT, TitledBorder.TOP, font, Color.BLACK));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(rightArea), BorderLayout.CENTER);
        rightPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sanitized Prompt", TitledBorder.LEFT, TitledBorder.TOP, font, Color.BLACK));

        // Button panel with > and < buttons side by side, ~ underneath
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton moveRightButton = new JButton(">");
        JButton moveLeftButton  = new JButton("<");

        moveRightButton.addActionListener(e -> controller.moveText(leftArea, rightArea, false));

        moveLeftButton.addActionListener(e -> controller.moveText(rightArea, leftArea, true));

        topButtons.add(moveLeftButton);
        topButtons.add(moveRightButton);

        JButton tildeButton = new JButton("~");
        tildeButton.addActionListener(e -> controller.handleTilde());

        buttonPanel.add(topButtons);
        buttonPanel.add(Box.createVerticalStrut(4));
        JPanel tildeRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tildeRow.add(tildeButton);
        buttonPanel.add(tildeRow);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Assemble the center panel — GridBagLayout for proportional widths (46% / 8% / 46%)
        JPanel topRow = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.weightx = 0.46; gbc.weighty = 1.0;
        topRow.add(leftPanel, gbc);

        gbc.weightx = 0.08; gbc.fill = GridBagConstraints.NONE;
        topRow.add(buttonPanel, gbc);

        gbc.weightx = 0.46; gbc.fill = GridBagConstraints.BOTH;
        topRow.add(rightPanel, gbc);

        centerPanel.add(topRow, BorderLayout.CENTER);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Size the window and center on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width / 2, screenSize.height / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
