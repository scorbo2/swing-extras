package ca.corbett.extras.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author scorbo2
 * @since 2025-03-11
 */
public class IntroPanel extends PanelBuilder {
    @Override
    public String getTitle() {
        return "Introduction";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        introPanel.setStandardLeftMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to swing-extras!", 24);
        label.setColor(Color.BLUE);
        introPanel.addFormField(label);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>This is a library of custom components and widgets for Java Swing that I have<br/>");
        sb.append("been building for years. These were originally part of the sc-util project, which<br/>");
        sb.append("was never published. In 2025, sc-util became <b>swing-extras</b> and was made public.<br/></html>");
        introPanel.addFormField(LabelField.createPlainHeaderLabel(sb.toString(), 14));

        sb = new StringBuilder();
        sb.append("<html>Take a look through the other tabs in this demo application to take a tour of some<br/>");
        sb.append("of the possibilities in this library.</html>");
        LabelField labelField = LabelField.createPlainHeaderLabel(sb.toString(), 14);
        label.setTopMargin(14);
        introPanel.addFormField(labelField);

        sb = new StringBuilder();
        sb.append("<html>You can also browse through the code and javadocs for more!</html>");
        labelField = LabelField.createPlainHeaderLabel(sb.toString(), 14);
        label.setTopMargin(12);
        label.setBottomMargin(12);
        introPanel.addFormField(labelField);

        introPanel.render();
        return introPanel;
    }
}
