package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.Version;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Builds a FormPanel with information about this project and its license.
 * Shows how to use PanelField to render a logo image inline with a FormPanel,
 * and also shows how to use a non-editable TextField to show a large
 * blob of text.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class AboutPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "About";
    }

    @Override
    public JPanel build() {
        final FormPanel aboutPanel = new FormPanel();

        PanelField logoField = new PanelField();
        JPanel panel = logoField.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        URL url = getClass().getResource("/ca/corbett/swing-forms/images/swing-forms-logo.jpg");
        ImageIcon logoImage = new ImageIcon(url);
        JLabel imageLabel = new JLabel(logoImage);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(imageLabel);
        aboutPanel.addFormField(logoField);

        String labelText = Version.FULL_NAME;
        LabelField labelField = new LabelField(labelText);
        labelField.setFont(new Font("SansSerif", Font.BOLD, 24));
        labelField.setLeftMargin(14);
        aboutPanel.addFormField(labelField);

        labelField = LabelField.createPlainHeaderLabel(Version.COPYRIGHT);
        labelField.setLeftMargin(14);
        aboutPanel.addFormField(labelField);
        labelField = LabelField.createPlainHeaderLabel(Version.PROJECT_URL);
        labelField.setLeftMargin(14);
        labelField.setBottomMargin(20);
        aboutPanel.addFormField(labelField);

        PanelField licenseField = new PanelField();
        licenseField.getPanel().setLayout(new BorderLayout());
        licenseField.setMargins(12, 16, 2, 2, 0);
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textArea.setColumns(70);
        textArea.setRows(16);
        textArea.setEditable(false);
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/ca/corbett/swing-forms/LICENSE")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            textArea.setText(sb.toString());
        }
        catch (IOException ioe) {
            textArea.setText("Unable to load license text: " + ioe.getMessage());
        }
        licenseField.getPanel().add(textArea, BorderLayout.CENTER);
        aboutPanel.addFormField(licenseField);

        aboutPanel.render();
        return aboutPanel;
    }
}
