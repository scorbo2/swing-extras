package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.gradient.Gradient;
import ca.corbett.extras.gradient.GradientType;
import ca.corbett.extras.image.LogoProperty;
import ca.corbett.extras.progress.MultiProgressAdapter;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.MultiProgressWorker;
import ca.corbett.extras.progress.SimpleProgressAdapter;
import ca.corbett.extras.progress.SimpleProgressWorker;
import ca.corbett.extras.progress.SplashProgressWindow;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.FlowLayout;

/**
 * A quick demo of the capabilities of the progress classes.
 * These can be used as a replacement for the Java built-in class ProgressMonitor,
 * which is a bit limiting. MultiProgressDialog, for example, can show major
 * and minor progress bars for long-running tasks, and can be supplied
 * any implementation of MultiProgressWorker or SimpleProgressWorker.
 * <p>
 *     This panel also shows off the SplashProgressWindow, which is
 *     kind of neat for long-running application startups.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since 2025-03-15
 */
public class ProgressDemoPanel extends PanelBuilder {

    private ShortTextField simpleProgressTextField;
    private NumberField simpleProgressStepsField;

    private ShortTextField majorProgressTextField;
    private ShortTextField minorProgressTextField;
    private NumberField majorProgressStepsField;
    private NumberField minorProgressStepsField;

    private ShortTextField splashAppNameField;
    private ColorField splashFgColorField;
    private ColorField splashBgColorField;
    private NumberField splashWidthField;
    private NumberField splashHeightField;
    private NumberField splashBorderWidthField;

