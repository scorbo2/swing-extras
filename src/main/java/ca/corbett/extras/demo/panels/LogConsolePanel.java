package ca.corbett.extras.demo.panels;

import ca.corbett.extras.logging.LogConsole;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.extras.logging.LogConsoleTheme;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.logging.Level;

/**
 * A demo panel to show off the LogConsole and the LogConsoleStyle/LogConsoleTheme
 * customizations that you can do to it.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class LogConsolePanel extends PanelBuilder {

    private FormPanel formPanel;
    private ShortTextField tokenField;
    private FontField tokenFontField;

    @Override
    public String getTitle() {
        return "LogConsole";
    }

    @Override
    public JPanel build() {
        formPanel = buildFormPanel("LogConsole");

        // Start with an introductory label:
        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>We can create a custom live-updated view of a log file with<br>" +
                        "configurable styles that can decide how to render the<br>" +
                        "log output based on string tokens within the log message.<br><br>" +
                        "This can make it visually easy to see what's going on!</html>", 14));

        // We can use PanelField to wrap a launcher button to show the LogConsole:
        JButton button = generateButton("Show LogConsole",
                                        e -> LogConsole.getInstance().setVisible(true));
        formPanel.add(generatePanelField(button));

        // Another informational label:
        LabelField labelField = LabelField.createPlainHeaderLabel(
                "Regular log messages will use whatever log theme is selected.",
                14);
        labelField.getMargins().setTop(24);
        formPanel.add(labelField);

        // And then our action buttons for logging sample messages:
        button = generateButton("Generate INFO message",
                                e -> logMessage("This is a regular INFO log message.", Level.INFO));
        PanelField panelField = generatePanelField(button);
        panelField.getMargins().setTop(0).setBottom(0);
        formPanel.add(panelField);

        button = generateButton("Generate WARNING message",
                                e -> logMessage("This is a WARNING log message.", Level.WARNING));
        panelField = generatePanelField(button);
        panelField.getMargins().setTop(0).setBottom(0);
        formPanel.add(panelField);

        button = generateButton("Generate SEVERE message",
                                e -> logMessage("This is a SEVERE log message.", Level.SEVERE));
        panelField = generatePanelField(button);
        panelField.getMargins().setTop(0).setBottom(0);
        formPanel.add(panelField);

        labelField = LabelField.createPlainHeaderLabel("We can create a custom swing-extras log theme!", 14);
        labelField.getMargins().setTop(24);
        formPanel.add(labelField);

        // We can also show how to customize log styling:
        tokenField = new ShortTextField("Messages containing:", 20).setAllowBlank(false);
        tokenField.setText("My custom message");
        formPanel.add(tokenField);

        // Use a FontField to allow customization of colors, font face, and style:
        tokenFontField = new FontField("Will look like this:",
                                       new Font(Font.MONOSPACED, Font.BOLD, 12),
                                       Color.WHITE,
                                       Color.BLACK);
        tokenFontField.setShowSizeField(false);
        formPanel.add(tokenFontField);

        button = generateButton("Try it!",
                                e -> {
                                    // Force a re-initialization with current style settings:
                                    initializeLogConsole();
                                    logMessage(tokenField.getText(), Level.INFO);
                                });
        panelField = generatePanelField(button);
        panelField.getMargins().setTop(0);
        formPanel.add(panelField);

        initializeLogConsole();

        return formPanel;
    }

    /**
     * Invoked internally to create a JButton with consistent font/sizing, and with the given ActionListener.
     */
    private JButton generateButton(String caption, ActionListener listener) {
        JButton button = new JButton(caption);
        button.setPreferredSize(new Dimension(220, 26));
        button.setFont(button.getFont().deriveFont(Font.PLAIN));
        button.addActionListener(listener);
        return button;
    }

    /**
     * Invoked internally to generate a PanelField wrapper for the given JButton.
     */
    private PanelField generatePanelField(JButton buttonToWrap) {
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        panelField.getMargins().setLeft(32);
        panelField.getPanel().add(buttonToWrap);
        return panelField;
    }

    /**
     * Take the current style settings in our UI and create a LogConsoleTheme and
     * LogConsoleStyle out of them.
     */
    private void initializeLogConsole() {
        LogConsoleTheme theme = LogConsoleTheme.createMatrixStyledTheme();

        LogConsoleStyle style = new LogConsoleStyle();
        style.setLogToken(tokenField.getText(), true);
        style.setFontFamilyName(tokenFontField.getSelectedFont().getFamily());
        style.setFontPointSize(tokenFontField.getSelectedFont().getSize());
        style.setIsBold(tokenFontField.getSelectedFont().isBold());
        style.setIsItalic(tokenFontField.getSelectedFont().isItalic());
        style.setFontColor(tokenFontField.getTextColor());
        style.setFontBgColor(tokenFontField.getBgColor());
        theme.setStyle("swing-extras-custom-style", style);

        LogConsole.getInstance().registerTheme("swing-extras custom", theme);
    }

    /**
     * Invoked internally to log a test message with the given log level.
     */
    private void logMessage(String msg, Level level) {
        if (!formPanel.isFormValid()) {
            return;
        }
        String logMessage = level.getName() + ": " + msg + "\n";
        LogConsole.getInstance().append(logMessage, level);
    }
}
