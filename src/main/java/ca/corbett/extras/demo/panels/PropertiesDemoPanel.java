package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.BooleanProperty;
import ca.corbett.extras.properties.ColorProperty;
import ca.corbett.extras.properties.ComboProperty;
import ca.corbett.extras.properties.DirectoryProperty;
import ca.corbett.extras.properties.EnumProperty;
import ca.corbett.extras.properties.FileProperty;
import ca.corbett.extras.properties.FontProperty;
import ca.corbett.extras.properties.IntegerProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.extras.properties.TextProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demo panel to show off PropertiesManager and PropertiesDialog capabilities.
 *
 * @author scorbo2
 * @since 2025-03-15
 */
public class PropertiesDemoPanel extends PanelBuilder {
    private static final Logger logger = Logger.getLogger(PropertiesDemoPanel.class.getName());

    private PropertiesManager propsManager;

    public enum TestEnum {
        VALUE1("This is value 1"),
        VALUE2("This is value 2"),
        VALUE3("This is value 3");

        final String label;

        TestEnum(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @Override
    public String getTitle() {
        return "Properties";
    }

    @Override
    public JPanel build() {
        try {
            File propsFile = File.createTempFile("temp", ".props");
            propsManager = new PropertiesManager(propsFile, buildProps(), "Test properties");
        }
        catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to create temp file for PropertiesManager.", ioe);
            propsManager = new PropertiesManager(new Properties(), buildProps(), "Test properties");
        }

        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        final LabelField label = LabelField.createBoldHeaderLabel("PropertiesManager", 20);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(label);

        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>Almost every application exposes properties for application settings and<br>" +
                        "preferences to the user. But why rewrite the UI code for this for each<br>" +
                        "new application? What if there was a way to easily specify the properties for your<br>" +
                        "application in code, and have a PropertiesManager and a PropertiesDialog that<br>" +
                        "could just generate the UI for you? Well, there is!</html>", 14));

        final ComboField<Alignment> alignmentField = new ComboField<>("Form alignment:",
                                                                      List.of(Alignment.values()), 1, false);
        formPanel.add(alignmentField);

        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Show dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    propsManager.load();
                }
                catch (Exception ex) {
                    logger.log(Level.SEVERE, "Couldn't load properties.", ex);
                }
                PropertiesDialog dialog = propsManager.generateDialog(DemoApp.getInstance(), "Test properties",
                                                                      alignmentField.getSelectedItem(), 16);
                dialog.setVisible(true);
                if (dialog.wasOkayed()) {
                    propsManager.save();
                }
            }
        });
        panelField.getPanel().add(btn);
        formPanel.add(panelField);

        formPanel.render();
        return formPanel;
    }

    private List<AbstractProperty> buildProps() {
        List<AbstractProperty> props = new ArrayList<>();

        props.add(
                new LabelProperty("Intro.Overview.label1", "All of the props on this dialog were generated in code."));
        props.add(new LabelProperty("Intro.Overview.label2", "No UI code was required to generate this dialog!"));

        props.add(new BooleanProperty("Intro.Overview.checkbox1", "Property types correspond to form field types"));
        List<String> options = new ArrayList<>();
        options.add("Option 1");
        options.add("Option 2 (default)");
        options.add("Option 3");
        props.add(new ComboProperty<>("Intro.Overview.combo1", "ComboProperty:", options, 1, false));

        props.add(new LabelProperty("Intro.Labels.someLabelProperty", "You can add labels, too!"));
        LabelProperty testLabel = new LabelProperty("Intro.Labels.someLabelProperty2",
                                                    "You can set label font properties");
        testLabel.setFont(new Font("Monospaced", Font.ITALIC, 14));
        testLabel.setColor(LookAndFeelManager.getLafColor("text.highlight", Color.BLUE));
        props.add(testLabel);
        props.add(new LabelProperty("Intro.Labels.label3", "You can also add hidden properties."));
        for (int i = 0; i < 10; i++) {
            props.add(new LabelProperty("Intro.Labels.scroll" + i, "Scroll down!"));
        }
        props.add(new LabelProperty("Intro.Labels.scrollSummary",
                                    "Long properties forms will automatically get scrollbars (horizontal and vertical as needed) so you can scroll to view everything - even long lines like this!"));

        props.add(new ColorProperty("Colors.someSolidColor", "Solid color:", ColorProperty.ColorType.SOLID, Color.RED));
        props.add(new ColorProperty("Colors.someGradient", "Gradient:", ColorProperty.ColorType.GRADIENT));
        props.add(new ColorProperty("Colors.someMultiColor", "Both:", ColorProperty.ColorType.BOTH));
        FontProperty fontProperty = new FontProperty("Colors.fontColor", "Font with color:",
                                                     new Font(Font.SANS_SERIF, Font.PLAIN, 14), Color.CYAN,
                                                     Color.DARK_GRAY);
        fontProperty.setAllowSizeSelection(false);
        props.add(fontProperty);

        props.add(new DirectoryProperty("Files.someDirProperty", "Directory:"));
        props.add(new FileProperty("Files.someFileProperty", "File:"));

        props.add(new TextProperty("Text.Single line.someTextProp1", "Text property1:", "hello"));
        props.add(new TextProperty("Text.Single line.someTextProp2", "Text property2:", ""));
        props.add(new TextProperty("Text.Multi line.someMultiLineTextProp", "Text entry:",
                                   "You can support long text as well.", 40, 4));

        // This property is readable and settable by the client application but it won't show up in the user dialog:
        IntegerProperty hiddenProp = new IntegerProperty("Hidden.someHiddenProp", "hiddenProp", 77);
        hiddenProp.setExposed(false);
        props.add(hiddenProp);

        props.add(new LabelProperty("Enums.Enums.label1", "You can easily make combo boxes from enums!"));
        props.add(new EnumProperty<>("Enums.Enums.enumField1", "Choose:", TestEnum.VALUE1));
        props.add(new LabelProperty("Enums.Enums.label2",
                                    "Alternatively, you can use the enum names instead of toString():"));
        props.add(new EnumProperty<>("Enums.Enums.enumField2", "Choose:", TestEnum.VALUE1, true));

        String explanation = "<html>Either way, your code deals natively with instances of your enum<br>" +
                "and the combobox is generated for you! And either way,<br>" +
                "the value saved to the properties file will be the enum name,<br>" +
                " in case the toString() changes over time or is localized to<br>" +
                " a different language.</html>";
        LabelProperty label = new LabelProperty("Enums.Enums.label3", explanation);
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        label.setExtraMargins(8, 0);
        props.add(label);

        return props;
    }
}
