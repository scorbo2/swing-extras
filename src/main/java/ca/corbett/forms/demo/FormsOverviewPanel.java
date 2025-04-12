package ca.corbett.forms.demo;

import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class FormsOverviewPanel extends PanelBuilder {

    @Override
    public String getTitle() {
        return "Forms: overview";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        introPanel.setStandardLeftMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to swing-forms!", 24);
        label.setColor(Color.BLUE);
        introPanel.addFormField(label);

        introPanel.addFormField(
                LabelField.createPlainHeaderLabel("<html>With swing-forms, you can <b>easily</b> and <b>quickly</b> " +
                                                          "build out forms without having<br>to write a bunch of <i>GridBagLayout</i> code!<br><br>" +
                                                          "Most forms contain fields consisting of labels and components:</html>",
                                                  14));

        ca.corbett.forms.fields.TextField exampleField = new TextField("Example text field:", 12, 1, true);
        exampleField.setText("hello");
        exampleField.setLeftMargin(24);
        exampleField.setTopMargin(16);
        exampleField.setBottomMargin(16);
        introPanel.addFormField(exampleField);

        LabelField field = LabelField.createPlainHeaderLabel(
                "<html>But you aren't limited to simple boring form fields! You can " +
                        "lay out and render<br>fields with custom validation and you can also " +
                        "specify custom actions<br>that occur when form fields change.</html>", 14);
        field.setBottomMargin(0);
        introPanel.addFormField(field);

        field = LabelField.createBoldHeaderLabel("Even this overview panel was rendered with swing-forms!", 14);
        field.setColor(Color.MAGENTA);
        field.setTopMargin(14);
        field.setBottomMargin(14);
        introPanel.addFormField(field);

        introPanel.addFormField(
                LabelField.createPlainHeaderLabel("Visit the other tabs to explore swing-forms features and usage!",
                                                  14));

        introPanel.render();
        return introPanel;
    }
}