    @Override
    public String getTitle() {
        return "Progress";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel(null);
        formPanel.add(buildHighlightedHeaderLabel("SimpleProgressDialog", 20));
        formPanel.add(LabelField.createPlainHeaderLabel("A simple replacement for ProgressMonitor!", 14));

        // Let's add a ShortTextField for inputting text to show as the progress bar label:
        simpleProgressTextField = new ShortTextField("Progress label:", 16).setAllowBlank(false);
        simpleProgressTextField.setText("Some task in progress...");
        formPanel.add(simpleProgressTextField);

        // And a NumberField for picking how many fake work steps we should do:
        simpleProgressStepsField = new NumberField("Progress steps:", 6, 1, 10, 1);
        formPanel.add(simpleProgressStepsField);

        // We can use PanelField to wrap a launcher button:
        PanelField panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Show simple progress dialog");
        btn.addActionListener(e -> showSimpleProgress());
        panelField.getPanel().add(btn);
        panelField.getMargins().setBottom(24);
        formPanel.add(panelField);

        // Now move on to MultiProgressDialog:
        formPanel.add(buildHighlightedHeaderLabel("MultiProgressDialog", 20));
        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>Java Swing comes with the ProgressMonitor class, which is great for<br>" +
                        "simple scenarios. But, sometimes it's useful to be able to show major and<br>" +
                        "minor progress for a more complicated task. Meet the MultiProgressDialog!</html>", 14));

        majorProgressTextField = new ShortTextField("Major progress label: ", 16).setAllowBlank(false);
        majorProgressTextField.setText("Some major task");
        formPanel.add(majorProgressTextField);

        majorProgressStepsField = new NumberField("Major progress steps:", 3, 1, 10, 1);
        formPanel.add(majorProgressStepsField);

        minorProgressTextField = new ShortTextField("Minor progress label:", 16).setAllowBlank(false);
        minorProgressTextField.setText("Some minor task");
        formPanel.add(minorProgressTextField);

        minorProgressStepsField = new NumberField("Minor progress steps:", 5, 1, 15, 1);
        formPanel.add(minorProgressStepsField);

        // Add a PanelField with a button for launching the MultiProgressDialog:
        panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        btn = new JButton("Show multi-progress dialog");
        btn.addActionListener(e -> showMultiProgress());
        panelField.getPanel().add(btn);
        panelField.getMargins().setBottom(24);
        formPanel.add(panelField);

        // And finally, move on to SplashProgressWindow:
        formPanel.add(buildHighlightedHeaderLabel("SplashProgressWindow", 20));
        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>Java offers the SplashScreen class for showing a splash screen as your application<br>" +
                        "starts up. But sometimes, your application startup may involve some complex loading<br>" +
                        "and you want to show a progress bar during startup. Let's look at SplashProgressWindow!</html>",
                14));

        splashAppNameField = new ShortTextField("Application name:", 15).setAllowBlank(false);
        splashAppNameField.setText(Version.NAME);
        formPanel.add(splashAppNameField);

        // We can allow either a Gradient or a solid color for the background of our SplashProgressWindow:
        Gradient gradient = new Gradient(GradientType.HORIZONTAL_STRIPE, Color.BLACK, Color.BLUE);
        splashBgColorField = new ColorField("Background:", ColorSelectionType.EITHER)
                .setColor(Color.BLACK)
                .setGradient(gradient);
        formPanel.add(splashBgColorField);

        // But let's limit selection to only solid colors for the foreground color:
        splashFgColorField = new ColorField("Foreground:", ColorSelectionType.SOLID)
                .setColor(Color.WHITE);
        formPanel.add(splashFgColorField);

        // We can add options for controlling the size of the splash window:
        splashWidthField = new NumberField("Logo width:", 400, 100, 2000, 25);
        formPanel.add(splashWidthField);

        splashHeightField = new NumberField("Logo height:", 100, 50, 1000, 10);
        formPanel.add(splashHeightField);

        splashBorderWidthField = new NumberField("Border width:", 0, 0, 6, 1);
        formPanel.add(splashBorderWidthField);

        // And a PanelField with a button for launching the SplashProgressWindow:
        panelField = new PanelField(new FlowLayout(FlowLayout.LEFT));
        btn = new JButton("Show splash progress dialog");
        btn.addActionListener(e -> showSplashProgress());
        panelField.getPanel().add(btn);
        formPanel.add(panelField);

        return formPanel;
    }

    /**
     * A quick internal method for either returning the input string, or returning the
     * defaultValue string if the input is null or blank.
     */
    private String getTextOrDefault(String input, String defaultValue) {
        return input == null || input.isBlank() ? defaultValue : input;
    }

    /**
     * Invoked internally to show our progress dialog with a fake SimpleProgressWorker.
     */
    private void showSimpleProgress() {
        // Create our dummy worker thread and add our progress listener:
        SimpleProgressDummyWorker worker = new SimpleProgressDummyWorker(
                getTextOrDefault(simpleProgressTextField.getText(), "Progress"),
                (Integer)simpleProgressStepsField.getCurrentValue());
        worker.addProgressListener(new SimpleProgressDummyAdapter());

        // Now we can create a MultiProgressDialog instance and give it our worker thread:
        MultiProgressDialog dialog = new MultiProgressDialog(DemoApp.getInstance(), "Fake work in progress");
        dialog.runWorker(worker, true); // shows the dialog automatically!
    }

    /**
     * Invoked internally to show our progress dialog with a fake MultiProgressWorker.
     */
    private void showMultiProgress() {
        // Create our dummy worker thread and add our progress listener to it:
        MultiProgressDummyWorker worker = new MultiProgressDummyWorker(
                getTextOrDefault(majorProgressTextField.getText(), "Major progress"),
                (Integer)majorProgressStepsField.getCurrentValue(),
                getTextOrDefault(minorProgressTextField.getText(), "Minor progress"),
                (Integer)minorProgressStepsField.getCurrentValue());
        worker.addProgressListener(new MultiProgressDummyAdapter());

        // Now we can create a MultiProgressDialog instance and hand it our worker thread:
        MultiProgressDialog dialog = new MultiProgressDialog(DemoApp.getInstance(), "Fake work in progress");
        dialog.runWorker(worker, true); // shows the dialog automatically!
    }

    /**
     * Invoked internally to show an example SplashProgressWindow.
     */
    private void showSplashProgress() {
        // Get the user-entered parameters for the splash window:
        String appName = getTextOrDefault(splashAppNameField.getText(), Version.NAME);
        LogoProperty config = new LogoProperty(appName);

        // Remember that our background color can either be a gradient or a solid color:
        Object something = splashBgColorField.getSelectedValue();
        if (something instanceof Gradient) {
            config.setBgColorType(LogoProperty.ColorType.GRADIENT);
            config.setBgGradient((Gradient)something);
        }
        else {
            config.setBgColorType(LogoProperty.ColorType.SOLID);
            config.setBgColor((Color)something);
        }

        // Collect the rest of the properties:
        config.setTextColorType(LogoProperty.ColorType.SOLID);
        config.setTextColor(splashFgColorField.getColor());
        config.setBorderColorType(LogoProperty.ColorType.SOLID);
        config.setBorderColor(splashFgColorField.getColor());
        config.setBorderWidth((Integer)splashBorderWidthField.getCurrentValue());
        config.setLogoWidth((Integer)splashWidthField.getCurrentValue());
        config.setLogoHeight((Integer)splashHeightField.getCurrentValue());

        // We don't need to create a worker thread for this one because the
        // SplashProgressWindow class has a built-in "showFakeProgress" method, lol.
        new SplashProgressWindow(DemoApp.getInstance(), appName, config).showFakeProgress(5, 666);
    }

    /**
     * This example implementation of SimpleProgressWorker will fake doing a certain number
     * of steps of work, with a configurable delay between each step. Good enough for
     * showing off the progress dialog, but it doesn't actually do anything.
     */
    private static class SimpleProgressDummyWorker extends SimpleProgressWorker {

        public final int STEP_DURATION_MS = 750;
        private final int totalSteps;
        private final String text;
        private boolean wasCanceled;

        public SimpleProgressDummyWorker(String text, int totalSteps) {
            this.totalSteps = totalSteps;
            this.text = text;
            this.wasCanceled = false;
        }

        public boolean wasCanceled() {
            return wasCanceled;
        }

        @Override
        public void run() {
            fireProgressBegins(totalSteps);
            int curStep = 0;
            while (!wasCanceled && curStep < totalSteps) {
                // Make sure to check for cancellations, which can be signaled by the
                // user hitting ESC on the dialog or clicking the "cancel" button:
                wasCanceled = !fireProgressUpdate(curStep, text);

                try {
                    Thread.sleep(STEP_DURATION_MS);
                }
                catch (InterruptedException ex) {
                    wasCanceled = true; // we'll treat a thread interrupt like a cancel
                }

                curStep++;
            }

            // Let callers know how it all ended:
            if (wasCanceled) {
                fireProgressCanceled();
            }
            else {
                fireProgressComplete();
            }
        }
    }

    /**
     * A very simple implementation of SimpleProgressListener that will just pop a dialog
     * when the progress is completed or canceled.
     */
    private static class SimpleProgressDummyAdapter extends SimpleProgressAdapter {
        @Override
        public void progressCanceled() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was canceled.");
            });
        }

        @Override
        public void progressComplete() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was completed.");
            });
        }
    }

    /**
     * This example implementation of MultiProgressWorker will complete a configurable number
     * of fake major and minor steps, each with a configurable delay in between. Useful for
     * showing off the progress dialog, but it doesn't actually do anything.
     */
    private static class MultiProgressDummyWorker extends MultiProgressWorker {
        public final int STEP_DURATION_MS = 500;
        private final int majorSteps;
        private final int minorSteps;
        private final String majorText;
        private final String minorText;
        private boolean wasCanceled;

        public MultiProgressDummyWorker(String majorText, int majorSteps, String minorText, int minorSteps) {
            this.majorSteps = majorSteps;
            this.minorSteps = minorSteps;
            this.majorText = majorText;
            this.minorText = minorText;
            this.wasCanceled = false;
        }

        public boolean wasCanceled() {
            return wasCanceled;
        }

        @Override
        public void run() {
            fireProgressBegins(majorSteps);
            int curMajorStep = 0;
            while (!wasCanceled && curMajorStep < majorSteps) {
                // Remember to check for cancellations on each major and minor step.
                // These can be signaled by the user hitting ESC or hitting the cancel button.
                wasCanceled = !fireMajorProgressUpdate(curMajorStep, minorSteps, majorText);

                for (int curMinorStep = 0; (curMinorStep < minorSteps) && !wasCanceled; curMinorStep++) {
                    wasCanceled = !fireMinorProgressUpdate(curMajorStep, curMinorStep, minorText);
                    try {
                        Thread.sleep(STEP_DURATION_MS);
                    }
                    catch (InterruptedException ex) {
                        wasCanceled = true;
                    }
                }
                curMajorStep++;
            }

            // Let callers know how it all ended:
            if (wasCanceled) {
                fireProgressCanceled();
            }
            else {
                fireProgressComplete();
            }
        }
    }

    /**
     * A simple implementation of MultiProgressListener that will simply pop a dialog
     * when the progress is completed or canceled.
     */
    private static class MultiProgressDummyAdapter extends MultiProgressAdapter {
        @Override
        public void progressCanceled() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was canceled.");
            });
        }

        @Override
        public void progressComplete() {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was completed.");
            });
        }
    }
}
