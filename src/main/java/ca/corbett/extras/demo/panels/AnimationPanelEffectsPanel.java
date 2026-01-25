package ca.corbett.extras.demo.panels;

import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.image.animation.BlurLayerUI;
import ca.corbett.extras.image.animation.FadeLayerUI;
import ca.corbett.extras.image.animation.SnowLayerUI;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ButtonField;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLayer;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Random;

/**
 * This demo panel shows off some of the fun "panel effects" that are
 * available in swing-extras as of the 2.7 release:
 * <ul>
 *     <li><b>Panel fade</b> - animate a "fade in" and "fade out" effect.</li>
 *     <li><b>Panel blur</b> - blur the contents of a panel.</li>
 *     <li><b>Falling snow</b> - animate falling snowflakes over a panel.</li>
 * </ul>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since swing-extras 2.7
 */
public class AnimationPanelEffectsPanel extends PanelBuilder {

    private final List<String> panelEffects = List.of(
            "No effects - boring!",
            "Fade in / fade out",
            "Blur panel contents",
            "Falling snowflakes"
    );

    private JPanel containerPanel;
    private JLayer<JPanel> targetLayer;
    private FormPanel samplePanel;

    private PanelField fadePanel;
    private FadeLayerUI fadeLayerUI;
    private ColorField fadeColorField;
    private ComboField<FadeLayerUI.AnimationDuration> fadeDurationField;
    private ComboField<FadeLayerUI.AnimationSpeed> fadeSpeedField;

    private PanelField blurPanel;
    private BlurLayerUI blurLayerUI;
    private ComboField<BlurLayerUI.BlurIntensity> blurIntensityField;
    private ColorField blurColorField;
    private ShortTextField overlayTextField;
    private NumberField overlayTextSizeField;
    private ColorField overlayTextColorField;

    private PanelField snowPanel;
    private SnowLayerUI snowLayerUI;
    private ComboField<SnowLayerUI.Quantity> snowQuantityField;
    private ComboField<SnowLayerUI.Wind> snowWindField;
    private ColorField snowColorField;

    @Override
    public String getTitle() {
        return "Animation: panel effects";
    }

    @Override
    public JPanel build() {
        FormPanel formPanel = buildFormPanel("Panel effects");

        formPanel.add(new LabelField("<html>Just for fun, swing-extras 2.7 includes some visual effects that<br>"
                                             + "can be applied to any JPanel. Try them out below!</html>"));

        // Build out our "container" panel, which will hold the example dummy panel:
        PanelField containerPanel = new PanelField(new BorderLayout());
        containerPanel.getMargins().setTop(12).setBottom(6);
        this.containerPanel = containerPanel.getPanel();
        this.samplePanel = new FormPanel(Alignment.TOP_LEFT).setBorderMargin(12);
        populateSamplePanel();
        containerPanel.getPanel().setBorder(BorderFactory.createLoweredBevelBorder());
        containerPanel.getPanel().add(samplePanel, BorderLayout.CENTER);
        formPanel.add(containerPanel);

        // Build out a simple combo box for choosing the panel effect:
        // Also add a change listener, so that we can show or hide the effect options as needed:
        ComboField<String> effectsChooser = new ComboField<>("Choose panel effect:", panelEffects, 0);
        effectsChooser.getMargins().setTop(24);
        effectsChooser.addValueChangedListener(e -> effectChooserChanged(effectsChooser.getSelectedIndex()));
        formPanel.add(effectsChooser);

        // Now add all our control options, initially invisible:
        formPanel.add(buildFadePanel());
        formPanel.add(buildBlurPanel());
        formPanel.add(buildSnowPanel());

        return formPanel;
    }

    /**
     * Invoked internally when the selected visual effect is changed.
     * We will cancel any effect in progress, reset the sample panel,
     * and present the appropriate effect controls.
     */
    private void effectChooserChanged(int selectedIndex) {
        // Update visibility of the effect control panels:
        fadePanel.setVisible(selectedIndex == 1);
        blurPanel.setVisible(selectedIndex == 2);
        snowPanel.setVisible(selectedIndex == 3);

        // Remove any existing effect layer:
        containerPanel.removeAll();
        containerPanel.add(samplePanel, BorderLayout.CENTER);
        containerPanel.revalidate();
        containerPanel.repaint();
        blurLayerUI.setBlurred(false); // unblur if we were blurred
        snowLayerUI.letItSnow(false); // stop snowing if we were snowing
    }

    /**
     * Invoked internally when any of the blur effect options are changed.
     * We can update the blur effect, even if it's currently active.
     */
    private void blurOptionsChanged() {
        blurLayerUI.setBlurIntensity(blurIntensityField.getSelectedItem());
        blurLayerUI.setBlurOverlayColor(blurColorField.getColor());
        blurLayerUI.setOverlayText(overlayTextField.getText());
        blurLayerUI.setOverlayTextSize(overlayTextSizeField.getCurrentValue().intValue());
        blurLayerUI.setOverlayTextColor(overlayTextColorField.getColor());

        // We have to tell the panel to repaint itself to pick up the changes:
        if (blurLayerUI.isBlurred()) {
            containerPanel.revalidate();
            containerPanel.repaint();
        }
    }

