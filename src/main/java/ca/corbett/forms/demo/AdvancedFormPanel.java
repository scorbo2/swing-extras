package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.CollapsiblePanelField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ListField;
import ca.corbett.forms.fields.SliderField;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;

public class AdvancedFormPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Forms: advanced fields";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        LabelField headerLabel = LabelField.createBoldHeaderLabel("And now for some more advanced examples...",
                                                                  20, 0, 8);
        headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> headerLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(headerLabel);

        formPanel.add(LabelField.createBoldHeaderLabel("Sliders are sometimes more interesting than number spinners!", 14));
        formPanel.add(new SliderField("Standard slider:", 0, 100, 50).setShowValueLabel(false));
        formPanel.add(new SliderField("With value label:", 0, 100, 50).setShowValueLabel(true));
        formPanel.add(new SliderField("Custom colors!:", 0, 100, 50)
                              .setColorStops(List.of(Color.RED, Color.YELLOW, Color.GREEN))
                              .setLabels(List.of("Very low", "Low", "Medium", "High", "Very high"), true));

        formPanel.add(LabelField.createBoldHeaderLabel("Panel fields", 14, 16, 8));
        CollapsiblePanelField panelField = new CollapsiblePanelField("Panel fields are great for grouping components together!",
                                                                     true,
                                                                     new BorderLayout());
        panelField.setShouldExpandHorizontally(true);
        FormPanel miniFormPanel = new FormPanel(Alignment.TOP_LEFT);
        miniFormPanel.getBorderMargin().setLeft(24);
        miniFormPanel.add(new CheckBoxField("Example field 1", true));
        miniFormPanel.add(new CheckBoxField("Example field 2", true));
        miniFormPanel.add(new ComboField<String>("Select:",
                                                 List.of("These fields belong together",
                                                         "You can collapse this panel!",
                                                         "That hides these grouped fields."),
                                                 0));
        panelField.getPanel().add(miniFormPanel, BorderLayout.CENTER);
        formPanel.add(panelField);

        panelField = new CollapsiblePanelField("Panels can be collapsed by default!",
                                                                                false,
                                                                                new FlowLayout(FlowLayout.LEFT));
        panelField.setShouldExpandHorizontally(true);
        panelField.getPanel().add(new JLabel("    Told you so!"));
        formPanel.add(panelField);

        formPanel.add(LabelField.createBoldHeaderLabel("List fields are supported too!", 14, 24, 8));
        ListField<String> listField1 = new ListField<>("Simple list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField1.setFixedCellWidth(80);
        listField1.setVisibleRowCount(4);
        formPanel.add(listField1);

        ListField<String> listField2 = new ListField<>("Wide list:",
                                                       List.of("One", "Two", "Three", "Four", "Five", "Six"));
        listField2.setLayoutOrientation(JList.VERTICAL_WRAP);
        listField2.setFixedCellWidth(80);
        listField2.setVisibleRowCount(3);
        formPanel.add(listField2);


        return formPanel;
    }
}
