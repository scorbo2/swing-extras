package ca.corbett.extras.image;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.NumberField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Objects;

/**
 * Provides a FormField implementation for viewing/editing config options
 * for ImagePanels.
 *
 * @author scorbett
 * @since 2018-01-26
 */
public class ImagePanelFormField extends FormField {

    private final JPanel wrapperPanel;
    private final FormPanel formPanel;
    private String labelText;
    private ColorField bgColorField;
    private ComboField<String> displayModeCombo;
    private ComboField<String> renderQualityCombo;
    private NumberField zoomIncrementSpinner;
    private CheckBoxField enableMouseCursorField;
    private CheckBoxField enableMouseDraggingField;
    private CheckBoxField zoomOnClickField;
    private CheckBoxField zoomOnWheelField;
    private boolean useTitleBorder;
    private boolean shouldExpand = true;

    public ImagePanelFormField(String label) {
        labelText = label;
        wrapperPanel = new JPanel(new BorderLayout());
        formPanel = createFormPanel();
        wrapperPanel.add(formPanel, BorderLayout.CENTER);
        wrapperPanel.setBorder(BorderFactory.createTitledBorder(label));
        useTitleBorder = true;
        fieldComponent = wrapperPanel;
    }

    @Override
    public boolean isMultiLine() {
        return true;
    }

    public ImagePanelFormField setShouldExpand(boolean should) {
        shouldExpand = should;
        return this;
    }

    @Override
    public boolean shouldExpand() {
        return shouldExpand;
    }

    /**
     * This field itself typically will not show a validation label, as we delegate field validation
     * to our embedded form fields. But, in keeping with the swing-forms general contract, you can
     * still assign FieldValidators to instances of this field if you wish.
     */
    @Override
    public boolean hasValidationLabel() {
        return !fieldValidators.isEmpty();
    }

    @Override
    public boolean validate() {
        super.validate(); // in case we have any FieldValidators assigned here
        return formPanel.isFormValid();
    }

    /**
     * Decides whether to use a titled border around this field component (the default), or
     * to use a traditional FormField field label instead.
     */
    public ImagePanelFormField setUseTitleBorder(boolean use) {
        if (use == useTitleBorder) {
            return this; // ignore no-op requests
        }
        if (use) {
            wrapperPanel.setBorder(BorderFactory.createTitledBorder(labelText));
            fieldLabel.setText("");
        }
        else {
            wrapperPanel.setBorder(null);
            fieldLabel.setText(labelText);
        }
        useTitleBorder = use;
        return this;
    }

    public boolean isUseTitleBorder() {
        return useTitleBorder;
    }

    /**
     * Returns the text that is either showing in the field label OR in the
     * field's title border, depending on the value of useTitleBorder.
     */
    public String getFieldLabelText() {
        return labelText;
    }

    public ImagePanelFormField setFieldLabelText(String text) {
        labelText = text;
        if (useTitleBorder && (wrapperPanel.getBorder() instanceof TitledBorder)) {
            ((TitledBorder)wrapperPanel.getBorder()).setTitle(labelText);
        }
        else if (!useTitleBorder) {
            fieldLabel.setText(labelText);
        }
        return this;
    }

    public ImagePanelFormField setBgColor(Color col) {
        if (Objects.equals(getBgColor(), col)) {
            return this; // ignore no-op requests
        }
        if (col == null) {
            return this; // ignore null
        }
        bgColorField.setColor(col);
        return this;
    }

    public Color getBgColor() {
        return bgColorField.getColor();
    }

    public ImagePanelFormField setDisplayMode(ImagePanelConfig.DisplayMode mode) {
        if (getDisplayMode() == mode || mode == null) {
            return this; // ignore no-op requests and null
        }
        displayModeCombo.setSelectedItem(mode.toString());
        return this;
    }

    public ImagePanelConfig.DisplayMode getDisplayMode() {
        return ImagePanelConfig.DisplayMode.fromLabel(displayModeCombo.getSelectedItem())
                                           .orElse(ImagePanelConfig.DisplayMode.BEST_FIT);
    }