    /**
     * Invoked internally when any of the snow effect options are changed.
     * We can update the snow effect, even if it's currently active.
     */
    private void snowOptionsChanged() {
        // This will take immediate effect if snow is currently falling:
        snowLayerUI.setQuantity(snowQuantityField.getSelectedItem());
        snowLayerUI.setSnowColor(snowColorField.getColor()); // this will only affect new flakes, not existing ones
        snowLayerUI.setWind(snowWindField.getSelectedItem()); // but this automatically updates existing flakes
    }

    /**
     * This builds out the control options for the fade effect:
     * <ul>
     *     <li><b>Fade color</b> - the color to fade to/from.</li>
     *     <li><b>Fade duration</b> - how long the fade animation should take.</li>
     *     <li><b>Fade speed</b> - controls the FPS of the animation - faster values use more CPU!</li>
     * </ul>
     */
    private PanelField buildFadePanel() {
        fadeLayerUI = new FadeLayerUI();
        fadePanel = new PanelField(new BorderLayout());
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);
        formPanel.add(LabelField.createBoldHeaderLabel("Fade effect controls"));
        fadeColorField = new ColorField("Fade color:", ColorSelectionType.SOLID)
                .setColor(FadeLayerUI.DEFAULT_FADE_COLOR);
        formPanel.add(fadeColorField);
        fadeDurationField = new ComboField<>("Fade duration:",
                                             List.of(FadeLayerUI.AnimationDuration.values()), 2);
        formPanel.add(fadeDurationField);
        fadeSpeedField = new ComboField<>("Fade speed:",
                                          List.of(FadeLayerUI.AnimationSpeed.values()), 2);
        formPanel.add(fadeSpeedField);

        ButtonField buttonField = new ButtonField(List.of(new FadeAction()));
        buttonField.setButtonPreferredSize(new Dimension(110, 25));
        formPanel.add(buttonField);

