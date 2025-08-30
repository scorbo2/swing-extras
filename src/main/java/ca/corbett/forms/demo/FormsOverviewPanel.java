package ca.corbett.forms.demo;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2029-11-25
 */
public class FormsOverviewPanel extends PanelBuilder {

    @Override
    public String getTitle() {
        return "Forms: overview";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel(Alignment.TOP_LEFT);
        introPanel.setBorderMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to swing-forms!", 24, 0, 8);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        introPanel.add(label);

        introPanel.add(
                LabelField.createPlainHeaderLabel("<html>With swing-forms, you can <b>easily</b> and <b>quickly</b> " +
                                                          "build out forms without having<br>to write a bunch of <i>GridBagLayout</i> code!<br><br>" +
                                                          "Most forms contain fields consisting of labels and components:</html>",
                                                  14));

        ShortTextField exampleField = new ShortTextField("Example text field:", 12);
        exampleField.setText("hello");
        exampleField.getMargins().setLeft(24).setTop(16).setBottom(16);
        introPanel.add(exampleField);

        LabelField field = LabelField.createPlainHeaderLabel(
                "<html>But you aren't limited to simple boring form fields! You can " +
                        "lay out and render<br>fields with custom validation and you can also " +
                        "specify custom actions<br>that occur when form fields change.</html>", 14);
        field.getMargins().setBottom(0);
        introPanel.add(field);

        field = LabelField.createBoldHeaderLabel("Even this overview panel was rendered with swing-forms!", 14);
        field.setColor(Color.MAGENTA);
        field.getMargins().setTop(14).setBottom(14);
        introPanel.add(field);

        introPanel.add(
                LabelField.createPlainHeaderLabel("Visit the other tabs to explore swing-forms features and usage!",
                                                  14));

        return introPanel;
    }
}
