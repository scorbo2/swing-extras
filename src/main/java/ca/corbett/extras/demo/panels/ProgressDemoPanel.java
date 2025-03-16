package ca.corbett.extras.demo.panels;

import ca.corbett.extras.Version;
import ca.corbett.extras.demo.DemoApp;
import ca.corbett.extras.gradient.GradientColorField;
import ca.corbett.extras.gradient.GradientConfig;
import ca.corbett.extras.gradient.GradientUtil;
import ca.corbett.extras.image.LogoConfig;
import ca.corbett.extras.progress.MultiProgressAdapter;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.MultiProgressWorker;
import ca.corbett.extras.progress.SimpleProgressWorker;
import ca.corbett.extras.progress.SplashProgressWindow;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.TextField;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A quick demo of the capabilities of the progress classes.
 *
 * @author scorbo2
 * @since 2025-03-15
 */
public class ProgressDemoPanel extends PanelBuilder {

    private TextField majorProgressTextField;
    private TextField minorProgressTextField;
    private NumberField majorProgressStepsField;
    private NumberField minorProgressStepsField;

    private TextField splashAppNameField;
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
        FormPanel formPanel = new FormPanel();

        LabelField labelField = new LabelField("MultiProgressDialog");
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.BOLD, 18f));
        labelField.setExtraMargins(16, 4);
        formPanel.addFormField(labelField);

        labelField = createSimpleLabelField("<html>Java Swing comes with the ProgressMonitor class,<br>" +
                "which is great for simple scenarios. But, sometimes it's<br>" +
                "useful to be able to show major and minor progress for a<br>" +
                "more complicated task. Meet the MultiProgressDialog!</html>");
        formPanel.addFormField(labelField);

        majorProgressTextField = new TextField("Major progress label: ", 16, 1, false);
        majorProgressTextField.setText("Some major task");
        formPanel.addFormField(majorProgressTextField);

        majorProgressStepsField = new NumberField("Major progress steps:", 3, 1, 10, 1);
        formPanel.addFormField(majorProgressStepsField);

        minorProgressTextField = new TextField("Minor progress label:", 16, 1, false);
        minorProgressTextField.setText("Some minor task");
        formPanel.addFormField(minorProgressTextField);

        minorProgressStepsField = new NumberField("Minor progress steps:", 5, 1, 15, 1);
        formPanel.addFormField(minorProgressStepsField);

        PanelField panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btn = new JButton("Show multi-progress dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMultiProgress();
            }
        });
        panelField.getPanel().add(btn);
        panelField.setBottomMargin(24);
        formPanel.addFormField(panelField);

        labelField = new LabelField("SplashProgress");
        labelField.setFont(labelField.getFieldLabelFont().deriveFont(Font.BOLD, 18f));
        labelField.setExtraMargins(2, 4);
        formPanel.addFormField(labelField);

        labelField = createSimpleLabelField("<html>Java offers the SplashScreen class for showing a splash<br>" +
                "screen as your application starts up. But sometimes,<br>" +
                "your application startup may involve some complex loading<br>" +
                "and you want to show a progress bar during startup.<br>" +
                "Let's look at SplashProgressWindow!");
        formPanel.addFormField(labelField);

        splashAppNameField = new TextField("Application name:", 15, 1, false);
        splashAppNameField.setText(Version.NAME);
        formPanel.addFormField(splashAppNameField);

        GradientConfig gradient = new GradientConfig();
        gradient.setColor1(Color.BLACK);
        gradient.setColor2(Color.BLUE);
        gradient.setGradientType(GradientUtil.GradientType.HORIZONTAL_STRIPE);
        splashBgColorField = new GradientColorField("Background:", Color.BLACK, gradient, false);
        formPanel.addFormField(splashBgColorField);

        splashFgColorField = new ColorField("Foreground:", Color.WHITE);
        formPanel.addFormField(splashFgColorField);

        splashWidthField = new NumberField("Logo width:", 400, 100, 2000, 25);
        formPanel.addFormField(splashWidthField);

        splashHeightField = new NumberField("Logo height:", 100, 50, 1000, 10);
        formPanel.addFormField(splashHeightField);

        splashBorderWidthField = new NumberField("Border width:", 0, 0, 6, 1);
        formPanel.addFormField(splashBorderWidthField);

        panelField = new PanelField();
        panelField.getPanel().setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("Show splash progress dialog");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSplashProgress();
            }
        });
        panelField.getPanel().add(btn);
        formPanel.addFormField(panelField);

        formPanel.render();
        return formPanel;
    }

    private void showMultiProgress() {
        String majorText = majorProgressTextField.getText().isBlank() ? "Major progress" : majorProgressTextField.getText();
        String minorText = minorProgressTextField.getText().isBlank() ? "Minor progress" : minorProgressTextField.getText();
        int majorSteps = (Integer) majorProgressStepsField.getCurrentValue();
        int minorSteps = (Integer) minorProgressStepsField.getCurrentValue();

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
            config.setBgGradient((GradientConfig) something);
        } else {
            config.setBgColorType(LogoConfig.ColorType.SOLID);
            config.setBgColor((Color) something);
        }
        config.setTextColorType(LogoConfig.ColorType.SOLID);
        config.setTextColor(splashFgColorField.getColor());
        config.setBorderColorType(LogoConfig.ColorType.SOLID);
        config.setBorderColor(splashFgColorField.getColor());
        config.setBorderWidth((Integer) splashBorderWidthField.getCurrentValue());
        config.setLogoWidth((Integer) splashWidthField.getCurrentValue());
        config.setLogoHeight((Integer) splashHeightField.getCurrentValue());
        new SplashProgressWindow(DemoApp.getInstance(), appName, config).showFakeProgress(5, 666);
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
                    } catch (InterruptedException ex) {
                        wasCanceled = true;
                    }
                }
                curMajorStep++;
            }
            if (wasCanceled) {
                fireProgressCanceled();
            } else {
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
