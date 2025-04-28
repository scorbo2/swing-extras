package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.properties.LookAndFeelProperty;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
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
        FormPanel introPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);
        introPanel.setStandardLeftMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to swing-extras!", 24);
        label.setColor(LookAndFeelManager.getLafColor("text.highlight", Color.BLUE));
        introPanel.addFormField(label);

        String txt = "<html>This is a library of components for Java Swing application. This collection has<br>" +
                "been in development since around 2012, but was not publicly available until 2025.<br>" +
                "This documentation guide covers the possibilities that swing-extras offers that allow<br>" +
                "you to quickly and easily add useful functionality to your Java Swing applications.<br/></html>";
        introPanel.addFormField(LabelField.createPlainHeaderLabel(txt, 14));

        txt = "<html>This demo application will show you just some of the many possibilities that this<br>" +
                "library has to offer. Refer to the full documentation and developer guide below for<br>" +
                "many more details!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(txt, 14);
        label.setTopMargin(14);
        introPanel.addFormField(labelField);

        txt = "<html>swing-extras is licensed under the MIT license, which allows you to use it as you<br/>"
                + "wish, provided the copyright notices remain intact.</html>";
        labelField = LabelField.createPlainHeaderLabel(txt, 14);
        label.setTopMargin(14);
        introPanel.addFormField(labelField);

        labelField = LabelField.createPlainHeaderLabel("Important links and references:", 14);
        label.setTopMargin(12);
        introPanel.addFormField(labelField);

        labelField = new LabelField("Source code:", Version.PROJECT_URL);
        Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        labelField.setFieldLabelFont(labelFont);
        labelField.setFont(labelFont);
        labelField.setLeftMargin(48);
        labelField.setBottomMargin(0);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(Version.PROJECT_URL)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(Version.PROJECT_URL)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Project URL is not well-formed.");
            }
        }
        introPanel.addFormField(labelField);

        final String docUrl = "http://www.corbett.ca/swing-extras-book/";
        labelField = new LabelField("Documentation:", docUrl);
        labelField.setFieldLabelFont(labelFont);
        labelField.setFont(labelFont);
        labelField.setLeftMargin(48);
        labelField.setBottomMargin(0);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(docUrl)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(docUrl)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Documentation URL is not well-formed.");
            }
        }
        introPanel.addFormField(labelField);

        final String javadocUrl = "http://www.corbett.ca/swing-extras-javadoc/";
        labelField = new LabelField("Javadocs:", javadocUrl);
        labelField.setFieldLabelFont(labelFont);
        labelField.setFont(labelFont);
        labelField.setLeftMargin(48);
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(javadocUrl)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(javadocUrl)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Javadocs URL is not well-formed.");
            }
        }
        introPanel.addFormField(labelField);

        LookAndFeelProperty lafProperty = new LookAndFeelProperty("blah", "Change demo app look and feel:");
        final ComboField lafCombo = (ComboField)lafProperty.generateFormField();
        lafCombo.addValueChangedAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LookAndFeelManager.switchLaf(lafProperty.getLafClass(lafCombo.getSelectedIndex()));
            }
        });
        lafCombo.setTopMargin(38);
        introPanel.addFormField(lafCombo);

        introPanel.render();
        return introPanel;
    }
}
