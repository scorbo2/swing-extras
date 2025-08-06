package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.properties.LookAndFeelProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author scorbo2
 * @since 2025-03-11
 */
public class IntroPanel extends PanelBuilder {

    private static final Logger logger = Logger.getLogger(IntroPanel.class.getName());

    @Override
    public String getTitle() {
        return "Introduction";
    }

    @Override
    public JPanel build() {
        FormPanel introPanel = new FormPanel(Alignment.TOP_LEFT);
        introPanel.setBorderMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to swing-extras!", 24);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        introPanel.add(label);

        String txt = "<html>This is a library of components for Java Swing application. This collection has<br>" +
                "been in development since around 2012, but was not publicly available until 2025.<br>" +
                "This documentation guide covers the possibilities that swing-extras offers that allow<br>" +
                "you to quickly and easily add useful functionality to your Java Swing applications.<br/></html>";
        introPanel.add(LabelField.createPlainHeaderLabel(txt, 14));

        txt = "<html>This demo application will show you just some of the many possibilities that this<br>" +
                "library has to offer. Refer to the full documentation and developer guide below for<br>" +
                "many more details!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(txt, 14);
        label.getMargins().setTop(14);
        introPanel.add(labelField);

        txt = "<html>swing-extras is licensed under the MIT license, which allows you to use it as you<br/>"
                + "wish, provided the copyright notices remain intact.</html>";
        labelField = LabelField.createPlainHeaderLabel(txt, 14);
        label.getMargins().setTop(14);
        introPanel.add(labelField);

        labelField = LabelField.createPlainHeaderLabel("Important links and references:", 14);
        label.getMargins().setTop(12);
        introPanel.add(labelField);

        labelField = new LabelField("Source code:", Version.PROJECT_URL);
        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48);
        labelField.getMargins().setBottom(0);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(Version.PROJECT_URL)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(Version.PROJECT_URL)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Project URL is not well-formed.");
            }
        }
        introPanel.add(labelField);

        final String docUrl = "http://www.corbett.ca/swing-extras-book/";
        labelField = new LabelField("Documentation:", docUrl);
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48);
        labelField.getMargins().setBottom(0);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(docUrl)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(docUrl)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Documentation URL is not well-formed.");
            }
        }
        introPanel.add(labelField);

        final String javadocUrl = "http://www.corbett.ca/swing-extras-javadoc/";
        labelField = new LabelField("Javadocs:", javadocUrl);
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(javadocUrl)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(javadocUrl)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Javadocs URL is not well-formed.");
            }
        }
        introPanel.add(labelField);

        LookAndFeelProperty lafProperty = new LookAndFeelProperty("blah", "Change demo app look and feel:");
        //noinspection unchecked
        final ComboField<String> lafCombo = (ComboField<String>)lafProperty.generateFormField();
        lafCombo.addValueChangedListener(field -> {
            LookAndFeelManager.switchLaf(lafProperty.getLafClass(lafCombo.getSelectedIndex()));
        });
        lafCombo.getMargins().setTop(38);
        introPanel.add(lafCombo);

        return introPanel;
    }
}
