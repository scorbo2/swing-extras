package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.logging.LogConsole;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.extras.logging.LogConsoleTheme;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.FontField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

public class LogConsolePanel extends PanelBuilder {

    private FormPanel formPanel;
    private TextField tokenField;
    private FontField tokenFontField;

    public LogConsolePanel() {
    }

    @Override
    public String getTitle() {
        return "LogConsole";
    }

    @Override
    public JPanel build() {
        formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        final LabelField label = LabelField.createBoldHeaderLabel("LogConsole", 24);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(label);

        LabelField labelField = LabelField.createPlainHeaderLabel(
                "<html>We can create a custom live-updated view of a log file with<br>" +
                        "configurable styles that can decide how to render the<br>" +
                        "log output based on string tokens within the log message.<br><br>" +
                        "This can make it visually easy to see what's going on!", 14);
        formPanel.add(labelField);

        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        panelField.getMargins().setLeft(32);
        JButton button = generateButton("Show LogConsole");
        button.addActionListener(e -> LogConsole.getInstance().setVisible(true));
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        labelField = LabelField.createPlainHeaderLabel("Regular log messages will use whatever log theme is selected.",
                                                       14);
        labelField.getMargins().setTop(24);
        formPanel.add(labelField);

        panelField = new PanelField();
        panelField.getMargins().setLeft(32).setTop(0).setBottom(0);
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        button = generateButton("Generate INFO message");
        button.addActionListener(e -> logMessage("This is a regular info log message.", Level.INFO));
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        panelField = new PanelField();
        panelField.getMargins().setLeft(32).setTop(0).setBottom(0);
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        button = generateButton("Generate WARNING message");
        button.addActionListener(e -> logMessage("This is a regular warning log message.", Level.WARNING));
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        panelField = new PanelField();
        panelField.getMargins().setLeft(32).setTop(0);
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        button = generateButton("Generate SEVERE message");
        button.addActionListener(e -> logMessage("This is a regular SEVERE log message.", Level.SEVERE));
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        labelField = LabelField.createPlainHeaderLabel("We can create a custom swing-extras log theme!", 14);
        labelField.getMargins().setTop(24);
        formPanel.add(labelField);

        tokenField = TextField.ofSingleLine("Messages containing:", 20).setAllowBlank(false);
        tokenField.setText("My custom message");
        formPanel.add(tokenField);

        tokenFontField = new FontField("Will look like this:", new Font(Font.MONOSPACED, Font.BOLD, 12), Color.WHITE,
                                       Color.BLACK);
        //TODO this was kind of a nice feature... shame to lose it: tokenFontField.setShowValidationLabel(false);
        tokenFontField.setShowSizeField(false);
        formPanel.add(tokenFontField);

        panelField = new PanelField();
        panelField.getMargins().setLeft(32).setTop(0);
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        button = generateButton("Try it!");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeLogConsole();
                logMessage(tokenField.getText(), Level.INFO);
            }
        });
        panelField.getPanel().add(button);
        formPanel.add(panelField);

        initializeLogConsole();

        return formPanel;
    }

    private JButton generateButton(String caption) {
        JButton button = new JButton(caption);
        button.setPreferredSize(new Dimension(220, 26));
        button.setFont(button.getFont().deriveFont(Font.PLAIN));
        return button;
    }

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
        theme.setStyle("customStyle", style);

        LogConsole.getInstance().registerTheme("swing-extras custom", theme);
    }

    private void logMessage(String msg, Level level) {
        if (!formPanel.isFormValid()) {
            return;
        }
        String logMessage = level.getName() + ": " + msg + "\n";
        LogConsole.getInstance().append(logMessage, level);
    }
}
