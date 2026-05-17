package promptsanitizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.Box;
import javax.swing.BoxLayout;
import org.json.JSONObject;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.util.Map;
import java.util.HashMap;

//compile with javac -cp json-20250107.jar MainSanitizerApp.java
//run with java -cp json-20250107.jar:. MainSanitizerApp
//json-20250107.jar comes from https://repo1.maven.org/maven2/org/json/json/20250107/
public class MainSanitizerApp {

    private final JTextArea leftArea  = new JTextArea();
    private final JTextArea rightArea = new JTextArea();
    private Map<String, String> dictionary;

    public static void main(String[] args) {
        new MainSanitizerApp().createUI();
    }

    /** Load the personal dictionary from disk. Returns an empty map if the file doesn't exist. */
    private void loadDictionary() {
        File f = new File("personal_dictionary.json");
        if (!f.exists()) {
            dictionary = Map.of();
            return;
        }
        try {
            JSONObject json = new JSONObject(Files.readString(Path.of("personal_dictionary.json")));
            dictionary = new HashMap<>();
            for (String k : json.keySet()) {
                dictionary.put(k, json.getString(k));
            }
        } catch (Exception ex) {
            dictionary = Map.of();
        }
    }

    /** Apply all replacements from the dictionary, in the appropriate direction, to the given text. */
    private String applyDictionary(String text, boolean isReverseDirection) {
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            if(isReverseDirection) {
                text = text.replace(entry.getValue(), entry.getKey());
            } else {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        return text;
    }

    private void createUI() {
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

        moveRightButton.addActionListener(e -> moveText(leftArea, rightArea, false));

        moveLeftButton.addActionListener(e -> moveText(rightArea, leftArea, true));

        topButtons.add(moveLeftButton);
        topButtons.add(moveRightButton);

        JButton tildeButton = new JButton("~");
        tildeButton.addActionListener(e -> {
            dictionary = null;
            new DictionaryEditor().createUI();
        });

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

        gbc.weightx = 0.46; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
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

    /** Move text from one area to another, applying the dictionary replacements in the appropriate direction. */
    private void moveText(JTextArea fromArea, JTextArea toArea, boolean isReverseDirection) {
        if (dictionary == null) loadDictionary();
        String text = fromArea.getText();
        if(!text.isEmpty()) {
            toArea.setText(applyDictionary(text, isReverseDirection));
            fromArea.setText("");
        }
    }
}
