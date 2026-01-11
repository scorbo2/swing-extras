package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.BooleanProperty;
import ca.corbett.extras.properties.ButtonProperty;
import ca.corbett.extras.properties.CollapsiblePanelProperty;
import ca.corbett.extras.properties.ColorProperty;
import ca.corbett.extras.properties.ComboProperty;
import ca.corbett.extras.properties.DirectoryProperty;
import ca.corbett.extras.properties.EnumProperty;
import ca.corbett.extras.properties.FileProperty;
import ca.corbett.extras.properties.FontProperty;
import ca.corbett.extras.properties.FormFieldGenerationListener;
import ca.corbett.extras.properties.HtmlLabelProperty;
import ca.corbett.extras.properties.IntegerProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.ListProperty;
import ca.corbett.extras.properties.ListSubsetProperty;
import ca.corbett.extras.properties.LongTextProperty;
import ca.corbett.extras.properties.PanelProperty;
import ca.corbett.extras.properties.PasswordProperty;
import ca.corbett.extras.properties.Properties;
import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.extras.properties.ShortTextProperty;
import ca.corbett.extras.properties.SliderProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.SliderField;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
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
 * This is very hard to demo, because it's HIGHLY configurable and will vary greatly
 * from application to application. Your application can define a list of AbstractProperty
 * instances and hand them to a PropertiesDialog to automatically create a UI for
 * those properties. You don't have to write any UI code at all to support this!
 * <p>
 *     To get an idea of the kinds of things you can do with PropertiesManager and PropertiesDialog,
 *     read through the buildProps() method in this class. It shows off some advanced
 *     capabilities for specifying dynamic properties for your application!
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-15
 */
public class PropertiesDemoPanel extends PanelBuilder {
    private static final Logger logger = Logger.getLogger(PropertiesDemoPanel.class.getName());

    private PropertiesManager propsManager;
    private ComboField<Alignment> alignmentField;
    private NumberField borderMarginField;

