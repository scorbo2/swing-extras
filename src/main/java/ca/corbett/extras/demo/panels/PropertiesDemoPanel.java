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
import ca.corbett.extras.properties.DecimalProperty;
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
import ca.corbett.extras.properties.PropertiesManager;
import ca.corbett.extras.properties.ShortTextProperty;
import ca.corbett.extras.properties.SliderProperty;
import ca.corbett.extras.properties.dialog.PropertiesDialog;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.SwingFormsResources;
import ca.corbett.forms.actions.ListItemClearAction;
import ca.corbett.forms.actions.ListItemMoveAction;
import ca.corbett.forms.actions.ListItemRemoveAction;
import ca.corbett.forms.demo.ListFieldPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;
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
    private ComboField<String> dialogTypeField;

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

        alignmentField = new ComboField<>("Form alignment:", List.of(Alignment.values()), 0, false);
        formPanel.add(alignmentField);

        // New in swing-extras 2.7: we can control the borderMargin for generated form panels!
        borderMarginField = new NumberField("Border margin:", 8, 0, 64, 1);
        formPanel.add(borderMarginField);

        // New in swing-extras 2.8: client applications can choose between the "classic" style of
        // properties dialog with tabs, or the new and sexy ActionPanel style of properties dialog with a
        // navigation panel on the left. Let's give the user the option to choose between these styles for this demo:
        dialogTypeField = new ComboField<>("Dialog type:",
                                           List.of("Classic (tabbed pane)", "New style (ActionPanel)"),
                                           0,
                                           false);
        formPanel.add(dialogTypeField);

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

        // Let's start with some introductory text explaining what the user is looking at:
        props.add(new LabelProperty("Overview.Overview.label1",
                                    "<html>Welcome to the auto-generated PropertiesDialog!<br><br>" +
                                            "100% of the properties that you will find on this dialog<br>" +
                                            "were generated in code using the built-in AbstractProperty<br>" +
                                            "subclasses and the PropertiesManager.<br><br>" +
                                            "Your application can do this too!<br>" +
                                            "<br>No UI code was required to generate this dialog!</html>"));

        String category = "Basic property types";
        props.addAll(buildBooleanProps(category, "Checkboxes"));
        props.addAll(buildNumberProps(category, "Number fields"));
        props.addAll(buildComboProps(category, "Comboboxes"));
        props.addAll(buildLabelProps(category, "Labels"));
        props.addAll(buildFontProps(category, "Font choosers"));
        props.addAll(buildTextProps(category, "Text fields"));

        category = "Advanced property types";
        props.addAll(buildColorProps(category, "Color choosers"));
        props.addAll(buildFileProps(category, "File choosers"));
        props.addAll(buildListProps(category, "List fields"));
        props.addAll(buildEnumProps(category, "Enum properties"));

        category = "Cool stuff";
        props.addAll(buildButtonProps(category, "Button properties"));
        props.addAll(buildSliderProps(category, "Sliders"));
        props.addAll(buildPanelProps(category, "Panel properties"));

        // By the way, you can add "hidden" properties by setting setExposed(false) on them.
        // Why would you do this? Your application may want to store and read configurable properties
        // without exposing them to the user. For example, application state such as window size and position.
        // This property is readable and settable by the client application, but it won't show up in the user dialog:
        props.add(new IntegerProperty("Hidden.someHiddenProp",
                                      "hiddenProp",
                                      77) // just an example
                          .setExposed(false));

        return props;
    }

    /**
     * Builds some checkboxes for our example properties dialog.
     */
    private List<AbstractProperty> buildBooleanProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new BooleanProperty(category + "." + subCategory + ".checkbox0",
                                      "Checkboxes are great for simple yes/no properties",
                                      true));
        props.add(new BooleanProperty(category + "." + subCategory + ".checkbox1",
                                      "They are also very easy to create!",
                                      true));
        props.add(new BooleanProperty(category + "." + subCategory + ".checkbox2",
                                      "Simply create a BooleanProperty instance",
                                      true));
        return props;
    }

    /**
     * Shows how to add numeric choosers to your properties dialog.
     */
    private List<AbstractProperty> buildNumberProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Number properties are rendered with IntegerProperty or DecimalProperty" +
                                            "<br>which both use NumberField as their generated form field." +
                                            "<br><br>You can set a minimum and maximum allowable value.</html>"));
        props.add(new IntegerProperty(category + "." + subCategory + ".number1",
                                      "Integer value:",
                                      42, 0, 100, 1));
        props.add(new DecimalProperty(category + "." + subCategory + ".number2",
                                      "Decimal value:",
                                      0.5, 0.0, 1.0, 0.1));
        return props;
    }

    /**
     * No properties dialog would be complete without comboboxes!
     */
    private List<AbstractProperty> buildComboProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Combo properties are rendered with ComboProperty, which uses" +
                                            "<br>ComboField as its generated form field." +
                                            "<br><br>You can set the options for the combo box with a list of strings." +
                                            "<br><br>The field can later be queried for its selected index, or" +
                                            "<br>for its selected value.</html>"));
        List<String> options = List.of("Option 1", "Option 2", "Option 3");
        props.add(new ComboProperty<>(category + "." + subCategory + ".combo1",
                                      "Choose an option:",
                                      options,
                                      0, false));
        props.add(new ComboProperty<>(category + "." + subCategory + ".combo2",
                                      "Choose or edit:",
                                      options,
                                      0, true));
        return props;
    }

    /**
     * Labels are very easy to work with in swing-extras, and there are a surprising
     * number of options for them.
     */
    private List<AbstractProperty> buildLabelProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Label properties are rendered with LabelProperty, which uses" +
                                            "<br>LabelField as its generated form field." +
                                            "<br><br>Label properties are great for showing static text on the dialog," +
                                            "<br>or for showing some dynamic text that gets updated based on other properties.</html>"));

        LabelProperty testLabel = new LabelProperty(category + "." + subCategory + ".someLabelProperty2",
                                                    "You can set label font properties");
        testLabel.setFont(new Font("Monospaced", Font.ITALIC, 14));
        testLabel.setColor(LookAndFeelManager.getLafColor("text.highlight", Color.BLUE));
        props.add(testLabel);

        // New in swing-extras 2.6: let's show off HtmlLabelProperty:
        HtmlLabelProperty htmlLabel = new HtmlLabelProperty(category + "." + subCategory + ".htmlLabel1",
                                                            "<html>Labels can have hyperlinks: "
                                                                    + "<a href='link1'>link 1</a> "
                                                                    + "<a href='link2'>link 2</a></html>",
                                                            new HyperlinkActionHandler());
        props.add(htmlLabel);

        // This seems pretty minor, but before swing-extras 2.7, you couldn't show a field label on a LabelProperty.
        LabelProperty labelWithFieldLabel = new LabelProperty(category + "." + subCategory + ".labelWithFieldLabel",
                                                              "This label shows its field label too.");
        labelWithFieldLabel.setFieldLabelText("Field label:");
        props.add(labelWithFieldLabel);

        // Now add some dummy labels to force a scroll bar to appear:
        for (int i = 0; i < 10; i++) {
            props.add(new LabelProperty(category + "." + subCategory + ".scroll" + i, "Scroll down!"));
        }

        // And we can show a very long label with no line breaks to show horizontal scrolling as well:
        props.add(new LabelProperty(category + "." + subCategory + ".scrollSummary",
                                    "Long properties forms will automatically get scrollbars "
                                            + "(horizontal and vertical as needed) so you can scroll "
                                            + "to view everything - even long lines like this!"));
        return props;
    }

    private List<AbstractProperty> buildFontProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Font properties are rendered with FontProperty, which uses" +
                                            "<br>FontField as its generated form field." +
                                            "<br><br>FontProperty can also allow choosing background" +
                                            "<br>and foreground colors!</html>"));

        FontProperty fontProp1 = new FontProperty(category + "." + subCategory + ".font1",
                                                  "Just the font:",
                                                  new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        fontProp1.setAllowSizeSelection(false);
        props.add(fontProp1);

        FontProperty fontProp2 = new FontProperty(category + "." + subCategory + ".font2",
                                                  "Font with size:",
                                                  new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        fontProp2.setAllowSizeSelection(true);
        props.add(fontProp2);

        FontProperty fontProp3 = new FontProperty(category + "." + subCategory + ".font3",
                                                  "With text color:",
                                                  new Font(Font.SANS_SERIF, Font.PLAIN, 14),
                                                  Color.MAGENTA);
        fontProp3.setAllowSizeSelection(true);
        props.add(fontProp3);

        FontProperty fontProp4 = new FontProperty(category + "." + subCategory + ".font4",
                                                  "With background:",
                                                  new Font(Font.SANS_SERIF, Font.PLAIN, 14),
                                                  Color.RED,
                                                  Color.YELLOW);
        fontProp4.setAllowSizeSelection(true);
        props.add(fontProp4);

        return props;
    }

    /**
     * Text fields are easy to work with, and have single and multi-line variants.
     */
    private List<AbstractProperty> buildTextProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Text properties are rendered with ShortTextProperty or LongTextProperty," +
                                            "<br>which use ShortTextField and LongTextField as their generated form fields." +
                                            "<br><br>Short text properties are for single-line text, and long text properties" +
                                            "<br>are for multi-line text. LongTextProperty has an option to allow pop-out editing!</html>"));
        props.add(new ShortTextProperty(category + "." + subCategory + ".shortText1",
                                        "Short text:",
                                        "Hello world!"));
        props.add(LongTextProperty.ofFixedSizeMultiLine(category + "." + subCategory + ".longText1",
                                                        "Long text:",
                                                        4,
                                                        30)
                                  .setValue("You can support long text as well.\n\nPop-out editing is optional.")
                                  .setAllowPopoutEditing(true));

        props.add(LabelProperty.createLabel(category + "." + subCategory + ".passwordLabel",
                                            "<html>A special kind of text property is PasswordProperty," +
                                                    "<br>which hides the text input in the generated " +
                                                    "form field by default.</html>"));
        props.add(new PasswordProperty(category + "." + subCategory + ".password1", "Password entry:")
                          .setPassword("password"));
        return props;
    }

    /**
     * Working with colors in swing-extras is super easy!
     */
    private List<AbstractProperty> buildColorProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Color properties are rendered with ColorProperty, which uses" +
                                            "<br>ColorField as its generated form field." +
                                            "<br><br>Solid color selection is of course supported, but in" +
                                            "<br>swing-extras, there is also first-class support for" +
                                            "<br>color gradients!</html>"));

        props.add(new ColorProperty(category + "." + subCategory + ".solidColor",
                                    "Solid color:",
                                    ColorSelectionType.SOLID).setSolidColor(Color.RED));
        props.add(new ColorProperty(category + "." + subCategory + ".gradientColor",
                                    "Gradient color:",
                                    ColorSelectionType.GRADIENT));
        props.add(new ColorProperty(category + "." + subCategory + ".eitherColor",
                                    "Either:",
                                    ColorSelectionType.EITHER));
        return props;
    }

    /**
     * File and directory choosers are supported. With file selection, you also can enable
     * an optional image preview accessory!
     */
    private List<AbstractProperty> buildFileProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>File properties are rendered with FileProperty or DirectoryProperty," +
                                            "<br>which use FileField as their generated form field." +
                                            "<br><br>FileProperty allows selection of files, while DirectoryProperty" +
                                            "<br>allows selection of directories. Both can be configured to allow" +
                                            "<br>multiple selection as well!</html>"));

        // I want this checkbox to be able to affect the file choosers on this properties tab. But how?
        BooleanProperty showHidden = new BooleanProperty(category + "." + subCategory + ".showHidden",
                                                         "Show hidden files",
                                                         false);

        // We can add a change listener to its generated FormField, even though it's not generated yet, that's how!
        showHidden.addFormFieldChangeListener(event -> {
            boolean isChecked = ((CheckBoxField)event.formField()).isChecked();

            // And in here we can look up other FormFields by id!
            List<String> ids = List.of("someDirProperty", "someFileProperty", "withImagePreview");
            for (String id : ids) {
                FileField field = (FileField)event.formPanel().getFormField(category + "." + subCategory + "." + id);
                if (field != null) {
                    field.setFileHidingEnabled(!isChecked);
                }
            }
        });
        props.add(showHidden);

        // Now we can create properties for our file and directory choosers:
        props.add(new DirectoryProperty(category + "." + subCategory + ".someDirProperty", "Directory:", true));
        props.add(new FileProperty(category + "." + subCategory + ".someFileProperty", "File:", true));

        // For this file chooser, I want to include an image preview panel on the dialog. But how?
        FileProperty withImagePreview = new FileProperty(category + "." + subCategory + ".withImagePreview",
                                                         "With image preview:",
                                                         true);

        // We can add a FormFieldGeneration listener, which lets us tweak the FormField when it gets created later!
        withImagePreview.addFormFieldGenerationListener((property, formField) -> {
            // FileField comes with a built-in image preview accessory that we can use:
            ((FileField)formField).setAccessory(new FileField.ImagePreviewAccessory());
        });
        props.add(withImagePreview);

        return props;
    }

    /**
     * There are many options for working with lists in swing-extras.
     */
    private List<AbstractProperty> buildListProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>List properties are rendered with ListProperty or ListSubsetProperty," +
                                            "<br>which use JList as their generated form field." +
                                            "<br><br>ListProperty allows selection of one or more items from the list." +
                                            "<br><br>ListSubsetProperty allows visual selection of a list subset." +
                                            "<br><br>Both properties have options for controlling the visible row " +
                                            "<br>count and fixed cell width of the generated JList.</html>"));

        List<String> listItems = List.of("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7",
                                         "Item 8");
        props.add(new ListProperty<String>(category + "." + subCategory + ".listField", "List field:")
                          .setItems(listItems)
                          .setVisibleRowCount(6)
                          .setFixedCellWidth(120));
        props.add(new ListSubsetProperty<>(category + "." + subCategory + ".listSubsetField",
                                           "List subset field:",
                                           listItems,
                                           new int[]{5, 6, 7})
                          .setVisibleRowCount(6)
                          .setFixedCellWidth(120));

        // New in swing-extras 2.7, let's show off adding action buttons to the ListField!
        ListProperty<String> listProp = new ListProperty<String>(category + "." + subCategory + ".listWithButtons",
                                                                 "With controls:")
                .setItems(listItems)
                .setVisibleRowCount(6)
                .setFixedCellWidth(120);
        listProp.setButtonPosition(ListField.ButtonPosition.BOTTOM);
        listProp.setButtonLayout(FlowLayout.CENTER, 2, 2);
        listProp.addFormFieldGenerationListener((property, formField) -> {
            @SuppressWarnings("unchecked") final ListField<String> listField = (ListField<String>)formField;
            listField.setButtonPanelBorder(BorderFactory.createLoweredBevelBorder());
            listField.addButton(new ListFieldPanel.ListItemAddAction(listField));
            listField.addButton(new ListItemMoveAction<>(SwingFormsResources.getMoveUpIcon(16),
                                                         listField, ListItemMoveAction.Direction.UP));
            listField.addButton(new ListItemMoveAction<>(SwingFormsResources.getMoveDownIcon(16),
                                                         listField, ListItemMoveAction.Direction.DOWN));
            listField.addButton(new ListItemRemoveAction(SwingFormsResources.getRemoveIcon(16), listField));
            listField.addButton(new ListItemClearAction(SwingFormsResources.getRemoveAllIcon(16), listField));
            listField.addButton(new ListFieldPanel.ListItemHelpAction());
        });
        props.add(listProp);

        return props;
    }

    /**
     * Enums previously required manual work with ComboProperty, but now you can use EnumProperty
     * and deal with native instances of your own enums!
     */
    private List<AbstractProperty> buildEnumProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Enum properties are rendered with EnumProperty, which uses" +
                                            "<br>ComboField as their generated form field." +
                                            "<br><br>Just give it a list of enum values and it will handle the rest!</html>"));

        props.add(new EnumProperty<>(category + "." + subCategory + ".enumField1", "Choose:", TestEnum.VALUE1));

        props.add(new LabelProperty(category + "." + subCategory + ".label2",
                                    "Alternatively, you can use ComboProperty to show the enum names instead:"));

        // Prior to swing-extras 2.7, EnumProperty had the option of using name() instead of toString().
        // That option has been removed from EnumProperty, but you can still do it with ComboProperty if you like:
        List<TestEnum> enumValues = List.of(TestEnum.values());
        List<String> enumNames = new ArrayList<>();
        for (TestEnum val : enumValues) {
            enumNames.add(val.name());
        }
        props.add(new ComboProperty<>(category + "." + subCategory + ".enumField1_names",
                                      "Choose:",
                                      enumNames, 0, false));

        props.add(new LabelProperty(category + "." + subCategory + ".label3",
                                    "<html>EnumProperty is the better option, as your code deals natively<br>" +
                                            "with instances of your enum and the combo is generated for you!<br>" +
                                            "Also, the value saved to the properties file will be the enum name,<br>" +
                                            " in case the toString() changes over time or is localized to<br>" +
                                            " a different language.</html>")
                          .setFont(new Font(Font.DIALOG, Font.PLAIN, 12))
                          .setExtraMargins(8, 0));
        return props;
    }

    /**
     * ButtonProperty lets you add buttons to your properties dialog that can perform
     * whatever action you like!
     */
    private List<AbstractProperty> buildButtonProps(String category, String subCategory) {
        // New in swing-extras 2.6: let's show off ButtonProperty:
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Button properties are rendered with ButtonProperty, which uses" +
                                            "<br>ButtonField as their generated form field." +
                                            "<br><br>Button properties are a great way to add buttons to your properties dialog" +
                                            "<br>that perform actions related to the properties. They don't have values like" +
                                            "<br>other properties, but they can still be very useful!</html>"));

        ButtonProperty buttonProp1 = new ButtonProperty(category + "." + subCategory + ".buttonProp1",
                                                        "Simple buttons:");
        buttonProp1.addFormFieldGenerationListener(new ButtonPropertyFieldListener());
        props.add(buttonProp1);

        ButtonProperty buttonProp2 = new ButtonProperty(category + "." + subCategory + ".buttonProp2",
                                                        "Styled buttons:");
        buttonProp2.addFormFieldGenerationListener(new ButtonPropertyWithStyleListener());
        props.add(buttonProp2);

        return props;
    }

    /**
     * Sliders are highly customizable in swing-extras, and are fun to use!
     */
    private List<AbstractProperty> buildSliderProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Slider properties are rendered with SliderProperty, which uses" +
                                            "<br>SliderField as their generated form field." +
                                            "<br><br>Sliders can sometimes be more useful than number spinners." +
                                            "<br>And with custom labels on them (optional), the can" +
                                            "<br>even replace comboboxes for some use cases!</html>"));

        SliderField.setIsDefaultBorderEnabled(false);
        props.add(new SliderProperty(category + "." + subCategory + ".slider1", "Default slider:", 0, 100, 50)
                          .setShowValueLabel(false));
        props.add(new SliderProperty(category + "." + subCategory + ".slider2", "With value label:", 0, 100, 50));
        props.add(new SliderProperty(category + "." + subCategory + ".slider3", "With colors!:", 0, 100, 50)
                          .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE)));
        props.add(new SliderProperty(category + "." + subCategory + ".slider4", "Custom labels:", 0, 100, 50)
                          .setColorStops(List.of(Color.BLACK, Color.BLUE, Color.CYAN, Color.WHITE))
                          .setLabels(List.of("Black", "Blue", "Cyan", "White"), false));
        props.add(new SliderProperty(category + "." + subCategory + ".slider5", "Label + value:", 0, 100, 50)
                          .setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN))
                          .setLabels(List.of("Bad", "Meh", "Okay", "Good", "Great!", "FANTASTIC!"), true));
        return props;
    }

    /**
     * PanelProperty and CollapsiblePanelProperty let you combine and contain multiple subcomponents
     * together into a single visual container. They are useful for grouping static fields, like labels,
     * together. And with CollapsiblePanelProperty, you can start them in their collapsed state to
     * save space on the form.
     */
    private List<AbstractProperty> buildPanelProps(String category, String subCategory) {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new LabelProperty(category + "." + subCategory + ".label1",
                                    "<html>Panel properties are rendered with PanelProperty, which uses" +
                                            "<br>FormPanel as their generated form field." +
                                            "<br><br>Panel properties are a great way to group static properties together" +
                                            "<br>on the dialog.</html>"));

        // New in swing-extras 2.6, let's show off PanelProperty:
        PanelProperty panelProp = new PanelProperty(category + "." + subCategory + ".panelProp", new BorderLayout());
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
                new CollapsiblePanelProperty(category + "." + subCategory + ".collapsiblePanelProp",
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

        return props;
    }

    /**
     * A very quick and dirty ActionListener for handling hyperlink clicks in our HtmlLabelProperty demo.
     */
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

            // Choosing between the two dialog styles is just this easy!
            boolean isClassic = dialogTypeField.getSelectedIndex() == 0;
            PropertiesDialog dialog = isClassic
                    ? propsManager.generateClassicDialog(DemoApp.getInstance(),
                                                         "Test properties",
                                                         true)
                    : propsManager.generateDialog(DemoApp.getInstance(),
                                                  "Test properties",
                                                  true);

            // There is some common configuration between the two styles:
            dialog.setAlignment(alignmentField.getSelectedItem());
            dialog.setBorderMargin(borderMarginField.getCurrentValue().intValue());
            dialog.setVisible(true);

            // If the dialog was okayed, we can save the changes to disk.
            // It actually works in this demo! Try hitting OK with changes and then
            // bring the dialog back up!
            if (dialog.wasOkayed()) {
                propsManager.updateFromDialog(dialog);
            }
        }
    }

    /**
     * Generates a very simple ButtonField for our properties dialog.
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

    private static class ButtonPropertyWithStyleListener implements FormFieldGenerationListener {

        @Override
        public void formFieldGenerated(AbstractProperty property, FormField formField) {
            ButtonField buttonField = (ButtonField)formField;
            buttonField.setAlignment(FlowLayout.CENTER);
            buttonField.setHgap(4);
            buttonField.setVgap(4);
            buttonField.getFieldComponent().setBorder(BorderFactory.createLoweredBevelBorder());
            buttonField.addButton(new AbstractAction(null, SwingFormsResources.getMoveAllLeftIcon(48)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(DemoApp.getInstance(), "These buttons are just examples...");
                }
            });
            buttonField.addButton(new AbstractAction(null, SwingFormsResources.getMoveAllRightIcon(48)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(DemoApp.getInstance(), "These buttons are just examples...");
                }
            });
        }
    }
}
