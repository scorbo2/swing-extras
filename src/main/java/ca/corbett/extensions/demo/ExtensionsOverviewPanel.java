package ca.corbett.extensions.demo;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extensions.ui.ExtensionManagerDialog;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.demo.panels.PanelBuilder;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.ShortTextProperty;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * A demo panel to show off some of what ExtensionManager can do.
 * This is a very difficult feature to demo! ExtensionManager, by design,
 * is extremely customizable, and through extensions, you can add almost
 * any additional functionality to an existing application without changing
 * the application code itself.
 * <p>
 * This demo panel focuses mostly on the ExtensionManagerDialog, which
 * gives your application a free UI to show your extensions to the user,
 * and to allow them to be toggled on or off. For the purpose of this demo,
 * we create and present three "fake" extensions that don't actually do anything.
 * They only exist so that you can get a feel for what the ExtensionManagerDialog
 * might look like in your application.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
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
        FormPanel formPanel = buildFormPanel("Welcome to app-extensions!");

        // We'll just throw a couple of informational LabelFields here to
        // explain what this is about.
        String txt = "<html>ExtensionManager provides a way to allow your applications to be extended<br>" +
                "at runtime by providing extension classes in jar files that can be dynamically<br>" +
                "interrogated and loaded. These extensions can take advantage of whatever<br>" +
                "extension points you decide to add to your application!</html>";
        formPanel.add(LabelField.createPlainHeaderLabel(txt, 14));

        txt = "<html>ExtensionManager comes with a built-in dialog that your users can use<br>" +
                "to manage extensions at runtime, or to enable and disable them.<br>" +
                "Try the simulated ExtensionManagerDialog below for an example:</html>";
        formPanel.add(LabelField.createPlainHeaderLabel(txt, 14));

        // Now we can use PanelField to wrap a launcher button for our ExtensionManagerDialog:
        PanelField field = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("Launch simulated ExtensionManagerDialog");
        button.addActionListener(e -> showExtensionDialog());
        field.getPanel().add(button);
        formPanel.add(field);

        return formPanel;
    }

    /**
     * Invoked internally to create and show an ExtensionManagerDialog to show our fake extensions.
     */
    private void showExtensionDialog() {
        ExtensionManagerDialog<AppExtension> dialog = new ExtensionManagerDialog<>(extManager, DemoApp.getInstance());
        dialog.setVisible(true);
    }

    /**
     * Normally, an application would supply its own ExtensionManager implementation, typed to
     * the extension type for the application. But for our demo purposes, our extensions don't
     * actually do anything, so we will provide a stubbed-out dummy implementation of ExtensionManager.
     */
    private static class FakeExtensionManager<AppExtension>
            extends ExtensionManager<ca.corbett.extensions.AppExtension> {
    }

    /**
     * Normally, extensions are not defined as part of the application's codebase like this (although
     * they can be... these are called "internal" or "built-in" extensions). Typically, an extension is a separate
     * project in a separate repo and can be developed and maintained independently from the application.
     * They are deployed in jar format and the application loads them dynamically.
     * But for our demo purposes, a built-in extension is good enough to show off ExtensionManagerDialog.
     */
    private static class FakeExtension1 extends AppExtension {

        @Override
        public AppExtensionInfo getInfo() {
            // Extensions must publish an AppExtensionInfo instance, which is usually
            // in a json file packaged into the jar's resources. Built-in extensions
            // like this one can just return the AppExtensionInfo object directly.
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
        protected void loadJarResources() {
            // We have no resources to load for this fake extension.
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            // We have no configuration properties for this fake extension.
            return List.of();
        }
    }

    /**
     * Normally, extensions are not defined as part of the application's codebase like this (although
     * they can be... these are called "internal" or "built-in" extensions). Typically, an extension is a separate
     * project in a separate repo and can be developed and maintained independently from the application.
     * They are deployed in jar format and the application loads them dynamically.
     * But for our demo purposes, a built-in extension is good enough to show off ExtensionManagerDialog.
     */
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
        protected void loadJarResources() {
            // We have no resources to load for this fake extension.
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            // We have no configuration properties for this fake extension.
            return List.of();
        }
    }

    /**
     * Normally, extensions are not defined as part of the application's codebase like this (although
     * they can be... these are called "internal" or "built-in" extensions). Typically, an extension is a separate
     * project in a separate repo and can be developed and maintained independently from the application.
     * They are deployed in jar format and the application loads them dynamically.
     * But for our demo purposes, a built-in extension is good enough to show off ExtensionManagerDialog.
     */
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
        protected void loadJarResources() {
            // We have no resources to load for this fake extension.
        }

        @Override
        protected List<AbstractProperty> createConfigProperties() {
            // Let's throw some example config properties into this fake extension, just to show
            // off the fact that ExtensionManagerDialog can provide a read-only preview of those
            // properties. In a "real" extension, any configuration properties we return here
            // will automatically get added to the application's config dialog.
            // No custom UI layout code is required for this! Just supply the properties
            // that your extension needs... it's really that easy.
            List<AbstractProperty> props = new ArrayList<>();
            props.add(new LabelProperty("FakeExtension3.General.label1",
                                        "This extension defines some config properties"));
            props.add(new ShortTextProperty("FakeExtension3.General.text1", "Text field 1:", "Default value", 20));
            props.add(new ShortTextProperty("FakeExtension3.General.text2", "Text field 2:", "Hello", 20));
            props.add(new LabelProperty("FakeExtension3.General.label2", "You can view the defaults here."));
            return props;
        }
    }
}
