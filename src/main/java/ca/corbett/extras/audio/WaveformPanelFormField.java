package ca.corbett.extras.audio;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.Margins;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.ValueChangedListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;

/**
 * A ConfigPanel implementation for AudioWaveformPanel. Note that WaveformConfig
 * has its own ConfigPanel which should be used in conjunction with this one - this panel
 * is for configuring the AudioWaveformPanel options, such as control placement, editing
 * functions, and so on, while the WaveformConfigPanel is for viewing and
 * editing the cosmetic properties of the generated waveform (colour, scale, etc).
 *
 * @author scorbett
 * @since 2018-01-26
 */
public class WaveformPanelFormField extends FormField {

    private final JPanel wrapperPanel;
    private final FormPanel formPanel;
    private String labelText;
    private boolean useTitleBorder;
    private boolean shouldExpand = true;

    private ComboField<String> controlTypeCombo;
    private ComboField<String> controlSizeCombo;
    private ComboField<String> controlPositionCombo;

    public WaveformPanelFormField(String label) {
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

    public WaveformPanelFormField setShouldExpand(boolean should) {
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
        // ask the parent to validate also, in case we have any FieldValidators assigned here
        return super.validate() && formPanel.isFormValid();
    }

    /**
     * Decides whether to use a titled border around this field component (the default), or
     * to use a traditional FormField field label instead.
     */
    public WaveformPanelFormField setUseTitleBorder(boolean use) {
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

    public WaveformPanelFormField setFieldLabelText(String text) {
        labelText = text;
        if (useTitleBorder && (wrapperPanel.getBorder() instanceof TitledBorder)) {
            ((TitledBorder)wrapperPanel.getBorder()).setTitle(labelText);
        }
        else if (!useTitleBorder) {
            fieldLabel.setText(labelText);
        }
        return this;
    }

    public WaveformPanelFormField setControlType(AudioWaveformPanel.ControlType controlType) {
        if (getControlType() == controlType || controlType == null) {
            return this; // reject no-op requests and null
        }
        controlTypeCombo.setSelectedItem(controlType.toString());
        return this;
    }

    public AudioWaveformPanel.ControlType getControlType() {
        return AudioWaveformPanel.ControlType.fromLabel(controlTypeCombo.getSelectedItem()).orElse(
                AudioWaveformPanel.ControlType.ALLOW_ALL);
    }

    public WaveformPanelFormField setControlSize(AudioWaveformPanel.ControlSize controlSize) {
        if (getControlSize() == controlSize || controlSize == null) {
            return this; // reject no-op requests and null
        }
        controlSizeCombo.setSelectedItem(controlSize.toString());
        return this;
    }

    public AudioWaveformPanel.ControlSize getControlSize() {
        return AudioWaveformPanel.ControlSize.fromLabel(controlSizeCombo.getSelectedItem()).orElse(
                AudioWaveformPanel.ControlSize.NORMAL);
    }

    public WaveformPanelFormField setControlPosition(AudioWaveformPanel.ControlPosition controlPosition) {
        if (getControlPosition() == controlPosition) {
            return this; // reject no-op requests and null
        }
        controlPositionCombo.setSelectedItem(controlPosition.toString());
        return this;
    }

    public AudioWaveformPanel.ControlPosition getControlPosition() {
        return AudioWaveformPanel.ControlPosition.fromLabel(controlPositionCombo.getSelectedItem()).orElse(
                AudioWaveformPanel.ControlPosition.SIDE_EDGES);
    }

    private FormPanel createFormPanel() {
        FormPanel formPanel = new FormPanel();

        ValueChangedListener listener = field -> fireValueChangedEvent();
        Margins fieldMargins = new Margins(6, 2, 6, 2, 4);

        controlTypeCombo = new ComboField<>("Controls type:", AudioWaveformPanel.ControlType.getLabels(), 3);
        controlTypeCombo.setMargins(fieldMargins);
        controlTypeCombo.addValueChangedListener(listener);
        formPanel.add(controlTypeCombo);

        controlSizeCombo = new ComboField<>("Controls size:", AudioWaveformPanel.ControlSize.getLabels(), 2);
        controlSizeCombo.setMargins(fieldMargins);
        controlSizeCombo.addValueChangedListener(listener);
        formPanel.add(controlSizeCombo);

        controlPositionCombo = new ComboField<>("Controls position:", AudioWaveformPanel.ControlPosition.getLabels(),
                                                3);
        controlPositionCombo.setMargins(fieldMargins);
        controlPositionCombo.addValueChangedListener(listener);
        formPanel.add(controlPositionCombo);

        return formPanel;
    }
}