    public ImagePanelFormField setRenderQuality(ImagePanelConfig.Quality quality) {
        if (getRenderQuality() == quality || quality == null) {
            return this; // ignore no-op requests and null
        }
        renderQualityCombo.setSelectedItem(quality.toString());
        return this;
    }

    public ImagePanelConfig.Quality getRenderQuality() {
        return ImagePanelConfig.Quality.fromLabel(renderQualityCombo.getSelectedItem())
                                       .orElse(ImagePanelConfig.Quality.SLOW_AND_ACCURATE);
    }

    public ImagePanelFormField setZoomIncrement(double increment) {
        if (getZoomIncrement() == increment) {
            return this; // ignore no-op requests
        }
        zoomIncrementSpinner.setCurrentValue(increment);
        return this;
    }

    public double getZoomIncrement() {
        return (double)zoomIncrementSpinner.getCurrentValue();
    }

    public ImagePanelFormField setEnableMouseCursor(boolean enable) {
        if (isEnableMouseCursor() == enable) {
            return this; // ignore no-op requests
        }
        enableMouseCursorField.setChecked(enable);
        return this;
    }

    public boolean isEnableMouseCursor() {
        return enableMouseCursorField.isChecked();
    }

    public ImagePanelFormField setEnableMouseDragging(boolean enable) {
        if (isEnableMouseDragging() == enable) {
            return this; // ignore no-op requests
        }
        enableMouseDraggingField.setChecked(enable);
        return this;
    }

    public boolean isEnableMouseDragging() {
        return enableMouseDraggingField.isChecked();
    }

    public ImagePanelFormField setEnableZoomOnMouseClick(boolean enable) {
        if (isEnableZoomOnMouseClick() == enable) {
            return this; // ignore no-op requests
        }
        zoomOnClickField.setChecked(enable);
        return this;
    }

    public boolean isEnableZoomOnMouseClick() {
        return zoomOnClickField.isChecked();
    }

    public ImagePanelFormField setEnableZoomOnMouseWheel(boolean enable) {
        if (isEnableZoomOnMouseWheel() == enable) {
            return this; // ignore no-op requests
        }
        zoomOnWheelField.setChecked(enable);
        return this;
    }

    public boolean isEnableZoomOnMouseWheel() {
        return zoomOnWheelField.isChecked();
    }

    private FormPanel createFormPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);

        ValueChangedListener listener = field -> fireValueChangedEvent();

        bgColorField = new ColorField("Background:", ColorSelectionType.SOLID)
                .setColor(LookAndFeelManager.getLafColor("Panel.background", Color.DARK_GRAY));
        bgColorField.addValueChangedListener(listener);
        formPanel.add(bgColorField);

        displayModeCombo = new ComboField<>("Display mode:", ImagePanelConfig.DisplayMode.getLabels(), 2);
        displayModeCombo.addValueChangedListener(listener);
        formPanel.add(displayModeCombo);

        renderQualityCombo = new ComboField<>("Render quality:", ImagePanelConfig.Quality.getLabels(), 1);
        renderQualityCombo.addValueChangedListener(listener);
        formPanel.add(renderQualityCombo);

        zoomIncrementSpinner = new NumberField("Zoom increment:", 0.1, 0.01, 1.0, 0.01);
        zoomIncrementSpinner.addValueChangedListener(listener);
        formPanel.add(zoomIncrementSpinner);

        enableMouseCursorField = new CheckBoxField("Enable mouse cursor", true);
        enableMouseCursorField.addValueChangedListener(listener);
        formPanel.add(enableMouseCursorField);

        enableMouseDraggingField = new CheckBoxField("Enable mouse dragging", true);
        enableMouseDraggingField.addValueChangedListener(listener);
        formPanel.add(enableMouseDraggingField);

        zoomOnClickField = new CheckBoxField("Zoom on mouse click", true);
        zoomOnClickField.addValueChangedListener(listener);
        formPanel.add(zoomOnClickField);

        zoomOnWheelField = new CheckBoxField("Zoom on mouse wheel", true);
        zoomOnWheelField.addValueChangedListener(listener);
        formPanel.add(zoomOnWheelField);

        return formPanel;
    }
}
