package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FileField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.TextField;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a FormPanel that contains an example of each of the basic form field types.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class BasicFormPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: The basics";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        formPanel.setStandardLeftMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("Looking for basic Swing components? No problem!",
                                                                  20);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.addFormField(headerLabel);

        formPanel.addFormField(new TextField("Single-line text:", 15, 1, true));
        TextField textField = new TextField("Multi-line text:", 18, 4, true);
        textField.setAddScrollPaneWhenMultiLine(false);
        textField.setBottomMargin(12);
        formPanel.addFormField(textField);
        formPanel.addFormField(new CheckBoxField("Checkboxes", true));
        formPanel.addFormField(buildComboField());
        formPanel.addFormField(new ColorField("Color chooser:", Color.BLUE));

        LabelField labelField = LabelField.createPlainHeaderLabel(
                "Header fields help to organize the form.");
        labelField.setFont(new Font("SansSerif", Font.BOLD, 18));
        labelField.setTopMargin(24);
        labelField.setBottomMargin(18);
        formPanel.addFormField(labelField);

        formPanel.addFormField(new FileField("File chooser:", null, 15, FileField.SelectionType.ExistingFile));
        formPanel.addFormField(
                new FileField("Directory chooser:", null, 15, FileField.SelectionType.ExistingDirectory));

        LabelField linkField = new LabelField("Hyperlink:", "Yes, you can add hyperlinks to your forms!");
        linkField.setTopMargin(10);
        linkField.setBottomMargin(10);
        linkField.setHyperlink(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked the link! Hooray!");
            }
        });
        formPanel.addFormField(linkField);

        formPanel.addFormField(new NumberField("Number chooser:", 0, 0, 100, 1));

        ListField<String> listField1 = new ListField<>("Simple list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField1.setFixedCellWidth(80);
        listField1.setVisibleRowCount(3);
        formPanel.addFormField(listField1);

        ListField<String> listField2 = new ListField<>("Wide list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField2.setLayoutOrientation(JList.VERTICAL_WRAP);
        listField2.setFixedCellWidth(80);
        listField2.setVisibleRowCount(3);
        formPanel.addFormField(listField2);

        formPanel.render();
        return formPanel;
    }

    private ComboField buildComboField() {
        List<String> options = new ArrayList<>();
        options.add("Option 1");
        options.add("Option 2");
        options.add("Option 3");
        return new ComboField("Comboboxes:", options, 0, false);
    }
}
