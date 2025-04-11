package ca.corbett.extras.demo.panels;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;

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
        return "Intro";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel();

        LabelField label = createSimpleLabelField("Welcome to swing-extras!");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setColor(Color.BLUE);
        label.setTopMargin(24);
        label.setBottomMargin(16);
        introPanel.addFormField(label);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>This is a library of custom components and widgets for<br/>");
        sb.append("Java Swing that I have been slowly building for years. These were<br/>");
        sb.append("originally part of the sc-util project, which was never published.<br/>");
        sb.append("In 2025, sc-util became <b>swing-extras</b> and was made public.</html>");
        introPanel.addFormField(createSimpleLabelField(sb.toString()));

        sb = new StringBuilder();
        sb.append("<html>Take a look through the other tabs in this demo application<br/>");
        sb.append("to take a tour of some of the possibilities in this library.</html>");
        LabelField labelField = createSimpleLabelField(sb.toString());
        label.setTopMargin(14);
        introPanel.addFormField(labelField);

        sb = new StringBuilder();
        sb.append("<html>You can also browse through the code and javadocs for more!</html>");
        labelField = createSimpleLabelField(sb.toString());
        label.setTopMargin(12);
        label.setBottomMargin(12);
        introPanel.addFormField(labelField);

        introPanel.render();
        return introPanel;
    }
}
