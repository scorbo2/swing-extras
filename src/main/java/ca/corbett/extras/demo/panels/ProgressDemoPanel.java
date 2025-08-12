package ca.corbett.extras.demo.panels;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.LogoConfig;
import ca.corbett.extras.progress.MultiProgressAdapter;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.MultiProgressWorker;
import ca.corbett.extras.progress.SimpleProgressAdapter;
import ca.corbett.extras.progress.SimpleProgressWorker;
import ca.corbett.extras.progress.SplashProgressWindow;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A quick demo of the capabilities of the progress classes.
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
    private GradientColorField splashBgColorField;
    private NumberField splashWidthField;
    private NumberField splashHeightField;
    private NumberField splashBorderWidthField;

    @Override
    public String getTitle() {
        return "Progress";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(24);

        final LabelField simpleLabel = LabelField.createBoldHeaderLabel("SimpleProgressDialog", 20);
        simpleLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> simpleLabel.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(simpleLabel);

        formPanel.add(LabelField.createPlainHeaderLabel("A simple replacement for ProgressMonitor!", 14));

        simpleProgressTextField = new ShortTextField("Progress label:", 16).setAllowBlank(false);
        simpleProgressTextField.setText("Some task in progress...");
        formPanel.add(simpleProgressTextField);

        simpleProgressStepsField = new NumberField("Progress steps:", 6, 1, 10, 1);
        formPanel.add(simpleProgressStepsField);

        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btn = new JButton("Show simple progress dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSimpleProgress();
            }
        });
        panelField.getPanel().add(btn);
        panelField.getMargins().setBottom(24);
        formPanel.add(panelField);

        final LabelField label = LabelField.createBoldHeaderLabel("MultiProgressDialog", 20);
        label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> label.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(label);

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

        panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        btn = new JButton("Show multi-progress dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMultiProgress();
            }
        });
        panelField.getPanel().add(btn);
        panelField.getMargins().setBottom(24);
        formPanel.add(panelField);

        final LabelField labelField = LabelField.createBoldHeaderLabel("SplashProgress", 20);
        labelField.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE));
        LookAndFeelManager.addChangeListener(
                e -> labelField.setColor(LookAndFeelManager.getLafColor("textHighlight", Color.BLUE)));
        formPanel.add(labelField);

        formPanel.add(LabelField.createPlainHeaderLabel(
                "<html>Java offers the SplashScreen class for showing a splash screen as your application<br>" +
                        "starts up. But sometimes, your application startup may involve some complex loading<br>" +
                        "and you want to show a progress bar during startup. Let's look at SplashProgressWindow!</html>",
                14));

        splashAppNameField = new ShortTextField("Application name:", 15).setAllowBlank(false);
        splashAppNameField.setText(Version.NAME);
        formPanel.add(splashAppNameField);

        GradientConfig gradient = new GradientConfig();
        gradient.setColor1(Color.BLACK);
        gradient.setColor2(Color.BLUE);
        gradient.setGradientType(GradientUtil.GradientType.HORIZONTAL_STRIPE);
        splashBgColorField = new GradientColorField("Background:", Color.BLACK, gradient, false);
        formPanel.add(splashBgColorField);

        splashFgColorField = new ColorField("Foreground:", Color.WHITE);
        formPanel.add(splashFgColorField);

        splashWidthField = new NumberField("Logo width:", 400, 100, 2000, 25);
        formPanel.add(splashWidthField);

        splashHeightField = new NumberField("Logo height:", 100, 50, 1000, 10);
        formPanel.add(splashHeightField);

        splashBorderWidthField = new NumberField("Border width:", 0, 0, 6, 1);
        formPanel.add(splashBorderWidthField);

        panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
        btn = new JButton("Show splash progress dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSplashProgress();
            }
        });
        panelField.getPanel().add(btn);
        formPanel.add(panelField);

        return formPanel;
    }

    private void showSimpleProgress() {
        String text = simpleProgressTextField.getText().isBlank() ? "Progress" : simpleProgressTextField.getText();
        int totalSteps = (Integer)simpleProgressStepsField.getCurrentValue();
        SimpleProgressDummyWorker worker = new SimpleProgressDummyWorker(text, totalSteps);
        worker.addProgressListener(new SimpleProgressAdapter() {
            @Override
            public void progressCanceled() {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was canceled.");
            }

            @Override
            public void progressComplete() {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was completed.");
            }
        });

        MultiProgressDialog dialog = new MultiProgressDialog(DemoApp.getInstance(), "Fake work in progress");
        dialog.runWorker(worker, true);
    }

    private void showMultiProgress() {
        String majorText = majorProgressTextField.getText()
                                                 .isBlank() ? "Major progress" : majorProgressTextField.getText();
        String minorText = minorProgressTextField.getText()
                                                 .isBlank() ? "Minor progress" : minorProgressTextField.getText();
        int majorSteps = (Integer)majorProgressStepsField.getCurrentValue();
        int minorSteps = (Integer)minorProgressStepsField.getCurrentValue();

        MultiProgressDummyWorker worker = new MultiProgressDummyWorker(majorText, majorSteps, minorText, minorSteps);
        worker.addProgressListener(new MultiProgressAdapter() {
            @Override
            public void progressCanceled() {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was canceled.");
            }

            @Override
            public void progressComplete() {
                JOptionPane.showMessageDialog(DemoApp.getInstance(), "The fake work was completed.");
            }
        });

        MultiProgressDialog dialog = new MultiProgressDialog(DemoApp.getInstance(), "Fake work in progress");
        dialog.runWorker(worker, true);
    }

    private void showSplashProgress() {
        String appName = splashAppNameField.getText().isBlank() ? Version.NAME : splashAppNameField.getText();
        LogoConfig config = new LogoConfig(appName);
        Object something = splashBgColorField.getSelectedValue();
        if (something instanceof GradientConfig) {
            config.setBgColorType(LogoConfig.ColorType.GRADIENT);
            config.setBgGradient((GradientConfig)something);
        }
        else {
            config.setBgColorType(LogoConfig.ColorType.SOLID);
            config.setBgColor((Color)something);
        }
        config.setTextColorType(LogoConfig.ColorType.SOLID);
        config.setTextColor(splashFgColorField.getColor());
        config.setBorderColorType(LogoConfig.ColorType.SOLID);
        config.setBorderColor(splashFgColorField.getColor());
        config.setBorderWidth((Integer)splashBorderWidthField.getCurrentValue());
        config.setLogoWidth((Integer)splashWidthField.getCurrentValue());
        config.setLogoHeight((Integer)splashHeightField.getCurrentValue());
        new SplashProgressWindow(DemoApp.getInstance(), appName, config).showFakeProgress(5, 666);
    }

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
                wasCanceled = !fireProgressUpdate(curStep, text);

                try {
                    Thread.sleep(STEP_DURATION_MS);
                }
                catch (InterruptedException ex) {
                    wasCanceled = true;
                }

                curStep++;
            }
            if (wasCanceled) {
                fireProgressCanceled();
            }
            else {
                fireProgressComplete();
            }
        }
    }

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
            if (wasCanceled) {
                fireProgressCanceled();
            }
            else {
                fireProgressComplete();
            }
        }
    }

    private class MyWorker extends SimpleProgressWorker {

        @Override
        public void run() {

        }
    }

    ;
}