        fadePanel.getPanel().add(formPanel, BorderLayout.CENTER);
        fadePanel.setVisible(false);
        return fadePanel;
    }

    /**
     * This builds out the control options for the blur effect:
     * <ul>
     *     <li><b>Blur intensity</b> - some preset options let you choose the blurring strength.</li>
     *     <li><b>Blur color</b> - you can apply a colored tint over the blurred panel.</li>
     *     <li><b>Overlay text</b> - optional text to show over the blurred panel.</li>
     *     <li><b>Overlay text size</b> - choose the font size for the overlay text.</li>
     *     <li><b>Overlay text color</b> - choose the font color for the overlay text.</li>
     * </ul>
     */
    private PanelField buildBlurPanel() {
        blurLayerUI = new BlurLayerUI();
        blurPanel = new PanelField(new BorderLayout());
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);
        formPanel.add(LabelField.createBoldHeaderLabel("Blur effect controls"));
        blurIntensityField = new ComboField<>("Blur intensity:",
                                              List.of(BlurLayerUI.BlurIntensity.values()), 1);
        blurIntensityField.addValueChangedListener(e -> blurOptionsChanged());
        formPanel.add(blurIntensityField);
        blurColorField = new ColorField("Blur overlay color:", ColorSelectionType.SOLID)
                .setColor(BlurLayerUI.DEFAULT_BLUR_OVERLAY_COLOR);
        blurColorField.addValueChangedListener(e -> blurOptionsChanged());
        formPanel.add(blurColorField);
        overlayTextField = new ShortTextField("Overlay text:", 20)
                .setText("This panel is blurred!");
        overlayTextField.addValueChangedListener(e -> blurOptionsChanged());
        formPanel.add(overlayTextField);
        overlayTextSizeField = new NumberField("Overlay text size:",
                                               BlurLayerUI.DEFAULT_TEXT_SIZE,
                                               BlurLayerUI.TEXT_MINIMUM_SIZE,
                                               BlurLayerUI.TEXT_MAXIMUM_SIZE,
                                               2);
        overlayTextSizeField.addValueChangedListener(e -> blurOptionsChanged());
        formPanel.add(overlayTextSizeField);
        overlayTextColorField = new ColorField("Overlay text color:", ColorSelectionType.SOLID)
                .setColor(BlurLayerUI.DEFAULT_TEXT_COLOR);
        overlayTextColorField.addValueChangedListener(e -> blurOptionsChanged());
        formPanel.add(overlayTextColorField);

        ButtonField buttonField = new ButtonField(List.of(new BlurAction()));
        buttonField.setButtonPreferredSize(new Dimension(110, 25));
        formPanel.add(buttonField);

        blurPanel.getPanel().add(formPanel, BorderLayout.CENTER);
        blurPanel.setVisible(false);
        return blurPanel;
    }

    /**
     * This builds out the control options for the falling snow effect:
     * <ul>
     *     <li><b>Snow amount</b> - The intensity of the snow storm.</li>
     *     <li><b>Wind strength</b> - How much wind is blowing the snowflakes sideways.</li>
     *     <li><b>Snow color</b> - Why would you ever choose anything other than white for snow????</li>
     * </ul>
     */
    private PanelField buildSnowPanel() {
        snowLayerUI = new SnowLayerUI();
        snowPanel = new PanelField(new BorderLayout());
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);
        formPanel.add(LabelField.createBoldHeaderLabel("Falling snow effect"));

        snowQuantityField = new ComboField<>("Snow amount:",
                                             List.of(SnowLayerUI.Quantity.values()), 2);
        snowQuantityField.addValueChangedListener(e -> snowOptionsChanged());
        formPanel.add(snowQuantityField);
        snowWindField = new ComboField<>("Wind strength:",
                                         List.of(SnowLayerUI.Wind.values()), 2);
        snowWindField.addValueChangedListener(e -> snowOptionsChanged());
        formPanel.add(snowWindField);
        snowColorField = new ColorField("Snow color:", ColorSelectionType.SOLID)
                .setColor(SnowLayerUI.DEFAULT_SNOW_COLOR);
        snowColorField.addValueChangedListener(e -> snowOptionsChanged());
        snowColorField.setHelpText("Why would you ever choose anything other than white for snow????");
        formPanel.add(snowColorField);

        ButtonField buttonField = new ButtonField(List.of(new SnowAction()));
        buttonField.setButtonPreferredSize(new Dimension(110, 25));
        formPanel.add(buttonField);

        snowPanel.getPanel().add(formPanel, BorderLayout.CENTER);
        snowPanel.setVisible(false);
        return snowPanel;
    }

    /**
     * Invoked internally to clear the example panel and repopulate it
     * with a random number of dummy controls.
     */
    private void populateSamplePanel() {
        Random rand = new Random();
        samplePanel.removeAll();

        // We'll generate a random number of dummy controls to add to the panel:
        final int numControls = 2 + rand.nextInt(3);

        // Pick a random header to show at the top:
        List<String> headers = List.of(
                "This is an example panel for demo purposes.",
                "The example controls here do nothing",
                "This is just an example. Nothing here actually works.",
                "This sample panel shows some dummy controls",
                "This is just a demo panel for the effects demo"
        );
        samplePanel.add(new LabelField(headers.get(rand.nextInt(headers.size()))));

        // Just throw some simple single-line controls onto the form, it doesn't matter what:
        for (int i = 0; i < numControls; i++) {
            FormField newField = switch (rand.nextInt(3)) {
                case 0 -> new CheckBoxField("Example checkbox " + (i + 1), rand.nextBoolean());
                case 1 -> new ShortTextField("Example text " + (i + 1), 15).setText("hello");
                case 2 -> new LabelField("Example label " + (i + 1));
                default -> null;
            };
            samplePanel.add(newField);
        }

        // Force a repaint of the container panel to pick up the changes:
        containerPanel.invalidate();
        containerPanel.revalidate();
        containerPanel.repaint();
    }

    /**
     * A quick example action to fade out the current sample panel, repopulate it
     * with new random controls, and then fade it back in.
     */
    private class FadeAction extends AbstractAction {

        public FadeAction() {
            super("Start fade");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            containerPanel.removeAll();
            targetLayer = new JLayer<>(samplePanel, fadeLayerUI);
            containerPanel.add(targetLayer, BorderLayout.CENTER);
            containerPanel.revalidate();
            containerPanel.repaint();

            fadeLayerUI.setFadeColor(fadeColorField.getColor());
            fadeLayerUI.setAnimationDuration(fadeDurationField.getSelectedItem());
            fadeLayerUI.setAnimationSpeed(fadeSpeedField.getSelectedItem());

            // Fade out, swap the panel contents, and then fade back in:
            fadeLayerUI.fadeOut(() -> {
                populateSamplePanel();
                fadeLayerUI.fadeIn(null);
            });
        }
    }

    /**
     * A quick example action to blur the current sample panel, or unblur it if
     * it was already blurred.
     */
    private class BlurAction extends AbstractAction {

        public BlurAction() {
            super("Toggle blur");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            containerPanel.removeAll();
            targetLayer = new JLayer<>(samplePanel, blurLayerUI);
            containerPanel.add(targetLayer, BorderLayout.CENTER);
            containerPanel.revalidate();
            containerPanel.repaint();

            if (!blurLayerUI.isBlurred()) {
                blurLayerUI.setOverlayText(overlayTextField.getText());
                blurLayerUI.setBlurIntensity(blurIntensityField.getSelectedItem());
                blurLayerUI.setBlurred(true);
            }
            else {
                blurLayerUI.setBlurred(false);
            }
        }
    }

    /**
     * A quick example action to start or stop the falling snow effect
     * on the sample panel.
     */
    private class SnowAction extends AbstractAction {

        public SnowAction() {
            super("Toggle snow");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            containerPanel.removeAll();
            targetLayer = new JLayer<>(samplePanel, snowLayerUI);
            containerPanel.add(targetLayer, BorderLayout.CENTER);
            containerPanel.revalidate();
            containerPanel.repaint();

            snowLayerUI.letItSnow(!snowLayerUI.isSnowing());
        }
    }
}