    /**
     * An example enum to show off EnumProperty.
     */
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
            propsFile.deleteOnExit();
            propsManager = new PropertiesManager(propsFile, buildProps(), "Test properties");
        }
        catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to create temp file for PropertiesManager.", ioe);
            propsManager = new PropertiesManager(new Properties(), buildProps(), "Test properties");
        }

        FormPanel formPanel = buildFormPanel("PropertiesManager");
        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>Almost every application exposes properties for application settings and<br>" +
                        "preferences to the user. But why rewrite the UI code for this for each<br>" +
                        "new application? What if there was a way to easily specify the properties for your<br>" +
                        "application in code, and have a PropertiesManager and a PropertiesDialog that<br>" +
                        "could just generate the UI for you? Well, there is!</html>", 14));

        alignmentField = new ComboField<>("Form alignment:", List.of(Alignment.values()), 1, false);
        formPanel.add(alignmentField);

        // New in swing-extras 2.7: we can control the borderMargin for generated form panels!
        borderMarginField = new NumberField("Border margin:", 16, 0, 64, 1);
        formPanel.add(borderMarginField);

        // We can use PanelField to wrap a button for launching the dialog:
        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Show dialog");
        btn.addActionListener(new LaunchDialogAction());
        panelField.getPanel().add(btn);
        formPanel.add(panelField);

        return formPanel;
    }

    /**
     * Invoked internally to build a huge list of AbstractProperty instances for demo purposes.
     * This shows off the capabilities that are available to applications that use this code!
     */
    private List<AbstractProperty> buildProps() {
        List<AbstractProperty> props = new ArrayList<>();

        // We can start with some simple LabelProperty instances.
        // These are handy for showing static text for informational purposes.
        // You can use html tags with br line breaks if you need a multi-line label, like this one:
        props.add(new LabelProperty("Intro.Overview.label1",
                                    "<html>All of the props on this dialog were generated in code."
                                            + "<br>No UI code was required to generate this dialog!</html>"));

        // BooleanProperty instances will equate to checkboxes on the generated form:
        props.add(new LabelProperty("Intro.Property types.label1",
                                    "There are many different property types to choose from."));
        props.add(new BooleanProperty("Intro.Property types.checkbox1",
                                      "Checkboxes"));
        props.add(new IntegerProperty("Intro.Property types.numberField1",
                                      "Number fields:", 10));
        props.add(new ComboProperty<>("Intro.Property types.combo1",
                                      "Comboboxes:",
                                      List.of("Option 1", "Option 2 (default)", "Option 3"),
                                      1,
                                      false));

        // New in swing-extras 2.6: let's show off ButtonProperty:
        ButtonProperty buttonProperty = new ButtonProperty("Intro.Property types.buttonProp",
                                                           "Button fields:");
        buttonProperty.addFormFieldGenerationListener(new ButtonPropertyFieldListener());
        props.add(buttonProperty);


        // Show some label capabilities:
        props.add(new LabelProperty("Intro.Labels.someLabelProperty",
                                    "Labels can be used to show static text."));
        LabelProperty testLabel = new LabelProperty("Intro.Labels.someLabelProperty2",
                                                    "You can set label font properties");
        testLabel.setFont(new Font("Monospaced", Font.ITALIC, 14));
        testLabel.setColor(LookAndFeelManager.getLafColor("text.highlight", Color.BLUE));
        props.add(testLabel);

        // New in swing-extras 2.6: let's show off HtmlLabelProperty:
        HtmlLabelProperty htmlLabel = new HtmlLabelProperty("Intro.Labels.htmlLabel1",
                                                            "<html>Labels can have hyperlinks: "
                                                                    + "<a href='link1'>link 1</a> "
                                                                    + "<a href='link2'>link 2</a></html>",
                                                            new HyperlinkActionHandler());
        props.add(htmlLabel);

        // Now add some dummy labels to force a scroll bar to appear:
        for (int i = 0; i < 10; i++) {
            props.add(new LabelProperty("Intro.Labels.scroll" + i, "Scroll down!"));
        }

        // And we can show a very long label with no line breaks to show horizontal scrolling as well:
        props.add(new LabelProperty("Intro.Labels.scrollSummary",
                                    "Long properties forms will automatically get scrollbars "
                                            + "(horizontal and vertical as needed) so you can scroll "
                                            + "to view everything - even long lines like this!"));

        // That's the end of the first tab!
        // But wait, how do we move to the next tab?
        // Notice that every property has a dotted name, like "Intro.Labels.scrollSummary"
        // The first component in that name is the name of the tab!
        // You can create new tabs at will by giving your properties a corresponding name!
        // For example, let's create a tab called "Colors" and put some ColorProperty instances on it:
        props.add(new ColorProperty("Colors.someSolidColor",
                                    "Solid color:",
                                    ColorSelectionType.SOLID)
                          .setSolidColor(Color.RED));
        props.add(new ColorProperty("Colors.someGradient",
                                    "Gradient:",
                                    ColorSelectionType.GRADIENT));
        props.add(new ColorProperty("Colors.someMultiColor",
                                    "Both:",
                                    ColorSelectionType.EITHER));

        // We'll put a FontProperty on this Colors tab also, because you can play with font colors:
        FontProperty fontProperty = new FontProperty("Colors.fontColor",
                                                     "Font with color:",
                                                     new Font(Font.SANS_SERIF, Font.PLAIN, 14),
                                                     Color.CYAN,
                                                     Color.DARK_GRAY);
        fontProperty.setAllowSizeSelection(false);
        props.add(fontProperty);

        // Now let's move on to the Files tab and show off file and directory choosers:
        props.add(new LabelProperty("Files.Overview.label1",
                                    "File and directory properties are easy to work with!"));

        // I want this checkbox to be able to affect the file choosers on this properties tab. But how?
        BooleanProperty showHidden = new BooleanProperty("Files.Examples.showHidden",
                                                         "Show hidden files",
                                                         false);

        // We can add a change listener to its generated FormField, even though it's not generated yet, that's how!
        showHidden.addFormFieldChangeListener(event -> {
            boolean isChecked = ((CheckBoxField)event.formField()).isChecked();

            // And in here we can look up other FormFields by id!
            List<String> ids = List.of("someDirProperty", "someFileProperty", "withImagePreview");
            for (String id : ids) {
                FileField field = (FileField)event.formPanel().getFormField("Files.Examples." + id);
                if (field != null) {
                    field.setFileHidingEnabled(!isChecked);
                }
            }
        });
        props.add(showHidden);

        // Now we can create properties for our file and directory choosers:
        props.add(new DirectoryProperty("Files.Examples.someDirProperty", "Directory:", true));
        props.add(new FileProperty("Files.Examples.someFileProperty", "File:", true));

        // For this file chooser, I want to include an image preview panel on the dialog. But how?
        FileProperty withImagePreview = new FileProperty("Files.Examples.withImagePreview",
                                                         "With image preview:",
                                                         true);

        // We can add a FormFieldGeneration listener, which lets us tweak the FormField when it gets created later!
        withImagePreview.addFormFieldGenerationListener((property, formField) -> {
            // FileField comes with a built-in image preview accessory that we can use:
            ((FileField)formField).setAccessory(new FileField.ImagePreviewAccessory());
        });
        props.add(withImagePreview);

        // Now let's look at text properties:
        props.add(new ShortTextProperty("Text.Single line.someTextProp1", "Text property1:", "hello"));
        props.add(new ShortTextProperty("Text.Single line.someTextProp2", "Text property2:", ""));
        props.add(new PasswordProperty("Text.Single line.password", "Password entry:")
                          .setPassword("password"));
        props.add(LongTextProperty.ofFixedSizeMultiLine("Text.Multi line.someMultiLineTextProp",
                                                        "Text entry:",
                                                        4,
                                                        30)
                                  .setValue("You can support long text as well.\n\nPop-out editing is optional.")
                                  .setAllowPopoutEditing(true));

        // By the way, you can add "hidden" properties by setting setExposed(false) on them.
        // Why would you do this? Your application may want to store and read configurable properties
        // without exposing them to the user. For example, application state such as window size and position.

        // This property is readable and settable by the client application, but it won't show up in the user dialog:
        props.add(new IntegerProperty("Hidden.someHiddenProp",
                                      "hiddenProp",
                                      77)
                          .setExposed(false));

        // Let's show off the list fields!
        List<String> listItems = List.of("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7",
                                         "Item 8");
        props.add(new ListProperty<String>("Lists.General.listField", "List field:")
                          .setItems(listItems)
                          .setVisibleRowCount(6)
                          .setFixedCellWidth(120));
        props.add(new ListSubsetProperty<>("Lists.General.listSubsetField",
                                           "List subset field:",
                                           listItems,
                                           new int[]{5, 6, 7})
                          .setVisibleRowCount(6)
                          .setFixedCellWidth(120));

        // Now let's show off sliders!
        // The SliderField in swing-forms is extremely customizable, much more so than a standard JSlider:
        SliderField.setIsDefaultBorderEnabled(false);
        props.add(new LabelProperty("Sliders.General.label",
                                    "<html>Sliders can sometimes be more useful than number spinners!"
                                            + "<br>And with custom labels on them, they can even replace comboboxes.</html>"));
        props.add(new SliderProperty("Sliders.General.slider1", "Default slider:", 0, 100, 50)
                          .setShowValueLabel(false));
        props.add(new SliderProperty("Sliders.General.slider2", "With value label:", 0, 100, 50));
        props.add(new SliderProperty("Sliders.General.slider3", "With colors!:", 0, 100, 50)
                          .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE)));
        props.add(new SliderProperty("Sliders.General.slider4", "Custom labels:", 0, 100, 50)
                          .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE))
                          .setLabels(List.of("Black", "Blue", "Cyan", "White"), false));
        props.add(new SliderProperty("Sliders.General.slider5", "Label + value:", 0, 100, 50)
                          .setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN))
                          .setLabels(List.of("Bad", "Meh", "Okay", "Good", "Great!", "FANTASTIC!"), true));

        // New in swing-extras 2.6, let's show off PanelProperty:
        PanelProperty panelProp = new PanelProperty("Panels.General.panelProp", new BorderLayout());
        panelProp.addFormFieldGenerationListener((property, formField) -> {
            PanelField panelField = (PanelField)formField;
            panelField.setShouldExpand(true);
            panelField.getPanel().setBorder(BorderFactory.createLoweredBevelBorder());

            FormPanel subForm = new FormPanel(Alignment.TOP_LEFT);
            subForm.setBorderMargin(12);
            subForm.add(new LabelField("This is a PanelProperty. You can add whatever static components you like."));
            subForm.add(new LabelField("Images, help text, whatever."));
            subForm.add(new LabelField("Just be aware that nothing here gets saved to properties."));
            panelField.getPanel().add(subForm, BorderLayout.CENTER);
        });
        props.add(panelProp);

        // New in swing-extras 2.6, let's show off CollapsiblePanelProperty:
        CollapsiblePanelProperty collapsiblePanelProp =
                new CollapsiblePanelProperty("Panels.General.collapsiblePanelProp",
                                             "Collapsible panel example",
                                             new BorderLayout());
        collapsiblePanelProp.addFormFieldGenerationListener((prop, formField) -> {
            CollapsiblePanelField panelField = (CollapsiblePanelField)formField;
            panelField.setShouldExpandHorizontally(true);

            FormPanel subForm = new FormPanel(Alignment.TOP_LEFT);
            subForm.setBorderMargin(12);
            subForm.add(new LabelField("You can also add collapsible panels as properties!"));
            subForm.add(new LabelField("Same rules as for regular panel properties."));
            panelField.getPanel().add(subForm, BorderLayout.CENTER);
        });
        props.add(collapsiblePanelProp);

        // And finally, we can show off EnumProperty, which is a handy way of generating combo boxes from enums:
        props.add(new LabelProperty("Enums.Enums.label1",
                                    "With EnumProperty, you can easily make combo boxes from enums!"));
        props.add(new EnumProperty<>("Enums.Enums.enumField1", "Choose:", TestEnum.VALUE1));
        props.add(new LabelProperty("Enums.Enums.label2",
                                    "Alternatively, you can use ComboProperty to show the enum names instead:"));

        // Prior to swing-extras 2.7, EnumProperty had the option of using name() instead of toString().
        // That option has been removed from EnumProperty, but you can still do it with ComboProperty if you like:
        List<TestEnum> enumValues = List.of(TestEnum.values());
        List<String> enumNames = new ArrayList<>();
        for (TestEnum val : enumValues) {
            enumNames.add(val.name());
        }
        props.add(new ComboProperty<>("Enums.Enums.enumField1_names",
                                      "Choose:",
                                      enumNames, 0, false));

        props.add(new LabelProperty("Enums.Enums.label3",
                                    "<html>EnumProperty is the better option, as your code deals natively<br>" +
                                            "with instances of your enum and the combo is generated for you!<br>" +
                                            "Also, the value saved to the properties file will be the enum name,<br>" +
                                            " in case the toString() changes over time or is localized to<br>" +
                                            " a different language.</html>")
                          .setFont(new Font(Font.DIALOG, Font.PLAIN, 12))
                          .setExtraMargins(8, 0));

        return props;
    }

    private static class HyperlinkActionHandler extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String message;
            if ("link1".equals(command)) {
                message = "You clicked link 1!";
            }
            else if ("link2".equals(command)) {
                message = "You clicked link 2!";
            }
            else {
                message = "Unknown link clicked: " + command;
            }
            JOptionPane.showMessageDialog(DemoApp.getInstance(), message);
        }
    }

    /**
     * A simple ActionListener for launching the properties dialog with our properties list.
     */
    private class LaunchDialogAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                propsManager.load();
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, "Couldn't load properties.", ex);
            }
            PropertiesDialog dialog = propsManager.generateDialog(DemoApp.getInstance(),
                                                                  "Test properties",
                                                                  alignmentField.getSelectedItem(),
                                                                  borderMarginField.getCurrentValue().intValue());
            dialog.setVisible(true);
            if (dialog.wasOkayed()) {
                propsManager.save();
            }
        }
    }

    /**
     * A FormFieldGenerationListener for our ButtonProperty demo.
     */
    private static class ButtonPropertyFieldListener implements FormFieldGenerationListener {
        @Override
        public void formFieldGenerated(AbstractProperty property, FormField formField) {
            ButtonField buttonField = (ButtonField)formField;
            buttonField.addButton(new AbstractAction("Button1") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked Button1!");
                }
            });
            buttonField.addButton(new AbstractAction("Button2") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked Button2!");
                }
            });
        }
    }
}
