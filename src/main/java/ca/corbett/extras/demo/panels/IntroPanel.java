package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.properties.LookAndFeelProperty;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;

import javax.swing.JPanel;
import java.awt.Font;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Builds a very simple FormPanel with some introductory text. This one isn't very interesting,
 * but it contains a nice easy introduction to the rest of the demo app.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
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
        FormPanel introPanel = buildFormPanel("Welcome to swing-extras!");

        // Multi-line labels are quite easy to generate by wrapping the text in html tags:
        String txt = "<html>This is a library of components and add-ons for Java Swing applications.<br>" +
                "Applications built with swing-extras can quickly and easily incorporate<br>" +
                "powerful functionality.</html>";
        introPanel.add(LabelField.createPlainHeaderLabel(txt, 14));

        txt = "<html>This demo application will show you just some of the many possibilities that this<br>" +
                "library has to offer. Refer to the full documentation and developer guide below for<br>" +
                "many more details!</html>";
        LabelField labelField = LabelField.createPlainHeaderLabel(txt, 14);
        introPanel.add(labelField);

        txt = "<html>swing-extras is licensed under the MIT license, which allows you to use it as you<br/>"
                + "wish, provided the copyright notices remain intact.</html>";
        labelField = LabelField.createPlainHeaderLabel(txt, 14);
        introPanel.add(labelField);

        labelField = LabelField.createPlainHeaderLabel("Important links and references:", 14);
        labelField.getMargins().setTop(12);
        introPanel.add(labelField);

        // We can use custom left margins to indent sub-fields like these.
        // We can also reduce the bottom margin to 0 to push the fields together vertically.
        labelField = new LabelField("Project home:", Version.PROJECT_URL);
        Font labelFont = LabelField.getDefaultLabelFont().deriveFont(14f); // make it a bit bigger
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48).setBottom(0); // indent and also group together vertically
        addHyperlinkIfUrlIsValid(labelField, Version.PROJECT_URL);
        introPanel.add(labelField);

        final String docUrl = "http://www.corbett.ca/swing-extras-book/";
        labelField = new LabelField("Documentation:", docUrl);
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48).setBottom(0); // indent and also group together vertically
        addHyperlinkIfUrlIsValid(labelField, docUrl);
        introPanel.add(labelField);

        final String javadocUrl = "http://www.corbett.ca/swing-extras-javadocs/";
        labelField = new LabelField("Javadocs:", javadocUrl);
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48).setBottom(0); // indent and also group together vertically
        addHyperlinkIfUrlIsValid(labelField, javadocUrl);
        introPanel.add(labelField);

        labelField = new LabelField("Copyright:", "2012-2025 Steve Corbett");
        labelField.getFieldLabel().setFont(labelFont);
        labelField.setFont(labelFont);
        labelField.getMargins().setLeft(48); // indent
        introPanel.add(labelField);

        // We can use LookAndFeelProperty to generate a FormField for us that we can use
        // to change the Look and Feel for the demo app. Wiring up this field is quite easy:
        LookAndFeelProperty lafProperty = new LookAndFeelProperty("", "Change look and feel:");
        //noinspection unchecked
        final ComboField<String> lafCombo = (ComboField<String>)lafProperty.generateFormField();
        lafCombo.addValueChangedListener(field -> {
            // Use selected value in the FormField to switch to the corresponding Look and Feel:
            LookAndFeelManager.switchLaf(lafProperty.getLafClass(lafCombo.getSelectedIndex()));
        });
        lafCombo.getMargins().setTop(38);
        introPanel.add(lafCombo);

        return introPanel;
    }

    /**
     * Invoked internally to set a hyperlink in the given LabelField, if the
     * given String URL is well-formed, and if browsing is supported in the current JRE.
     */
    private void addHyperlinkIfUrlIsValid(LabelField labelField, String url) {
        if (DemoApp.isBrowsingSupported() && DemoApp.isUrl(url)) {
            try {
                labelField.setHyperlink(new DemoApp.BrowseAction(URI.create(url)));
            }
            catch (IllegalArgumentException e) {
                logger.warning("Unable to hyperlink: URL is not well-formed: " + url);
            }
        }
        else {
            logger.warning("Unable to set label hyperlink - the current JRE does not support browsing.");
        }
    }
}
