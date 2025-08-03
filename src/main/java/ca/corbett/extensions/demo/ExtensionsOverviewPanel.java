package ca.corbett.extensions.demo;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extensions.ui.ExtensionManagerDialog;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.TextProperty;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class ExtensionsOverviewPanel extends PanelBuilder {

    FakeExtensionManager<AppExtension> extManager;

    public ExtensionsOverviewPanel() {
        extManager = new FakeExtensionManager<>();
        extManager.addExtension(new FakeExtension1(), true);
        extManager.addExtension(new FakeExtension2(), true);
        extManager.addExtension(new FakeExtension3(), true);
    }

    @Override
    public String getTitle() {
        return "Extensions: overview";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        LabelField label = LabelField.createBoldHeaderLabel("Welcome to app-extensions!", 24);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.addFormField(label);

        String txt = "<html>ExtensionManager provides a way to allow your applications to be extended<br>" +
                "at runtime by providing extension classes in jar files that can be dynamically<br>" +
                "interrogated and loaded. These extensions can take advantage of whatever<br>" +
                "extension points you decide to add to your application!</html>";
        formPanel.addFormField(LabelField.createPlainHeaderLabel(txt, 14));

        txt = "<html>ExtensionManager comes with a built-in dialog that your users can use<br>" +
                "to manage extensions at runtime, or to enable and disable them.<br>" +
                "Try the simulated ExtensionManagerDialog below for an example:</html>";
        formPanel.addFormField(LabelField.createPlainHeaderLabel(txt, 14));

        PanelField field = new PanelField();
        JPanel panel = field.getPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Launch simulated ExtensionManagerDialog");
        button.addActionListener(e -> showExtensionDialog());
        panel.add(button);
        formPanel.addFormField(field);

        formPanel.render();
        return formPanel;
    }

    private void showExtensionDialog() {

        ExtensionManagerDialog<AppExtension> dialog = new ExtensionManagerDialog<>(extManager, DemoApp.getInstance());
        dialog.setVisible(true);
    }

    private static class FakeExtensionManager<AppExtension>
            extends ExtensionManager<ca.corbett.extensions.AppExtension> {

    }

    private static class FakeExtension1 extends AppExtension {

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Example extension 1")
                    .setVersion("1.0")
                    .setShortDescription("An example extension")
                    .setAuthor("Steve Corbett")
                    .setLongDescription("This is just an example extension that doesn't actually do anything. " +
                                                "It's just here to show the ExtensionManagerDialog. " +
                                                "In an actual application, you can have as many extensions " +
                                                "and extension points as you want!\n\n" +
                                                "This dialog and the ability to enable/disable extensions is " +
                                                "provided out of the box and requires no UI code!")
                    .setReleaseNotes("example")
                    .build();
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            return List.of();
        }
    }

    private static class FakeExtension2 extends AppExtension {

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Example extension 2")
                    .setVersion("1.0")
                    .setShortDescription("Another example extension")
                    .setAuthor("Steve Corbett")
                    .setLongDescription("The ExtensionManagerDialog is built dynamically " +
                                                "based on the extensions that are found and loaded when " +
                                                "your application starts up.")
                    .setReleaseNotes("example")
                    .build();
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            return List.of();
        }
    }

    private static class FakeExtension3 extends AppExtension {

        @Override
        public AppExtensionInfo getInfo() {
            return new AppExtensionInfo.Builder("Example extension 3")
                    .setVersion("1.0")
                    .setShortDescription("Final example")
                    .setAuthor("Steve Corbett")
                    .setLongDescription("It's hard to demonstrate ExtensionManager because it can be extended " +
                                                "in so many ways to accomplish almost anything! It might be " +
                                                "better to look at some of the applications that have been " +
                                                "built with it, such as musicplayer or imageviewer or snotes " +
                                                "or tasktracker, etc.")
                    .setReleaseNotes("example")
                    .build();
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            List<AbstractProperty> props = new ArrayList<>();
            props.add(new LabelProperty("FakeExtension3.General.label1",
                                        "This extension defines some config properties"));
            props.add(new TextProperty("FakeExtension3.General.text1", "Text field 1:", "Default value"));
            props.add(new TextProperty("FakeExtension3.General.text2", "Text field 2:", "Hello"));
            props.add(new LabelProperty("FakeExtension3.General.label2", "You can view the defaults here."));
            return props;
        }
    }
}
