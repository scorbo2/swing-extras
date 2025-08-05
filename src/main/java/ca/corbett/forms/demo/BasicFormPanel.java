package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
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
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("Looking for basic Swing components? No problem!",
                                                                  20);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);

        formPanel.add(new TextField("Single-line text:", 15, 1, true));
        TextField textField = new TextField("Multi-line text:", 18, 4, true);
        textField.getMargins().setBottom(12);
        formPanel.add(textField);
        formPanel.add(new CheckBoxField("Checkboxes", true));
        formPanel.add(buildComboField());
        formPanel.add(new ColorField("Color chooser:", Color.BLUE));

        LabelField labelField = LabelField.createPlainHeaderLabel(
                "Header fields help to organize the form.");
        labelField.setFont(new Font("SansSerif", Font.BOLD, 18));
        labelField.getMargins().setTop(24).setBottom(18);
        formPanel.add(labelField);

        formPanel.add(new FileField("File chooser:", null, 15, FileField.SelectionType.ExistingFile));
        formPanel.add(
                new FileField("Directory chooser:", null, 15, FileField.SelectionType.ExistingDirectory));

        LabelField linkField = new LabelField("Hyperlink:", "Yes, you can add hyperlinks to your forms!");
        linkField.getMargins().setTop(10).setBottom(10);
        linkField.setHyperlink(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "You clicked the link! Hooray!");
            }
        });
        formPanel.add(linkField);

        formPanel.add(new NumberField("Number chooser:", 0, 0, 100, 1));

        ListField<String> listField1 = new ListField<>("Simple list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField1.setFixedCellWidth(80);
        listField1.setVisibleRowCount(3);
        formPanel.add(listField1);

        ListField<String> listField2 = new ListField<>("Wide list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField2.setLayoutOrientation(JList.VERTICAL_WRAP);
        listField2.setFixedCellWidth(80);
        listField2.setVisibleRowCount(3);
        formPanel.add(listField2);

        return formPanel;
    }

    private ComboField<String> buildComboField() {
        List<String> options = new ArrayList<>();
        options.add("Option 1");
        options.add("Option 2");
        options.add("Option 3");
        return new ComboField<>("Comboboxes:", options, 0, false);
    }
}
