package ca.corbett.forms.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author scorbo2
 * @since 2029-11-25
 */
public class IntroPanel extends PanelBuilder {

    @Override
    public String getTitle() {
        return "Intro";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel();

        LabelField label = new LabelField("Welcome to swing-forms!");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setColor(Color.BLUE);
        label.setTopMargin(24);
        label.setBottomMargin(16);
        introPanel.addFormField(label);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>With swing-forms, you can <b>easily</b> and <b>quickly</b> ");
        sb.append("build out forms<br/>without having to write a bunch of <i>GridBagLayout</i> code!<br/>");
        sb.append("Most forms contain fields consisting of labels and components:</html>");
        introPanel.addFormField(LabelField.createPlainHeaderLabel(sb.toString()));

        ca.corbett.forms.fields.TextField exampleField = new TextField("Example text field:", 12, 1, true);
        exampleField.setText("hello");
        exampleField.setLeftMargin(24);
        exampleField.setTopMargin(16);
        exampleField.setBottomMargin(16);
        introPanel.addFormField(exampleField);

        sb = new StringBuilder();
        sb.append("<html>But you aren't limited to simple boring form fields! You can <br/>");
        sb.append("lay out and render fields with custom validation and you can also<br/>");
        sb.append("specify custom actions that occur when form fields change.</html>");
        LabelField field = LabelField.createPlainHeaderLabel(sb.toString());
        field.setBottomMargin(0);
        introPanel.addFormField(field);

        field = new LabelField("Even this intro panel was rendered with swing-forms!");
        field.setFont(new Font("SansSerif", Font.BOLD, 12));
        field.setColor(Color.MAGENTA);
        field.setTopMargin(12);
        field.setBottomMargin(12);
        introPanel.addFormField(field);

        introPanel.addFormField(
                LabelField.createPlainHeaderLabel("Visit the other tabs to explore swing-forms features and usage!"));

        introPanel.render();
        return introPanel;
    }
}
